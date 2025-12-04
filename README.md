# Spring Boot Security PERMISSION-Based OAuth2

A Spring Boot 2.x reference implementation that demonstrates how to secure REST APIs with a classic OAuth2 Authorization Server, JWT token store, and fine-grained permission checks on each endpoint. The project focuses on showing how roles aggregate permissions and how those permissions are enforced via `@PreAuthorize` expressions.

## Features

- **OAuth2 Authorization Server**: Classic OAuth2 server with password grant type
- **JWT Token Store**: Stateless authentication using JWT tokens
- **Permission-Based Security**: Fine-grained access control using custom permissions
- **Role Aggregation**: Roles contain multiple permissions for simplified management
- **Method Security**: Endpoints secured with `@PreAuthorize` annotations
- **H2 Database**: In-memory database with sample data preloaded

## Architecture

### Domain Model

The application uses a three-tier security model:

1. **Users**: Application users with credentials
2. **Roles**: Named groups that aggregate permissions (e.g., ADMIN, MANAGER, USER)
3. **Permissions**: Fine-grained access rights (e.g., USER_READ, USER_CREATE, PRODUCT_UPDATE)

### Sample Data

The application initializes with three demo users:

| Username | Password    | Role    | Permissions                                                                                             |
|----------|-------------|---------|---------------------------------------------------------------------------------------------------------|
| admin    | admin123    | ADMIN   | All permissions (USER_*, PRODUCT_*, REPORT_*)                                                          |
| manager  | manager123  | MANAGER | USER_READ, USER_UPDATE, PRODUCT_READ, PRODUCT_CREATE, PRODUCT_UPDATE, REPORT_READ, REPORT_CREATE      |
| user     | user123     | USER    | USER_READ, PRODUCT_READ, REPORT_READ                                                                   |

### Permissions

The application defines the following permissions:

- **User Management**: `USER_READ`, `USER_CREATE`, `USER_UPDATE`, `USER_DELETE`
- **Product Management**: `PRODUCT_READ`, `PRODUCT_CREATE`, `PRODUCT_UPDATE`, `PRODUCT_DELETE`
- **Report Management**: `REPORT_READ`, `REPORT_CREATE`

## Getting Started

### Prerequisites

- Java 17 or higher
- Maven 3.6+

### Building the Project

```bash
mvn clean package
```

### Running the Application

```bash
mvn spring-boot:run
```

Or run the JAR file:

```bash
java -jar target/oauth2-permission-security-1.0.0.jar
```

The application will start on port 8080.

## Usage

### 1. Obtain an Access Token

Use the OAuth2 password grant to obtain a JWT access token:

```bash
curl -X POST http://localhost:8080/oauth/token \
  -H "Authorization: Basic Y2xpZW50LWFwcDpzZWNyZXQ=" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "grant_type=password&username=admin&password=admin123"
```

The `Authorization` header contains `client-app:secret` encoded in Base64.

Response:
```json
{
  "access_token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "token_type": "bearer",
  "refresh_token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "expires_in": 3599,
  "scope": "read write"
}
```

### 2. Access Protected Endpoints

Use the access token to call protected endpoints:

```bash
# Get user profile (requires authentication)
curl -X GET http://localhost:8080/api/users/profile \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN"

# List all users (requires USER_READ permission)
curl -X GET http://localhost:8080/api/users \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN"

# Create a new user (requires USER_CREATE permission)
curl -X POST http://localhost:8080/api/users \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"username":"newuser","password":"password"}'
```

### 3. Test Permission-Based Access Control

Try accessing endpoints with different user roles:

```bash
# As 'user' (read-only access)
# ✓ GET /api/users (has USER_READ)
# ✗ POST /api/users (missing USER_CREATE)

# As 'manager' (read-write access)
# ✓ GET /api/users (has USER_READ)
# ✓ POST /api/users (has USER_CREATE)
# ✗ DELETE /api/users/1 (missing USER_DELETE)

# As 'admin' (full access)
# ✓ All operations
```

## API Endpoints

