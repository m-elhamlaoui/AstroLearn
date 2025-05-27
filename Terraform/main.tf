terraform {
  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.0"
    }
  }
}

provider "aws" {
  region = var.region
  access_key =  var.access_key
  secret_key = var.secret_key
}

resource "random_id" "suffix" {
  byte_length = 4
}

resource "aws_vpc" "app_vpc" {
  cidr_block = var.vpc_cidr
  enable_dns_hostnames = true

  tags = {
    Name      = "app-vpc-${random_id.suffix.hex}"
    Project   = "Astrolearn"
    ManagedBy = "Terraform"
  }
}

resource "aws_subnet" "public" {
  count = length(var.availability_zones)
  cidr_block        = cidrsubnet(var.vpc_cidr, 8, count.index)
  vpc_id            = aws_vpc.app_vpc.id
  availability_zone = element(var.availability_zones, count.index)
  map_public_ip_on_launch = true

  tags = {
    Name      = "public-subnet-${element(var.availability_zones, count.index)}-${random_id.suffix.hex}"
    Project   = "Astrolearn"
    ManagedBy = "Terraform"
  }
}

resource "aws_subnet" "private" {
  count = length(var.availability_zones)
  cidr_block        = cidrsubnet(var.vpc_cidr, 8, length(var.availability_zones) + count.index)
  vpc_id            = aws_vpc.app_vpc.id
  availability_zone = element(var.availability_zones, count.index)
  map_public_ip_on_launch = false

  tags = {
    Name      = "private-subnet-${element(var.availability_zones, count.index)}-${random_id.suffix.hex}"
    Project   = "Astrolearn"
    ManagedBy = "Terraform"
  }
}

resource "aws_internet_gateway" "app_igw" {
  vpc_id = aws_vpc.app_vpc.id

  tags = {
    Name      = "app-igw-${random_id.suffix.hex}"
    Project   = "Astrolearn"
    ManagedBy = "Terraform"
  }
}

resource "aws_route_table" "public" {
  vpc_id = aws_vpc.app_vpc.id

  route {
    cidr_block = "0.0.0.0/0"
    gateway_id = aws_internet_gateway.app_igw.id
  }

  tags = {
    Name      = "public-route-table-${random_id.suffix.hex}"
    Project   = "Astrolearn"
    ManagedBy = "Terraform"
  }
}

resource "aws_route_table_association" "public" {
  count = length(aws_subnet.public)
  subnet_id = element(aws_subnet.public[*].id, count.index)
  route_table_id = aws_route_table.public.id
}

