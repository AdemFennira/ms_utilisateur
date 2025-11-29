# Script pour exécuter les tests d'intégration Newman
# Usage: .\run-integration-tests.ps1 [-ServiceUrl "http://localhost:8080"]

param(
    [string]$ServiceUrl = ""
)

Write-Host "=== Tests d'intégration Newman ===" -ForegroundColor Cyan

# Se déplacer dans le dossier des tests
Set-Location -Path "$PSScriptRoot\..\tests\newman"

# Vérifier que npm est installé
if (-not (Get-Command npm -ErrorAction SilentlyContinue)) {
    Write-Host "npm n'est pas installé. Installez Node.js depuis https://nodejs.org/" -ForegroundColor Red
    exit 1
}

# Installer les dépendances si nécessaire
if (-not (Test-Path "node_modules")) {
    Write-Host "`nInstallation des dépendances..." -ForegroundColor Yellow
    npm install
}

# Déterminer l'URL du service
if ([string]::IsNullOrEmpty($ServiceUrl)) {
    Write-Host "`nRécupération de l'URL du service depuis Minikube..." -ForegroundColor Yellow
    try {
        $ServiceUrl = minikube service univ-soa --url -n soa-integration 2>$null
        if ([string]::IsNullOrEmpty($ServiceUrl)) {
            $ServiceUrl = "http://localhost:8080"
        }
    } catch {
        $ServiceUrl = "http://localhost:8080"
    }
}

Write-Host "URL du service: $ServiceUrl" -ForegroundColor Green

# Mettre à jour le fichier d'environnement
if (Test-Path "env.json") {
    Write-Host "`nMise à jour du fichier d'environnement..." -ForegroundColor Yellow
    $env_content = Get-Content "env.json" | ConvertFrom-Json
    if ($env_content.values -and $env_content.values.Count -gt 0) {
        $env_content.values[0].value = $ServiceUrl
    }
    $env_content | ConvertTo-Json -Depth 10 | Set-Content "env.tmp.json"
} else {
    Write-Host "Fichier env.json non trouvé, utilisation de l'URL par défaut" -ForegroundColor Yellow
}

# Exécuter les tests Newman
Write-Host "`nExécution des tests Newman..." -ForegroundColor Yellow
node index.js --collection ./collection.json --environment ./env.tmp.json --data ./dataset.json

$exitCode = $LASTEXITCODE

# Nettoyer le fichier temporaire
if (Test-Path "env.tmp.json") {
    Remove-Item "env.tmp.json" -Force
}

# Retourner à la racine
Set-Location -Path "$PSScriptRoot\..\.."

if ($exitCode -eq 0) {
    Write-Host "`n=== Tests réussis ===" -ForegroundColor Green
} else {
    Write-Host "`n=== Tests échoués ===" -ForegroundColor Red
    exit $exitCode
}

