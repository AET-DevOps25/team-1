resource "aws_db_subnet_group" "main" {
  name       = "${var.resource_prefix}db-subnet-group"
  subnet_ids = var.private_subnet_ids

  tags = merge(var.tags, {
    Name = "${var.resource_prefix}db-subnet-group"
  })
}

resource "aws_db_parameter_group" "postgres" {
  name   = "${var.resource_prefix}postgres15-params" # Changed from family to name for uniqueness if needed
  family = "postgres15"

  parameter {
    name  = "shared_preload_libraries"
    value = "vector"
  }

  tags = merge(var.tags, {
    Name = "${var.resource_prefix}postgres-params"
  })
}

resource "aws_db_instance" "postgres" {
  identifier = "${var.resource_prefix}postgres-db"

  allocated_storage     = 20
  max_allocated_storage = 100
  storage_type          = "gp2"
  storage_encrypted     = true # Recommended for production

  engine         = "postgres"
  engine_version = "15.4" # Ensure this version supports pgvector or adjust as needed
  instance_class = var.db_instance_class

  db_name  = var.db_name
  username = var.db_username
  password = var.db_password

  vpc_security_group_ids = [var.db_security_group_id]
  db_subnet_group_name   = aws_db_subnet_group.main.name
  parameter_group_name   = aws_db_parameter_group.postgres.name

  backup_retention_period = 7
  backup_window           = "03:00-04:00"
  maintenance_window      = "sun:04:00-sun:05:00"

  skip_final_snapshot = true # Set to false for production usually
  deletion_protection = false # Set to true for production

  multi_az = false # Consider true for production for higher availability

  tags = merge(var.tags, {
    Name = "${var.resource_prefix}postgres-db"
  })
} 