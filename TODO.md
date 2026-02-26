# Bank-of-Java — Production Roadmap

## Architecture Summary
| Layer | Technology | Hosting |
|---|---|---|
| API Gateway | Custom Spring Boot (REST in, gRPC out) + oauth2-resource-server | ECS Fargate |
| Microservices | Spring Boot 3.5.x + gRPC | ECS Fargate |
| Databases | PostgreSQL (per service) + MongoDB (analytics) | AWS RDS (Multi-AZ) + DocumentDB |
| Messaging | Apache Kafka (KRaft, no Zookeeper) | AWS MSK Serverless |
| Auth | Custom JWT (RS256, JJWT 0.12.6) + JWKS endpoint | ECS Fargate |
| Container Registry | Docker images | Amazon ECR |
| CI/CD | GitHub Actions → ECR → ECS rolling deploy | GitHub Actions |
| Observability | Micrometer + CloudWatch + structured JSON logs + Zipkin | AWS CloudWatch |
| Local Dev | Docker Compose (Kafka, PostgreSQL, MongoDB, Zipkin) | localhost |

## Service Map
| Service | REST Port | gRPC Port | DB | Status |
|---|---|---|---|---|
| customer-service | 3001 | 9090 | customerdb (PostgreSQL, 5000) | COMPLETE |
| account-service | 3002 | 9091 | accountdb (PostgreSQL, 5001) | COMPLETE |
| payment-service | 3003 | 9092 | paymentdb (PostgreSQL, 5002) | COMPLETE |
| notification-service | 3004 | — | notificationdb (PostgreSQL, 5003) | COMPLETE |
| analytics-service | 3005 | — | analyticsdb (MongoDB, 27017) | COMPLETE |
| auth-service | 3006 | — | authdb (PostgreSQL, 5005) | COMPLETE |
| api-gateway | 8080 | — | — | COMPLETE |

---

## Milestone 1 — Payment Service (Complete)
> Goal: payment-service is fully functional, tested, and Kafka-enabled.

### 1.1 gRPC Server
Payment-service must expose a gRPC server so the API Gateway can call it internally.
The pattern across all services: API Gateway accepts REST → translates to gRPC → calls the target service.

- [x] Add `net.devh:grpc-server-spring-boot-starter:3.1.0.RELEASE` to `payment-service/pom.xml`
- [x] Set `grpc.server.port=9092` in `application.yml`
- [x] Create `PaymentGrpcService.java` in `GRPC/` package:
  - Extend `PaymentServiceGrpc.PaymentServiceImplBase` (generated from `payment.proto`)
  - Annotate with `@GrpcService`
  - Implement `processPayment()` — delegate to `PaymentService.processPayment()`, map proto ↔ DTO
  - Implement `getPaymentById()` — delegate to `PaymentService.getPayment()`
  - Implement `getPaymentsByAccount()` — delegate to `PaymentService.getPaymentsByAccount()`
  - Use `responseObserver.onNext()` + `responseObserver.onCompleted()` for success
  - Catch exceptions and call `responseObserver.onError(Status.NOT_FOUND.withDescription(...).asRuntimeException())`
- [x] Create `GlobalGrpcExceptionHandler.java` in `Exception/` package:
  - `@GrpcAdvice` class
  - Handle `PaymentNotFoundException` → `Status.NOT_FOUND`
  - Handle `InsufficientFundsException` → `Status.FAILED_PRECONDITION`
  - Handle `AccountNotActiveException` → `Status.FAILED_PRECONDITION`
  - Handle generic `Exception` → `Status.INTERNAL`

**Requirement:** All gRPC error responses must use proper `Status` codes. Never let unhandled exceptions reach the client as opaque `UNKNOWN` errors.

### 1.2 Kafka Event Publishing
After a payment completes (or fails), publish an event to Kafka so downstream services can react asynchronously. This decouples notification and analytics from the payment flow.

- [x] Create `PaymentEventDTO.java` in `DTO/` package:
  - Fields: `paymentId` (String), `fromAccountId`, `toAccountId`, `amount`, `status` (String), `type` (String), `occurredAt` (String ISO-8601)
  - This is the event contract — downstream consumers will deserialize this
- [x] Create `PaymentEventProducer.java` in `Kafka/` package:
  - Inject `KafkaTemplate<String, PaymentEventDTO>`
  - Method: `publishPaymentEvent(PaymentEventDTO event)` — sends to topic from `@Value("${kafka.topic.payment-processed}")`
  - Use the `paymentId` as the Kafka message key (ensures ordering per payment)
  - Log on success and failure
