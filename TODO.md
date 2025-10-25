# 🏦 Banking Platform – Full-Stack Microservice TODO

## 1. 📦 Project Setup
- [ ] Create mono-repo structure:
  banking-platform/
  ├── api-gateway/
  ├── auth-service/
  ├── customer-service/
  ├── account-service/
  ├── payments-service/
  ├── notifications-service/
  ├── analytics-service/
  ├── frontend/
  ├── proto/
  └── docker-compose.yml
- [ ] Configure `.gitignore` for Java, Node, IDEs, Docker, etc.
- [ ] Add root `pom.xml` with `<modules>` for microservices.
- [ ] Create shared `proto` directory for gRPC/Kafka events.
- [ ] Initialize Maven + Spring Boot 3.5.x for each microservice.
- [ ] Create individual Dockerfiles for each service.

## 2. ☁️ Infrastructure Layer
### 2.1 Local Development
- [ ] Add **docker-compose.yml** to spin up:
    - Kafka + Zookeeper (or KRaft)
    - PostgreSQL (separate DBs per service)
    - Redis
    - Keycloak (Auth)
    - Zipkin / OpenTelemetry Collector (tracing)
    - Grafana + Prometheus (metrics)
- [ ] Expose service ports:
    - Gateway → 8080
    - Customer → 3001
    - Account → 3002
    - Payments → 3003
    - Notifications → 3004
    - Analytics → 3005
    - Keycloak → 8081

### 2.2 Environment Variables
- [ ] Use `.env` or Docker secrets for DB credentials and Kafka hosts.
- [ ] Define common environment variables:
  SPRING_PROFILES_ACTIVE=dev  
  KAFKA_BOOTSTRAP_SERVERS=kafka:9092  
  POSTGRES_USER=app  
  POSTGRES_PASSWORD=app  
  REDIS_HOST=redis

## 3. 🔐 Authentication & Security
- [ ] Set up Keycloak realm `banking` with clients `frontend`, `gateway` and roles `CUSTOMER`, `ADMIN`.
- [ ] Configure Spring Security Resource Server for JWT validation.
- [ ] Implement `@PreAuthorize` roles for sensitive endpoints.
- [ ] Add Keycloak adapter to API Gateway for OAuth2 token validation.

