# Production-Grade HashiCorp Vault with OCI Instance Principal Authentication

## Architecture Overview

```
┌─────────────────────────────────────────────────────────────┐
│                    OCI Compute Instance                      │
│                                                              │
│  ┌──────────────────┐         ┌─────────────────────────┐  │
│  │   Vault Agent    │────────→│   Spring Boot App       │  │
│  │   (Sidecar)      │         │   (tours-backend)       │  │
│  │                  │         │                         │  │
│  │ - Authenticates  │         │ - Reads config files    │  │
│  │ - Renews tokens  │         │ - No Vault deps         │  │
│  │ - Templates      │         │ - Standard Spring Boot  │  │
│  │   secrets        │         │                         │  │
│  └────────┬─────────┘         └─────────────────────────┘  │
│           │                              ↑                  │
│           │ OCI Instance                 │ Reads files      │
│           │ Principal Auth               │                  │
│           ↓                              │                  │
│  ┌──────────────────┐         ┌─────────┴─────────┐        │
│  │ Metadata Service │         │  Shared Volume    │        │
│  │ 169.254.169.254  │         │  /vault/secrets   │        │
│  └──────────────────┘         └───────────────────┘        │
└─────────────────────────────────────────────────────────────┘
                      │
                      ↓
              ┌───────────────┐
              │ Vault Server  │
              │  (Container)  │
              └───────────────┘
```

## Prerequisites

1. OCI Compute Instance running
2. Podman/Docker installed on the instance
3. HashiCorp Vault server running (containerized or external)
4. OCI tenancy OCID and compartment OCID
5. Root/sudo access on the compute instance

---

## Part 1: OCI Configuration

### Step 1.1: Create Dynamic Group

In OCI Console → Identity & Security → Dynamic Groups:

**Name:** `tours-backend-dynamic-group`

**Matching Rules:**
```
instance.compartment.id = 'ocid1.compartment.oc1..your-compartment-ocid'
```

Or for specific instance:
```
instance.id = 'ocid1.instance.oc1.your-region.your-instance-ocid'
```

**Get the Dynamic Group OCID** (save this for later):
```
DYNAMIC_GROUP_OCID=ocid1.dynamicgroup.oc1..aaaaaa...
```

### Step 1.2: Create IAM Policy

In OCI Console → Identity & Security → Policies:

**Name:** `tours-backend-vault-policy`

**Compartment:** Root compartment (for tenancy-wide access)

**Policy Statements:**
```
Allow dynamic-group tours-backend-dynamic-group to read instances in compartment id ocid1.compartment.oc1..your-compartment-ocid
Allow dynamic-group tours-backend-dynamic-group to read instance-family in compartment id ocid1.compartment.oc1..your-compartment-ocid
Allow dynamic-group tours-backend-dynamic-group to inspect instances in compartment id ocid1.compartment.oc1..your-compartment-ocid
Allow dynamic-group tours-backend-dynamic-group to read compute-management-family in compartment id ocid1.compartment.oc1..your-compartment-ocid
Allow dynamic-group tours-backend-dynamic-group to read dynamic-groups in tenancy
```

### Step 1.3: Verify Instance Membership

SSH into your OCI instance and verify it can access the metadata service:

```bash
# Test metadata service access
curl -s http://169.254.169.254/opc/v2/instance/ | jq .

# Should return instance metadata including:
# - id
# - displayName
# - compartmentId
# - region
```

---

## Part 2: Vault Server Configuration

### Step 2.1: Set Environment Variables

On your Vault server host:

```bash
export VAULT_ADDR="http://localhost:8200"
export VAULT_TOKEN="your-root-token"
export TENANCY_OCID="ocid1.tenancy.oc1..aaaaaa..."
export DYNAMIC_GROUP_OCID="ocid1.dynamicgroup.oc1..aaaaaa..."
```

### Step 2.2: Enable and Configure OCI Auth Method