resource "aws_security_group" "ssh_allowed" {
  name        = "ssh-access-${random_id.suffix.hex}"
  description = "Allow SSH access"
  vpc_id      = aws_vpc.app_vpc.id

  ingress {
    from_port = 22
    to_port   = 22
    protocol  = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = {
    Name      = "Allow SSH Access"
    Project   = "Astrolearn"
    ManagedBy = "Terraform"
  }
}

resource "aws_security_group" "alb_ingress" {
  name        = "alb-ingress-${random_id.suffix.hex}"
  description = "Allow HTTP/HTTPS access to ALBs"
  vpc_id      = aws_vpc.app_vpc.id

  ingress {
    from_port   = 80
    to_port     = 80
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = {
    Name      = "ALB Ingress"
    Project   = "Astrolearn"
    ManagedBy = "Terraform"
  }
}

resource "aws_security_group" "frontend_instances_sg" {
  name        = "frontend-instances-sg-${random_id.suffix.hex}"
  description = "Allow traffic to Frontend instances from Frontend ALB and allow outbound to Backend ALB"
  vpc_id      = aws_vpc.app_vpc.id

  ingress {
    description     = "Frontend App Port from Frontend ALB"
    from_port       = var.frontend_app_port
    to_port         = var.frontend_app_port
    protocol        = "tcp"
    security_groups = [aws_security_group.alb_ingress.id]
  }

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

resource "aws_security_group" "backend_alb_sg" {
  name        = "backend-alb-sg-${random_id.suffix.hex}"
  description = "Allow traffic to Backend ALB from Frontend Instances"
  vpc_id      = aws_vpc.app_vpc.id

  ingress {
    description     = "Backend App Port from Internet"
    from_port       = var.backend_app_port
    to_port         = var.backend_app_port
    protocol        = "tcp"
    cidr_blocks     = ["0.0.0.0/0"]
  }

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

resource "aws_security_group" "app_ingress_from_alb" {
  name        = "app-ingress-from-alb-${random_id.suffix.hex}"
  description = "Allow traffic to app ports from ALBs"
  vpc_id      = aws_vpc.app_vpc.id

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = {
    Name      = "App Ingress from ALB"
    Project   = "Astrolearn"
    ManagedBy = "Terraform"
  }
}


resource "aws_security_group" "rds_ingress_from_backend" {
  name        = "rds-ingress-from-backend-${random_id.suffix.hex}"
  description = "Allow DB traffic from Backend EC2s"
  vpc_id      = aws_vpc.app_vpc.id

  ingress {
    from_port = var.db_port
    to_port   = var.db_port
    protocol  = "tcp"
    security_groups = [aws_security_group.backend_instances_sg.id]
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = {
    Name      = "RDS Ingress from Backend"
    Project   = "Astrolearn"
    ManagedBy = "Terraform"
  }
}

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
  source_security_group_id = aws_security_group.rds_ingress_from_backend.id
  description              = "Egress from Backend instances to RDS"
}

resource "aws_db_subnet_group" "app_db_subnet_group" {
  name        = "app-db-subnet-group-${random_id.suffix.hex}"
  subnet_ids  = aws_subnet.private[*].id
  description = "Subnet group for RDS"

  tags = {
    Name      = "App DB Subnet Group"
    Project   = "Astrolearn"
    ManagedBy = "Terraform"
  }
}

resource "aws_db_instance" "app_db" {
  allocated_storage    = var.db_allocated_storage
  engine               = var.db_engine
  engine_version       = var.db_engine_version
  instance_class       = var.db_instance_class
  db_name              = var.db_name
  username             = var.db_username
  password             = var.db_password
  db_subnet_group_name = aws_db_subnet_group.app_db_subnet_group.name
  apply_immediately    = true
  multi_az             = false
  publicly_accessible  = false
  vpc_security_group_ids = [aws_security_group.rds_ingress_from_backend.id]
  skip_final_snapshot = true
  identifier           = "app-db-instance-${random_id.suffix.hex}"

  tags = {
    Name      = "App Database"
    Project   = "Astrolearn"
    ManagedBy = "Terraform"
  }

  lifecycle {
    ignore_changes = [engine_version]
  }
}


resource "aws_lb_target_group" "frontend_tg" {
  name     = "frontend-tg-${random_id.suffix.hex}"
  port     = var.frontend_app_port
  protocol = "HTTP"
  vpc_id   = aws_vpc.app_vpc.id
  target_type = "instance"

  health_check {
    protocol = "HTTP"
    port     = "traffic-port"
    path     = var.frontend_health_check_path
    healthy_threshold   = 2
    unhealthy_threshold = 2
    timeout             = 3
    interval            = 30
    matcher             = "200"
  }

  tags = {
    Name      = "Frontend Target Group"
    Project   = "Astrolearn"
    ManagedBy = "Terraform"
  }
}

resource "aws_lb_target_group" "backend_tg" {
  name     = "backend-tg-${random_id.suffix.hex}"
  port     = var.backend_app_port
  protocol = "HTTP"
  vpc_id   = aws_vpc.app_vpc.id
  target_type = "instance"

  health_check {
    protocol = "HTTP"
    port     = "traffic-port"
    path     = var.backend_health_check_path
    healthy_threshold   = 2
    unhealthy_threshold = 2
    timeout             = 3
    interval            = 30
    matcher             = "200"
  }

  tags = {
    Name      = "Backend Target Group"
    Project   = "Astrolearn"
    ManagedBy = "Terraform"
  }
}

resource "aws_lb" "frontend_alb" {
  name               = "frontend-alb-${random_id.suffix.hex}"
  load_balancer_type = "application"
  internal           = false
  subnets            = aws_subnet.public[*].id
  security_groups    = [aws_security_group.alb_ingress.id]

  tags = {
    Name      = "Frontend ALB"
    Project   = "Astrolearn"
    ManagedBy = "Terraform"
  }
}

resource "aws_lb" "backend_alb" {
  name               = "backend-alb-${random_id.suffix.hex}"
  load_balancer_type = "application"
  internal           = false
  subnets            = aws_subnet.public[*].id
  security_groups    = [aws_security_group.backend_alb_sg.id]

  tags = {
    Name      = "Backend ALB"
    Project   = "Astrolearn"
    ManagedBy = "Terraform"
  }
}


resource "aws_lb_listener" "frontend_http_listener" {
  load_balancer_arn = aws_lb.frontend_alb.arn
  port              = 80
  protocol          = "HTTP"

  default_action {
    type             = "forward"
    target_group_arn = aws_lb_target_group.frontend_tg.arn
  }
}

resource "aws_lb_listener" "backend_http_listener" {
  load_balancer_arn = aws_lb.backend_alb.arn
  port              = var.backend_app_port
  protocol          = "HTTP"

  default_action {
    type             = "forward"
    target_group_arn = aws_lb_target_group.backend_tg.arn
  }
}

resource "aws_launch_template" "frontend_template" {
  name_prefix = "frontend-template-${random_id.suffix.hex}-"

  image_id      = var.ami_id
  instance_type = var.instance_type

  network_interfaces {
    delete_on_termination = true
    security_groups = [
      aws_security_group.ssh_allowed.id,
      aws_security_group.frontend_instances_sg.id,
    ]
    associate_public_ip_address = true
  }

  key_name = var.key_pair_name

  user_data = base64encode(templatefile("user_data_frontend.sh", {
    backend_alb_dns_name = aws_lb.backend_alb.dns_name
    backend_app_port     = var.backend_app_port
    docker_image_frontend = var.docker_image_frontend
  }))

  depends_on = [
    aws_autoscaling_group.backend_asg,
    aws_lb.backend_alb
  ]

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

  tags = {
    Name      = "Frontend Launch Template"
    Project   = "Astrolearn"
    ManagedBy = "Terraform"
  }
}

resource "aws_launch_template" "backend_template" {
  name_prefix = "backend-template-${random_id.suffix.hex}-"

  image_id      = var.ami_id
  instance_type = var.instance_type

  network_interfaces {
    delete_on_termination = true
    security_groups = [
      aws_security_group.ssh_allowed.id,
      aws_security_group.backend_instances_sg.id,
    ]
    associate_public_ip_address = true
  }

  key_name = var.key_pair_name

  user_data = base64encode(templatefile("user_data_backend.sh", {
    rds_instance_endpoint = aws_db_instance.app_db.endpoint
    db_name               = var.db_name
    db_username           = var.db_username
    db_password           = var.db_password
    docker_image_backend  = var.docker_image_backend
  }))

  depends_on = [
    aws_lb.backend_alb,
    aws_db_instance.app_db
  ]

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

  tags = {
    Name      = "Backend Launch Template"
    Project   = "Astrolearn"
    ManagedBy = "Terraform"
  }
}


resource "aws_autoscaling_group" "frontend_asg" {
  name                = "frontend-asg-${random_id.suffix.hex}"
  desired_capacity    = var.frontend_asg_desired_capacity
  max_size           = var.frontend_asg_max_size
  min_size           = var.frontend_asg_min_size
  target_group_arns  = [aws_lb_target_group.frontend_tg.arn]
  vpc_zone_identifier = aws_subnet.public[*].id
  health_check_type  = "ELB"
  health_check_grace_period = 300

  launch_template {
    id      = aws_launch_template.frontend_template.id
    version = "$Latest"
  }

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

resource "aws_autoscaling_group" "backend_asg" {
  name                = "backend-asg-${random_id.suffix.hex}"
  desired_capacity    = var.backend_asg_desired_capacity
  max_size           = var.backend_asg_max_size
  min_size           = var.backend_asg_min_size
  target_group_arns  = [aws_lb_target_group.backend_tg.arn]
  vpc_zone_identifier = aws_subnet.public[*].id
  health_check_type  = "ELB"
  health_check_grace_period = 300

  launch_template {
    id      = aws_launch_template.backend_template.id
    version = "$Latest"
  }

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
