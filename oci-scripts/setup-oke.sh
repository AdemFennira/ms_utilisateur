#!/bin/bash

set -e

echo "============================================"
echo "   Configuration OKE SmartDish"
echo "============================================"
echo ""

# Variables
CLUSTER_ID="ocid1.cluster.oc1.eu-paris-1.aaaaaaaaafkhi5vnahyc14ozq2ulnwstf3t6hslnvmomgeq5ulqsvca3gwzgw3tq"
REGION="eu-paris-1"
NAMESPACE_INTEGRATION="smartdish"
NAMESPACE_PRODUCTION="smartdish-prod"

echo "Vérification des prérequis..."

# Vérifier oci cli
if ! command -v oci &> /dev/null; then
    echo "❌ OCI CLI n'est pas installé"
    echo "Installez-le depuis: https://docs.oracle.com/en-us/iaas/Content/API/SDKDocs/cliinstall.htm"
    exit 1
fi
echo "✓ OCI CLI installé"

# Vérifier kubectl
if ! command -v kubectl &> /dev/null; then
    echo "❌ kubectl n'est pas installé"
    echo "Installez-le depuis: https://kubernetes.io/docs/tasks/tools/"
    exit 1
fi
echo "✓ kubectl installé"

# Vérifier helm
if ! command -v helm &> /dev/null; then
    echo "❌ Helm n'est pas installé"
    echo "Installez-le depuis: https://helm.sh/docs/intro/install/"
    exit 1
fi
echo "✓ Helm installé"

echo ""
echo "Configuration du kubeconfig..."

# Créer le dossier .kube si nécessaire
mkdir -p ~/.kube

# Obtenir le kubeconfig
oci ce cluster create-kubeconfig \
  --cluster-id ${CLUSTER_ID} \
  --file ~/.kube/config \
  --region ${REGION} \
  --token-version 2.0.0 \
  --overwrite

echo "✓ Kubeconfig configuré"

# Tester la connexion
echo ""
echo "Test de connexion au cluster..."
kubectl cluster-info
kubectl get nodes

echo ""
echo "============================================"
echo "   Installation des composants Kubernetes"
echo "============================================"
echo ""

# Créer les namespaces
echo "Création des namespaces..."
kubectl apply -f k8s/oci/namespace.yaml
echo "✓ Namespaces créés"

# Installer NGINX Ingress Controller
echo ""
echo "Installation de NGINX Ingress Controller..."
if kubectl get namespace ingress-nginx &> /dev/null; then
    echo "⚠️  NGINX Ingress déjà installé"
else
    kubectl apply -f https://raw.githubusercontent.com/kubernetes/ingress-nginx/controller-v1.9.4/deploy/static/provider/cloud/deploy.yaml
    echo "✓ NGINX Ingress installé"
    echo "Attente que le LoadBalancer soit prêt..."
    kubectl wait --namespace ingress-nginx \
      --for=condition=ready pod \
      --selector=app.kubernetes.io/component=controller \
      --timeout=120s
fi

# Installer Cert-Manager
echo ""
echo "Installation de Cert-Manager..."
if kubectl get namespace cert-manager &> /dev/null; then
    echo "⚠️  Cert-Manager déjà installé"
else
    kubectl apply -f https://github.com/cert-manager/cert-manager/releases/download/v1.13.0/cert-manager.yaml
    echo "✓ Cert-Manager installé"
    echo "Attente que Cert-Manager soit prêt..."
    kubectl wait --namespace cert-manager \
      --for=condition=ready pod \
      --selector=app.kubernetes.io/instance=cert-manager \
      --timeout=120s
fi

# Configurer les secrets (interactif)
echo ""
echo "============================================"
echo "   Configuration des secrets"
echo "============================================"
echo ""

read -p "Host MySQL (ex: mysql.example.com): " MYSQL_HOST
read -p "User MySQL (ex: admin): " MYSQL_USER
read -sp "Password MySQL: " MYSQL_PASSWORD
echo ""
read -sp "Root Password MySQL: " MYSQL_ROOT_PASSWORD
echo ""

# Créer les secrets MySQL
echo ""
echo "Création des secrets MySQL..."
export MYSQL_HOST
export MYSQL_USER
export MYSQL_PASSWORD
export MYSQL_ROOT_PASSWORD

envsubst < k8s/oci/mysql-secrets.yaml | kubectl apply -f -
echo "✓ Secrets MySQL créés"

# Créer les ConfigMaps
echo ""
echo "Création des ConfigMaps..."
kubectl apply -f k8s/oci/configmap.yaml
echo "✓ ConfigMaps créés"

# Obtenir l'IP du LoadBalancer
echo ""
echo "============================================"
echo "   Configuration DNS"
echo "============================================"
echo ""

echo "Récupération de l'IP du LoadBalancer..."
LB_IP=""
for i in {1..10}; do
    LB_IP=$(kubectl get svc -n ingress-nginx ingress-nginx-controller -o jsonpath='{.status.loadBalancer.ingress[0].ip}' 2>/dev/null || echo "")
    if [ -n "$LB_IP" ]; then
        break
    fi
    echo "Attente de l'IP du LoadBalancer... ($i/10)"
    sleep 10
done

if [ -z "$LB_IP" ]; then
    echo "⚠️  L'IP du LoadBalancer n'est pas encore disponible"
    echo "Exécutez plus tard: kubectl get svc -n ingress-nginx ingress-nginx-controller"
else
    echo "✓ LoadBalancer IP: ${LB_IP}"
    echo ""
    echo "Configurez vos DNS avec cette IP:"
    echo "  *.smartdish-integration.cloud → ${LB_IP}"
    echo "  *.smartdish.cloud → ${LB_IP}"
    echo ""
    echo "Pour tester sans DNS, utilisez:"
    echo "  soa-api.${LB_IP}.nip.io"
fi

# Afficher les informations pour GitHub Secrets
echo ""
echo "============================================"
echo "   Secrets GitHub à configurer"
echo "============================================"
echo ""

echo "1. OCI_KUBECONFIG (base64):"
echo "-------------------------------------------"
cat ~/.kube/config | base64 -w 0
echo ""
echo "-------------------------------------------"
echo ""

echo "2. Autres secrets à configurer manuellement:"
echo "   - OCI_USERNAME: <tenancy-namespace>/<username>"
echo "   - OCI_AUTH_TOKEN: <token>"
echo "   - OCI_TENANCY_NAMESPACE: <namespace>"
echo "   - MYSQL_HOST: ${MYSQL_HOST}"
echo "   - MYSQL_USER: ${MYSQL_USER}"
echo "   - MYSQL_PASSWORD: <password>"
echo "   - MYSQL_ROOT_PASSWORD: <root-password>"
echo ""

echo "============================================"
echo "   Configuration terminée !"
echo "============================================"
echo ""
echo "Prochaines étapes:"
echo "1. Configurez les secrets GitHub"
echo "2. Configurez les DNS ou utilisez nip.io"
echo "3. Pushez votre code sur une branche feat/** ou fix/**"
echo "4. La CI/CD déploiera automatiquement sur Kubernetes"
echo ""

