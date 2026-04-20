# Tours Advisor - Setup Guide

## Quick Start

### 1. Prerequisites
- Podman and Podman Compose installed
- At least 4GB of available RAM

### 2. Initial Setup

**Create environment file:**
```bash
cp .env.example .env
```

**Generate secure passwords:**
```bash
# Generate passwords for your .env file
echo "POSTGRES_PASSWORD=$(openssl rand -base64 32)"
echo "KEYCLOAK_DB_PASSWORD=$(openssl rand -base64 32)"
echo "KEYCLOAK_ADMIN_PASSWORD=$(openssl rand -base64 32)"
echo "APP_DB_PASSWORD=$(openssl rand -base64 32)"
```

Update your `.env` file with these generated passwords.

> **Important:** Also update the hardcoded passwords in `tours-advisor-platform/infra/init-db.sql` to match the values you generated above.

**Add to .gitignore:**
```bash
echo ".env" >> .gitignore
```

### 3. Create the Podman Network

```bash
podman network create tours-network
```

### 4. Start Infrastructure Services

```bash
cd tours-advisor-platform/infra

# Start all services
podman compose up -d

# Check status
podman compose ps

# View logs
podman compose logs -f
```

### 5. Verify Services

**PostgreSQL:**
```bash
podman exec -it tours-advisor-db psql -U postgres -c "\l"
# Should show: tours_advisor_db and keycloak_db
```

**Vault:**
- Access: http://localhost:8200
- Vault starts sealed and must be initialized (see Vault Configuration section below)

**Keycloak:**
- Access: http://localhost:8080
- Admin Console: http://localhost:8080/admin
- Username: admin (or value from KEYCLOAK_ADMIN)
- Password: Value from KEYCLOAK_ADMIN_PASSWORD

## Vault Configuration

### Initialize and Unseal Vault

Vault starts sealed. You need to initialize it once, then unseal it each time the container restarts.

```bash
# Initialize Vault (only once — save the output securely!)
podman exec -e VAULT_ADDR='http://127.0.0.1:8200' tours-advisor-vault \
  vault operator init -key-shares=1 -key-threshold=1

# Save the Unseal Key and Root Token from the output
# NEVER commit these values to version control

# Unseal Vault (required after every container restart)
podman exec -e VAULT_ADDR='http://127.0.0.1:8200' tours-advisor-vault \
  vault operator unseal <YOUR_UNSEAL_KEY>
```

### Enable KV-v2 Secrets Engine

```bash
podman exec -e VAULT_ADDR='http://127.0.0.1:8200' -e VAULT_TOKEN='<YOUR_ROOT_TOKEN>' \
  tours-advisor-vault vault secrets enable -path=kv-tours kv-v2
```

### Store Application Secrets

```bash
# Store AI config secrets
podman exec -e VAULT_ADDR='http://127.0.0.1:8200' -e VAULT_TOKEN='<YOUR_ROOT_TOKEN>' \
  tours-advisor-vault vault kv put kv-tours/backend/local/ai-config \
  spring.ai.google.genai.api-key=<YOUR_GEMINI_API_KEY>

# Store database secrets
podman exec -e VAULT_ADDR='http://127.0.0.1:8200' -e VAULT_TOKEN='<YOUR_ROOT_TOKEN>' \
  tours-advisor-vault vault kv put kv-tours/backend/local/database \
  spring.datasource.username=app_user \
  spring.datasource.password=<YOUR_APP_DB_PASSWORD>
```

### Create Policy and AppRole Authentication

**Write the policy:**
```bash
cat ./tours-advisor-platform/infra/vault/policies/tours-app.hcl | \
  podman exec -i -e VAULT_ADDR='http://127.0.0.1:8200' -e VAULT_TOKEN='<YOUR_ROOT_TOKEN>' \
  tours-advisor-vault vault policy write tours-app -
```

**Enable AppRole auth method:**
```bash
podman exec -e VAULT_ADDR='http://127.0.0.1:8200' -e VAULT_TOKEN='<YOUR_ROOT_TOKEN>' \
  tours-advisor-vault vault auth enable approle
```

**Create the AppRole and attach the policy:**

