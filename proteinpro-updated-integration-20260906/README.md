# ProteinPro

ProteinPro is the integrated React + Spring Boot microservices project described by the supplied HLD. The browser talks only to API Gateway. Authentication and User Profile own separate MySQL databases, Bookmark owns MongoDB, Protein is a stateless adapter to the supplied external Protein API, and Kafka carries asynchronous lifecycle events.

## Runtime topology

| Component | Port | Responsibility |
|---|---:|---|
| React/nginx | 3000 | Responsive SPA |
| API Gateway | 8080 | Routing, CORS, JWT validation, trusted identity headers |
| Authentication | 8081 | Credentials, BCrypt, login, reset, JWT, auth events |
| User Profile | 8082 | Registration and profile details |
| Protein | 8083 | Typed proxy to `GET /proteindata`; no database |
| Bookmark | 8084 | Owner-scoped bookmark CRUD and required comments |
| Eureka | 8761 | Service registration/discovery |
| Config Server | 8888 | Centralized runtime configuration |
| MySQL | host 3307 | Separate `authentication_db` and `user_profile_db` |
| MongoDB | 27017 | `proteinpro_bookmarks` |
| Kafka | 9092 | Internal asynchronous event bus |
| External Protein API | 3232 | Supplied `stackroutenew/proteinapi` image |
| SonarQube (quality profile) | 9000 | Static analysis and Quality Gate |

## Start the application

1. Copy `.env.example` to `.env` and replace every placeholder. `JWT_SECRET` must contain at least 32 characters.
2. Start the stack:

```powershell
docker compose up --build -d
docker compose ps
```

3. Open `http://localhost:3000`. Eureka is at `http://localhost:8761`.

The browser never needs direct calls to ports 8081–8084. All UI calls use `VITE_API_BASE_URL`, which defaults to `http://localhost:8080`.

## API contract

- Public: `POST /api/profiles/register`, `POST /api/auth/login`, `GET /api/proteins`.
- Authenticated: `GET/PUT /api/profiles/me`, `POST /api/auth/password-reset`, `POST /api/auth/logout`, and all `/api/bookmarks` operations.
- Protein and bookmark snapshots use exactly: `id`, `source`, `cost_grams`, `cost_package`, `protein_ per_pack`, `vegetarian`, `vegen`.
- Values for `vegetarian` and `vegen` are the case-sensitive external values `T` or `F`.

## Build, coverage, Sonar, and E2E

```powershell
mvn clean verify
```

The Maven build enforces at least 90% JaCoCo line coverage in every executable backend module and creates XML under each module's `target/site/jacoco/jacoco.xml`. Only framework bootstrap `*Application.class` files are excluded.

Start SonarQube and configure the project gate once:

```powershell
docker compose --profile quality up -d sonarqube
.\scripts\configure-sonar-quality-gate.ps1 -Token $env:SONAR_TOKEN
mvn clean verify org.sonarsource.scanner.maven:sonar-maven-plugin:sonar `
  -Dsonar.host.url=$env:SONAR_HOST_URL `
  -Dsonar.token=$env:SONAR_TOKEN `
  -Dsonar.qualitygate.wait=true
```

Run the real-infrastructure cross-service test after the main stack is healthy:

```powershell
.\scripts\e2e.ps1
```

See `IMPLEMENTATION_REPORT.md` for the architecture, change list, test evidence, and explicitly unverified items.
