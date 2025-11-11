# 🚀 Guide d'Installation de l'Infrastructure Kubernetes Azure

## 📋 Prérequis

Avant de commencer, assurez-vous d'avoir :
- ✅ Un compte Azure Student (avec crédit de 100$)
- ✅ Azure CLI installé : https://aka.ms/installazurecliwindows
- ✅ kubectl installé
- ✅ Helm installé
- ✅ PowerShell 5.1 ou supérieur
- ✅ Accès administrateur aux settings GitHub du dépôt

## 📦 Architecture de l'Infrastructure

```
SmartDish (Dépôt Parent)
├── Azure AKS Cluster (1 nœud B2s)
├── Azure Container Registry (Basic)
├── Azure MySQL Flexible Server (Gratuit 750h/mois)
├── Azure Key Vault (pour les secrets)
└── Namespaces Kubernetes:
    ├── soa-integration (environnement d'intégration)
    └── soa-production (environnement de production)

Microservices (Forks du parent):
└── Chaque fork déploie automatiquement via CI/CD
```

---

## 🎯 ÉTAPE 1 : Provisionner l'Infrastructure Azure (15 min)

### 1.1 Vérifier Azure CLI

```powershell
az --version
```

Si non installé, téléchargez depuis : https://aka.ms/installazurecliwindows

### 1.2 Exécuter le script de provisionnement

Ouvrez PowerShell en tant qu'**Administrateur** et exécutez :

```powershell
cd C:\Users\lenovo\git\RecipeYouLove
.\azure-scripts\setup-smartdish-aks.ps1
```

**Ce que fait le script :**
- Se connecte à votre compte Azure Student
- Crée un Resource Group `smartdish-rg`
- Provisionne un cluster AKS avec 1 nœud B2s (~18€/mois)
- Crée un Azure Container Registry Basic (~5€/mois)
- Provisionne MySQL Flexible Server (GRATUIT avec Azure Student)
- Crée une base de données unique `smartdish` (partagée par tous les MS)
- Crée un Azure Key Vault avec un nom unique
- Stocke les secrets MySQL dans Key Vault
- Active le CSI Driver pour synchroniser les secrets
- Crée un Service Principal pour GitHub Actions
- Configure kubectl avec les credentials du cluster

### 1.3 Sauvegarder les valeurs affichées

À la fin du script, **COPIEZ ET SAUVEGARDEZ** les valeurs suivantes :
- `AZURE_CREDENTIALS` (JSON complet)
- `AZURE_RESOURCE_GROUP`
- `ACR_LOGIN_SERVER`
- `AKS_CLUSTER_NAME`
- `MYSQL_HOST`
- `KEY_VAULT_NAME`
- `TENANT_ID`
- `CLIENT_ID` (identité managée du CSI Driver)

---

## 🎯 ÉTAPE 2 : Installer les Composants Kubernetes (10 min)

### 2.1 Vérifier la connexion au cluster

```powershell
kubectl cluster-info
kubectl get nodes
```

Vous devriez voir 1 nœud en état `Ready`.

### 2.2 Installer NGINX Ingress Controller

```powershell
helm repo add ingress-nginx https://kubernetes.github.io/ingress-nginx
helm repo update

helm install ingress-nginx ingress-nginx/ingress-nginx `
  --namespace ingress-nginx `
  --create-namespace `
  --set controller.replicaCount=1 `
  --set controller.nodeSelector."kubernetes\.io/os"=linux `
  --set defaultBackend.nodeSelector."kubernetes\.io/os"=linux `
  --set controller.service.type=LoadBalancer `
  --set controller.service.externalTrafficPolicy=Local
```

**Attendre que l'IP externe soit assignée** (3-5 min) :

```powershell
kubectl get service -n ingress-nginx --watch
```

Une fois l'`EXTERNAL-IP` affichée (ex: `20.123.45.67`), notez-la et appuyez sur `Ctrl+C`.

### 2.3 Installer Cert-Manager (pour les certificats SSL)

```powershell
kubectl apply -f https://github.com/cert-manager/cert-manager/releases/download/v1.14.0/cert-manager.yaml

# Attendre que les pods soient prêts
kubectl wait --for=condition=Available --timeout=300s deployment --all -n cert-manager
```

### 2.4 Créer les ClusterIssuers pour Let's Encrypt

```powershell
kubectl apply -f - <<EOF
apiVersion: cert-manager.io/v1
kind: ClusterIssuer
metadata:
  name: letsencrypt-staging
