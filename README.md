# Funding Request Service

A small Spring Boot backend created for a software-engineering technical assessment.

The service allows a user or another system to:

- create funding requests
- evaluate each request against eligibility rules
- retrieve a request by ID
- retrieve all requests
- filter requests by status

This project is **backend only**. It does not include a React, HTML, or LWC user interface. The API can be tested with Postman, curl, or another HTTP client.

---

## 1. Business Rules

Every new funding request starts with status:

```text
PENDING
```

A request is approved only when both conditions are true:

1. the organization has operated for at least 2 years
2. the requested amount does not exceed the configured maximum for its country

Otherwise the request is rejected.

Possible statuses are:

```text
PENDING
APPROVED
REJECTED
```

Possible rejection reasons are:

```text
ORGANIZATION_TOO_YOUNG
AMOUNT_EXCEEDS_COUNTRY_LIMIT
```

If more than one rule fails, more than one rejection reason can be returned.

---

## 2. Configurable Country Limits

The initial limits are:

| Country | Maximum Amount |
|---|---:|
| Kenya | 100,000 |
| Uganda | 80,000 |
| Tanzania | 90,000 |
| Rwanda | 75,000 |
| Zambia | 85,000 |

These values are stored in the `country_limits` database table instead of being hardcoded inside the eligibility logic.

For example, changing Kenya's maximum from `100000` to `110000` only requires a data change:

```sql
UPDATE country_limits
SET max_amount = 110000.00
WHERE country = 'KENYA';
```

No change to `EligibilityEvaluator` is required.

---

## 3. Technology

- Java 21
- Spring Boot
- Spring Web
- Bean Validation
- Spring Data JPA
- PostgreSQL
- Flyway database migrations
- JUnit 5
- MockMvc
- H2 for automated tests
- Maven
- GitHub Actions

---

## 4. Project Architecture

The code intentionally separates HTTP concerns, application orchestration, business rules, and persistence.

```text
Postman / HTTP Client
        |
        v
FundingRequestController
        |
        v
FundingRequestService
        |
        +--------------------+
        |                    |
        v                    v
EligibilityEvaluator    JPA Repositories
  business rules              |
                              v
                    PostgreSQL Database
```

### Main responsibilities

**FundingRequestController**

- receives HTTP requests
- validates request bodies
- delegates work to the service
- returns HTTP responses

**FundingRequestService**

- coordinates application operations
- loads funding requests
- loads country configuration
- calls the evaluator
- persists decisions

**EligibilityEvaluator**

- contains the eligibility rules
- does not know the actual Kenya/Uganda/etc. limit values
- can be unit-tested without HTTP or database access

**Repositories**

- provide persistence through Spring Data JPA

**Flyway**

- creates the database schema
- inserts the initial country limits

---

# 5. Run Locally

## Prerequisites

Install and verify:

- Java 21
- Maven 3.9+
- PostgreSQL

Check them from a terminal:

```powershell
java -version
javac -version
mvn -version
psql --version
```

The expected Java version is Java 21.

---

## 5.1 Clone the Repository

```powershell
cd C:\dev
git clone https://github.com/hartleyzuberi/mock.git
cd mock
```

If the repository already exists locally:

```powershell
cd C:\dev\mock
git pull origin main
```

---

## 5.2 Create the PostgreSQL Database

Connect to PostgreSQL:

```powershell
psql -U postgres
```

Then create the application database:

```sql
CREATE DATABASE funding_db;
```

Exit PostgreSQL:

```text
\q
```

You only need to create the database itself.

**Do not manually create the application tables.** Flyway will create them automatically when Spring Boot starts.

---

## 5.3 Configure Database Connection

The application uses these defaults:

```text
Database: funding_db
Host: localhost
Port: 5432
User: postgres
Password: postgres
```

If your PostgreSQL password is different, set environment variables before starting the application.

### Windows PowerShell

