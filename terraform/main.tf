provider "aws" {
  region = "us-east-1"
}

resource "aws_instance" "existing" {
  ami                         = "ami-084568db4383264d4"
  instance_type               = "t3.small"
  key_name                    = "vockey"
  vpc_security_group_ids      = ["sg-01d6992f68e85ef75"]
  subnet_id                   = "subnet-006efa2da636c282b"
  associate_public_ip_address = true

  tags = {
    Name = "liu"
  }
}
