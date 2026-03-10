.PHONY: up down restart logs test test-backend test-frontend clean

up:
	docker compose up --build -d

down:
	docker compose down

restart:
	docker compose down
	docker compose up --build -d

logs:
	docker compose logs -f

test: test-backend test-frontend

test-backend:
	cd backend && mvn test

test-frontend:
	cd frontend && npx vitest run

clean:
	docker compose down -v
