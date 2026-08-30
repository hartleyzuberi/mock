# Funding Request Service — First Spring Boot Application Guide

This repository contains a small **Spring Boot REST API** for creating and evaluating funding requests.

It was originally built as a software-engineering technical-assessment exercise, but this README is also written as a **from-zero learning guide**.

If this is your first Spring Boot application, the goal is not only to run the finished code. The goal is to understand:

- what Spring Boot generates for you
- what you must code yourself
- why the folders exist
- what order to build the application in
- how HTTP, Java, Spring, JPA, Flyway and PostgreSQL work together
- how to test each layer before moving to the next one

---

# 1. What Are We Building?

A service provider submits a funding request containing:

- organization name
- country
- requested amount
- organization age in years

A new request starts as:

```text
PENDING
```

It becomes `APPROVED` only when:

1. the organization is at least 2 years old
2. the requested amount does not exceed the configured country limit

Otherwise it becomes:

```text
REJECTED
```

Possible rejection reasons are:

```text
ORGANIZATION_TOO_YOUNG
AMOUNT_EXCEEDS_COUNTRY_LIMIT
```

This is a **backend-only** project. There is no React, HTML or Lightning Web Component frontend.

We interact with it using Postman, curl, automated tests, or another HTTP client.

---

# 2. First Mental Model: What Is Spring Boot?

Java is the programming language.

Spring Boot is a Java framework that handles much of the infrastructure required to build a backend application.

Without Spring Boot, you would have to manually configure many things such as:

- HTTP server setup
- request routing
- JSON serialization
- dependency management
- database integration
- dependency injection
- validation infrastructure

Spring Boot gives us these building blocks so we can focus on the application's business logic.

Think of the final application like this:

```text
Postman / Client
       |
       | HTTP
       v
Spring Boot Controller
       |
       v
Application Service
       |
       +---------------------+
       |                     |
       v                     v
EligibilityEvaluator     Repository
business rules               |
                             v
                         PostgreSQL
```

---

# 3. Do I Manually Create the Original Spring Files?

Usually, **no**.

For a new Spring Boot application, the normal workflow is:

```text
Generate Spring Boot skeleton
          ↓
Open it in VS Code / IntelliJ
          ↓
Run the empty application once
          ↓
Create your own packages/classes
          ↓
Add database configuration
          ↓
Implement business logic
          ↓
Add API endpoints
          ↓
Add tests
```

The official project generator is called **Spring Initializr**.

You can use it in several ways.

## Option A — Spring Initializr Website

Open:

```text
https://start.spring.io
```

Choose approximately:

```text
Project: Maven
Language: Java
Spring Boot: 3.x
Group: com.aceli
Artifact: funding-request-service
Name: funding-request-service
Package name: com.aceli.mock
Packaging: Jar
Java: 21
```

Add these dependencies:

```text
Spring Web
Spring Data JPA
Validation
PostgreSQL Driver
Flyway Migration
H2 Database
```

Then click **Generate**.

Spring Initializr downloads a ZIP containing the starter project.

Extract it and open the folder in VS Code.

## Option B — VS Code Spring Initializr

If you have the Spring Boot extensions installed in VS Code:

```text
Ctrl + Shift + P
```

Search for:

```text
Spring Initializr: Create a Maven Project
```

Then select Java 21 and the same dependencies listed above.

## Option C — Command Line

You can also generate a project directly from Spring Initializr using a command such as:

```powershell
curl.exe "https://start.spring.io/starter.zip?type=maven-project&language=java&javaVersion=21&groupId=com.aceli&artifactId=funding-request-service&name=funding-request-service&packageName=com.aceli.mock&dependencies=web,data-jpa,validation,postgresql,flyway,h2" -o funding-request-service.zip
```

Then extract it:

```powershell
Expand-Archive .\funding-request-service.zip -DestinationPath .\funding-request-service
cd .\funding-request-service
```

For a first Spring application, **Spring Initializr through the website or VS Code is easier to understand**.

---

# 4. What Does Spring Initializr Generate?

A generated Maven Spring Boot project normally starts roughly like this:

