terraform {
  required_version = ">= 1.0"
  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.0"
    }
  }
}

provider "aws" {
  region = var.aws_region
}

module "vpc" {
  source = "./modules/vpc"

  vpc_cidr             = var.vpc_cidr
  availability_zones   = var.availability_zones
  public_subnet_cidrs  = var.public_subnet_cidrs
  private_subnet_cidrs = var.private_subnet_cidrs
  resource_prefix      = var.resource_prefix
  tags                 = var.common_tags
}

module "security" {
  source = "./modules/security"

  vpc_id          = module.vpc.vpc_id
  resource_prefix = var.resource_prefix
  tags            = var.common_tags
}

module "rds" {
  source = "./modules/rds"

  vpc_id                = module.vpc.vpc_id
  private_subnet_ids    = module.vpc.private_subnet_ids
  db_security_group_id  = module.security.db_security_group_id
  resource_prefix       = var.resource_prefix
  db_instance_class     = var.db_instance_class
  db_name               = var.db_name
  db_username           = var.db_username
  db_password           = var.db_password
  tags                  = var.common_tags
}

module "ec2" {
  source = "./modules/ec2"

  vpc_id                = module.vpc.vpc_id
  public_subnet_ids     = module.vpc.public_subnet_ids
  web_security_group_id = module.security.web_security_group_id
  alb_security_group_id = module.security.alb_security_group_id
  resource_prefix       = var.resource_prefix
  instance_type         = var.instance_type
  key_name              = var.key_name
  db_endpoint           = module.rds.db_endpoint
  tags                  = var.common_tags
  enable_frontend       = var.enable_frontend
  enable_backend        = var.enable_backend
}
