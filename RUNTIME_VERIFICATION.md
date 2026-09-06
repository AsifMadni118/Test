# Runtime verification — current integration

This file records only checks actually executed against this revision on 6 September 2026. Prepared-but-unexecuted checks are listed separately.

## Executed successfully

- Enforced `mvn clean verify`: exit 0; 60 tests, 0 failures, 0 errors, 0 skipped; every configured coverage check reported met.
- JaCoCo reports: Gateway 56/56 (100%), Authentication 190/190 (100%), User Profile 140/140 (100%), Protein 38/38 (100%), Bookmark 158/158 (100%).
- Java compilation after typed Protein, Bookmark, Kafka, and error-response changes.
- Frontend source bundle compilation with local esbuild 0.21.5: JavaScript 229.1 KB and CSS 9.3 KB before gzip. This exposed and then verified the fix for an invalid `await` in `ProfilePage`.
- Static source scan found no `Map<String,Object>`, `List<Map<...>>`, `KafkaTemplate<String,Object>`, or `ResponseEntity<Map<...>>` in production service code.

## Not executed on this host

- `docker compose up`: Docker CLI/engine is not installed on the host.
- Real MySQL, MongoDB, Kafka, Eureka, Gateway, external Protein API, and browser/API E2E for this revision: blocked by the missing Docker runtime.
- SonarQube analysis and Quality Gate: blocked by the missing Docker runtime and absence of a running SonarQube server/token.
- Normal Vite command: the sandbox denied Node's esbuild child-process launch with Windows `EPERM`; direct esbuild source compilation passed. The Docker build remains the authoritative Vite production-build path.

The controlling prompt requires a real Sonar Quality Gate pass and real-infrastructure E2E evidence. This revision must not be described as fully complete until the commands in `README.md` pass in a Docker-enabled environment.
