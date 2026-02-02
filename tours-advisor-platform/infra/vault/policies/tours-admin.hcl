# Allow full CRUD on secrets
path "kv-tours/data/*" {
  capabilities = ["create", "read", "update", "delete", "list"]
}

# Allow metadata access (required for KV-v2 UI and history)
path "kv-tours/metadata/*" {
  capabilities = ["list", "read", "delete"]
}

# Allow the user to see the list of secrets engines in the UI
path "sys/mounts" {
  capabilities = ["read"]
}