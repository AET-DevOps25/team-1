#!/bin/bash

# Build image and start services
docker-compose -f docker-compose-dev.yml up --build -d

# Optional: Print database connection information
echo "PostgreSQL is running at: localhost:5432"
echo "Database: hrapp | User: postgres | Password: postgres"