```text
funding-request-service/
│
├── .mvn/
├── mvnw
├── mvnw.cmd
├── pom.xml
│
└── src/
    ├── main/
    │   ├── java/
    │   │   └── com/aceli/mock/
    │   │       └── FundingRequestApplication.java
    │   │
    │   └── resources/
    │       └── application.properties
    │
    └── test/
        └── java/
            └── com/aceli/mock/
                └── FundingRequestApplicationTests.java
```

The important point is:

```text
Spring Initializr creates the skeleton.
YOU create the actual business application.
```

You would normally create these packages yourself:

```text
domain/
repository/
service/
web/
exception/
```

and these application-specific classes yourself:

```text
Country
CountryLimit
FundingRequest
RequestStatus
RejectionReason
FundingRequestRepository
CountryLimitRepository
EligibilityEvaluator
FundingRequestService
FundingRequestController
FundingRequestDtos
FundingRequestException
GlobalExceptionHandler
```

---

# 5. Our Final Project Structure

This repository now looks conceptually like this:

```text
mock/
│
├── .github/
│   └── workflows/
│       └── ci.yml
│
├── .env                 # local secrets - never commit
├── .env.example         # safe example configuration
├── .gitignore
├── pom.xml
├── README.md
│
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/aceli/mock/
│   │   │       ├── FundingRequestApplication.java
│   │   │       │
│   │   │       ├── domain/
│   │   │       │   ├── Country.java
│   │   │       │   ├── CountryLimit.java
│   │   │       │   ├── FundingRequest.java
│   │   │       │   ├── RejectionReason.java
│   │   │       │   └── RequestStatus.java
│   │   │       │
│   │   │       ├── repository/
│   │   │       │   ├── CountryLimitRepository.java
│   │   │       │   └── FundingRequestRepository.java
│   │   │       │
│   │   │       ├── service/
│   │   │       │   ├── EligibilityEvaluator.java
│   │   │       │   └── FundingRequestService.java
│   │   │       │
│   │   │       ├── web/
│   │   │       │   ├── FundingRequestController.java
│   │   │       │   └── FundingRequestDtos.java
│   │   │       │
│   │   │       └── exception/
│   │   │           ├── FundingRequestException.java
│   │   │           └── GlobalExceptionHandler.java
│   │   │
│   │   └── resources/
│   │       ├── application.yml
│   │       └── db/
│   │           └── migration/
│   │               └── V1__create_schema_and_seed_country_limits.sql
│   │
│   └── test/
│       ├── java/
│       │   └── com/aceli/mock/
│       │       ├── service/
│       │       │   └── EligibilityEvaluatorTest.java
│       │       └── web/
│       │           └── FundingRequestControllerIntegrationTest.java
│       │
│       └── resources/
│           └── application-test.yml
│
└── target/              # generated by Maven; do not edit
```

Remember this distinction:

```text
src/main = real application
src/test = automated tests
target   = generated build output
```

---

# 6. Build It Yourself — Recommended Manual Coding Order

If you were given this assessment and had to build it manually, this is a sensible order.

Do **not** start by writing every file at once.

Use this loop:

```text
create a small piece
      ↓
compile/test it
      ↓
fix problems
      ↓
continue
```

The recommended sequence is:

```text
1. Generate Spring project
2. Run empty project
3. Configure database
4. Create domain enums
5. Create entities
6. Create Flyway migration
7. Create repositories
8. Create evaluator
9. Unit-test evaluator
10. Create DTOs
11. Create service
12. Create exceptions
13. Create controller
14. Run application
15. Test manually with Postman
16. Add integration tests
17. Run mvn clean verify
18. Add README / CI / final polish
```

The following sections explain that flow in detail.

---

# 7. Step 1 — Generate and Run the Empty Spring Project

After generating the project, first verify Java and Maven:

```powershell
java -version
javac -version
mvn -version
```

Then run the generated project **before writing business code**:

```powershell
mvn spring-boot:run
```

At this point there may be no useful endpoint yet.

That is fine.

The objective is simply to prove:

```text
Java works
Maven works
Dependencies download
Spring starts
Tomcat starts
```

If the starter project cannot run, fix that before building business logic.

---

# 8. Step 2 — Understand `pom.xml`

