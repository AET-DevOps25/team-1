resource "aws_security_group" "web" {
  name_prefix = "${var.resource_prefix}web-"
  vpc_id      = var.vpc_id

  ingress {
    description = "HTTP from ALB"
    from_port   = 8080 # Assuming backend services run on 8080
    to_port     = 8080
    protocol    = "tcp"
    security_groups = [aws_security_group.alb.id] # Allow traffic only from the ALB
  }

  ingress {
    description = "SSH"
    from_port   = 22
    to_port     = 22
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"] # Consider restricting this to your IP in production
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = merge(var.tags, {
    Name = "${var.resource_prefix}web-sg"
  })
}

resource "aws_security_group" "database" {
  name_prefix = "${var.resource_prefix}db-"
  vpc_id      = var.vpc_id

  ingress {
    description     = "PostgreSQL from Web SG"
    from_port       = 5432
    to_port         = 5432
    protocol        = "tcp"
    security_groups = [aws_security_group.web.id] # Allow traffic only from the web security group
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = merge(var.tags, {
    Name = "${var.resource_prefix}db-sg"
  })
}

resource "aws_security_group" "alb" {
  name_prefix = "${var.resource_prefix}alb-"
  vpc_id      = var.vpc_id

  ingress {
    description = "HTTP"
    from_port   = 80
    to_port     = 80
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  ingress {
    description = "HTTPS (if you add a listener for it)"
    from_port   = 443
    to_port     = 443
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = merge(var.tags, {
    Name = "${var.resource_prefix}alb-sg"
  })
} 