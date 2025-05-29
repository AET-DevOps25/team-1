# 🔐 Service-Auth - Authentication Microservice

Authentication microservice for the AI-HR recruitment system, responsible for user authentication, JWT token management, and user registration.

## 🚀 Features

### Authentication Functions
- ✅ **User Login**: Email + password authentication
- ✅ **Candidate Registration**: Automatic registration with CANDIDATE role
- ✅ **Token Refresh**: Get new access token using refresh token
- ✅ **User Logout**: Simple logout response
- ✅ **User Profile**: Get current logged-in user information

### User Management
- ✅ **HR User Creation**: Existing HR users can create other HR accounts

### Technical Features
- 🔑 **RSA-JWT**: JWT tokens signed with RSA private key
- 🛡️ **Argon2**: Modern password hashing algorithm
- 🏛️ **Spring Security**: Native JWT support
- 📊 **Unified Response**: Strictly follows API documentation format
- ⚡ **Stateless**: JWT tokens require no storage

## 🏗️ Technology Stack

- **Spring Boot 3.4**
- **JDK 21**
- **Spring Security 6.x**
- **Spring Data JPA**
- **PostgreSQL**
- **Argon2 Password Encoding**
- **RSA-256 JWT**

## 📡 API Endpoints

### Authentication Endpoints
```http
POST /api/v1/auth/login      # User login
POST /api/v1/auth/register   # Candidate registration
POST /api/v1/auth/refresh    # Token refresh
POST /api/v1/auth/logout     # User logout
GET  /api/v1/auth/profile    # Get user information
```

### User Management Endpoints
```http
POST /api/v1/users/hr        # Create HR user (requires HR permission)
```

### Health Check
```http
GET  /api/v1/health          # Service health status
```

## 🔧 Configuration Requirements

### Environment Variables
```bash
DB_USERNAME=aihr_user
DB_PASSWORD=your_password
JWT_PRIVATE_KEY_PATH=classpath:rsa-private-key.pem
JWT_ISSUER=ai-hr-system
```

### RSA Private Key
RSA private key file must be provided at `src/main/resources/rsa-private-key.pem`:
```pem
-----BEGIN PRIVATE KEY-----
Your RSA private key content here
-----END PRIVATE KEY-----
```

## 🚀 Running the Service

### Development Environment
```bash
# Run from root directory
./gradlew :service-auth:bootRun

# Or from service-auth directory
cd service-auth
../gradlew bootRun
```

### Docker
```bash
# Build image
docker build -t service-auth .

# Run container
docker run -p 8080:8080 \
  -e DB_USERNAME=aihr_user \
  -e DB_PASSWORD=password \
  service-auth
```

## 📊 JWT Token Format

### Access Token (1 hour validity)
```json
{
  "iss": "ai-hr-system",
  "sub": "user-uuid",
  "iat": 1640995200,
  "exp": 1640998800,
  "email": "user@example.com",
  "role": "CANDIDATE",
  "fullName": "John Smith",
  "type": "access"
}
```

### Refresh Token (7 days validity)
```json
{
  "iss": "ai-hr-system", 
  "sub": "user-uuid",
  "iat": 1640995200,
  "exp": 1641600000,
  "type": "refresh"
}
```

## 🔐 Security Features

- **Argon2 Password Hashing**: Resistant to timing/memory attacks
- **RSA-256 Signing**: Private key signing, public key verification
- **CORS Support**: Frontend integration
- **Input Validation**: Bean Validation
- **Global Exception Handling**: Unified error responses

## 🧪 Testing Examples

### User Registration
```bash
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "fullName": "John Smith",
    "email": "john@example.com", 
    "password": "password123"
  }'
```

### User Login
```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "john@example.com",
    "password": "password123"
  }'
```

### Get User Information
```bash
curl -X GET http://localhost:8080/api/v1/auth/profile \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN"
```

## 📝 Database Schema

The service uses PostgreSQL database with schema defined in `postgresql/init.sql` in the root directory:

```sql
CREATE TABLE users (
    user_id UUID PRIMARY KEY,
    full_name VARCHAR(255) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    password_hash TEXT NOT NULL,
    role user_role NOT NULL,
    creation_timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

## 🚧 Development Notes

1. **Key Management**: Production environment must use real RSA key pairs
2. **Database Connection**: Ensure PostgreSQL service is available
3. **Log Level**: Enable DEBUG logs in development environment
4. **CORS Configuration**: Adjust CORS settings according to frontend domain

## 📞 Contact Information

- **Team**: AI-HR Team
- **Email**: yifei.liu@tum.de
- **License**: MIT 