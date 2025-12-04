# Spring Boot Security – Permission-Based OAuth2

A Spring Boot 2.2 reference implementation that demonstrates how to secure REST APIs with a classic OAuth2 Authorization Server, JWT token store, and fine-grained permission checks on each endpoint. The project focuses on showing how roles aggregate permissions and how those permissions are enforced via `@PreAuthorize` expressions.

## Tech Stack
- Spring Boot 2.2.6 (web, data JPA)
- Spring Security OAuth2 + Spring Security JWT
- H2 in-memory database with schema/data bootstrap scripts
- Lombok-free Java 8 code base for clarity

## Architecture Overview
| Layer | Location | Responsibility |
| --- | --- | --- |
| Authorization Server | `security/config/AuthorizationServerConfig.java` | Issues JWT access tokens for the `admin/password` client; exposes `/oauth/token` supporting password, authorization-code, refresh-token, and implicit grants. |
| Resource Server | `security/config/ResourceServerConfig.java` | Validates JWTs and guards `/admin/**` plus any `@PreAuthorize` protected controller methods. |
| Web Security | `security/config/WebSecurityConfig.java` | Wires the custom `UserDetailsService`, enables method-level security, exposes `AuthenticationManager` bean for OAuth2 flows. |
| Domain & Services | `user/model`, `user/service` | Models users/roles/permissions, loads authorities, and exposes CRUD APIs from `UserController`. |
| Exception & Logging | `exception/*`, `logback.xml` | Centralizes API error responses and ships logs both to STDOUT and `rest-api.log`. |

A high-level swimlane of the auth dance is captured in `docs/Role-Based-oAuth.gif`.

## Prerequisites
- JDK 8+
- Maven 3.6+
- curl or Postman for trying the token + data flows
- (Optional) Browser access to the H2 console at `http://localhost:8090/h2-console`

## Getting Started
```bash
# from any folder
git clone https://github.com/recaifurkan/Spring-Boot-Security-PERMISSION-Based-oAuth2
cd Spring-Boot-Security-PERMISSION-Based-oAuth2
mvn clean spring-boot:run
```
The app starts on port `8090` (see `src/main/resources/application.properties`). Schema and seed data are loaded automatically from `schema.sql` and `data.sql`.

### Database Bootstrap & H2 Console
- JDBC URL: `jdbc:h2:mem:research`
- Username: `sa`
- Password: *(blank)*
- Console path: `/h2-console`

Seeded accounts and permissions live in `src/main/resources/data.sql`. The default API user is `admin` with password `password` (BCrypted in the script).

## OAuth2 Token Flow
1. Base64 encode the OAuth2 client credentials `admin:password` and place them in the HTTP Basic header.
2. POST to `/oauth/token` with the desired grant type. Password grant is the quickest way to get a token locally.
3. Use the returned JWT (`access_token`) as a `Bearer` token on protected resources.

### Password Grant Example
```bash
curl -X POST http://localhost:8090/oauth/token \
  -H 'Authorization: Basic YWRtaW46cGFzc3dvcmQ=' \
  -H 'Content-Type: application/x-www-form-urlencoded' \
  -d 'grant_type=password&username=admin&password=password&scope=read'
```
Response payload (abridged):
```json
{
  "access_token": "eyJhbGciOiJIUzI1NiJ9...",
  "token_type": "bearer",
  "expires_in": 43199,
  "scope": "read write trust"
}
```

### Refreshing Tokens
```bash
curl -X POST http://localhost:8090/oauth/token \
  -H 'Authorization: Basic YWRtaW46cGFzc3dvcmQ=' \
  -H 'Content-Type: application/x-www-form-urlencoded' \
  -d 'grant_type=refresh_token&refresh_token=<refresh-token>'
```

### Authorization Code and Implicit
Sample redirect URLs are not baked into the project, but the authorization server is configured to allow `authorization_code` and `implicit` grants for experimentation if you add a front-end client.

## Calling Protected APIs
All application endpoints hang under `/users`. Method-level guards map to specific permissions.

```bash
TOKEN=$(curl -s -X POST http://localhost:8090/oauth/token \
  -H 'Authorization: Basic YWRtaW46cGFzc3dvcmQ=' \
  -H 'Content-Type: application/x-www-form-urlencoded' \
  -d 'grant_type=password&username=admin&password=password' | jq -r '.access_token')

curl -H "Authorization: Bearer $TOKEN" http://localhost:8090/users
```

| Endpoint | Method | Required Authority | Description |
| --- | --- | --- | --- |
| `/users` | GET | `PERMISSION_USER_READ` | List all users. |
| `/users/{id}` | GET | `PERMISSION_USER_READ` | Fetch a single user. |
| `/users` | POST | `PERMISSION_USER_CREATE` | Create a user (payload `UserDto`). |
| `/users/{id}` | PUT | `PERMISSION_USER_UPDATE` | Update user fields and role assignments. |
| `/users/{id}` | DELETE | `PERMISSION_USER_DELETE` | Remove a user. |
| `/admin/**` | any | `ROLE_ADMIN` (ResourceServer rule) | Extra admin endpoints (if added) are role-gated before permission checks. |

Method security is declared via `@PreAuthorize` in `UserController`, and the authorities come from the union of roles + permissions resolved in `UserServiceImpl`.

## Roles & Permissions
Roles aggregate permissions through the join tables defined in `schema.sql`. Seed data ships with one role, but the code is ready for more.

| Role (`RoleType`) | Description | Seeded Permissions |
| --- | --- | --- |
| `ADMIN` | Super user for bootstrapping | `USER_READ`, `USER_CREATE`, `USER_UPDATE`, `USER_DELETE` |

| Permission (`PermissionType`) | Purpose | Typical Usage |
| --- | --- | --- |
| `USER_READ` | View user directory | GET `/users`, GET `/users/{id}` |
| `USER_CREATE` | Onboard a new user | POST `/users` |
| `USER_UPDATE` | Change profile, reset password | PUT `/users/{id}` |
| `USER_DELETE` | Remove an account | DELETE `/users/{id}` |

### How Authorities Are Built
1. `UserServiceImpl#loadUserByUsername` fetches the `TUser` + eager roles.
2. Each `TRole` contributes `ROLE_<NAME>`.
3. Each `TPermission` contributes `PERMISSION_<NAME>`.
4. The combined authorities feed Spring Security’s decision manager for both URL and method security.

### Adding a New Permission
1. Extend `PermissionType` and add a record to `T_PERMISSIONS` (schema/data or runtime via API).
2. Assign it to a role in `T_ROLE_PERMISSIONS`.
3. Use `@PreAuthorize("hasAuthority('PERMISSION_NEW')")` on the controller method.

## Testing & Verification
```bash
mvn test
```
Integration tests under `src/test/java/com/star/sud/user/controller` demonstrate the secured controller using Spring’s test slices.

## Troubleshooting
- **401 Unauthorized**: confirm the token is valid and the `Authorization` header is present.
- **403 Forbidden**: user lacks the permission attached to the endpoint; inspect role ↔ permission mappings.
- **H2 console fails**: make sure JDBC URL matches `jdbc:h2:mem:research` and that the app is running.
- **JWT signature issues**: ensure both Authorization and Resource Servers share the same signing key (`as466gf`).

## Next Steps
- Add more granular permissions per domain entity.
- Swap H2 with PostgreSQL/MySQL by editing `application.properties`.
- Externalize the OAuth client details to a persistent store if you need multi-tenant clients.
