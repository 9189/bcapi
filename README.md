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

- A manufacturer must exist before a beer can be created or updated. `manufacturerId` is validated server-side and returns a `404` problem detail if the referenced manufacturer does not exist. No upsert.

---

## Design Decisions

### Project setup

#### Spring Boot 4.0.6 / Java 25:
Targets the latest stable platform versions to take advantage of virtual threads and modern language features.

#### Spring Security:
Included from the start so security concerns are addressed at the design stage rather than retrofitted later.

#### Testcontainers:
Wired up in test configuration to support realistic integration tests against containerized infrastructure without requiring a local database.
Not really a gamechanger when working with an in-memory H2 instance, however enables to defer concrete decision on persistence, since easily exchangeable for a test setup that reflects production configuration as close as possible.

#### OpenApi Code Generator:
API first, we ensure well thought through APIs, which also allows for parallelization of workable units in a real world setting.
Generating code out of the API specification ensures the documentation reflects the implementation, reducing the possibility for documentation drift.

Reduction of documentation drift can also be achieved through validating the api specification against the implementation in integration tests, for example with [swagger-request-validator](https://bitbucket.org/atlassian/swagger-request-validator/src/master/).
However, for the sake of simplicity and saving myself a bit of boilerplate, I decided to go with generating code out of the spec.

### API Design:

This API targets **[Richardson Maturity Model](https://martinfowler.com/articles/richardsonMaturityModel.html) Level 2**. Resources are addressable via distinct URIs, HTTP verbs are used as intended (`GET` for safe reads, `POST`/`PUT`/`DELETE` for state changes), and response codes carry semantic meaning. Level 3 (HATEOAS) is deliberately out of scope: embedding hypermedia controls in every response would add significant complexity and payload overhead with little practical benefit for a catalogue API where clients already know the resource structure.

#### UUID identifiers:
Resources use `string/uuid` rather than sequential integers as public identifiers. This decouples the internal database identity from the API surface, prevents clients from inferring record counts or enumerating resources, and simplifies future federation or migration scenarios where sequential IDs would collide.

#### Separate request and response schemas:
Request and response schemas are always distinct types. Response schemas carry server-assigned fields (`id`, `createdAt`, `updatedAt`, embedded relations) marked `readOnly`; request schemas carry only client-supplied fields with validation constraints. This makes the contract explicit: clients cannot accidentally submit server-owned fields, and the two can evolve independently.

#### Separate create and update request schemas:
Each resource has a dedicated `CreateRequest` and `UpdateRequest` schema rather than a single shared request type. This allows create and update operations to diverge over time — for example, `manufacturerId` is required on `BeerCreateRequest` to associate a beer with an existing manufacturer at creation time, while future requirements may allow reassigning or locking it on updates. A single shared schema would force both operations to share the same constraints, making the contract less expressive and harder to evolve.


#### Embedded manufacturer in Beer response:
The `Manufacturer` is inlined into the `Beer` representation rather than returned as a link or ID reference. This avoids forcing clients to make a second request to resolve a manufacturer. Acceptable here because a beer without its manufacturer context is rarely useful on its own.

#### `x-extensible-enum` for beer type:
Rather than a strict OpenAPI `enum`, the `type` field uses `x-extensible-enum`. This signals to clients that the listed values are the known set today, but the API may introduce new values in the future, so clients must handle unknown values gracefully rather than treating the list as closed.

#### RFC 7807 Problem Details for errors:
All error responses use `application/problem+json` aligned to RFC 7807. This gives consumers a stable, standardized error contract with machine-readable `type`, human-readable `detail`, and a traceable `instance` URI, instead of an ad-hoc message field whose structure varies by endpoint.

#### Offset/limit pagination over cursor-based:
Pagination uses `offset`/`limit` query parameters and returns `hasMore` alongside the result set. Cursor-based pagination would be more scalable under high write volume but adds implementation complexity not justified for a bounded beer catalogue. `hasMore` is preferred over `totalPages`/`totalElements` to avoid a `COUNT(*)` on every request.


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

| Tool | Summary |
|------|---------|
| Claude Code (claude-sonnet-4-6) | README skeleton and section boilerplate |
| Claude Code (claude-sonnet-4-6) | springdoc and openapi-generator-maven-plugin setup boilerplate; initial spec skeleton |
| Claude Code (claude-sonnet-4-6) | Brainstorming on pagination options; spec boilerplate for offset/limit switch |
| Claude Code (claude-sonnet-4-6) | RFC 7807 lookup and Problem schema boilerplate |
| Claude Code (claude-sonnet-4-6) | README rephrasing and copy edits |
