# bcapi - Beer Catalogue API

---

## How to Run

### Locally

**Prerequisites:** Java 25+, Maven 3.9+

API interfaces and DTOs are generated from the OpenAPI spec at build time. Run this once before starting the application
or after any spec change:

```bash
./mvnw clean install -DskipTests
```

The `dev` profile activates the default credentials, seed data, and the H2 console.

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

The server starts on `http://localhost:8080`. API documentation is available at `http://localhost:8080/swagger-ui.html`.

**Run tests**

```bash
./mvnw test
```

**H2 console** (dev profile only)

Navigate to `http://localhost:8080/h2-console`. JDBC URL: `jdbc:h2:mem:testdb`.

---

### Docker

**Prerequisites:** Docker

```bash
./run-docker.sh
```

Builds the JAR, builds the image, and starts the container exposed on `http://localhost:8080`. Credentials are set via
environment variables; nothing is baked into the image.

---

### Kubernetes (Minikube + Helm)

**Prerequisites:** Java 25+, Maven 3.9+, Docker, [Minikube](https://minikube.sigs.k8s.io), [Helm](https://helm.sh)

**1. Start Minikube**

```bash
minikube start
```

**2. Build the image into Minikube's Docker daemon**

```bash
eval $(minikube docker-env)
./mvnw package -DskipTests
docker build -t bcapi:0.1.0 .
```

**3. Create credentials as a Kubernetes Secret**

Passwords are kept out of version control and supplied at deploy time.

```bash
kubectl create secret generic bcapi-user-secret \
  --from-literal=admin-password=<password> \
  --from-literal=manufacturer-password=<password> \
  --from-literal=brewer-password=<password>
```

**4. Deploy**

```bash
helm install bcapi ./helm/bcapi
```

**5. Access the application**

```bash
minikube service bcapi
```

**Upgrade after a code change**

```bash
eval $(minikube docker-env)
./mvnw package -DskipTests
docker build -t bcapi:0.2.0 .
helm upgrade bcapi ./helm/bcapi --set image.tag=0.2.0
```

**Tear down**

```bash
helm uninstall bcapi
kubectl delete secret bcapi-user-secret
```

---

## Assumptions

- A manufacturer must exist before a beer can be created or updated. `manufacturerId` is validated server-side and
  returns a `404` problem detail if the referenced manufacturer does not exist.
- A beer is always linked to exactly one manufacturer. Collaborations between manufacturers are out of scope.
- A manufacturer's `originCountry` is the country of legal registration, not necessarily where its beers are brewed or sold. One manufacturer, one country.
- Beer `type` is validated against a known BJCP-based list of styles. Unknown types are rejected.
- Only admin users can create manufacturers. Manufacturer users can update and delete their own manufacturers (those
  they are linked to via the `owner` column). Admin users can edit everything.
- Beer search matches across all fields (`name`, `type`, `abv`, manufacturer `name`) with equal weight. There is no
  relevance ranking. All matching results are treated equally.

---

## Design Decisions

### Project Setup

#### Package Structure

Code is organized by cohesive business domain (`beer`, `manufacturer`) rather than by technical layer. Each domain is
self-contained with its own web, service, and persistence concerns. Within each domain, a layered structure with
hexagonal influence is applied: the domain layer has no dependency on JPA or HTTP, and the persistence and web layers
depend on the domain, never the other way around. Package-private visibility is used to enforce encapsulation at the
layer boundary, ensuring that implementation details such as mappers and adapters cannot leak outside the package they
belong to.

Mapping between layers is handled by dedicated mapper classes rather than embedding transformation logic in the domain
objects themselves. Putting `from(Dto)` and `toDto()` methods directly on domain records is a valid pattern in richer
domain models, but was disregarded here as it would introduce a dependency on generated DTO types into the domain layer,
violating the inward dependency rule.

#### Repository Interface Granularity

The repository interface exposes all CRUD operations as methods on a single interface rather than splitting them into
per-operation functional interfaces (one interface per method). The Interface Segregation Principle would favor the
latter: services depend only on the operations they actually use, and each interface is independently mockable and
replaceable. In practice, the repository operations are tightly cohesive and splitting them would add file overhead
without a clear motivating need at this scale.

#### Testing Strategy

The test suite is deliberately heavy on full-stack integration tests for simplicity. `WebEnvironment.RANDOM_PORT` boots
the complete application context and exercises the full HTTP, security, and persistence stack in a single pass. This
makes tests straightforward to write and reason about, at the cost of speed and granularity.

Repository adapters are not tested in isolation. They are thin by design, delegating to JPA with a mapper call, and
contain no logic worth unit testing. The questions that matter (does the SQL work, do constraints hold, does the mapping
survive a round-trip) require a real database and are covered by the full-stack tests.

For repository-heavy functionality like search and sorting, a `@DataJpaTest` slice would be more appropriate: it spins
up only the persistence layer, runs faster, and makes query correctness the explicit subject of the test rather than a
side effect of an HTTP call. This is the natural next step as query complexity grows.

#### UUID and Timestamp Generation

UUIDs and timestamps are generated by Hibernate (`@UuidGenerator`, `@CreationTimestamp`, `@UpdateTimestamp`) rather than
by the database. JPA's identity management requires the application to know the generated ID immediately after insert;
purely DB-side generation would require an additional SELECT to retrieve it, adding complexity with no practical
benefit. The result is functionally equivalent since `@UuidGenerator` also produces UUIDv4. If Postgres-specific SQL
were introduced in migrations, H2 would need to be configured in `MODE=PostgreSQL` to maintain compatibility across both
databases.

#### OpenAPI Code Generator

API first approach ensures well thought through APIs and allows parallelization of workable units in a real world
setting. Generating code out of the API specification ensures the documentation reflects the implementation, reducing
the possibility for documentation drift.

Reduction of documentation drift can also be achieved through validating the api specification against the
implementation in integration tests, for example
with [swagger-request-validator](https://bitbucket.org/atlassian/swagger-request-validator/src/master/).
However, for the sake of simplicity and saving myself a bit of boilerplate, I decided to go with generating code out of
the spec.

#### Null Safety

Null handling is addressed at the API boundary via Bean Validation (`@NotNull`, `@NotBlank`) and at the domain level via
`Optional<T>` for absent values. Constructor-level null guards on service dependencies are omitted for brevity, but
would be a natural addition in a production codebase to fail fast and make dependency contracts explicit. Lombok's
`@NonNull` on constructor parameters is a clean way to generate these checks automatically without boilerplate.

---

### API Design

This API targets **[Richardson Maturity Model](https://martinfowler.com/articles/richardsonMaturityModel.html) Level 2
**. Resources are addressable via distinct URIs, HTTP verbs are used as intended (`GET` for safe reads, `POST`/`PUT`/
`DELETE` for state changes), and response codes carry semantic meaning. Level 3 (HATEOAS) is deliberately out of scope:
embedding hypermedia controls in every response would add significant complexity and payload overhead with little
practical benefit for a catalogue API where clients already know the resource structure.

#### UUID Identifiers

Resources use `string/uuid` rather than sequential integers as public identifiers. This decouples the internal database
identity from the API surface, prevents clients from inferring record counts or enumerating resources, and simplifies
future federation or migration scenarios where sequential IDs would collide.

#### Separate Request and Response Schemas

Request and response schemas are always distinct types. Response schemas carry server-assigned fields (`id`,
`createdAt`, `updatedAt`, embedded relations) marked `readOnly`; request schemas carry only client-supplied fields with
validation constraints. This makes the contract explicit: clients cannot accidentally submit server-owned fields, and
the two can evolve independently.

#### Separate Create and Update Request Schemas

Each resource has a dedicated `CreateRequest` and `UpdateRequest` schema rather than a single shared request type. This
allows create and update operations to diverge over time; for example, `manufacturerId` is required on
`BeerCreateRequest` to associate a beer with an existing manufacturer at creation time, while future requirements may
allow reassigning or locking it on updates. A single shared schema would force both operations to share the same
constraints, making the contract less expressive and harder to evolve.

#### `originCountry` as ISO 3166-1 alpha-2 String

The manufacturer's country of origin is stored and validated as a two-letter ISO 3166-1 alpha-2 code (e.g. `"DE"`,
`"AT"`). A richer alternative would be a `Country` value object that encapsulates validation and makes invalid states
unrepresentable, but a validated string is sufficient for this scope.

#### Embedded Manufacturer in Beer Response

The `Manufacturer` is inlined into the `Beer` representation rather than returned as a link or ID reference. This avoids
forcing clients to make a second request to resolve a manufacturer. Acceptable here because a beer without its
manufacturer context is rarely useful on its own.

#### `x-extensible-enum` for Beer Type

Rather than a strict OpenAPI `enum`, the `type` field uses `x-extensible-enum`. This signals to clients that the listed
values are the known set today, but the API may introduce new values in the future, so clients must handle unknown
values gracefully rather than treating the list as closed.

#### Beer Type Validation at the Application Layer

For simplicity, no database-level check constraint is added for beer `type`. In production this would be valuable as a
last line of defense against unknown values being written directly via migration scripts or tooling, independent of the
application. Omitted here to avoid maintaining the allowed values in two places.

#### RFC 7807 Problem Details for Errors

All error responses use `application/problem+json` aligned to RFC 7807. This gives consumers a stable, standardized
error contract with machine-readable `type`, human-readable `detail`, and a traceable `instance` URI, instead of an
ad-hoc message field whose structure varies by endpoint.

Error handling uses custom `RuntimeException` subclasses thrown from the service layer and caught centrally by
`@ControllerAdvice`. This is the accepted Spring standard but has a known trade-off: method signatures don't reveal what
can fail, making error paths implicit. An alternative is returning `Optional<T>` from service methods and unwrapping at
the controller, which makes absence explicit at the call site. The downside is that consistent
`application/problem+json` responses then have to be constructed at each unwrap site rather than once in the advice. A
more expressive future direction is Java sealed classes with pattern matching, where the compiler enforces exhaustive
handling of all outcomes, but this is a significant architectural shift not justified at this scope. For simplicity,
`@ControllerAdvice` was chosen as the single point of truth for error response formatting.

#### Beer Manufacturer Fetch Strategy

`BeerEntity` uses `FetchType.EAGER` for the `@ManyToOne` manufacturer association. Since `Beer` always embeds
`Manufacturer` in both the domain model and the API response, there is no use case where a beer is loaded without
needing the manufacturer. Keeping it `LAZY` would cause an N+1 problem; each beer in a page result would trigger a
separate SELECT to load its manufacturer. Fixing that with `LAZY` would require `@EntityGraph` on every repository
method that accesses manufacturer fields, which is error-prone and easy to forget. `EAGER` makes the intent explicit at
the mapping level.

#### Beer Search

Beer listing supports a single `search` query parameter that performs a case-insensitive `LIKE` match across `name`,
`type`, `abv`, and manufacturer `name` using JPA Specifications. Text fields use `lower(field) LIKE '%term%'`. ABV is
converted to string using Hibernate's built-in `str()` function, keeping the implementation clean without resorting to
manual parsing or DB-specific SQL.

The main trade-offs of this approach: a leading wildcard prevents index usage so every search is a full table scan,
acceptable for a bounded catalogue but would not scale to large datasets. Relevance is not considered, all matches are
treated equally. For production, Postgres `pg_trgm` trigram indexes or a dedicated search engine (Elasticsearch) would
address both concerns.

#### Page/Size Pagination over Cursor-Based

Pagination uses `page`/`size` query parameters and returns `hasMore` alongside the result set. Cursor-based pagination
would be more scalable under high write volume but adds implementation complexity not justified for a bounded beer
catalogue. `hasMore` is preferred over `totalPages`/`totalElements` to avoid a `COUNT(*)` on every request.

Offset/limit was considered as it gives clients more flexibility over the starting position, but it does not map cleanly
to Spring Data's page-based model. Arbitrary offsets that are not a multiple of the page size silently return incorrect
results, making the contract misleading. Page/size was chosen for simplicity, aligning naturally with Spring Data's
page-based model.

---

### Security

#### Roles and Authorization

Three roles are enforced: anonymous users have read-only access, manufacturer users can create beers and edit their own
data, and admin users can edit everything. Method-level authorization via `@PreAuthorize` lives in the service layer so
rules apply regardless of how the service is called. A `RoleHierarchy` of `ADMIN > MANUFACTURER` means admins implicitly
satisfy any manufacturer-level check without redundant annotations.

#### Ownership Model

Each manufacturer has a single owning user, stored as an `owner` column on the `manufacturers` table. Multi-user
ownership per manufacturer is deliberately out of scope for simplicity. Ownership checks use Spring Security's
`hasPermission()` in SpEL expressions, which delegates to `OwnershipPermissionEvaluator`. The evaluator checks the admin
role first (avoiding a DB query) and falls back to an ownership lookup against the `manufacturers` table. Beer write
access is resolved transitively through the beer's manufacturer.

`@AuthenticationPrincipal` was ruled out because controller methods implement generated interfaces and cannot carry
extra parameters not present in the interface signature. `@PreAuthorize` on the service is the idiomatic alternative and
keeps authorization entirely out of the web layer.

#### Authentication

HTTP Basic is used deliberately for simplicity; it keeps the focus on domain logic and API design without the overhead
of a token infrastructure. Credentials are externalized to configuration and can be overridden via environment variables
at deploy time, with no credentials baked into the image.

The natural next step for production is JWT via Spring Security OAuth2 Resource Server. The service-layer authorization
annotations stay unchanged; only the authentication mechanism swaps out, replacing `httpBasic()` with
`oauth2ResourceServer().jwt()` pointed at a JWKS endpoint.

Integration tests run with `WebEnvironment.RANDOM_PORT` and real Basic auth credentials to exercise the full HTTP and
security filter stack, including authentication and authorization.

---

## API Usage Examples

Examples below assume the server is running locally on port 8080 with the `dev` profile, which loads seed data and
default credentials.

### Manufacturers

**List manufacturers**

```bash
curl http://localhost:8080/api/manufacturers
```

**Get a manufacturer by ID**

```bash
curl http://localhost:8080/api/manufacturers/a0000000-0000-0000-0000-000000000002
```

**Create a manufacturer** (admin only)

```bash
curl -X POST http://localhost:8080/api/manufacturers \
  -u admin:admin \
  -H "Content-Type: application/json" \
  -d '{"name": "Cloudwater Brew Co.", "originCountry": "GB"}'
```

**Update a manufacturer** (admin or owner)

```bash
curl -X PUT http://localhost:8080/api/manufacturers/a0000000-0000-0000-0000-000000000002 \
  -u manufacturer:manufacturer \
  -H "Content-Type: application/json" \
  -d '{"name": "BrewDog", "originCountry": "GB"}'
```

**Delete a manufacturer** (admin or owner)

```bash
curl -X DELETE http://localhost:8080/api/manufacturers/a0000000-0000-0000-0000-000000000002 \
  -u admin:admin
```

---

### Beers

**List beers**

```bash
curl http://localhost:8080/api/beers
```

**List beers with search, sorting, and pagination**

```bash
curl "http://localhost:8080/api/beers?search=ipa&sortBy=abv&sortDirection=desc&page=0&size=5"
```

**Get a beer by ID**

```bash
curl http://localhost:8080/api/beers/b0000000-0000-0000-0000-000000000003
```

**Create a beer** (manufacturer or admin)

```bash
curl -X POST http://localhost:8080/api/beers \
  -u manufacturer:manufacturer \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Neon Rays",
    "type": "IPA",
    "abv": 6.5,
    "description": "Hazy session IPA with tropical notes.",
    "manufacturerId": "a0000000-0000-0000-0000-000000000002"
  }'
```

**Update a beer** (admin or owner of the beer's manufacturer)

```bash
curl -X PUT http://localhost:8080/api/beers/b0000000-0000-0000-0000-000000000003 \
  -u manufacturer:manufacturer \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Punk IPA",
    "type": "IPA",
    "abv": 5.6,
    "description": "Post-modern classic. Bitter, fresh and hoppy.",
    "manufacturerId": "a0000000-0000-0000-0000-000000000002"
  }'
```

**Delete a beer** (admin or owner of the beer's manufacturer)

```bash
curl -X DELETE http://localhost:8080/api/beers/b0000000-0000-0000-0000-000000000003 \
  -u manufacturer:manufacturer
```

---

## Not Implemented

### Beer Picture Upload

Each beer could expose a picture as an image sub-resource: `POST /api/beers/{id}/picture` accepting `multipart/form-data`, and `GET /api/beers/{id}/picture` to retrieve it. The picture URL would be stored alongside the beer record.

Locally and in Docker, images would be written to the filesystem. In a production environment the upload would be delegated to an S3 bucket; the application writes to S3 and stores the resulting object URL, keeping binary data out of the database. The API contract stays identical across environments; only the backing storage changes.

### AWS-Hosted Database (RDS)

Connecting to a PostgreSQL instance on AWS RDS requires swapping the H2 datasource configuration for a Postgres JDBC URL, username, and password. The credentials would follow the same pattern already in place for user passwords: stored as a Kubernetes Secret out-of-band and referenced in the Helm deployment via `secretKeyRef`. A `values.yaml` entry for the RDS endpoint (non-sensitive) and a secret reference for the credentials is all the wiring needed. The application code and migrations require no changes.

---

## AI Usage Log

| Tool                            | Summary                                                                                      |
|---------------------------------|----------------------------------------------------------------------------------------------|
| Claude Code (claude-sonnet-4-6) | README skeleton and section boilerplate                                                      |
| Claude Code (claude-sonnet-4-6) | springdoc and openapi-generator-maven-plugin setup boilerplate; initial spec skeleton        |
| Claude Code (claude-sonnet-4-6) | Brainstorming on pagination options; spec boilerplate for offset/limit switch                |
| Claude Code (claude-sonnet-4-6) | RFC 7807 lookup and Problem schema boilerplate                                               |
| Claude Code (claude-sonnet-4-6) | README rephrasing and copy edits                                                             |
| Claude Code (claude-sonnet-4-6) | Liquibase setup guidance and initial migration boilerplate                                   |
| Claude Code (claude-sonnet-4-6) | Brainstorming on role-based access, auth mechanism trade-offs, and security design decisions |
| Claude Code (claude-sonnet-4-6) | Brainstorming on package structure, layering, encapsulation trade-offs, and mapping strategy |
| Claude Code (claude-sonnet-4-6) | Test case outlines for integration tests, validators, and entity mapper                      |
| Claude Code (claude-sonnet-4-6) | ManufacturerEntityMapper implementation and entity constructor boilerplate                   |
| Claude Code (claude-sonnet-4-6) | Brainstorming on error handling, null safety, testing strategy, and UUID choices             |
| Claude Code (claude-sonnet-4-6) | Role-based access: @PreAuthorize + hasPermission() + OwnershipPermissionEvaluator            |
| Claude Code (claude-sonnet-4-6) | helm & minikube setup guidance                                                               |
| Claude Code (claude-sonnet-4-6) | README cleanup                                                                               |