- [x] Wire `PaymentEventProducer` into `PaymentService.processPayment()`:
  - Publish after `status = COMPLETED` and after `status = FAILED` (both outcomes are events)
  - Wrap in try/catch — a Kafka failure must NOT roll back the payment. Log the error instead.
    The payment already succeeded; losing the event is preferable to undoing the transaction.

**Requirement:** Kafka publishing is fire-and-forget from the payment perspective. Do not make the payment outcome dependent on Kafka availability.

### 1.3 Unit Tests
- [x] `PaymentMapperTest.java` — `toEntity()` happy path, `toDTO()` happy path
- [x] `PaymentServiceTest.java` — mock `paymentRepo`, `AccountServiceGrpcClient`, `PaymentEventProducer`:
  - `getAllPayments_shouldReturnList`
  - `getPayment_shouldReturnDTO_whenFound`
  - `getPayment_shouldThrow_whenNotFound`
  - `getPaymentsByAccount_shouldReturnList`
  - `processPayment_shouldComplete_whenValidRequest`
  - `processPayment_shouldThrow_whenFromAccountInactive`
  - `processPayment_shouldThrow_whenToAccountInactive`
  - `processPayment_shouldThrow_whenInsufficientFunds`
  - `processPayment_shouldMarkFailed_whenAccountUpdateFails`
  - `processPayment_shouldReverseDebit_whenCreditFails`
  - `processPayment_shouldPublishEvent_afterCompletion`
  - `processPayment_shouldPublishFailedEvent_afterFailure`
- [x] `PaymentControllerTest.java` — `@WebMvcTest` + `MockMvc`:
  - `GET /api/payments` → 200 list
  - `GET /api/payments/{id}` → 200 single
  - `GET /api/payments/{id}` → 404 when not found
  - `GET /api/payments/account/{accountId}` → 200 list
  - `POST /api/payments` → 200 with valid body
  - `POST /api/payments` → 400 with missing required fields

**Requirement:** All tests must pass with `mvn clean verify`. No mocking of the class under test.

---

## Milestone 2 — Notification Service
> Goal: notification-service consumes payment events from Kafka and sends alerts.

### 2.1 Domain Model
- [ ] Create `Notification.java` entity:
  - Fields: `id` (UUID), `customerId` (String), `paymentId` (String), `type` (enum: PAYMENT_SUCCESS, PAYMENT_FAILED), `message` (String), `sentAt` (LocalDateTime), `channel` (enum: EMAIL, SMS)
  - `@PrePersist` sets `sentAt`
- [ ] Create `NotificationRequestDTO.java`, `NotificationResponseDTO.java`
- [ ] Create `notificationRepo.java` extending `JpaRepository<Notification, UUID>`
- [ ] Create `NotificationMapper.java` — static `toEntity()`, `toDTO()`

### 2.2 Kafka Consumer
- [ ] Add `spring-kafka` dependency to `pom.xml` (already in parent if using bom)
- [ ] Create `PaymentEventConsumer.java` in `Kafka/` package:
  - `@Component` with `@KafkaListener(topics = "${kafka.topic.payment-processed}", groupId = "notification-group")`
  - Deserialize incoming message as `PaymentEventDTO` (copy the DTO class or share via proto-config)
  - On `PAY_COMPLETED`: call `NotificationService.sendPaymentSuccessNotification()`
  - On `PAY_FAILED`: call `NotificationService.sendPaymentFailedNotification()`
  - Log all received events
- [ ] Configure `application.yml`:
  ```yaml
  spring:
    kafka:
      bootstrap-servers: ${KAFKA_BOOTSTRAP_SERVERS:localhost:9092}
      consumer:
        group-id: notification-group
        auto-offset-reset: earliest
        key-deserializer: org.apache.kafka.common.serialization.StringDeserializer
        value-deserializer: org.springframework.kafka.support.serializer.JsonDeserializer
        properties:
          spring.json.trusted.packages: "com.pm.*"
  ```

### 2.3 Notification Service & Controller
- [ ] Create `NotificationService.java`:
  - `sendPaymentSuccessNotification(PaymentEventDTO event)` — build message, save to DB, log (mock send)
  - `sendPaymentFailedNotification(PaymentEventDTO event)` — build message, save to DB, log (mock send)
  - `getNotificationsByCustomer(String customerId)` — for audit trail endpoint
- [ ] Create `NotificationController.java`:
  - `GET /api/notifications/customer/{customerId}` — returns audit log of all notifications for a customer
- [ ] Create `GlobalExceptionHandler.java` with standard exception handling

### 2.4 Exception & Configuration
- [ ] `NotificationNotFoundException.java`
- [ ] `application.yml`: port 3004, DB on `notificationdb` schema

