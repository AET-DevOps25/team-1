#!/bin/bash
# User data script for EC2 instances

yum update -y
yum install -y docker git

systemctl start docker
systemctl enable docker
usermod -a -G docker ec2-user

# Install Docker Compose (specific version for stability or use latest)
COMPOSE_VERSION="2.21.0"
curl -L "https://github.com/docker/compose/releases/download/v${COMPOSE_VERSION}/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
chmod +x /usr/local/bin/docker-compose

# Git clone application repository
APP_DIR="/home/ec2-user/app"
GIT_REPO_URL="https://github.com/${GITHUB_REPOSITORY}.git" # Assuming GITHUB_REPOSITORY is passed by Actions, or hardcode
GIT_BRANCH="${BRANCH_NAME:-main}" # Assuming BRANCH_NAME is passed or default to main

mkdir -p $APP_DIR
cd $APP_DIR
# For private repos, SSH agent forwarding or deploy keys would be needed from CI
# For public repos, this is fine.
# Consider using a deploy token for private repos if not using SSH.
# git clone --branch $GIT_BRANCH $GIT_REPO_URL .
# For now, we'll assume the code is either public or the CI handles auth
# Placeholder for actual git clone logic which needs a repo URL
echo "Cloning repository (placeholder)..." > $APP_DIR/clone_status.txt

# Create a .env file for Docker Compose
# Variables are templated by Terraform and passed to this script
cat > $APP_DIR/.env <<EOF
# Database Configuration
POSTGRES_HOST=${db_endpoint}
POSTGRES_PORT=5432
POSTGRES_DB=${db_name}
POSTGRES_USER=${db_user}
POSTGRES_PASSWORD='${db_password}' # Ensure password is appropriately quoted for .env

# Spring Profiles (if applicable for backend)
SPRING_PROFILES_ACTIVE=docker,aws

# Resource Prefix (for logging or internal use if needed)
RESOURCE_PREFIX=${resource_prefix}

# Service Enablement Flags (read by docker-compose.yml or entrypoints)
ENABLE_FRONTEND=${enable_frontend}
ENABLE_BACKEND=${enable_backend}
EOF

# Docker Compose Up
# The docker-compose.yml should be designed to conditionally start services
# based on ENABLE_FRONTEND and ENABLE_BACKEND environment variables.

# Example: Modify docker-compose.yml to use these flags, or pass them to service entrypoints.
# For simplicity, this script assumes docker-compose.yml handles it.
# If not, you might need to dynamically generate a docker-compose override file.

# Check which services to run based on flags
SERVICES_TO_RUN=""
if [ "${enable_frontend}" = "true" ]; then
  # Assuming 'frontend' is a service name in docker-compose.yml
  # This requires your docker-compose.yml to be structured to allow selective service startup.
  # A more robust way is to have separate compose files or profiles.
  echo "Frontend deployment enabled by Terraform variable."
  # For now, we assume docker-compose up will run all and app entrypoints might control behavior
  # or specific compose files are chosen by Ansible/CI later.
fi

if [ "${enable_backend}" = "true" ]; then
  echo "Backend deployment enabled by Terraform variable."
fi

# This is a simplified approach. Ideally, your docker-compose.yml would define profiles
# or your entrypoint scripts inside containers would check these env vars.
# For now, we run all services defined in the main docker-compose.yml
# and expect the application or CI/Ansible to handle specifics if needed.
# If you want to strictly run only certain services, you'd typically do:
# docker-compose up -d service1 service2
# But this requires knowing service names and modifying the compose command.

echo "Starting services based on docker-compose.yml..." >> $APP_DIR/clone_status.txt
# If the git clone is not done here, the docker-compose file won't exist.
# This script currently assumes the docker-compose.yml is part of the repo.
# docker-compose -f $APP_DIR/docker-compose.yml up -d

# Setup a systemd service to manage the application stack
cat > /etc/systemd/system/team1-app.service << EOF
[Unit]
Description=Team 1 HR Application Stack (${resource_prefix})
After=docker.service
Requires=docker.service

[Service]
Type=oneshot
RemainAfterExit=yes
WorkingDirectory=$APP_DIR
# EnvironmentFile=$APP_DIR/.env # Systemd can also load env files directly
ExecStart=/usr/local/bin/docker-compose up -d
ExecStop=/usr/local/bin/docker-compose down
TimeoutStartSec=0
StandardOutput=journal
StandardError=journal

[Install]
WantedBy=multi-user.target
EOF

systemctl daemon-reload
# systemctl enable team1-app.service # Enable to start on boot
# systemctl start team1-app.service  # Start immediately

echo "User data script completed." > /home/ec2-user/user_data_finished.txt
# Add a final reboot for all settings to take effect, especially usermod for docker group
# reboot # Use with caution, might interrupt Terraform provisioning if not handled carefully 