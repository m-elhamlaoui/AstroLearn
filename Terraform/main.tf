# --- TERRAFORM AND PROVIDER CONFIGURATION ---
# Fixed Syntax: 'terraform', 'required_providers', 'provider'
terraform {
  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.0"
    }
  }
}

# Fixed Syntax: 'provider', 'aws'
provider "aws" {
  # Variable: The AWS region to deploy resources into
  region = var.region
  access_key =  var.access_key
  secret_key = var.secret_key
}

# Resource to generate a random suffix for names to ensure uniqueness
# Fixed Syntax: 'resource', 'random_id'
resource "random_id" "suffix" {
  # Fixed Value: Length of the random suffix
  byte_length = 4
}

# --- NETWORK (VPC, Subnets, Gateway, Route Tables) ---

# Create a new VPC
# Fixed Syntax: 'resource', 'aws_vpc'
resource "aws_vpc" "app_vpc" {
  # Variable: The CIDR block for the VPC
  cidr_block = var.vpc_cidr
  # Fixed Setting: Enable DNS hostnames for instances in the VPC
  enable_dns_hostnames = true

  # Variable: Tags for identification
  tags = {
    Name      = "app-vpc-${random_id.suffix.hex}"
    Project   = "Astrolearn"
    ManagedBy = "Terraform"
  }
}

# Create Public Subnets (for internet-facing ALBs)
# Fixed Syntax: 'resource', 'aws_subnet'
resource "aws_subnet" "public" {
  # Variable: Count of public subnets (should match availability zones)
  count = length(var.availability_zones)
  # Variable: CIDR block for this specific subnet, calculated based on count
  cidr_block        = cidrsubnet(var.vpc_cidr, 8, count.index) # Adjust prefix length (8) as needed
  # Fixed Syntax: Reference to the VPC ID
  vpc_id            = aws_vpc.app_vpc.id
  # Variable: Assign to a specific availability zone
  availability_zone = element(var.availability_zones, count.index)
  # Fixed Setting: Automatically assign public IP addresses to instances launched in this subnet
  map_public_ip_on_launch = true

  # Variable: Tags for identification
  tags = {
    Name      = "public-subnet-${element(var.availability_zones, count.index)}-${random_id.suffix.hex}"
    Project   = "Astrolearn"
    ManagedBy = "Terraform"
  }
}

# Create Private Subnets (for EC2 instances in ASGs and RDS)
# Fixed Syntax: 'resource', 'aws_subnet'
resource "aws_subnet" "private" {
  # Variable: Count of private subnets (should match availability zones)
  count = length(var.availability_zones)
  # Variable: CIDR block for this specific subnet, calculated based on count (offset from public subnets)
  cidr_block        = cidrsubnet(var.vpc_cidr, 8, length(var.availability_zones) + count.index) # Adjust prefix length (8) as needed
  # Fixed Syntax: Reference to the VPC ID
  vpc_id            = aws_vpc.app_vpc.id
  # Variable: Assign to a specific availability zone
  availability_zone = element(var.availability_zones, count.index)
  # Fixed Setting: Do NOT automatically assign public IP addresses
  map_public_ip_on_launch = false # Instances here won't have public IPs by default

  # Variable: Tags for identification
  tags = {
    Name      = "private-subnet-${element(var.availability_zones, count.index)}-${random_id.suffix.hex}"
    Project   = "Astrolearn"
    ManagedBy = "Terraform"
  }
}

# Create an Internet Gateway to allow communication between the VPC and the internet
# Fixed Syntax: 'resource', 'aws_internet_gateway'
resource "aws_internet_gateway" "app_igw" {
  # Fixed Syntax: Reference to the VPC ID
  vpc_id = aws_vpc.app_vpc.id

  # Variable: Tags for identification
  tags = {
    Name      = "app-igw-${random_id.suffix.hex}"
    Project   = "Astrolearn"
    ManagedBy = "Terraform"
  }
}

# Create a Route Table for public subnets to direct internet traffic through the IGW
# Fixed Syntax: 'resource', 'aws_route_table'
resource "aws_route_table" "public" {
  # Fixed Syntax: Reference to the VPC ID
  vpc_id = aws_vpc.app_vpc.id

  # Fixed Syntax: Define a route
  route {
    # Fixed Value: Destination CIDR block (0.0.0.0/0 means "all traffic")
    cidr_block = "0.0.0.0/0"
    # Fixed Syntax: Target for the route (the Internet Gateway ID)
    gateway_id = aws_internet_gateway.app_igw.id
  }

  # Variable: Tags for identification
  tags = {
    Name      = "public-route-table-${random_id.suffix.hex}"
    Project   = "Astrolearn"
    ManagedBy = "Terraform"
  }
}

