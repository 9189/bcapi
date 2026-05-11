# bcapi - Beer Catalogue API

---

## Setup and Execution

**Prerequisites**

- Java 25+
- Maven 3.9+ (or use the included `./mvnw` wrapper)

**Run the application**

```bash
./mvnw spring-boot:run
```

The server starts on `http://localhost:8080` by default.

**Run tests**

```bash
./mvnw test
```

**Build a JAR**

```bash
./mvnw package
java -jar target/bcapi-0.0.1-SNAPSHOT.jar
```

**H2 console** (available in dev mode)

Navigate to `http://localhost:8080/h2-console`. JDBC URL: `jdbc:h2:mem:testdb`.

---

## Assumptions

- Testcontainers is scaffolded for future integration tests that may require a containerized database or other services.

---

## Design Decisions

- **Spring Boot 4.0.6 / Java 25** — targets the latest stable platform versions to take advantage of virtual threads and modern language features.
- **Spring Security** — included from the start so security concerns are addressed at the design stage rather than retrofitted later.
- **Maven wrapper (`mvnw`)** — checked in so contributors do not need a local Maven installation.
- **Testcontainers** — wired up in test configuration to support realistic integration tests against containerized infrastructure without requiring a local database.

---

## API Usage Examples

> Endpoints will be documented here as they are implemented. The examples below assume the server is running on `http://localhost:8080`.

**Health check (Spring Boot Actuator, if enabled)**

```bash
curl http://localhost:8080/actuator/health
```

**Example: create a resource**

```bash
curl -X POST http://localhost:8080/api/resource \
  -H "Content-Type: application/json" \
  -d '{"field": "value"}'
```

**Example: retrieve a resource**

```bash
curl http://localhost:8080/api/resource/1
```

**Example: authenticated request (Bearer token)**

```bash
curl http://localhost:8080/api/resource \
  -H "Authorization: Bearer <token>"
```

---

## AI Usage Log

| Date | Tool | Prompt summary | Output summary                  |
|------|------|----------------|---------------------------------|
| 2026-05-11 | Claude Code (claude-sonnet-4-6) | Generate README with setup, assumptions, design decisions, API examples, and AI usage log sections | Initial README skeleton created |
