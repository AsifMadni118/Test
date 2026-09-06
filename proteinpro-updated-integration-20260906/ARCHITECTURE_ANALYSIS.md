# CURRENT PROJECT ANALYSIS (historical migration record)

> This file describes the earlier MySQL-only migration stage. Use `IMPLEMENTATION_REPORT.md` for the current frontend, Docker, typed-DTO, coverage, and Sonar integration.

This is the existing `protein-pro-app` multi-module Maven repository delivered in the prior phase. It contains one parent build and seven Spring Boot modules. The MySQL migration was applied in place; no project or module was regenerated.

| Area before this migration | Inspected implementation |
|---|---|
| Build | Java 17, Spring Boot 3.3.5, Spring Cloud 2023.0.3 Maven reactor |
| Control plane | Config Server `:8888`, Eureka `:8761`, API Gateway `:8080` |
| Authentication `:8081` | MongoDB `Credential`, BCrypt, JWT, Kafka consumer/producer |
| User Profile `:8082` | MongoDB `UserProfile`, Feign credential creation, Kafka producer |
| Protein `:8083` | No database; stateless Feign client for external `/proteindata` |
| Bookmark `:8084` | MongoDB document/repository and Kafka producers |
| Service identity | UUID string `userId` shared through Feign, Kafka, JWT, profile API, and Bookmark references |
| Configuration | Every client loads native centralized configuration from Config Server |
| Schema migration tooling | None before this change |

The complete repository inspection covered all POMs, Java sources, tests, configurations, DTOs, entities/documents, repositories, controllers, security filters, JWT code, Feign clients, Kafka code, exception handlers, environment variables, and documentation. The initial MongoDB search found references only in Authentication, User Profile, Bookmark, centralized configuration, and documentation.

# CONTROLLING CHANGE REQUEST

`mysql prompt.txt` changes only Authentication and User Profile persistence to separate MySQL databases. It also requires existing REST, JWT, Kafka, Feign, Eureka, Config Server, Gateway, validation, and security behavior to remain compatible.

One statement in the prompt's generic target diagram says Protein uses MongoDB. The inspected Protein module has never had a model, repository, Mongo dependency, or database configuration; it reads an external API. Adding unused MongoDB persistence would invent behavior and contradict the same prompt's instructions to inspect the actual repository and avoid unrelated rewrites. Protein therefore remains stateless. Bookmark remains the only MongoDB-backed service.

# EXACT MIGRATION DELTA

| Area | Before | Current target |
|---|---|---|
| Authentication dependency | Spring Data MongoDB | Spring Data JPA, MySQL JDBC, Flyway MySQL |
| Authentication model | `@Document` | `@Entity` mapped to `authentication_db.credentials` |
| Authentication repository | `MongoRepository` | `JpaRepository` with identical derived methods |
| User Profile dependency | Spring Data MongoDB | Spring Data JPA, MySQL JDBC, Flyway MySQL |
| User Profile model | `@Document` | `@Entity` mapped to `user_profile_db.user_profiles` |
| User Profile repository | `MongoRepository` | `JpaRepository` with identical derived methods |
| Duplicate constraint advice | `DuplicateKeyException` | `DataIntegrityViolationException` with the same 409 API messages |
| Schema management | Mongo auto-index creation | Versioned Flyway SQL plus Hibernate `ddl-auto=validate` |
| Configuration | Two MongoDB URIs | Two independent JDBC URLs, usernames, and passwords |
| Bookmark | MongoDB | Unchanged MongoDB implementation |
| Protein | Stateless external API | Unchanged stateless implementation |

# COMPATIBILITY DECISIONS

- All entity fields are direct mappings of the existing models; no domain fields or relationships were added.
- UUID `String` user IDs remain `VARCHAR(36)` and are unchanged in DTOs, JWT claims, Kafka payloads, Feign requests, Bookmark ownership, and API responses.
- Authentication retains a separate opaque credential ID, now generated as a UUID string before JPA persistence.
- Email uniqueness remains enforced in the service and is also enforced by named MySQL unique constraints.
- BCrypt, JWT signing/claims/expiry, routes, status codes, validation, event topics, payloads, keys, consumer group, retries, and internal API-key authentication are unchanged.
- Flyway is the only schema-change mechanism. Hibernate validates mappings and does not create or update tables.
- No cross-database JPA relationship or database access was introduced. Runtime grants were verified to deny each service user access to the other database.
- No Docker, Docker Compose, frontend, API Gateway, Eureka, Protein, or Bookmark source change was required.

# DATA MIGRATION BOUNDARY

The repository contains no production MongoDB dump or approved row-level migration input. The code migration does not silently copy historical Authentication/Profile documents into MySQL. If real MongoDB data exists outside this workspace, keep it intact and run a separately reviewed export/transform/import process that preserves UUID user IDs and BCrypt hashes. Password hashes must be copied as hashes; they must never be decrypted or replaced with raw passwords.

# IMPLEMENTATION AND VALIDATION PLAN

1. Replace Mongo dependencies only in Authentication and User Profile.
2. Convert their existing models/repositories to JPA without changing public contracts.
3. Add independent Flyway V1 schemas and centralized environment-driven MySQL configuration.
4. Update duplicate-integrity handling, environment examples, regression tests, and local setup documentation.
5. Run the full Maven tests and package reactor.
6. Start real MySQL, MongoDB, Kafka, Config Server, Eureka, the five registered clients, and Gateway.
7. Verify Flyway/JPA, registration, Feign, Kafka activation, login/JWT/reset, profile read/update, duplicate email, database isolation, Bookmark MongoDB behavior, Protein behavior, routing, discovery, and health.