`pom.xml` is Maven's project definition.

It tells Maven:

- project name/version
- Java version
- dependencies
- build plugins

This project uses Java 21 and Spring Boot.

The important dependencies are conceptually:

```text
spring-boot-starter-web
    HTTP server, controllers, REST/JSON

spring-boot-starter-data-jpa
    ORM/database repositories

spring-boot-starter-validation
    input validation

postgresql
    PostgreSQL JDBC driver

flyway-core
flyway-database-postgresql
    database migrations

h2
    temporary database used during tests

spring-boot-starter-test
    JUnit, MockMvc, AssertJ, testing support
```

You normally do not download these JAR files yourself.

Maven reads `pom.xml` and downloads them for you.

---

# 9. Step 3 — Understand the Main Application Class

Spring Initializr generates a class similar to:

```java
@SpringBootApplication
public class FundingRequestApplication {
    public static void main(String[] args) {
        SpringApplication.run(FundingRequestApplication.class, args);
    }
}
```

Normal Java applications begin at:

```java
public static void main(String[] args)
```

Spring Boot is still Java.

This line:

```java
SpringApplication.run(FundingRequestApplication.class, args);
```

roughly starts:

```text
Spring container
component scanning
dependency injection
database configuration
Tomcat web server
controllers
repositories
Flyway
```

`@SpringBootApplication` tells Spring this is the application's root configuration class.

---

# 10. Step 4 — Create the Packages

Under:

```text
src/main/java/com/aceli/mock/
```

create:

```text
domain
repository
service
web
exception
```

In PowerShell, you could create them with:

```powershell
New-Item -ItemType Directory -Force src\main\java\com\aceli\mock\domain
New-Item -ItemType Directory -Force src\main\java\com\aceli\mock\repository
New-Item -ItemType Directory -Force src\main\java\com\aceli\mock\service
New-Item -ItemType Directory -Force src\main\java\com\aceli\mock\web
New-Item -ItemType Directory -Force src\main\java\com\aceli\mock\exception
```

You do not need to use commands; creating the folders/packages from VS Code is fine.

The purpose of these packages is:

```text
domain      business concepts/data
repository  persistence/database access
service     business use cases/rules
web         HTTP/API boundary
exception   error handling
```

---

# 11. Step 5 — Start With the Simplest Domain Types

Do not begin with controllers.

Begin by modeling the business.

## 11.1 `RequestStatus`

Create:

```text
src/main/java/com/aceli/mock/domain/RequestStatus.java
```

```java
public enum RequestStatus {
    PENDING,
    APPROVED,
    REJECTED
}
```

Why an enum instead of a `String`?

Because a string could contain anything:

```text
approved
APPROVVED
banana
```

An enum restricts the valid states to the business states we actually support.

## 11.2 `RejectionReason`

Create:

```text
src/main/java/com/aceli/mock/domain/RejectionReason.java
```

with the two rule-failure codes:

```text
ORGANIZATION_TOO_YOUNG
AMOUNT_EXCEEDS_COUNTRY_LIMIT
```

## 11.3 `Country`

Create the supported countries as an enum.

The current implementation also stores:

```text
KENYA -> KE -> Kenya
UGANDA -> UG -> Uganda
...
```

This lets the API accept friendly names/codes while Java still uses a controlled enum internally.

After these classes exist, compile:

```powershell
mvn test -DskipTests
```

or simply:

```powershell
mvn compile
```

Fix compile errors before continuing.

---

# 12. Step 6 — Model the Database Entities

Now create the Java objects that represent persisted business data.

## 12.1 `CountryLimit`

A country limit contains:

```text
country
maxAmount
```

It maps to the PostgreSQL table:

```text
country_limits
```

Important annotations:

```java
@Entity
```

means:

> this Java class is a JPA entity that can be persisted.

```java
@Table(name = "country_limits")
```

means:

> map this entity to that database table.

```java
@Id
```

means:

> this field is the table primary key.

Use:

```java
BigDecimal
```

for money rather than `double`.

## 12.2 `FundingRequest`

Model the fields from the requirement:

```text
id
organizationName
country
requestedAmount
organizationAgeYears
status
decisionReason
createdAt
```

