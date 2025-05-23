# Output the Frontend ALB DNS name
output "frontend_alb_dns" {
  description = "DNS name of the Frontend Application Load Balancer"
  value       = aws_lb.frontend_alb.dns_name
}

# Output the Backend ALB DNS name (internal)
output "backend_alb_dns" {
  description = "DNS name of the Backend Application Load Balancer (internal)"
  value       = aws_lb.backend_alb.dns_name
}

# Output the RDS instance endpoint
output "rds_instance_endpoint" {
  description = "Endpoint of the RDS database instance"
  value       = aws_db_instance.app_db.endpoint
}

# Output the RDS instance port
output "rds_instance_port" {
  description = "Port of the RDS database instance"
  value       = aws_db_instance.app_db.port
}

# Add a delay to ensure instances are fully provisioned
resource "time_sleep" "wait_for_instances" {
  depends_on = [aws_autoscaling_group.frontend_asg, aws_autoscaling_group.backend_asg]
  create_duration = "90s"
}

# Create a data source to get instance IPs from ASGs
data "aws_instances" "frontend_instances" {
  instance_tags = {
    "aws:autoscaling:groupName" = aws_autoscaling_group.frontend_asg.name
  }
  depends_on = [time_sleep.wait_for_instances]
  
  # Add filter to ensure we only get running instances
  filter {
    name   = "instance-state-name"
    values = ["running"]
  }
}

data "aws_instances" "backend_instances" {
  instance_tags = {
    "aws:autoscaling:groupName" = aws_autoscaling_group.backend_asg.name
  }
  depends_on = [time_sleep.wait_for_instances]
  
  # Add filter to ensure we only get running instances
  filter {
    name   = "instance-state-name"
    values = ["running"]
  }
}

# Output Frontend instance Private IPs
output "frontend_instance_private_ips" {
  description = "Private IPs of Frontend instances"
  value       = data.aws_instances.frontend_instances.private_ips
}

# Output Frontend instance Public IPs
output "frontend_instance_public_ips" {
  description = "Public IPs of Frontend instances"
  value       = data.aws_instances.frontend_instances.public_ips
}

# Output Backend instance Private IPs
output "backend_instance_private_ips" {
  description = "Private IPs of Backend instances"
  value       = data.aws_instances.backend_instances.private_ips
}

# Output Backend instance Public IPs
output "backend_instance_public_ips" {
  description = "Public IPs of Backend instances"
  value       = data.aws_instances.backend_instances.public_ips
}

# Create local files with instance IPs for Ansible
resource "local_file" "ansible_inventory" {
  filename = "${path.module}/ansible/inventory.ini"
  content  = <<-EOT
[frontend]
${join("\n", [for idx, private_ip in data.aws_instances.frontend_instances.private_ips : 
  format("%s ansible_host=%s ansible_user=ec2-user", 
    private_ip, 
    length(data.aws_instances.frontend_instances.public_ips) > idx ? data.aws_instances.frontend_instances.public_ips[idx] : private_ip
  )
])}

[backend]
${join("\n", [for idx, private_ip in data.aws_instances.backend_instances.private_ips :
  format("%s ansible_host=%s ansible_user=ec2-user",
    private_ip,
    length(data.aws_instances.backend_instances.public_ips) > idx ? data.aws_instances.backend_instances.public_ips[idx] : private_ip
  )
])}

[all:vars]
ansible_ssh_private_key_file=${path.module}/ansible/ssh_key.pem
ansible_ssh_common_args='-o StrictHostKeyChecking=no'
EOT

  depends_on = [time_sleep.wait_for_instances]
}
