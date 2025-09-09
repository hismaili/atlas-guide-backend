# --- Terraform Configuration ---
# Specify required providers and their versions.
terraform {
  required_providers {
    oci = {
      source  = "oracle/oci"
      version = "7.16.0" # Use a recent, stable version of the OCI provider
    }
  }
  # It's good practice to pin the Terraform CLI version as well
  # required_version = ">= 1.0"
}

# --- Provider Configuration ---
# Configure the OCI provider. Authentication is typically handled via
# OCI CLI configuration (~/.oci/config) or environment variables.
# For GitHub Actions, we will use OCI IAM Service Principals.
provider "oci" {
  auth = "SecurityToken"
  tenancy_ocid        = var.tenancy_ocid
  region = var.oci_region
  config_file_profile = "Default"
#  private_key_password = var.private_key_password
}

# --- Networking Resources ---
# These resources establish the basic network infrastructure in OCI.

# Virtual Cloud Network (VCN) - The foundational network
#resource "oci_core_virtual_network" "vcn" {
#  cidr_block     = "10.0.0.0/16"
#  compartment_id = var.compartment_ocid
#  display_name   = "tf-free-tier-arm-vcn"
#  dns_label      = "tfarmvcn" # Required for hostnames to resolve within the VCN
#}

# Internet Gateway (IGW) - Allows traffic to/from the internet
#resource "oci_core_internet_gateway" "igw" {
#  compartment_id = var.compartment_ocid
#  display_name   = "tf-free-tier-arm-igw"
#  enabled        = true # Must be enabled to route traffic
#  vcn_id         = "ocid1.vcn.oc1.eu-amsterdam-1.amaaaaaaikdkjiqakct47ueltz3h7agt2cdpczasuml7xbzfjdnkn23jlmfa"
#}

# Route Table - Directs traffic within the VCN
resource "oci_core_route_table" "public_route_table" {
  compartment_id = var.compartment_ocid
  vcn_id         = "ocid1.vcn.oc1.eu-amsterdam-1.amaaaaaaikdkjiqa3n425os3rq6ksjfl3ieodlxx5cdexhpjvs2oabd5hmxa"
  display_name   = "tf-free-tier-arm-public-rt"

  route_rules {
    network_entity_id = "ocid1.vcn.oc1.eu-amsterdam-1.amaaaaaaikdkjiqa3n425os3rq6ksjfl3ieodlxx5cdexhpjvs2oabd5hmxa" # Route traffic via the IGW
    destination       = "0.0.0.0/0" # For all IP addresses
    cidr_block        = "0.0.0.0/0" # This argument is required by the provider
  }
}

# Data source to fetch default security list and DHCP options from the created VCN
# This avoids needing to manage these separately if defaults are acceptable.
data "oci_core_vcn" "selected_vcn" {
  vcn_id = "ocid1.vcn.oc1.eu-amsterdam-1.amaaaaaaikdkjiqa3n425os3rq6ksjfl3ieodlxx5cdexhpjvs2oabd5hmxa"
}

# Public Subnet - Where the compute instance will reside
resource "oci_core_subnet" "public_subnet" {
  compartment_id           = var.compartment_ocid
  vcn_id                   = "ocid1.vcn.oc1.eu-amsterdam-1.amaaaaaaikdkjiqa3n425os3rq6ksjfl3ieodlxx5cdexhpjvs2oabd5hmxa"
  cidr_block               = "10.0.1.0/24"
  display_name             = "tf-free-tier-arm-public-subnet"
  route_table_id           = oci_core_route_table.public_route_table.id # Associate with our public route table
  security_list_ids        = [data.oci_core_vcn.selected_vcn.default_security_list_id] # Use the VCN's default security list
  dhcp_options_id          = data.oci_core_vcn.selected_vcn.default_dhcp_options_id # Use the VCN's default DHCP options
  dns_label                = "tfarmsubnet"
  prohibit_internet_ingress = false # Allows ingress from the internet (needed for SSH, Certbot, services)
}

# --- Compute Instance ---
# Provisioning the ARM compute instance with the specified configuration.

