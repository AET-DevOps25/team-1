# Assessment Service (service-assess)

AI-powered assessment management microservice for the AI-HR recruitment system.

## Overview

The Assessment Service handles the creation, management, and scoring of AI-powered assessments for job candidates. It provides comprehensive assessment lifecycle management including creation by HR users, candidate participation, and automated/manual scoring.

## Features

- **Assessment Creation**: HR users can create various types of assessments
- **Assessment Lifecycle**: Complete workflow from creation to completion
- **AI Integration**: Support for AI-powered assessment data and analysis
- **Scoring System**: Flexible scoring with feedback mechanisms
- **Role-Based Access**: Different access levels for HR and candidates
- **Real-time Tracking**: Assessment progress and timing tracking

## Technology Stack

- **Framework**: Spring Boot 3.4
- **Language**: Java 21
- **Database**: PostgreSQL with JPA/Hibernate
- **Security**: JWT-based authentication with role-based authorization
- **Build Tool**: Gradle 8.5

## API Endpoints

### Assessment Management

#### Create Assessment (HR Only)
```http
POST /api/v1/assessments
Authorization: Bearer <jwt-token>
Content-Type: application/json

{
  "applicationId": "uuid",
  "assessmentType": "Technical Assessment",
  "maxScore": 100,
  "assessmentData": "JSON configuration"
}
```

#### List Assessments
```http
GET /api/v1/assessments?page=0&size=10&status=PENDING
Authorization: Bearer <jwt-token>
```

#### Get Assessment Details
```http
GET /api/v1/assessments/{assessmentId}
Authorization: Bearer <jwt-token>
```

### Assessment Participation (Candidates)

#### Start Assessment
```http
POST /api/v1/assessments/{assessmentId}/start
Authorization: Bearer <jwt-token>
```

#### Submit Assessment
```http
POST /api/v1/assessments/{assessmentId}/submit
Authorization: Bearer <jwt-token>
Content-Type: application/json

"assessment response data"
```

### Assessment Scoring (HR Only)

#### Score Assessment
```http
POST /api/v1/assessments/{assessmentId}/score?score=85&feedback=Great work
Authorization: Bearer <jwt-token>
```

### Health Check
```http
GET /api/v1/assessments/health
```

## Assessment Workflow

1. **Creation**: HR user creates an assessment for a specific application
2. **Assignment**: Assessment is assigned to the candidate
3. **Start**: Candidate starts the assessment (status: IN_PROGRESS)
4. **Completion**: Candidate submits assessment responses (status: COMPLETED)
5. **Scoring**: HR or AI system scores the assessment
6. **Feedback**: Results and feedback are provided

## Assessment Status

- `PENDING`: Assessment created but not started
- `IN_PROGRESS`: Assessment started by candidate
- `COMPLETED`: Assessment submitted by candidate
- `CANCELLED`: Assessment cancelled by HR

## Database Schema

### Assessment Entity
```sql
CREATE TABLE assessments (
    assessment_id UUID PRIMARY KEY,
    application_id UUID NOT NULL,
    assessment_type VARCHAR(100) NOT NULL,
    status assessment_status NOT NULL DEFAULT 'PENDING',
    score INTEGER,
    max_score INTEGER,
    feedback TEXT,
    assessment_data TEXT,
    creation_timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    start_time TIMESTAMP,
    end_time TIMESTAMP,
    last_modified_timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    hr_creator_id UUID NOT NULL
);
```

## Configuration

### Application Properties (application.yml)

```yaml
server:
  port: 8083

spring:
  application:
    name: assess-service
  datasource:
    url: jdbc:postgresql://localhost:5432/ai_hr_db
    username: ai_hr_user
    password: ai_hr_password

app:
  jwt:
    public-key: |
      -----BEGIN PUBLIC KEY-----
      [RSA Public Key for JWT validation]
      -----END PUBLIC KEY-----
```

## Running the Service

### Prerequisites
- JDK 21
- PostgreSQL database running
- Valid JWT public key configuration

### Using Gradle
```bash
# From project root
./gradlew :service-assess:bootRun

# Or from service directory
cd service-assess
../gradlew bootRun
```

### Using Docker
```bash
# Build image
docker build -t assess-service .

# Run container
docker run -p 8083:8083 \
  -e DB_HOST=host.docker.internal \
  assess-service
```

### Using Docker Compose
```bash
# From project root
docker-compose up service-assess
```

## Development

### Project Structure
```
service-assess/
├── src/main/java/de/tum/devops/assess/
│   ├── AssessServiceApplication.java
│   ├── controller/
│   │   └── AssessmentController.java
│   ├── dto/
│   │   ├── AssessmentDto.java
│   │   ├── CreateAssessmentRequest.java
│   │   ├── ApiResponse.java
│   │   └── ...
│   ├── entity/
│   │   ├── Assessment.java
│   │   └── AssessmentStatus.java
│   ├── repository/
│   │   └── AssessmentRepository.java
│   ├── service/
│   │   ├── AssessmentService.java
│   │   ├── ApplicationService.java
│   │   └── UserService.java
│   ├── config/
│   │   ├── SecurityConfig.java
│   │   └── WebConfig.java
│   └── exception/
│       └── GlobalExceptionHandler.java
└── src/main/resources/
    └── application.yml
```

### Testing
```bash
# Run unit tests
./gradlew :service-assess:test

# Run with coverage
./gradlew :service-assess:test jacocoTestReport
```

## Inter-Service Communication

The Assessment Service communicates with:

- **service-auth**: User information and validation
- **service-application**: Application details and validation

Communication is handled via HTTP REST calls using WebClient.

## Security

- **JWT Authentication**: All endpoints require valid JWT tokens
- **Role-Based Authorization**: Different access levels for HR and candidates
- **Method-Level Security**: `@PreAuthorize` annotations on sensitive operations

### Required Roles

- **HR**: Can create, view, and score all assessments
- **CANDIDATE**: Can start, submit, and view own assessments

## Monitoring

### Health Check
```bash
curl http://localhost:8083/api/v1/assessments/health
```

### Metrics
- Application metrics available via Spring Boot Actuator
- Custom metrics for assessment completion rates and scores

## Error Handling

The service provides consistent error responses:

```json
{
  "success": false,
  "message": "Error description",
  "data": null,
  "timestamp": "2024-01-15T10:30:00",
  "code": 400
}
```

## Future Enhancements

- **AI Integration**: Direct integration with AI scoring services
- **Assessment Templates**: Predefined assessment templates
- **Advanced Analytics**: Assessment performance analytics
- **Real-time Notifications**: WebSocket support for real-time updates
- **Assessment Scheduling**: Time-based assessment scheduling

## Contributing

1. Follow the existing code style and patterns
2. Add unit tests for new functionality
3. Update documentation for API changes
4. Ensure all endpoints are properly secured

## Support

For issues and questions related to the Assessment Service, please refer to the main project documentation or create an issue in the repository. 