```bash
# Enable OCI auth method
podman exec -e VAULT_ADDR=$VAULT_ADDR -e VAULT_TOKEN=$VAULT_TOKEN tours-advisor-vault \
    vault auth enable oci

# Configure OCI auth with your tenancy
podman exec -e VAULT_ADDR=$VAULT_ADDR -e VAULT_TOKEN=$VAULT_TOKEN tours-advisor-vault \
    vault write auth/oci/config \
    home_tenancy_id=$TENANCY_OCID

# Verify configuration
podman exec -e VAULT_ADDR=$VAULT_ADDR -e VAULT_TOKEN=$VAULT_TOKEN tours-advisor-vault \
    vault read auth/oci/config
```

### Step 2.3: Create Vault Policy

Create a policy file `tours-app-policy.hcl`:

```hcl
# Read-only access to application secrets
path "secret/data/tours-app/*" {
  capabilities = ["read", "list"]
}

# Read database credentials
path "database/creds/tours-db-role" {
  capabilities = ["read"]
}

# Renew own token
path "auth/token/renew-self" {
  capabilities = ["update"]
}

# Lookup own token
path "auth/token/lookup-self" {
  capabilities = ["read"]
}
```

Apply the policy:

```bash
podman exec -i -e VAULT_ADDR=$VAULT_ADDR -e VAULT_TOKEN=$VAULT_TOKEN tours-advisor-vault \
    vault policy write tours-app - < tours-app-policy.hcl

# Verify policy
podman exec -e VAULT_ADDR=$VAULT_ADDR -e VAULT_TOKEN=$VAULT_TOKEN tours-advisor-vault \
    vault policy read tours-app
```

### Step 2.4: Create OCI Auth Role

```bash
# Create role mapping dynamic group to policy
podman exec -e VAULT_ADDR=$VAULT_ADDR -e VAULT_TOKEN=$VAULT_TOKEN tours-advisor-vault \
    vault write auth/oci/role/tours-backend-oci-role \
    auth_type="instance" \
    bound_dynamic_group_id=$DYNAMIC_GROUP_OCID \
    token_policies="tours-app" \
    token_ttl="1h" \
    token_max_ttl="24h" \
    token_period="0" \
    token_bound_cidrs="" \
    token_no_default_policy=false

# Verify role
podman exec -e VAULT_ADDR=$VAULT_ADDR -e VAULT_TOKEN=$VAULT_TOKEN tours-advisor-vault \
    vault read auth/oci/role/tours-backend-oci-role
```

### Step 2.5: Store Application Secrets

```bash
# Enable KV secrets engine (if not already enabled)
podman exec -e VAULT_ADDR=$VAULT_ADDR -e VAULT_TOKEN=$VAULT_TOKEN tours-advisor-vault \
    vault secrets enable -path=secret kv-v2

# Store database credentials
podman exec -e VAULT_ADDR=$VAULT_ADDR -e VAULT_TOKEN=$VAULT_TOKEN tours-advisor-vault \
    vault kv put secret/tours-app/database \
    host="postgres.example.com" \
    port="5432" \
    database="tours_db" \
    username="tours_user" \
    password="your-secure-password"

# Store API keys
podman exec -e VAULT_ADDR=$VAULT_ADDR -e VAULT_TOKEN=$VAULT_TOKEN tours-advisor-vault \
    vault kv put secret/tours-app/api-keys \
    maps_api_key="AIza..." \
    payment_api_key="pk_live_..."

# Store application configuration
podman exec -e VAULT_ADDR=$VAULT_ADDR -e VAULT_TOKEN=$VAULT_TOKEN tours-advisor-vault \
    vault kv put secret/tours-app/config \
    jwt_secret="your-jwt-secret" \
    encryption_key="your-encryption-key" \
    environment="production"

# Verify secrets
podman exec -e VAULT_ADDR=$VAULT_ADDR -e VAULT_TOKEN=$VAULT_TOKEN tours-advisor-vault \
    vault kv get secret/tours-app/database
```

---

## Part 3: Vault Agent Configuration

### Step 3.1: Create Directory Structure

On your OCI compute instance:

