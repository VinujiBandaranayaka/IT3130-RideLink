# IT3130-RideLink
IT3130 Application Development - RideLink Group Assignment

# RideLink – Backend Microservices

RideLink is a backend microservices-based ride-sharing platform developed for the IT3130 – Application Development group assignment.

The system is implemented using Java and Spring Boot. The application is divided into four independently executable microservices, with each service responsible for a specific business capability and its own data store.

---

## 1. Project Overview

RideLink provides backend services for a simulated ride-sharing platform.

The system supports:

- Passenger and driver account management
- Driver and vehicle management
- Ride request and ride lifecycle management
- Fare calculation and simulated payment processing
- Authentication and role-based access control
- Inter-service communication through REST APIs
- API testing using Postman
- API documentation using Swagger/OpenAPI

---

## 2. System Architecture

RideLink follows a microservices architecture.

Each microservice:

- Runs independently
- Has its own business responsibility
- Maintains its own persistence boundary
- Exposes REST APIs
- Communicates with other services through APIs where required

### Microservices

| Service | Responsibility | Port | Database |
|---|---|---:|---|
| Account Service | Account registration, login, roles, profiles and account status | 8081 | account_db |
| Driver & Vehicle Service | Driver profiles, vehicles, availability, service area and location | 8082 | driver_vehicle_db |
| Ride Management Service | Ride requests, driver assignment and ride lifecycle | 8083 | ride_management_db |
| Fare & Payment Service | Fare estimation, final fare and simulated payment | 8084 | fare_payment_db |

---

## 3. Microservices

### 3.1 Account Service

The Account Service manages passenger and driver accounts.

Main responsibilities:

- Passenger registration
- Driver registration
- Login and token issuance
- Role management
- Profile management
- Account status management

Current implemented endpoints:

```text
POST /api/accounts
GET  /api/accounts/{id}
