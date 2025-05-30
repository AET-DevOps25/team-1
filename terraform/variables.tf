variable "aws_region" {
  default = "us-east-1"
}

variable "instance_type" {
  default = "t2.micro"
}

variable "key_name" {
  description = "The name of the SSH key pair"
}

variable "public_key_path" {
  description = "Path to your local public key file"
}

variable "private_key_path" {
  description = "Path to your local private key file"
}

variable "ami" {
  default = "ami-0c02fb55956c7d316" # Ubuntu 22.04 LTS (us-east-1)
}