# Associate the public route table with the public subnets
# Fixed Syntax: 'resource', 'aws_route_table_association'
resource "aws_route_table_association" "public" {
  # Variable: Create one association per public subnet
  count = length(aws_subnet.public)
  # Fixed Syntax: Reference to the public subnet ID
  subnet_id = element(aws_subnet.public[*].id, count.index)
  # Fixed Syntax: Reference to the public route table ID
  route_table_id = aws_route_table.public.id
}

# Note: Private subnets do NOT get a route to the IGW in this basic setup.
# If instances in private subnets need outbound internet access (e.g., for updates),
# you would need a NAT Gateway in a public subnet and a route in the private route table
# pointing to the NAT Gateway. This is skipped for simplicity under Free Tier.

# --- SECURITY GROUPS ---

# Security Group for allowing SSH access to EC2 instances (primarily for Ansible)
# Fixed Syntax: 'resource', 'aws_security_group'
resource "aws_security_group" "ssh_allowed" {
  # Variable: Name for the security group
  name        = "ssh-access-${random_id.suffix.hex}"
  description = "Allow SSH access"
  # Fixed Syntax: Reference to the VPC ID
  vpc_id      = aws_vpc.app_vpc.id

  # Fixed Syntax: Define an ingress rule
  ingress {
    # Fixed Values: Port range
    from_port = 22
    to_port   = 22
    # Fixed Value: Protocol
    protocol  = "tcp"
    # Variable: Source CIDR block (0.0.0.0/0 means anywhere - restrict this for production!)
    cidr_blocks = ["0.0.0.0/0"] # <-- WARNING: Highly insecure for production! Restrict to your IP.
  }

  # Fixed Syntax: Define an egress rule (allow all outbound traffic)
  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1" # Fixed Value: -1 means all protocols
    cidr_blocks = ["0.0.0.0/0"] # Fixed Value: 0.0.0.0/0 means anywhere
  }

  # Variable: Tags for identification
  tags = {
    Name      = "Allow SSH Access"
    Project   = "Astrolearn"
    ManagedBy = "Terraform"
  }
}

# Security Group for the ALBs to allow incoming HTTP/HTTPS traffic from the internet
# Fixed Syntax: 'resource', 'aws_security_group'
resource "aws_security_group" "alb_ingress" {
  # Variable: Name for the security group
  name        = "alb-ingress-${random_id.suffix.hex}"
  description = "Allow HTTP/HTTPS access to ALBs"
  # Fixed Syntax: Reference to the VPC ID
  vpc_id      = aws_vpc.app_vpc.id

  # Ingress rule for HTTP
  ingress {
    from_port   = 80
    to_port     = 80
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"] # Variable: Source (0.0.0.0/0 means anywhere)
  }

  # Optional: Ingress rule for HTTPS if you plan to use it (requires ACM certificate)
  # ingress {
  #   from_port   = 443
  #   to_port     = 443
  #   protocol    = "tcp"
  #   cidr_blocks = ["0.0.0.0/0"]
  # }

  # Egress rule (allow all outbound, typically ALBs don't need much)
  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  # Variable: Tags for identification
  tags = {
    Name      = "ALB Ingress"
    Project   = "Astrolearn"
    ManagedBy = "Terraform"
  }
}

