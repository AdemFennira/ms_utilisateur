#!/bin/bash

set -e

MICROSERVICE_NAME=$1
NAMESPACE=$2
IMAGE_URL=$3
IMAGE_TAG=$4
REPLICAS=${5:-1}
VERSION=${6:-"1.0.0"}

if [ -z "$MICROSERVICE_NAME" ] || [ -z "$NAMESPACE" ] || [ -z "$IMAGE_URL" ] || [ -z "$IMAGE_TAG" ]; then
  echo "Usage: $0 <microservice-name> <namespace> <image-url> <image-tag> [replicas] [version]"
  echo "Example: $0 smartdish-parent smartdish fra.ocir.io/tenancy/smartdish-parent v1.0.0 2 1.0.0"
  exit 1
fi

echo "Deploiement de ${MICROSERVICE_NAME} dans le namespace ${NAMESPACE}"
echo "Image: ${IMAGE_URL}:${IMAGE_TAG}"
echo "Replicas: ${REPLICAS}"

# Remplacer les variables dans le template
export MICROSERVICE_NAME
export NAMESPACE
export IMAGE_URL
export IMAGE_TAG
export REPLICAS
export VERSION

# Appliquer le déploiement
envsubst < k8s/oci/deployment-template.yaml | kubectl apply -f -

# Attendre que le déploiement soit prêt
kubectl rollout status deployment/${MICROSERVICE_NAME} -n ${NAMESPACE} --timeout=5m

echo "Deploiement termine avec succes !"

# Afficher les pods
echo ""
echo "Pods en cours d'execution:"
kubectl get pods -n ${NAMESPACE} -l app=${MICROSERVICE_NAME}

# Afficher le service
echo ""
echo "Service:"
kubectl get svc -n ${NAMESPACE} -l app=${MICROSERVICE_NAME}

# Afficher les URLs d'accès
if [ "$NAMESPACE" = "smartdish" ]; then
  DOMAIN="smartdish-integration.cloud"
else
  DOMAIN="smartdish.cloud"
fi

echo ""
echo "URL d'acces: https://soa-${MICROSERVICE_NAME}-${NAMESPACE}.${DOMAIN}"