spec:
  acme:
    server: https://acme-staging-v02.api.letsencrypt.org/directory
    email: votre-email@example.com
    privateKeySecretRef:
      name: letsencrypt-staging
    solvers:
    - http01:
        ingress:
          class: nginx
---
apiVersion: cert-manager.io/v1
kind: ClusterIssuer
metadata:
  name: letsencrypt-prod
spec:
  acme:
    server: https://acme-v02.api.letsencrypt.org/directory
    email: votre-email@example.com
    privateKeySecretRef:
      name: letsencrypt-prod
    solvers:
    - http01:
        ingress:
          class: nginx
EOF
```

**⚠️ Remplacez `votre-email@example.com` par votre vraie adresse email !**

### 2.5 Créer les namespaces pour les environnements

```powershell
kubectl create namespace soa-integration
kubectl label namespace soa-integration environment=integration

kubectl create namespace soa-production
kubectl label namespace soa-production environment=production
```

### 2.6 Configurer le SecretProviderClass dans chaque namespace

Le script `setup-smartdish-aks.ps1` a déjà créé le SecretProviderClass dans le namespace `smartdish`, mais nous devons le répliquer pour nos namespaces :

```powershell
# Récupérer les valeurs du script
$KeyVaultName = kubectl get secretproviderclass azure-keyvault-secrets -n smartdish -o jsonpath='{.spec.parameters.keyvaultName}'
$ClientId = kubectl get secretproviderclass azure-keyvault-secrets -n smartdish -o jsonpath='{.spec.parameters.userAssignedIdentityID}'
$TenantId = kubectl get secretproviderclass azure-keyvault-secrets -n smartdish -o jsonpath='{.spec.parameters.tenantId}'

# Créer pour integration
kubectl apply -f - <<EOF
apiVersion: secrets-store.csi.x-k8s.io/v1
kind: SecretProviderClass
metadata:
  name: azure-keyvault-secrets
  namespace: soa-integration
spec:
  provider: azure
  parameters:
    usePodIdentity: "false"
    useVMManagedIdentity: "true"
    userAssignedIdentityID: "$ClientId"
    keyvaultName: "$KeyVaultName"
    cloudName: ""
    objects: |
      array:
        - |
          objectName: mysql-host
          objectType: secret
        - |
          objectName: mysql-port
          objectType: secret
        - |
          objectName: mysql-user
          objectType: secret
        - |
          objectName: mysql-password
          objectType: secret
        - |
          objectName: mysql-database
          objectType: secret
    tenantId: "$TenantId"
  secretObjects:
  - secretName: mysql-secrets
    type: Opaque
    data:
    - objectName: mysql-host
      key: MYSQL_HOST
    - objectName: mysql-port
      key: MYSQL_PORT
    - objectName: mysql-user
      key: MYSQL_USERNAME
    - objectName: mysql-password
      key: MYSQL_PASSWORD
    - objectName: mysql-database
      key: MYSQL_DATABASE
EOF

# Créer pour production
kubectl apply -f - <<EOF
apiVersion: secrets-store.csi.x-k8s.io/v1
kind: SecretProviderClass
metadata:
  name: azure-keyvault-secrets
  namespace: soa-production
spec:
  provider: azure
  parameters:
    usePodIdentity: "false"
    useVMManagedIdentity: "true"
    userAssignedIdentityID: "$ClientId"
    keyvaultName: "$KeyVaultName"
    cloudName: ""
    objects: |
      array:
        - |
          objectName: mysql-host
          objectType: secret
        - |
          objectName: mysql-port
          objectType: secret
        - |
          objectName: mysql-user
          objectType: secret
        - |
          objectName: mysql-password
          objectType: secret
        - |
          objectName: mysql-database
          objectType: secret
    tenantId: "$TenantId"
  secretObjects:
  - secretName: mysql-secrets
    type: Opaque
    data:
    - objectName: mysql-host
      key: MYSQL_HOST
    - objectName: mysql-port
      key: MYSQL_PORT
    - objectName: mysql-user
      key: MYSQL_USERNAME
    - objectName: mysql-password
      key: MYSQL_PASSWORD
    - objectName: mysql-database
      key: MYSQL_DATABASE
