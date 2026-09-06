# MySQL migration report (historical stage)

> Statements about the absence of Docker/frontend apply only to the earlier migration stage. The current result is documented in `IMPLEMENTATION_REPORT.md` and `RUNTIME_VERIFICATION.md`.

## 1. Scope and result

The existing repository was modified in place. Authentication now owns MySQL `authentication_db`; User Profile now owns MySQL `user_profile_db`; Bookmark remains on MongoDB; Protein remains the inspected stateless external-API adapter. API Gateway, Eureka, Config Server, Kafka, Feign, JWT, BCrypt, DTOs, controllers, routes, ports, and event contracts remain compatible.

## 2. Files changed

| File | Reason |
|---|---|
| `.env.example` | Replace Authentication/Profile Mongo URIs with six service-owned MySQL variables |
| `authentication-service/pom.xml` | Replace Mongo starter with JPA, MySQL JDBC, Flyway Core, and Flyway MySQL |
| `authentication-service/.../model/Credential.java` | Map the existing fields to the `credentials` JPA table while retaining string IDs and timestamps |
| `authentication-service/.../repository/CredentialRepository.java` | Replace `MongoRepository` with `JpaRepository`; preserve all method signatures |
| `authentication-service/.../web/GlobalExceptionHandler.java` | Map relational integrity violations to the existing sanitized 409 response |
| `user-profile-service/pom.xml` | Replace Mongo starter with JPA, MySQL JDBC, Flyway Core, and Flyway MySQL |
| `user-profile-service/.../model/UserProfile.java` | Map the existing fields to the `user_profiles` JPA table without adding fields |
| `user-profile-service/.../repository/UserProfileRepository.java` | Replace `MongoRepository` with `JpaRepository`; preserve `existsByEmail` |
| `user-profile-service/.../web/GlobalExceptionHandler.java` | Map relational integrity violations to the existing sanitized 409 response |
| `config-server/.../config/authentication-service.yml` | Add Authentication datasource, JPA validation, and Flyway properties |
| `config-server/.../config/user-profile-service.yml` | Add Profile datasource, JPA validation, and Flyway properties |
| `README.md` | Document the hybrid database architecture and complete local runbook |
| `ARCHITECTURE_ANALYSIS.md` | Record inspection findings, source conflict, migration delta, and decisions |
| `RUNTIME_VERIFICATION.md` | Record the post-migration build and real-infrastructure evidence |

Paths abbreviated with `...` retain their existing Java package roots.

## 3. Files added

| File | Purpose |
|---|---|
| `authentication-service/src/main/resources/db/migration/V1__create_credentials.sql` | Reproducible Authentication schema |
| `user-profile-service/src/main/resources/db/migration/V1__create_user_profiles.sql` | Reproducible User Profile schema |
| `authentication-service/src/test/java/com/proteinpro/auth/model/CredentialPersistenceMappingTest.java` | Regression check for JPA/table/repository and string identity |
| `user-profile-service/src/test/java/com/proteinpro/profile/model/UserProfilePersistenceMappingTest.java` | Regression check for JPA/table/repository and unchanged UUID identity |
| `MYSQL_MIGRATION_REPORT.md` | Required file-by-file migration and validation report |

## 4. Files removed

None. Mongo-specific imports and dependencies were replaced only inside the two migrated services. No source file, test, endpoint, or unrelated module was deleted.

## 5. Authentication database changes

- Database: `authentication_db`.
- Table: `credentials`.
- Existing fields preserved: `id`, `userId`, `email`, `passwordHash`, `active`, `createdAt`, `updatedAt`.
- SQL columns: `id`, `user_id`, `email`, `password_hash`, `active`, `created_at`, `updated_at`.
- `id` remains an opaque string and is generated as a UUID before persistence.
- `user_id` remains the Profile-generated UUID shared through JWT, Feign, Kafka, and other services.
- Unique constraints protect `user_id` and normalized `email`.
- BCrypt hashes remain 60-character values; no raw password is persisted, returned, logged, or published.