**Requirement:** The consumer must be idempotent — if the same event is consumed twice (Kafka at-least-once delivery), do not send duplicate notifications. Use the `paymentId` as a deduplication key.

---

## Milestone 3 — Analytics Service
> Goal: analytics-service consumes all events and exposes aggregated metrics.

### 3.1 Domain Model
- [x] Create `PaymentEventRecord.java` — MongoDB `@Document(collection = "payment_events")`:
  - Fields: `id` (String), `paymentId` (String, `@Indexed(unique = true)`), `fromAccountId`, `toAccountId`, `amount` (BigDecimal/Decimal128), `status`, `type`, `occurredAt`, `recordedAt` (LocalDateTime)
- [x] Create `paymentEventRepo.java` extending `MongoRepository<PaymentEventRecord, String>`:
  - Spring Data derives `existsByPaymentId()` and `countByStatus()` from method names
  - Complex aggregations (sum, daily group-by, top-N) done via `MongoTemplate` in service layer

### 3.2 Kafka Consumer
- [x] Create `PaymentEventConsumer.java` — same pattern as notification-service consumer
- [x] Persist each event to MongoDB without calling any other service (idempotent via `existsByPaymentId`)
- [x] Configure `application.yml` with `group-id: analytics-group`, MongoDB URI, `spring.json.use.type.headers: false`

### 3.3 Analytics API
- [x] Create `AnalyticsService.java` — uses `MongoTemplate` aggregation pipeline for:
  - `getSummary()` — total payments, total volume (`$sum`), success rate
  - `getDailyVolume(from, to)` — `$match → $project dateToString → $group → $sort`
  - `getTopAccounts(limit)` — `$group by fromAccountId → $sort desc → $limit N`
- [x] Create `AnalyticsController.java`:
  - `GET /api/analytics/summary`
  - `GET /api/analytics/volume?from=YYYY-MM-DD&to=YYYY-MM-DD`
  - `GET /api/analytics/top-accounts?limit=10`
- [x] Create `AnalyticsSummaryDTO.java`, `DailyVolumeDTO.java`, `TopAccountDTO.java`

### 3.4 Unit Tests (14/14 passing)
- [x] `AnalyticsServiceTest.java` — mocks `paymentEventRepo` + `MongoTemplate`; covers idempotency, empty collection, date range validation, top-accounts
- [x] `AnalyticsControllerTest.java` — `@WebMvcTest`; covers all three endpoints + 400 on bad date range
- [x] `PaymentEventConsumerTest.java` — verifies consumer delegates to service

### 3.5 Infrastructure
- [x] `analyticsdb` Docker container: `mongo:7` on port `27017:27017` in `docker-compose.yml`

### 3.6 Observability (deferred to Milestone 8)
- [ ] Expose Micrometer metrics via `/actuator/prometheus`
- [ ] Add custom counter: `payments.processed.count` tagged by `status`

---

## Milestone 4 — Auth Service
> Goal: issue and validate JWTs for all protected endpoints.

### 4.1 Architecture Decision
Two options exist. Choose one before starting:

**Option A — Keycloak (recommended for production portfolio):**
- Run Keycloak in Docker Compose locally, on ECS Fargate in AWS
- Realm: `banking`, clients: `gateway`, roles: `CUSTOMER`, `ADMIN`
- API Gateway validates JWT against Keycloak's public key
- No custom auth code needed in individual services

**Option B — Custom JWT (simpler, faster to build):**
- auth-service issues JWTs signed with an RS256 private key
- API Gateway validates JWT using the auth-service's public key endpoint
- More code to write but less infrastructure dependency

**Recommendation:** Option A for production. Option B is acceptable for portfolio if time-constrained.

### 4.2 If Building Custom Auth Service
- [x] Add `jjwt-api`, `jjwt-impl`, `jjwt-jackson` (0.12.6) to `pom.xml`
- [x] Create `User.java` entity: `id`, `email`, `passwordHash`, `role` (enum: CUSTOMER, ADMIN) — `@Table(name = "Users")` to avoid PostgreSQL reserved word
- [x] Create `AuthController.java`:
  - `POST /api/auth/register` — hash password with BCrypt (strength 12), create user, return JWT
  - `POST /api/auth/login` — validate credentials, return JWT + refresh token
  - `POST /api/auth/refresh` — validate refresh token, issue new JWT
- [x] Create `JwtService.java`:
  - RS256 asymmetric signing — private key signs, public key verifies
  - `@PostConstruct` generates RSA key pair at startup
  - Access token: 15 min, refresh token: 7 days
  - Expose `GET /api/auth/.well-known/jwks.json` — RSA public key as JWKS for gateway
