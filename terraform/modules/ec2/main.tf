data "aws_ami" "amazon_linux" {
  most_recent = true
  owners      = ["amazon"]

  filter {
    name   = "name"
    values = ["amzn2-ami-hvm-*-x86_64-gp2"]
  }
}

resource "aws_launch_template" "app" {
  name_prefix   = "${var.resource_prefix}app-"
  image_id      = data.aws_ami.amazon_linux.id
  instance_type = var.instance_type
  key_name      = var.key_name

  vpc_security_group_ids = [var.web_security_group_id]

  user_data = base64encode(templatefile("${path.module}/user_data.sh", {
    db_endpoint     = var.db_endpoint
    db_name         = var.db_name
    db_user         = var.db_user
    db_password     = var.db_password # Note: Passing password here, ensure user_data script handles it securely
    enable_frontend = var.enable_frontend
    enable_backend  = var.enable_backend
    resource_prefix = var.resource_prefix # To potentially tag containers or differentiate logs
  }))

  tag_specifications {
    resource_type = "instance"
    tags = merge(var.tags, {
      Name = "${var.resource_prefix}app-instance"
    })
  }
  # Add IAM instance profile if your application needs to interact with other AWS services
  # iam_instance_profile {
  #   name = aws_iam_instance_profile.app.name
  # }
}

resource "aws_autoscaling_group" "app" {
  name                = "${var.resource_prefix}app-asg"
  vpc_zone_identifier = var.public_subnet_ids
  target_group_arns   = [aws_lb_target_group.app.arn]
  health_check_type   = "ELB"
  health_check_grace_period = 300

  min_size         = 1
  max_size         = 3
  desired_capacity = 1 # Start with 1, can be adjusted

  launch_template {
    id      = aws_launch_template.app.id
    version = "$Latest"
  }

  # Propagate tags to instances
  dynamic "tag" {
    for_each = merge(var.tags, { Name = "${var.resource_prefix}app-asg-instance" })
    content {
      key                 = tag.key
      value               = tag.value
      propagate_at_launch = true
    }
  }
}

resource "aws_lb" "app" {
  name               = "${var.resource_prefix}app-alb"
  internal           = false
  load_balancer_type = "application"
  security_groups    = [var.alb_security_group_id]
  subnets            = var.public_subnet_ids

  enable_deletion_protection = false

  tags = merge(var.tags, {
    Name = "${var.resource_prefix}app-alb"
  })
}

resource "aws_lb_target_group" "app" {
  name_prefix = substr("${var.resource_prefix}app-tg", 0, 25) # Ensure name is not too long
  port        = 80 # ALB listens on 80, target group points to container port via user_data
  protocol    = "HTTP"
  vpc_id      = var.vpc_id
  target_type = "instance"

  health_check {
    enabled             = true
    healthy_threshold   = 2
    interval            = 30
    matcher             = "200-399" # Broader range for health checks
    path                = "/"       # Default path, change if app has specific health endpoint
    port                = "traffic-port"
    protocol            = "HTTP"
    timeout             = 5
    unhealthy_threshold = 2
  }

  tags = merge(var.tags, {
    Name = "${var.resource_prefix}app-tg"
  })
}

resource "aws_lb_listener" "http" {
  load_balancer_arn = aws_lb.app.arn
  port              = 80
  protocol          = "HTTP"

  default_action {
    type             = "forward"
    target_group_arn = aws_lb_target_group.app.arn
  }
}

# Optional: HTTPS listener (requires ACM certificate)
# resource "aws_lb_listener" "https" {
#   load_balancer_arn = aws_lb.app.arn
#   port              = 443
#   protocol          = "HTTPS"
#   ssl_policy        = "ELBSecurityPolicy-2016-08"
#   certificate_arn   = var.acm_certificate_arn # Add this variable if using HTTPS
#
#   default_action {
#     type             = "forward"
#     target_group_arn = aws_lb_target_group.app.arn
#   }
# }

resource "aws_autoscaling_policy" "scale_up" {
  name                   = "${var.resource_prefix}scale-up"
  scaling_adjustment     = 1
  adjustment_type        = "ChangeInCapacity"
  cooldown               = 300
  autoscaling_group_name = aws_autoscaling_group.app.name
}

resource "aws_autoscaling_policy" "scale_down" {
  name                   = "${var.resource_prefix}scale-down"
  scaling_adjustment     = -1
  adjustment_type        = "ChangeInCapacity"
  cooldown               = 300
  autoscaling_group_name = aws_autoscaling_group.app.name
}

resource "aws_cloudwatch_metric_alarm" "cpu_high" {
  alarm_name          = "${var.resource_prefix}cpu-high"
  comparison_operator = "GreaterThanThreshold"
  evaluation_periods  = 2
  metric_name         = "CPUUtilization"
  namespace           = "AWS/EC2"
  period              = 120
  statistic           = "Average"
  threshold           = 70
  alarm_description   = "This metric monitors EC2 CPU utilization for ${var.resource_prefix}"
  alarm_actions       = [aws_autoscaling_policy.scale_up.arn]

  dimensions = {
    AutoScalingGroupName = aws_autoscaling_group.app.name
  }
}

resource "aws_cloudwatch_metric_alarm" "cpu_low" {
  alarm_name          = "${var.resource_prefix}cpu-low"
  comparison_operator = "LessThanThreshold"
  evaluation_periods  = 2
  metric_name         = "CPUUtilization"
  namespace           = "AWS/EC2"
  period              = 120
  statistic           = "Average"
  threshold           = 30
  alarm_description   = "This metric monitors EC2 CPU utilization for ${var.resource_prefix}"
  alarm_actions       = [aws_autoscaling_policy.scale_down.arn]

  dimensions = {
    AutoScalingGroupName = aws_autoscaling_group.app.name
  }
} 