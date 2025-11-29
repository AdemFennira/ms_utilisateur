# Script pour configurer Minikube localement
Write-Host "  - Logs: minikube logs" -ForegroundColor White
Write-Host "  - Services: minikube service list" -ForegroundColor White
Write-Host "  - IP Minikube: minikube ip" -ForegroundColor White
Write-Host "  - Dashboard: minikube dashboard" -ForegroundColor White
Write-Host "`nCommandes utiles:" -ForegroundColor Cyan
Write-Host "`n=== Minikube configuré avec succès ===" -ForegroundColor Green

minikube addons enable dashboard
minikube addons enable metrics-server
minikube addons enable ingress
Write-Host "`nActivation des addons Minikube..." -ForegroundColor Yellow
# Activer les addons utiles

kubectl create namespace soa-integration --dry-run=client -o yaml | kubectl apply -f -
Write-Host "`nCréation du namespace soa-integration..." -ForegroundColor Yellow
# Créer le namespace

kubectl get nodes
kubectl cluster-info
Write-Host "`nVérification du cluster..." -ForegroundColor Yellow
# Vérifier le statut

minikube start --memory=4096 --cpus=2 --driver=docker
Write-Host "`nDémarrage de Minikube..." -ForegroundColor Yellow
# Démarrer Minikube

}
    exit 1
    Write-Host "Minikube n'est pas installé. Installez-le depuis https://minikube.sigs.k8s.io/docs/start/" -ForegroundColor Red
if (-not (Get-Command minikube -ErrorAction SilentlyContinue)) {
# Vérifier si Minikube est installé

Write-Host "=== Configuration de Minikube pour RecipeYouLove ===" -ForegroundColor Cyan

# Usage: .\setup-minikube.ps1

