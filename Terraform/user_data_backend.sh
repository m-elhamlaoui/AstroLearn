#!/bin/bash
yum update -y
yum install -y docker
service docker start
usermod -a -G docker ec2-user

# Docker image name
BACKEND_IMAGE="omarbdoc/astrolearn-back:latest"

# Pull the Docker image
docker pull $BACKEND_IMAGE

# Run the Docker container with environment variables
# Note: The template variables rds_instance_endpoint, db_name, db_username, db_password are passed by Terraform
docker run -d -p 8088:8088 \
  -e SPRING_DATASOURCE_URL="jdbc:postgresql://${rds_instance_endpoint}/${db_name}" \
  -e SPRING_DATASOURCE_USERNAME="${db_username}" \
  -e SPRING_DATASOURCE_PASSWORD="${db_password}" \
  -e SERVER_PORT="8088" \
  $BACKEND_IMAGE
