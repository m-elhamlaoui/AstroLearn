variable "access_key" {
  description = "AWS access key"
  type        = string
  sensitive   = true
}

variable "secret_key" {
  description = "AWS secret key"
  type        = string
  sensitive   = true
}

variable "region" {
  description = "AWS region to deploy resources into."
  type        = string
  default     = "us-east-1"
}

variable "vpc_cidr" {
  description = "CIDR block for the VPC."
  type        = string
  default     = "10.0.0.0/16"
}

variable "availability_zones" {
  description = "List of availability zones to use for public and private subnets."
  type        = list(string)
  default     = ["us-east-1a", "us-east-1b"]
}

variable "frontend_app_port" {
  description = "The port your frontend application container listens on."
  type        = number
  default     = 3000
}

variable "backend_app_port" {
  description = "The port your backend application container listens on."
  type        = number
  default     = 8088
}

variable "db_engine" {
  description = "Database engine for RDS (e.g., 'mysql', 'postgres')."
  type        = string
  default     = "postgres"
}

variable "db_engine_version" {
  description = "Database engine version. Verify Free Tier eligibility for this version in your region."
  type        = string
  default     = "17.4"
}

variable "db_instance_class" {
  description = "DB instance class for RDS. Use db.t2.micro or db.t3.micro for Free Tier."
  type        = string
  default     = "db.t3.micro"
}

variable "db_allocated_storage" {
  description = "Allocated storage for the database (in GB). 20GB is typically the Free Tier limit."
  type        = number
  default     = 20
}

variable "db_name" {
  description = "Name for the database schema/database."
  type        = string
  default     = "db2"
}

variable "db_username" {
  description = "Master username for the database. Use Secrets Manager in production."
  type        = string
  default     = "astrolearner"
}

variable "db_password" {
  description = "Master password for the database. Use Secrets Manager in production. **CHANGE THIS DEFAULT**"
  type        = string
  default     = "password"
  sensitive   = true
}

variable "db_port" {
  description = "Port for the database (e.g., 3306 for MySQL, 5432 for PostgreSQL)."
  type        = number
  default     = 5432
}


variable "ami_id" {
  description = "The AMI ID for the EC2 instances. Choose a Free Tier eligible Amazon Linux 2 or 2023 AMI for your region."
  type        = string
  default     = "ami-0b86aaed8ef90e45f"
}

variable "instance_type" {
  description = "The EC2 instance type. Use t2.micro or t3.micro for Free Tier."
  type        = string
  default     = "t2.micro"
}

variable "key_pair_name" {
  description = "The name of the EC2 Key Pair you created in your AWS region for SSH access."
  type        = string
}

variable "frontend_health_check_path" {
  description = "HTTP path for the frontend service health check."
  type        = string
  default     = "/"
}

variable "backend_health_check_path" {
  description = "HTTP path for the backend service health check."
  type        = string
  default     = "/health"
}

variable "docker_image_frontend" {
  description = "Docker image for the frontend service"
  type        = string
  default = "omarbdoc/astrolearn-front:latest"
}

variable "docker_image_backend" {
  description = "Docker image for the backend service"
  type        = string
  default = "omarbdoc/astrolearn-back:latest"
}


variable "frontend_asg_min_size" {
  description = "Minimum number of frontend instances."
  type        = number
  default     = 1
}

variable "frontend_asg_max_size" {
  description = "Maximum number of frontend instances."
  type        = number
  default     = 2
}

variable "frontend_asg_desired_capacity" {
  description = "Desired number of frontend instances to start with."
  type        = number
  default     = 1
}

variable "backend_asg_min_size" {
  description = "Minimum number of backend instances."
  type        = number
  default     = 1
}

variable "backend_asg_max_size" {
  description = "Maximum number of backend instances."
  type        = number
  default     = 2
}

variable "backend_asg_desired_capacity" {
  description = "Desired number of backend instances to start with."
  type        = number
  default     = 1
}