The entity should initialize a new request with:

```text
status = PENDING
createdAt = current time
```

Instead of exposing setters for every field, use meaningful methods such as:

```java
FundingRequest.create(...)
```

and:

```java
applyDecision(...)
```

This is **encapsulation**: the object protects its own valid state transitions instead of letting any code change anything arbitrarily.

---

# 13. Step 7 — Create the Database Schema With Flyway

Do not rely on Hibernate automatically changing your production schema.

Create:

```text
src/main/resources/db/migration/
```

then:

```text
V1__create_schema_and_seed_country_limits.sql
```

Flyway naming is important:

```text
V1__description.sql
^^  ^^
|   human-readable migration name
migration version
```

The migration creates:

```text
country_limits
funding_requests
```

and adds important database constraints such as:

```text
requested_amount > 0
organization_age_years >= 0
status must be PENDING/APPROVED/REJECTED
foreign key from funding request country to country_limits
```

It also inserts the initial country limits.

This is where the changing configuration lives:

| Country | Maximum amount |
|---|---:|
| Kenya | 100,000 |
| Uganda | 80,000 |
| Tanzania | 90,000 |
| Rwanda | 75,000 |
| Zambia | 85,000 |

The evaluator should **not** contain these numbers.

---

# 14. Step 8 — Configure PostgreSQL and `.env`

First create only the database:

```powershell
psql -U postgres -h 127.0.0.1
```

Then:

```sql
CREATE DATABASE funding_db;
```

Exit:

```text
\q
```

Do not manually create the tables. Flyway will do that.

Create a local `.env` from the example:

```powershell
Copy-Item .env.example .env
```

Edit `.env`:

```properties
DB_URL=jdbc:postgresql://localhost:5432/funding_db
DB_USER=postgres
DB_PASSWORD=YOUR_REAL_POSTGRES_PASSWORD
PORT=8080
```

`application.yml` imports the optional `.env` file and maps these values into Spring's datasource configuration.

Never commit your real `.env`.

The `.gitignore` protects it.

---

# 15. Step 9 — Create the Repositories

Now give the application a persistence boundary.

## `CountryLimitRepository`

Conceptually:

```java
public interface CountryLimitRepository
        extends JpaRepository<CountryLimit, Country> {
}
```

Spring Data JPA automatically provides methods such as:

```text
findById
findAll
save
delete
```

You do not write the repository implementation yourself.

Spring creates it at runtime.

## `FundingRequestRepository`

It extends:

```text
JpaRepository<FundingRequest, Long>
```

and defines query methods such as:

```java
findAllByStatusOrderByCreatedAtDesc(...)
```

Spring interprets this method name and builds the query.

Conceptually it means:

```sql
SELECT ...
FROM funding_requests
WHERE status = ?
ORDER BY created_at DESC;
```

At this point your persistence structure exists, but the application still needs business logic.

---

# 16. Step 10 — Implement the Business Rule Before the API

This is one of the most important habits in this project.

Create:

```text
service/EligibilityEvaluator.java
```

The evaluator receives:

```text
FundingRequest
CountryLimit
```

and returns:

```text
EvaluationDecision
```

The logic is intentionally small:

```text
if age < 2
    reject for ORGANIZATION_TOO_YOUNG

if requestedAmount > countryLimit.maxAmount
    reject for AMOUNT_EXCEEDS_COUNTRY_LIMIT

if no reasons
    APPROVED
else
    REJECTED
```

The crucial design choice is what is **not** inside this class.

Wrong design:

```java
if (country == KENYA && amount > 100000) ...
if (country == UGANDA && amount > 80000) ...
```

Better design:

```java
requestedAmount.compareTo(countryLimit.getMaxAmount()) > 0
```

The evaluator knows the rule:

> amount must not exceed the configured country limit.

It does not know the current value of Kenya's limit.

That is how we satisfy:

> changing a country's limit must not require modifying evaluation logic.

---

# 17. Step 11 — Immediately Unit-Test the Evaluator

Before writing a controller, test the highest-risk business logic.

Create:

```text
src/test/java/com/aceli/mock/service/EligibilityEvaluatorTest.java
```

