# Script de démarrage rapide pour RecipeYouLove
# Usage: .\quick-start.ps1

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  RecipeYouLove - Démarrage Rapide     " -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Vérifier les prérequis
Write-Host "Vérification des prérequis..." -ForegroundColor Yellow

$prereqs = @{
    "Docker" = { docker --version }
    "Minikube" = { minikube version }
    "kubectl" = { kubectl version --client }
    "Maven" = { mvn --version }
    "Node.js" = { node --version }
}

$missing = @()
foreach ($tool in $prereqs.Keys) {
    try {
        $null = & $prereqs[$tool] 2>&1
        Write-Host "  ✓ $tool installé" -ForegroundColor Green
    } catch {
        Write-Host "  ✗ $tool manquant" -ForegroundColor Red
        $missing += $tool
    }
}

if ($missing.Count -gt 0) {
    Write-Host "`nOutils manquants: $($missing -join ', ')" -ForegroundColor Red
    Write-Host "Installez-les avant de continuer." -ForegroundColor Red
    exit 1
}

Write-Host "`nTous les prérequis sont installés !" -ForegroundColor Green
Write-Host ""

# Menu interactif
Write-Host "Que voulez-vous faire ?" -ForegroundColor Cyan
Write-Host "  1. Configuration complète (Minikube + ArgoCD + Déploiement)" -ForegroundColor White
Write-Host "  2. Setup Minikube uniquement" -ForegroundColor White
Write-Host "  3. Déployer l'application" -ForegroundColor White
Write-Host "  4. Lancer les tests d'intégration" -ForegroundColor White
Write-Host "  5. Setup ArgoCD" -ForegroundColor White
Write-Host "  6. Afficher les commandes utiles" -ForegroundColor White
Write-Host "  0. Quitter" -ForegroundColor White
Write-Host ""

$choice = Read-Host "Votre choix"

switch ($choice) {
    "1" {
        Write-Host "`n=== Configuration complète ===" -ForegroundColor Cyan
        Write-Host "1/4 - Setup Minikube..." -ForegroundColor Yellow
        .\k8s\setup-minikube.ps1

        Write-Host "`n2/4 - Build de l'application..." -ForegroundColor Yellow
        mvn clean package -DskipTests

        Write-Host "`n3/4 - Déploiement..." -ForegroundColor Yellow
        .\k8s\deploy-local.ps1

        Write-Host "`n4/4 - Setup ArgoCD..." -ForegroundColor Yellow
        .\k8s\setup-argocd.ps1

        Write-Host "`n✅ Configuration terminée !" -ForegroundColor Green
    }

    "2" {
        Write-Host "`n=== Setup Minikube ===" -ForegroundColor Cyan
        .\k8s\setup-minikube.ps1
    }

    "3" {
        Write-Host "`n=== Déploiement de l'application ===" -ForegroundColor Cyan

        $build = Read-Host "Voulez-vous rebuild l'application ? (o/N)"
        if ($build -eq "o" -or $build -eq "O") {
            Write-Host "Build Maven..." -ForegroundColor Yellow
            mvn clean package -DskipTests
        }

        .\k8s\deploy-local.ps1
    }

    "4" {
        Write-Host "`n=== Tests d'intégration ===" -ForegroundColor Cyan
        .\k8s\run-integration-tests.ps1
    }

    "5" {
        Write-Host "`n=== Setup ArgoCD ===" -ForegroundColor Cyan
        .\k8s\setup-argocd.ps1
    }

    "6" {
        Write-Host "`n=== Commandes Utiles ===" -ForegroundColor Cyan
        Write-Host "`nMinikube:" -ForegroundColor Yellow
        Write-Host "  minikube start" -ForegroundColor White
        Write-Host "  minikube stop" -ForegroundColor White
        Write-Host "  minikube dashboard" -ForegroundColor White
        Write-Host "  minikube service univ-soa --url -n soa-integration" -ForegroundColor White

        Write-Host "`nKubernetes:" -ForegroundColor Yellow
        Write-Host "  kubectl get pods -n soa-integration" -ForegroundColor White
        Write-Host "  kubectl get svc -n soa-integration" -ForegroundColor White
        Write-Host "  kubectl logs -f deployment/univ-soa -n soa-integration" -ForegroundColor White
        Write-Host "  kubectl describe pod <pod-name> -n soa-integration" -ForegroundColor White

        Write-Host "`nArgoCD:" -ForegroundColor Yellow
        Write-Host "  kubectl port-forward svc/argocd-server -n argocd 8080:443" -ForegroundColor White
        Write-Host "  kubectl get applications -n argocd" -ForegroundColor White

        Write-Host "`nTests Newman:" -ForegroundColor Yellow
        Write-Host "  cd tests\newman" -ForegroundColor White
        Write-Host "  npm test" -ForegroundColor White

        Write-Host "`nDocker:" -ForegroundColor Yellow
        Write-Host "  docker build -t univ-soa:dev ." -ForegroundColor White
        Write-Host "  docker run -p 8080:8080 univ-soa:dev" -ForegroundColor White

        Write-Host "`nMaven:" -ForegroundColor Yellow
        Write-Host "  mvn clean package" -ForegroundColor White
        Write-Host "  mvn test" -ForegroundColor White
        Write-Host "  mvn spring-boot:run" -ForegroundColor White
    }

    "0" {
        Write-Host "`nAu revoir !" -ForegroundColor Cyan
        exit 0
    }

    default {
        Write-Host "`nChoix invalide." -ForegroundColor Red
        exit 1
    }
}

Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host "Pour plus d'informations, consultez:" -ForegroundColor White
Write-Host "  - GUIDE-DEPLOIEMENT.txt" -ForegroundColor Cyan
Write-Host "  - VERIFICATION-MIGRATION.txt" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan

