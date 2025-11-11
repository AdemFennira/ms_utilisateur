# SmartDish Helm Chart

Chart Helm pour déployer les microservices SmartDish sur Kubernetes (OKE).

## Structure

```
helm/smartdish/
├── Chart.yaml                    # Métadonnées du chart
├── values.yaml                   # Valeurs par défaut
├── values-integration.yaml       # Valeurs pour l'environnement d'intégration
├── values-production.yaml        # Valeurs pour l'environnement de production
└── manifests/
    ├── _helpers.tpl             # Templates helpers
    ├── deployment.yaml          # Template Deployment
    ├── service.yaml             # Template Service
    ├── ingress.yaml             # Template Ingress
    └── hpa.yaml                 # Template HorizontalPodAutoscaler
```

## Utilisation

### Installation basique

```bash
helm install smartdish-parent ./helm/smartdish \
  --namespace smartdish \
  --create-namespace
```

### Installation avec valeurs personnalisées

#### Environnement d'intégration

```bash
helm upgrade --install smartdish-parent ./helm/smartdish \
  --namespace smartdish \
  --create-namespace \
  --values ./helm/smartdish/values-integration.yaml \
  --set image.repository=fra.ocir.io/<tenancy>/smartdish/smartdish-parent \
  --set image.tag=v1.0.0 \
  --set microserviceName=smartdish-parent
```

#### Environnement de production

```bash
helm upgrade --install smartdish-parent ./helm/smartdish \
  --namespace smartdish-prod \
  --create-namespace \
  --values ./helm/smartdish/values-production.yaml \
  --set image.repository=fra.ocir.io/<tenancy>/smartdish/smartdish-parent \
  --set image.tag=v1.0.0 \
  --set microserviceName=smartdish-parent
```

## Configuration

### Valeurs principales

| Paramètre | Description | Défaut |
|-----------|-------------|---------|
| `replicaCount` | Nombre de réplicas | `1` |
| `image.repository` | Repository de l'image Docker | `fra.ocir.io/tenancy/smartdish` |
| `image.tag` | Tag de l'image | `latest` |
| `image.pullPolicy` | Politique de pull de l'image | `Always` |
| `service.type` | Type de service Kubernetes | `ClusterIP` |
| `service.port` | Port du service | `8090` |
| `ingress.enabled` | Activer l'ingress | `true` |
| `ingress.className` | Classe d'ingress | `nginx` |
| `resources.limits.cpu` | Limite CPU | `500m` |
| `resources.limits.memory` | Limite mémoire | `512Mi` |
| `resources.requests.cpu` | Requête CPU | `250m` |
| `resources.requests.memory` | Requête mémoire | `256Mi` |

### Variables d'environnement

Les variables d'environnement sont chargées depuis :
- **ConfigMap** : `app-config` (variables non sensibles)
- **Secret** : `mysql-secrets` (informations MySQL)

Variables disponibles :
- `SPRING_PROFILES_ACTIVE` : Profil Spring actif
- `LOG_LEVEL` : Niveau de logs général
- `LOG_LEVEL_JDBC` : Niveau de logs JDBC
- `MYSQL_HOST` : Hôte MySQL
- `MYSQL_PORT` : Port MySQL
- `MYSQL_DATABASE` : Nom de la base de données
- `MYSQL_USER` : Utilisateur MySQL
- `MYSQL_PASSWORD` : Mot de passe MySQL

### Autoscaling

L'autoscaling est désactivé par défaut en intégration et activé en production.

Configuration production :
```yaml
autoscaling:
  enabled: true
  minReplicas: 2
  maxReplicas: 5
  targetCPUUtilizationPercentage: 70
  targetMemoryUtilizationPercentage: 80
```

### Ingress

L'ingress est configuré automatiquement avec :
- NGINX Ingress Controller
- Cert-Manager pour les certificats SSL
- Redirection HTTPS forcée

Format des URLs :
- **Integration** : `https://soa-{microservice}-integration.smartdish-integration.cloud`
- **Production** : `https://soa-{microservice}.smartdish.cloud`

## Commandes utiles

### Lister les releases

```bash
helm list -n smartdish
helm list -n smartdish-prod
```

### Voir les valeurs déployées

```bash
helm get values smartdish-parent -n smartdish
```

### Historique des déploiements

```bash
helm history smartdish-parent -n smartdish
```

### Rollback

```bash
helm rollback smartdish-parent 1 -n smartdish
```

### Désinstaller

```bash
helm uninstall smartdish-parent -n smartdish
```

### Debug / Dry-run

```bash
helm install smartdish-parent ./helm/smartdish \
  --namespace smartdish \
  --dry-run \
  --debug
```

### Tester le chart

```bash
helm lint ./helm/smartdish
```

## Dépendances

Le chart nécessite :
- Kubernetes 1.24+
- NGINX Ingress Controller
- Cert-Manager (pour SSL)
- ConfigMap `app-config` créé dans le namespace
- Secret `mysql-secrets` créé dans le namespace
- Secret `ocir-secret` pour pull les images

## Troubleshooting

### Les pods ne démarrent pas

```bash
# Vérifier le déploiement
kubectl describe deployment smartdish-parent -n smartdish

# Vérifier les pods
kubectl get pods -n smartdish
kubectl describe pod <pod-name> -n smartdish
kubectl logs <pod-name> -n smartdish
```

### Problème avec les secrets

```bash
# Vérifier que les secrets existent
kubectl get secret -n smartdish

# Voir les détails
kubectl describe secret mysql-secrets -n smartdish
kubectl describe secret ocir-secret -n smartdish
```

### Problème d'ingress

```bash
# Vérifier l'ingress
kubectl get ingress -n smartdish
kubectl describe ingress smartdish-parent -n smartdish

# Vérifier NGINX
kubectl get pods -n ingress-nginx
kubectl logs -n ingress-nginx deployment/ingress-nginx-controller
```

## Exemple de déploiement complet

```bash
# 1. Créer le namespace
kubectl create namespace smartdish

# 2. Créer les secrets MySQL
kubectl create secret generic mysql-secrets \
  --from-literal=MYSQL_HOST=mysql.example.com \
  --from-literal=MYSQL_PORT=3306 \
  --from-literal=MYSQL_DATABASE=smartdish \
  --from-literal=MYSQL_USER=admin \
  --from-literal=MYSQL_PASSWORD=password \
  --from-literal=MYSQL_ROOT_PASSWORD=rootpassword \
  --namespace=smartdish

# 3. Créer la ConfigMap
kubectl apply -f k8s/oci/configmap.yaml

# 4. Créer le secret OCIR
kubectl create secret docker-registry ocir-secret \
  --docker-server=fra.ocir.io \
  --docker-username=<tenancy-namespace>/<username> \
  --docker-password=<auth-token> \
  --namespace=smartdish

# 5. Déployer avec Helm
helm upgrade --install smartdish-parent ./helm/smartdish \
  --namespace smartdish \
  --values ./helm/smartdish/values-integration.yaml \
  --set image.repository=fra.ocir.io/<tenancy>/smartdish/smartdish-parent \
  --set image.tag=v1.0.0 \
  --set microserviceName=smartdish-parent \
  --wait

# 6. Vérifier le déploiement
kubectl get all -n smartdish
```

## Support

Pour plus d'informations, consultez la [documentation principale](../../README.md) et le [guide de configuration OCI](../../OCI_CONFIGURATION.md).