Test at least:

```text
99,999.99  -> approved
100,000.00 -> approved
100,000.01 -> rejected
age 1      -> rejected
age 2      -> approved
both fail  -> two rejection reasons
```

Run only this class:

```powershell
mvn -Dtest=EligibilityEvaluatorTest test
```

Why do this early?

Because if these tests pass, you know the core business rule works before HTTP, JSON and PostgreSQL introduce more moving parts.

---

# 18. Step 12 — Create API DTOs

Now define what clients are allowed to send and receive.

Create:

```text
web/FundingRequestDtos.java
```

The create request should accept only:

```text
organizationName
country
requestedAmount
organizationAgeYears
```

Do not let the caller set things such as:

```text
id
status
createdAt
```

Those belong to the application.

Validation annotations include:

```text
@NotBlank
@NotNull
@Size
@Min
@DecimalMin
@Digits
```

For example:

```java
@NotNull
@DecimalMin(value = "0.00", inclusive = false)
BigDecimal requestedAmount
```

means the amount must exist and be strictly greater than zero.

DTO = **Data Transfer Object**.

It separates your HTTP contract from your database entity.

---

# 19. Step 13 — Create `FundingRequestService`

Now create the use-case coordinator.

The service is responsible for operations such as:

```text
create funding request
evaluate funding request
retrieve request
list/filter requests
```

For evaluation, the workflow is:

```text
find request by ID
      ↓
find configured country limit
      ↓
call EligibilityEvaluator
      ↓
apply decision to entity
      ↓
save updated entity
```

The service should not contain HTTP routing logic.

The evaluator should not load from PostgreSQL.

Each class should have a focused responsibility.

`@Transactional` is used on operations that modify persisted state so the database operation behaves atomically.

---

# 20. Step 14 — Create Error Handling

Create:

```text
exception/FundingRequestException.java
exception/GlobalExceptionHandler.java
```

The service may need to represent errors such as:

```text
request not found
country limit missing
```

The global handler converts application errors into predictable API responses.

For example:

```json
{
  "timestamp": "...",
  "status": 404,
  "code": "FUNDING_REQUEST_NOT_FOUND",
  "message": "Funding request 99 was not found",
  "details": {}
}
```

`@RestControllerAdvice` allows one central class to handle REST exceptions instead of duplicating error handling in every controller method.

---

# 21. Step 15 — Create the Controller Last

Now create:

```text
web/FundingRequestController.java
```

The controller defines the HTTP endpoints.

The base URL is:

```text
/api/v1/funding-requests
```

Endpoints:

```text
POST /api/v1/funding-requests
    create

POST /api/v1/funding-requests/{id}/evaluate
    evaluate

GET /api/v1/funding-requests/{id}
    retrieve one

GET /api/v1/funding-requests
    retrieve all

GET /api/v1/funding-requests?status=APPROVED
    filter
```

A good controller should mostly:

```text
receive input
validate input
delegate to service
map output
return response
```

It should not contain large business rules.

---

# 22. Step 16 — Run the Real Application

Once the first end-to-end feature exists, run:

```powershell
mvn spring-boot:run
```

A successful startup should eventually show messages indicating that Tomcat started on port `8080` and the application started.

During startup:

```text
Spring reads application.yml
        ↓
loads .env
        ↓
connects to PostgreSQL
        ↓
Flyway checks migrations
        ↓
runs V1 if needed
        ↓
Hibernate/JPA initializes
        ↓
repositories are created
        ↓
Tomcat starts
```

The API becomes available at:

```text
http://localhost:8080
```

There is intentionally no homepage.

---

# 23. Step 17 — Test Manually With Postman

## Create

```http
POST http://localhost:8080/api/v1/funding-requests
```

```json
{
  "organizationName": "Green Growers Ltd",
  "country": "Kenya",
  "requestedAmount": 50000.00,
  "organizationAgeYears": 3
}
```

Expected initial status:

```text
PENDING
```

## Evaluate

If the returned ID is `1`:

```http
POST http://localhost:8080/api/v1/funding-requests/1/evaluate
```

Expected:

```text
APPROVED
```