```bash
# Create directory structure
sudo mkdir -p /opt/vault-agent/{config,templates,secrets}
sudo chmod 755 /opt/vault-agent
sudo chmod 755 /opt/vault-agent/config
sudo chmod 755 /opt/vault-agent/templates
sudo chmod 700 /opt/vault-agent/secrets  # Restrict secrets directory
```

### Step 3.2: Create Vault Agent Config

Create `/opt/vault-agent/config/agent-config.hcl`:

```hcl
# Vault Agent Configuration
pid_file = "/tmp/vault-agent.pid"

vault {
  address = "http://vault-server-host:8200"  # Update with your Vault address
  retry {
    num_retries = 5
  }
}

# Auto-auth using OCI Instance Principal
auto_auth {
  method {
    type = "oci"
    
    config = {
      role = "tours-backend-oci-role"
      type = "instance"
    }
  }

  sink {
    type = "file"
    config = {
      path = "/vault/secrets/.vault-token"
      mode = 0640
    }
  }
}

# Template for application.properties
template {
  source      = "/vault/templates/application.properties.tpl"
  destination = "/vault/secrets/application.properties"
  command     = "echo '[Vault Agent] application.properties updated at $(date)' >> /vault/secrets/agent.log"
  error_on_missing_key = true
  backup      = true
}

# Template for database configuration (separate file for sensitive data)
template {
  source      = "/vault/templates/database.properties.tpl"
  destination = "/vault/secrets/database.properties"
  command     = "echo '[Vault Agent] database.properties updated at $(date)' >> /vault/secrets/agent.log"
  error_on_missing_key = true
  backup      = true
}

# Template for API keys
template {
  source      = "/vault/templates/api-keys.properties.tpl"
  destination = "/vault/secrets/api-keys.properties"
  command     = "echo '[Vault Agent] api-keys.properties updated at $(date)' >> /vault/secrets/agent.log"
  error_on_missing_key = true
  backup      = true
}

# Template config
template_config {
  static_secret_render_interval = "5m"
  exit_on_retry_failure = false
  max_connections_per_host = 10
}

# Logging
log_level = "info"
log_file = "/vault/secrets/vault-agent.log"
```

### Step 3.3: Create Secret Templates

**File: `/opt/vault-agent/templates/application.properties.tpl`**

```properties
# Application Configuration - Generated by Vault Agent
# Last updated: {{ timestamp }}

# Application settings
{{ with secret "secret/data/tours-app/config" }}
app.jwt.secret={{ .Data.data.jwt_secret }}
app.encryption.key={{ .Data.data.encryption_key }}
app.environment={{ .Data.data.environment }}
{{ end }}

# Server configuration
server.port=8080
server.servlet.context-path=/api

# Logging
logging.level.root=INFO
logging.level.com.tours=DEBUG

# Spring profile
spring.profiles.active=production
```

**File: `/opt/vault-agent/templates/database.properties.tpl`**

```properties
# Database Configuration - Generated by Vault Agent
# Last updated: {{ timestamp }}

{{ with secret "secret/data/tours-app/database" }}
spring.datasource.url=jdbc:postgresql://{{ .Data.data.host }}:{{ .Data.data.port }}/{{ .Data.data.database }}
spring.datasource.username={{ .Data.data.username }}
spring.datasource.password={{ .Data.data.password }}
{{ end }}

# Connection pool settings
spring.datasource.hikari.maximum-pool-size=10
spring.datasource.hikari.minimum-idle=5
spring.datasource.hikari.connection-timeout=30000
spring.datasource.hikari.idle-timeout=600000
spring.datasource.hikari.max-lifetime=1800000

# JPA/Hibernate
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.show-sql=false
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect
spring.jpa.properties.hibernate.format_sql=true
```

**File: `/opt/vault-agent/templates/api-keys.properties.tpl`**

```properties
# API Keys Configuration - Generated by Vault Agent
# Last updated: {{ timestamp }}

{{ with secret "secret/data/tours-app/api-keys" }}
integration.maps.api-key={{ .Data.data.maps_api_key }}
integration.payment.api-key={{ .Data.data.payment_api_key }}
{{ end }}
```

