Restart Spring Boot if necessary:

$env:MONGODB_URI = "mongodb://localhost:27017/ridelink_payment_db"

$env:JWT_SECRET = "RideLinkDevSecretKey2026Member4ABC123"

.\mvnw.cmd spring-boot:run

Swagger UI:
http://localhost:8084/swagger-ui.html

OpenAPI JSON:
http://localhost:8084/v3/api-docs

your one-hour test token may have expired. In that case regenerate it with:
.\mvnw.cmd -Dtest=JwtTestTokenGeneratorTest test