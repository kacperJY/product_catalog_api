# Product Catalog API

A small REST API for a product catalog, built as a focused exercise in Spring Cache. The project explores how an in-memory cache reduces repeated database reads, how product writes affect cached values, and what happens when several requests miss the cache at the same time.

## Features

- Create, list, read, update, and delete products through a REST API.
- Validate incoming product data and return structured errors.
- Manage the PostgreSQL schema with Flyway.
- Cache reads by product ID with Spring Cache and Caffeine.
- Update or evict the relevant cache entry after product changes.
- Test concurrent reads of the same uncached product.

## Tech stack

Java 25, Spring Boot 4.1, Spring Web MVC, Spring Data JPA, PostgreSQL 18, Flyway, Spring Cache, Caffeine, Maven, JUnit 5, Mockito, and Testcontainers.

## Run the application

### Docker Compose

Requires Docker with Compose. From the project root:

```bash
cp .env.example .env
docker compose --profile app up --build -d
```

The API is available at `http://localhost:8080`. The example credentials in `.env.example` are for local development; `.env` is ignored by Git.

To stop the containers while retaining the database volume:

```bash
docker compose --profile app down
```

### Run Spring Boot locally

Requires Java 25 and Docker. Start only PostgreSQL, then run the application with the Maven wrapper:

```bash
cp .env.example .env
docker compose --profile db-only up -d
./mvnw spring-boot:run
```

The default application settings connect to PostgreSQL at `localhost:5432` with the credentials from `.env.example`. You can override them with `DB_URL`, `DB_USER`, and `DB_PASSWORD` environment variables.

## API

| Method | Path | Result |
| --- | --- | --- |
| `POST` | `/api/products` | `201 Created`; response body contains the new product ID |
| `GET` | `/api/products/{id}` | `200 OK`; product JSON, or `404 Not Found` |
| `GET` | `/api/products` | `200 OK`; JSON array of products |
| `PUT` | `/api/products/{id}` | `200 OK` with an empty body, or `404 Not Found` |
| `DELETE` | `/api/products/{id}` | `204 No Content`, or `404 Not Found` |

Example request:

```bash
curl -i -X POST http://localhost:8080/api/products \
  -H 'Content-Type: application/json' \
  -d '{"name":"Coca-Cola","description":"2 L bottle","price":9.99,"stockQuantity":25}'
```

The request body has four fields: `name` is required and limited to 150 characters; `description` is optional; `price` must be positive with at most two decimal places; and `stockQuantity` must be zero or greater. Invalid input returns `400 Bad Request` with a `problems` map containing field errors. Missing product IDs return `404 Not Found`.

## Caching behavior

Only `GET /api/products/{id}` is cached. The product ID is the cache key, and Caffeine stores up to 50 entries in the application's memory. Repeated reads of one product can therefore avoid another repository lookup. `GET /api/products` remains uncached.

The service uses `@Cacheable(sync = true)` so concurrent requests for the same missing key share one value computation within an application instance. `PUT` uses `@CachePut` to replace that product's cached value, and `DELETE` uses `@CacheEvict` to remove it. `POST` leaves the new product uncached until its first read.

This is a local cache: separate application instances do not share entries. Coordinating cache changes across instances and cache behavior around failed transactions are outside this project's current scope.

## Tests

Run the full suite with Docker available for Testcontainers:

```bash
./mvnw verify
```

The suite includes service tests, API and cache integration tests against PostgreSQL, and a concurrency test. The concurrency test holds the first repository read while eight calls request the same product, then checks that the repository is called once. It also checks later reads against the populated cache.

Flyway creates the schema from `src/main/resources/migration/scripts/V1__init.sql`; Hibernate validates the schema rather than generating it.
