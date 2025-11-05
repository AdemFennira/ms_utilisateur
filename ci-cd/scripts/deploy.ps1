# Script de déploiement pour RecipeYouLove sur Kubernetes
# Ce script déploie l'application dans l'environnement spécifié (integration ou production)

param(
    [Parameter(Mandatory=$true)]
    [ValidateSet("integration", "production")]
    [string]$Environment
)

Write-Host "🚀 Déploiement de RecipeYouLove sur Kubernetes - Environnement: $Environment" -ForegroundColor Cyan
Write-Host ""

# Variables
$namespace = "soa-$Environment"
$deploymentFile = "k8s\environments\$Environment\deployment.yaml"

# Vérifier que Minikube est démarré
Write-Host "📊 Vérification de l'état de Minikube..." -ForegroundColor Yellow
$minikubeStatus = minikube status 2>&1
if ($LASTEXITCODE -ne 0) {
    Write-Host "❌ Minikube n'est pas démarré. Démarrage en cours..." -ForegroundColor Red
    minikube start
    if ($LASTEXITCODE -ne 0) {
        Write-Host "❌ Échec du démarrage de Minikube" -ForegroundColor Red
        exit 1
    }
}
Write-Host "✅ Minikube est opérationnel" -ForegroundColor Green
Write-Host ""

# Créer le namespace s'il n'existe pas
Write-Host "📦 Création du namespace $namespace s'il n'existe pas..." -ForegroundColor Yellow
kubectl create namespace $namespace 2>$null
if ($LASTEXITCODE -eq 0) {
    Write-Host "✅ Namespace $namespace créé" -ForegroundColor Green
} else {
    Write-Host "ℹ️  Namespace $namespace existe déjà" -ForegroundColor Gray
}
Write-Host ""

# Vérifier que Vault est opérationnel
Write-Host "🔐 Vérification de Vault..." -ForegroundColor Yellow
$vaultPods = kubectl get pods -n vault -l app.kubernetes.io/name=vault -o jsonpath='{.items[0].status.phase}'
if ($vaultPods -ne "Running") {
    Write-Host "❌ Vault n'est pas opérationnel" -ForegroundColor Red
    exit 1
}
Write-Host "✅ Vault est opérationnel" -ForegroundColor Green
Write-Host ""

# Vérifier que les secrets existent dans Vault
Write-Host "🔑 Vérification des secrets dans Vault..." -ForegroundColor Yellow
$secretCheck = kubectl exec -n vault vault-0 -- vault kv get secret/$Environment/mysql 2>&1
if ($LASTEXITCODE -ne 0) {
    Write-Host "❌ Les secrets $Environment ne sont pas configurés dans Vault" -ForegroundColor Red
    Write-Host "ℹ️  Exécutez d'abord: .\k8s\vault\setup-secrets.ps1" -ForegroundColor Yellow
    exit 1
}
Write-Host "✅ Secrets Vault configurés" -ForegroundColor Green
Write-Host ""

# Vérifier que les bases de données sont opérationnelles
Write-Host "🗄️  Vérification des bases de données..." -ForegroundColor Yellow
$mysqlPod = kubectl get pods -n databases -l app=mysql -o jsonpath='{.items[0].status.phase}'
if ($mysqlPod -eq "Running") {
    Write-Host "✅ MySQL est opérationnel" -ForegroundColor Green
} else {
    Write-Host "⚠️  MySQL n'est pas opérationnel" -ForegroundColor Yellow
}
Write-Host ""

# Appliquer les manifestes Kubernetes
Write-Host "📝 Application des manifestes Kubernetes..." -ForegroundColor Yellow
kubectl apply -f $deploymentFile
if ($LASTEXITCODE -ne 0) {
    Write-Host "❌ Échec de l'application des manifestes" -ForegroundColor Red
    exit 1
}
Write-Host "✅ Manifestes appliqués avec succès" -ForegroundColor Green
Write-Host ""

# Attendre que le déploiement soit prêt
Write-Host "⏳ Attente du déploiement..." -ForegroundColor Yellow
kubectl rollout status deployment/recipeyoulove-api -n $namespace --timeout=5m
if ($LASTEXITCODE -ne 0) {
    Write-Host "❌ Le déploiement a échoué ou timeout" -ForegroundColor Red
    Write-Host ""
    Write-Host "📋 Logs du déploiement:" -ForegroundColor Yellow
    kubectl get pods -n $namespace
    Write-Host ""
    Write-Host "Pour voir les logs d'un pod:" -ForegroundColor Yellow
    Write-Host "kubectl logs <pod-name> -n $namespace -c recipeyoulove-api" -ForegroundColor Cyan
    exit 1
}
Write-Host "✅ Déploiement terminé avec succès" -ForegroundColor Green
Write-Host ""

# Afficher l'état du déploiement
Write-Host "📊 État du déploiement:" -ForegroundColor Cyan
Write-Host ""
Write-Host "Pods:" -ForegroundColor Yellow
kubectl get pods -n $namespace -l app=recipeyoulove-api
Write-Host ""
Write-Host "Services:" -ForegroundColor Yellow
kubectl get svc -n $namespace
Write-Host ""
Write-Host "Ingress:" -ForegroundColor Yellow
kubectl get ingress -n $namespace
Write-Host ""

# Obtenir l'URL d'accès
if ($Environment -eq "integration") {
    $url = "http://soa-api-integration.recipeyoulove.app"
} else {
    $url = "http://soa-api.recipeyoulove.app"
}

Write-Host "🎉 Déploiement terminé avec succès!" -ForegroundColor Green
Write-Host ""
Write-Host "🌐 URL d'accès: $url" -ForegroundColor Cyan
Write-Host ""
Write-Host "📝 Commandes utiles:" -ForegroundColor Yellow
Write-Host "  • Voir les logs:        kubectl logs -f deployment/recipeyoulove-api -n $namespace -c recipeyoulove-api" -ForegroundColor Gray
Write-Host "  • Voir les pods:        kubectl get pods -n $namespace" -ForegroundColor Gray
Write-Host "  • Décrire un pod:       kubectl describe pod <pod-name> -n $namespace" -ForegroundColor Gray
Write-Host "  • Port-forward:         kubectl port-forward -n $namespace svc/recipeyoulove-api-service 8080:8080" -ForegroundColor Gray
Write-Host "  • Supprimer:            kubectl delete -f $deploymentFile" -ForegroundColor Gray
Write-Host ""
Write-Host "💡 Pour accéder via le nom de domaine, assurez-vous que minikube tunnel est actif:" -ForegroundColor Yellow
Write-Host "   minikube tunnel" -ForegroundColor Cyan
Write-Host ""
Write-Host "   Puis ajoutez dans C:\Windows\System32\drivers\etc\hosts:" -ForegroundColor Yellow
Write-Host "   127.0.0.1 soa-api-integration.recipeyoulove.app" -ForegroundColor Cyan
Write-Host "   127.0.0.1 soa-api.recipeyoulove.app" -ForegroundColor Cyan

