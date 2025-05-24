# AI-HR Gateway Service

## Overview

The Gateway Service is the authentication and user management service for the AI-HR system, responsible for:

- User registration and login
- JWT token management
- User permission control
- System initialization

## Technology Stack

- **Spring Boot 3.5.0** - Main framework
- **Spring Security** - Security framework
- **Spring Data JPA** - Data access
- **PostgreSQL** - Production database
- **H2** - Test database
- **JWT (jjwt 0.12.3)** - Token authentication
- **Argon2** - Password encryption
- **Log4j2** - Log management

## Quick Start

### 1. Prerequisites

- Java 21+
- PostgreSQL 12+
- Gradle 8.13+

### 2. Database Setup

- Database should already be created as 'hrapp'
- Run the database initialization script
- See ../postgresql/init.sql

### 3. Run Application

#### Development Environment
```bash
# Run with development configuration
./gradlew runDev

# Or
./gradlew bootRun
```

#### Production Environment
```bash
# Build production JAR
./gradlew buildProd

# Run production JAR
java -jar build/libs/service-gateway-0.0.1-SNAPSHOT-prod.jar --spring.profiles.active=prod
```

### 4. Default Admin Account

The system automatically creates a default HR admin account on startup:

- **Email**: admin@aihr.com
- **Password**: Admin@123!
- **Role**: HR

⚠️ **Please change the password after first login!**

## API Documentation

### Authentication Endpoints

#### User Login
```http
POST /auth/login
Content-Type: application/json

{
  "email": "admin@aihr.com",
  "password": "Admin@123!"
}
```

#### User Registration
```http
POST /auth/register
Content-Type: application/json

{
  "fullName": "John Doe",
  "email": "john.doe@example.com",
  "password": "Password@123",
  "role": "CANDIDATE"
}
```

#### Get User Profile
```http
GET /auth/profile
Authorization: Bearer <access_token>
```

#### Refresh Token
```http
POST /auth/refresh
Content-Type: application/json

{
  "refreshToken": "<refresh_token>"
}
```

#### User Logout
```http
POST /auth/logout
Authorization: Bearer <access_token>
```

### User Management Endpoints

#### Add HR User (HR Only)
```http
POST /users/hr
Authorization: Bearer <hr_access_token>
Content-Type: application/json

{
  "fullName": "Jane Smith",
  "email": "jane.smith@aihr.com",
  "password": "HRPassword@123",
  "role": "HR"
}
```

#### Get All HR Users (HR Only)
```http
GET /users/hr
Authorization: Bearer <hr_access_token>
```

#### Get All Candidates (HR Only)
```http
GET /users/candidates
Authorization: Bearer <hr_access_token>
```

#### Search Users (HR Only)
```http
GET /users/search?email=john
Authorization: Bearer <hr_access_token>
```

#### Get User Details
```http
GET /users/{userId}
Authorization: Bearer <access_token>
```

#### Update User Profile
```http
PUT /users/{userId}/profile
Authorization: Bearer <access_token>
Content-Type: application/json

{
  "fullName": "New Full Name"
}
```

#### Change Password
```http
PUT /users/{userId}/password
Authorization: Bearer <access_token>
Content-Type: application/json

{
  "currentPassword": "old_password",
  "newPassword": "new_password"
}
```

## Configuration

### Environment Profiles

#### Development Environment (application-dev.properties)
- Local PostgreSQL database
- Detailed debug logging
- Development JWT secret

#### Production Environment (application-prod.properties)
- Environment variable configuration
- Optimized logging settings
- Secure JWT secret

### Environment Variables

Production environment requires the following environment variables:

```bash
# Database Configuration
DATABASE_URL=jdbc:postgresql://localhost:5432/hrapp
DATABASE_USERNAME=postgres
DATABASE_PASSWORD=your_password

# JWT Configuration
JWT_SECRET=your_super_secret_key_here
JWT_EXPIRATION=86400
JWT_REFRESH_EXPIRATION=604800

# Initial HR Account
INITIAL_HR_EMAIL=admin@yourcompany.com
INITIAL_HR_PASSWORD=YourSecurePassword
INITIAL_HR_FULLNAME=System Administrator

# Log Directory
LOG_DIR=/app/logs
```

## Security Features

### Password Security
- Uses Argon2id algorithm for password encryption
- Memory: 64MB, Iterations: 10, Parallelism: 2

### JWT Tokens
- Access tokens: 24-hour validity
- Refresh tokens: 7-day validity
- HMAC-SHA256 signature

### Permission Control
- **CANDIDATE**: Regular users, can only manage their own profile
- **HR**: Admin users, can manage all users and system functions

## Development Guide

### Project Structure
```
src/main/java/com/aihr/service/gateway/
├── entity/          # Entity classes
├── repository/      # Data access layer
├── service/         # Business logic layer
├── controller/      # Controller layer
├── dto/            # Data transfer objects
├── config/         # Configuration classes
└── util/           # Utility classes
```

### Build Commands

```bash
# Compile
./gradlew compileJava

# Run tests
./gradlew test

# Build JAR
./gradlew build

# Development run
./gradlew runDev

# Production build
./gradlew buildProd

# CI/CD build
./gradlew cicdBuild

# Show configuration info
./gradlew showProfiles
```

## Troubleshooting

### Common Issues

1. **Database Connection Failed**
   - Check if PostgreSQL is running
   - Verify database connection configuration
   - Ensure database 'hrapp' exists

2. **Invalid JWT Token**
   - Check if token has expired
   - Verify JWT secret configuration
   - Confirm token format is correct

3. **Permission Denied**
   - Check user role
   - Verify token validity
   - Confirm API endpoint permission requirements

### View Logs

Development environment:
```bash
# View application logs
tail -f logs/dev/application.log
```

Production environment:
```bash
# View application logs
tail -f /app/logs/application.log

# View error logs
tail -f /app/logs/error.log
```

## API Response Examples

### Successful Login Response
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "tokenType": "Bearer",
  "expiresIn": 86400,
  "user": {
    "userID": "123e4567-e89b-12d3-a456-426614174000",
    "fullName": "System Administrator",
    "email": "admin@aihr.com",
    "role": "HR",
    "creationTimestamp": "2024-01-01T10:00:00"
  }
}
```

### Error Response
```json
{
  "error": "Invalid email or password"
}
```

## Testing

### Unit Tests
```bash
# Run all tests
./gradlew test

# Run tests with coverage
./gradlew test jacocoTestReport
```

### API Testing with curl

#### Login
```bash
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "admin@aihr.com",
    "password": "Admin@123!"
  }'
```

#### Get Profile
```bash
curl -X GET http://localhost:8080/auth/profile \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN"
```

## Contributing

1. Fork the project
2. Create a feature branch
3. Commit your changes
4. Push to the branch
5. Create a Pull Request

## License

This project is licensed under the MIT License - see the [LICENSE](../LICENSE) file for details. 