### Step 3.4: Set Permissions

```bash
# Set ownership (adjust user/group as needed)
sudo chown -R 1000:1000 /opt/vault-agent

# Set permissions
sudo chmod 644 /opt/vault-agent/config/agent-config.hcl
sudo chmod 644 /opt/vault-agent/templates/*.tpl
sudo chmod 700 /opt/vault-agent/secrets
```

---

## Part 4: Container Deployment with Podman

### Step 4.1: Create Podman Network

```bash
# Create a custom network for your containers
podman network create tours-network
```

### Step 4.2: Create Podman Volume

```bash
# Create a shared volume for secrets
podman volume create vault-secrets
```

### Step 4.3: Deploy Vault Agent Container

Create a deployment script `deploy-vault-agent.sh`:

```bash
#!/bin/bash

# Configuration
VAULT_ADDR="http://vault-server-host:8200"  # Update with your Vault address
CONTAINER_NAME="vault-agent"
IMAGE="hashicorp/vault:1.15"  # Use latest stable version

# Stop and remove existing container
podman stop $CONTAINER_NAME 2>/dev/null
podman rm $CONTAINER_NAME 2>/dev/null

# Run Vault Agent with host network (for metadata service access)
podman run -d \
    --name $CONTAINER_NAME \
    --network host \
    --restart unless-stopped \
    -v vault-secrets:/vault/secrets:Z \
    -v /opt/vault-agent/config:/vault/config:ro,Z \
    -v /opt/vault-agent/templates:/vault/templates:ro,Z \
    -e VAULT_ADDR=$VAULT_ADDR \
    $IMAGE \
    agent -config=/vault/config/agent-config.hcl

# Wait for agent to start
echo "Waiting for Vault Agent to initialize..."
sleep 5

# Check agent status
if podman ps | grep -q $CONTAINER_NAME; then
    echo "✓ Vault Agent started successfully"
    podman logs --tail 20 $CONTAINER_NAME
else
    echo "✗ Vault Agent failed to start"
    podman logs $CONTAINER_NAME
    exit 1
fi

# Verify secrets were generated
echo ""
echo "Checking generated secrets..."
podman exec $CONTAINER_NAME ls -la /vault/secrets/
```

Make it executable and run:

```bash
chmod +x deploy-vault-agent.sh
./deploy-vault-agent.sh
```

### Step 4.4: Verify Vault Agent

```bash
# Check logs
podman logs -f vault-agent

# Expected output:
# [INFO]  auth.handler: authenticating
# [INFO]  auth.handler: authentication successful
# [INFO]  template.server: template rendered: template=/vault/templates/application.properties.tpl
# [INFO]  template.server: template rendered: template=/vault/templates/database.properties.tpl

# Verify secrets were created
podman exec vault-agent ls -la /vault/secrets/

# Should show:
# .vault-token
# application.properties
# database.properties
# api-keys.properties
# vault-agent.log

# Check token is valid
podman exec vault-agent cat /vault/secrets/.vault-token
```

### Step 4.5: Deploy Spring Boot Application

Create a deployment script `deploy-spring-app.sh`:

```bash
#!/bin/bash

# Configuration
CONTAINER_NAME="tours-backend"
IMAGE="your-registry/tours-backend:latest"  # Update with your image
VAULT_SECRETS_VOLUME="vault-secrets"

# Stop and remove existing container
podman stop $CONTAINER_NAME 2>/dev/null
podman rm $CONTAINER_NAME 2>/dev/null

# Run Spring Boot application
podman run -d \
    --name $CONTAINER_NAME \
    --network tours-network \
    --restart unless-stopped \
    -p 8080:8080 \
    -v vault-secrets:/vault/secrets:ro,Z \
    -e SPRING_CONFIG_IMPORT="file:/vault/secrets/application.properties,file:/vault/secrets/database.properties,file:/vault/secrets/api-keys.properties" \
    -e JAVA_OPTS="-Xmx512m -Xms256m" \
    -e TZ="UTC" \
    --health-cmd="curl -f http://localhost:8080/actuator/health || exit 1" \
    --health-interval=30s \
    --health-timeout=10s \
    --health-retries=3 \
    $IMAGE

# Wait for application to start
echo "Waiting for Spring Boot application to start..."
sleep 10

# Check application status
if podman ps | grep -q $CONTAINER_NAME; then
    echo "✓ Spring Boot application started successfully"
    podman logs --tail 30 $CONTAINER_NAME
else
    echo "✗ Spring Boot application failed to start"
    podman logs $CONTAINER_NAME
    exit 1
fi

# Test health endpoint
echo ""
echo "Testing application health..."
curl -f http://localhost:8080/actuator/health || echo "Health check failed"
```

