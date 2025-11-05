#!/bin/bash
# Script d'installation rapide de l'infrastructure complète

set -e

echo "=========================================="
echo "Installation de l'Infrastructure Kubernetes"
echo "RecipeYouLove - CI/CD & Deployment"
echo "=========================================="
echo ""

# Vérification des prérequis
echo "1. Vérification des prérequis..."
command -v kubectl >/dev/null 2>&1 || { echo "kubectl n'est pas installé. Veuillez l'installer."; exit 1; }
command -v helm >/dev/null 2>&1 || { echo "helm n'est pas installé. Veuillez l'installer."; exit 1; }

echo "   kubectl: OK"
echo "   helm: OK"
echo ""

# Vérification de l'accès au cluster
echo "2. Vérification de l'accès au cluster Kubernetes..."
kubectl cluster-info > /dev/null 2>&1 || { echo "Impossible d'accéder au cluster Kubernetes."; exit 1; }
echo "   Cluster Kubernetes: OK"
echo ""

# Installation de NGINX Ingress Controller
echo "3. Installation de NGINX Ingress Controller..."
if kubectl get namespace ingress-nginx > /dev/null 2>&1; then
    echo "   NGINX Ingress déjà installé"
else
    kubectl apply -f https://raw.githubusercontent.com/kubernetes/ingress-nginx/controller-v1.9.4/deploy/static/provider/cloud/deploy.yaml
    kubectl wait --namespace ingress-nginx \
        --for=condition=ready pod \
        --selector=app.kubernetes.io/component=controller \
        --timeout=300s
    echo "   NGINX Ingress installé"
fi
echo ""

# Installation de cert-manager
echo "4. Installation de cert-manager (pour TLS)..."
if kubectl get namespace cert-manager > /dev/null 2>&1; then
    echo "   cert-manager déjà installé"
else
    kubectl apply -f https://github.com/cert-manager/cert-manager/releases/download/v1.13.2/cert-manager.yaml
    kubectl wait --namespace cert-manager \
        --for=condition=available \
        --timeout=300s \
        deployment/cert-manager
    echo "   cert-manager installé"
fi
echo ""

# Installation d'ArgoCD
echo "5. Installation d'ArgoCD..."
if kubectl get namespace argocd > /dev/null 2>&1; then
    echo "   ArgoCD déjà installé"
else
    kubectl create namespace argocd
    kubectl apply -n argocd -f https://raw.githubusercontent.com/argoproj/argo-cd/stable/manifests/install.yaml
    kubectl wait --namespace argocd \
        --for=condition=available \
        --timeout=600s \
        deployment/argocd-server
    echo "   ArgoCD installé"
fi

# Récupérer le mot de passe admin ArgoCD
ARGOCD_PASSWORD=$(kubectl -n argocd get secret argocd-initial-admin-secret -o jsonpath="{.data.password}" | base64 -d 2>/dev/null || echo "N/A")
echo ""

# Installation de Vault
echo "6. Installation de HashiCorp Vault..."
helm repo add hashicorp https://helm.releases.hashicorp.com > /dev/null 2>&1
helm repo update > /dev/null 2>&1

if kubectl get namespace vault > /dev/null 2>&1; then
    echo "   Vault déjà installé"
else
    kubectl create namespace vault
    helm install vault hashicorp/vault \
        --namespace vault \
        --set "server.dev.enabled=true" \
        --set "injector.enabled=true" \
        --set "ui.enabled=true" \
        --wait \
        --timeout=5m
    echo "   Vault installé (mode dev)"
fi
echo ""

# Configuration de Vault
echo "7. Configuration de Vault..."
kubectl exec -n vault vault-0 -- vault auth enable kubernetes 2>/dev/null || echo "   Kubernetes auth déjà activé"
kubectl exec -n vault vault-0 -- vault secrets enable -path=secret kv-v2 2>/dev/null || echo "   KV secret engine déjà activé"

# Configurer l'authentification Kubernetes
kubectl exec -n vault vault-0 -- vault write auth/kubernetes/config \
    kubernetes_host="https://kubernetes.default.svc:443" 2>/dev/null || true

