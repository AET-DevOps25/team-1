#!/bin/bash

OLLAMA_BASE_URL=""
OLLAMA_MODEL=""
OLLAMA_API_KEY=""

# paste key as one line
echo "JWT_PRIV_KEY" > priv.pem
echo "JWT_PUB_KEY"  > pub.pem

DISCORD_WEBHOOK=""

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

helm dependency update ./helm/aihr
tar -xvzf helm/aihr/charts/grafana-9.2.10.tgz
rm helm/aihr/charts/grafana-9.2.10.tgz

cp -r ./dashboards/* ./helm/aihr/charts/grafana/dashboards/

helm upgrade --install ai-hr-dev ./helm/aihr \
  --namespace ai-hr-dev --create-namespace \
  -f "${SCRIPT_DIR}/helm/aihr/values-dev.yaml" \
  -f "${SCRIPT_DIR}/helm/aihr/values-dev-prometheus.yaml" \
  -f "${SCRIPT_DIR}/helm/aihr/values-dev-grafana.yaml" \
  -f "${SCRIPT_DIR}/helm/aihr/values-dev-loki.yaml" \
  -f "${SCRIPT_DIR}/helm/aihr/values-dev-promtail.yaml" \
  -f "${SCRIPT_DIR}/helm/aihr/values-dev-frontend.yaml" \
  -f "${SCRIPT_DIR}/helm/aihr/values-common-loki.yaml" \
  -f "${SCRIPT_DIR}/helm/aihr/values-common-promtail.yaml" \
  -f "${SCRIPT_DIR}/helm/aihr/values-common-prometheus.yaml" \
  -f "${SCRIPT_DIR}/helm/aihr/values-common-grafana.yaml" \
  -f "${SCRIPT_DIR}/helm/aihr/values-common-discord-alert.yaml" \
  --set discordWebhook.url="${DISCORD_WEBHOOK}" \
  --set global.ghcrUser=aet-devops25 \
  --set global.ghcrRepo=team-1 \
  --set global.imageTag="$(git rev-parse --short HEAD)" \
  --set-file global.jwt.privateKey=priv.pem \
  --set-file global.jwt.publicKey=pub.pem \
  --set global.ollama.ollamaBaseUrl="${OLLAMA_BASE_URL}" \
  --set global.ollama.ollamaModel="${OLLAMA_MODEL}" \
  --set global.ollama.ollamaApiKey="${OLLAMA_API_KEY}" \
  --wait --timeout 15m

helm upgrade --install ai-hr-prod ./helm/aihr \
  --namespace ai-hr --create-namespace \
  -f "${SCRIPT_DIR}/helm/aihr/values-prod.yaml" \
  -f "${SCRIPT_DIR}/helm/aihr/values-prod-prometheus.yaml" \
  -f "${SCRIPT_DIR}/helm/aihr/values-prod-grafana.yaml" \
  -f "${SCRIPT_DIR}/helm/aihr/values-prod-loki.yaml" \
  -f "${SCRIPT_DIR}/helm/aihr/values-prod-promtail.yaml" \
  -f "${SCRIPT_DIR}/helm/aihr/values-prod-frontend.yaml" \
  -f "${SCRIPT_DIR}/helm/aihr/values-common-loki.yaml" \
  -f "${SCRIPT_DIR}/helm/aihr/values-common-promtail.yaml" \
  -f "${SCRIPT_DIR}/helm/aihr/values-common-prometheus.yaml" \
  -f "${SCRIPT_DIR}/helm/aihr/values-common-grafana.yaml" \
  -f "${SCRIPT_DIR}/helm/aihr/values-common-discord-alert.yaml" \
  --set discordWebhook.url="${DISCORD_WEBHOOK}" \
  --set global.ghcrUser=aet-devops25 \
  --set global.ghcrRepo=team-1 \
  --set global.imageTag="$(git rev-parse --short HEAD)" \
  --set-file global.jwt.privateKey=priv.pem \
  --set-file global.jwt.publicKey=pub.pem \
  --set global.ollama.ollamaBaseUrl="${OLLAMA_BASE_URL}" \
  --set global.ollama.ollamaModel="${OLLAMA_MODEL}" \
  --set global.ollama.ollamaApiKey="${OLLAMA_API_KEY}" \
  --wait