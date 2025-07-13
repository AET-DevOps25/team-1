variable "vpc_id" {
  description = "ID of the VPC"
  type        = string
}

variable "resource_prefix" {
  description = "Prefix for AWS resources within this module"
  type        = string
}

variable "tags" {
  description = "Tags to apply to resources"
  type        = map(string)
  default     = {}
} 