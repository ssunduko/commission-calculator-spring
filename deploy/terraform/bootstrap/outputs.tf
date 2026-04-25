output "state_bucket_name" {
  description = "Pass this to the main module: terraform init -backend-config=bucket=<value>"
  value       = aws_s3_bucket.tfstate.id
}

output "lock_table_name" {
  value = aws_dynamodb_table.tflock.id
}
