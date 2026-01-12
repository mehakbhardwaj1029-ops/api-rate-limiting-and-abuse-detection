---
# Adaptive API Rate Limiter & Abuse Detection Service

## Overview

This project protects APIs from abuse by combining **distributed rate limiting, adaptive traffic analysis, and temporary client blocking**.

Unlike basic fixed-limit rate limiters, this system **observes client behavior over time**, detects suspicious request patterns (bursts, rapid retries), and dynamically restricts or blocks abusive clients — while allowing legitimate traffic to continue uninterrupted.

The service is designed as a **standalone backend component** that can sit in front of any API and enforce fair usage automatically.


## Important

I have divided this project in various modules and each module has its own documentation to provide a better clarity in implementation 

Here i have put down things on a very high level , for better understanding you can check out doc respective to every module in a structured way.

---

## Key Features

### 1. Fingerprint-Based Client Identification

- Generates a stable client fingerprint using request attributes (IP, headers, etc.)
- Avoids reliance on IP alone
- Enables accurate per-client tracking

### 2. Redis-Backed Token Bucket Rate Limiting

- Implements the **Token Bucket algorithm**
- Enforced using **Redis + Lua scripts** for atomicity
- Safe under high concurrency and distributed deployments
- Supports configurable burst capacity and refill rate

### 3. Adaptive Abuse Detection

- Detects abnormal traffic patterns such as:
  - Sudden request bursts
  - Very fast repeated requests
- Assigns a **risk score** based on behavioral heuristics
- Escalates restrictions dynamically for suspicious clients

### 4. Temporary Client Blocking

- Tracks repeated rate-limit violations within a time window
- Automatically blocks abusive clients for a configurable duration
- Uses Redis TTLs for self-healing behavior (no manual unblocking)

### 5. Observability & Metrics (Planned / In Progress)

- Exposes internal metrics using **Spring Boot Actuator**
- Designed for integration with **Prometheus**
- Tracks:
  - Allowed vs blocked requests
  - Adaptive triggers
  - Blocked clients

---

## Architecture

Client Request
|
v
RateLimitFilter (Spring Filter)
|
+--> Fingerprint Generation
|
+--> Block Check (Redis)
|
+--> Token Bucket Check (Redis Lua)
|
+--> Adaptive Risk Evaluation
|
+--> Temporary Blocking (if needed)
|
v
Backend API

---

## Why This Project Matters

Most applications rely on:

- API gateways
- Fixed request limits
- Simple IP-based throttling

This project demonstrates:

- Deep understanding of **backend traffic control**
- Safe concurrency using **Redis Lua scripting**
- Real-world **abuse detection strategies**
- Observability-first design

It reflects the kind of **infrastructure-level thinking** used in large-scale systems.

---

## Example Use Cases

- Protecting public APIs from scrapers and bots
- Preventing credential-stuffing attacks
- Throttling abusive clients without impacting legitimate users
- Acting as a custom API gateway component

---

## Technology Stack

- **Java 21**
- **Spring Boot**
- **Redis**
- **Redis Lua Scripts**
- **Micrometer + Spring Boot Actuator**
- **Prometheus** (metrics scraping)
- **Docker** (optional deployment)

## How to run project

Prerequisites

Make sure the following are installed on your system:

Java 21

Maven 3.9+

Redis (local or Docker)

Git

1. Clone the Repository:

git clone https://github.com/<your-username>/intelligent-api-rate-limiter.git
cd intelligent-api-rate-limiter

2. Start redis -

You can either use:

1. docker to run redis cli locally
2. Install it on your machine

3. Configure port redis in application.yml

spring:
redis:
host: localhost
port: 6379

4. Build and Run Application

mvn clean install

mvn spring-boot:run

The application will start on the port 8080 by default or on the configured port in yml file.

http://localhost:8080

5. Testing

You can use Apache JMeter for testing on heavy inputs or manually test on this endpoint - curl http://localhost:8080/api/test

6. Verification

You can verify the results in redis-cli

ex- KEYS \*
