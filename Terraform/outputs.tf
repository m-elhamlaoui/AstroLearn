output "frontend_alb_dns" {
  description = "DNS name of the Frontend Application Load Balancer"
  value       = aws_lb.frontend_alb.dns_name
}

output "backend_alb_dns" {
  description = "DNS name of the Backend Application Load Balancer (internal)"
  value       = aws_lb.backend_alb.dns_name
}

output "rds_instance_endpoint" {
  description = "Endpoint of the RDS database instance"
  value       = aws_db_instance.app_db.endpoint
}

output "rds_instance_port" {
  description = "Port of the RDS database instance"
  value       = aws_db_instance.app_db.port
}

resource "time_sleep" "wait_for_instances" {
  depends_on = [aws_autoscaling_group.frontend_asg, aws_autoscaling_group.backend_asg]
  create_duration = "90s"
}

data "aws_instances" "frontend_instances" {
  instance_tags = {
    "aws:autoscaling:groupName" = aws_autoscaling_group.frontend_asg.name
  }
  depends_on = [time_sleep.wait_for_instances]
  
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
  
  filter {
    name   = "instance-state-name"
    values = ["running"]
  }
}

output "frontend_instance_private_ips" {
  description = "Private IPs of Frontend instances"
  value       = data.aws_instances.frontend_instances.private_ips
}

output "frontend_instance_public_ips" {
  description = "Public IPs of Frontend instances"
  value       = data.aws_instances.frontend_instances.public_ips
}

output "backend_instance_private_ips" {
  description = "Private IPs of Backend instances"
  value       = data.aws_instances.backend_instances.private_ips
}

output "backend_instance_public_ips" {
  description = "Public IPs of Backend instances"
  value       = data.aws_instances.backend_instances.public_ips
}

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
