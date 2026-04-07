
# KuluBotti 🤖
### AI-Powered Receipt Finance Tracker Microservices

KuluBotti is a custom-built, event-driven microservices platform for automated expense management. Designed from scratch using **Java 21** and **Spring Boot**, this personal project demonstrates a deep understanding of scalable backend patterns, dynamic service discovery, and resilient asynchronous processing.

## Core Architecture

This platform utilizes a distributed microservices architecture, implementing the **Database-per-Service** pattern to ensure strict domain isolation.

* **Service Discovery (Netflix Eureka):** Acts as the dynamic address book for the cluster, allowing services to find each other without hardcoded IPs.
* **API Gateway (Spring Cloud Gateway):** The single entry point for the frontend, handling routing, CORS, and initial token validation.
* **Auth Service:** Manages user registration and JWT generation, backed by its own isolated PostgreSQL database.
* **Expense Service:** The core business engine. Handles CRUD operations for expenses and publishes events to the message broker.
* **AI Parser Service:** A decoupled Kafka consumer that asynchronously processes receipt images.

## Asynchronous AI Pipeline (Gemini & Cloudinary)

To ensure the frontend remains highly responsive, the platform utilizes an event-driven "Fire-and-Forget" pattern:

1. **Secure Image Hosting:** When a user uploads a receipt via the React frontend, the image is securely uploaded to **Cloudinary**, returning a highly optimized, CDN-delivered URL.
2. **Non-Blocking Submission:** The Expense Service validates the payload, initializes the database record as `PENDING`, and immediately returns an **HTTP 202 Accepted** response so the UI never freezes.
3. **Event Orchestration (Kafka):** An `ExpenseCreatedEvent` (containing the image URL and ID) is published to a dedicated Apache Kafka topic.
4. **Generative AI Extraction:** The decoupled AI Parser Service consumes the event and prompts the **Google Gemini LLM** to perform multimodal OCR and structural JSON extraction on the receipt image.
5. **State Synchronization:** The AI Service broadcasts the structured results back to Kafka. The Expense Service consumes this result, maps the JSONB payload directly into PostgreSQL, and transitions the record to a `PROCESSED` state for the frontend dashboard to display.

## Development vs. Production Environments

A major architectural focus of this project was ensuring a clean separation between development and production environments using **Spring Profiles** (`dev`, `prod`) and CI/CD environment variable injection.

* **Local Development (`dev`):** Utilizes `docker-compose` to spin up local instances of PostgreSQL, Redis, and Zookeeper/Kafka. Services communicate via internal Docker network hostnames, and the React frontend uses a local Vite proxy to bypass CORS restrictions.
* **Cloud Production (`prod`):** Deployed via a continuous deployment (CI/CD) pipeline linked to GitHub. The production environment replaces local containers with managed cloud infrastructure: **Neon Serverless PostgreSQL** for databases and **Aiven** for the Kafka cluster. Environment variables strictly overwrite local settings (e.g., injecting secure `JDBC` URLs, dynamically assigning IP addresses to Eureka to bypass cloud DNS issues, and enforcing strict CORS headers at the API Gateway).
* **Network Throttling:** Production services are configured with extended Eureka heartbeat intervals (90-120 seconds) to comply with cloud provider rate limits and prevent DDoS-protection blocks.

## Security & Data Isolation

* **Defense-in-Depth:** Payloads are filtered through immutable **Java Records (DTOs)** to prevent mass assignment attacks.
* **Stateless Authentication:** Implements stateless JWT (JSON Web Tokens) validated at the API Gateway level before requests are routed to internal microservices.
* **Secret Management:** Sensitive credentials (LLM API keys, Database passwords, Cloudinary secrets) are never committed to version control; they are injected securely via `.env` files locally or CI/CD environment variables in production.

## Project Structure

```text
KuluBotti_Project/
├── docker-compose.yml       # Local Dev Infrastructure (Postgres, Kafka, Redis)
├── .env                     # Secure environment variables (Git-ignored)
├── kulubotti-discovery/     # Port 8761: Service Discovery registry
├── kulubotti-gateway/       # Port 8080: The front door, CORS, and router
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