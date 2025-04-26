# Notes API – Quarkus Bootcamp Project

A simple, production-style Notes API built with Java, Quarkus, and Maven.

---

## Features

- CRUD operations for notes (Create, Read, Update, Delete)
- Pagination, sorting, and filtering support
- Enum support for urgency (`LOW`, `MEDIUM`, `HIGH`)
- Input validation and error handling with JSON responses
- API documentation via Swagger/OpenAPI (`/q/swagger-ui`)
- Ready-to-run with in-memory H2 database (test/dev)

---

## Tech Stack

- Java 17
- [Quarkus](https://quarkus.io/) (JAX-RS, Hibernate ORM)
- H2 Database (in-memory, dev/test)
- Maven
- [OpenAPI/Swagger UI](https://quarkus.io/guides/openapi-swaggerui) (auto-generated docs)
- JUnit & RestAssured for integration tests

---

## Getting Started

### Prerequisites

- Java 17+
- Maven 3.8+

### Running Locally

```sh
git clone https://github.com/your-username/notes-api.git
cd notes-api
./mvnw quarkus:dev
```
The API will be available at [http://localhost:8081/notes](http://localhost:8081/notes).

---

## API Documentation

Once the app is running, see the full API docs and interact with the endpoints at:  
[http://localhost:8081/q/swagger-ui](http://localhost:8081/q/swagger-ui)

---

## Running Tests

```sh
./mvnw test
```

---

## Example API Usage

### Create a Note

```sh
curl -X POST "http://localhost:8081/notes" \
  -H "Content-Type: application/json" \
  -d '{"title": "First note", "content": "Hello world!", "urgency": "HIGH"}'
```

### List Notes

```sh
curl "http://localhost:8081/notes?page=1&size=5"
```

### Get Note By ID

```sh
curl "http://localhost:8081/notes/1"
```

### Update a Note

```sh
curl -X PUT "http://localhost:8081/notes/1" \
  -H "Content-Type: application/json" \
  -d '{"title": "Updated title", "content": "Updated content", "urgency": "MEDIUM"}'
```

### Delete a Note

```sh
curl -X DELETE "http://localhost:8081/notes/1"
```

---

## Project Structure

```
notes-api/
  ├── src/
  │   ├── main/
  │   │   ├── java/com/onur/bootcamp/      # Main Java code (resources, services, models)
  │   │   └── resources/application.properties  # App configuration (ports, db, etc.)
  │   └── test/java/com/onur/bootcamp/     # Integration tests
  ├── .gitignore
  ├── mvnw / mvnw.cmd
  ├── pom.xml
  └── README.md
```
