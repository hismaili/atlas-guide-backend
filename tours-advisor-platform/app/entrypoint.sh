#!/bin/bash
set -e

# Wait for Vault to be unsealed (Status 200)
echo "Checking Vault health at http://vault:8200/v1/sys/health..."
until [ "$(curl -s -o /dev/null -w "%{http_code}" http://vault:8200/v1/sys/health)" == "200" ]; do
  echo "Vault is sealed, initializing, or unreachable. Waiting 5s..."
  sleep 5
done

echo "Vault is unsealed. Launching Spring Boot..."
exec "$@"