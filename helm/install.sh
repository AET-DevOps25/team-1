helm upgrade --install aihr-dev ./helm/aihr \
  --namespace aihr-dev --create-namespace \
  -f helm/aihr/values-dev.yaml \
  --set global.ghcrUser=AET-DevOps25 \
  --set global.ghcrRepo=team-1 \
  --set global.imageTag=$(git rev-parse --short HEAD) \
  --set global.db.username=${DB_USER} # export DB_USER=postgres

helm upgrade --install aihr-prod ./helm/aihr \
  --namespace aihr --create-namespace \
  -f helm/aihr/values-prod.yaml \
  --set global.ghcrUser=aet-devops25 \
  --set global.ghcrRepo=team-1 \
  --set global.imageTag=$(git rev-parse --short HEAD) \
  --set global.db.username=postgres