## 4. ⚙️ API Gateway
- [ ] Create `api-gateway` using Spring Cloud Gateway.
- [ ] Define route mappings in `application.yml`:
  spring:
  cloud:
  gateway:
  routes:
  - id: customer
  uri: http://customer-service:3001
  predicates:
  - Path=/api/customers/**
  - id: payments
  uri: http://payments-service:3003
  predicates:
  - Path=/api/payments/**
- [ ] Add CORS, rate limiting and JWT filters.
- [ ] Add `/actuator/health` endpoint.

## 5. 👤 Customer Service
- [ ] Model → Customer(id, name, email, phone, createdAt)
- [ ] Endpoints:
    - POST /api/customers
    - GET /api/customers/{id}
    - PUT /api/customers/{id}
- [ ] Persist data in PostgreSQL (`customerdb`).
- [ ] Emit CustomerCreatedEvent to Kafka (`customers` topic).

## 6. 💰 Account Service
- [ ] Model → Account(id, customerId, balance, currency, status, createdAt) and Posting(id, txnId, accountId, amount, direction, timestamp)
- [ ] Implement double-entry ledger logic.
- [ ] Endpoints:
    - POST /api/accounts
    - GET /api/accounts/{id}
    - POST /api/accounts/{id}/debit
    - POST /api/accounts/{id}/credit
- [ ] Validate currency and balance.
- [ ] Emit AccountDebited / AccountCredited events to Kafka.

## 7. 💸 Payments Service
- [ ] Handle transfers between accounts.
- [ ] Model → Payment(id, fromAccountId, toAccountId, amount, currency, status, reference)
- [ ] Endpoints: POST /api/payments (with Idempotency-Key header)
- [ ] Use Outbox pattern → persist + publish PaymentInitiated event.
- [ ] Listen for AccountDebited / AccountCredited to confirm settlement.
- [ ] Emit PaymentSettled / PaymentFailed.

## 8. 🔔 Notifications Service
- [ ] Consume PaymentSettled, PaymentFailed, CustomerCreated.
- [ ] Integrate mock email / SMS service.
- [ ] Use Redis queue for retries.
- [ ] Provide /api/notifications for audit log.

## 9. 📊 Analytics Service
- [ ] Consume all domain events from Kafka.
- [ ] Store in PostgreSQL or Elasticsearch.
- [ ] Endpoint GET /api/analytics/summary.
- [ ] Aggregate → totals by currency, payment volume per day/week.
- [ ] Expose Prometheus metrics.

## 10. 🧠 Shared Protobuf Contracts
- [ ] Folder structure:
  proto/
  ├── customers.proto
  ├── accounts.proto
  ├── payments.proto
- [ ] Add protobuf-maven-plugin to each service.
- [ ] Example message:
  message PaymentInitiated {
  string eventId = 1;
  string fromAccountId = 2;
  string toAccountId = 3;
  int64 amount = 4;
  string currency = 5;
  int64 timestamp = 6;
  }

## 11. ⚙️ Kafka Configuration
- [ ] Topics → customers, accounts, payments, notifications, analytics
- [ ] Default Spring config:
  spring:
  kafka:
  bootstrap-servers: kafka:9092
  producer:
  key-serializer: org.apache.kafka.common.serialization.StringSerializer
  value-serializer: org.apache.kafka.common.serialization.ByteArraySerializer
  consumer:
  key-deserializer: org.apache.kafka.common.serialization.StringDeserializer
  value-deserializer: org.apache.kafka.common.serialization.ByteArrayDeserializer
  listener:
  type: single
  ack-mode: record

## 12. 🧪 Testing & QA
- [ ] Use Testcontainers for Kafka + Postgres integration tests.
- [ ] Write @SpringBootTest suites per service.
- [ ] End-to-end tests via RestAssured or Postman.
- [ ] Demo seed data for accounts/customers.

## 13. 🖥️ Frontend (React/Next.js)
- [ ] Init → npx create-next-app frontend
- [ ] Add Tailwind CSS and React Query.
- [ ] Pages → /login, /dashboard, /transfer, /analytics.
- [ ] Fetch via Gateway with JWT token.
- [ ] Store token in cookies (preferred).

## 14. 📈 Observability & Logging
- [ ] Include spring-boot-starter-actuator.
- [ ] Expose /health, /metrics, /prometheus.
- [ ] JSON logback configuration.
- [ ] Prometheus → Grafana dashboards.

## 15. 🚀 Deployment & CI/CD
- [ ] GitHub Actions workflow → build, test, docker push.
- [ ] Deploy to Kubernetes (minikube or dev cluster).
- [ ] Helm/Kustomize charts + HPA.
- [ ] HTTPS via NGINX Ingress and Cert Manager.

## 16. 🔐 Compliance & Hardening
- [ ] Mask PII in logs.
- [ ] TLS for service-to-service.
- [ ] Secret rotation policy.
- [ ] Rate limit + validation middleware.
- [ ] Follow DoD/NIST data guidelines.

## 17. 📄 Documentation
- [ ] Generate OpenAPI specs with springdoc-openapi.
- [ ] `.proto` → Markdown schema docs.
- [ ] README per service (outline and env vars).
- [ ] ARCHITECTURE.md with Mermaid/PlantUML diagrams.

## 18. ✅ Stretch Goals
- [ ] Fraud Detection microservice (ML scoring).
- [ ] WebSocket notifications to frontend.
- [ ] Currency conversion service (3rd-party API).
- [ ] OpenTelemetry tracing → Tempo/Zipkin.
- [ ] Sandbox developer API portal.

## 19. 🧩 Developer Experience
- [ ] Add Makefile shortcuts:
  run-dev: docker-compose up --build  
  test: mvn clean verify  
  proto: mvn protobuf:compile
- [ ] VS Code Dev Container config.
- [ ] Pre-commit lint + format.
- [ ] Flyway DB migrations on startup.

## 🏁 Final Milestone
Fully running banking system with:
• Keycloak auth  
• Independent Spring Boot microservices  
• Kafka event messaging  
• PostgreSQL persistence  
• Dockerized local setup  
• Next.js frontend with dashboards  
• Security + Observability integrated