### Public Endpoints (No Authentication)

- `GET /api/public/health` - Health check
- `GET /api/public/info` - Application information

### User Management

- `GET /api/users` - List all users (requires `USER_READ`)
- `GET /api/users/{id}` - Get user by ID (requires `USER_READ`)
- `GET /api/users/profile` - Get current user profile (requires authentication)
- `POST /api/users` - Create new user (requires `USER_CREATE`)
- `PUT /api/users/{id}` - Update user (requires `USER_UPDATE`)
- `DELETE /api/users/{id}` - Delete user (requires `USER_DELETE`)

### Product Management

- `GET /api/products` - List all products (requires `PRODUCT_READ`)
- `GET /api/products/{id}` - Get product by ID (requires `PRODUCT_READ`)
- `POST /api/products` - Create new product (requires `PRODUCT_CREATE`)
- `PUT /api/products/{id}` - Update product (requires `PRODUCT_UPDATE`)
- `DELETE /api/products/{id}` - Delete product (requires `PRODUCT_DELETE`)

### Report Management

- `GET /api/reports/sales` - Get sales report (requires `REPORT_READ`)
- `GET /api/reports/financial` - Get financial report (requires `REPORT_READ` and `ROLE_ADMIN`)
- `POST /api/reports/generate` - Generate report (requires `REPORT_CREATE`)

## Configuration

Key configuration properties in `application.properties`:

```properties
# OAuth2 Client Configuration
security.oauth2.client.client-id=client-app
security.oauth2.client.client-secret=secret

# JWT Configuration
security.oauth2.jwt.signing-key=oauth2-demo-secret-key-for-jwt-token-signing

# Token Validity (seconds)
security.oauth2.access-token-validity=3600
security.oauth2.refresh-token-validity=7200
```

## H2 Database Console

Access the H2 console at: http://localhost:8080/h2-console

- JDBC URL: `jdbc:h2:mem:oauth2db`
- Username: `sa`
- Password: (empty)

## Security Implementation Details

### CustomUserDetailsService

Loads user details from the database and constructs authorities from roles and permissions.

### Authorization Server

Configured to issue JWT tokens using the password grant type. Tokens include user authorities (roles and permissions).

### Resource Server

Validates JWT tokens and enforces method-level security using `@PreAuthorize` annotations.

### Permission Checks

Endpoints use Spring Security expressions:

```java
@PreAuthorize("hasAuthority('USER_READ')")  // Requires specific permission
@PreAuthorize("hasRole('ADMIN')")           // Requires specific role
@PreAuthorize("hasAuthority('REPORT_READ') and hasRole('ADMIN')")  // Multiple conditions
```

## Technology Stack

- Spring Boot 2.7.18
- Spring Security with OAuth2
- Spring Data JPA
- H2 Database
- JWT (JSON Web Tokens)
- Lombok
- Maven

## Notes for Production Use

This is a reference implementation for educational purposes. For production deployment, consider:

1. **Security Configuration**: The implementation uses `WebSecurityConfigurerAdapter` which is deprecated in Spring Security 5.7+. For new projects, use `SecurityFilterChain` bean configuration instead.

2. **JWT Signing Key**: Move the JWT signing key to environment variables or secure configuration management (e.g., Spring Cloud Config, HashiCorp Vault).

3. **Account Status**: Implement actual account expiration, locking, and credential expiration logic based on business requirements.

4. **Database**: Replace H2 with a production-grade database (PostgreSQL, MySQL, etc.).

5. **Password Policies**: Implement password strength requirements and rotation policies.

6. **Logging**: Add comprehensive audit logging for security events.

7. **Rate Limiting**: Implement rate limiting to prevent brute force attacks.

8. **CSRF Protection**: This implementation disables CSRF protection as it's designed for stateless JWT-based authentication where tokens are sent via Authorization headers (not cookies). JWT tokens are not vulnerable to CSRF attacks. If you implement session-based authentication or use cookies, enable CSRF protection.

## License

This is a reference implementation for educational purposes.