## 6. User Profile database changes

- Database: `user_profile_db`.
- Table: `user_profiles`.
- Existing fields preserved: `id`, `firstName`, `lastName`, `email`, `createdAt`, `updatedAt`.
- SQL columns: `id`, `first_name`, `last_name`, `email`, `created_at`, `updated_at`.
- The Profile-generated UUID string remains the primary key and public user identity.
- The existing unique-email check remains and MySQL also enforces a named unique constraint.
- No password field exists on the entity or response.
- Profile deletion was not added because the inspected API has no deletion endpoint.

## 7. Schema migration strategy

There was no existing Flyway or Liquibase mechanism. Flyway is now the single schema mechanism for both MySQL services. Each has one V1 migration. Hibernate uses `ddl-auto=validate`, so it validates the Flyway-created schema rather than silently changing it. Future changes must use new versioned migrations; applied V1 files must not be edited in a deployed environment.

## 8. MongoDB services left unchanged

Bookmark retains its Mongo starter, `@Document`, compound/index annotations, `MongoRepository`, Mongo URI, APIs, ownership rules, event publishers, and tests. Runtime verification connected it to real MongoDB and completed create/list/update/delete through Gateway.

Protein contains no MongoDB code in the existing repository. It has no entity, repository, Mongo dependency, or URI and remains the stateless Feign adapter for the external Protein API. Adding an unused MongoDB connection solely to match the prompt's generic diagram would invent behavior; this conflict is documented rather than hidden.

## 9. Maven dependency changes

Authentication and User Profile now use:

- `spring-boot-starter-data-jpa`
- `com.mysql:mysql-connector-j` at runtime
- `org.flywaydb:flyway-core`
- `org.flywaydb:flyway-mysql`

Their MongoDB starters were removed. Bookmark retains its MongoDB starter. No framework, Java, Spring Boot, Spring Cloud, JWT, Kafka, or test version was upgraded.

## 10. Configuration changes

Both datasource configurations remain centralized in Config Server and use environment placeholders. Each config specifies the MySQL JDBC driver, disables Open EntityManager in View, enables Flyway validation/migration, selects the MySQL dialect, and sets Hibernate DDL handling to `validate`.

No database configuration was added to Gateway, Eureka, Config Server, or Protein. Bookmark's Mongo configuration is unchanged.

## 11. Required environment variables

```text
AUTH_MYSQL_URL
AUTH_MYSQL_USERNAME
AUTH_MYSQL_PASSWORD
USER_PROFILE_MYSQL_URL
USER_PROFILE_MYSQL_USERNAME
USER_PROFILE_MYSQL_PASSWORD
BOOKMARK_MONGODB_URI
JWT_SECRET
INTERNAL_API_KEY
KAFKA_BOOTSTRAP_SERVERS (optional localhost default)
EUREKA_URL (optional localhost default)
EUREKA_INSTANCE_HOSTNAME (optional localhost default)
CONFIG_SERVER_URL (optional localhost default)
PROTEIN_API_BASE_URL (optional localhost default)
FRONTEND_ORIGIN (optional localhost default)
JWT_EXPIRATION_SECONDS (optional 1800 default)
```

No populated `.env`, database password, internal API key, or JWT secret is committed.

## 12. MySQL database creation

Run as a MySQL administrator, replacing the example passwords:

```sql
CREATE DATABASE authentication_db
  CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
CREATE DATABASE user_profile_db
  CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;

CREATE USER 'proteinpro_auth'@'localhost'
  IDENTIFIED BY 'replace-with-local-auth-db-password';
CREATE USER 'proteinpro_profile'@'localhost'
  IDENTIFIED BY 'replace-with-local-profile-db-password';

GRANT ALL PRIVILEGES ON authentication_db.*
  TO 'proteinpro_auth'@'localhost';
GRANT ALL PRIVILEGES ON user_profile_db.*
  TO 'proteinpro_profile'@'localhost';
FLUSH PRIVILEGES;
```

