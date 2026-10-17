# Java Full-Stack Microservices Lab

A personal learning project for Java, Spring Boot, React, TypeScript, and microservices. Development is incremental; this first milestone delivers the Product Service only.

## Planned architecture

```text
java-fullstack-microservices-lab/
├── product-service/          # Implemented: REST API and its own H2 database
├── order-service/            # Planned: separate service and H2 database
├── frontend/                 # Planned: React + TypeScript + Vite
├── settings.gradle.kts       # Gradle multi-project configuration
├── build.gradle.kts
├── gradle.properties
└── README.md
```

The Product and Order services will own separate data stores and communicate through REST APIs. The frontend will call their APIs. The Order Service and frontend are not part of this milestone. Controllers handle HTTP, services own business rules, repositories handle persistence, and DTOs keep API requests separate from entities.

## Technology

- Java 21 and Spring Boot 3.5
- Gradle Kotlin DSL
- Spring Web, Spring Data JPA, Jakarta Bean Validation
- H2 file database for local Product Service development
- JUnit 5, Mockito, and Spring MVC test support

No external SQL database or Docker is required.

## Prerequisites

- JDK 21 or newer (source and API compatibility are restricted to Java 21)
- Internet access on the first Gradle Wrapper run to download Gradle and dependencies

The project uses the Gradle Wrapper, so a separate Gradle installation is not required.

## Run and test

From the repository root:

```powershell
.\gradlew.bat :product-service:bootRun
```

The service listens on `http://localhost:8081`. When started with the Gradle task, its H2 database is stored under `product-service/data/` and survives service restarts. The local H2 console is available at `http://localhost:8081/h2-console` with JDBC URL `jdbc:h2:file:./data/productdb`, username `sa`, and an empty password.

Run the build and tests:

```powershell
.\gradlew.bat build
.\gradlew.bat :product-service:test
```

## Product API

Base path: `/api/products`

| Method | Path | Result |
|---|---|---|
| `GET` | `/api/products` | List products |
| `GET` | `/api/products/{id}` | Get a product |
| `POST` | `/api/products` | Create a product (`201 Created`) |
| `PUT` | `/api/products/{id}` | Replace a product |
| `DELETE` | `/api/products/{id}` | Delete a product (`204 No Content`) |

Product request fields: `name` (required, up to 120 characters), `sku` (required and unique, up to 50 characters), `description` (optional, up to 1000 characters), `price` (greater than zero, up to 10 integer and 2 fractional digits), and `quantity` (zero or greater). Validation, missing products, duplicate SKUs, and persistence conflicts use a consistent JSON error response.

Example:

```powershell
Invoke-RestMethod -Method Post -Uri http://localhost:8081/api/products `
  -ContentType 'application/json' `
  -Body '{"name":"Keyboard","sku":"KEY-01","description":"Mechanical keyboard","price":79.99,"quantity":12}'
```

## Next milestones

1. Implement the Order Service with its own H2 database and tests.
2. Implement the React + TypeScript frontend and connect it to the services.
3. Add product search, filtering, and pagination as follow-up Product Service exercises.