Make it executable and run:

```bash
chmod +x deploy-spring-app.sh
./deploy-spring-app.sh
```

---

## Part 5: Spring Boot Application Configuration

### Step 5.1: Update pom.xml (Maven)

**Remove** Spring Cloud Vault dependency (no longer needed):

```xml
<dependencies>
    <!-- Standard Spring Boot dependencies only -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-jpa</artifactId>
    </dependency>
    <!-- No Vault dependencies needed! -->
</dependencies>
```

### Step 5.2: Minimal Application Configuration

**File: `src/main/resources/application.yml`**

```yaml
# Minimal bootstrap configuration
# Vault Agent will provide all secrets via config files

spring:
  application:
    name: tours-backend
  
  # Config import from Vault Agent generated files
  config:
    import:
      - file:/vault/secrets/application.properties
      - file:/vault/secrets/database.properties
      - file:/vault/secrets/api-keys.properties

# Actuator for health checks
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics
  endpoint:
    health:
      show-details: when-authorized
```

### Step 5.3: Example Application Code

```java
@SpringBootApplication
public class ToursBackendApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(ToursBackendApplication.class, args);
    }
}

@RestController
@RequestMapping("/api")
public class HealthController {
    
    @Value("${app.environment}")
    private String environment;
    
    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of(
            "status", "UP",
            "environment", environment,
            "timestamp", Instant.now().toString()
        );
    }
}
```

---

## Part 6: Production Hardening

### Step 6.1: Enable TLS for Vault Communication

Update Vault Agent config to use HTTPS:

```hcl
vault {
  address = "https://vault.example.com:8200"
  
  # TLS configuration
  tls_skip_verify = false  # Set to true only for testing
  ca_cert = "/vault/tls/ca.crt"
  client_cert = "/vault/tls/client.crt"
  client_key = "/vault/tls/client.key"
}
```

### Step 6.2: Implement Secret Rotation Handler

Create a rotation script `/opt/vault-agent/scripts/handle-rotation.sh`:

```bash
#!/bin/bash

# Secret rotation handler
# This script is called when secrets change

SECRET_FILE=$1
LOG_FILE="/vault/secrets/rotation.log"

echo "[$(date)] Secret rotation detected: $SECRET_FILE" >> $LOG_FILE

# Example: Restart application gracefully
if [[ "$SECRET_FILE" == *"database.properties"* ]]; then
    echo "[$(date)] Database credentials changed - sending SIGHUP to application" >> $LOG_FILE
    # Send signal to application to reload DB connection pool
    podman exec tours-backend kill -HUP 1
fi

# Example: Reload specific component
if [[ "$SECRET_FILE" == *"api-keys.properties"* ]]; then
    echo "[$(date)] API keys changed - triggering component reload" >> $LOG_FILE
    # Call application endpoint to reload configuration
    curl -X POST http://localhost:8080/actuator/refresh
fi
```

Update template config to use the handler:

```hcl
template {
  source      = "/vault/templates/database.properties.tpl"
  destination = "/vault/secrets/database.properties"
  command     = "/vault/scripts/handle-rotation.sh database.properties"
  error_on_missing_key = true
  backup      = true
}
```

### Step 6.3: Monitoring and Alerting

Create a health check script `check-vault-agent.sh`:

```bash
#!/bin/bash

# Vault Agent health check

# Check if container is running
if ! podman ps | grep -q vault-agent; then
    echo "ERROR: Vault Agent container is not running"
    exit 1
fi

# Check if token file exists and is recent (< 2 hours old)
TOKEN_FILE="/var/lib/containers/storage/volumes/vault-secrets/_data/.vault-token"
if [ ! -f "$TOKEN_FILE" ]; then
    echo "ERROR: Vault token file does not exist"
    exit 1
fi

TOKEN_AGE=$(($(date +%s) - $(stat -c %Y "$TOKEN_FILE")))
if [ $TOKEN_AGE -gt 7200 ]; then
    echo "WARNING: Vault token file is older than 2 hours"
    exit 1
fi

# Check if secrets are being updated
SECRETS_DIR="/var/lib/containers/storage/volumes/vault-secrets/_data"
LATEST_UPDATE=$(find $SECRETS_DIR -name "*.properties" -type f -printf '%T@\n' | sort -n | tail -1)
CURRENT_TIME=$(date +%s)
TIME_DIFF=$((CURRENT_TIME - ${LATEST_UPDATE%.*}))

if [ $TIME_DIFF -gt 3600 ]; then
    echo "WARNING: Secrets have not been updated in over 1 hour"
    exit 1
fi

echo "OK: Vault Agent is healthy"
exit 0
```

Add to crontab for monitoring:

```bash
# Add to crontab
*/5 * * * * /opt/vault-agent/scripts/check-vault-agent.sh || systemctl restart vault-agent
```

### Step 6.4: Log Aggregation

Configure centralized logging:

```bash
# Configure podman to use json-file logging
podman run -d \
    --name vault-agent \
    --log-driver json-file \
    --log-opt max-size=10m \
    --log-opt max-file=3 \
    # ... rest of configuration
```

### Step 6.5: Backup and Disaster Recovery

Create backup script `backup-vault-config.sh`:

```bash
#!/bin/bash

BACKUP_DIR="/backup/vault-agent"
TIMESTAMP=$(date +%Y%m%d_%H%M%S)

# Create backup directory
mkdir -p $BACKUP_DIR

# Backup configuration
tar -czf $BACKUP_DIR/vault-agent-config-$TIMESTAMP.tar.gz \
    /opt/vault-agent/config \
    /opt/vault-agent/templates

# Backup secrets (encrypted)
tar -czf - /var/lib/containers/storage/volumes/vault-secrets/_data | \
    openssl enc -aes-256-cbc -salt -out $BACKUP_DIR/vault-secrets-$TIMESTAMP.tar.gz.enc

# Keep only last 7 days of backups
find $BACKUP_DIR -name "*.tar.gz*" -mtime +7 -delete

echo "Backup completed: $BACKUP_DIR/vault-agent-config-$TIMESTAMP.tar.gz"
```

---

## Part 7: Testing and Validation

### Step 7.1: Test Authentication Flow

```bash
# Test OCI metadata service access from Vault Agent
podman exec vault-agent curl -s http://169.254.169.254/opc/v2/instance/

# Check Vault Agent logs for successful authentication
podman logs vault-agent | grep "authentication successful"

# Verify token is valid
podman exec vault-agent cat /vault/secrets/.vault-token
```

### Step 7.2: Test Secret Retrieval

```bash
# Check if secrets were templated correctly
podman exec vault-agent cat /vault/secrets/application.properties
podman exec vault-agent cat /vault/secrets/database.properties

# Verify Spring Boot can read the secrets
podman exec tours-backend cat /vault/secrets/application.properties
```

### Step 7.3: Test Secret Rotation