- [x] Create `SecurityConfig.java`:
  - Permit `/api/auth/**` without auth
  - All other endpoints require valid JWT
- [x] Unit tests: JwtServiceTest (5), AuthServiceTest (7), AuthControllerTest (7) — 19/19 passing

**Requirement:** Never store plain-text passwords. Use `BCryptPasswordEncoder` with strength 12.

---

## Milestone 5 — API Gateway
> Goal: single external entry point — accepts REST, routes to services via gRPC, enforces auth.

### 5.1 Architecture
The gateway is the only service that speaks REST to the outside world. It translates incoming REST requests to gRPC calls on the target service, validates JWTs, enforces rate limiting, and returns REST responses.

```
Client (REST/JSON)
      ↓
API Gateway (port 8080)
      ↓ JWT validation
      ↓ rate limiting
      ↓ gRPC call
Target Service (gRPC)
      ↓ response
API Gateway
      ↓ REST response
Client
```

### 5.2 Implementation
- [x] Add `net.devh:grpc-client-spring-boot-starter:3.1.0.RELEASE` to `pom.xml`
- [x] Add `spring-boot-starter-oauth2-resource-server` — validates JWT via JWKS URI (no shared secret)
- [x] Add `spring-boot-starter-web`
- [x] Configure gRPC clients in `application.yml` with env var overrides per environment
- [x] Create gateway controllers (no separate GrpcClient classes — stubs injected directly with `@GrpcClient`):
  - `CustomerGatewayController` — `/api/customers/**` → customer-service gRPC (9090)
  - `AccountGatewayController` — `/api/accounts/**` → account-service gRPC (9091)
  - `PaymentGatewayController` — `/api/payments/**` → payment-service gRPC (9092)
  - `AnalyticsGatewayController` — `/api/analytics/**` → analytics-service REST (RestTemplate)
- [x] Create `SecurityConfig.java`:
  - Validates JWT via JWKS URI pointing to auth-service `/.well-known/jwks.json`
  - Custom role claim converter (extracts `role` claim → `ROLE_CUSTOMER` / `ROLE_ADMIN`)
  - `/api/analytics/**` and `/actuator/**` require `ROLE_ADMIN`
- [x] Create `GlobalExceptionHandler.java` — maps `StatusRuntimeException` → HTTP status codes
- [ ] Create `RateLimitFilter.java` (deferred — use AWS WAF in production)

### 5.3 CORS
- [ ] Configure CORS to allow the frontend origin only
- [ ] Never use `allowedOrigins("*")` in production

**Requirement:** The gateway is the only service with a public-facing port in AWS. All other services run in a private VPC subnet with no public IP.

---

## Milestone 6 — Local Development Environment
> Goal: `docker-compose up` brings the entire platform online with no manual setup.

### 6.1 docker-compose.yml
- [x] Infrastructure containers: customerdb, accountdb, paymentdb, notificationdb, authdb (PostgreSQL), analyticsdb (MongoDB), kafka (KRaft), zipkin
- [x] App service containers: all 7 services with env vars, depends_on, and health checks
- [x] Kafka dual-listener setup: INTERNAL (kafka:9092) for container-to-container, EXTERNAL (localhost:9094) for host access
- [x] Multi-stage Dockerfiles per service (builder stage with Maven, runtime stage with JRE only):
  - `customer.Dockerfile`, `account.Dockerfile`, `payment.Dockerfile`
  - `notification.Dockerfile`, `analytics.Dockerfile`, `auth.Dockerfile`, `gateway.Dockerfile`
  - Services that depend on proto-config build and install it first in Stage 1
- [x] All DB URLs use `${DB_HOST:localhost}:${DB_PORT:xxxx}` env vars — no hardcoded addresses
- [x] All `@Entity` classes have explicit `@Table(name = "...")` to avoid SQL reserved word conflicts

### 6.2 Environment Variable Strategy
Every service must be configurable purely via environment variables (12-factor app).
No hardcoded hostnames, ports, or credentials in `application.yml`.
Use `${ENV_VAR:default}` syntax so local dev still works without setting every variable.

