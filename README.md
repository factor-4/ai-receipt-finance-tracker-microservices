# AI Receipt Finance Tracker Microservices


# KuluBotti 🤖🧾

KuluBotti is a custom-built, event-driven microservices platform for automated expense management. 

Designed entirely from scratch using modern **Java 21** and **Spring Boot 4**, this project moves beyond standard monolithic CRUD applications to demonstrate a deep understanding of scalable backend patterns, dynamic service discovery, and asynchronous AI data processing.

## Core Architecture

This platform is built on a distributed microservices architecture, utilizing the following core components:

* **API Gateway (Spring Cloud Gateway MVC):** Acts as the single entry point for all client requests, leveraging Java 21 Virtual Threads for high-concurrency, non-blocking routing.
* **Service Registry (Netflix Eureka):** Provides dynamic service discovery, allowing microservices to register themselves on boot without hardcoded IP addresses.
* **Auth Service:** Manages user registration and authentication. Secures the perimeter using BCrypt password hashing and stateless JSON Web Tokens (JWT).
* **Expense Service :** The core business engine responsible for managing receipt data. It implements asynchronous status updates, Jakarta Bean Validation for data integrity, and a Global Exception Handling layer for secure, standardized API responses.
* **AI Parser Service :** A decoupled worker service that simulates AI data extraction. It communicates with the Expense Service via Apache Kafka, allowing for non-blocking, high-latency processing of financial documents.

## Asynchronous AI Pipeline 
* To maintain high availability and prevent blocking the main request thread during expensive OCR operations, the platform utilizes an event-driven "Fire-and-Forget" pattern:

* Non-Blocking Submission: When a user uploads a receipt via the Expense Service, the system validates the payload, initializes the record as PENDING, and immediately returns an HTTP 202 Accepted response.

* Event Orchestration (Apache Kafka): An ExpenseCreatedEvent is published to a dedicated Kafka topic. The AI Parser Service—operating as a decoupled consumer—picks up the task to simulate data extraction.

* State Synchronization: Once processing is complete, the results are broadcast back through a response topic. The Expense Service consumes this result, updates the database, and transitions the record to a PROCESSED state.

## Security & Data Isolation

* **Defense-in-Depth:** Incoming payloads are strictly filtered through immutable Java Records (DTOs) to prevent mass assignment attacks. 
* **Database-per-Service Pattern:** To ensure strict domain isolation and minimize blast radius, the Auth Service and Expense Service operate on entirely separate PostgreSQL databases. 
* **Secret Management:** No sensitive credentials or API keys are committed to version control. All secrets are injected via `.env` files or environment variables.

## Project Structure

The repository is organized as a monorepo containing distinct, deployable services:

```text
KuluBotti_Project/
├── docker-compose.yml       # Infrastructure blueprint (Databases, Kafka)
├── .env                     # Secure environment variables (Ignored by Git)
├── .gitignore               # Strict exclusion rules for compiled/sensitive data
├── kulubotti-gateway/       # Port 8080: The front door and router
├── kulubotti-eureka/        # Port 8761: The dynamic service phonebook
├── kulubotti-auth/          # Port 8081: Security and JWT generation
└── kulubotti-expense/       # Port 8082: Core business logic and receipt handling
