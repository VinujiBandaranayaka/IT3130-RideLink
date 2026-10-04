# RideLink

RideLink is a Java-based microservices backend for a ride-sharing platform developed as part of the IT3130 Application Development group project. The system is designed around clear service boundaries, with each microservice responsible for a distinct domain such as identity, driver management, ride orchestration, or fare and payment processing.

The project demonstrates modern backend service design using Spring Boot, MongoDB, JWT-based authentication, REST APIs, Swagger/OpenAPI documentation, and inter-service communication in a realistic distributed architecture.

## Overview

RideLink simulates a complete ride-hailing flow, covering:

- User and driver account management
- Authentication and role-based authorization
- Driver and vehicle registration
- Availability and location tracking
- Ride creation, assignment, lifecycle management, and cancellation
- Fare estimation and final fare calculation
- Simulated payment processing with receipts
- Service-to-service API integration
- API testing with Postman and Swagger

## System Architecture

RideLink follows a microservices architecture, where each service runs independently and manages its own data domain.

| Service | Responsibility | Port | Persistence |
|---|---|---:|---|
| [account-service](./account-service) | Account creation, login, JWT issuance, roles, profile and status management | 8081 | MongoDB |
| [driver-vehicle-service](./driver-vehicle-service) | Driver profiles, vehicles, availability, location, service area logic | 8082 | MongoDB |
| [ride-management-service](./ride-management-service) | Ride requests, assignment, lifecycle tracking, authorization checks | 8083 | MongoDB |
| [fare-payment-service](./fare-payment-service) | Fare estimation, final fare calculation, payments, receipts | 8084 | MongoDB |

### Typical request flow

1. A user registers or logs in through the Account Service.
2. Drivers are created and maintained in the Driver & Vehicle Service.
3. Passengers create rides through the Ride Management Service.
4. The ride service validates the driver, updates lifecycle status, and enforces authorization rules.
5. Fare calculations are completed in the Fare & Payment Service.
6. Payment processing and receipt generation complete the transaction flow.

## Tech Stack

- Java 17
- Spring Boot
- Spring Web / REST APIs
- Spring Security
- JWT authentication
- MongoDB
- Maven
- OpenAPI / Swagger UI
- Postman for API testing

## Repository Structure

```text
IT3130-RideLink/
├── account-service/
│   ├── src/
│   ├── pom.xml
│   └── mvnw
├── driver-vehicle-service/
│   ├── src/
│   ├── pom.xml
│   └── mvnw
├── ride-management-service/
│   ├── src/
│   ├── pom.xml
│   └── mvnw
├── fare-payment-service/
│   ├── src/
│   ├── pom.xml
│   └── mvnw
├── postman/
│   ├── RideLink-Complete.postman_collection.json
│   ├── RideLink-Local.postman_environment.json
│   └── RideLink-Ride-Management.postman_collection.json
├── login-test.json
├── .gitignore
├── README.md
└── .github/
```

## Service Details

### 1) Account Service

The Account Service manages all identity and access concerns.

Features:

- User registration and login
- JWT token generation
- Role-based access restrictions
- Account updates and status control
- Account retrieval by ID

Key endpoint group:

```http
POST /api/accounts
POST /api/accounts/login
GET  /api/accounts/{id}
PUT  /api/accounts/{id}
PATCH /api/accounts/{id}/status
PATCH /api/accounts/{id}/role
```

### 2) Driver & Vehicle Service

The Driver & Vehicle Service handles everything related to drivers and their vehicles.

Features:

- Driver profile creation and retrieval
- Vehicle-related operations
- Driver availability updates
- Current location tracking
- Driver lookup by service area
- Authorization checks for driver operations

Sample endpoint group:

```http
POST   /api/drivers
GET    /api/drivers
GET    /api/drivers/{id}
PUT    /api/drivers/{id}
DELETE /api/drivers/{id}
PATCH  /api/drivers/{id}/availability
PATCH  /api/drivers/{id}/location
GET    /api/drivers/available
GET    /api/drivers/available/{serviceArea}
```

### 3) Ride Management Service

The Ride Management Service is responsible for the ride lifecycle and operational flow.

Features:

- Ride creation and retrieval
- Driver assignment workflow
- Ride state validation
- Status transitions such as REQUESTED, ASSIGNED, ACCEPTED, IN_PROGRESS, COMPLETED, and CANCELLED
- Driver availability enforcement
- Role-based authorization for ride actions

Common ride lifecycle:

```text
REQUESTED -> ASSIGNED -> ACCEPTED -> IN_PROGRESS -> COMPLETED
```

