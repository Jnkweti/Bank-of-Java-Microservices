# Bank of Java

Microservices banking platform. 7 Spring Boot services talking over gRPC, Kafka for async event processing, JWT auth, Angular frontend. The whole thing spins up with `docker-compose up`.

Built this to learn microservice patterns hands-on — saga transactions, event-driven architecture, gRPC service mesh, the works.

## How it works

The API gateway is the only public-facing service. It takes REST requests from the frontend, translates them to gRPC calls to the appropriate backend service, and handles JWT validation. No service-to-service REST — everything internal is gRPC.

When a payment goes through, the payment service publishes an event to Kafka. Two consumers pick it up independently: notification-service logs the alert, analytics-service stores it in MongoDB for aggregation queries. Both are idempotent so duplicate delivery doesn't cause issues.

```
Angular (:4200)
   │
   ▼  REST
API Gateway (:8080) ── JWT validation, CORS
   │
   ├─ gRPC ──► customer-service (:3001/9090) ──► PostgreSQL
   ├─ gRPC ──► account-service  (:3002/9091) ──► PostgreSQL
   ├─ gRPC ──► payment-service  (:3003/9092) ──► PostgreSQL
   │                │
   │                └─ Kafka ("payment-processed")
   │                     ├──► notification-service (:3004) ──► PostgreSQL
   │                     └──► analytics-service    (:3005) ──► MongoDB
   │
   ├─ REST ──► auth-service (:3006) ──► PostgreSQL
   └─ REST ──► analytics-service (:3005)
```

## Running it

### Docker (full stack)

```bash
git clone https://github.com/jnkweti/Bank-of-Java-Microservices.git
cd Bank-of-Java
docker-compose up --build
```

That's it. Starts 5 Postgres instances, MongoDB, Kafka (KRaft, no Zookeeper), Zipkin, and all 7 services.

- Gateway: http://localhost:8080
- Kafka UI: http://localhost:8090
- Zipkin: http://localhost:9411

### Frontend

```bash
cd bank-frontend
npm install
npx ng serve
```

Open http://localhost:4200. Register an account, create a bank account from the dashboard, send a payment.

### Local dev

If you want to run services outside Docker (for debugging, hot reload, etc.), start just the infrastructure:

```bash
docker-compose up customerdb accountdb paymentdb notificationdb authdb analyticsdb kafka zipkin
```

Then run whichever service you're working on:

```bash
cd payment-service
./mvnw spring-boot:run
```

Note: use `./mvnw`, not `mvn` — the wrapper is included, Maven install isn't required.

## What each service does

**customer-service** — CRUD for customer profiles. gRPC server on 9090. When a customer is created, it calls account-service over gRPC to auto-create a savings account.

**account-service** — Bank accounts (checking, savings). gRPC server on 9091. Validates that the customer exists before creating an account by calling customer-service.

**payment-service** — Processes transfers between accounts using a saga pattern. Debits the source account, credits the destination — if the credit fails, it reverses the debit. Publishes the outcome to Kafka regardless of success or failure.

**notification-service** — Kafka consumer. Picks up payment events and persists a notification record. Deduplicates by paymentId.

**analytics-service** — Kafka consumer. Stores payment events in MongoDB. Exposes aggregation endpoints: payment summary, daily volume, top accounts by transaction count. Uses MongoTemplate pipelines.

**auth-service** — Issues JWTs signed with RS256. Exposes a JWKS endpoint so the gateway can validate tokens without sharing secrets. Access tokens expire in 15 minutes, refresh tokens in 7 days.

**api-gateway** — The frontend's only point of contact. Translates REST to gRPC for customer/account/payment services. Forwards to auth-service and analytics-service via REST (they don't have gRPC). Enforces JWT auth on all routes except `/auth/**`. Analytics endpoints require ROLE_ADMIN.

## Payment saga

This is probably the most interesting part. The payment flow is:

1. Check both accounts exist and are ACTIVE
2. Debit the source account
3. Credit the destination account
4. If step 3 fails, **reverse step 2** (compensating transaction)
5. Publish event to Kafka (COMPLETED or FAILED)

The Kafka publish is fire-and-forget — if Kafka is down, the payment still goes through. Losing a notification is better than rolling back a successful transfer.

## API

Everything goes through the gateway on port 8080.

**Auth** (no token needed)
```
POST /auth/register      { email, password }
POST /auth/login         { email, password }  → { accessToken, refreshToken }
POST /auth/refresh       { refreshToken }
```

**Customers**
```
GET    /api/customers/{id}
GET    /api/customers/email/{email}
POST   /api/customers          { firstName, lastName, email, address, birthDate }
PUT    /api/customers/{id}
DELETE /api/customers/{id}
```

**Accounts**
```
GET    /api/accounts/{id}
GET    /api/accounts/customer/{customerId}
POST   /api/accounts           { accName, customerId, type, status, balance }
PUT    /api/accounts/{id}
DELETE /api/accounts/{id}
```

**Payments**
```
POST   /api/payments           { fromAccountId, toAccountId, amount, type, description }
GET    /api/payments/{id}
GET    /api/payments/account/{accountId}
```

**Analytics** (admin only)
```
GET /api/analytics/summary
GET /api/analytics/volume?from=YYYY-MM-DD&to=YYYY-MM-DD
GET /api/analytics/top-accounts?limit=10
```

## Frontend

Angular 19 with Material. Template-driven forms, functional route guards, HTTP interceptor for JWT.

Pages: login, register (creates auth user + customer profile in one flow), dashboard (account cards with balances), create account, send payment.

The auth interceptor catches 401s and kicks you back to login — so when the JWT expires after 15 minutes, you get redirected instead of staring at broken API calls.

## Project layout

```
Bank-of-Java/
├── proto-config/            # shared .proto files (customer, account, payment)
├── customer-service/
├── account-service/
├── payment-service/
├── notification-service/
├── analytics-service/
├── auth-service/
├── api-gateway/
├── bank-frontend/           # Angular
├── docker-compose.yml
├── *.Dockerfile             # multi-stage builds (Maven build → JRE runtime)
└── TODO.md
```

## Tests

```bash
cd customer-service && ./mvnw clean verify     # all passing
cd account-service && ./mvnw clean verify      # all passing
cd payment-service && ./mvnw clean verify      # 26 tests
cd notification-service && ./mvnw clean verify # 15 tests
cd analytics-service && ./mvnw clean verify    # 14 tests
cd auth-service && ./mvnw clean verify         # 19 tests
```

All services use `@WebMvcTest` / `@ExtendWith(MockitoExtension.class)` — no Spring context or real DB needed, they run fast.

## Tech

- Java 21, Spring Boot 3.5.7
- gRPC (protobuf 3.25.5, grpc-java 1.73.0)
- Apache Kafka (KRaft mode)
- PostgreSQL 16, MongoDB 7
- Flyway for database migrations
- Angular 19, Angular Material
- Zipkin for distributed tracing
- Docker multi-stage builds

## Config

All services pull config from environment variables with sensible localhost defaults, so you can run locally without setting anything. Docker Compose overrides them for container networking. No secrets hardcoded in `application.yml`.

## License

MIT
