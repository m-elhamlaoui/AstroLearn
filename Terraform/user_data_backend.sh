#!/bin/bash
yum update -y
yum install -y docker
service docker start
usermod -a -G docker ec2-user

# Install Docker Compose
curl -L "https://github.com/docker/compose/releases/latest/download/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
chmod +x /usr/local/bin/docker-compose

# Create app directory
mkdir -p /app
cd /app

# Create a simple test API
cat > app.py << 'EOF'
from flask import Flask, jsonify

app = Flask(__name__)

@app.route('/health')
def health_check():
    return jsonify({"status": "healthy"})

@app.route('/')
def hello():
    return jsonify({"message": "Backend is working!"})

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=8088)
EOF

# Create requirements.txt
cat > requirements.txt << 'EOF'
flask==2.0.1
EOF

# Create Dockerfile
cat > Dockerfile << 'EOF'
FROM python:3.9-slim
WORKDIR /app
COPY requirements.txt .
RUN pip install -r requirements.txt
COPY app.py .
EXPOSE 8088
CMD ["python", "app.py"]
EOF

# Build and run the Docker container
docker build -t backend-app .
docker run -d -p 8088:8088 backend-app 