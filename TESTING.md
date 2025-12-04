# Testing Guide

This document provides detailed instructions for testing the OAuth2 permission-based security implementation.

## Prerequisites

Ensure the application is running:

```bash
mvn spring-boot:run
```

## Test Scenarios

### Scenario 1: Public Endpoints (No Authentication Required)

```bash
# Health check
curl http://localhost:8080/api/public/health

# Application info
curl http://localhost:8080/api/public/info
```

**Expected Result**: Both endpoints return 200 OK without requiring authentication.

---

### Scenario 2: Obtaining Access Tokens

The Authorization header value `Y2xpZW50LWFwcDpzZWNyZXQ=` is Base64 encoded `client-app:secret`.

#### Admin User Token

```bash
curl -X POST http://localhost:8080/oauth/token \
  -H "Authorization: Basic Y2xpZW50LWFwcDpzZWNyZXQ=" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "grant_type=password&username=admin&password=admin123"
```

#### Manager User Token

```bash
curl -X POST http://localhost:8080/oauth/token \
  -H "Authorization: Basic Y2xpZW50LWFwcDpzZWNyZXQ=" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "grant_type=password&username=manager&password=manager123"
```

#### Regular User Token

```bash
curl -X POST http://localhost:8080/oauth/token \
  -H "Authorization: Basic Y2xpZW50LWFwcDpzZWNyZXQ=" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "grant_type=password&username=user&password=user123"
```

**Expected Result**: Each request returns a JSON response with `access_token`, `refresh_token`, and `expires_in`.

---

### Scenario 3: Testing Permission-Based Access Control

Set your access token in a variable for easier testing:

```bash
# Replace with your actual token
export ACCESS_TOKEN="your_access_token_here"
```

#### Test 1: Admin User (Full Access)

Admin has all permissions and should be able to access all endpoints.

```bash
# View profile and authorities
curl http://localhost:8080/api/users/profile \
  -H "Authorization: Bearer $ACCESS_TOKEN"

# Read users (USER_READ)
curl http://localhost:8080/api/users \
  -H "Authorization: Bearer $ACCESS_TOKEN"

# Create user (USER_CREATE)
curl -X POST http://localhost:8080/api/users \
  -H "Authorization: Bearer $ACCESS_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser","password":"test123"}'

# Update user (USER_UPDATE)
curl -X PUT http://localhost:8080/api/users/1 \
  -H "Authorization: Bearer $ACCESS_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"username":"updateduser"}'

# Delete user (USER_DELETE)
curl -X DELETE http://localhost:8080/api/users/1 \
  -H "Authorization: Bearer $ACCESS_TOKEN"

# Access financial report (REPORT_READ + ROLE_ADMIN)
curl http://localhost:8080/api/reports/financial \
  -H "Authorization: Bearer $ACCESS_TOKEN"
```

**Expected Result**: All requests return 200 OK with appropriate responses.

#### Test 2: Manager User (Read/Write Access)

Manager has read/write permissions but cannot delete users or access admin-only endpoints.

```bash
# Read users (USER_READ) - Should succeed
curl http://localhost:8080/api/users \
  -H "Authorization: Bearer $ACCESS_TOKEN"

# Create product (PRODUCT_CREATE) - Should succeed
curl -X POST http://localhost:8080/api/products \
  -H "Authorization: Bearer $ACCESS_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"name":"Laptop","price":999.99}'

# Delete user (USER_DELETE) - Should fail (403)
curl -X DELETE http://localhost:8080/api/users/1 \
  -H "Authorization: Bearer $ACCESS_TOKEN"

# Access financial report (ROLE_ADMIN required) - Should fail (403)
curl http://localhost:8080/api/reports/financial \
  -H "Authorization: Bearer $ACCESS_TOKEN"
```

**Expected Results**:
- Read and create operations: 200 OK
- Delete user: 403 Forbidden
- Financial report: 403 Forbidden

#### Test 3: Regular User (Read-Only Access)

Regular user has only read permissions.

```bash
# Read users (USER_READ) - Should succeed
curl http://localhost:8080/api/users \
  -H "Authorization: Bearer $ACCESS_TOKEN"

# Read products (PRODUCT_READ) - Should succeed
curl http://localhost:8080/api/products \
  -H "Authorization: Bearer $ACCESS_TOKEN"

# Create product (PRODUCT_CREATE) - Should fail (403)
curl -X POST http://localhost:8080/api/products \
  -H "Authorization: Bearer $ACCESS_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"name":"Laptop","price":999.99}'

# Delete product (PRODUCT_DELETE) - Should fail (403)
curl -X DELETE http://localhost:8080/api/products/1 \
  -H "Authorization: Bearer $ACCESS_TOKEN"
```

**Expected Results**:
- Read operations: 200 OK
- Write/delete operations: 403 Forbidden

---

### Scenario 4: Testing Without Authentication

```bash
# Try to access protected endpoint without token
curl http://localhost:8080/api/users

# Try with invalid token
curl http://localhost:8080/api/users \
  -H "Authorization: Bearer invalid_token"
```

**Expected Result**: 401 Unauthorized

---

### Scenario 5: Token Refresh

```bash
# Use refresh token to get new access token
curl -X POST http://localhost:8080/oauth/token \
  -H "Authorization: Basic Y2xpZW50LWFwcDpzZWNyZXQ=" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "grant_type=refresh_token&refresh_token=YOUR_REFRESH_TOKEN"
```

