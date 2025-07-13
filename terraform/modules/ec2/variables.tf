variable "vpc_id" {
  description = "ID of the VPC"
  type        = string
}

variable "public_subnet_ids" {
  description = "IDs of the public subnets"
  type        = list(string)
}

variable "web_security_group_id" {
  description = "ID of the web security group for EC2 instances"
  type        = string
}

variable "alb_security_group_id" {
  description = "ID of the security group for the Application Load Balancer"
  type        = string
}

variable "resource_prefix" {
  description = "Prefix for AWS resources within this module"
  type        = string
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

variable "db_endpoint" {
  description = "Database endpoint (passed to user_data)"
  type        = string
}

variable "db_name" {
  description = "Database name (passed to user_data)"
  type        = string
}

variable "db_user" {
  description = "Database username (passed to user_data)"
  type        = string
}

variable "db_password" {
  description = "Database password (passed to user_data)"
  type        = string
  sensitive   = true
}

variable "enable_frontend" {
  description = "Flag to enable frontend deployment (passed to user_data)"
  type        = bool
  default     = true
}

variable "enable_backend" {
  description = "Flag to enable backend deployment (passed to user_data)"
  type        = bool
  default     = true
}

variable "tags" {
  description = "Tags to apply to resources"
  type        = map(string)
  default     = {}
}

# Optional: Add if using HTTPS for ALB
# variable "acm_certificate_arn" {
#   description = "ARN of the ACM certificate for HTTPS listener"
#   type        = string
#   default     = ""
# } 