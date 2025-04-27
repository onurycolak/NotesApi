# Notes API – Quarkus Bootcamp Project

A simple, production-style Notes API built with Java, Quarkus, and Maven.

---

## Features

- CRUD operations for notes (Create, Read, Update, Delete)
- Pagination, sorting, and filtering support
- Enum support for urgency (`LOW`, `MEDIUM`, `HIGH`)
- Input validation and error handling with JSON responses
- API key authentication for create, update, delete endpoints
- API documentation via Swagger/OpenAPI (`/q/swagger-ui`)
- Ready-to-run with in-memory H2 database (test/dev)

---

## Tech Stack

- Java 17
- [Quarkus](https://quarkus.io/) (JAX-RS, Hibernate ORM)
- In-memory H2 database for development and testing.
- Maven
- [OpenAPI/Swagger UI](https://quarkus.io/guides/openapi-swaggerui) (auto-generated docs)
- JUnit & RestAssured for integration tests

---

## Getting Started

### Prerequisites

- Java 17+
- Maven 3.8+

### Running with Docker
A Dockerfile is provided for running the application as a container.

### Build the application JAR:

```sh
./mvnw package
```

Build the Docker image:

```sh
docker build -f src/main/docker/Dockerfile.jvm -t notes-api-jvm .
```

Run the container:

```sh
docker run -i --rm -p 8080:8080 notes-api-jvm
```
The API will be available at **http://localhost:8080/notes**.

---

## API Documentation

Once the app is running, see the full API docs and interact with the endpoints at:  
[http://localhost:8080/q/swagger-ui](http://localhost:8080/q/swagger-ui)

---

## Running Tests

```sh
./mvnw test
```

---

## API Authentication

Most write operations (POST, PUT, DELETE) require an API key header:

    X-API-Key: <your-api-key>

You can find/set your API key in `src/main/resources/application.properties` under `app.api-key`.
The default is:

    app.api-key=appsecretkey

## Example API Usage

### Create a Note

```sh
curl -X POST "http://localhost:8080/notes" \
  -H "X-API-Key: appsecretkey" \
  -H "Content-Type: application/json" \
  -d '{"title": "First note", "content": "Hello world!", "urgency": "HIGH"}'
```

### List Notes

```sh
curl "http://localhost:8080/notes?page=1&size=5"
```

### Get Note By ID

```sh
curl "http://localhost:8080/notes/1"
```

### Update a Note

```sh
curl -X PUT "http://localhost:8080/notes/1" \
  -H "X-API-Key: appsecretkey" \
  -H "Content-Type: application/json" \
  -d '{"title": "Updated title", "content": "Updated content", "urgency": "MEDIUM"}'
```

### Delete a Note

```sh
curl -X DELETE "http://localhost:8080/notes/1" \
  -H "X-API-Key: appsecretkey"
```