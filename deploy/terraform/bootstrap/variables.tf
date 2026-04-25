variable "region" {
  type    = string
  default = "us-east-1"
}

variable "state_bucket_name" {
  description = "Globally unique S3 bucket name for terraform state. Suggested format: commission-calc-tfstate-<accountId>"
  type        = string
}

variable "lock_table_name" {
  description = "Must match dynamodb_table in ../backend.tf"
  type        = string
  default     = "commission-calc-tflock"
}
