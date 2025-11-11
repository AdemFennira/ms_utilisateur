# SmartDish - Application de Recommandation de Recettes

Application permettant de recommander des recettes à l'utilisateur en fonction des ingrédients saisis et de ses retours. Le système intègre un agent d'apprentissage par renforcement (RL) qui ajuste ses recommandations au fil du temps.

## Architecture

### Microservices

Ce projet est structuré en architecture microservices avec :
- **Parent Repository** (ce dépôt) : Template et configuration CI/CD partagée
- **Microservices forkés** : Chaque microservice est un fork de ce repository

### Infrastructure

- **Cloud Provider** : Oracle Cloud Infrastructure (OCI)
- **Kubernetes** : Oracle Kubernetes Engine (OKE)
- **Container Registry** : Oracle Cloud Container Registry (OCIR)
- **Base de données** : MySQL Database Service (OCI) ou MySQL sur Kubernetes
- **CI/CD** : GitHub Actions
- **Gestion des secrets** : Kubernetes Secrets (ou Vault)

### Environnements

1. **Integration** (`smartdish` namespace)
   - Déclenchement : Push sur `feat/**`, `fix/**`, `develop`
   - URL : `https://soa-{microservice}-integration.smartdish-integration.cloud`
   - Replicas : 1 pod par microservice

2. **Production** (`smartdish-prod` namespace)
   - Déclenchement : Merge sur `main`
   - URL : `https://soa-{microservice}.smartdish.cloud`
   - Replicas : 2+ pods avec autoscaling

## Prérequis

### Développement local

- Java 21
- Maven 3.8+
- Docker
- Git

### Déploiement

- Compte Oracle Cloud Infrastructure (OCI)
- Cluster OKE configuré
- OCI CLI installé
- kubectl installé
- Helm 3 installé

## Installation

### 1. Configuration du cluster OKE

Suivez le guide détaillé : [OCI_CONFIGURATION.md](./OCI_CONFIGURATION.md)

Ou exécutez le script automatique :

```bash
chmod +x oci-scripts/setup-oke.sh
./oci-scripts/setup-oke.sh
```

### 2. Configuration des secrets GitHub

Allez dans `Settings > Secrets and variables > Actions` de votre repository.

#### Secrets requis

```yaml
# OCI
OCI_USERNAME: <tenancy-namespace>/<username>
OCI_AUTH_TOKEN: <auth-token>
OCI_TENANCY_NAMESPACE: <tenancy-namespace>
OCI_KUBECONFIG: <kubeconfig-base64>

# MySQL
MYSQL_HOST: <mysql-host>
MYSQL_USER: admin
MYSQL_PASSWORD: <password>
MYSQL_ROOT_PASSWORD: <root-password>

# SonarQube
SONAR_TOKEN: <token>
SONAR_HOST_URL: <url>
```

#### Variables requises

```yaml
MICROSERVICE_NAME: smartdish-parent
COVERAGE_THRESHOLD: 60
```

### 3. Forker pour un microservice

1. Forkez ce repository
2. Renommez-le selon votre microservice (ex: `smartdish-user-service`)
3. Modifiez la variable `MICROSERVICE_NAME` dans les secrets GitHub
4. Développez votre microservice dans `src/`
5. Les workflows CI/CD sont automatiquement disponibles

## Développement

### Structure du projet

```
.
├── .github/workflows/       # Workflows CI/CD
├── helm/smartdish/          # Helm Chart pour le déploiement
├── k8s/oci/                 # Manifests Kubernetes pour OCI
├── oci-scripts/             # Scripts de configuration OCI
├── src/                     # Code source Java
├── Dockerfile               # Image Docker
├── pom.xml                  # Configuration Maven
└── OCI_CONFIGURATION.md     # Guide de configuration détaillé
```

### Build local

```bash
# Compiler le projet
mvn clean package

# Exécuter les tests
mvn test

# Vérifier la couverture de code
mvn test jacoco:report
# Rapport : target/site/jacoco/index.html

# Build Docker
docker build -t smartdish:local .

# Exécuter localement
docker run -p 8090:8090 smartdish:local
```

### Tests

```bash
# Tests unitaires
mvn test

# Tests avec couverture
mvn clean test jacoco:report

# Le seuil de couverture requis est de 60%
```

## CI/CD Pipeline

### Workflow automatique

