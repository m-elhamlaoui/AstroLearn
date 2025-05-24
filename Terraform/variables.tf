# --- AWS Credentials ---
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

# --- AWS Region ---
variable "region" {
  description = "AWS region to deploy resources into."
  type        = string
  default     = "us-east-1" # N. Virginia often has good Free Tier availability.
}

# --- Network (VPC) ---
variable "vpc_cidr" {
  description = "CIDR block for the VPC."
  type        = string
  default     = "10.0.0.0/16" # Standard private range.
}

variable "availability_zones" {
  description = "List of availability zones to use for public and private subnets."
  type        = list(string)
  # Ensure these AZs are available in your selected region. 2 AZs are required for RDS Subnet Group.
  default     = ["us-east-1a", "us-east-1b"]
}

# --- Application Ports ---
variable "frontend_app_port" {
  description = "The port your frontend application container listens on."
  type        = number
  default     = 3000 # Matches frontend Dockerfile EXPOSE.
}

variable "backend_app_port" {
  description = "The port your backend application container listens on."
  type        = number
  default     = 8088 # Example Node.js/Java port. Adjust based on your backend.
}

# --- Database (RDS) ---
variable "db_engine" {
  description = "Database engine for RDS (e.g., 'mysql', 'postgres')."
  type        = string
  default     = "postgres" # PostgreSQL is often Free Tier eligible.
}

variable "db_engine_version" {
  description = "Database engine version. Verify Free Tier eligibility for this version in your region."
  type        = string
  default     = "17.4" # Updated to a supported PostgreSQL version
}

variable "db_instance_class" {
  description = "DB instance class for RDS. Use db.t2.micro or db.t3.micro for Free Tier."
  type        = string
  default     = "db.t3.micro" # Free Tier eligible instance class.
}

variable "db_allocated_storage" {
  description = "Allocated storage for the database (in GB). 20GB is typically the Free Tier limit."
  type        = number
  default     = 20 # Free Tier eligible storage amount (General Purpose SSD gp2/gp3).
}

variable "db_name" {
  description = "Name for the database schema/database."
  type        = string
  default     = "db2" # Your database name.
}

variable "db_username" {
  description = "Master username for the database. Use Secrets Manager in production."
  type        = string
  default     = "astrolearner" # Example username.
}

variable "db_password" {
  description = "Master password for the database. Use Secrets Manager in production. **CHANGE THIS DEFAULT**"
  type        = string
  default     = "password" # **CHANGE THIS DEFAULT PASSWORD IMMEDIATELY**
  sensitive   = true # Marks this variable as sensitive.
}

variable "db_port" {
  description = "Port for the database (e.g., 3306 for MySQL, 5432 for PostgreSQL)."
  type        = number
  default     = 5432 # Default port for PostgreSQL. Change if using MySQL (3306).
}


# --- EC2 Instances (used by Launch Templates) ---
variable "ami_id" {
  description = "The AMI ID for the EC2 instances. Choose a Free Tier eligible Amazon Linux 2 or 2023 AMI for your region."
  type        = string
  # IMPORTANT: Verify this AMI ID is valid for your selected region (us-east-1) and architecture (x86_64).
  # Find AMIs in the AWS console (EC2 -> AMIs, filter by 'Free tier eligible' and 'Amazon Linux').
  # Example for us-east-1, Amazon Linux 2023 (x86_64): ami-053b0a9e2d60c5c97
  default     = "ami-0b86aaed8ef90e45f" # <-- VERIFY THIS is a valid Free Tier AMI in us-east-1 (x86_64)
}

variable "instance_type" {
  description = "The EC2 instance type. Use t2.micro or t3.micro for Free Tier."
  type        = string
  default     = "t2.micro" # Free Tier eligible instance type (check eligibility for t3.micro).
}

variable "key_pair_name" {
  description = "The name of the EC2 Key Pair you created in your AWS region for SSH access."
  type        = string
  # default     = "your-key-pair-name-here" # <-- **MUST CHANGE THIS to your actual key pair name**
}

# --- Health Checks ---
variable "frontend_health_check_path" {
  description = "HTTP path for the frontend service health check."
  type        = string
  default     = "/" # Common default.
}

variable "backend_health_check_path" {
  description = "HTTP path for the backend service health check."
  type        = string
  default     = "/health" # Common default, adjust if needed.
}

# --- Docker Images ---
variable "docker_image_frontend" {
  description = "Docker image for the frontend service"
  type        = string
}

variable "docker_image_backend" {
  description = "Docker image for the backend service"
  type        = string
}


# --- Auto Scaling Group Sizes ---
variable "frontend_asg_min_size" {
  description = "Minimum number of frontend instances."
  type        = number
  default     = 1 # Keep minimum at 1 for availability.
}

variable "frontend_asg_max_size" {
  description = "Maximum number of frontend instances."
  type        = number
  default     = 2 # Set max to 1 to minimize potential costs beyond Free Tier hours.
}

variable "frontend_asg_desired_capacity" {
  description = "Desired number of frontend instances to start with."
  type        = number
  default     = 1 # Match min/max for cost control.
}

variable "backend_asg_min_size" {
  description = "Minimum number of backend instances."
  type        = number
  default     = 1 # Keep minimum at 1 for availability.
}

variable "backend_asg_max_size" {
  description = "Maximum number of backend instances."
  type        = number
  default     = 2 # Set max to 1 to minimize potential costs beyond Free Tier hours.
}

variable "backend_asg_desired_capacity" {
  description = "Desired number of backend instances to start with."
  type        = number
  default     = 1 # Match min/max for cost control.
}

# --- User Data Files ---
# These variables aren't directly used but represent required files.
# Ensure 'user_data_frontend.sh' and 'user_data_backend.sh' exist in the same directory.
# variable "user_data_frontend_script" {
#   description = "Path to the frontend user data script (informational)."
#   type        = string
#   default     = "user_data_frontend.sh"
# }
# variable "user_data_backend_script" {
#   description = "Path to the backend user data script (informational)."
#   type        = string
#   default     = "user_data_backend.sh"
# }
