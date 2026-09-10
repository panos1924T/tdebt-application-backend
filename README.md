# T-Debt Backend

REST API for tracking debts and transactions, built with Java 21 and Spring Boot.

[Frontend repository](https://github.com/panos1924T/tdebt-application-frontend)

## Features

- User registration and JWT authentication.
- Role and capability-based authorization (ADMIN / USER).
- Debt creation, updates, filtering, pagination, and archiving.
- Transaction tracking with automatic debt balance updates.
- Transaction corrections that preserve the financial history.
- Soft deletion of users and debts.
- Ownership checks and structured validation/error responses.
- Initial admin account seeding from configuration.

## Tech stack

Java 21 · Spring Boot · Spring Security · Spring Data JPA · PostgreSQL · Flyway · Gradle Kotlin DSL · Lombok · dotenv-java · JUnit · Mockito · JaCoCo

## Local setup: build and run

Follow these steps in order. PostgreSQL must be available **before the build**, because the build includes a Spring context test that connects to the database and runs Flyway migrations.

### 1. Prerequisites and clone

Install Git, JDK 21, and PostgreSQL. DBeaver or pgAdmin is optional for database administration.

Gradle does not need to be installed separately: use the included Gradle Wrapper.

```bash
git clone https://github.com/panos1924T/tdebt-application-backend.git
cd tdebt-application-backend
```

Run all commands below from this project directory.

### 2. Create a local database

Connect to your PostgreSQL server as an administrator, for example through DBeaver. Execute these statements separately, with auto-commit enabled:

```sql
CREATE USER tdebt_user WITH PASSWORD 'replace_with_your_password';
```

```sql
CREATE DATABASE tdebt OWNER tdebt_user;
```

Replace the example password. If you already have a suitable database and user, use them instead of creating new ones.

**Create only the database.** Flyway creates the configured schema and tables automatically. The application database user must have permission to create the schema; making that user the database owner covers this requirement for a new local database.

Use a dedicated local development/test database. The context test uses the configured database and may apply migrations and run startup initialization.

### 3. Configure `.env`

Create `.env` in the project root, next to `gradlew.bat`, or copy `.env.example` if available.

```dotenv
DB_HOST=localhost
DB_PORT=5433
DB_NAME=tdebt
DB_USERNAME=tdebt_user
DB_PASSWORD=replace_with_your_password
CURRENT_DB_SCHEMA=tdebtapp

JWT_SECRET_KEY=replace_with_your_base64_secret
JWT_EXPIRATION=86400000

ADMIN_EMAIL=admin@example.com
ADMIN_PASSWORD=replace_with_a_strong_password

ALLOWED_ORIGINS=http://localhost:3000,http://localhost:4200
SPRING_PROFILES_ACTIVE=dev
```

- Set `DB_PORT` to the port of **your** PostgreSQL server. The application configuration defaults to `5433`; use `5432` if that is where your installation listens.
- `DB_NAME`, `DB_USERNAME`, and `DB_PASSWORD` must match step 2.
- `CURRENT_DB_SCHEMA` selects the schema for **both the application and Flyway**, including local runs. It is not a Docker-only setting.
- `JWT_SECRET_KEY` must be a Base64-encoded secret suitable for the application's JWT signing algorithm.
- `JWT_EXPIRATION` is in milliseconds: `86400000` is 24 hours.
- Supply the admin values for the initial admin account. Use your own credentials.
- Keep `.env` out of Git. Commit only a `.env.example` containing placeholders.

The Flyway settings in `application.yaml` must use the same schema variable as the datasource:

```yaml
spring:
  flyway:
    locations: classpath:db/migration
    baseline-on-migrate: true
    schemas: ${CURRENT_DB_SCHEMA}
    default-schema: ${CURRENT_DB_SCHEMA}
```

These settings are part of the existing configuration; do not append a second `spring:` block.

The variables must be available to both Gradle tests and the running application. If a command reports an unresolved placeholder, check the project's `.env` loading configuration or supply the variables in that terminal/IDE run configuration. Values configured only in an IDE run configuration are not automatically available to a separate terminal.

### 4. Build and run tests

**Windows**

```powershell
.\gradlew.bat clean build
```

**macOS / Linux**

```bash
./gradlew clean build
```

If the wrapper is not executable on macOS/Linux, run `chmod +x gradlew` first.

A successful build compiles the application, runs the tests, and generates the JAR in `build/libs/`.

During `contextLoads()`, Flyway applies pending SQL migrations from `src/main/resources/db/migration`. Hibernate then validates the database structure; `ddl-auto: validate` does not create tables itself.

### 5. Start the backend

**Windows**

```powershell
.\gradlew.bat bootRun
```

**macOS / Linux**

```bash
./gradlew bootRun
```

Default API base URL: `http://localhost:8080`.

The backend provides API endpoints; run the frontend separately for the user interface. Stop the backend with `Ctrl+C`.

## Tests and coverage

| Task | Windows | macOS / Linux |
| --- | --- | --- |
| Run tests | `.\gradlew.bat test` | `./gradlew test` |
| Run tests and generate coverage | `.\gradlew.bat test jacocoTestReport` | `./gradlew test jacocoTestReport` |

Reports:

- Tests: `build/reports/tests/test/index.html`
- Coverage: `build/reports/jacoco/test/html/index.html`

Tests include the application context, debt service, transaction service, and transaction mapper. The context test requires the configured PostgreSQL database.

## Docker

The repository also includes `Dockerfile` and `docker-compose.yml` for containerized deployment. Docker and Docker Compose are only needed for this option.

The existing Dockerfile requires a built application JAR. Complete the build above against an available database before building the image.

Before starting Compose, check its environment and port mappings. The app container must connect to PostgreSQL using the database service name and its internal port; `localhost` inside the app container refers to the app container itself. Keep these settings separate from the host-based local setup above.

```bash
docker compose up --build -d
```

Stop the containers:

```bash
docker compose down
```

`docker compose down -v` also removes the Compose-managed database volume and its stored data.

## API overview

Use the JWT returned by authentication as `Authorization: Bearer <token>` when calling protected endpoints. Access also depends on role, capabilities, and resource ownership.

| Area | Method | Endpoint | Purpose |
| --- | --- | --- | --- |
| Public | POST | `/api/v1/users` | Register |
| Public | POST | `/api/v1/auth` | Authenticate |
| Users | GET | `/api/v1/users` | List users |
| Users | GET / PUT / DELETE | `/api/v1/users/{uuid}` | Read, update, or delete a user |
| Debts | POST / GET | `/api/v1/debts` | Create or list debts |
| Debts | GET / PUT / DELETE | `/api/v1/debts/{debtUuid}` | Read, update, or delete a debt |
| Debts | PATCH | `/api/v1/debts/{debtUuid}` | Toggle OPEN / ARCHIVED |
| Transactions | POST / GET | `/api/v1/debts/{debtUuid}/transactions` | Create or list debt transactions |
| Transactions | PUT | `/api/v1/debts/{debtUuid}/transactions/{transUuid}` | Update or correct a transaction |
| Transactions | GET | `/api/v1/transactions/{transUuid}` | Read a transaction |
| Transactions | GET | `/api/v1/transactions` | List the user's transactions across debts |

List endpoints support pagination and applicable filters. Example: `/api/v1/transactions?page=0&size=10&sort=date,desc`.

## Troubleshooting

| Problem | What to check |
| --- | --- |
| Database connection fails | PostgreSQL is running; host, port, database name, and credentials are correct. |
| `permission denied for database` during Flyway startup | The configured migration user has permission to create a schema in that database. |
| `Schema validation: missing table` | Datasource and Flyway use the same `CURRENT_DB_SCHEMA`; inspect Flyway startup output and migration history. |
| `Could not resolve placeholder` | The required variable is available to the process running the build or application. |
| `compileTestJava` fails | Test source code does not compile. Read the compiler error before investigating database settings. |
| `contextLoads()` fails | Open the test report and read the deepest `Caused by` message for the actual startup failure. |
| HTTP 401 | The authentication token is missing, invalid, or expired. |
| HTTP 403 | The request is denied by the application's access rules. |

Runtime logs are written under `logs/`.