```
Push feat/** ou fix/**
    ↓
┌─────────────────────┐
│ 1. Build Maven      │
│    - Compile        │
│    - Tests          │
│    - Package JAR    │
└─────────────────────┘
    ↓
┌─────────────────────┐
│ 2. Check Coverage   │
│    - JaCoCo Report  │
│    - Threshold: 60% │
└─────────────────────┘
    ↓
┌─────────────────────┐
│ 3. Build Docker     │
│    - Build Image    │
│    - Push to OCIR   │
└─────────────────────┘
    ↓
┌─────────────────────┐
│ 4. Security Scan    │
│    - Trivy          │
│    - SARIF Report   │
└─────────────────────┘
    ↓
┌─────────────────────┐
│ 5. Deploy K8s       │
│    - Helm upgrade   │
│    - Integration    │
└─────────────────────┘
```

### Pull Request vers main

```
Pull Request → main
    ↓
┌─────────────────────┐
│ 1. Build & Tests    │
└─────────────────────┘
    ↓
┌─────────────────────┐
│ 2. SonarQube        │
│    - Quality Gate   │
│    - Code Smells    │
│    - Security       │
└─────────────────────┘
```

### Merge vers main

```
Merge → main
    ↓
[Build → Tests → Coverage → Docker → Security → Deploy Production]
```

## Déploiement manuel

### Avec Helm

```bash
# Integration
helm upgrade --install smartdish-parent ./helm/smartdish \
  --namespace smartdish \
  --values ./helm/smartdish/values-integration.yaml \
  --set image.repository=fra.ocir.io/<tenancy>/smartdish/smartdish-parent \
  --set image.tag=v1.0.0 \
  --set microserviceName=smartdish-parent

# Production
helm upgrade --install smartdish-parent ./helm/smartdish \
  --namespace smartdish-prod \
  --values ./helm/smartdish/values-production.yaml \
  --set image.repository=fra.ocir.io/<tenancy>/smartdish/smartdish-parent \
  --set image.tag=v1.0.0 \
  --set microserviceName=smartdish-parent
```

### Avec kubectl

```bash
# Appliquer les configurations
kubectl apply -f k8s/oci/namespace.yaml
kubectl apply -f k8s/oci/configmap.yaml

# Configurer les secrets
export MYSQL_HOST="mysql.example.com"
export MYSQL_USER="admin"
export MYSQL_PASSWORD="password"
export MYSQL_ROOT_PASSWORD="rootpassword"

envsubst < k8s/oci/mysql-secrets.yaml | kubectl apply -f -

# Déployer avec le template
export MICROSERVICE_NAME="smartdish-parent"
export NAMESPACE="smartdish"
export IMAGE_URL="fra.ocir.io/tenancy/smartdish/smartdish-parent"
export IMAGE_TAG="v1.0.0"
export REPLICAS="1"
export VERSION="1.0.0"

envsubst < k8s/oci/deployment-template.yaml | kubectl apply -f -
```

## Monitoring

### Vérifier l'état des pods

```bash
kubectl get pods -n smartdish
kubectl logs -f deployment/smartdish-parent -n smartdish
kubectl describe pod <pod-name> -n smartdish
```

### Vérifier les services et ingress

```bash
kubectl get svc -n smartdish
kubectl get ingress -n smartdish
```

### Métriques

```bash
kubectl top pods -n smartdish
kubectl top nodes
```

## Troubleshooting

### Les pods ne démarrent pas

```bash
kubectl describe pod <pod-name> -n smartdish
kubectl logs <pod-name> -n smartdish
```

### Problème de connexion MySQL

```bash
# Vérifier les secrets
kubectl get secret mysql-secrets -n smartdish -o yaml

# Tester la connexion depuis un pod
kubectl run -it --rm debug --image=mysql:8 --restart=Never -n smartdish -- \
  mysql -h <mysql-host> -u <user> -p
```

### Image Docker non trouvée

```bash
# Vérifier le secret OCIR
kubectl get secret ocir-secret -n smartdish -o yaml

# Recréer le secret
kubectl delete secret ocir-secret -n smartdish
kubectl create secret docker-registry ocir-secret \
  --docker-server=fra.ocir.io \
  --docker-username=<tenancy-namespace>/<username> \
  --docker-password=<auth-token> \
  --namespace=smartdish
```

### Ingress ne fonctionne pas

```bash
# Vérifier NGINX Ingress
kubectl get pods -n ingress-nginx
kubectl logs -n ingress-nginx deployment/ingress-nginx-controller

# Vérifier l'ingress
kubectl describe ingress smartdish-ingress -n smartdish
```

## Documentation

- [Configuration OCI complète](./OCI_CONFIGURATION.md)
- [Helm Charts](./helm/smartdish/README.md)
- [Dockerfile](./Dockerfile)

## Support

Pour toute question ou problème :
1. Consultez la documentation OCI
2. Vérifiez les logs des pods
3. Consultez les issues GitHub

## License

Ce projet est sous licence MIT.

