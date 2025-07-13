variable "aws_region" {
  description = "AWS region for resources"
  type        = string
  default     = "us-west-2"
}

variable "resource_prefix" {
  description = "Prefix for AWS resources to ensure uniqueness (e.g., 'niko-', 'gemini-')"
  type        = string
  default     = "dev-" # Default prefix for general development branches
}

variable "vpc_cidr" {
  description = "CIDR block for VPC"
  type        = string
  default     = "10.0.0.0/16"
}

variable "availability_zones" {
  description = "Availability zones"
  type        = list(string)
  default     = ["us-west-2a", "us-west-2b"]
}

variable "public_subnet_cidrs" {
  description = "CIDR blocks for public subnets"
  type        = list(string)
  default     = ["10.0.1.0/24", "10.0.2.0/24"]
}

variable "private_subnet_cidrs" {
  description = "CIDR blocks for private subnets"
  type        = list(string)
  default     = ["10.0.10.0/24", "10.0.20.0/24"]
}

variable "instance_type" {
  description = "EC2 instance type"
  type        = string
  default     = "t3.medium"
}

variable "key_name" {
  description = "AWS Key Pair name for EC2 instances"
  type        = string
}

variable "db_instance_class" {
  description = "RDS instance class"
  type        = string
  default     = "db.t3.micro"
}

variable "db_name" {
  description = "Database name"
  type        = string
  default     = "hrapp"
}

variable "db_username" {
  description = "Database username"
  type        = string
  default     = "postgres"
}

variable "db_password" {
  description = "Database password"
  type        = string
  sensitive   = true
}

variable "common_tags" {
  description = "Common tags for all resources"
  type        = map(string)
  default = {
    Project     = "team-1-hr-app"
    Environment = "development" # Default to development, can be overridden
    ManagedBy   = "terraform"
  }
}

variable "enable_frontend" {
  description = "Flag to enable frontend deployment on EC2 (AWS)"
  type        = bool
  default     = true
}

variable "enable_backend" {
  description = "Flag to enable backend deployment on EC2 (AWS)"
  type        = bool
  default     = true
} 