**Expected Result**: Returns a new access token.

---

## Complete Test Script

Here's a comprehensive test script that validates all scenarios:

```bash
#!/bin/bash

BASE_URL="http://localhost:8080"
CLIENT_AUTH="Y2xpZW50LWFwcDpzZWNyZXQ="

echo "==================================="
echo "OAuth2 Permission Security Tests"
echo "==================================="

# Test 1: Public endpoints
echo -e "\n[TEST 1] Public endpoints"
curl -s $BASE_URL/api/public/health | jq .

# Test 2: Get tokens
echo -e "\n[TEST 2] Obtaining tokens"
ADMIN_TOKEN=$(curl -s -X POST $BASE_URL/oauth/token \
  -H "Authorization: Basic $CLIENT_AUTH" \
  -d "grant_type=password&username=admin&password=admin123" | jq -r '.access_token')

MANAGER_TOKEN=$(curl -s -X POST $BASE_URL/oauth/token \
  -H "Authorization: Basic $CLIENT_AUTH" \
  -d "grant_type=password&username=manager&password=manager123" | jq -r '.access_token')

USER_TOKEN=$(curl -s -X POST $BASE_URL/oauth/token \
  -H "Authorization: Basic $CLIENT_AUTH" \
  -d "grant_type=password&username=user&password=user123" | jq -r '.access_token')

echo "Admin token: ${ADMIN_TOKEN:0:50}..."
echo "Manager token: ${MANAGER_TOKEN:0:50}..."
echo "User token: ${USER_TOKEN:0:50}..."

# Test 3: Admin permissions (should succeed)
echo -e "\n[TEST 3] Admin accessing DELETE endpoint"
curl -s -X DELETE $BASE_URL/api/users/1 \
  -H "Authorization: Bearer $ADMIN_TOKEN" | jq .

# Test 4: Manager permissions (should fail on delete)
echo -e "\n[TEST 4] Manager accessing DELETE endpoint (should fail)"
curl -s -X DELETE $BASE_URL/api/users/1 \
  -H "Authorization: Bearer $MANAGER_TOKEN"

# Test 5: User read-only (should succeed)
echo -e "\n[TEST 5] User accessing READ endpoint"
curl -s $BASE_URL/api/products \
  -H "Authorization: Bearer $USER_TOKEN" | jq .

# Test 6: User write attempt (should fail)
echo -e "\n[TEST 6] User accessing CREATE endpoint (should fail)"
curl -s -X POST $BASE_URL/api/products \
  -H "Authorization: Bearer $USER_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"name":"test"}'

# Test 7: No authentication (should fail)
echo -e "\n[TEST 7] Accessing protected endpoint without token (should fail)"
curl -s $BASE_URL/api/users

echo -e "\n==================================="
echo "Tests completed!"
echo "==================================="
```

Save this as `test-oauth2.sh`, make it executable with `chmod +x test-oauth2.sh`, and run it.

---

## Permission Matrix

| Endpoint                        | Admin | Manager | User |
|---------------------------------|-------|---------|------|
| GET /api/users                  | ✓     | ✓       | ✓    |
| POST /api/users                 | ✓     | ✗       | ✗    |
| PUT /api/users/{id}             | ✓     | ✓       | ✗    |
| DELETE /api/users/{id}          | ✓     | ✗       | ✗    |
| GET /api/products               | ✓     | ✓       | ✓    |
| POST /api/products              | ✓     | ✓       | ✗    |
| PUT /api/products/{id}          | ✓     | ✓       | ✗    |
| DELETE /api/products/{id}       | ✓     | ✗       | ✗    |
| GET /api/reports/sales          | ✓     | ✓       | ✓    |
| GET /api/reports/financial      | ✓     | ✗       | ✗    |
| POST /api/reports/generate      | ✓     | ✓       | ✗    |

✓ = Allowed, ✗ = Denied (403 Forbidden)

---

## Troubleshooting

### Issue: 401 Unauthorized

**Cause**: Token is missing, invalid, or expired.

**Solution**:
1. Verify you're including the Authorization header
2. Check that the token hasn't expired (default: 1 hour)
3. Obtain a new token

### Issue: 403 Forbidden

**Cause**: User lacks the required permission for the operation.

**Solution**:
1. Check the user's role and permissions
2. Use a user with appropriate permissions
3. Verify the `@PreAuthorize` annotation on the endpoint

### Issue: Invalid client credentials

**Cause**: Incorrect client ID or secret.

**Solution**:
- Verify the Basic Auth header contains base64 encoded `client-app:secret`
- Check `application.properties` for correct client credentials

---

## H2 Console Access

You can inspect the database tables at: http://localhost:8080/h2-console

- JDBC URL: `jdbc:h2:mem:oauth2db`
- Username: `sa`
- Password: (leave empty)

Useful queries:

```sql
-- View all users and their roles
SELECT u.username, r.name as role_name
FROM users u
JOIN user_roles ur ON u.id = ur.user_id
JOIN roles r ON ur.role_id = r.id;

-- View all roles and their permissions
SELECT r.name as role_name, p.name as permission_name
FROM roles r
JOIN role_permissions rp ON r.id = rp.role_id
JOIN permissions p ON rp.permission_id = p.id
ORDER BY r.name, p.name;
```
