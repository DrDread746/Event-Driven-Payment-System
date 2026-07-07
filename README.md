# Event-Driven Payment Processing System

A production-inspired event-driven payment processing system built using **Java**, **Spring Boot**, **RabbitMQ**, **Redis**, **PostgreSQL**, and **Docker**. The project demonstrates how modern payment systems achieve reliable asynchronous processing through messaging, idempotency, retries, audit logging, the Transactional Outbox Pattern, and observability using Prometheus and Grafana.

---

## Features

- Event-driven payment processing using RabbitMQ
- Support for multiple payment channels:
  - UPI
  - Card
  - Bank Transfer
  - Wallet
- Transactional Outbox Pattern for reliable event publishing
- Redis-based idempotency to prevent duplicate payment processing
- Retry queues with delayed redelivery
- Dead Letter Queues (DLQs) for failed messages
- Append-only payment audit trail
- Payment Status API
- Prometheus metrics
- Grafana dashboards
- Docker Compose deployment
- k6 load testing and performance optimization

---

# Architecture

```
                 Client
                    │
                    ▼
          Spring Boot REST API
                    │
         ┌──────────┴──────────┐
         │                     │
         ▼                     ▼
  Payment Audit         Outbox Events
 (PostgreSQL)          (PostgreSQL)
         │
     Database Commit
         │
         ▼
    Outbox Relay Service
         │
         ▼
        RabbitMQ
         │
 ┌───────┼────────┬───────────┬──────────┐
 ▼       ▼        ▼           ▼
UPI    CARD   BANK TRANSFER  WALLET
Consumer Consumer Consumer   Consumer
         │
         ▼
 Redis Idempotency Check
         │
         ▼
 Payment Processing
         │
 ├──────────────┐
 │              │
 ▼              ▼
Success      Temporary Failure
 │              │
 ▼              ▼
Audit      Retry Queue
Update          │
                ▼
          Dead Letter Queue
```

---

# Technology Stack

| Category | Technology |
|----------|------------|
| Language | Java 21 |
| Framework | Spring Boot |
| Messaging | RabbitMQ |
| Database | PostgreSQL |
| Cache | Redis |
| ORM | Spring Data JPA / Hibernate |
| Monitoring | Prometheus + Grafana |
| Containerization | Docker + Docker Compose |
| Load Testing | k6 |

---

# Key Reliability Patterns

## Transactional Outbox Pattern

Payment events are first stored in the database inside the same transaction as the payment record. A dedicated Outbox Relay publishes pending events to RabbitMQ, ensuring messages are never lost even if the broker is temporarily unavailable.

---

## Idempotent Consumers

Redis is used to detect duplicate payment requests using the payment ID. If the same request is received multiple times, it is processed only once, preventing duplicate payments.

---

## Retry Mechanism

Transient failures are handled using RabbitMQ retry queues with delayed retries.

```
Payment
   ↓
Consumer
   ↓
Temporary Failure
   ↓
Retry Queue
   ↓
Consumer
   ↓
Success
```

---

## Dead Letter Queue (DLQ)

Messages that exceed the maximum retry count are automatically routed to a Dead Letter Queue for manual investigation instead of blocking the main processing flow.

---

## Payment Audit Trail

Every payment state transition is stored as an append-only record.

Example:

```
INITIATED
      ↓
PROCESSING
      ↓
PROCESSING (Retry)
      ↓
COMPLETED
```

This enables:

- Complete payment history
- Operational debugging
- Monitoring
- Compliance-style record keeping

---

# Payment Status API

Retrieve the latest payment status

```
GET /api/payments/{paymentId}/status
```

Retrieve complete payment history

```
GET /api/payments/{paymentId}/history
```

---

# Monitoring

Prometheus metrics include:

- Payments Processed
- Payment Retry Count
- Outbox Pending Events
- Outbox Relay Lag

Grafana dashboards visualize:

- Payment throughput
- Success vs Failed payments
- Retry activity
- Outbox health
- System performance

---

# Load Testing

The system was tested using **k6**.

Performance tuning included:

- Identifying slow database queries
- Using `EXPLAIN ANALYZE`
- Adding a missing database index
- Reducing **p95 latency by approximately 33%**

---

# Running the Project

## Clone the Repository

```bash
git clone https://github.com/DrDread746/Event-Driven-Payment-System.git
```

---

## Start the Application

```bash
docker compose up --build
```

---

## Services

| Service | Port |
|----------|------|
| Spring Boot | 8080 |
| RabbitMQ Management | 15672 |
| PostgreSQL | 5432 |
| Redis | 6379 |
| Prometheus | 9090 |
| Grafana | 3000 |

---

# Sample Payment Request

```json
{
  "paymentId": "a0d713df-119a-4def-8260-01839641a420",
  "senderId": "USER-001",
  "receiverId": "MERCHANT-123",
  "amount": 499.99,
  "currency": "INR",
  "channel": "UPI",
  "status": "INITIATED"
}
```

---

# Future Improvements

- Distributed tracing using OpenTelemetry
- Authentication & Authorization
- Fraud detection service
- Payment settlement workflow
- Event versioning
- Kubernetes deployment
- GitHub Actions CI/CD pipeline
