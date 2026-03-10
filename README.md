# Task Manager

A task management application built with Spring Boot (backend) and React (frontend).

## Architecture

```
Browser  →  React SPA (port 3100)  →  Backend API (Spring Boot, port 4000)  →  PostgreSQL
```

## Prerequisites

- Docker and Docker Compose

For local development and testing:
- Java 21, Maven 3.x
- Node.js 20+

## Quick Start

Start all services (database, backend, frontend) with:

```bash
make up
```

Or on Windows (where `make` is not available):

```bash
docker compose up --build -d
```

The application will be available at http://localhost:3100.
API documentation is at http://localhost:4000/swagger-ui.html.

## Commands

| Task                    | macOS/Linux             | Windows (no `make`)                                   |
|-------------------------|-------------------------|-------------------------------------------------------|
| Build and start         | `make up`               | `docker compose up --build -d`                        |
| Stop all services       | `make down`             | `docker compose down`                                 |
| Rebuild and restart     | `make restart`          | `docker compose down && docker compose up --build -d` |
| Follow logs             | `make logs`             | `docker compose logs -f`                              |
| Run all tests           | `make test`             | `cd backend && mvn test && cd ../frontend && npx vitest run` |
| Run backend tests only  | `make test-backend`     | `cd backend && mvn test`                              |
| Run frontend tests only | `make test-frontend`    | `cd frontend && npx vitest run`                       |
| Stop and remove data    | `make clean`            | `docker compose down -v`                              |

## API

### Endpoints

| Method | Endpoint             | Description            |
|--------|----------------------|------------------------|
| POST   | `/tasks`             | Create a new task      |
| GET    | `/tasks`             | Retrieve all tasks     |
| GET    | `/tasks/{id}`        | Retrieve a task by ID  |
| PATCH  | `/tasks/{id}/status` | Update a task's status |
| DELETE | `/tasks/{id}`        | Delete a task          |

Full request/response schemas are available via Swagger UI at `/swagger-ui.html` when the backend is running.

### Task Model

| Field       | Type     | Required | Max Length | Description                        |
|-------------|----------|----------|------------|------------------------------------|
| title       | string   | Yes      | 255        | Task title                         |
| description | string   | No       | 1000       | Task description                   |
| status      | enum     | Yes      | —          | `TODO`, `IN_PROGRESS`, `COMPLETED` |
| dueDateTime | datetime | Yes      | —          | Due date and time (ISO 8601)       |

### Example: Create a task

```bash
curl -X POST http://localhost:4000/tasks \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Review documents",
    "description": "Review all submitted documents for task #123",
    "status": "TODO",
    "dueDateTime": "2026-03-10T14:00:00"
  }'
```

## Tech Stack

### Backend
- Java 21, Spring Boot 3.4
- Spring Data JPA with PostgreSQL
- Bean Validation for request validation
- SpringDoc OpenAPI for API documentation
- JUnit 5 and Mockito for testing

### Frontend
- React 19 with Vite
- Vitest and React Testing Library for testing

### Infrastructure
- Docker Compose for orchestration
- PostgreSQL 16

## Database

PostgreSQL is managed via Docker Compose. Data is persisted in a named volume (`pgdata`). Run `make clean` (or `docker compose down -v`) to stop services and remove the data volume.
