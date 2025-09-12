variable "ssh_public_key" {
  description = "Public SSH key for accessing the instance."
  type        = string
  sensitive   = true
}

variable "compartment_ocid" {
  description = "OCI Compartment OCID where resources will be created."
  type        = string
}

variable "oci_region" {
  description = "OCI Region for deployment."
  type        = string
  default     = "us-ashburn-1"
}

variable "arm_instance_image_ocid" {
  description = "OCID of the ARM Linux image for the region (e.g., Oracle Linux 8 ARM)."
  type        = string
  # IMPORTANT: Replace this placeholder with your actual image OCID.
  # Example: "ocid1.image.oc1.iad.aaaaaaaagxxxxxxxxyyyyyyyyzzzzzzzzzzzz"
}

variable "user_ocid" {
  description = "User OCID where resources will be created."
  type        = string
}

variable "tenancy_ocid" {
  description = "Tenancy OCID where resources will be created."
  type        = string
}

variable "private_key_path" {
  description = "Path to the private key file for OCI API authentication."
  type        = string
}

variable "private_key_password" {
  description = "The password for the private key, if it is encrypted."
  type        = string
}