EOF
```

---

## 🎯 ÉTAPE 3 : Configurer DNS (Optionnel mais Recommandé)

### Option A : Utiliser un domaine personnalisé

Si vous avez un domaine (ex: `smartdish.app`), configurez un enregistrement DNS :

```
Type: A
Nom: *.smartdish.app
Valeur: <EXTERNAL-IP du Load Balancer>
```

Ensuite, mettez à jour les variables GitHub :
- `BASE_DOMAIN` = `smartdish.app`

### Option B : Utiliser nip.io (pour les tests)

Si vous n'avez pas de domaine, utilisez nip.io (DNS wildcard gratuit) :

```
soa-recipeyoulove-integration.<EXTERNAL-IP>.nip.io
```

Mettez à jour les variables GitHub :
- `BASE_DOMAIN` = `<EXTERNAL-IP>.nip.io`

Exemple : Si votre IP est `20.123.45.67`, alors :
- `BASE_DOMAIN` = `20.123.45.67.nip.io`
- Les microservices seront accessibles sur :
  - `soa-recipeyoulove-integration.20.123.45.67.nip.io`
  - `soa-recipeyoulove-production.20.123.45.67.nip.io`

---

## 🎯 ÉTAPE 4 : Configurer les Secrets GitHub (5 min)

### 4.1 Aller dans les Settings du dépôt

1. Allez sur : `https://github.com/<VOTRE-USERNAME>/RecipeYouLove/settings/secrets/actions`
2. Cliquez sur **"New repository secret"**

### 4.2 Créer les Secrets

| Nom du Secret | Valeur | Description |
|---------------|--------|-------------|
| `AZURE_CREDENTIALS` | JSON du Service Principal | Copié depuis le script PowerShell |
| `AZURE_RESOURCE_GROUP` | `smartdish-rg` | Nom du Resource Group |
| `ACR_LOGIN_SERVER` | `smartdishacr.azurecr.io` | URL du Container Registry |
| `AKS_CLUSTER_NAME` | `smartdish-aks` | Nom du cluster AKS |
| `MYSQL_HOST` | `smartdish-mysql.mysql.database.azure.com` | Host MySQL |
| `SONAR_TOKEN` | (optionnel) | Token SonarQube si vous l'utilisez |

### 4.3 Créer les Variables d'Environnement

Allez dans : `Settings > Secrets and variables > Actions > Variables`

| Nom de la Variable | Valeur | Description |
|-------------------|--------|-------------|
| `BASE_DOMAIN` | `20.123.45.67.nip.io` | Domaine de base (ou votre domaine) |
| `COVERAGE_THRESHOLD` | `60` | Seuil de couverture de code |

### 4.4 Créer un KUBECONFIG secret

```powershell
# Récupérer le kubeconfig et l'encoder en base64
$kubeconfigPath = "$env:USERPROFILE\.kube\config"
$kubeconfigContent = Get-Content $kubeconfigPath -Raw
$kubeconfigBase64 = [Convert]::ToBase64String([Text.Encoding]::UTF8.GetBytes($kubeconfigContent))

# Afficher (copiez cette valeur)
Write-Host "Copiez cette valeur pour le secret KUBECONFIG:"
Write-Host $kubeconfigBase64
```

Créez un secret GitHub `KUBECONFIG` avec cette valeur encodée.

---

## 🎯 ÉTAPE 5 : Tester le Déploiement (10 min)

### 5.1 Pousser le code sur une branche feat/

```powershell
git checkout -b feat/test-infrastructure
git add .
git commit -m "feat: test infrastructure kubernetes"
git push origin feat/test-infrastructure
```

### 5.2 Vérifier le workflow GitHub Actions

1. Allez dans l'onglet **"Actions"** de votre dépôt
2. Vérifiez que le workflow **"CI/CD Pipeline - Orchestrateur"** se lance
3. Attendez que toutes les étapes passent au vert ✅

### 5.3 Vérifier le déploiement sur Kubernetes

```powershell
# Vérifier les pods
kubectl get pods -n soa-integration

# Vérifier les services
kubectl get svc -n soa-integration

# Vérifier les ingress
kubectl get ingress -n soa-integration

# Voir les logs d'un pod
kubectl logs -n soa-integration -l app=recipeyoulove-api --tail=100
```

### 5.4 Tester l'accès à l'application

```powershell
# Récupérer l'URL de l'ingress
$INGRESS_URL = kubectl get ingress -n soa-integration -o jsonpath='{.items[0].spec.rules[0].host}'
Write-Host "URL de l'application : https://$INGRESS_URL"

# Tester l'endpoint de santé
curl https://$INGRESS_URL/actuator/health
```

---

## 🎯 ÉTAPE 6 : Déploiement des Microservices (Forks)

### Pour chaque microservice forké :