> **Warning:** When attaching policies, make sure there are no extra/nested quotes around
> the policy name. The policy name must be `tours-app`, not `"tours-app"`.
> Double-quoting can cause silent 403 permission errors.

```bash
podman exec -e VAULT_ADDR='http://127.0.0.1:8200' -e VAULT_TOKEN='<YOUR_ROOT_TOKEN>' \
  tours-advisor-vault vault write auth/approle/role/tours-backend-role \
  token_policies="default,tours-app" \
  secret_id_ttl=0 \
  token_ttl=1h \
  token_max_ttl=4h
```

**Retrieve role-id and generate secret-id:**
```bash
# Get the role-id
podman exec -e VAULT_ADDR='http://127.0.0.1:8200' -e VAULT_TOKEN='<YOUR_ROOT_TOKEN>' \
  tours-advisor-vault vault read auth/approle/role/tours-backend-role/role-id

# Generate a secret-id
podman exec -e VAULT_ADDR='http://127.0.0.1:8200' -e VAULT_TOKEN='<YOUR_ROOT_TOKEN>' \
  tours-advisor-vault vault write -f auth/approle/role/tours-backend-role/secret-id
```

Save the `role_id` and `secret_id` values — you'll need them to run the Spring Boot app.

### Verify AppRole Access

```bash
# Login with AppRole
podman exec -e VAULT_ADDR='http://127.0.0.1:8200' tours-advisor-vault \
  vault write auth/approle/login role_id=<YOUR_ROLE_ID> secret_id=<YOUR_SECRET_ID>

# Use the returned token to test reading a secret
VAULT_TOKEN=<TOKEN_FROM_LOGIN> podman exec \
  -e VAULT_ADDR='http://127.0.0.1:8200' -e VAULT_TOKEN="$VAULT_TOKEN" \
  tours-advisor-vault vault kv get kv-tours/backend/local/database
```

## Keycloak Configuration

### Create Realm

1. Access Keycloak Admin Console: http://localhost:8080/admin
2. Login with admin credentials
3. Click dropdown "Master" → "Create Realm"
4. Name: `tours-advisor`
5. Click "Create"

### Create Client for React Native App

1. In `tours-advisor` realm, go to "Clients"
2. Click "Create client"
3. Configure:
    - **Client type**: OpenID Connect
    - **Client ID**: `tours-advisor-mobile`
    - Click "Next"
4. **Capability config**:
    - ✅ Standard flow (Authorization Code)
    - ✅ Direct access grants (for development/testing)
    - Click "Next"
5. **Login settings**:
    - **Valid redirect URIs**:
        - `myapp://oauth/callback` (for React Native)
        - `http://localhost:*` (for development)
    - **Web origins**: `*` (for CORS in development)
    - Click "Save"

### Create Client for Spring Boot Backend

1. Go to "Clients" → "Create client"
2. Configure:
    - **Client type**: OpenID Connect
    - **Client ID**: `tours-advisor-backend`
    - Click "Next"
3. **Capability config**:
    - ✅ Client authentication (for confidential client)
    - ✅ Service accounts roles
    - Click "Next"
