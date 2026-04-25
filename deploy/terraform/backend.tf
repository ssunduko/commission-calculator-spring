terraform {
  backend "s3" {
    key            = "commission-calc/terraform.tfstate"
    region         = "us-east-1"
    dynamodb_table = "commission-calc-tflock"
    encrypt        = true
  }
}
