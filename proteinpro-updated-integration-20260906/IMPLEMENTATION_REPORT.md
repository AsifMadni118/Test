# ProteinPro implementation report

## 1. Scope and source-of-truth decisions

The existing backend remained the base and the uploaded frontend was integrated into it. The supplied PDF/HLD was inspected for system boundaries and required flows, while `updated prompt.txt` controlled implementation and completion rules. No service, database, topic, or business workflow was added where the sources did not define one.

Persistence ownership is Authentication → MySQL `authentication_db`, User Profile → MySQL `user_profile_db`, Bookmark → MongoDB `proteinpro_bookmarks`, and Protein → no database.

## 2. End-to-end workflow

```text
React :3000
   |
   | REST + optional Bearer JWT
   v
API Gateway :8080 ---- service lookup ----> Eureka :8761
   |              central configuration --> Config Server :8888
   |
   +--> User Profile :8082 --> MySQL/user_profile_db
   |         |
   |         +-- typed Feign credential --> Authentication :8081 --> MySQL/authentication_db
   |         +-- USER_CREATED -----------> Kafka :9092 -- consume --> Auth activation
   |
   +--> Authentication :8081 -- login/reset events --> Kafka
   +--> Protein :8083 -- typed Feign GET /proteindata --> External API :3232
   +--> Bookmark :8084 --> MongoDB/proteinpro_bookmarks
                       +-- create/update/delete events --> Kafka
```

Registration creates an inactive credential first. User Profile then persists the password-free profile and publishes `USER_CREATED`. Authentication consumes the typed event, verifies `userId` and email, and activates the credential. This preserves the supplied event-driven design and prevents login before profile activation.

## 3. Frontend integration

The uploaded React design and navigation were preserved. Its data layer now uses one Axios client with `VITE_API_BASE_URL`, a 15-second timeout, normalized errors, Bearer headers, and centralized 401 handling.

| UI action | Gateway API |
|---|---|
| Register | `POST /api/profiles/register` |
| Login | `POST /api/auth/login` |
| Load/update profile | `GET/PUT /api/profiles/me` |
| Reset password | `POST /api/auth/password-reset` |
| Logout | `POST /api/auth/logout` plus local token disposal |
| Search/filter proteins | `GET /api/proteins` with exact query names |
| Bookmark CRUD | `/api/bookmarks` and `/{id}/comment` |

After login, the UI retrieves `/api/profiles/me`; it does not invent a user ID from email. Registration briefly retries real login while Kafka activates the credential. A failed profile bootstrap clears the partially stored token. A 30-minute inactivity timer logs the browser out, and any backend 401 clears the shared session. No mock list or dummy API response remains.

The UI consumes only `id`, `source`, `cost_grams`, `cost_package`, `protein_ per_pack`, `vegetarian`, and `vegen`. Its Docker image builds the pnpm lockfile and serves the Vite bundle with nginx on port 3000.

## 4. Typed service boundaries

Protein's Feign client no longer accepts a query map or returns `List<Map<String,Object>>`. It uses `ProteinSearchRequest`, `ExternalProteinResponse`, and `ProteinResponse`. JSON annotations retain external spellings. Invalid upstream records produce 502, timeouts 504, and other Feign failures 502.

Bookmark no longer accepts or persists an arbitrary protein map. `ProteinSnapshot` validates the exact API contract and maps to typed embedded `BookmarkProteinData`. API errors are typed records. Kafka publishers use their actual event types; the Authentication listener factory is typed to `UserCreatedEvent`.

## 5. Security and identity

- Authentication signs JWTs with subject=email and a required `userId` claim.
- Gateway removes spoofed identity headers, validates JWT, then writes trusted values.
- Authentication, Profile, and Bookmark independently validate the Bearer token.
- Bookmark ownership queries include bookmark ID and authenticated user ID.
- Passwords are BCrypt hashes in Authentication only; Profile, Bookmark, Kafka, and responses carry no hashes.
- The internal credential endpoint compares `X-Internal-Api-Key` in constant time.
- Secrets and database passwords come from environment variables.
- Sanitized error bodies expose no stack trace or infrastructure credential.

