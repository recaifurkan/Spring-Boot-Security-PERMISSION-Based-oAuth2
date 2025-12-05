# Spring Authorization & Resource Server Demo

A dual Spring Boot 3.3 project that demonstrates how to issue and validate OAuth 2.1 access tokens with fine-grained, scope-based permissions. The repo contains:

| Module | Port | Purpose |
| --- | --- | --- |
| `auth-server` | 9000 | Spring Authorization Server issuing JWTs via client credentials and the custom password grant. |
| `resource-server` | 8081 | REST API exposing `/products`, protected with `SCOPE_product.read`. |

A helper script (`request.sh`) mimics the full password-grant flow from the CLI.

## Requirements
- JDK 17+
- Maven 3.9+
- `curl` and `jq` (used by `request.sh`)

## Run the services
Run each Spring Boot app from separate terminals so both ports stay alive.

```bash
# Terminal 1
cd auth-server
mvn spring-boot:run
```

```bash
# Terminal 2
cd resource-server
mvn spring-boot:run
```

## Token + API flow
Client and user fixtures are in-memory:
- OAuth client: `my-client` / `my-secret`
- Resource owner: `ahmet` / `12345`
- Required scope: `product.read`

`request.sh` automates the password grant followed by the `/products` call.

```bash
bash request.sh
```

Manual equivalent:

```bash
TOKEN=$(curl -s -X POST http://localhost:9000/oauth2/token \
  -u my-client:my-secret \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "grant_type=password" \
  -d "username=ahmet" \
  -d "password=12345" \
  -d "scope=product.read" | jq -r '.access_token')

curl -H "Authorization: Bearer $TOKEN" http://localhost:8081/products
```

If the scope is missing or insufficient, the resource server responds with `403 Forbidden` and the `WWW-Authenticate` header explains the missing privilege (`error="insufficient_scope"`).

## Integration testing
A lightweight RestClient-based integration test mimics the same two-step flow without invoking curl:
- `resource-server/src/test/java/com/example/resourceserver/ProductFlowIT`

Run it (services must already be running):

```bash
mvn -pl resource-server -Dtest=ProductFlowIT test
```

## Troubleshooting
- **401 Unauthorized (token call)** – confirm client credentials and username/password; the password grant only works for registered clients.
- **403 Forbidden (resource call)** – include the `product.read` scope when requesting the token; the resource server checks `SCOPE_product.read`.
- **Port conflicts** – override ports via `SERVER_PORT` env var when starting each app.
- **JWKS fetch failures** – ensure the authorization server is reachable at `http://localhost:9000/oauth2/jwks`, or override `AUTH_JWKS_URI` for the resource server.

## Next steps
1. Add more scopes (`product.write`, etc.) and guard extra endpoints in `ProductController`.
2. Externalize clients/users to a database instead of in-memory stores.