```powershell
$env:DB_URL="jdbc:postgresql://localhost:5432/funding_db"
$env:DB_USER="postgres"
$env:DB_PASSWORD="YOUR_POSTGRES_PASSWORD"
```

Do not commit your real password to Git.

### Linux/macOS

```bash
export DB_URL=jdbc:postgresql://localhost:5432/funding_db
export DB_USER=postgres
export DB_PASSWORD=YOUR_POSTGRES_PASSWORD
```

---

## 5.4 Run the Automated Tests First

Before starting the backend, verify the project:

```powershell
mvn clean verify
```

A successful run ends with:

```text
BUILD SUCCESS
```

Automated tests use an isolated H2 database, so they do not require your local PostgreSQL database to be running.

---

## 5.5 Start the Backend

```powershell
mvn spring-boot:run
```

During startup, Flyway automatically runs the migration:

```text
V1__create_schema_and_seed_country_limits.sql
```

It creates:

```text
country_limits
funding_requests
```

and inserts the five initial country limits.

When startup succeeds, Spring Boot runs on:

```text
http://localhost:8080
```

There is no homepage because this is a REST backend, not a frontend application.

---

# 6. Test the API with Postman

## 6.1 Create a Funding Request

Method:

```http
POST
```

URL:

```text
http://localhost:8080/api/v1/funding-requests
```

Body → raw → JSON:

```json
{
  "organizationName": "Green Growers Ltd",
  "country": "Kenya",
  "requestedAmount": 50000.00,
  "organizationAgeYears": 3
}
```

Example response:

```json
{
  "id": 1,
  "organizationName": "Green Growers Ltd",
  "country": "Kenya",
  "requestedAmount": 50000.00,
  "organizationAgeYears": 3,
  "status": "PENDING",
  "decisionReasons": [],
  "createdAt": "2026-08-30T09:00:00Z"
}
```

Remember the returned `id` because it is used by the next requests.

Supported country values include the country name, enum name, or country code. For example:

```text
Kenya
KENYA
KE
```

---

## 6.2 Evaluate the Request

Assuming the request ID is `1`:

```http
POST http://localhost:8080/api/v1/funding-requests/1/evaluate
```

No request body is required.

For the example above, the result should be approved because:

```text
organization age = 3 years
required minimum = 2 years

requested amount = 50,000
Kenya maximum = 100,000
```

Example response:

```json
{
  "requestId": 1,
  "status": "APPROVED",
  "reasons": []
}
```

---

## 6.3 Retrieve One Request

```http
GET http://localhost:8080/api/v1/funding-requests/1
```

---

## 6.4 Retrieve All Requests

```http
GET http://localhost:8080/api/v1/funding-requests
```

---

## 6.5 Filter by Status

Approved requests:

```http
GET http://localhost:8080/api/v1/funding-requests?status=APPROVED
```

Rejected requests:

```http
GET http://localhost:8080/api/v1/funding-requests?status=REJECTED
```

Pending requests:

```http
GET http://localhost:8080/api/v1/funding-requests?status=PENDING
```

---

# 7. Useful Manual Test Cases

## Approved: Below Limit

```json
{
  "organizationName": "Example Organization",
  "country": "Kenya",
  "requestedAmount": 99999.99,
  "organizationAgeYears": 3
}
```

Expected after evaluation:

```text
APPROVED
```

## Approved: Exactly at Limit

```json
{
  "organizationName": "Example Organization",
  "country": "Kenya",
  "requestedAmount": 100000.00,
  "organizationAgeYears": 2
}
```

Expected:

```text
APPROVED
```

## Rejected: Amount Above Limit

```json
{
  "organizationName": "Example Organization",
  "country": "Kenya",
  "requestedAmount": 100000.01,
  "organizationAgeYears": 3
}
```

Expected reason:

```text
AMOUNT_EXCEEDS_COUNTRY_LIMIT
```

## Rejected: Organization Too Young

