#!/bin/bash
# Script de vérification rapide de la configuration OCIR
# Usage: ./check-ocir-config.sh

echo "========================================="
echo "Vérification de la configuration OCIR"
echo "========================================="
echo ""

# Couleurs
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
CYAN='\033[0;36m'
NC='\033[0m' # No Color

# Compteurs
ERRORS=0
WARNINGS=0
SUCCESS=0

# Fonction pour afficher les résultats
check_item() {
    local description="$1"
    local status="$2"

    if [ "$status" = "ok" ]; then
        echo -e "${GREEN}✅${NC} $description"
        ((SUCCESS++))
    elif [ "$status" = "warning" ]; then
        echo -e "${YELLOW}⚠️${NC}  $description"
        ((WARNINGS++))
    else
        echo -e "${RED}❌${NC} $description"
        ((ERRORS++))
    fi
}

echo "1. Vérification OCI CLI"
echo "------------------------"
if command -v oci &> /dev/null; then
    check_item "OCI CLI est installé" "ok"

    # Vérifier la configuration OCI
    if [ -f ~/.oci/config ]; then
        check_item "Fichier de configuration OCI trouvé" "ok"
    else
        check_item "Fichier de configuration OCI non trouvé" "error"
    fi

    # Essayer de récupérer le tenancy namespace
    TENANCY_NS=$(oci os ns get 2>/dev/null || echo "")
    if [ -n "$TENANCY_NS" ]; then
        echo -e "   ${CYAN}Tenancy Namespace:${NC} $TENANCY_NS"
        check_item "Tenancy namespace récupéré: $TENANCY_NS" "ok"
    else
        check_item "Impossible de récupérer le tenancy namespace (authentification requise?)" "warning"
    fi
else
    check_item "OCI CLI n'est pas installé" "error"
    echo "   Installation: https://docs.oracle.com/en-us/iaas/Content/API/SDKDocs/cliinstall.htm"
fi

echo ""
echo "2. Vérification Docker"
echo "----------------------"
if command -v docker &> /dev/null; then
    check_item "Docker est installé" "ok"

    # Vérifier si Docker est en cours d'exécution
    if docker info &> /dev/null; then
        check_item "Docker daemon est actif" "ok"
    else
        check_item "Docker daemon n'est pas actif" "error"
    fi

    # Vérifier si connecté à OCIR
    if docker system info 2>/dev/null | grep -q "fra.ocir.io"; then
        check_item "Session Docker active pour fra.ocir.io" "ok"
    else
        check_item "Pas de session Docker active pour fra.ocir.io" "warning"
        echo "   Utilisez: docker login fra.ocir.io"
    fi
else
    check_item "Docker n'est pas installé" "error"
fi

echo ""
echo "3. Vérification kubectl (optionnel)"
echo "------------------------------------"
if command -v kubectl &> /dev/null; then
    check_item "kubectl est installé" "ok"

    # Vérifier la connexion au cluster
    if kubectl cluster-info &> /dev/null; then
        CLUSTER=$(kubectl config current-context 2>/dev/null || echo "unknown")
        check_item "Connecté au cluster: $CLUSTER" "ok"
    else
        check_item "Pas de connexion active à un cluster K8s" "warning"
    fi
else
    check_item "kubectl n'est pas installé (optionnel pour le développement local)" "warning"
fi

echo ""
echo "4. Vérification des fichiers du projet"
echo "---------------------------------------"

# Vérifier les fichiers importants
FILES=(
    "Dockerfile"
    "pom.xml"
    ".github/workflows/build-docker-image.yml"
    ".github/workflows/config-vars.yml"
)

for file in "${FILES[@]}"; do
    if [ -f "$file" ]; then
        check_item "Fichier trouvé: $file" "ok"
    else
        check_item "Fichier manquant: $file" "error"
    fi
done

echo ""
echo "5. Vérification de la structure du projet"
echo "------------------------------------------"