The runtime verification also proved that each scoped user was denied a query against the other service's database.

## 13. API compatibility

No controller, route, HTTP method, request record, response record, validation annotation, success status, authentication requirement, or error-body shape changed. Verified through Gateway:

- `POST /api/profiles/register` returned 201.
- `POST /api/auth/login` returned a Bearer JWT and 1800-second expiry.
- `GET /api/profiles/me` returned the registered identity.
- `PUT /api/profiles/me` persisted the update.
- `POST /api/auth/password-reset` returned 204 and the new password logged in.
- Duplicate registration returned 409.
- Direct protected Bookmark access without JWT returned 401.

## 14. JWT and password compatibility

JWT generation, validation, signing key input, subject, `userId` claim, expiry, Gateway validation, and downstream validation are unchanged. Runtime login and protected calls passed. BCrypt encoding and matching are unchanged. SQL verification found a 60-character BCrypt hash rather than the submitted password.

## 15. Kafka compatibility

Topics, event records, keys, producer serializers, consumer deserializer, group `authentication-service-user-events`, retries, and acknowledgement waits are unchanged. Against real Kafka 3.9.2, registration published `USER_CREATED`; Authentication deserialized it and logged credential activation; login then succeeded. Authentication and Bookmark producer operations received broker acknowledgements. No Kafka mock was introduced.

## 16. Feign compatibility

The existing Profile-to-Authentication `POST /internal/credentials` call through Eureka is unchanged. Runtime registration proved it created the MySQL credential before Profile persisted the matching MySQL row. Protein's external API Feign client is unchanged.

## 17. Eureka, Config Server, and Gateway verification

Config Server served both new datasource configurations. Eureka reported `API-GATEWAY`, `AUTHENTICATION-SERVICE`, `USER-PROFILE-SERVICE`, `PROTEIN-SERVICE`, and `BOOKMARK-SERVICE` as `UP`. Gateway routed registration, authentication, Profile, Bookmark, and Protein calls. All ports `8888`, `8761`, `8080`, `8081`, `8082`, `8083`, and `8084` reported health `UP` during the migration test.

## 18. Tests and builds executed

- Full reactor `mvn test` after the persistence conversion: passed.
- Full reactor `mvn package` before runtime verification: passed.
- Final `mvn clean test` and `mvn clean package` both exited 0. Ten tests ran with 0 failures, 0 errors, and 0 skipped; details are recorded in `RUNTIME_VERIFICATION.md`.
- Tests were not disabled or removed.

## 19. Security review

- Repository-derived JPA queries remain parameterized; no user-built SQL was added.
- Database users are scoped per database.
- Passwords remain BCrypt-only at rest and absent from Kafka/API responses.
- JWT and database secrets remain environment supplied.
- SQL exceptions are sanitized to existing 409/500 response shapes.
- Complete Authorization headers, database credentials, and connection strings are not logged by application code.
- Gateway and downstream JWT checks, CORS, validation, Bookmark ownership, and limited Actuator exposure are unchanged.

## 20. Remaining manual actions and disclosed limitations

1. Install and operate durable local/hosted MySQL, MongoDB, and Kafka outside this temporary verification environment.
2. MySQL 8.0 is recommended with Spring Boot 3.3.5's managed Flyway. Runtime testing on MySQL 8.4.11 succeeded, but Flyway logged that 8.4 is newer than its tested range; this warning is not hidden.
3. No production Authentication/Profile MongoDB dump was supplied, so historical data was not silently copied. A separately reviewed data migration must preserve UUID IDs and BCrypt hashes if real source data exists.
4. Successful Protein data retrieval remains dependent on the separately supplied external API. With no API at the configured URL, the preserved behavior returns 502; no fake data source was introduced.
5. Registration remains a cross-service Feign/Kafka workflow without a distributed transaction. The existing inactive-credential activation design is preserved; no unrequested outbox/saga was invented.
6. No Dockerfile or Compose file was added.