1. **Cloner le fork** :
   ```powershell
   git clone https://github.com/<VOTRE-USERNAME>/<NOM-DU-FORK>.git
   cd <NOM-DU-FORK>
   ```

2. **Synchroniser avec le parent** :
   ```powershell
   git remote add upstream https://github.com/EmilieHascoet/RecipeYouLove.git
   git fetch upstream
   git merge upstream/main
   ```

3. **Mettre à jour les secrets GitHub** du fork avec les mêmes valeurs que le parent

4. **Pousser sur une branche feat/** :
   ```powershell
   git checkout -b feat/deploy-microservice
   git push origin feat/deploy-microservice
   ```

5. **Vérifier le déploiement** :
   Le microservice sera automatiquement déployé sur le même cluster AKS, dans son propre namespace.

---

## 📊 Vérification de l'Infrastructure Complète

### Commandes utiles

```powershell
# Vue globale du cluster
kubectl get all --all-namespaces

# Vérifier les pods de tous les environnements
kubectl get pods -n soa-integration
kubectl get pods -n soa-production

# Vérifier les ingress (URLs d'accès)
kubectl get ingress --all-namespaces

# Vérifier les secrets Azure Key Vault
kubectl get secretproviderclass --all-namespaces

# Logs du NGINX Ingress Controller
kubectl logs -n ingress-nginx -l app.kubernetes.io/name=ingress-nginx --tail=100

# Vérifier Cert-Manager
kubectl get certificate --all-namespaces
kubectl get certificaterequest --all-namespaces
```

---

## 🎯 Coûts Estimés

| Ressource | SKU | Coût mensuel | Note |
|-----------|-----|--------------|------|
| AKS | 1 nœud B2s | ~18€ | Essentiel |
| Azure Container Registry | Basic | ~5€ | 10 GB de stockage |
| MySQL Flexible Server | B1s | **GRATUIT** | 750h incluses dans Azure Student |
| Azure Key Vault | Standard | **GRATUIT** | 25k opérations/mois incluses |
| Load Balancer | Basic | **GRATUIT** | Inclus |
| Stockage (PV) | Standard | ~2€ | 30 GB |
| **TOTAL** | | **~25€/mois** | 4 mois avec 100$ de crédit |

---

## ❌ Troubleshooting

### Problème : Les pods ne démarrent pas

```powershell
# Voir les événements du pod
kubectl describe pod <NOM-DU-POD> -n soa-integration

# Voir les logs
kubectl logs <NOM-DU-POD> -n soa-integration --previous
```

### Problème : Les secrets ne sont pas montés

```powershell
# Vérifier le SecretProviderClass
kubectl get secretproviderclass -n soa-integration

# Vérifier les logs du CSI Driver
kubectl logs -n kube-system -l app=secrets-store-csi-driver
```

### Problème : L'ingress ne fonctionne pas

```powershell
# Vérifier que l'Ingress Controller est en cours d'exécution
kubectl get pods -n ingress-nginx

# Vérifier les logs
kubectl logs -n ingress-nginx -l app.kubernetes.io/name=ingress-nginx

# Vérifier que le service a une IP externe
kubectl get svc -n ingress-nginx
```

### Problème : Le certificat SSL ne se génère pas

```powershell
# Vérifier les certificats
kubectl get certificate --all-namespaces

# Voir les détails d'un certificat
kubectl describe certificate <NOM-DU-CERTIFICAT> -n soa-integration

# Vérifier les logs de cert-manager
kubectl logs -n cert-manager -l app=cert-manager
```

---

## 🎉 Prochaines Étapes

Une fois l'infrastructure en place :

1. ✅ Configurer le monitoring avec Azure Monitor
2. ✅ Mettre en place des alertes
3. ✅ Configurer l'autoscaling HPA (Horizontal Pod Autoscaler)
4. ✅ Ajouter des tests d'intégration end-to-end
5. ✅ Documenter les APIs avec Swagger
6. ✅ Mettre en place un système de backup automatique

---

## 📚 Ressources Utiles

- [Documentation Azure AKS](https://learn.microsoft.com/en-us/azure/aks/)
- [Helm Charts](https://helm.sh/docs/)
- [NGINX Ingress Controller](https://kubernetes.github.io/ingress-nginx/)
- [Cert-Manager](https://cert-manager.io/docs/)
- [Azure Key Vault CSI Driver](https://azure.github.io/secrets-store-csi-driver-provider-azure/)

---

**Besoin d'aide ?** Consultez les logs et les issues GitHub du projet.

