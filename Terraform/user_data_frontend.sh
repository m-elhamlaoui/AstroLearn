#!/bin/bash
yum update -y
yum install -y docker
service docker start
usermod -a -G docker ec2-user

# Install Docker Compose
curl -L "https://github.com/docker/compose/releases/latest/download/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
chmod +x /usr/local/bin/docker-compose

# Pull and run the specified Docker image
docker pull ${docker_image_frontend}
# Ensure the host port matches the target group port (var.frontend_app_port, which is 3000)
# The container itself exposes 3000 (as per its Dockerfile)
docker run -d -p 3000:3000 \
  -e NEXT_PUBLIC_API_URL="http://${backend_alb_dns_name}:${backend_app_port}" \
  ${docker_image_frontend}