## 6. Data and external API

Authentication and Profile use Spring Data JPA, MySQL Connector/J, Flyway V1 migrations, and Hibernate `ddl-auto=validate`. Each owns a separate database and scoped Compose user. Bookmark retains Spring Data MongoDB and its unique `(userId, proteinId)` compound index. Protein has no repository, persistence dependency, or database configuration.

The external image is exactly `stackroutenew/proteinapi`. Protein forwards only defined case-sensitive filters to `GET /proteindata`; no external create workflow was invented.

## 7. Kafka contracts

| Topic | Producer | Current consumer | Key |
|---|---|---|---|
| `user-created` | User Profile | Authentication | `userId` |
| `user-updated` | User Profile | none defined | `userId` |
| `authentication-events` | Authentication | none defined | `userId` |
| `password-reset-events` | Authentication | none defined | `userId` |
| `bookmark-created` | Bookmark | none defined | `userId` |
| `bookmark-updated` | Bookmark | none defined | `userId` |
| `bookmark-deleted` | Bookmark | none defined | `userId` |

Events have UUID event IDs, event types, required domain identifiers, and timestamps. Producers wait up to five seconds for broker acknowledgement. Interrupted and failed sends are surfaced. Authentication consumes with three exponential-backoff retries. `protein-events` was not implemented because no event name, payload, trigger, or consumer was defined.

## 8. Containers and quality tooling

Compose defines one network, persistent MySQL/MongoDB/Kafka/Sonar volumes, health checks, ordered dependencies, seven backend containers, frontend, external Protein API, and optional SonarQube. `Dockerfile.backend` builds a selected Maven module; frontend uses a locked pnpm build and nginx runtime.

JaCoCo runs at `verify`, emits XML, and fails any executable module below 90% line coverage. Only Spring Boot launcher classes are excluded because they contain no domain behavior. Sonar receives all five reports and waits for its gate. `scripts/configure-sonar-quality-gate.ps1` provisions a 90% coverage condition. `scripts/e2e.ps1` exercises real containers and critical cross-service flows.

## 9. Verification evidence

Executed on this revision:

| Check | Result |
|---|---|
| Backend tests | 60 run, 0 failed, 0 errors, 0 skipped |
| Gateway coverage | 56/56 = 100% |
| Authentication coverage | 190/190 = 100% |
| User Profile coverage | 140/140 = 100% |
| Protein coverage | 38/38 = 100% |
| Bookmark coverage | 158/158 = 100% |
| Frontend source compilation | Passed after fixing `ProfilePage` effect syntax |
| Untyped production-boundary scan | No raw map/object boundary patterns found |

Not executed on this host:

- Docker Compose and real-infrastructure E2E, because Docker is not installed.
- SonarQube scan and Quality Gate, because there is no running Sonar server/token.
- Standard Vite command, because this sandbox blocks Node from spawning esbuild; direct esbuild compilation passed.

The hard completion rule remains unsatisfied until real E2E and Sonar Quality Gate pass on a Docker-enabled host. No pass is claimed for those checks.

## 10. Material changes

- Integrated `frontend/` with real Gateway calls, JWT session flow, profile bootstrap, Kafka activation retry, and Docker/nginx build.
- Reworked Protein Feign/controller/service contracts into validated DTOs.
- Reworked Bookmark API and Mongo embedded snapshot into a typed schema.
- Added typed error responses and Kafka producer/listener generics.
- Added meaningful tests for services, filters, JWT claims, controllers, ownership, publishers, consumers, retry/failure paths, models, and topics.
- Added JaCoCo enforcement, Sonar configuration, Quality Gate setup, real-infrastructure E2E, secure Compose secrets, persistent volumes, and health checks.
- Removed obsolete fixed-password MySQL initialization files; Compose uses the environment-validated script.

No profile deletion, forgotten-password token flow, JWT revocation store, Protein write API, numeric rate-limit policy, dead-letter topic, or Protein Kafka event was invented because those contracts were not supplied.
