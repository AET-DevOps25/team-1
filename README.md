# AI-HR Recruitment System

A comprehensive microservice-based AI-powered HR recruitment system built with Spring Boot 3.4, JDK 21, and PostgreSQL.

## Architecture Overview

This system consists of four main microservices:

- **service-auth** (Port 8080): Authentication and user management
- **service-job** (Port 8081): Job posting and management  
- **service-application** (Port 8082): Application submission and tracking
- **service-assess** (Port 8083): AI assessments and scoring

## Technology Stack

- **Backend**: Spring Boot 3.4, JDK 21
- **Database**: PostgreSQL 15
- **Security**: JWT with RSA-256 signing, Argon2 password hashing
- **Build Tool**: Gradle 8.5
- **Containerization**: Docker & Docker Compose

## Quick Start

### Prerequisites

- JDK 21
- Docker and Docker Compose
- Git

### 1. Clone the Repository

```bash
git clone <repository-url>
cd team-1
```

### 2. Start the Database

```bash
docker-compose up -d postgresql
```

### 3. Run Database Migrations

The database schema is automatically applied using the `postgresql/init.sql` file.

### 4. Build and Run Services

#### Option A: Using Docker Compose (Recommended)

```bash
# Build and start all services
docker-compose up --build

# Or start specific services
docker-compose up service-auth service-job
```

#### Option B: Using Gradle (Development)

```bash
# Build all services
./gradlew build

# Run individual services
./gradlew :service-auth:bootRun     # Port 8080
./gradlew :service-job:bootRun      # Port 8081  
./gradlew :service-application:bootRun  # Port 8082
./gradlew :service-assess:bootRun   # Port 8083
```

## Service Details

### service-auth (Authentication Service)

**Port**: 8080  
**Endpoints**:
- `POST /api/v1/auth/register` - User registration
- `POST /api/v1/auth/login` - User login
- `POST /api/v1/auth/refresh` - Refresh JWT token
- `POST /api/v1/auth/logout` - User logout
- `GET /api/v1/auth/profile` - Get user profile
- `POST /api/v1/users/hr` - Create HR user (admin only)

**Features**:
- JWT token-based authentication with RSA-256 signing
- Dual JWT strategy (access + refresh tokens)
- Argon2 password hashing
- Role-based access control (HR, CANDIDATE)
- User profile management

### service-job (Job Management Service)

**Port**: 8081  
**Endpoints**:
- `GET /api/v1/jobs` - List jobs (with pagination and role-based filtering)
- `POST /api/v1/jobs` - Create job (HR only)
- `GET /api/v1/jobs/{jobId}` - Get job details
- `PUT /api/v1/jobs/{jobId}` - Update job (HR only)
- `POST /api/v1/jobs/{jobId}/close` - Close job (HR only)

**Features**:
- Job CRUD operations
- Role-based job visibility (OPEN jobs for candidates, all jobs for HR)
- Job status management (DRAFT, OPEN, CLOSED)
- Pagination and filtering

### service-application (Application Management Service)

**Port**: 8082  
**Endpoints**:
- `POST /api/v1/applications` - Submit application (Candidates only)
- `GET /api/v1/applications` - List applications (role-based)
- `GET /api/v1/applications/{applicationId}` - Get application details
- `PUT /api/v1/applications/{applicationId}/status` - Update application status (HR only)
- `DELETE /api/v1/applications/{applicationId}` - Withdraw application (Candidates only)

**Features**:
- Application submission with resume upload
- Application status tracking (PENDING, UNDER_REVIEW, REJECTED, ACCEPTED)
- File storage for resumes (PDF, DOC, DOCX support)
- Role-based application access

### service-assess (Assessment Service)

**Port**: 8083  
**Endpoints**:
- `POST /api/v1/assessments` - Create assessment (HR only)
- `GET /api/v1/assessments` - List assessments (role-based)
- `GET /api/v1/assessments/{assessmentId}` - Get assessment details
- `POST /api/v1/assessments/{assessmentId}/start` - Start assessment (Candidates only)
- `POST /api/v1/assessments/{assessmentId}/submit` - Submit assessment (Candidates only)
- `POST /api/v1/assessments/{assessmentId}/score` - Score assessment (HR only)

**Features**:
- AI assessment creation and management
- Assessment lifecycle tracking (PENDING, IN_PROGRESS, COMPLETED, CANCELLED)
- Scoring and feedback system
- Time tracking for assessments

## API Authentication

All protected endpoints require a valid JWT token in the Authorization header:

```bash
Authorization: Bearer <your-jwt-token>
```

### Getting a JWT Token

1. Register a user:
```bash
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "fullName": "John Doe",
    "email": "john@example.com", 
    "password": "password123"
  }'
```

2. Login to get tokens:
```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "john@example.com",
    "password": "password123"
  }'
```

## Database Schema

The system uses a PostgreSQL database with the following main tables:

- `users` - User accounts and profiles
- `user_roles` - User role assignments  
- `jobs` - Job postings
- `applications` - Job applications
- `assessments` - AI assessments

Full schema available in `postgresql/init.sql`.

## Configuration

### Environment Variables

Key configuration options can be set via environment variables:

```bash
# Database
DB_HOST=localhost
DB_PORT=5432
DB_NAME=ai_hr_db
DB_USERNAME=ai_hr_user
DB_PASSWORD=ai_hr_password

# JWT (Optional - uses fallback key generation)
JWT_PRIVATE_KEY_PATH=/path/to/private-key.pem
JWT_PUBLIC_KEY_PATH=/path/to/public-key.pem
```

### Service Ports

- service-auth: 8080
- service-job: 8081
- service-application: 8082
- service-assess: 8083
- PostgreSQL: 5432

## Development

### Project Structure

```
team-1/
├── service-auth/           # Authentication service
├── service-job/            # Job management service  
├── service-application/    # Application management service
├── service-assess/         # Assessment service
├── postgresql/             # Database initialization
├── frontend/               # Frontend application (placeholder)
├── docs/                   # Documentation
├── build.gradle           # Root build configuration
├── settings.gradle         # Multi-module setup
└── docker-compose.yml     # Container orchestration
```

### Building Individual Services

```bash
# Build specific service
./gradlew :service-auth:build
./gradlew :service-job:build
./gradlew :service-application:build
./gradlew :service-assess:build

# Run tests
./gradlew :service-auth:test

# Clean build
./gradlew clean build
```

### Inter-Service Communication

Services communicate via HTTP REST APIs. Currently using mock data for development, with plans to implement proper service discovery and load balancing.

## Security Features

- **JWT Authentication**: RSA-256 signed tokens with 1-hour access tokens and 7-day refresh tokens
- **Password Security**: Argon2 hashing with salt
- **Role-Based Access**: HR and CANDIDATE roles with endpoint-level security
- **CORS Configuration**: Configurable cross-origin resource sharing
- **Input Validation**: Comprehensive request validation with error handling

## Monitoring and Health Checks

Each service provides a health check endpoint:

- `GET /api/v1/auth/health` (service-auth)
- `GET /api/v1/jobs/health` (service-job)
- `GET /api/v1/applications/health` (service-application)
- `GET /api/v1/assessments/health` (service-assess)

## API Documentation

Detailed API documentation is available in `api-documentation.yaml` (OpenAPI 3.0 format).

## Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Support

For support and questions, please open an issue in the repository.