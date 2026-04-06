# KuluBotti 🤖🧾
### AI-Powered Receipt Finance Tracker Microservices

KuluBotti is a custom-built, event-driven microservices platform for automated expense management. Designed from scratch using **Java 21** and **Spring Boot**, this project demonstrates a deep understanding of scalable backend patterns, dynamic service discovery, and resilient asynchronous processing.

## ## Core Architecture

## ## Asynchronous AI Pipeline (Gemini & Cloudinary)

This platform is built on a distributed microservices architecture utilizing the following core components:

**Secure Image Hosting:** When a user uploads a receipt via the React frontend, the image is securely uploaded to **Cloudinary**, returning a highly optimized, CDN-delivered URL.
2. **Non-Blocking Submission:** The Expense Service validates the payload, initializes the database record as `PENDING`, and immediately returns an **HTTP 202 Accepted** response so the user interface never freezes.
3. **Event Orchestration (Kafka):** An `ExpenseCreatedEvent` (containing the image URL and ID) is published to a dedicated Apache Kafka topic.
4. **Generative AI Extraction:** The decoupled AI Parser Service consumes the event and prompts the **Google Gemini LLM** to perform multimodal OCR and structural JSON extraction on the receipt image.
5. **State Synchronization:** The AI Service broadcasts the structured results back to Kafka. The Expense Service consumes this result, maps the JSONB payload directly into PostgreSQL, and transitions the record to a `PROCESSED` state for the frontend dashboard to display.

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



##  Quick Start (Local Development)

To run this microservices architecture locally, you will need **Docker**, **Java 21**, and **Node.js** installed.

### 1. Spin up the Infrastructure
The core dependencies (PostgreSQL databases, Kafka, Zookeeper) are containerized to keep your local machine clean.

```bash
# Start all infrastructure in the background
docker-compose up -d

# Verify all containers are running successfully
docker ps
```

### 2. Boot the Microservices
Because this architecture relies on dynamic Service Discovery, the services must be booted in a specific order. Run these via your IDE or using `./mvnw spring-boot:run`:

1. **Eureka Server** (Port 8761) - *Wait for this to fully start first.*
2. **API Gateway** (Port 8080)
3. **Auth Service** (Port 8081)
4. **Expense Service** (Port 8082)
5. **AI Parser Service** (Kafka Consumer)

### 3. Launch the Frontend Dashboard
Once the backend Gateway is running, open a new terminal window in the `kulubotti-frontend` directory:

```bash
# Install dependencies
npm install

# Start the Vite development server (with Gateway proxy enabled)
npm run dev
```
The dashboard will now be accessible at `http://localhost:5173`.