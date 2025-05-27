#!/bin/bash
yum update -y
yum install -y docker
service docker start
usermod -a -G docker ec2-user

# Install Docker Compose
curl -L "https://github.com/docker/compose/releases/latest/download/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
chmod +x /usr/local/bin/docker-compose

# Pull the specified Docker image
docker pull ${docker_image_frontend}

# Stop any existing containers (in case of instance reuse)
docker ps -q | xargs -r docker stop
docker ps -a -q | xargs -r docker rm

# Create a temporary directory to mount into the container
mkdir -p /tmp/frontend-config

# Create the runtime config.js file with the correct backend URL
cat > /tmp/frontend-config/config.js << EOL
window.RUNTIME_CONFIG = {
  API_URL: 'http://${backend_alb_dns_name}:${backend_app_port}'
};
EOL

echo "Created runtime config with backend URL: http://${backend_alb_dns_name}:${backend_app_port}"

# Run with port 80 mapped to container port 3000 for ALB health checks
# Also keep 3000:3000 mapping for direct access if needed
# Mount the config file into the container's public directory
docker run -d \
  -p 80:3000 \
  -p 3000:3000 \
  -v /tmp/frontend-config/config.js:/app/public/config.js \
  -e NEXT_PUBLIC_API_URL="http://${backend_alb_dns_name}:${backend_app_port}" \
  --restart always \
  ${docker_image_frontend}
