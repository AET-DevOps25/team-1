provider "aws" {
  region = var.aws_region
}

resource "aws_key_pair" "deployer" {
  key_name   = var.key_name
  public_key = file(var.public_key_path)
}

resource "aws_instance" "app_server" {
  ami                    = var.ami
  instance_type          = var.instance_type
  key_name               = aws_key_pair.deployer.key_name
  associate_public_ip_address = true

  tags = {
    Name = "ai-hr-server"
  }

  provisioner "local-exec" {
    command = "echo ${self.public_ip} > ../ansible/hosts.txt"
  }
}