# Créer les politiques
kubectl exec -n vault vault-0 -- vault policy write integration - <<EOF
path "secret/data/integration/*" {
  capabilities = ["read", "list"]
}
EOF

kubectl exec -n vault vault-0 -- vault policy write production - <<EOF
path "secret/data/production/*" {
  capabilities = ["read", "list"]
}
EOF

# Créer les rôles
kubectl exec -n vault vault-0 -- vault write auth/kubernetes/role/integration \
    bound_service_account_names=default \
    bound_service_account_namespaces=soa-integration \
    policies=integration \
    ttl=24h 2>/dev/null || true

kubectl exec -n vault vault-0 -- vault write auth/kubernetes/role/production \
    bound_service_account_names=default \
    bound_service_account_namespaces=soa-production \
    policies=production \
    ttl=24h 2>/dev/null || true

echo "   Vault configuré"
echo ""

# Créer les secrets initiaux
echo "8. Création des secrets initiaux dans Vault..."
kubectl exec -n vault vault-0 -- vault kv put secret/integration/database \
    mysql_host="mysql.soa-integration.svc.cluster.local" \
    mysql_port="3306" \
    mysql_database="recipeyoulove_integration" \
    mysql_username="app_user" \
    mysql_password="integration_password_CHANGE_ME" > /dev/null

kubectl exec -n vault vault-0 -- vault kv put secret/integration/mongodb \
    mongo_host="mongodb.soa-integration.svc.cluster.local" \
    mongo_port="27017" \
    mongo_database="recipeyoulove_integration" \
    mongo_username="app_user" \
    mongo_password="integration_password_CHANGE_ME" > /dev/null

kubectl exec -n vault vault-0 -- vault kv put secret/integration/app \
    spring_profiles_active="integration" \
    log_level="DEBUG" \
    jwt_secret="integration_jwt_secret_CHANGE_ME" > /dev/null

kubectl exec -n vault vault-0 -- vault kv put secret/production/database \
    mysql_host="mysql.soa-production.svc.cluster.local" \
    mysql_port="3306" \
    mysql_database="recipeyoulove_production" \
    mysql_username="app_user" \
    mysql_password="PRODUCTION_PASSWORD_CHANGE_ME" > /dev/null

kubectl exec -n vault vault-0 -- vault kv put secret/production/mongodb \
    mongo_host="mongodb.soa-production.svc.cluster.local" \
    mongo_port="27017" \
    mongo_database="recipeyoulove_production" \
    mongo_username="app_user" \
    mongo_password="PRODUCTION_PASSWORD_CHANGE_ME" > /dev/null

kubectl exec -n vault vault-0 -- vault kv put secret/production/app \
    spring_profiles_active="production" \
    log_level="INFO" \
    jwt_secret="PRODUCTION_JWT_SECRET_CHANGE_ME" > /dev/null

echo "   Secrets créés"
echo ""

# Déployer les applications ArgoCD
echo "9. Déploiement des applications ArgoCD..."
kubectl apply -f k8s/argocd/applications/ > /dev/null 2>&1 || true
echo "   Applications ArgoCD déployées"
echo ""

# Résumé
echo "=========================================="
echo "Installation terminée avec succès!"
echo "=========================================="
echo ""
echo "Informations d'accès:"
echo ""
echo "ArgoCD:"
echo "  URL: http://localhost:8080 (via port-forward)"
echo "  Utilisateur: admin"
echo "  Mot de passe: $ARGOCD_PASSWORD"
echo "  Commande: kubectl port-forward svc/argocd-server -n argocd 8080:443"
echo ""
echo "Vault:"
echo "  URL: http://localhost:8200 (via port-forward)"
echo "  Commande: kubectl port-forward -n vault svc/vault-ui 8200:8200"
echo ""
echo "IMPORTANT:"
echo "  1. Changez les mots de passe par défaut dans Vault"
echo "  2. Configurez votre DNS ou fichier hosts pour accéder aux services"
echo "  3. Consultez README.md pour plus d'informations"
echo ""
echo "Prochaines étapes:"
echo "  - Accédez à ArgoCD pour voir vos applications"
echo "  - Configurez les secrets dans Vault"
echo "  - Déployez vos microservices via GitHub Actions"
echo ""

