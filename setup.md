# Tours Advisor - Setup Guide

## 🚀 Quick Start

### 1. Prerequisites
- Docker and Docker Compose installed
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

**Add to .gitignore:**
```bash
echo ".env" >> .gitignore
```

### 3. Start Services

```bash
# Start all services
docker-compose up -d

# Check status
docker-compose ps

# View logs
docker-compose logs -f
```

### 4. Verify Services

**PostgreSQL:**
```bash
docker exec -it tours-advisor-db psql -U postgres -c "\l"
# Should show: tours_advisor_db and keycloak_db
```

**Vault:**
- Access: http://localhost:8200
- Token: Value from VAULT_ROOT_TOKEN in .env

**Keycloak:**
- Access: http://localhost:8080
- Admin Console: http://localhost:8080/admin
- Username: admin (or value from KEYCLOAK_ADMIN)
- Password: Value from KEYCLOAK_ADMIN_PASSWORD

## 🔐 Keycloak Configuration

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
    - **Password**: Test123!
    - **Temporary**: OFF
6. Click "Save"

## 🔧 Spring Boot Configuration

Add to your `application.yml`:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/tours_advisor_db
    username: app_user
    password: ${APP_DB_PASSWORD}
  
  security:
    oauth2:
      resourceserver:
        jwt:
          issuer-uri: http://localhost:8080/realms/tours-advisor
          jwk-set-uri: http://localhost:8080/realms/tours-advisor/protocol/openid-connect/certs
      
      client:
        registration:
          keycloak:
            client-id: tours-advisor-backend
            client-secret: YOUR_CLIENT_SECRET_FROM_KEYCLOAK
            authorization-grant-type: client_credentials
            scope: openid, profile, email
        provider:
          keycloak:
            issuer-uri: http://localhost:8080/realms/tours-advisor
```

Add Spring Security dependency to `pom.xml`:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-oauth2-resource-server</artifactId>
</dependency>
```

## 📱 React Native Configuration

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

## 🧪 Testing the Setup

### Test Keycloak Login

```bash
# Get access token
curl -X POST 'http://localhost:8080/realms/tours-advisor/protocol/openid-connect/token' \
  -H 'Content-Type: application/x-www-form-urlencoded' \
  -d 'username=testuser' \
  -d 'password=Test123!' \
  -d 'grant_type=password' \
  -d 'client_id=tours-advisor-mobile'
```

### Test Protected Endpoint

```bash
# Use the access_token from previous response
curl -H "Authorization: Bearer YOUR_ACCESS_TOKEN" \
  http://localhost:8081/api/protected-endpoint
```

## 🛠️ Useful Commands

**Stop all services:**
```bash
docker-compose down
```

**Stop and remove volumes (⚠️ deletes data):**
```bash
docker-compose down -v
```

**Restart specific service:**
```bash
docker-compose restart keycloak
```

**View logs:**
```bash
# All services
docker-compose logs -f

# Specific service
docker-compose logs -f keycloak
```

**Access database:**
```bash
docker exec -it tours-advisor-db psql -U postgres -d tours_advisor_db
```

**Backup database:**
```bash
docker exec tours-advisor-db pg_dump -U postgres tours_advisor_db > backup.sql
```

## 🔒 Production Considerations

Before deploying to production:

1. **Remove port exposures** for database
2. **Enable HTTPS** on Keycloak
3. **Use managed database** services (RDS, Cloud SQL)
4. **Enable Vault in production mode** (not dev mode)
5. **Use proper secrets management**
6. **Set up SSL certificates**
7. **Configure rate limiting**
8. **Enable logging and monitoring**

## 📚 Additional Resources

- [Keycloak Documentation](https://www.keycloak.org/documentation)
- [Spring Security OAuth2](https://spring.io/guides/tutorials/spring-boot-oauth2/)
- [React Native App Auth](https://github.com/FormidableLabs/react-native-app-auth)
- [HashiCorp Vault](https://www.vaultproject.io/docs)

## 🆘 Troubleshooting

**Keycloak won't start:**
- Check if database is ready: `docker-compose logs db`
- Verify database credentials in `.env`

**Can't connect to database:**
- Ensure PostgreSQL is running: `docker-compose ps`
- Check if port 5432 is available

**Keycloak admin console not accessible:**
- Wait 30-60 seconds for Keycloak to fully start
- Check logs: `docker-compose logs keycloak`

**React Native OAuth not working:**
- Verify redirect URI matches exactly in Keycloak client config
- Check that client ID is correct
- Ensure Keycloak is accessible from your device/emulator