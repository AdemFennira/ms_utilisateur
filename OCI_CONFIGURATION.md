# Configuration Oracle Cloud Infrastructure (OCI)

## Vue d'ensemble

Ce projet utilise Oracle Cloud Infrastructure pour :
- **OKE (Oracle Kubernetes Engine)** : Cluster Kubernetes pour héberger les microservices
- **OCIR (Oracle Cloud Container Registry)** : Registry Docker pour stocker les images
- **MySQL Database Service** : Base de données MySQL managée
- **Vault** : Gestion des secrets (optionnel, peut utiliser Kubernetes Secrets)

## Architecture

### Environnements

1. **Integration** (`smartdish` namespace)
   - Branche: `feat/**`, `fix/**`, `develop`
   - Domaine: `*.smartdish-integration.cloud`
   - Replicas: 1 pod par microservice

2. **Production** (`smartdish-prod` namespace)
   - Branche: `main`
   - Domaine: `*.smartdish.cloud`
   - Replicas: 2+ pods par microservice avec autoscaling

### Structure des URLs

- **Integration**: `https://soa-{microservice-name}-integration.smartdish-integration.cloud`
- **Production**: `https://soa-{microservice-name}.smartdish.cloud`

Exemple :
- Parent (Integration): `https://soa-api-integration.smartdish-integration.cloud`
- Parent (Production): `https://soa-api.smartdish.cloud`

## Prérequis

### 1. Cluster OKE

Votre cluster OKE est déjà créé :
- **Cluster ID**: `...khi5vnahyc14ozq2ulnwstf3t6hslnvmomgeq5ulqsvca3gwzgw3tq`
- **API Endpoint**: `https://10.0.0.6:6445` (privé) / `https://141.253.126.64:6443` (public)
- **Region**: France Central (Paris)
- **VCN**: `oke-vcn-quick-K3s-cluster-42186fdb7`

### 2. Secrets GitHub à configurer

Accédez à `Settings > Secrets and variables > Actions` de votre repository et créez :

#### Secrets OCI
```yaml
OCI_USERNAME: <tenancy-namespace>/<votre-username>
OCI_AUTH_TOKEN: <votre-auth-token>
OCI_TENANCY_NAMESPACE: <tenancy-namespace>
OCI_KUBECONFIG: <kubeconfig-base64-encoded>
```

#### Secrets MySQL
```yaml
MYSQL_HOST: <mysql-host>
MYSQL_USER: admin
MYSQL_PASSWORD: <mysql-password>
MYSQL_ROOT_PASSWORD: <mysql-root-password>
```

#### Secrets SonarQube
```yaml
SONAR_TOKEN: <sonar-token>
SONAR_HOST_URL: <sonar-url>
```

### 3. Variables GitHub à configurer

Dans `Settings > Secrets and variables > Actions > Variables` :

```yaml
MICROSERVICE_NAME: smartdish-parent
COVERAGE_THRESHOLD: 60
```

## Configuration détaillée

### 1. Obtenir les informations OCI

#### a) Tenancy Namespace
```bash
oci os ns get
```

#### b) Générer un Auth Token
1. Console OCI > User Settings > Auth Tokens
2. Cliquer sur "Generate Token"
3. Copier le token (visible une seule fois)

#### c) Username pour OCIR
Format: `<tenancy-namespace>/<username>`
Exemple: `axgbvr6e8mzp/oracleidentitycloudservice/user@example.com`

### 2. Configurer kubeconfig

#### a) Installer OCI CLI
```bash
# Windows (PowerShell)
Invoke-WebRequest -Uri https://raw.githubusercontent.com/oracle/oci-cli/master/scripts/install/install.ps1 -OutFile install.ps1
.\install.ps1

# Linux/Mac
bash -c "$(curl -L https://raw.githubusercontent.com/oracle/oci-cli/master/scripts/install/install.sh)"
```

#### b) Configurer OCI CLI
```bash
oci setup config
```

#### c) Obtenir le kubeconfig
```bash
# Remplacez <cluster-id> par votre Cluster OCID
oci ce cluster create-kubeconfig \
  --cluster-id <cluster-id> \
  --file ~/.kube/config \
  --region eu-paris-1 \
  --token-version 2.0.0
```

Avec votre cluster :
```bash
oci ce cluster create-kubeconfig \
  --cluster-id ocid1.cluster.oc1.eu-paris-1.aaaaaaaaafkhi5vnahyc14ozq2ulnwstf3t6hslnvmomgeq5ulqsvca3gwzgw3tq \
  --file ~/.kube/config \
  --region eu-paris-1 \
  --token-version 2.0.0
```

#### d) Encoder le kubeconfig en base64
```bash
# Linux/Mac
cat ~/.kube/config | base64 -w 0

# Windows PowerShell
[Convert]::ToBase64String([System.IO.File]::ReadAllBytes("$env:USERPROFILE\.kube\config"))
```

Copiez le résultat dans le secret `OCI_KUBECONFIG`.

### 3. Créer la base de données MySQL sur OCI

#### Option 1 : MySQL Database Service (recommandé)
1. Console OCI > Databases > MySQL > DB Systems
2. Create DB System
3. Configuration :
   - **Name**: smartdish-mysql
   - **Shape**: MySQL.VM.Standard.E3.1.8GB (Free Tier si disponible)
   - **Storage**: 50GB
   - **Username**: admin
   - **Password**: <votre-mot-de-passe>
   - **VCN**: Sélectionnez le même VCN que votre cluster OKE
   - **Subnet**: Private subnet

