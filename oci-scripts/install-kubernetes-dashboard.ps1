# Script d'installation de Kubernetes Dashboard
# Interface graphique officielle pour gérer vos ressources Kubernetes

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "   Installation Kubernetes Dashboard" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# 1. Installer Kubernetes Dashboard
Write-Host "1. Installation du Dashboard..." -ForegroundColor Yellow
kubectl apply -f https://raw.githubusercontent.com/kubernetes/dashboard/v2.7.0/aio/deploy/recommended.yaml
Write-Host "   Dashboard installe" -ForegroundColor Green
Write-Host ""

# 2. Créer un ServiceAccount admin
Write-Host "2. Creation du compte admin..." -ForegroundColor Yellow
@"
apiVersion: v1
kind: ServiceAccount
metadata:
  name: admin-user
  namespace: kubernetes-dashboard
---
apiVersion: rbac.authorization.k8s.io/v1
kind: ClusterRoleBinding
metadata:
  name: admin-user
roleRef:
  apiGroup: rbac.authorization.k8s.io
  kind: ClusterRole
  name: cluster-admin
subjects:
- kind: ServiceAccount
  name: admin-user
  namespace: kubernetes-dashboard
"@ | kubectl apply -f -
Write-Host "   Compte admin cree" -ForegroundColor Green
Write-Host ""

# 3. Obtenir le token d'accès
Write-Host "3. Generation du token d'acces..." -ForegroundColor Yellow
Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "   TOKEN D'ACCES AU DASHBOARD" -ForegroundColor Yellow
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Attendre un peu que le ServiceAccount soit créé
Start-Sleep -Seconds 2

$token = kubectl -n kubernetes-dashboard create token admin-user --duration=876000h
Write-Host $token -ForegroundColor Green
Write-Host ""
Write-Host "COPIEZ CE TOKEN ! Il sera demande lors de la connexion." -ForegroundColor Yellow
Write-Host ""

# Sauvegarder le token dans un fichier
$token | Out-File "$env:USERPROFILE\kubernetes-dashboard-token.txt"
Write-Host "Token sauvegarde dans: $env:USERPROFILE\kubernetes-dashboard-token.txt" -ForegroundColor Green
Write-Host ""

# 4. Instructions pour accéder
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "   ACCES AU DASHBOARD" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "Etape 1: Demarrer le proxy Kubernetes" -ForegroundColor Yellow
Write-Host "  kubectl proxy" -ForegroundColor White
Write-Host ""
Write-Host "Etape 2: Ouvrir dans le navigateur" -ForegroundColor Yellow
Write-Host "  http://localhost:8001/api/v1/namespaces/kubernetes-dashboard/services/https:kubernetes-dashboard:/proxy/" -ForegroundColor White
Write-Host ""
Write-Host "Etape 3: Choisir 'Token' et coller le token ci-dessus" -ForegroundColor Yellow
Write-Host ""
Write-Host "OU directement:" -ForegroundColor Yellow
Write-Host ""
Write-Host "  kubectl proxy" -ForegroundColor White
Write-Host "  start http://localhost:8001/api/v1/namespaces/kubernetes-dashboard/services/https:kubernetes-dashboard:/proxy/" -ForegroundColor White
Write-Host ""

