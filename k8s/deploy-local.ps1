# Script pour déployer l'application localement sur Minikube
# Usage: .\deploy-local.ps1

param(
    [string]$ImageTag = "latest"
)

Write-Host "=== Déploiement de l'application sur Minikube ===" -ForegroundColor Cyan

# Vérifier que Minikube est démarré
$minikubeStatus = minikube status --format='{{.Host}}' 2>$null
if ($minikubeStatus -ne "Running") {
    Write-Host "Minikube n'est pas démarré. Exécutez d'abord .\setup-minikube.ps1" -ForegroundColor Red
    exit 1
}

# Configurer Docker pour utiliser le daemon Minikube
Write-Host "`nConfiguration de Docker pour Minikube..." -ForegroundColor Yellow
& minikube -p minikube docker-env --shell powershell | Invoke-Expression

# Builder l'image Docker dans Minikube
Write-Host "`nConstruction de l'image Docker..." -ForegroundColor Yellow
Set-Location ..
docker build -t univ-soa:$ImageTag -f Dockerfile .

# Appliquer les manifests Kubernetes
Write-Host "`nDéploiement sur Kubernetes..." -ForegroundColor Yellow
kubectl apply -f k8s/minikube/ -n soa-integration

# Mettre à jour l'image du déploiement
Write-Host "`nMise à jour de l'image..." -ForegroundColor Yellow
kubectl set image deployment/univ-soa univ-soa=univ-soa:$ImageTag -n soa-integration

# Attendre le déploiement
Write-Host "`nAttente du déploiement..." -ForegroundColor Yellow
kubectl rollout status deployment/univ-soa -n soa-integration --timeout=300s

# Afficher les pods
Write-Host "`nPods déployés:" -ForegroundColor Yellow
kubectl get pods -n soa-integration -o wide

# Obtenir l'URL du service
Write-Host "`nURL du service:" -ForegroundColor Yellow
$serviceUrl = minikube service univ-soa --url -n soa-integration
Write-Host $serviceUrl -ForegroundColor Green

Write-Host "`n=== Déploiement terminé ===" -ForegroundColor Green
Write-Host "`nPour tester l'application:" -ForegroundColor Cyan
Write-Host "  curl $serviceUrl/actuator/health" -ForegroundColor White
Write-Host "`nPour voir les logs:" -ForegroundColor Cyan
Write-Host "  kubectl logs -f deployment/univ-soa -n soa-integration" -ForegroundColor White

