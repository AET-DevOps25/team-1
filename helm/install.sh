helm upgrade --install ai-hr-dev ./helm/aihr \
  --namespace ai-hr-dev --create-namespace \
  -f helm/aihr/values-dev.yaml \
  --set global.ghcrUser=aet-devops25 \
  --set global.ghcrRepo=team-1 \
  --set global.imageTag=$(git rev-parse --short HEAD) \
  --set global.db.username=${DB_USER} # export DB_USER=postgres

helm upgrade --install ai-hr-prod ./helm/aihr \
  --namespace ai-hr --create-namespace \
  -f helm/aihr/values-prod.yaml \
  --set global.ghcrUser=aet-devops25 \
  --set global.ghcrRepo=team-1 \
  --set global.imageTag=$(git rev-parse --short HEAD) \
  --set global.db.username=postgres