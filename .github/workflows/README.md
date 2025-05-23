# GitHub Actions CI/CD Pipeline for AstroLearn

This document explains how to use the CI/CD pipeline set up with GitHub Actions.

## Pipeline Overview

The CI/CD pipeline automates the following processes:

1. **Build**: Builds Docker images for frontend and backend services
2. **Test**: Runs tests for both frontend and backend
3. **Push**: Pushes Docker images to Docker Hub registry
4. **Deploy**: Deploys the application using Terraform to AWS

## Required Secrets

To use this pipeline, you need to add the following secrets to your GitHub repository:

### Docker Hub Credentials
- `DOCKERHUB_USERNAME`: Your Docker Hub username
- `DOCKERHUB_TOKEN`: Your Docker Hub access token (not your password)

### AWS Credentials
- `AWS_ACCESS_KEY_ID`: Your AWS access key ID
- `AWS_SECRET_ACCESS_KEY`: Your AWS secret access key
- `AWS_REGION`: The AWS region to deploy to (e.g., us-east-1)

## How to Add Secrets to GitHub

1. Go to your GitHub repository
2. Click on "Settings"
3. Click on "Secrets and variables" in the left sidebar
4. Click on "Actions"
5. Click on "New repository secret"
6. Add each of the required secrets

## Workflow Triggers

The pipeline is triggered on:
- Push to main or master branch
- Pull requests to main or master branch
- Manual trigger via GitHub Actions interface

## Customization

You can customize the pipeline by editing the `.github/workflows/ci-cd-pipeline.yml` file. Common customizations include:

- Changing the Docker image names
- Adding more test steps
- Modifying the deployment configuration
- Adding notifications (e.g., Slack, email)

## Terraform Variables

The pipeline passes the following variables to Terraform:
- AWS credentials
- AWS region
- Docker image names with their tags

Make sure your Terraform configuration accepts these variables.