```json
{
  "organizationName": "New Organization",
  "country": "Kenya",
  "requestedAmount": 50000.00,
  "organizationAgeYears": 1
}
```

Expected reason:

```text
ORGANIZATION_TOO_YOUNG
```

## Rejected: Two Rules Fail

```json
{
  "organizationName": "New Large Request",
  "country": "Kenya",
  "requestedAmount": 120000.00,
  "organizationAgeYears": 1
}
```

Expected reasons:

```text
ORGANIZATION_TOO_YOUNG
AMOUNT_EXCEEDS_COUNTRY_LIMIT
```

---

# 8. Validation

The API rejects invalid input including:

- blank organization name
- unsupported country
- zero requested amount
- negative requested amount
- negative organization age

Important numeric rules are also protected at database level using constraints.

Money is represented with:

```text
Java: BigDecimal
Database: NUMERIC(15,2)
```

This avoids floating-point errors for financial values.

---

# 9. Database Schema

Flyway creates two tables.

## `country_limits`

Stores configurable business policy:

```text
country
max_amount
```

## `funding_requests`

Stores submitted requests and their decisions:

```text
id
organization_name
country
requested_amount
organization_age_years
status
decision_reason
created_at
```

The database includes relevant checks, a foreign key from funding requests to configured countries, and an index on status.

---

# 10. Automated Testing

Run all tests:

```powershell
mvn clean verify
```

The tests cover important behavior including:

- amount below the limit
- amount exactly equal to the limit
- amount above the limit
- organization younger than two years
- organization exactly two years old
- multiple rejection reasons
- API validation
- request creation
- evaluation
- retrieval
- filtering
- not-found behavior

The pure eligibility rules are tested separately from the HTTP/database layer.

---

# 11. GitHub Actions

The repository contains:

```text
.github/workflows/ci.yml
```

The CI workflow runs Maven verification automatically so changes can be checked by GitHub as well as locally.

---

# 12. Common Problems

## `mvn` is not recognized

Verify Maven:

```powershell
mvn -version
```

If Windows cannot find Maven, ensure `%MAVEN_HOME%\bin` is present in the Windows `Path` environment variable and then open a new PowerShell window.

## PostgreSQL password authentication failed

Example:

```text
FATAL: password authentication failed for user "postgres"
```

This means PostgreSQL is running but the supplied username/password combination is not accepted. Verify or reset the PostgreSQL password before starting Spring Boot.

## Database does not exist

If startup reports that `funding_db` does not exist, create it:

```sql
CREATE DATABASE funding_db;
```

## Port 8080 is already in use

Use another port:

```powershell
$env:PORT="8081"
mvn spring-boot:run
```

The API will then be available at:

```text
http://localhost:8081
```

---

# 13. Assumptions and Tradeoffs

- The assessment provides one common monetary unit; currency conversion is outside scope.
- Evaluation uses the country limit configured at evaluation time.
- Re-evaluation is currently allowed because the assessment does not prohibit it.
- Rejection reasons are stored as a compact comma-separated field because only two fixed reasons currently exist.
- Authentication and authorization are outside the assessment scope.
- The list endpoint is intentionally simple and unpaginated for the assessment.

For a larger production system, likely extensions would include pagination, decision history, effective-dated country limits, authentication/authorization, auditing, and PostgreSQL Testcontainers.

---

# 14. Optional Design Question: External Compliance API

If approval later requires checking an external compliance service, the business logic should not directly depend on HTTP code.

A clean extension would introduce an application-facing abstraction such as:

```text
ComplianceGateway
```

with a concrete HTTP adapter such as:

```text
RestComplianceClient
```

Conceptually:

```text
FundingRequestService
        |
        +----> EligibilityEvaluator
        |
        +----> ComplianceGateway
                     |
                     v
             RestComplianceClient
                     |
                     v
           External REST Service
```

This keeps external integration details outside the core eligibility rules, allows the client to be replaced later, and makes automated testing possible using a stub or mock instead of real network calls.
