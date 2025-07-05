#!/bin/bash

OLLAMA_BASE_URL=""
OLLAMA_MODEL=""
OLLAMA_API_KEY=""

# paste key as one line
private_key=""
public_key=""

helm upgrade --install ai-hr-dev ./helm/aihr \
  --namespace ai-hr-dev --create-namespace \
  -f helm/aihr/values-dev.yaml \
  --set global.ghcrUser=aet-devops25 \
  --set global.ghcrRepo=team-1 \
  --set global.imageTag="$(git rev-parse --short HEAD)" \
  --set-file global.jwt.privateKey="${private_key}" \
  --set-file global.jwt.publicKey="${public_key}" \
  --set charts.service-genai.env.ollamaBaseUrl="${OLLAMA_BASE_URL}" \
  --set charts.service-genai.env.ollamaModel="${OLLAMA_MODEL}" \
  --set charts.service-genai.env.ollamaApiKey="${OLLAMA_API_KEY}" \
  --wait --timeout 15m

helm upgrade --install ai-hr-prod ./helm/aihr \
  --namespace ai-hr --create-namespace \
  -f helm/aihr/values-prod.yaml \
  --set global.ghcrUser=aet-devops25 \
  --set global.ghcrRepo=team-1 \
  --set global.imageTag="$(git rev-parse --short HEAD)" \
  --set-file global.jwt.privateKey="${private_key}" \
  --set-file global.jwt.publicKey="${public_key}" \
  --set charts.service-genai.env.ollamaBaseUrl="${OLLAMA_BASE_URL}" \
  --set charts.service-genai.env.ollamaModel="${OLLAMA_MODEL}" \
  --set charts.service-genai.env.ollamaApiKey="${OLLAMA_API_KEY}" \
  --wait --timeout 15m

helm upgrade --install ai-hr-monitoring ./helm/monitor \
  --namespace ai-hr-monitoring --create-namespace \
  --wait --timeout 20m