```bash
# Update a secret in Vault
podman exec -e VAULT_ADDR=$VAULT_ADDR -e VAULT_TOKEN=$VAULT_TOKEN tours-advisor-vault \
    vault kv put secret/tours-app/database \
    host="postgres.example.com" \
    port="5432" \
    database="tours_db" \
    username="tours_user" \
    password="NEW-PASSWORD-123"

# Watch Vault Agent logs
podman logs -f vault-agent

# Should see:
# [INFO]  template.server: template rendered: template=/vault/templates/database.properties.tpl

# Verify the file was updated
podman exec vault-agent cat /vault/secrets/database.properties | grep password

# Check application picked up the change (if configured for hot reload)
podman logs tours-backend | tail -20
```

### Step 7.4: Test Failure Scenarios

**Scenario 1: Vault Server Down**

```bash
# Stop Vault server
podman stop tours-advisor-vault

# Verify Vault Agent continues running with cached secrets
podman exec vault-agent cat /vault/secrets/application.properties

# Verify Spring Boot continues running
curl http://localhost:8080/actuator/health

# Start Vault server again
podman start tours-advisor-vault

# Verify Vault Agent reconnects
podman logs vault-agent | grep "renewed auth token"
```

**Scenario 2: Network Issues**

```bash
# Simulate network partition (requires root)
sudo iptables -A OUTPUT -d VAULT_SERVER_IP -j DROP

# Verify application continues with cached secrets
curl http://localhost:8080/api/health

# Restore network
sudo iptables -D OUTPUT -d VAULT_SERVER_IP -j DROP
```

**Scenario 3: Token Expiration**

```bash
# Create a role with very short TTL for testing
vault write auth/oci/role/tours-backend-oci-role-test \
    auth_type="instance" \
    bound_dynamic_group_id=$DYNAMIC_GROUP_OCID \
    token_policies="tours-app" \
    token_ttl="2m" \
    token_max_ttl="5m"

# Watch Vault Agent automatically renew the token
podman logs -f vault-agent | grep "renewed auth token"
```

---

## Part 8: Systemd Integration (Production)

### Step 8.1: Create Systemd Service for Vault Agent

Create `/etc/systemd/system/vault-agent.service`:

```ini
[Unit]
Description=HashiCorp Vault Agent
Documentation=https://www.vaultproject.io/docs/agent
After=network-online.target
Wants=network-online.target

[Service]
Type=notify
User=root
Group=root
ExecStartPre=-/usr/bin/podman stop vault-agent
ExecStartPre=-/usr/bin/podman rm vault-agent
ExecStart=/usr/bin/podman run \
    --name vault-agent \
    --network host \
    --sdnotify=conmon \
    -v vault-secrets:/vault/secrets:Z \
    -v /opt/vault-agent/config:/vault/config:ro,Z \
    -v /opt/vault-agent/templates:/vault/templates:ro,Z \
    -e VAULT_ADDR=http://vault-server:8200 \
    hashicorp/vault:1.15 \
    agent -config=/vault/config/agent-config.hcl

ExecStop=/usr/bin/podman stop -t 10 vault-agent
Restart=always
RestartSec=10
TimeoutStartSec=120
TimeoutStopSec=30

[Install]
WantedBy=multi-user.target
```

### Step 8.2: Create Systemd Service for Spring Boot App

Create `/etc/systemd/system/tours-backend.service`:

```ini
[Unit]
Description=Tours Backend Spring Boot Application
After=vault-agent.service
Requires=vault-agent.service

[Service]
Type=notify
User=root
Group=root
ExecStartPre=/bin/sleep 10
ExecStartPre=-/usr/bin/podman stop tours-backend
ExecStartPre=-/usr/bin/podman rm tours-backend
ExecStart=/usr/bin/podman run \
    --name tours-backend \
    --network tours-network \
    --sdnotify=conmon \
    -p 8080:8080 \
    -v vault-secrets:/vault/secrets:ro,Z \
    -e SPRING_CONFIG_IMPORT="file:/vault/secrets/application.properties,file:/vault/secrets/database.properties,file:/vault/secrets/api-keys.properties" \
    your-registry/tours-backend:latest

ExecStop=/usr/bin/podman stop -t 30 tours-backend
Restart=always
RestartSec=10
TimeoutStartSec=180
TimeoutStopSec=60

[Install]
WantedBy=multi-user.target
```

### Step