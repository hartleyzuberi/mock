# Funding Request Service

Assessment-sized Spring Boot service for creating, evaluating, retrieving, and filtering funding requests for service providers operating across five African countries.

## Design

The implementation separates HTTP concerns, application orchestration, eligibility rules, and persistence:

```text
HTTP Controller -> FundingRequestService -> EligibilityEvaluator
                         |                       |
                         v                       v
               JPA Repositories          pure business rules
                         |
                         v
             funding_requests / country_limits
```

Country limits are stored in the database and seeded by Flyway. `EligibilityEvaluator` receives a `CountryLimit` instead of containing country-specific amounts, so changing a limit does not require changing evaluation code.

## Technology

- Java 21
- Spring Boot
- Spring Web / Validation
- Spring Data JPA
- PostgreSQL
- Flyway
- JUnit 5 / MockMvc
- H2 in PostgreSQL compatibility mode for automated tests

## Run locally

### Prerequisites

- Java 21
- Maven 3.9+
- PostgreSQL

Create a database named `funding_db`, then configure connection values if they differ from the defaults:

```bash
export DB_URL=jdbc:postgresql://localhost:5432/funding_db
export DB_USER=postgres
export DB_PASSWORD=postgres
```

Windows PowerShell:

```powershell
$env:DB_URL="jdbc:postgresql://localhost:5432/funding_db"
$env:DB_USER="postgres"
$env:DB_PASSWORD="postgres"
```

Run:

```bash
mvn spring-boot:run
```

Flyway automatically creates the schema and seeds the five country limits.

## Tests

```bash
mvn clean verify
```

Automated tests use an isolated H2 database and run the same Flyway migration.

## API

### Create

```http
POST /api/v1/funding-requests
Content-Type: application/json
```

```json
{
  "organizationName": "Green Growers Ltd",
  "country": "Kenya",
  "requestedAmount": 50000.00,
  "organizationAgeYears": 3
}
```

### Evaluate

```http
POST /api/v1/funding-requests/{id}/evaluate
```

### Retrieve one

```http
GET /api/v1/funding-requests/{id}
```

### Retrieve/filter

```http
GET /api/v1/funding-requests
GET /api/v1/funding-requests?status=APPROVED
```

## Configurable country limits

Initial limits are inserted by `V1__create_schema_and_seed_country_limits.sql`:

| Country | Maximum amount |
|---|---:|
| Kenya | 100,000 |
| Uganda | 80,000 |
| Tanzania | 90,000 |
| Rwanda | 75,000 |
| Zambia | 85,000 |

Changing a limit is a data/configuration operation, for example:

```sql
UPDATE country_limits SET max_amount = 110000.00 WHERE country = 'KENYA';
```

No evaluation code changes are required.

## Validation and integrity

- organization name is required
- only supported countries are accepted
- requested amount must be positive
- organization age cannot be negative
- database checks mirror critical numeric invariants
- country is protected by a foreign key to configured limits
- request status is constrained in the database
- money uses `BigDecimal` / `NUMERIC(15,2)`

## Assumptions

- Amounts use the common unit implied by the assessment; currency conversion is outside scope.
- Evaluation uses the country limit that exists at evaluation time.
- Re-evaluation is allowed because the brief does not prohibit it; this makes changed limits immediately applicable.
- Rejection reasons are stored as a compact comma-separated decision field because the assessment has only two fixed rejection reasons. If decisions became richer/auditable, they should move to a normalized decision/history table.
- Authentication and authorization are outside scope.

## Scalability notes

The status column is indexed because filtering by status is a required access pattern. For substantially larger datasets, the list endpoint should become paginated and limits could be cached with explicit invalidation if reads became significant.

## Optional design question: external compliance API

I would introduce a small application-facing interface such as `ComplianceGateway` with a method that returns a compliance result for an organization. The funding evaluation service would depend on that interface, not on HTTP-specific code. A separate infrastructure adapter such as `RestComplianceClient` would implement the interface using Spring's HTTP client and configuration for the base URL/timeouts.

This keeps the business logic independent from the external provider, makes the integration replaceable, and lets unit tests use a stub/mock gateway without making network calls. The compliance check would be composed into the approval decision before a request is marked `APPROVED`, with explicit handling for timeouts and unavailable upstream services.

## Tradeoffs / future improvements

- Add pagination to the list endpoint at high volume.
- Add decision history/versioning if limits need effective dates or historical auditability.
- Add authentication/authorization if exposed beyond an internal trusted network.
- Add Testcontainers PostgreSQL tests if PostgreSQL-specific behaviour becomes important.