4. Click "Save"
5. Go to "Credentials" tab
6. Copy the "Client Secret" (you'll need this for Spring Boot)

### Create Test User

1. Go to "Users" → "Add user"
2. Fill in:
    - **Username**: testuser
    - **Email**: testuser@example.com
    - **First name**: Test
    - **Last name**: User
3. Click "Create"
4. Go to "Credentials" tab
5. Click "Set password"
    - **Password**: *******
    - **Temporary**: OFF
6. Click "Save"

## Spring Boot Configuration

The application configuration is already defined in `src/main/resources/application.yml`. To run the app locally you need to set the following environment variables:

```bash
# Vault connection (from the AppRole setup above)
export VAULT_URI=http://localhost:8200
export VAULT_ROLE_ID=<YOUR_ROLE_ID>
export VAULT_SECRET_ID=<YOUR_SECRET_ID>

# Environment (determines which Vault path to read: kv-tours/backend/<environment>/*)
export environment=local

# Database (fallback if not provided by Vault)
export DB_URL=jdbc:postgresql://localhost:5432/tours_advisor_db
export APP_DB_USER=app_user
export APP_DB_PASSWORD=<YOUR_APP_DB_PASSWORD>
```

Then run:
```bash
mvn spring-boot:run
```

The app starts on port **8090** by default (override with `SERVER_PORT` env var).

## React Native Configuration

Install required packages:

```bash
npm install react-native-app-auth
# or
yarn add react-native-app-auth
```

Configure OAuth:

```javascript
import { authorize } from 'react-native-app-auth';

const config = {
  issuer: 'http://localhost:8080/realms/tours-advisor',
  clientId: 'tours-advisor-mobile',
  redirectUrl: 'myapp://oauth/callback',
  scopes: ['openid', 'profile', 'email'],
};

// Login
const authState = await authorize(config);
// authState.accessToken - use this for API calls
```

## Testing the Setup

### Test Keycloak Login

```bash
# Get access token
curl -X POST 'http://localhost:8080/realms/tours-advisor/protocol/openid-connect/token' \
  -H 'Content-Type: application/x-www-form-urlencoded' \
  -d 'username=testuser' \
  -d 'password=<password>' \
  -d 'grant_type=password' \
  -d 'client_id=tours-advisor-mobile'
```

### Test Protected Endpoint

```bash
# Use the access_token from previous response
curl -H "Authorization: Bearer <YOUR_ACCESS_TOKEN>" \
  http://localhost:8090/api/health
```

## Useful Commands

All compose commands should be run from `tours-advisor-platform/infra/`.

**Stop all services:**
```bash
podman compose down
```

**Stop and remove volumes (deletes data):**
```bash
podman compose down -v
```

**Restart specific service:**
```bash
podman compose restart keycloak
```

**Unseal Vault (required after container restart):**
```bash
podman exec -e VAULT_ADDR='http://127.0.0.1:8200' tours-advisor-vault \
  vault operator unseal <YOUR_UNSEAL_KEY>
```

**View logs:**
```bash
# All services
podman compose logs -f

# Specific service
podman compose logs -f keycloak
```

**Access database:**
```bash
podman exec -it tours-advisor-db psql -U postgres -d tours_advisor_db
```

**Backup database:**
```bash
podman exec tours-advisor-db pg_dump -U postgres tours_advisor_db > backup.sql
```

## Production Considerations

Before deploying to production:

1. **Remove port exposures** for database
2. **Enable HTTPS** on Keycloak
3. **Use managed database** services (RDS, Cloud SQL)
4. **Enable Vault in production mode** (not dev mode)
5. **Use proper secrets management**
6. **Set up SSL certificates**
7. **Configure rate limiting**
8. **Enable logging and monitoring**

## Additional Resources

- [Keycloak Documentation](https://www.keycloak.org/documentation)
- [Spring Security OAuth2](https://spring.io/guides/tutorials/spring-boot-oauth2/)
- [React Native App Auth](https://github.com/FormidableLabs/react-native-app-auth)
- [HashiCorp Vault](https://www.vaultproject.io/docs)

## Troubleshooting

**Keycloak won't start:**
- Check if database is ready: `podman compose logs db`
- Verify database credentials in `.env`

**Can't connect to database:**
- Ensure PostgreSQL is running: `podman compose ps`
- Check if port 5432 is available

**Keycloak admin console not accessible:**
- Wait 30-60 seconds for Keycloak to fully start
- Check logs: `podman compose logs keycloak`

**Vault 403 Permission Denied:**
- Verify the policy is attached to the AppRole: `vault read auth/approle/role/tours-backend-role`
- Check that `token_policies` shows `[default tours-app]` without extra quotes around the policy name
- If the policy name has literal double-quotes (e.g. `"tours-app"` instead of `tours-app`), re-attach it:
  ```bash
  vault write auth/approle/role/tours-backend-role token_policies="default,tours-app"
  ```
- Ensure the `environment` env var is set (e.g. `local`) — it determines the Vault path

**Vault is sealed after restart:**
- Vault with file storage must be unsealed after every container restart
- Run: `podman exec -e VAULT_ADDR='http://127.0.0.1:8200' tours-advisor-vault vault operator unseal <YOUR_UNSEAL_KEY>`

**React Native OAuth not working:**
- Verify redirect URI matches exactly in Keycloak client config
- Check that client ID is correct
- Ensure Keycloak is accessible from your device/emulator