because:

```text
age 3 >= 2
50000 <= Kenya limit 100000
```

## Retrieve

```http
GET http://localhost:8080/api/v1/funding-requests/1
```

## Filter

```http
GET http://localhost:8080/api/v1/funding-requests?status=APPROVED
```

---

# 24. Step 18 — Add Integration Tests

A unit test checks a small class such as the evaluator.

An integration test checks multiple real application components together.

`FundingRequestControllerIntegrationTest` uses:

```text
MockMvc
Controller
Validation
Service
Evaluator
Repository
H2 test database
Flyway
```

A useful integration scenario is:

```text
POST create
     ↓
verify PENDING
     ↓
POST evaluate
     ↓
verify APPROVED
     ↓
GET by ID
     ↓
GET filter by APPROVED
```

It also tests validation, unsupported countries and 404 responses.

---

# 25. Why H2 Is Used for Tests

The test profile uses:

```text
src/test/resources/application-test.yml
```

Instead of your real PostgreSQL database, tests use an in-memory H2 database in PostgreSQL compatibility mode.

Therefore:

```powershell
mvn clean verify
```

can create test records without changing your real `funding_db`.

Conceptually:

```text
normal run -> PostgreSQL funding_db

automated tests -> temporary H2 database
```

---

# 26. Complete Automatic Verification

Run:

```powershell
mvn clean verify
```

A successful build ends with:

```text
BUILD SUCCESS
```

This is the main command to remember for assessment verification.

You can also run only the evaluator tests:

```powershell
mvn -Dtest=EligibilityEvaluatorTest test
```

or only the integration tests:

```powershell
mvn -Dtest=FundingRequestControllerIntegrationTest test
```

Test reports are generated under:

```text
target/surefire-reports/
```

---

# 27. One Complete Request Flow

This is the most important diagram in the README.

Suppose Postman sends:

```http
POST /api/v1/funding-requests
```

with JSON.

The flow is:

```text
1. Tomcat receives HTTP request
           ↓
2. Spring routing finds FundingRequestController
           ↓
3. Jackson converts JSON into CreateFundingRequestRequest
           ↓
4. Bean Validation checks @NotBlank/@Min/etc.
           ↓
5. Controller calls FundingRequestService.create()
           ↓
6. Service creates FundingRequest domain/entity
           ↓
7. FundingRequest starts PENDING
           ↓
8. Repository.save() is called
           ↓
9. Spring Data JPA / Hibernate produces SQL
           ↓
10. PostgreSQL stores the row and assigns ID
           ↓
11. Entity is mapped to FundingRequestResponse
           ↓
12. Jackson converts Java response to JSON
           ↓
13. HTTP 201 response goes back to Postman
```

Evaluation is:

```text
POST /1/evaluate
       ↓
Controller
       ↓
Service
       ↓
FundingRequestRepository.findById(1)
       ↓
CountryLimitRepository.findById(country)
       ↓
EligibilityEvaluator.evaluate(request, limit)
       ↓
APPROVED / REJECTED + reasons
       ↓
FundingRequest.applyDecision()
       ↓
Repository saves update
       ↓
JSON response
```

If you understand these two flows, you understand most of the application.

---

# 28. Folder Responsibilities — Quick Reference

| Folder | Responsibility |
|---|---|
| `domain` | business concepts and persisted entities |
| `repository` | database access |
| `service` | business rules and use-case coordination |
| `web` | REST endpoints, JSON DTOs, validation boundary |
| `exception` | structured application/API errors |
| `resources` | runtime configuration and Flyway SQL |
| `src/test` | automated tests |
| `.github/workflows` | continuous integration |
| `target` | generated Maven build output |

---

# 29. Important Spring Annotations in This Project

## `@SpringBootApplication`

Starts/configures the Spring Boot application and component scanning.

## `@Entity`

Marks a Java class as a JPA persistence entity.

## `@Id`

Marks the entity primary key.

## `@Enumerated(EnumType.STRING)`

Stores enum values as strings such as `APPROVED` instead of numeric positions.

## `@Service`

Marks a class as an application/service component managed by Spring.

## `@Component`

Marks a general Spring-managed component.

