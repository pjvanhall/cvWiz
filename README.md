# cvWiz

cvWiz is a Spring Boot API for managing managers, consultants, CVs, skill matrices, login users, and consultant onboarding.

The API is designed to use JWT bearer tokens, BCrypt password hashing, request sanitization, validation annotations, and role checks. Security is temporarily relaxed for local development so the API can be exercised while frontend/client authentication is still being wired up.

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

### Temporary Local Security Bypass

Security is currently commented out in the controller and method-security configuration. This is intended only for local development and API testing while authentication integration is in progress. Do not deploy this mode to a shared, staging, or production environment.

Current bypass state:

- `SecConfig` still disables CSRF and permits every HTTP request with `.anyRequest().permitAll()`.
- `@EnableMethodSecurity` is commented out in `src/main/java/nl/codeclan/cvwiz/config/SecConfig.java`.
- `@PreAuthorize` is commented out in `ManagerController`, `ConsultantController`, and `CustomUserController`.
- `ConsultantController` has temporary `authentication == null` fallbacks for reading and updating consultants without a bearer token.
- `/medewerkers/curriculumVitae/eersteLogin` still expects an authenticated consultant because the onboarding flow needs the current username.

### Restore Full Security

Before merging or deploying outside local development, restore all security gates:

1. Uncomment `@EnableMethodSecurity(securedEnabled = true, jsr250Enabled = true)` in `SecConfig`.
2. Uncomment the `@PreAuthorize` annotations in:
   - `src/main/java/nl/codeclan/cvwiz/controller/ManagerController.java`
   - `src/main/java/nl/codeclan/cvwiz/controller/ConsultantController.java`
   - `src/main/java/nl/codeclan/cvwiz/controller/CustomUserController.java`
3. Remove the temporary `authentication == null` fallback branches from `ConsultantController`.
4. Replace the development `permitAll` HTTP rule in `SecConfig` with explicit secured matchers. A secure baseline is:

```java
.authorizeHttpRequests((auth) -> auth
        .requestMatchers("/gebruikers/login").permitAll()
        .requestMatchers("/beheerders/**").hasAuthority("ROLE_MANAGER")
        .requestMatchers("/medewerkers/**").hasAuthority("ROLE_CONSULTANT")
        .anyRequest().authenticated())
```

5. Run `.\mvnw.cmd test`.
6. Re-test the Postman collection using manager and consultant JWTs.

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
