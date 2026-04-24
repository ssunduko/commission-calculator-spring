output "public_ip" {
  description = "Public IPv4 of the EC2 instance"
  value       = aws_instance.app.public_ip
}

output "public_dns" {
  description = "Public DNS name of the EC2 instance"
  value       = aws_instance.app.public_dns
}

output "app_url" {
  description = "HTTP endpoint for the Commission Calculator app"
  value       = "http://${aws_instance.app.public_ip}:8081"
}

output "ssh_command" {
  description = "Convenience SSH command — adjust the key path to match your local setup"
  value       = "ssh -i ~/.ssh/${var.key_pair_name}.pem ec2-user@${aws_instance.app.public_ip}"
}
