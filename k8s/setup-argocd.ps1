# Script pour installer et configurer ArgoCD sur Minikube
# Usage: .\setup-argocd.ps1

Write-Host "=== Installation d'ArgoCD ===" -ForegroundColor Cyan

# Vérifier que kubectl fonctionne
if (-not (Get-Command kubectl -ErrorAction SilentlyContinue)) {
    Write-Host "kubectl n'est pas installé ou configuré" -ForegroundColor Red
    exit 1
}

# Créer le namespace ArgoCD
Write-Host "`nCréation du namespace argocd..." -ForegroundColor Yellow
kubectl create namespace argocd --dry-run=client -o yaml | kubectl apply -f -

# Installer ArgoCD
Write-Host "`nInstallation d'ArgoCD..." -ForegroundColor Yellow
kubectl apply -n argocd -f https://raw.githubusercontent.com/argoproj/argo-cd/stable/manifests/install.yaml

# Attendre que les pods soient prêts
Write-Host "`nAttente du démarrage d'ArgoCD (cela peut prendre quelques minutes)..." -ForegroundColor Yellow
kubectl wait --for=condition=available --timeout=300s deployment/argocd-server -n argocd

# Obtenir le mot de passe initial
Write-Host "`nRécupération du mot de passe admin..." -ForegroundColor Yellow
$password = kubectl -n argocd get secret argocd-initial-admin-secret -o jsonpath="{.data.password}" | ForEach-Object { [System.Text.Encoding]::UTF8.GetString([System.Convert]::FromBase64String($_)) }

Write-Host "`n=== ArgoCD installé avec succès ===" -ForegroundColor Green
Write-Host "`nInformations de connexion:" -ForegroundColor Cyan
Write-Host "  Username: admin" -ForegroundColor White
Write-Host "  Password: $password" -ForegroundColor White

Write-Host "`nPour accéder à l'UI ArgoCD:" -ForegroundColor Cyan
Write-Host "  kubectl port-forward svc/argocd-server -n argocd 8080:443" -ForegroundColor White
Write-Host "  Puis ouvrez: https://localhost:8080" -ForegroundColor White

Write-Host "`nPour déployer l'application:" -ForegroundColor Cyan
Write-Host "  kubectl apply -f k8s/argocd/applications/app.yaml" -ForegroundColor White