# Security Group for Frontend EC2 instances
resource "aws_security_group" "frontend_instances_sg" {
  name        = "frontend-instances-sg-${random_id.suffix.hex}"
  description = "Allow traffic to Frontend instances from Frontend ALB and allow outbound to Backend ALB"
  vpc_id      = aws_vpc.app_vpc.id

  ingress {
    description     = "Frontend App Port from Frontend ALB"
    from_port       = var.frontend_app_port
    to_port         = var.frontend_app_port
    protocol        = "tcp"
    security_groups = [aws_security_group.alb_ingress.id] # Traffic from Frontend ALB
  }

  # Egress rule to Backend ALB will be defined separately to break cycle
  egress {
    description = "Allow all outbound traffic for OS updates etc."
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = {
    Name      = "Frontend Instances SG"
    Project   = "Astrolearn"
    ManagedBy = "Terraform"
  }
}

# Security Group for Backend ALB
resource "aws_security_group" "backend_alb_sg" {
  name        = "backend-alb-sg-${random_id.suffix.hex}"
  description = "Allow traffic to Backend ALB from Frontend Instances"
  vpc_id      = aws_vpc.app_vpc.id

  ingress {
    description     = "Backend App Port from Internet"
    from_port       = var.backend_app_port
    to_port         = var.backend_app_port
    protocol        = "tcp"
    cidr_blocks     = ["0.0.0.0/0"] # Allow from anywhere on the internet
  }

  # Egress rule to Backend Instances will be defined separately to break cycle
   egress {
    description = "Allow all outbound traffic if necessary (e.g. health checks to instances)"
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = {
    Name      = "Backend ALB SG"
    Project   = "Astrolearn"
    ManagedBy = "Terraform"
  }
}

# Security Group for Backend EC2 instances
resource "aws_security_group" "backend_instances_sg" {
  name        = "backend-instances-sg-${random_id.suffix.hex}"
  description = "Allow traffic to Backend instances from Backend ALB and allow outbound to RDS"
  vpc_id      = aws_vpc.app_vpc.id

  ingress {
    description     = "Backend App Port from Backend ALB"
    from_port       = var.backend_app_port
    to_port         = var.backend_app_port
    protocol        = "tcp"
    security_groups = [aws_security_group.backend_alb_sg.id]
  }

  # Egress rule to RDS will be defined separately to break cycle
  egress {
    description = "Allow all outbound traffic for OS updates etc."
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = {
    Name      = "Backend Instances SG"
    Project   = "Astrolearn"
    ManagedBy = "Terraform"
  }
}

# Security Group for EC2 instances (Frontend/Backend) allowing traffic from ALBs
# Fixed Syntax: 'resource', 'aws_security_group'
resource "aws_security_group" "app_ingress_from_alb" {
  # Variable: Name for the security group
  name        = "app-ingress-from-alb-${random_id.suffix.hex}"
  description = "Allow traffic to app ports from ALBs"
  # Fixed Syntax: Reference to the VPC ID
  vpc_id      = aws_vpc.app_vpc.id

  # These rules are now handled by more specific SGs (frontend_instances_sg, backend_instances_sg)
  # and the overly permissive rules are removed.
  # This SG might become unused or repurposed for other needs if any.
  # For now, we remove its specific ingress rules related to app ports.

  # Egress rule (allow all outbound)
  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  # Variable: Tags for identification
  tags = {
    Name      = "App Ingress from ALB"
    Project   = "Astrolearn"
    ManagedBy = "Terraform"
  }
}


# Security Group for RDS instance allowing traffic *only* from Backend EC2 instances
# Fixed Syntax: 'resource', 'aws_security_group'
resource "aws_security_group" "rds_ingress_from_backend" {
  # Variable: Name for the security group
  name        = "rds-ingress-from-backend-${random_id.suffix.hex}"
  description = "Allow DB traffic from Backend EC2s"
  # Fixed Syntax: Reference to the VPC ID
  vpc_id      = aws_vpc.app_vpc.id

  # Ingress rule for database port
  ingress {
    from_port = var.db_port # Variable: Database port (e.g., 3306 for MySQL, 5432 for PostgreSQL)
    to_port   = var.db_port
    protocol  = "tcp"
    # Fixed Syntax: Source is the Security Group ID of the *Backend* EC2 instances
    # Source is the Security Group ID of the Backend EC2 instances
    security_groups = [aws_security_group.backend_instances_sg.id]
  }

  # Egress rule (allow all outbound - RDS needs to talk to S3 for backups sometimes)
  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  # Variable: Tags for identification
  tags = {
    Name      = "RDS Ingress from Backend"
    Project   = "Astrolearn"
    ManagedBy = "Terraform"
  }
}

# --- Standalone Security Group Rules to break cycles ---

resource "aws_security_group_rule" "frontend_to_backend_alb_egress" {
  type                     = "egress"
  from_port                = var.backend_app_port
  to_port                  = var.backend_app_port
  protocol                 = "tcp"
  security_group_id        = aws_security_group.frontend_instances_sg.id
  source_security_group_id = aws_security_group.backend_alb_sg.id
  description              = "Egress from Frontend instances to Backend ALB"
}

resource "aws_security_group_rule" "backend_alb_to_backend_instances_egress" {
  type                     = "egress"
  from_port                = var.backend_app_port
  to_port                  = var.backend_app_port
  protocol                 = "tcp"
  security_group_id        = aws_security_group.backend_alb_sg.id
  source_security_group_id = aws_security_group.backend_instances_sg.id
  description              = "Egress from Backend ALB to Backend instances"
}

resource "aws_security_group_rule" "backend_instances_to_rds_egress" {
  type                     = "egress"
  from_port                = var.db_port
  to_port                  = var.db_port
  protocol                 = "tcp"
  security_group_id        = aws_security_group.backend_instances_sg.id
  source_security_group_id = aws_security_group.rds_ingress_from_backend.id # This is the SG of the RDS
  description              = "Egress from Backend instances to RDS"
}

# --- RDS DATABASE ---

# DB Subnet Group for RDS (must span at least two Availability Zones)
# Fixed Syntax: 'resource', 'aws_db_subnet_group'
resource "aws_db_subnet_group" "app_db_subnet_group" {
  # Variable: Name for the subnet group
  name        = "app-db-subnet-group-${random_id.suffix.hex}"
  # Fixed Syntax: List of subnet IDs (using the private subnets)
  subnet_ids  = aws_subnet.private[*].id
  description = "Subnet group for RDS"

  # Variable: Tags for identification
  tags = {
    Name      = "App DB Subnet Group"
    Project   = "Astrolearn"
    ManagedBy = "Terraform"
  }
}

# RDS Database Instance
# Fixed Syntax: 'resource', 'aws_db_instance'
resource "aws_db_instance" "app_db" {
  # Variable: Allocated storage size (in GB)
  allocated_storage    = var.db_allocated_storage
  # Variable: Database engine (e.g., "mysql", "postgres")
  engine               = var.db_engine
  # Variable: Engine version
  engine_version       = var.db_engine_version
  # Variable: Instance class (e.g., "db.t2.micro", "db.t3.micro")
  instance_class       = var.db_instance_class # Use a Free Tier eligible class like db.t2.micro or db.t3.micro
  # Variable: Name for the database (not the instance identifier)
  db_name              = var.db_name
  # Variable: Master username (store securely, e.g., in Secrets Manager, but variable for learning)
  username             = var.db_username
  # Variable: Master password (store securely!)
  password             = var.db_password
  # Fixed Syntax: Reference to the DB Subnet Group name
  db_subnet_group_name = aws_db_subnet_group.app_db_subnet_group.name
  # Fixed Setting: Apply updates immediately (be careful in production)
  apply_immediately    = true
  # Fixed Setting: Do NOT enable multi-AZ for Free Tier
  multi_az             = false
  # Fixed Setting: Disable public accessibility (should only be accessed from VPC)
  publicly_accessible  = false
  # Fixed Syntax: List of Security Group IDs
  vpc_security_group_ids = [aws_security_group.rds_ingress_from_backend.id]
  # Fixed Setting: Skip final snapshot on destroy (useful for testing, remove for production)
  skip_final_snapshot = true
  # Variable: Name for the database instance identifier
  identifier           = "app-db-instance-${random_id.suffix.hex}"

  # Variable: Tags for identification
  tags = {
    Name      = "App Database"
    Project   = "Astrolearn"
    ManagedBy = "Terraform"
  }

  # Added lifecycle block to ignore changes to engine_version
  # This prevents Terraform from trying to upgrade/downgrade the DB engine version
  # if it drifts (e.g., due to automatic minor version upgrades by AWS).
  lifecycle {
    ignore_changes = [engine_version]
  }
}


# --- LOAD BALANCERS (ALB) AND TARGET GROUPS ---

# Target Group for Frontend ASG
# Fixed Syntax: 'resource', 'aws_lb_target_group'
resource "aws_lb_target_group" "frontend_tg" {
  # Variable: Name for the target group
  name     = "frontend-tg-${random_id.suffix.hex}"
  # Variable: Port the target group listens on (should match Frontend container port)
  port     = var.frontend_app_port
  # Fixed Value: Protocol for targets
  protocol = "HTTP"
  # Fixed Syntax: Reference to the VPC ID
  vpc_id   = aws_vpc.app_vpc.id
  # Fixed Value: Type of target (instance, ip, lambda)
  target_type = "instance"

  # Fixed Syntax: Health Check configuration
  health_check {
    # Fixed Value: Protocol for health check
    protocol = "HTTP"
    # Fixed Value: Port for health check (can be 'traffic-port' or a specific port)
    port     = "traffic-port" # Or specify var.frontend_app_port
    # Variable: Path for health check (e.g., /health)
    path     = var.frontend_health_check_path
    # Fixed Value: Healthy threshold count
    healthy_threshold   = 2
    # Fixed Value: Unhealthy threshold count
    unhealthy_threshold = 2
    # Fixed Value: Health check timeout in seconds
    timeout             = 3
    # Fixed Value: Health check interval in seconds
    interval            = 30
    # Fixed Value: Success codes
    matcher             = "200"
  }

  # Variable: Tags for identification
  tags = {
    Name      = "Frontend Target Group"
    Project   = "Astrolearn"
    ManagedBy = "Terraform"
  }
}

# Target Group for Backend ASG
# Fixed Syntax: 'resource', 'aws_lb_target_group'
resource "aws_lb_target_group" "backend_tg" {
  # Variable: Name for the target group
  name     = "backend-tg-${random_id.suffix.hex}"
  # Variable: Port the target group listens on (should match Backend container port)
  port     = var.backend_app_port
  # Fixed Value: Protocol for targets
  protocol = "HTTP" # Or HTTPS if your backend container handles TLS
  # Fixed Syntax: Reference to the VPC ID
  vpc_id   = aws_vpc.app_vpc.id
  # Fixed Value: Type of target (instance, ip, lambda)
  target_type = "instance"

  # Fixed Syntax: Health Check configuration
  health_check {
    protocol = "HTTP"
    port     = "traffic-port" # Or specify var.backend_app_port
    path     = var.backend_health_check_path
    healthy_threshold   = 2
    unhealthy_threshold = 2
    timeout             = 3
    interval            = 30
    matcher             = "200"
  }

  # Variable: Tags for identification
  tags = {
    Name      = "Backend Target Group"
    Project   = "Astrolearn"
    ManagedBy = "Terraform"
  }
}

# Internet-facing Application Load Balancer for Frontend
# Fixed Syntax: 'resource', 'aws_lb'
resource "aws_lb" "frontend_alb" {
  # Variable: Name for the load balancer
  name               = "frontend-alb-${random_id.suffix.hex}"
  # Fixed Value: Type of load balancer
  load_balancer_type = "application"
  # Fixed Value: Scheme (internet-facing or internal)
  internal           = false  # This makes it internet-facing
  # Fixed Syntax: List of public subnet IDs
  subnets            = aws_subnet.public[*].id
  # Fixed Syntax: List of Security Group IDs (the one allowing internet ingress)
  security_groups    = [aws_security_group.alb_ingress.id]

  # Variable: Tags for identification
  tags = {
    Name      = "Frontend ALB"
    Project   = "Astrolearn"
    ManagedBy = "Terraform"
  }
}

# Internal Application Load Balancer for Backend
# Fixed Syntax: 'resource', 'aws_lb'
resource "aws_lb" "backend_alb" {
  # Variable: Name for the load balancer
  name               = "backend-alb-${random_id.suffix.hex}"
  # Fixed Value: Type of load balancer
  load_balancer_type = "application"
  # Fixed Value: Scheme (internal)
  internal           = false # This makes it internet-facing
  # Fixed Syntax: List of public subnet IDs (internal ALBs can be in public or private subnets)
  # Using public here means Frontend instances (in private subnets) can route to it.
  subnets            = aws_subnet.public[*].id # Or aws_subnet.private[*].id if you prefer to keep ALBs separate
  security_groups    = [aws_security_group.backend_alb_sg.id]

  # Variable: Tags for identification
  tags = {
    Name      = "Backend ALB"
    Project   = "Astrolearn"
    ManagedBy = "Terraform"
  }
}


# Listener for Frontend ALB (HTTP traffic on port 80)
# Fixed Syntax: 'resource', 'aws_lb_listener'
resource "aws_lb_listener" "frontend_http_listener" {
  # Fixed Syntax: Reference to the Frontend ALB ARN
  load_balancer_arn = aws_lb.frontend_alb.arn
  # Fixed Value: Port the listener listens on
  port              = 80
  # Fixed Value: Protocol the listener listens on
  protocol          = "HTTP"

  # Fixed Syntax: Default action for incoming traffic
  default_action {
    # Fixed Value: Type of action (forward to a target group)
    type             = "forward"
    # Fixed Syntax: Reference to the Frontend Target Group ARN
    target_group_arn = aws_lb_target_group.frontend_tg.arn
  }
}

# Listener for Backend ALB (e.g., HTTP traffic on backend app port)
# Fixed Syntax: 'resource', 'aws_lb_listener'
resource "aws_lb_listener" "backend_http_listener" {
  # Fixed Syntax: Reference to the Backend ALB ARN
  load_balancer_arn = aws_lb.backend_alb.arn
  # Variable: Port the listener listens on (should match Backend container port)
  port              = var.backend_app_port # Frontend will connect to this port
  # Fixed Value: Protocol
  protocol          = "HTTP"

  # Fixed Syntax: Default action
  default_action {
    type             = "forward"
    # Fixed Syntax: Reference to the Backend Target Group ARN
    target_group_arn = aws_lb_target_group.backend_tg.arn
  }
}

# --- AUTO SCALING GROUPS (ASG) AND LAUNCH TEMPLATES ---

# Launch Template for Frontend EC2 instances
# Fixed Syntax: 'resource', 'aws_launch_template'
resource "aws_launch_template" "frontend_template" {
  # Variable: Name for the launch template
  name_prefix = "frontend-template-${random_id.suffix.hex}-"

  # Variable: AMI ID
  image_id      = var.ami_id
  # Variable: Instance Type (e.g., t2.micro)
  instance_type = var.instance_type

  # Fixed Syntax: Instance Market Options (optional, for Spot instances - not needed for Free Tier)
  # instance_market_options {
  #   market_type = "spot"
  # }

  # Fixed Syntax: Network Interfaces configuration
  network_interfaces {
    # Fixed Value: Delete network interface on instance termination
    delete_on_termination = true
    # Fixed Syntax: Reference to the list of Security Group IDs (SSH + App Ingress)
    security_groups = [
      aws_security_group.ssh_allowed.id,
      aws_security_group.frontend_instances_sg.id,
    ]
    # Instances in private subnets, temporarily enabling public IP for debugging
    associate_public_ip_address = true
  }

  # Variable: Key Pair name for SSH access
  key_name = var.key_pair_name

  # Variable: User data script to run on instance launch (e.g., install Docker)
  # This script will be executed by cloud-init.
  # You'll need to define this user_data in your variables or a separate file.
  user_data = base64encode(templatefile("user_data_frontend.sh", {
    backend_alb_dns_name = aws_lb.backend_alb.dns_name
    backend_app_port     = var.backend_app_port
    docker_image_frontend = var.docker_image_frontend
  }))

  depends_on = [
    aws_autoscaling_group.backend_asg,
    aws_lb.backend_alb
  ]

  # Variable: Tags applied to the instance *and* volumes
  tag_specifications {
    resource_type = "instance"
    tags = {
      Name      = "frontend-instance-${random_id.suffix.hex}"
      Project   = "Astrolearn"
      ManagedBy = "Terraform"
      Role      = "Frontend"
    }
  }
  tag_specifications {
    resource_type = "volume"
    tags = {
      Name      = "frontend-volume-${random_id.suffix.hex}"
      Project   = "Astrolearn"
      ManagedBy = "Terraform"
      Role      = "Frontend"
    }
  }

  # Variable: Tags applied to the Launch Template itself
  tags = {
    Name      = "Frontend Launch Template"
    Project   = "Astrolearn"
    ManagedBy = "Terraform"
  }
}

# Launch Template for Backend EC2 instances
# Fixed Syntax: 'resource', 'aws_launch_template'
resource "aws_launch_template" "backend_template" {
  # Variable: Name for the launch template
  name_prefix = "backend-template-${random_id.suffix.hex}-"

  # Variable: AMI ID
  image_id      = var.ami_id
  # Variable: Instance Type (can be the same or different from frontend)
  instance_type = var.instance_type

  # Fixed Syntax: Network Interfaces configuration
  network_interfaces {
    delete_on_termination = true
    # Fixed Syntax: Reference to the list of Security Group IDs (SSH + App Ingress)
    security_groups = [
      aws_security_group.ssh_allowed.id,
      aws_security_group.backend_instances_sg.id,
    ]
    # Instances in private subnets, temporarily enabling public IP for debugging
    associate_public_ip_address = true
  }

  # Variable: Key Pair name
  key_name = var.key_pair_name

  # Variable: User data script to run on instance launch (e.g., install Docker)
  user_data = base64encode(templatefile("user_data_backend.sh", {
    rds_instance_endpoint = aws_db_instance.app_db.endpoint
    db_name               = var.db_name
    db_username           = var.db_username
    db_password           = var.db_password
    docker_image_backend  = var.docker_image_backend
    # backend_app_port is already available via var.backend_app_port if needed by the script directly
    # but the primary use here is for DB connection.
  }))

  depends_on = [
    aws_lb.backend_alb,
    aws_db_instance.app_db
  ]

  # Variable: Tags applied to the instance *and* volumes
  tag_specifications {
    resource_type = "instance"
    tags = {
      Name      = "backend-instance-${random_id.suffix.hex}"
      Project   = "Astrolearn"
      ManagedBy = "Terraform"
      Role      = "Backend"
    }
  }
  tag_specifications {
    resource_type = "volume"
    tags = {
      Name      = "backend-volume-${random_id.suffix.hex}"
      Project   = "Astrolearn"
      ManagedBy = "Terraform"
    }
  }

  # Variable: Tags applied to the Launch Template itself
  tags = {
    Name      = "Backend Launch Template"
    Project   = "Astrolearn"
    ManagedBy = "Terraform"
  }
}


# Auto Scaling Group for Frontend instances
# Fixed Syntax: 'resource', 'aws_autoscaling_group'
resource "aws_autoscaling_group" "frontend_asg" {
  # Variable: Name for the ASG
  name                = "frontend-asg-${random_id.suffix.hex}"
  # Fixed Syntax: Reference to the launch template ID
  desired_capacity    = var.frontend_asg_desired_capacity
  max_size           = var.frontend_asg_max_size
  min_size           = var.frontend_asg_min_size
  target_group_arns  = [aws_lb_target_group.frontend_tg.arn]
  vpc_zone_identifier = aws_subnet.public[*].id # Temporarily use public subnets for debugging SSH
  health_check_type  = "ELB"
  health_check_grace_period = 300

  # Fixed Syntax: Reference to the launch template
  launch_template {
    id      = aws_launch_template.frontend_template.id
    version = "$Latest"
  }

  # Variable: Tags for identification
  tag {
    key                 = "Name"
    value               = "frontend-instance-${random_id.suffix.hex}"
    propagate_at_launch = true
  }
  tag {
    key                 = "Project"
    value               = "Astrolearn"
    propagate_at_launch = true
  }
  tag {
    key                 = "ManagedBy"
    value               = "Terraform"
    propagate_at_launch = true
  }
  tag {
    key                 = "Role"
    value               = "Frontend"
    propagate_at_launch = true
  }
}

# Auto Scaling Group for Backend instances
# Fixed Syntax: 'resource', 'aws_autoscaling_group'
resource "aws_autoscaling_group" "backend_asg" {
  # Variable: Name for the ASG
  name                = "backend-asg-${random_id.suffix.hex}"
  # Fixed Syntax: Reference to the launch template ID
  desired_capacity    = var.backend_asg_desired_capacity
  max_size           = var.backend_asg_max_size
  min_size           = var.backend_asg_min_size
  target_group_arns  = [aws_lb_target_group.backend_tg.arn]
  vpc_zone_identifier = aws_subnet.public[*].id # Temporarily use public subnets for debugging SSH
  health_check_type  = "ELB"
  health_check_grace_period = 300

  # Fixed Syntax: Reference to the launch template
  launch_template {
    id      = aws_launch_template.backend_template.id
    version = "$Latest"
  }

  # Variable: Tags for identification
  tag {
    key                 = "Name"
    value               = "backend-instance-${random_id.suffix.hex}"
    propagate_at_launch = true
  }
  tag {
    key                 = "Project"
    value               = "Astrolearn"
    propagate_at_launch = true
  }
  tag {
    key                 = "ManagedBy"
    value               = "Terraform"
    propagate_at_launch = true
  }
  tag {
    key                 = "Role"
    value               = "Backend"
    propagate_at_launch = true
  }
}
