variable "region" {
  description = "AWS region to deploy into"
  type        = string
  default     = "us-east-1"
}

variable "instance_type" {
  description = "EC2 instance type. t3.small is tight for Spring Boot; t3.medium (4 GB) is a safer baseline."
  type        = string
  default     = "t3.medium"
}

variable "image" {
  description = "Backend container image. Must be publicly pullable (or user_data needs a docker login step)."
  type        = string
  default     = "ghcr.io/ssunduko/commission-calculator:latest"
}

variable "ui_image" {
  description = "Next.js UI container image. Must be publicly pullable."
  type        = string
  default     = "ghcr.io/ssunduko/commission-calculator-ui:latest"
}

variable "allowed_ssh_cidr" {
  description = "CIDR range allowed to SSH in. Tighten to your IP for any non-throwaway deploy."
  type        = string
  default     = "0.0.0.0/0"
}

variable "key_pair_name" {
  description = "Name of an existing EC2 key pair in the chosen region"
  type        = string
}

variable "anthropic_api_key" {
  description = "Anthropic API key passed as ANTHROPIC_API_KEY env var to the container"
  type        = string
  sensitive   = true
}