A ride can also be cancelled at valid points in the workflow. Invalid transitions are rejected to maintain consistency.

Example endpoints:

```http
POST /api/rides
GET  /api/rides
GET  /api/rides/{id}
PUT  /api/rides/{id}/assign?driverId={driverId}
PUT  /api/rides/{id}/accept
PUT  /api/rides/{id}/start
PUT  /api/rides/{id}/complete?distanceKm={value}&durationMin={value}
PUT  /api/rides/{id}/cancel
```

### 4) Fare & Payment Service

The Fare & Payment Service calculates trip charges and handles simulated payment logic.

Features:

- Fare estimation for ride data
- Final fare calculation with trip details and surge logic
- Payment simulation and status tracking
- Receipt generation
- Response and exception handling for invalid or failed payments

Example endpoints:

```http
POST /api/fares/estimate
POST /api/fares/final
GET  /api/fares/ride/{rideId}
POST /api/payments
GET  /api/payments/{id}
GET  /api/payments/{id}/receipt
```

## Security Model

The services are secured with JWT-based authentication and role-aware authorization. Sensitive endpoints validate the caller's identity and permissions before executing the action. This includes restrictions for user-owned updates and admin-only operations.

Common security behaviors in the project:

- Missing JWT token => unauthorized request
- Invalid role or insufficient permission => forbidden request
- Protected operations are validated before business logic execution

## Prerequisites

Before running the project locally, ensure the following are available:

- Java 17+
- Maven
- MongoDB instance running locally or remotely
- Valid environment variables for JWT and service URLs

## Environment Variables

Set the required variables before starting any service.

| Variable | Used By | Purpose |
|---|---|---|
| `JWT_SECRET` | All services | Shared secret for JWT validation |
| `MONGODB_URI` | Ride/fare services | MongoDB connection string |
| `ACCOUNT_SERVICE_URL` | Driver service | Base URL for the Account Service |
| `DRIVER_SERVICE_URL` | Ride service | Base URL for the Driver & Vehicle Service |
| `RIDE_SERVICE_URL` | Fare service | Base URL for the Ride Management Service |

Example PowerShell configuration:

```powershell
$env:JWT_SECRET = "your-secret-key"
$env:MONGODB_URI = "mongodb://localhost:27017/ridelink_ride_db"
$env:ACCOUNT_SERVICE_URL = "http://localhost:8081"
$env:DRIVER_SERVICE_URL = "http://localhost:8082"
$env:RIDE_SERVICE_URL = "http://localhost:8083"
```

## Running the Project

Open a terminal for each service and start it separately.

### Account Service

```bash
cd account-service
./mvnw spring-boot:run
```

### Driver & Vehicle Service

```bash
cd driver-vehicle-service
./mvnw spring-boot:run
```

### Ride Management Service

```bash
cd ride-management-service
./mvnw spring-boot:run
```

### Fare & Payment Service

```bash
cd fare-payment-service
./mvnw spring-boot:run
```

### Default service URLs

- Account Service: http://localhost:8081
- Driver & Vehicle Service: http://localhost:8082
- Ride Management Service: http://localhost:8083
- Fare & Payment Service: http://localhost:8084

## Swagger / OpenAPI Documentation

When each service is running, Swagger UI is available at:

```text
http://localhost:<service-port>/swagger-ui.html
```

Examples:

- http://localhost:8083/swagger-ui.html
- http://localhost:8084/swagger-ui.html

## API Testing

The repository includes Postman collections for testing the services:

- [postman/RideLink-Complete.postman_collection.json](./postman/RideLink-Complete.postman_collection.json)
- [postman/RideLink-Ride-Management.postman_collection.json](./postman/RideLink-Ride-Management.postman_collection.json)
- [postman/RideLink-Local.postman_environment.json](./postman/RideLink-Local.postman_environment.json)

These collections support local testing of login flows, rides, and service interactions.

## Testing

Run tests in any service with:

```bash
./mvnw test
```

The project includes automated tests covering:

- account creation and login
- security and JWT validation
- driver and vehicle management logic
- ride lifecycle state transitions
- role-based authorization
- application context startup
- fare calculation and payment processing

## Notes

- This project is developed as a multi-service backend for a ride-sharing application.
- Each service is independently executable and owns a distinct business responsibility.
- MongoDB is used as the persistence layer for all services.
- The codebase is structured to be a strong academic example of microservice architecture, service integration, and secure API design.

## Contributor Context

This repository was developed as a group assignment for IT3130 Application Development. It demonstrates a realistic backend setup for managing a ride-sharing platform across separate service domains.

## License

This project is intended for academic and learning use within the coursework environment.
