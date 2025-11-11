# Script de test de connexion OCIR
# Usage: .\test-ocir-auth.ps1

Write-Host "=====================================" -ForegroundColor Cyan
Write-Host "Test de connexion OCIR" -ForegroundColor Cyan
Write-Host "=====================================" -ForegroundColor Cyan
Write-Host ""

# Demander les informations
Write-Host "Veuillez entrer vos informations OCI:" -ForegroundColor Yellow
Write-Host ""

$tenancyNamespace = Read-Host "Tenancy Namespace (ex: axgbvr6e8mzp)"
$username = Read-Host "Username OCI (ex: oracleidentitycloudservice/john.doe@example.com)"
$authToken = Read-Host "Auth Token" -AsSecureString

# Convertir SecureString en plain text pour Docker
$BSTR = [System.Runtime.InteropServices.Marshal]::SecureStringToBSTR($authToken)
$authTokenPlain = [System.Runtime.InteropServices.Marshal]::PtrToStringAuto($BSTR)

# Construire le username complet
$fullUsername = "$tenancyNamespace/$username"

Write-Host ""
Write-Host "=====================================" -ForegroundColor Cyan
Write-Host "Configuration détectée:" -ForegroundColor Cyan
Write-Host "=====================================" -ForegroundColor Cyan
Write-Host "Registry: fra.ocir.io" -ForegroundColor White
Write-Host "Tenancy Namespace: $tenancyNamespace" -ForegroundColor White
Write-Host "Username: $username" -ForegroundColor White
Write-Host "Full Username: $fullUsername" -ForegroundColor Green
Write-Host "Auth Token: ****" -ForegroundColor White
Write-Host ""

# Vérifier le format
Write-Host "Vérifications:" -ForegroundColor Yellow
if ($fullUsername -match "^[^/]+/.+$") {
    Write-Host "✅ Format du username correct" -ForegroundColor Green
} else {
    Write-Host "❌ Format du username incorrect" -ForegroundColor Red
    Write-Host "   Attendu: <tenancy-namespace>/<username>" -ForegroundColor Red
    exit 1
}

if ($tenancyNamespace.Length -gt 0) {
    Write-Host "✅ Tenancy namespace défini" -ForegroundColor Green
} else {
    Write-Host "❌ Tenancy namespace vide" -ForegroundColor Red
    exit 1
}

if ($authTokenPlain.Length -gt 0) {
    Write-Host "✅ Auth token défini" -ForegroundColor Green
} else {
    Write-Host "❌ Auth token vide" -ForegroundColor Red
    exit 1
}

Write-Host ""
Write-Host "=====================================" -ForegroundColor Cyan
Write-Host "Test de connexion à OCIR" -ForegroundColor Cyan
Write-Host "=====================================" -ForegroundColor Cyan

# Test de login
try {
    Write-Host "Tentative de connexion..." -ForegroundColor Yellow

    # Utiliser echo pour passer le token à docker login
    $loginCommand = "echo $authTokenPlain | docker login fra.ocir.io -u $fullUsername --password-stdin"
    $result = Invoke-Expression $loginCommand

    if ($LASTEXITCODE -eq 0) {
        Write-Host ""
        Write-Host "=====================================" -ForegroundColor Green
        Write-Host "✅ CONNEXION RÉUSSIE!" -ForegroundColor Green
        Write-Host "=====================================" -ForegroundColor Green
        Write-Host ""
        Write-Host "Vos informations sont correctes. Vous pouvez les utiliser dans GitHub Secrets:" -ForegroundColor White
        Write-Host ""
        Write-Host "OCI_TENANCY_NAMESPACE = $tenancyNamespace" -ForegroundColor Cyan
        Write-Host "OCI_USERNAME = $fullUsername" -ForegroundColor Cyan
        Write-Host "OCI_AUTH_TOKEN = ****" -ForegroundColor Cyan
        Write-Host ""

        # Test de pull d'une image (optionnel)
        Write-Host "Voulez-vous tester le pull d'une image? (y/n)" -ForegroundColor Yellow
        $testPull = Read-Host

        if ($testPull -eq "y") {
            Write-Host ""
            Write-Host "Liste des repositories disponibles..." -ForegroundColor Yellow
            Write-Host "Note: Cette commande nécessite OCI CLI installé" -ForegroundColor Gray

            # Vérifier si OCI CLI est installé
            $ociInstalled = Get-Command oci -ErrorAction SilentlyContinue
            if ($ociInstalled) {
                oci artifacts container repository list --compartment-id root
            } else {
                Write-Host "OCI CLI n'est pas installé. Installation recommandée:" -ForegroundColor Yellow
                Write-Host "https://docs.oracle.com/en-us/iaas/Content/API/SDKDocs/cliinstall.htm" -ForegroundColor Cyan
            }
        }
    } else {
        Write-Host ""
        Write-Host "=====================================" -ForegroundColor Red
        Write-Host "❌ ÉCHEC DE CONNEXION" -ForegroundColor Red
        Write-Host "=====================================" -ForegroundColor Red
        Write-Host ""
        Write-Host "Causes possibles:" -ForegroundColor Yellow
        Write-Host "1. Auth Token invalide ou expiré" -ForegroundColor White
        Write-Host "2. Username incorrect" -ForegroundColor White
        Write-Host "3. Tenancy namespace incorrect" -ForegroundColor White
        Write-Host "4. Permissions IAM insuffisantes" -ForegroundColor White
        Write-Host ""
        Write-Host "Actions recommandées:" -ForegroundColor Yellow
        Write-Host "1. Vérifiez votre tenancy namespace: oci os ns get" -ForegroundColor Cyan
        Write-Host "2. Régénérez un Auth Token dans la console OCI" -ForegroundColor Cyan
        Write-Host "3. Vérifiez vos permissions IAM" -ForegroundColor Cyan
        Write-Host ""
    }
} catch {
    Write-Host ""
    Write-Host "❌ ERREUR lors de la connexion:" -ForegroundColor Red
    Write-Host $_.Exception.Message -ForegroundColor Red
    Write-Host ""
}

# Nettoyage
[System.Runtime.InteropServices.Marshal]::ZeroFreeBSTR($BSTR)

Write-Host ""
Write-Host "Pressez une touche pour quitter..."
$null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")

