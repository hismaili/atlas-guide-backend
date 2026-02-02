# Grant full access to everything
path "*" {
  capabilities = ["create", "read", "update", "delete", "list", "sudo"]
}

# Explicitly allow system paths for discovery
path "sys/*" {
  capabilities = ["create", "read", "update", "delete", "list", "sudo"]
}

# Explicitly allow auth paths for token management
path "auth/*" {
  capabilities = ["create", "read", "update", "delete", "list", "sudo"]
}