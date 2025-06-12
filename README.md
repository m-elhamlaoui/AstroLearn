# AstroLearn: Collaborative Space Exploration Hub


## Table of Contents
- [Project Overview](#project-overview)
- [Core Features](#core-features)
- [Technical Architecture](#technical-architecture)
- [Domain Model](#domain-model)
- [DevOps Approach](#devops-approach)
  - [Local Development Environment](#local-development-environment)
  - [Cloud Deployment (AWS)](#cloud-deployment-aws)
- [Monitoring & Observability](#monitoring--observability)
- [CI/CD Pipeline](#cicd-pipeline)
- [Development Setup](#development-setup)
- [Deployment Instructions](#deployment-instructions)

## Project Overview
AstroLearn is a comprehensive platform designed for space enthusiasts to explore, learn, and contribute to the space exploration community. The application provides a rich set of features including user-generated content, interactive learning resources, and AI-powered assistance to create an engaging and educational experience.

## Video Demo

[Video Demo Link Google Drive](https://drive.google.com/file/d/1XZG86qxy7qdZBGmnuUAa331CfoFcctBf/view?usp=drive_link)


## Core Features

### User Management
- **Authentication System**: Secure JWT-based authentication with token refresh capabilities
- **User Profiles**: Customizable profiles with interests, achievements, and contribution history
- **Role-Based Access Control**: Different permission levels for regular users, content creators, and administrators
- **Verification System**: Three-tier verification status (Unverified, Pending, Verified) with admin approval process
- **Experience & Leveling System**: Users earn experience points for platform activities and progress through ranks:
  - Novice (0 XP) → Explorer (1,000 XP) → Astronaut (5,000 XP) → Galactic (10,000 XP)

### Content Management

#### Articles System
- **Creation & Publishing**: Rich text editor with support for images, videos, and formatting
- **Content Moderation**: AI-powered verification system to ensure quality and relevance
- **Interaction**: Voting (upvote/downvote), commenting, and sharing functionality
- **Search & Filter**: Advanced search with filters for topics, authors, and popularity
- **Reading History**: Tracks user engagement with articles for personalized recommendations

#### Courses Platform
- **Structured Learning Paths**: Multi-module courses with progressive difficulty levels
- **Interactive Content**: Video lessons, quizzes, and practical exercises
- **Progress Tracking**: Course completion metrics and achievement badges
- **Quiz System**: Interactive assessments with immediate feedback and scoring

### Interactive Features

#### Space Events Calendar
- **Mission Tracking**: Comprehensive database of past, current, and upcoming space missions
- **Interactive Timeline**: Visual representation of space exploration history
- **Mission Management**: Only verified users can add or edit mission information
- **Status Tracking**: Missions categorized as Upcoming, In-Progress, Completed, or Failed

#### AI Chatbot Assistant


## Technical Architecture


### Frontend
- **Framework**: Next.js with React for a responsive single-page application
- **State Management**: React Context API and custom hooks
- **UI Components**: Custom component library with responsive design
- **API Integration**: Axios for RESTful API communication with the backend
- **Containerization**: Docker image built and stored in Docker Hub

### Backend
- **Framework**: Spring Boot for robust API development
- **API Design**: RESTful architecture with comprehensive endpoint documentation
- **Security**: JWT authentication, CORS configuration, and input validation
- **Business Logic**: Service-oriented architecture with clear separation of concerns
- **Metrics**: Spring Actuator with Micrometer for Prometheus integration
- **Containerization**: Docker image built and stored in Docker Hub

### Database
- **RDBMS**: PostgreSQL for structured data storage
- **Schema Design**: Normalized database schema with efficient relationships
- **Data Access**: JPA/Hibernate for object-relational mapping
- **Performance**: Optimized queries and indexing for fast data retrieval
- **Persistence**: Kubernetes StatefulSet with persistent volume claims

### Media Storage
- **Cloud Storage**: AWS S3 integration for storing images and media files

## Domain Model

<div align="center">
  <img src="Diagrams/class diagram.png" alt="AstroLearn Domain Model" width="800"/>
  <p><i>AstroLearn Domain Model Class Diagram</i></p>
</div>

The application is structured around four main domains:

### Content Domain
- **Article**: Core content entity with title, summary, content, and image URLs
- **ArticleVote**: Represents user votes (upvote/downvote) on articles, replacing the traditional rating system
- **ArticleTag**: Tags for categorizing articles
- **Comment**: User comments on articles

### Learning Domain
- **Course**: Structured learning paths with multiple modules
- **Module**: Organizational units containing lessons
- **Lesson**: Individual learning units with content and videos
- **Quiz**: Assessments with questions and experience rewards
- **QuizQuestion**: Individual questions within quizzes
- **QuizCompletion**: Records of user quiz attempts and scores

### User Domain
- **User**: Core user entity with authentication, profile, and experience tracking
- **ReadingHistory**: Tracks user engagement with articles

### Mission Domain
- **SpaceMission**: Information about space missions with status tracking

## DevOps Approach

AstroLearn implements a comprehensive DevOps strategy with two deployment paths: local deployment and cloud deployment on AWS.

### Local deployment 

<div align="center">
  <img src="Diagrams/architecture application devops local.drawio.png" alt="Local DevOps Architecture" width="800"/>
  <p><i>Local Deployment Architecture</i></p>
</div>

The local development environment leverages Docker Desktop with Kubernetes for containerization and orchestration:

#### Infrastructure Components
- **Kubernetes**: Container orchestration platform running on Docker Desktop
- **Docker**: Containerization of application components
- **Jenkins**: CI/CD automation server for build and deployment pipelines

#### Deployment Architecture
- **Frontend**: Next.js application deployed as a Kubernetes Deployment with NodePort service
- **Backend**: Spring Boot application deployed as a Kubernetes Deployment with NodePort service
- **Database**: PostgreSQL deployed as a StatefulSet with persistent storage

#### Monitoring Stack
- **Prometheus**: Time-series database for metrics collection
- **Grafana**: Visualization and dashboarding platform
- **Node Exporter**: System metrics collection
- **Postgres Exporter**: Database metrics collection

<div align="center">
  <img src="Diagrams/prometheus targets.png" alt="Prometheus Targets" width="600"/>
  <p><i>Prometheus Monitoring Targets</i></p>
</div>

<div align="center">
  <img src="Diagrams/grafana dashboards.png" alt="Grafana Dashboards" width="600"/>
  <p><i>Grafana Monitoring Dashboards</i></p>
</div>

<div align="center">
  <img src="Diagrams/spring boot metrics dashboard.png" alt="Spring Boot Metrics" width="600"/>
  <p><i>Spring Boot Application Metrics Dashboard</i></p>
</div>

### Cloud Deployment (AWS)

<div align="center">
  <img src="Diagrams/schema deployment .jpg" alt="AWS Deployment Architecture" width="700"/>
  <p><i>AWS Three-Tier Architecture Deployment</i></p>
</div>

The AstroLearn cloud deployment leverages a Terraform-based approach for a secure, scalable web application on AWS, following best practices for isolation, modularity, and high availability.

#### Architecture Breakdown

**Network Layer:**
-  Custom VPC, public/private subnets, multi-AZ, route tables, internet gateway.

**Frontend Tier:**
- ASG + internet-facing ALB, Dockerized EC2 instances, health checks.

**Backend Tier:**
- Internal ALB, isolated ASG, containerized EC2, no internet access.

**Data Tier:**
- Amazon RDS (MySQL/PostgreSQL), private subnets, single-AZ (cost-optimized).


#### Security
- Tier-isolated security groups (frontend ↔ backend ↔ database)
- No direct database or backend internet access
- Open SSH for debugging (needs restriction for production).

#### Scalability & Availability
- Independent auto scaling per tier
- Cross-AZ load balancing
- RDS backups and health checks for fault tolerance
#### Deployment Tools
- **Terraform**: Infrastructure as Code for AWS resource provisioning
- **GitHub Actions**: CI/CD pipeline automation


#### Advantages
- **Scalable & Performant**: Load balancing, auto scaling, Docker deployments
- **Secure**: Private networking, least privilege access
- **Cost-Effective**: Free-tier resources, optional spot instances, dev-optimized RDS
- **Maintainable**: Modular Terraform code, versioning, consistent deployments

## CI/CD Pipeline

<div align="center">
  <img src="Diagrams/jenkins pipeline overview.png" alt="Jenkins Pipeline Overview" width="700"/>
  <p><i>Jenkins CI/CD Pipeline Overview</i></p>
</div>

<div align="center">
  <img src="Diagrams/jenkins pipeline.png" alt="Jenkins Pipeline Detail" width="700"/>
  <p><i>Jenkins Pipeline Execution Detail</i></p>
</div>

AstroLearn implements a robust CI/CD pipeline using Jenkins for the local environment. The pipeline is defined in a Jenkinsfile with declarative syntax and includes the following stages:

### Jenkins Pipeline Stages

1. **Checkout**:
   - Retrieves the latest code from the Git repository (production branch)
   - Uses GitHub credentials configured in Jenkins

2. **Build App**:
   - Compiles the Spring Boot backend application using Maven Wrapper
   - Packages the application JAR file (skipping tests at this stage)

3. **Deploy PostgreSQL**:
   - Applies Kubernetes manifests for PostgreSQL database
   - Sets up port-forwarding to make the database accessible
   - Ensures the database is ready before proceeding

4. **Run Unit Tests**:
   - Executes backend unit tests using Maven
   - Publishes test results using JUnit

5. **Build Docker Image**:
   - Creates Docker image for the backend application
   - Tags the image with build number and latest tag

6. **Push Docker Image**:
   - Authenticates with Docker Hub using credentials stored in Jenkins
   - Pushes the backend image to Docker Hub repository

7. **Verify K8s Access**:
   - Confirms access to the Kubernetes cluster
   - Checks available nodes before deployment

8. **Deploy Backend to K8s**:
   - Applies Kubernetes manifests for the backend service
   - Performs a rolling restart to ensure the latest image is used
   - Waits for successful deployment

9. **Build Frontend Docker Image**:
   - Creates Docker image for the Next.js frontend
   - Configures environment variables for API connectivity

10. **Push Frontend Docker Image**:
    - Pushes the frontend image to Docker Hub repository

11. **Deploy Frontend to K8s**:
    - Applies Kubernetes manifests for the frontend service
    - Verifies deployment status

12. **Verify Deployment**:
    - Retrieves NodePort information for services
    - Provides access URLs for the deployed application

<div align="center">
  <img src="Diagrams/docker hub repositories.png" alt="Docker Hub Repositories" width="600"/>
  <p><i>Docker Hub Repositories for AstroLearn</i></p>
</div>

### Pipeline Configuration

The Jenkins pipeline is configured with:
- Windows-specific commands using batch scripts
- Kubernetes configuration using local kubeconfig
- Docker Hub credentials for image publishing
- Post-build cleanup actions

## Monitoring & Observability

AstroLearn implements a focused monitoring solution using Prometheus and Grafana specifically for backend services and Kubernetes infrastructure:

### Metrics Collection
- **Backend Metrics**: Spring Boot Actuator with Micrometer for exposing JVM and application metrics
- **Kubernetes Metrics**: kube-state-metrics for cluster state monitoring

### Visualization
- **Grafana Dashboards**: Pre-configured dashboards for Spring Boot and Kubernetes
- **Prometheus**: Time-series database for metrics storage and querying

### Key Monitored Metrics
- **Backend**: JVM memory usage, garbage collection, HTTP request rates and latencies
- **Kubernetes**: Pod status, resource utilization, deployment health, node metrics

The monitoring stack is deployed using the kube-prometheus-stack Helm chart with customized values to target the Spring Boot backend application.

## Development Setup

### Prerequisites
- Java 17 or higher
- Node.js 16 or higher
- PostgreSQL 13 or higher
- Maven 3.8 or higher
- Docker Desktop with Kubernetes enabled
- Helm 3.x

### Backend Setup
```bash
# Navigate to backend directory
cd Backend/demo

# Build the application
./mvnw clean package

# Run the application locally
./mvnw spring-boot:run
```

### Frontend Setup
```bash
# Navigate to frontend directory
cd Frontend

# Install dependencies
npm install

# Run development server
npm run dev
```

### Docker Compose Setup
```bash
# Run the entire application stack with Docker Compose
docker-compose up -d

# View logs
docker-compose logs -f

# Stop the application
docker-compose down
```

The Docker Compose setup will start the following services:
- PostgreSQL database
- Spring Boot backend
- Next.js frontend
All services are configured with appropriate environment variables and network settings for seamless integration.

## Deployment Instructions

### Local Kubernetes Deployment

```bash
# Deploy PostgreSQL database
kubectl apply -f k8s/postgres.yaml

# Deploy Backend application
kubectl apply -f k8s/backend.yaml

# Deploy Frontend application
kubectl apply -f k8s/frontend.yaml

# Deploy Monitoring stack
helm install monitoring prometheus-community/kube-prometheus-stack -f k8s/monitoring/prometheus-stack-values.yaml
```

