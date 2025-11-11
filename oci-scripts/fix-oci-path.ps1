# Script pour corriger le PATH OCI CLI et kubectl

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "   Correction de la configuration OCI" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# 1. Trouver l'installation Python d'OCI
Write-Host "1. Recherche de l'installation OCI CLI..." -ForegroundColor Yellow

$possiblePaths = @(
    "$env:USERPROFILE\AppData\Local\Programs\Python\Python*\Scripts",
    "$env:USERPROFILE\AppData\Local\Programs\Python\Python*",
    "$env:ProgramFiles\Python*\Scripts",
    "$env:LOCALAPPDATA\Programs\Python\Python*\Scripts",
    "C:\Python*\Scripts"
)

$ociPath = $null
foreach ($pathPattern in $possiblePaths) {
    $paths = Get-ChildItem -Path (Split-Path $pathPattern -Parent) -Filter (Split-Path $pathPattern -Leaf) -ErrorAction SilentlyContinue -Recurse -Depth 1
    foreach ($path in $paths) {
        if (Test-Path "$path\oci.exe") {
            $ociPath = $path
            break
        }
    }
    if ($ociPath) { break }
}

if ($ociPath) {
    Write-Host "   OCI CLI trouve: $ociPath" -ForegroundColor Green

    # Ajouter au PATH de la session actuelle
    $env:Path = "$ociPath;$env:Path"

    # Ajouter au PATH permanent
    $currentPath = [Environment]::GetEnvironmentVariable("Path", "User")
    if ($currentPath -notlike "*$ociPath*") {
        [Environment]::SetEnvironmentVariable("Path", "$ociPath;$currentPath", "User")
        Write-Host "   OCI CLI ajoute au PATH permanent" -ForegroundColor Green
    }
} else {
    Write-Host "   OCI CLI non trouve, verification de python..." -ForegroundColor Yellow

    # Vérifier si python est installé
    $pythonCmd = Get-Command python -ErrorAction SilentlyContinue
    if ($pythonCmd) {
        $pythonPath = Split-Path $pythonCmd.Source
        $scriptsPath = Join-Path $pythonPath "Scripts"

        if (Test-Path "$scriptsPath\oci.exe") {
            $ociPath = $scriptsPath
            Write-Host "   OCI CLI trouve dans: $ociPath" -ForegroundColor Green

            $env:Path = "$ociPath;$env:Path"
            $currentPath = [Environment]::GetEnvironmentVariable("Path", "User")
            if ($currentPath -notlike "*$ociPath*") {
                [Environment]::SetEnvironmentVariable("Path", "$ociPath;$currentPath", "User")
                Write-Host "   OCI CLI ajoute au PATH" -ForegroundColor Green
            }
        }
    }
}

Write-Host ""

# 2. Vérifier que OCI fonctionne maintenant
Write-Host "2. Verification de OCI CLI..." -ForegroundColor Yellow
try {
    $ociVersion = & oci --version 2>&1
    Write-Host "   OCI CLI version: $ociVersion" -ForegroundColor Green
} catch {
    Write-Host "   ERREUR: OCI CLI toujours introuvable" -ForegroundColor Red
    Write-Host "   Solution: Relancez PowerShell en tant qu'administrateur" -ForegroundColor Yellow
    exit 1
}

Write-Host ""

# 3. Regénérer le kubeconfig
Write-Host "3. Regeneration du kubeconfig..." -ForegroundColor Yellow
$clusterId = "ocid1.cluster.oc1.eu-paris-1.aaaaaaaaafkhi5vnahyc14ozq2ulnwstf3t6hslnvmomgeq5ulqsvca3gwzgw3tq"

try {
    oci ce cluster create-kubeconfig `
        --cluster-id $clusterId `
        --file "$env:USERPROFILE\.kube\config" `
        --region eu-paris-1 `
        --token-version 2.0.0 `
        --overwrite

    Write-Host "   Kubeconfig regenere" -ForegroundColor Green
} catch {
    Write-Host "   ERREUR lors de la regeneration du kubeconfig" -ForegroundColor Red
    Write-Host "   $_" -ForegroundColor Red
    exit 1
}

Write-Host ""

# 4. Tester kubectl
Write-Host "4. Test de la connexion kubectl..." -ForegroundColor Yellow
try {
    $clusterInfo = kubectl cluster-info 2>&1
    Write-Host "   Connexion reussie!" -ForegroundColor Green
    Write-Host $clusterInfo -ForegroundColor White
} catch {
    Write-Host "   ERREUR: Connexion kubectl echouee" -ForegroundColor Red
    Write-Host "   $_" -ForegroundColor Red
}

Write-Host ""

# 5. Vérifier les pods
Write-Host "5. Verification des pods..." -ForegroundColor Yellow
kubectl get pods -n smartdish
kubectl get pods -n ingress-nginx

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "   Configuration terminee !" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "Vous pouvez maintenant:" -ForegroundColor Yellow
Write-Host "  - Utiliser kubectl normalement" -ForegroundColor White
Write-Host "  - Ouvrir Lens (qui detectera automatiquement le cluster)" -ForegroundColor White
Write-Host "  - Deployer votre application" -ForegroundColor White
Write-Host ""
Write-Host "IMPORTANT: Si kubectl ne fonctionne toujours pas," -ForegroundColor Yellow
Write-Host "fermez et relancez PowerShell pour recharger le PATH." -ForegroundColor Yellow
Write-Host ""

