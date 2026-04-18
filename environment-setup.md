# install podman
brew install podman

# Install Podman-compose
brew install podman-compose

# Create tours-network
podman network create tours-network
podman network ls

NETWORK ID    NAME           DRIVER
------------------------------------
2f259bab93aa  podman         bridge
1c5c417a645a  tours-network  bridge

# Launch the db, keycloak and hvault container
podman compose up
# Appendixes

## Note : Network management

podman network
Manage networks

Description:
Manage networks

Usage:
podman network [command]

Available Commands:
connect     Add container to a network
create      Create networks for containers and pods
disconnect  Disconnect a container from a network
exists      Check if network exists
inspect     Inspect network
ls          List networks
prune       Prune unused networks
rm          Remove networks
update      Update an existing podman network