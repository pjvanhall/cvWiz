# cvWiz

cvWiz is a Spring Boot API for managing managers, consultants, CVs, skill matrices, login users, and consultant onboarding.

The API is designed to use HTTP-only JWT cookies (with Bearer token fallback), BCrypt password hashing, request sanitization, validation annotations, and role checks. Security is temporarily relaxed for local development so the API can be exercised while frontend/client authentication is still being wired up.

## Tech Stack

- Java 26
- Spring Boot 4.0.6
- Spring Web
- Spring Security
- Spring Data JPA
- PostgreSQL
- JJWT
- Maven
- JUnit 5

## Local Setup

Prerequisites:

- Java 26
- PostgreSQL
- Maven or the included Maven wrapper

Create a local PostgreSQL database named `cvw`. The default development settings are in `src/main/resources/application.properties`.

Run the application:

```powershell
.\mvnw.cmd spring-boot:run
```

If the wrapper fails on Windows, use an installed Maven distribution:

```powershell
mvn spring-boot:run
```

Run tests:

```powershell
mvn test
```

## API Documentation

- API reference: `docs/API.md`
- Postman collection: `docs/postman/cvWiz.postman_collection.json`
- cURL snippets: `docs/postman/cvWiz.curl.md`

Import the Postman collection, run `Auth / Login as seeded manager`, and then use the manager requests. The collection stores the JWT, generated consultant username, password, and id as collection variables for the one-time consultant login flow.

## Security Notes



- Passwords are encoded with `BCryptPasswordEncoder`.
- JWTs include issuer, audience, subject, issued-at, not-before, expiration, token id, email, and authorities.
- Login attempts are throttled after repeated failures.
- Request bodies and query parameters are sanitized before controller handling.
- DTO validation is enabled with `jakarta.validation`.
- CORS is limited to common local frontend origins.
- `application.properties` still contains development database and JWT settings and should be externalized before production use.

## Project Layout

```text
src/main/java/nl/codeclan/cvwiz
  config/       Security, CORS, sanitization, and API error handling
  controller/   REST controllers
  dto/          API request and response DTOs
  mapper/       Entity/DTO mapping helpers
  model/        JPA entities
  repository/   Spring Data repositories
  service/      Business logic
  util/         JWT and input validation utilities
```

## Common Local URLs

```text
Base API: http://localhost:8080
Login:    POST http://localhost:8080/gebruikers/login
```