4. Récupérez l'endpoint après création

#### Option 2 : MySQL dans Kubernetes (dev/test)
```bash
kubectl apply -f k8s/oci/mysql-deployment.yaml
```

### 4. Installer les composants Kubernetes

#### a) NGINX Ingress Controller
```bash
kubectl apply -f https://raw.githubusercontent.com/kubernetes/ingress-nginx/controller-v1.9.4/deploy/static/provider/cloud/deploy.yaml
```

#### b) Cert-Manager (pour HTTPS)
```bash
kubectl apply -f https://github.com/cert-manager/cert-manager/releases/download/v1.13.0/cert-manager.yaml
```

#### c) Créer les namespaces
```bash
kubectl apply -f k8s/oci/namespace.yaml
```

#### d) Configurer les secrets
```bash
# MySQL secrets
export MYSQL_HOST="<mysql-host>"
export MYSQL_USER="admin"
export MYSQL_PASSWORD="<password>"
export MYSQL_ROOT_PASSWORD="<root-password>"

envsubst < k8s/oci/mysql-secrets.yaml | kubectl apply -f -

# ConfigMaps
kubectl apply -f k8s/oci/configmap.yaml
```

### 5. Configuration DNS

Pour utiliser les domaines personnalisés, vous devez :

1. **Obtenir l'IP publique du Load Balancer** :
```bash
kubectl get svc -n ingress-nginx ingress-nginx-controller
```

2. **Configurer vos enregistrements DNS** :
   - Type: A
   - Nom: `*.smartdish-integration.cloud`
   - Valeur: `<IP-du-Load-Balancer>`
   
   - Type: A
   - Nom: `*.smartdish.cloud`
   - Valeur: `<IP-du-Load-Balancer>`

3. **Ou utiliser nip.io pour les tests** :
   Format: `soa-api.<IP>.nip.io`
   Exemple: `soa-api.141.253.126.64.nip.io`

## Déploiement manuel

### Déployer un microservice avec Helm

```bash
# Integration
helm upgrade --install smartdish-parent ./helm/smartdish \
  --namespace smartdish \
  --create-namespace \
  --values ./helm/smartdish/values-integration.yaml \
  --set image.repository=fra.ocir.io/<tenancy>/smartdish/smartdish-parent \
  --set image.tag=v1.0.0 \
  --set microserviceName=smartdish-parent

# Production
helm upgrade --install smartdish-parent ./helm/smartdish \
  --namespace smartdish-prod \
  --create-namespace \
  --values ./helm/smartdish/values-production.yaml \
  --set image.repository=fra.ocir.io/<tenancy>/smartdish/smartdish-parent \
  --set image.tag=v1.0.0 \
  --set microserviceName=smartdish-parent
```

### Vérifier le déploiement

```bash
# Pods
kubectl get pods -n smartdish

# Services
kubectl get svc -n smartdish

# Ingress
kubectl get ingress -n smartdish

# Logs
kubectl logs -f deployment/smartdish-parent -n smartdish
```

## CI/CD Pipeline

### Workflow

1. **Push sur `feat/**` ou `fix/**`** :
   - Build Maven
   - Tests unitaires (couverture >= 60%)
   - Build Docker Image → OCIR
   - Security Scan (Trivy)
   - Deploy sur Integration

2. **Pull Request vers `main`** :
   - Build Maven
   - Tests + Couverture
   - SonarQube Analysis

3. **Merge vers `main`** :
   - Build Maven
   - Tests + Couverture
   - Build Docker Image → OCIR
   - Security Scan
   - Deploy sur Production

### Tags Docker

- `feat/**` → `feat-{branch}-{short-sha}`
- `fix/**` → `fix-{branch}-{short-sha}`
- `main` → `v{version}` + `latest`

## Monitoring et Logs

### Accéder aux logs

```bash
# Logs d'un pod
kubectl logs -f <pod-name> -n smartdish

# Logs de tous les pods d'un déploiement
kubectl logs -f deployment/smartdish-parent -n smartdish

# Logs avec stern (plus pratique)
stern smartdish -n smartdish
```

### Métriques

```bash
# CPU/Mémoire des pods
kubectl top pods -n smartdish

# CPU/Mémoire des nodes
kubectl top nodes
```

## Troubleshooting

### Les pods ne démarrent pas

```bash
kubectl describe pod <pod-name> -n smartdish
kubectl logs <pod-name> -n smartdish
```

### L'image n'est pas trouvée

Vérifiez le secret OCIR :
```bash
kubectl get secret ocir-secret -n smartdish -o yaml
```

### Les secrets MySQL ne sont pas accessibles

```bash
kubectl get secret mysql-secrets -n smartdish -o yaml
kubectl describe secret mysql-secrets -n smartdish
```

### L'ingress ne fonctionne pas

```bash
kubectl get ingress -n smartdish
kubectl describe ingress smartdish-ingress -n smartdish
kubectl logs -n ingress-nginx deployment/ingress-nginx-controller
```

## Ressources

- [OCI Documentation](https://docs.oracle.com/en-us/iaas/Content/home.htm)
- [OKE Documentation](https://docs.oracle.com/en-us/iaas/Content/ContEng/home.htm)
- [OCIR Documentation](https://docs.oracle.com/en-us/iaas/Content/Registry/home.htm)
- [Helm Documentation](https://helm.sh/docs/)
- [Kubernetes Documentation](https://kubernetes.io/docs/)