| Variable | Used By | Example |
|---|---|---|
| `DB_HOST` | All services | `localhost` / `rds-endpoint.aws.com` |
| `DB_PORT` | All services | `5432` |
| `DB_NAME` | Each service | `paymentdb` |
| `DB_USER` | Each service | `admin` |
| `DB_PASSWORD` | Each service | set via Secrets Manager in AWS |
| `KAFKA_BOOTSTRAP_SERVERS` | payment, notification, analytics | `localhost:9092` / `msk-endpoint:9092` |
| `GRPC_CUSTOMER_ADDRESS` | account-service, api-gateway | `localhost` / `customer-service` |
| `GRPC_ACCOUNT_ADDRESS` | payment-service, api-gateway | `localhost` / `account-service` |
| `GRPC_PAYMENT_ADDRESS` | api-gateway | `localhost` / `payment-service` |
| `JWT_SECRET` / `JWKS_URI` | api-gateway, auth-service | Secrets Manager value |

**Requirement:** `application.yml` must never contain a raw password, secret, or non-localhost address.

---

## Milestone 7 — Database Migrations (Complete)
> Goal: schema changes are versioned, repeatable, and safe to run in production.

- [x] Add `flyway-core` + `flyway-database-postgresql` to every PostgreSQL service's `pom.xml`
- [x] Remove `spring.jpa.hibernate.ddl-auto: update` from all `application.yml` files — replace with `validate`
- [x] Create `src/main/resources/db/migration/` in each service
- [x] V1 migrations: customer-service, account-service, payment-service, notification-service, auth-service
- [x] V2 migration for auth-service: rename "Users" → users (PostgreSQL case-sensitivity fix)
- [x] Naming convention: `V{number}__{description}.sql`

**Why:** `ddl-auto: update` silently drops columns on rename, cannot be rolled back, and is dangerous in production. Flyway gives you a traceable, tested history of every schema change.

---

## Milestone 8 — Observability
> Goal: every service emits structured logs, metrics, and traces that surface in AWS CloudWatch.

### 8.1 Structured Logging
- [ ] Add `logstash-logback-encoder` to every service `pom.xml`
- [ ] Create `src/main/resources/logback-spring.xml` in each service:
  - Production profile: JSON output via `LogstashEncoder`
  - Dev profile: human-readable console output
- [ ] Never log PII: mask account numbers, email addresses, and balance values in logs
  - Use a log filter or MDC to scrub sensitive fields

### 8.2 Health & Metrics
- [ ] Add `spring-boot-starter-actuator` to every service
- [ ] Configure in `application.yml`:
  ```yaml
  management:
    endpoints:
      web:
        exposure:
          include: health,info,prometheus
    endpoint:
      health:
        show-details: when-authorized
  ```
- [ ] Add `micrometer-registry-cloudwatch2` for AWS metrics export in production profile

### 8.3 Distributed Tracing
- [ ] Add `micrometer-tracing-bridge-brave` + `zipkin-reporter-brave` to each service
- [ ] Configure Zipkin endpoint in `application.yml`:
  ```yaml
  management:
    tracing:
      sampling:
        probability: 1.0  # 100% in dev, reduce to 0.1 in prod
  spring:
    zipkin:
      base-url: ${ZIPKIN_URL:http://localhost:9411}
  ```
- [ ] Verify trace IDs propagate across gRPC calls (requires `grpc-brave` instrumentation)

---

## Milestone 9 — Security Hardening
> Goal: the system is secure by default, not secure by accident.

### 9.1 Transport Security
- [ ] **Local dev:** plaintext gRPC is acceptable (`usePlaintext()`)
- [ ] **Production:** enable TLS on all gRPC channels
  - Use AWS ACM certificates or self-signed certs rotated via Secrets Manager
  - Replace `usePlaintext()` with `.useTransportSecurity()` and configure trust store
- [ ] API Gateway must use HTTPS only — terminate TLS at the AWS ALB

### 9.2 Input Validation
- [ ] All DTOs must have `@Valid` on every controller method that accepts a request body
- [ ] Validate: `@NotBlank`, `@NotNull`, `@Size`, `@DecimalMin("0.01")` on amounts
- [ ] Never trust data that arrives via Kafka — validate before persisting

### 9.3 Authorization
- [ ] Customers can only access their own accounts and payments (enforce `customerId` ownership check)
- [ ] Admin role required for: all-customer list, analytics, deleting accounts
- [ ] Never expose internal IDs in error messages returned to the client

### 9.4 Secrets
- [ ] Use AWS Secrets Manager for all credentials in production
- [ ] Use Spring Cloud AWS or the AWS SDK to load secrets at startup
- [ ] Never commit `.env` files or secrets to git — add to `.gitignore`

---

## Milestone 10 — CI/CD Pipeline
> Goal: every push to `main` builds, tests, packages, and deploys automatically.

### 10.1 GitHub Actions — Build & Test (`.github/workflows/ci.yml`)
Trigger: `push` or `pull_request` to `main`

