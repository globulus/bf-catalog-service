# catalog-service

Product catalog for Shopfront: products, categories and prices with history.

Spring Boot 3.4 / Java 17 / Maven. Postgres + Flyway.

## Build & run

```bash
./mvnw verify                 # compile + tests (H2, no DB needed)
./mvnw spring-boot:run        # needs Postgres, see below
```

Config is in `src/main/resources/application.properties`. Override with env vars:

- `DB_URL` (default `jdbc:postgresql://localhost:5432/catalog`)
- `DB_USER` / `DB_PASSWORD` (default `catalog` / `catalog`)
- `PORT` (default `8081`, the Docker image uses `8080`)

Swagger UI: http://localhost:8081/swagger-ui/index.html

## API spec

```bash
./mvnw -Popenapi test
```

Writes `target/openapi/catalog.json` (boots the app on H2 in a test, no server needed).
The spec is OpenAPI 3.0 on purpose, see the comment in `application.properties`.

Clients (web, mobile) copy this file into their `specs/` folder. If you change a
response, tell the web and mobile folks.

## Prices

A product can have many prices over time. `POST /api/products/{id}/prices` adds one and
closes the currently open one. `GET /api/products/{id}` returns the price valid right
now as `price`, or `null` if there isn't one. orders-service relies on that field.

## Docker

```bash
docker build -t catalog-service .
```
