# 1. Allow Spring to validate its own session
path "auth/token/lookup-self" {
  capabilities = ["read"]
}

# 2. Allow Spring to read the secrets (Note the /data/ segment for KV-v2)
path "kv-tours/data/backend/dev/*" {
  capabilities = ["read", "list"]
}

# 3. Allow Spring to check engine version/metadata
path "kv-tours/metadata/backend/dev/*" {
  capabilities = ["read", "list"]
}