- [ ] Steps:
  1. Checkout code
  2. Set up Java 21 (temurin)
  3. Cache Maven dependencies
  4. `mvn clean verify` — builds and runs all unit tests for every service
  5. Report test results
- [ ] Fail fast: if any service fails tests, stop the pipeline

### 10.2 GitHub Actions — Build & Push Docker Images (`.github/workflows/cd.yml`)
Trigger: push to `main` (after CI passes)

- [ ] Steps:
  1. Configure AWS credentials via `aws-actions/configure-aws-credentials`
  2. Login to Amazon ECR
  3. For each service: `docker build`, `docker tag`, `docker push` to ECR
  4. Tag images with `git SHA` (not `latest` — `latest` is not traceable)

### 10.3 GitHub Actions — Deploy to ECS (`.github/workflows/cd.yml` continued)
- [ ] Steps:
  1. Download current ECS task definition
  2. Update image URI in task definition to new ECR image
  3. Register new task definition revision
  4. Update ECS service to use new task definition (rolling deploy)
  5. Wait for service to stabilize — fail pipeline if deploy does not complete

**Requirement:** All secrets (AWS credentials, DB passwords) must be stored in GitHub Actions Secrets, never in workflow YAML files.

---

## Milestone 11 — AWS Infrastructure
> Goal: full production-grade deployment on AWS ECS Fargate.

### 11.1 Networking
- [ ] Create a VPC with:
  - 2 public subnets (for ALB)
  - 2 private subnets (for ECS tasks and RDS)
- [ ] Internet Gateway attached to public subnets
- [ ] NAT Gateway in each public subnet (for outbound traffic from private subnets)
- [ ] Security Groups:
  - ALB SG: allow 80, 443 inbound from internet
  - ECS SG: allow traffic only from ALB SG and within ECS SG (for inter-service gRPC)
  - RDS SG: allow 5432 only from ECS SG
  - MSK SG: allow 9092 only from ECS SG

### 11.2 RDS (PostgreSQL)
- [ ] Single `db.t3.medium` RDS instance (Multi-AZ for production)
- [ ] Create each service's database as a separate schema on the same instance
- [ ] Enable automated backups (7-day retention)
- [ ] Store master password in AWS Secrets Manager

### 11.3 MSK Serverless (Kafka)
- [ ] Create MSK Serverless cluster in private subnets
- [ ] Create topics: `payment-processed`
- [ ] Configure retention: 7 days, replication factor: 3
- [ ] IAM authentication (no username/password — use IAM roles on ECS tasks)
- [ ] Update `application.yml` to support SASL/IAM auth in production profile

### 11.4 ECS Fargate
- [ ] Create one ECS Cluster
- [ ] One ECS Service per microservice (7 services + api-gateway)
- [ ] Task definition per service:
  - CPU: 256, Memory: 512 (scale up as needed)
  - Environment variables injected from SSM Parameter Store or Secrets Manager
  - CloudWatch log group per service
- [ ] Application Load Balancer:
  - One ALB for the api-gateway only
  - Target group: api-gateway ECS service on port 8080
  - Internal ALBs or Service Discovery for gRPC inter-service communication
- [ ] AWS Cloud Map (Service Discovery):
  - Register each service as `service-name.banking.local`
  - api-gateway resolves `customer-service.banking.local:9090` etc.

### 11.5 Auto Scaling
- [ ] Configure ECS auto-scaling per service:
  - Metric: CPU utilization
  - Scale out at 70%, scale in at 30%
  - Min: 1, Max: 4 tasks per service (adjust per load testing results)

---

## Milestone 12 — Integration Tests
> Goal: validate the full system works end-to-end before deploying.

- [ ] Add `testcontainers-bom` to root `pom.xml`
- [ ] Create integration test module or `src/test` profile per service
- [ ] For each service, create `*IntegrationTest.java` annotated with `@SpringBootTest`:
  - Use `@Testcontainers` with `PostgreSQLContainer` for real DB
  - Use `@EmbeddedKafka` for Kafka tests in notification/analytics
  - Test the full request → service → DB → Kafka flow
- [ ] Create an end-to-end test suite (separate module) that calls the API Gateway REST endpoints and validates responses

---

## Milestone 13 — Frontend (In Progress — Angular)
> Goal: a working Angular dashboard that uses the API Gateway.

### 13.1 Core Pages (Complete)
- [x] Angular 19 project with Angular Material (azure/blue theme)
- [x] Login page — email/password → JWT stored in localStorage
- [x] Register page — auth register + customer profile creation (chained)
- [x] Dashboard — fetch customer by email → fetch accounts by customerId → display cards
- [x] Payment page — send payment form (from/to account, amount, type, description)
- [x] Create Account page — name + type (Checking/Savings), auto-resolves customerId from JWT

