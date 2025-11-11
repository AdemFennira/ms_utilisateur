# Script de vérification et test de déploiement

Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host "   VERIFICATION AVANT DEPLOIEMENT" -ForegroundColor Green
Write-Host "========================================`n" -ForegroundColor Cyan

# Vérifier si on est dans le bon répertoire
if (-not (Test-Path "pom.xml")) {
    Write-Host "ERREUR: Vous n'êtes pas dans le répertoire du projet!" -ForegroundColor Red
    Write-Host "Exécutez: cd C:\Users\lenovo\git\RecipeYouLove" -ForegroundColor Yellow
    exit 1
}

Write-Host "Projet détecté: RecipeYouLove`n" -ForegroundColor Green

# Vérifier l'état Git
Write-Host "Vérification de Git..." -ForegroundColor Yellow
$gitStatus = git status --porcelain
if ($gitStatus) {
    Write-Host "  Fichiers modifiés détectés" -ForegroundColor White
} else {
    Write-Host "  Repository propre" -ForegroundColor Green
}

Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host "   ACTIONS REQUISES" -ForegroundColor Yellow
Write-Host "========================================`n" -ForegroundColor Cyan

Write-Host "1. CORRIGER LA VARIABLE GITHUB" -ForegroundColor Yellow
Write-Host "   URL: https://github.com/AbdBoutchichi/RecipeYouLove/settings/variables/actions" -ForegroundColor Cyan
Write-Host "   Variable: MICROSERVICE_NAME" -ForegroundColor White
Write-Host "   Valeur actuelle: ms-template" -ForegroundColor Red
Write-Host "   Valeur correcte: smartdish-parent" -ForegroundColor Green
Write-Host ""

Write-Host "2. VERIFIER LES SECRETS MYSQL" -ForegroundColor Yellow
Write-Host "   URL: https://github.com/AbdBoutchichi/RecipeYouLove/settings/secrets/actions" -ForegroundColor Cyan
Write-Host "   Secrets à vérifier:" -ForegroundColor White
Write-Host "     - MYSQL_HOST = mysql.smartdish.svc.cluster.local" -ForegroundColor White
Write-Host "     - MYSQL_PORT = 3306" -ForegroundColor White
Write-Host "     - MYSQL_USER = smartdish_user" -ForegroundColor White
Write-Host "     - MYSQL_PASSWORD = smartdish_pass_2024" -ForegroundColor White
Write-Host "     - MYSQL_DATABASE = smartdishdb" -ForegroundColor White
Write-Host ""

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "   PREPARATION DU TEST" -ForegroundColor Green
Write-Host "========================================`n" -ForegroundColor Cyan

# Demander confirmation
Write-Host "Avez-vous corrigé la variable MICROSERVICE_NAME et vérifié les secrets MySQL?" -ForegroundColor Yellow
$response = Read-Host "Tapez 'oui' pour continuer"

if ($response -ne "oui") {
    Write-Host "`nAnnulation. Corrigez d'abord les configurations.`n" -ForegroundColor Red
    exit 0
}

Write-Host "`n1. Création de la branche de test..." -ForegroundColor Yellow
$branchName = "feat/test-deployment-oke"

# Vérifier si la branche existe déjà
$branchExists = git branch --list $branchName
if ($branchExists) {
    Write-Host "   La branche $branchName existe déjà" -ForegroundColor Yellow
    Write-Host "   Suppression de la branche locale..." -ForegroundColor Yellow
    git branch -D $branchName 2>&1 | Out-Null
}

# Créer la nouvelle branche
git checkout -b $branchName
Write-Host "   Branche créée: $branchName" -ForegroundColor Green

Write-Host "`n2. Création du fichier de test..." -ForegroundColor Yellow
$testContent = @"
# Test de déploiement OKE

Date: $(Get-Date -Format "yyyy-MM-dd HH:mm:ss")
Branch: $branchName

## Infrastructure

- Cluster OKE: Active
- MySQL: Running
- NGINX Ingress: 141.145.216.180

## Objectif

Tester le déploiement complet de l'application Spring Boot sur OKE avec :
- Build Maven
- Tests unitaires
- Couverture de code >= 60%
- Build Docker image
- Push vers OCIR
- Security scan
- Déploiement Kubernetes

## URL attendue

http://soa-smartdish-parent.141.145.216.180.nip.io
"@

$testContent | Out-File -FilePath "TEST_DEPLOYMENT.md" -Encoding UTF8
Write-Host "   Fichier créé: TEST_DEPLOYMENT.md" -ForegroundColor Green

Write-Host "`n3. Ajout et commit..." -ForegroundColor Yellow
git add TEST_DEPLOYMENT.md
git add CHECKLIST_TEST_DEPLOIEMENT.md
git commit -m "test: Premier déploiement OKE avec MySQL fonctionnel"
Write-Host "   Commit créé" -ForegroundColor Green

Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host "   PUSH VERS GITHUB" -ForegroundColor Green
Write-Host "========================================`n" -ForegroundColor Cyan

Write-Host "Prêt à pousser vers GitHub?" -ForegroundColor Yellow
Write-Host "Cela va déclencher la pipeline CI/CD complète." -ForegroundColor White
$pushResponse = Read-Host "Tapez 'push' pour continuer"

if ($pushResponse -eq "push") {
    Write-Host "`nPush en cours..." -ForegroundColor Yellow
    git push origin $branchName

    Write-Host "`n========================================" -ForegroundColor Cyan
    Write-Host "   PUSH REUSSI !" -ForegroundColor Green
    Write-Host "========================================`n" -ForegroundColor Cyan

    Write-Host "Suivez le déploiement sur:" -ForegroundColor Yellow
    Write-Host "  https://github.com/AbdBoutchichi/RecipeYouLove/actions`n" -ForegroundColor Cyan

    Write-Host "Commandes pour vérifier le déploiement (Cloud Shell):" -ForegroundColor Yellow
    Write-Host "  kubectl get pods -n smartdish" -ForegroundColor White
    Write-Host "  kubectl logs -f deployment/smartdish-parent -n smartdish" -ForegroundColor White
    Write-Host "  kubectl get ingress -n smartdish" -ForegroundColor White
    Write-Host "  curl http://soa-smartdish-parent.141.145.216.180.nip.io/actuator/health`n" -ForegroundColor White

    Write-Host "Ouvrir GitHub Actions dans le navigateur?" -ForegroundColor Yellow
    $openBrowser = Read-Host "Tapez 'oui' pour ouvrir"
    if ($openBrowser -eq "oui") {
        Start-Process "https://github.com/AbdBoutchichi/RecipeYouLove/actions"
    }
} else {
    Write-Host "`nPush annulé. Pour pousser manuellement:" -ForegroundColor Yellow
    Write-Host "  git push origin $branchName`n" -ForegroundColor White
}

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "   SCRIPT TERMINE" -ForegroundColor Green
Write-Host "========================================`n" -ForegroundColor Cyan