## `@RestController`

Marks a class as a REST/HTTP controller.

## `@RequestMapping`

Defines a controller's base path.

## `@PostMapping` / `@GetMapping`

Maps HTTP operations to Java methods.

## `@RequestBody`

Converts incoming JSON into a Java object.

## `@PathVariable`

Reads a value such as `{id}` from the URL.

## `@RequestParam`

Reads query parameters such as:

```text
?status=APPROVED
```

## `@Valid`

Tells Spring to run Bean Validation before entering the controller method.

## `@Transactional`

Creates a database transaction boundary around an operation.

## `@RestControllerAdvice`

Centralizes REST exception handling.

## `@SpringBootTest`

Loads the Spring application context for integration testing.

## `@Test`

Marks a JUnit test method.

---

# 30. Configurable Country Limits

Country thresholds are mutable business policy.

They are stored in:

```text
country_limits
```

not in `EligibilityEvaluator`.

For example:

```sql
UPDATE country_limits
SET max_amount = 110000.00
WHERE country = 'KENYA';
```

Now the evaluator automatically uses `110000` the next time it loads the Kenya configuration.

No Java evaluation rule changes are required.

This is a major design requirement of the assessment.

---

# 31. Validation and Data Integrity

Validation exists in multiple places.

## HTTP boundary

Bean Validation checks:

```text
organization name required
country required/supported
amount > 0
age >= 0
```

## Domain logic

The entity prevents an evaluation from returning to `PENDING`.

## Database

SQL constraints protect critical persisted values even if data enters from somewhere other than the REST API.

Examples:

```text
requested_amount > 0
organization_age_years >= 0
status in PENDING/APPROVED/REJECTED
```

This gives layered protection rather than relying only on frontend/API validation.

---

# 32. Why `BigDecimal` for Money?

Do not use `double` for important financial values.

This project uses:

```text
Java: BigDecimal
PostgreSQL: NUMERIC(15,2)
```

Comparison therefore uses:

```java
requestedAmount.compareTo(maxAmount)
```

Interpretation:

```text
< 0  requested amount is smaller
= 0  values are equal
> 0  requested amount is larger
```

That is why exactly equal to the limit is approved while one cent above is rejected.

---

# 33. Local Setup for This Existing Repository

If you are using the existing code rather than generating it again:

```powershell
cd C:\dev
git clone https://github.com/hartleyzuberi/mock.git
cd mock
```

If already cloned:

```powershell
cd C:\dev\mock
git pull origin main
```

Verify tools:

```powershell
java -version
javac -version
mvn -version
psql --version
```

Create the database if needed:

```powershell
psql -U postgres -h 127.0.0.1
```

```sql
CREATE DATABASE funding_db;
```

Then copy environment configuration:

```powershell
Copy-Item .env.example .env
notepad .env
```

Set your real PostgreSQL password.

Run automated tests:

```powershell
mvn clean verify
```

Then start:

```powershell
mvn spring-boot:run
```

---

# 34. Git and CI Flow

A sensible development workflow is:

```text
make a small change
      ↓
mvn test / mvn clean verify
      ↓
git status
      ↓
git add ...
      ↓
git commit
      ↓
git push
      ↓
GitHub Actions runs mvn clean verify again
```

The workflow lives in:

```text
.github/workflows/ci.yml
```

This is **Continuous Integration (CI)**.

It gives another clean environment that verifies the project builds and tests successfully.

---

# 35. Common Problems

## Maven not recognized

Check:

```powershell
mvn -version
```

If Windows cannot find Maven, ensure:

```text
MAVEN_HOME=C:\tools\apache-maven-...
```

and:

```text
%MAVEN_HOME%\bin
```

is in `Path`.

Open a new terminal after changing environment variables.

## PostgreSQL password authentication failed

Example:

```text
FATAL: password authentication failed for user "postgres"
```

The application reached PostgreSQL, but the credentials were rejected.

First test directly:

```powershell
psql -U postgres -h 127.0.0.1
```

Then put the working password in your local `.env`.

## Database does not exist

Create only the database:

```sql
CREATE DATABASE funding_db;
```

Flyway creates the application tables.

## Port 8080 already used

Edit `.env`:

```properties
PORT=8081
```

then restart the application.

---

# 36. What Was Generated vs What Was Designed by Us?

This distinction is worth memorizing.

## Usually generated by Spring Initializr

```text
pom.xml foundation
src/main/java package structure
main @SpringBootApplication class
src/main/resources
src/test skeleton
Maven wrapper when generated from Initializr
.gitignore foundation
```

## Application-specific code we designed

```text
business entities
country configuration model
eligibility evaluator
repositories
service/use cases
REST endpoints
DTOs
validation rules
error handling
Flyway schema
seed country limits
unit tests
integration tests
CI workflow
README/design decisions
```

Spring does not know what a funding request means.

We teach the framework our domain through our code.

---

# 37. If Rebuilding This From Zero, Stop at These Checkpoints

A useful learning exercise is to rebuild without copying the completed code all at once.

## Checkpoint 1

Generate Spring project and make this succeed:

```powershell
mvn spring-boot:run
```

## Checkpoint 2

Create enums/entities and make this succeed:

```powershell
mvn compile
```

## Checkpoint 3

Create evaluator and make its tests succeed:

```powershell
mvn -Dtest=EligibilityEvaluatorTest test
```

## Checkpoint 4

Create migration/repositories/service and start successfully against PostgreSQL.

## Checkpoint 5

Create controller and manually create one `PENDING` request through Postman.

## Checkpoint 6

Evaluate that request and verify the database changes.

## Checkpoint 7

Create integration tests and make this succeed:

```powershell
mvn clean verify
```

If you can rebuild the application through those checkpoints, you are no longer merely reading Spring Boot code — you understand how to construct a small Spring backend yourself.

---

# 38. Interview Explanation — Short Version

If asked to explain the architecture, a strong concise answer is:

> The application is a Spring Boot REST service separated into web, service, domain and persistence concerns. The controller handles HTTP and validation, the service coordinates use cases, the evaluator contains pure eligibility rules, and Spring Data repositories handle persistence. Country limits are stored as configurable database data rather than hardcoded into the evaluator. Flyway manages reproducible schema creation and seed configuration. Core rules are unit-tested separately, while MockMvc integration tests verify the REST-to-database flow.

---

# 39. Assumptions and Tradeoffs

This is intentionally an **assessment-sized** application.

Current choices include:

- one implied monetary unit; no currency conversion
- current country limit is used at evaluation time
- re-evaluation is allowed because the brief does not forbid it
- rejection reasons use a compact persisted representation
- no authentication/authorization because identity is outside scope
- list endpoint is unpaginated because the assignment is small

Possible production extensions:

```text
pagination
decision history
effective-dated country policies
authentication/authorization
auditing
optimistic locking
PostgreSQL Testcontainers
external compliance integration
```

These are future possibilities, not reasons to over-engineer the three-hour assessment.

---

# 40. Optional Design Question — External Compliance API

If approval later requires an external compliance check, keep the external HTTP implementation outside the core business rules.

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
            External REST API
```

`FundingRequestService` depends on the abstract application-facing contract (`ComplianceGateway`), not directly on HTTP client code.

Benefits:

- easier testing
- lower coupling
- replaceable provider
- external failures handled at a clear boundary
- business rules remain independent from HTTP technology

---

# 41. Final Learning Checklist

Before saying you understand this project, make sure you can answer these without reading the code:

```text
What does @SpringBootApplication do?

What is the difference between Java and Spring Boot?

What is pom.xml for?

Why do we have domain/service/repository/web packages?

What is an @Entity?

What does JpaRepository give us?

What is dependency injection?

Why is EligibilityEvaluator separate from FundingRequestService?

Why are country limits in the database?

Why use BigDecimal for money?

What does @Transactional protect?

What is a DTO?

What does @Valid do?

What does Flyway do?

What is the difference between a unit test and integration test?

Why do tests use H2?

What happens when POST /funding-requests is called?

What happens when POST /{id}/evaluate is called?

What does mvn clean verify do?
```

If you can explain those clearly and rebuild the project using the checkpoints above, you have a solid foundation for working with this Spring Boot application.