### 13.2 Auth & Security (Complete)
- [x] AuthService — login, register, getEmailFromToken, isLoggedIn
- [x] Auth interceptor — attaches Bearer token, handles 401 → redirect to login
- [x] Auth guard — protects /dashboard, /payment, /create-account routes
- [x] Environment config — `environment.ts` (dev) / `environment.prod.ts` (Docker)

### 13.3 Navigation (Complete)
- [x] Material toolbar navbar — conditional links based on auth state
- [x] Bank of Java link routes to /dashboard (logged in) or /login (logged out)

### 13.4 UX Improvements (TODO)
- [ ] Form validation on all forms (required fields, email format, password strength)
- [ ] Loading spinners while API calls are in flight
- [ ] Global error handling with Material snackbar/toast
- [ ] 404 page for unknown routes
- [ ] Disable submit buttons while submitting (prevent double-click)
- [ ] Active route highlighting in navbar

### 13.5 Missing Pages (TODO)
- [ ] Payment history — list past transactions per account
- [ ] Account detail page — full info, transaction history, wire up "View Details" button
- [ ] Customer profile page — view/edit personal info
- [ ] Analytics admin page — summary, volume chart, top accounts (ROLE_ADMIN only)

### 13.6 Code Quality (TODO)
- [ ] TypeScript interfaces for all API responses (replace `any`)
- [ ] RxJS best practices (switchMap over nested subscribes)
- [ ] Environment-aware API URLs (done)
- [ ] Meaningful unit tests for services and components

---

## Milestone 14 — Transaction Ledger Service
> Goal: immutable double-entry ledger — the foundation of real banking.

Every financial event creates a DEBIT and CREDIT entry. Account balances are derived from the ledger, never stored directly.

- [ ] Domain: `LedgerEntry` (id, transactionId, accountId, type DEBIT/CREDIT, amount, runningBalance, createdAt)
- [ ] Double-entry on every transaction: debit source + credit destination
- [ ] Transaction statuses: PENDING → POSTED → CLEARED, or REVERSED
- [ ] Idempotency via unique transactionId per request
- [ ] Paginated transaction history with date/type/amount filters
- [ ] End-of-day reconciliation job (compare ledger totals vs account balances)
- [ ] Refactor account-service: balance becomes a derived value from ledger, not a stored column

---

## Milestone 15 — Card Service
> Goal: debit/credit card lifecycle management.

- [ ] Domain: `Card` (id, accountId, cardNumber, type DEBIT/CREDIT, status, expiryDate, dailyLimit)
- [ ] Card issuance — physical and virtual cards linked to accounts
- [ ] Card statuses: ACTIVE, FROZEN, LOST, STOLEN, EXPIRED, CANCELLED
- [ ] Card controls — toggle online purchases, international transactions, ATM
- [ ] Spending limits — daily and per-transaction caps
- [ ] Virtual cards — temporary numbers for online shopping
- [ ] PIN management (hashed, never stored plaintext)

---

## Milestone 16 — Loan & Credit Service
> Goal: lending products from application through repayment.

- [ ] Loan products: PERSONAL, MORTGAGE, AUTO, LINE_OF_CREDIT
- [ ] Application flow: collect income/employment → credit check → underwriting → approval/denial
- [ ] Loan origination: generate amortization schedule, set terms and rate
- [ ] Disbursement: fund loan to customer's account via ledger
- [ ] Repayment tracking: monthly payments, principal vs interest split
- [ ] Late payment: grace period, late fees, penalty interest
- [ ] Early payoff calculation

---

## Milestone 17 — Fraud Detection Service
> Goal: real-time threat detection on every transaction.

- [ ] Transaction risk scoring (rules engine or ML model)
- [ ] Velocity checks — flag unusual frequency (N transactions in X minutes)
- [ ] Amount anomaly — flag transactions that deviate from customer's pattern
- [ ] Geo anomaly — transaction in two distant locations in short timeframe
- [ ] Device/IP anomaly — new device, VPN/proxy detection
- [ ] Alert generation for human review
- [ ] Case management workflow for investigators
- [ ] Account takeover detection (credential stuffing, brute force)

---

## Milestone 18 — Compliance & AML Service
> Goal: regulatory compliance — anti-money laundering, KYC, reporting.

