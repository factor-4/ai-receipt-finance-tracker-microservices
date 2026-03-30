# KuluBotti 🤖🧾
### AI-Powered Receipt Finance Tracker Microservices

KuluBotti is a custom-built, event-driven microservices platform for automated expense management. Designed from scratch using **Java 21** and **Spring Boot**, this project demonstrates a deep understanding of scalable backend patterns, dynamic service discovery, and resilient asynchronous processing.

## ## Core Architecture

This platform is built on a distributed microservices architecture utilizing the following core components:

* **API Gateway (Spring Cloud Gateway):** Single entry point for all client requests, leveraging Java 21 Virtual Threads for high-concurrency routing.
* **Service Registry (Netflix Eureka):** Provides dynamic service discovery, allowing microservices to register themselves without hardcoded IP addresses.
* **Auth Service:** Manages user identity using BCrypt hashing and stateless **JWT (JSON Web Tokens)** for secure, perimeter-based authentication.
* **Expense Service:** The core business engine managing receipt data, implementing Jakarta Bean Validation and a Global Exception Handling layer.
* **AI Parser Service:** A decoupled worker service that processes receipt data. It communicates via **Apache Kafka** to handle high-latency OCR/Extraction tasks without blocking the main user flow.

## ## Reliability & DevOps Standards

To achieve professional-grade stability and "full standardization," the following patterns are implemented:

* **Multi-Profile Environment Strategy:** Utilizes **Spring Profiles** (`dev`, `prod`) to decouple infrastructure from code. This ensures the app is "Environment Aware"—automatically switching between `localhost` for local IDE development and internal Docker networking for **CI/CD pipelines**.
* **API Resilience (Resilience4j):** Inter-service communication and Kafka producers are protected by **Circuit Breakers** and **Retry logic**. If a downstream service or the message broker fails, the system fails gracefully rather than crashing the user session.
* **Automated CI/CD Pipeline (GitLab CI):** A standardized pipeline automates the **Build, Test, and Package** stages. This ensures every commit is validated and compiled into a containerized Docker image.
* **Distributed Caching (Spring Data Redis):** To reduce database load and improve Dashboard latency, expensive "Get Expenses" queries are cached in a distributed Redis store.
* **Database Parity (PostgreSQL):** Uses PostgreSQL across all environments to catch database-specific bugs early, utilizing different port mappings and DDL policies for Dev vs. Prod.

## ## Asynchronous AI Pipeline

The platform utilizes an event-driven "Fire-and-Forget" pattern to maintain high availability:

1. **Non-Blocking Submission:** When a user uploads a receipt, the system validates the payload, initializes the record as `PENDING`, and immediately returns an **HTTP 202 Accepted** response.
2. **Event Orchestration (Kafka):** An `ExpenseCreatedEvent` is published to a dedicated Kafka topic.
3. **State Synchronization:** The AI Parser Service consumes the event, simulates/executes data extraction, and broadcasts results back. The Expense Service consumes the result and transitions the record to a `PROCESSED` state.

## ## Security & Data Isolation

* **Defense-in-Depth:** Payloads are filtered through immutable **Java Records (DTOs)** to prevent mass assignment attacks.
* **Database-per-Service Pattern:** Auth and Expense services operate on entirely separate PostgreSQL databases to ensure strict domain isolation.
* **Secret Management:** Sensitive credentials are never committed to version control; they are injected via `.env` files or CI/CD environment variables.

## ## Project Structure

```text
KuluBotti_Project/
├── .gitlab-ci.yml           # Automated CI/CD Pipeline blueprint
├── docker-compose.yml       # Infrastructure (Postgres, Kafka, Redis)
├── .env                     # Secure environment variables (Git-ignored)
├── kulubotti-gateway/       # Port 8080: The front door and router
├── kulubotti-eureka/        # Port 8761: Service Discovery registry
├── kulubotti-auth/          # Port 8081: Security & JWT Service
├── kulubotti-expense/       # Port 8082: Business Logic & Persistence
├── kulubotti-ai-parser/     # Kafka Consumer: AI Processing Logic
└── kulubotti-frontend/      # React + Tailwind Dashboard