resource "oci_core_instance" "arm_compute_instance" {
  # Instance lifecycle and availability configuration
  availability_config {
    is_live_migration_preferred = true # Prefer live migration for minimal downtime
  }

  compartment_id = var.compartment_ocid
  shape          = "VM.Standard.A1.Flex" # ARM-based shape for free tier
  shape_config {
    ocpus       = 2 # Allocate 2 OCPUs (Max 4 available in free tier)
    memory_in_gbs = 8 # Allocate 8 GB of memory (Max 24 GB available in free tier)
  }
  display_name = "free-tier-arm-compute-app"

  # Virtual Network Interface Card (VNIC) details
  create_vnic_details {
    subnet_id        = oci_core_subnet.public_subnet.id # Attach to our public subnet
    assign_public_ip = true # Assign a public IP for external access
    display_name     = "primaryVNIC"
    hostname_label   = "freecompute" # Hostname label for DNS resolution within VCN
    # For more granular network control, consider adding Network Security Groups (NSGs):
    # nsg_ids = [oci_core_network_security_group.my_nsg.id]
  }

  # Source details: Specify the ARM-compatible OS image OCID.
  # THIS MUST BE A VALID OCID FOR YOUR TARGET REGION AND ARM ARCHITECTURE.
  source_details {
    source_type = "image"
    #image_id    = var.arm_instance_image_ocid # Use the variable for the specific image OCID
  }

  # SSH key for secure access to the instance
  #ssh_authorized_keys = [var.ssh_public_key]

  # User data script for bootstrapping: runs once on instance creation.
  # Installs Podman, Podman-Compose, Git, and sets up directories.
  metadata = {
    user_data = base64encode(<<-EOF
      #!/bin/bash
      # Redirect stdout/stderr to a log file and the console for debugging.
      exec > >(tee /var/log/user-data.log|logger -t user-data -s 2>/dev/console) 2>&1
      echo "--- Starting instance bootstrap script ---"

      # --- System Updates and Package Installation ---
      echo "Updating packages and installing Podman, Podman-Compose, and Git..."
      # For Oracle Linux 8/9, 'yum' is appropriate. For newer Fedora-based systems, use 'dnf'.
      sudo yum update -y
      sudo yum install -y podman podman-compose git

      echo "Podman, Podman-Compose, and Git installed successfully."

      # --- Podman Systemd Socket Configuration (Optional) ---
      # Enabling the socket allows remote management of Podman.
      # echo "Enabling and starting Podman socket..."
      # sudo systemctl enable podman.socket
      # sudo systemctl start podman.socket
      # echo "Podman socket enabled and started."

      # --- Prepare directories for application deployment ---
      echo "Creating directories for application and Docker Compose configuration..."
      sudo mkdir -p /home/opc/app/docker-compose
      sudo chown opc:opc /home/opc/app/docker-compose # Ensure opc user owns the directories

      # --- Docker Compose Placeholder ---
      # This placeholder needs to be manually edited on the instance later
      # to include your actual service configurations (Vault, Keycloak, Java App)
      # and critically, the TLS certificate mount points and configurations.
      cat << 'EOL' > /home/opc/app/docker-compose/docker-compose.yml
      version: '3.8'

      services:
        # Placeholder for HashiCorp Vault. Requires specific configuration,
        # storage volumes, and TLS setup. Not included by default for simplicity.
        # vault:
        #   image: hashicorp/vault:latest
        #   container_name: vault
        #   ports:
        #     - "8200:8200"
        #   volumes:
        #     - vault-data:/vault/data # Persistent data volume
        #   cap_add:
        #     - IPC_LOCK # Required for Vault memory locking

        # Placeholder for Keycloak. Requires specific database configuration
        # and TLS setup for production.
        # keycloak:
        #   image: quay.io/keycloak/keycloak:latest
        #   container_name: keycloak
        #   ports:
        #     - "8080:8080" # Or map to a TLS-enabled port if configured
        #   environment:
        #     # Example: KC_HOSTNAME=your.domain.com, KC_HTTPS_ENABLED=true
        #     # Also needs KC_DB_URL, etc. for production.

        # Placeholder for your Java Application
        # REPLACE with your actual Java application image or build context.
        # You'll also need to configure volumes for your .jar file and ports.
        java-app:
          image: alpine:latest # Replace with your actual image name later
          container_name: java-app
          ports:
            - "8081:8080" # Map to your Java app's internal listening port
          # Example for running a JAR file directly:
          # volumes:
          #   - /home/opc/app/your-app.jar:/app/your-app.jar
          # command: ["java", "-jar", "/app/atlas-guide-backend.jar"]
          command: ["sleep", "infinity"] # Default command to keep container running as placeholder

      #volumes:
        # vault-data: # Define if uncommenting Vault service
      EOL

      echo "Placeholder docker-compose.yml created at /home/opc/app/docker-compose/docker-compose.yml"
      echo "NEXT STEPS: SSH into the instance, edit this docker-compose.yml, transfer your app files,"
      echo "and run 'podman-compose up -d' in /home/opc/app/docker-compose."
      echo "Remember to configure TLS certificates for public access."

      echo "--- Bootstrap script finished successfully ---"
    EOF
    )
  }

  # Explicitly define dependencies to ensure correct creation order.
  depends_on = [
    oci_core_subnet.public_subnet,
    oci_core_route_table.public_route_table,
    #data.oci_core_internet_gateway.selected_igw,
    data.oci_core_vcn.selected_vcn, # Data sources are usually evaluated first, but explicit dependency is good practice.
    #oci_core_virtual_network.vcn,
  ]
  availability_domain = "ocid1.domain.oc1..aaaaaaaai466hif4rdv6lbwlpsy373v54caohg3sarpbfworhauvpweillha"
}

# --- Outputs ---
# Provide useful information after the instance is provisioned.

output "instance_ocid" {
  description = "The OCID of the ARM compute instance."
  value       = oci_core_instance.arm_compute_instance.id
}

output "instance_public_ip" {
  description = "The public IP address of the ARM compute instance. Use this to SSH and access services."
  value       = oci_core_instance.arm_compute_instance.public_ip
}

output "instance_private_ip" {
  description = "The private IP address of the ARM compute instance."
  value       = oci_core_instance.arm_compute_instance.private_ip
}

output "ssh_command" {
  description = "Command to SSH into the instance. Replace <YOUR_PRIVATE_KEY_PATH> with the actual path to your private SSH key (e.g., ~/.ssh/id_rsa)."
  value       = "ssh -i ${var.private_key_path} opc@${oci_core_instance.arm_compute_instance.public_ip}"
}

#data "oci_identity_availability_domains" "ads" { compartment_id = var.compartment_ocid }