- [ ] AML screening — check customers against OFAC, PEP, sanctions lists
- [ ] Transaction monitoring — flag structuring, smurfing, rapid movement patterns
- [ ] SAR filing — Suspicious Activity Reports
- [ ] CTR filing — Currency Transaction Reports (cash > $10,000)
- [ ] CDD / EDD — Customer Due Diligence, Enhanced Due Diligence for high-risk
- [ ] Regulatory holds — freeze accounts on legal/government orders
- [ ] Data retention — 5+ year minimum per BSA regulations
- [ ] Full audit trail on every compliance decision

---

## Milestone 19 — Document Service
> Goal: statement generation, tax docs, KYC document storage.

- [ ] Monthly/quarterly account statement generation (PDF)
- [ ] Tax documents — 1099-INT, 1098 (mortgage interest)
- [ ] KYC document storage — uploaded ID photos, proof of address
- [ ] Loan documents — promissory notes, disclosures, amortization schedules
- [ ] Document retrieval — customers download past statements from app
- [ ] Retention policy — auto-archive/delete per regulatory schedule

---

## Milestone 20 — Fee & Interest Service
> Goal: automated fee calculation and interest accrual.

- [ ] Account fees — monthly maintenance, paper statement, wire transfer fees
- [ ] Overdraft fees — NSF charges, overdraft protection fees
- [ ] Interest accrual — daily calculation, monthly posting to ledger
- [ ] Interest rate tiers — higher rate for higher balances
- [ ] Fee waivers — auto-waive if minimum balance met or customer tier qualifies
- [ ] Promotional rates — introductory APY for new accounts

---

## Milestone 21 — Support & Dispute Service
> Goal: customer issue tracking and chargeback workflow.

- [ ] Ticket creation — customer submits issue via app
- [ ] Dispute filing — chargeback / unauthorized transaction dispute
- [ ] Provisional credit — temporary credit while dispute is investigated
- [ ] Investigation workflow — gather evidence, contact merchant, resolve
- [ ] Resolution — credit, deny, partial credit
- [ ] Regulatory timelines — Reg E: 10 business days to investigate

---

## Milestone 22 — Enhanced Auth (MFA, Device Trust, Sessions)
> Goal: production-grade authentication beyond basic JWT.

- [ ] MFA — TOTP (Google Authenticator), SMS OTP, email OTP
- [ ] Device fingerprinting — track trusted devices, flag new ones
- [ ] Session management — active sessions list, remote logout
- [ ] Password policy — min length, complexity, breach database check
- [ ] Account lockout — lock after N failed attempts, unlock via timer or support
- [ ] Fine-grained RBAC — per-action permissions (can_transfer, can_approve_loans, etc.)

---

## Final Production Checklist
Before calling this production-ready:

**Code Hygiene**
- [ ] Disable or remove REST controllers in all internal microservices — in production
      these are never called (API Gateway routes exclusively via gRPC). Services affected:
      - [ ] customer-service — disable `CustomerController.java`
      - [ ] account-service  — disable `AccountController.java`
      - [ ] payment-service  — disable `PaymentController.java`
      - [ ] notification-service — disable `NotificationController.java`
      - [ ] analytics-service   — disable `AnalyticsController.java`
      Approach: annotate each controller with `@Profile("!prod")` so it loads in
      dev/test but is excluded when `SPRING_PROFILES_ACTIVE=prod` is set in ECS.
      Do NOT delete them — they are needed for local development and debugging.

**Database**
- [x] All `ddl-auto: update` replaced with Flyway migrations
- [x] All hardcoded passwords/hosts removed from `application.yml` — use `${ENV_VAR:default}`

**Testing**
- [x] customer-service unit tests passing
- [x] account-service unit tests passing
- [x] payment-service unit tests passing (26/26)
- [x] notification-service unit tests passing (15/15)
- [x] analytics-service unit tests passing (14/14)
- [x] auth-service unit tests passing (19/19)
- [ ] Integration tests per service
- [ ] End-to-end API tests

**Observability**
- [x] All services have `/actuator/health` endpoint
- [ ] Structured JSON logging in production profile
- [ ] No PII in logs

**Security**
- [ ] gRPC TLS enabled in production
- [x] JWT auth (RS256) enforced on all non-public API Gateway routes
- [ ] All secrets in Secrets Manager, not in code or env files committed to git

**Infrastructure**
- [ ] GitHub Actions CI/CD pipeline running on every push to `main`
- [x] Docker Compose brings full stack up locally with `docker-compose up`
- [x] Dockerfiles written for all 7 services (multi-stage builds)
- [ ] AWS infrastructure is defined as code (CloudFormation or Terraform) — not click-ops
- [ ] RDS automated backups enabled
- [ ] MSK Serverless topic retention configured
- [ ] ECS auto-scaling configured
