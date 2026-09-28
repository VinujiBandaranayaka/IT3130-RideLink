# IT3130-RideLink
IT3130 Application Development - RideLink Group Assignment

# RideLink ΓÇô Backend Microservices

RideLink is a backend microservices-based ride-sharing platform developed for the IT3130 ΓÇô Application Development group assignment.

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

---

# Member 3 - Ride Management Service

The Ride Management Service manages ride creation, assignment, lifecycle, cancellation, and retrieval.

## Service Information

- Port: 8083
- Java: 17
- Spring Boot: 4.1.1
- Database: MongoDB Atlas
- Database Name: ride_management_db
- Branch: feature/ride-management-service

## Ride Lifecycle

REQUESTED -> ASSIGNED -> ACCEPTED -> IN_PROGRESS -> COMPLETED

A ride can also move to CANCELLED. Invalid state transitions are rejected.

## API Endpoints

| Method | Endpoint | Role | Description |
|---|---|---|---|
| POST | /api/rides | PASSENGER | Create a new ride |
| GET | /api/rides | PASSENGER / DRIVER / ADMIN | Get all rides |
| GET | /api/rides/{id} | PASSENGER / DRIVER / ADMIN | Get ride by ID |
| PUT | /api/rides/{id}/assign?driverId={driverId} | ADMIN | Assign an available driver |
| PUT | /api/rides/{id}/accept | DRIVER | Accept an assigned ride |
| PUT | /api/rides/{id}/start | DRIVER | Start an accepted ride |
| PUT | /api/rides/{id}/complete?distanceKm={value}&durationMin={value} | DRIVER | Complete a ride |
| PUT | /api/rides/{id}/cancel | PASSENGER / DRIVER / ADMIN | Cancel a ride |

## Security

The Ride Management Service uses JWT Bearer authentication.

Supported roles:

- PASSENGER
- DRIVER
- ADMIN

Security behavior verified:

- No JWT token -> 401 Unauthorized
- PASSENGER accessing ADMIN-only assignment endpoint -> 403 Forbidden

## Environment Variables

The following environment variables are required:

- MONGODB_URI
- JWT_SECRET
- DRIVER_SERVICE_URL

Default Driver Service URL:

http://localhost:8082

Do not commit database passwords, JWT secrets, or access tokens.

## Driver Service Integration

The Ride Management Service communicates with the Driver & Vehicle Service on port 8082.

When an ADMIN assigns a driver:

1. Ride Service receives the driver ID.
2. Ride Service calls GET /api/drivers/{id}.
3. It verifies that the driver exists.
4. It verifies that availability is AVAILABLE.
5. It stores the driver ID in the ride.
6. Ride status changes from REQUESTED to ASSIGNED.

## Running the Ride Management Service

Navigate to:

C:\Users\Dr.PC\Desktop\IT3130-RideLink\ride-management-service

Then run:

mvn spring-boot:run

Ride Service URL:

http://localhost:8083

## Swagger / OpenAPI

Swagger UI is available while the Ride Management Service is running:

http://localhost:8083/swagger-ui.html

JWT Bearer authentication can be supplied using the Swagger Authorize option.

## Automated Tests

Run all Ride Management tests with:

mvn test

Current verified result:

- Tests run: 18
- Failures: 0
- Errors: 0
- Skipped: 0
- BUILD SUCCESS

The tests cover:

- Ride lifecycle
- Invalid lifecycle transitions
- Driver availability validation
- JWT authentication
- Role-based authorization
- Application context loading

## Manual End-to-End Verification

The following lifecycle was successfully tested:

REQUESTED -> ASSIGNED -> ACCEPTED -> IN_PROGRESS -> COMPLETED

Negative lifecycle test:

COMPLETED -> START = 409 Conflict

Security verification:

- No JWT token -> 401 Unauthorized
- PASSENGER accessing ADMIN-only endpoint -> 403 Forbidden

## Postman Collection

Postman collection:

postman/RideLink-Ride-Management.postman_collection.json

The collection includes login requests and all Ride Management endpoints.
Passwords and JWT tokens are not stored in the committed collection.

## Related Services

| Service | Port |
|---|---|
| Account Service | 8081 |
| Driver & Vehicle Service | 8082 |
| Ride Management Service | 8083 |

## Member 3 Completed Work

- Ride creation and retrieval
- Ride lifecycle management
- Ride cancellation
- Lifecycle validation
- Error handling
- JWT authentication
- Role-based authorization
- Swagger / OpenAPI
- Unit tests
- Security tests
- Driver Service integration
- Driver availability validation
- Postman collection
- End-to-end lifecycle testing

## Remaining Group-Level Work

- Fare / Payment Service integration verification
- CI / GitHub Actions verification
- Final Pull Request and code review
- Final integration testing
- Demo / viva preparation
