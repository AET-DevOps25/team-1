output "load_balancer_dns" {
  description = "DNS name of the Application Load Balancer"
  value       = aws_lb.app.dns_name
}

output "load_balancer_zone_id" {
  description = "Zone ID of the Application Load Balancer (for Route53 alias records)"
  value       = aws_lb.app.zone_id
}

output "autoscaling_group_name" {
  description = "Name of the Auto Scaling Group"
  value       = aws_autoscaling_group.app.name
}

output "launch_template_id" {
  description = "ID of the Launch Template"
  value       = aws_launch_template.app.id
}

output "launch_template_latest_version" {
  description = "Latest version of the Launch Template"
  value       = aws_launch_template.app.latest_version
} 