# Vérifier les répertoires
DIRS=(
    "src/main/java"
    "src/main/resources"
    "target"
    ".github/workflows"
)

for dir in "${DIRS[@]}"; do
    if [ -d "$dir" ]; then
        check_item "Répertoire trouvé: $dir" "ok"
    else
        if [ "$dir" = "target" ]; then
            check_item "Répertoire manquant: $dir (normal si pas encore compilé)" "warning"
        else
            check_item "Répertoire manquant: $dir" "error"
        fi
    fi
done

echo ""
echo "6. Vérification Maven"
echo "---------------------"

if command -v mvn &> /dev/null; then
    check_item "Maven est installé" "ok"

    # Extraire le nom du microservice
    if [ -f "pom.xml" ]; then
        MS_NAME=$(mvn help:evaluate -Dexpression=project.artifactId -q -DforceStdout 2>/dev/null || echo "unknown")
        if [ -n "$MS_NAME" ] && [ "$MS_NAME" != "unknown" ]; then
            echo -e "   ${CYAN}Nom du microservice:${NC} $MS_NAME"
            check_item "Nom du microservice extrait: $MS_NAME" "ok"
        else
            check_item "Impossible d'extraire le nom du microservice" "warning"
        fi
    fi
else
    check_item "Maven n'est pas installé" "error"
fi

echo ""
echo "7. Informations pour GitHub Secrets"
echo "------------------------------------"

if [ -n "$TENANCY_NS" ]; then
    echo -e "${CYAN}Configurez ces secrets dans GitHub:${NC}"
    echo ""
    echo "OCI_TENANCY_NAMESPACE:"
    echo "  $TENANCY_NS"
    echo ""
    echo "OCI_USERNAME (format):"
    echo "  ${TENANCY_NS}/<votre-username>"
    echo "  Exemple: ${TENANCY_NS}/oracleidentitycloudservice/john.doe@example.com"
    echo ""
    echo "OCI_AUTH_TOKEN:"
    echo "  Générez un Auth Token dans la console OCI"
    echo "  User Settings → Auth Tokens → Generate Token"
else
    echo -e "${YELLOW}⚠️  Configurez OCI CLI pour obtenir automatiquement ces informations${NC}"
    echo ""
    echo "Configuration manuelle:"
    echo "1. Obtenez votre Tenancy Namespace: oci os ns get"
    echo "2. Formatez votre username: <tenancy-namespace>/<username>"
    echo "3. Générez un Auth Token dans la console OCI"
fi

echo ""
echo "========================================="
echo "Résumé"
echo "========================================="
echo -e "${GREEN}Succès:${NC} $SUCCESS"
echo -e "${YELLOW}Avertissements:${NC} $WARNINGS"
echo -e "${RED}Erreurs:${NC} $ERRORS"
echo ""

if [ $ERRORS -eq 0 ]; then
    echo -e "${GREEN}✅ Configuration de base OK !${NC}"
    echo ""
    echo "Prochaines étapes:"
    echo "1. Configurez les secrets GitHub (voir ci-dessus)"
    echo "2. Testez l'authentification OCIR: ./test-ocir-auth.ps1 (Windows)"
    echo "3. Poussez un commit pour déclencher le pipeline"
else
    echo -e "${RED}❌ Des erreurs ont été détectées${NC}"
    echo ""
    echo "Actions recommandées:"
    if ! command -v oci &> /dev/null; then
        echo "• Installez OCI CLI"
    fi
    if ! command -v docker &> /dev/null; then
        echo "• Installez Docker"
    fi
    if ! command -v mvn &> /dev/null; then
        echo "• Installez Maven"
    fi
fi

echo ""
echo "Pour plus d'aide, consultez:"
echo "• FIX_OCIR_AUTH.md"
echo "• CHECKLIST_OCIR_SECRETS.md"
echo "• OCI_CONFIGURATION.md"
echo ""

exit $ERRORS

