# Aide-mémoire : Commandes OCI et Kubernetes

## 🔧 OCI CLI

### Configuration initiale

```bash
# Installer OCI CLI
# Windows PowerShell (admin)
Invoke-WebRequest -Uri https://raw.githubusercontent.com/oracle/oci-cli/master/scripts/install/install.ps1 -OutFile install.ps1
.\install.ps1

# Configurer OCI CLI
oci setup config

# Tester la configuration
oci iam region list
```

### Cluster OKE

```bash
# Obtenir le kubeconfig
oci ce cluster create-kubeconfig \
  --cluster-id <cluster-ocid> \
  --file ~/.kube/config \
  --region eu-paris-1 \
  --token-version 2.0.0

# Lister les clusters
oci ce cluster list --compartment-id <compartment-ocid>

# Détails d'un cluster
oci ce cluster get --cluster-id <cluster-ocid>
```

### Container Registry (OCIR)

```bash
# Obtenir le tenancy namespace
oci os ns get

# Lister les repositories
oci artifacts container repository list --compartment-id <compartment-ocid>

# Lister les images dans un repository
oci artifacts container image list \
  --compartment-id <compartment-ocid> \
  --repository-name smartdish/smartdish-parent
```

### MySQL Database

```bash
# Lister les DB Systems
oci mysql db-system list --compartment-id <compartment-ocid>

# Détails d'un DB System
oci mysql db-system get --db-system-id <db-system-ocid>
```

## ☸️ Kubernetes (kubectl)

### Contexte et Configuration

```bash
# Afficher le contexte actuel
kubectl config current-context

# Lister tous les contextes
kubectl config get-contexts

# Changer de contexte
kubectl config use-context <context-name>

# Vérifier la connexion
kubectl cluster-info
kubectl get nodes
```

### Namespaces

```bash
# Lister les namespaces
kubectl get namespaces

# Créer un namespace
kubectl create namespace smartdish

# Supprimer un namespace
kubectl delete namespace smartdish

# Utiliser un namespace par défaut
kubectl config set-context --current --namespace=smartdish
```

### Pods

```bash
# Lister les pods
kubectl get pods -n smartdish

# Détails d'un pod
kubectl describe pod <pod-name> -n smartdish

# Logs d'un pod
kubectl logs <pod-name> -n smartdish

# Logs en temps réel
kubectl logs -f <pod-name> -n smartdish

# Logs des 100 dernières lignes
kubectl logs --tail=100 <pod-name> -n smartdish

# Se connecter à un pod
kubectl exec -it <pod-name> -n smartdish -- /bin/bash

# Supprimer un pod
kubectl delete pod <pod-name> -n smartdish
```

### Deployments

```bash
# Lister les deployments
kubectl get deployments -n smartdish

# Détails d'un deployment
kubectl describe deployment smartdish-parent -n smartdish

# Scaler un deployment
kubectl scale deployment smartdish-parent --replicas=3 -n smartdish

# Redémarrer un deployment
kubectl rollout restart deployment smartdish-parent -n smartdish

# Historique des rollouts
kubectl rollout history deployment smartdish-parent -n smartdish

# Rollback au déploiement précédent
kubectl rollout undo deployment smartdish-parent -n smartdish

# Rollback à une version spécifique
kubectl rollout undo deployment smartdish-parent --to-revision=2 -n smartdish

# Status du rollout
kubectl rollout status deployment smartdish-parent -n smartdish
```

### Services

```bash
# Lister les services
kubectl get services -n smartdish
kubectl get svc -n smartdish

# Détails d'un service
kubectl describe service smartdish-parent -n smartdish

# Tester un service (port-forward)
kubectl port-forward service/smartdish-parent 8090:8090 -n smartdish
```

### Ingress

```bash
# Lister les ingress
kubectl get ingress -n smartdish

# Détails d'un ingress
kubectl describe ingress smartdish-ingress -n smartdish

# Logs de NGINX Ingress Controller
kubectl logs -n ingress-nginx deployment/ingress-nginx-controller
```

### ConfigMaps et Secrets

```bash
# Lister les ConfigMaps
kubectl get configmaps -n smartdish

# Voir un ConfigMap
kubectl get configmap app-config -n smartdish -o yaml
kubectl describe configmap app-config -n smartdish

# Lister les Secrets
kubectl get secrets -n smartdish

# Voir un Secret (encodé)
kubectl get secret mysql-secrets -n smartdish -o yaml

# Décoder un secret
kubectl get secret mysql-secrets -n smartdish -o jsonpath='{.data.MYSQL_PASSWORD}' | base64 -d

# Créer un Secret
kubectl create secret generic mysql-secrets \
  --from-literal=MYSQL_HOST=mysql.example.com \
  --from-literal=MYSQL_PORT=3306 \
  --from-literal=MYSQL_DATABASE=smartdish \
  --from-literal=MYSQL_USER=admin \
  --from-literal=MYSQL_PASSWORD=password \
  --namespace=smartdish

# Créer un Secret Docker Registry
kubectl create secret docker-registry ocir-secret \
  --docker-server=fra.ocir.io \
  --docker-username=<tenancy-namespace>/<username> \
  --docker-password=<auth-token> \
  --namespace=smartdish

# Supprimer un Secret
kubectl delete secret mysql-secrets -n smartdish
```

### Ressources globales

```bash
# Tout dans un namespace
kubectl get all -n smartdish

# Tous les événements
kubectl get events -n smartdish --sort-by='.lastTimestamp'

# Métriques CPU/Mémoire des pods
kubectl top pods -n smartdish

# Métriques des nodes
kubectl top nodes

# Ressources utilisées par namespace
kubectl top pods --all-namespaces
```

### Apply et Delete

```bash
# Appliquer un fichier YAML
kubectl apply -f k8s/oci/namespace.yaml

# Appliquer un dossier entier
kubectl apply -f k8s/oci/

# Supprimer depuis un fichier
kubectl delete -f k8s/oci/namespace.yaml

# Dry-run (test sans appliquer)
kubectl apply -f k8s/oci/deployment.yaml --dry-run=client

# Afficher le YAML qui serait appliqué
kubectl apply -f k8s/oci/deployment.yaml --dry-run=client -o yaml
```

## 🎩 Helm

### Installation et gestion des releases

```bash
# Installer un chart
helm install smartdish-parent ./helm/smartdish \
  --namespace smartdish \
  --create-namespace

# Upgrade d'une release (ou install si n'existe pas)
helm upgrade --install smartdish-parent ./helm/smartdish \
  --namespace smartdish

# Lister les releases
helm list -n smartdish
helm list --all-namespaces

# Status d'une release
helm status smartdish-parent -n smartdish

# Historique d'une release
helm history smartdish-parent -n smartdish

# Rollback d'une release
helm rollback smartdish-parent 1 -n smartdish

# Désinstaller une release
helm uninstall smartdish-parent -n smartdish
```

### Valeurs et configuration

```bash
# Afficher les valeurs par défaut
helm show values ./helm/smartdish

# Afficher les valeurs déployées
helm get values smartdish-parent -n smartdish

# Afficher toutes les valeurs (défaut + custom)
helm get values smartdish-parent -n smartdish --all

# Installer avec des valeurs personnalisées
helm install smartdish-parent ./helm/smartdish \
  --namespace smartdish \
  --values ./helm/smartdish/values-integration.yaml \
  --set image.tag=v1.0.0 \
  --set replicaCount=2

# Dry-run (tester sans installer)
helm install smartdish-parent ./helm/smartdish \
  --namespace smartdish \
  --dry-run \
  --debug
```

### Debug et validation

```bash
# Valider la syntaxe d'un chart
helm lint ./helm/smartdish

# Afficher les manifests qui seraient générés
helm template smartdish-parent ./helm/smartdish \
  --namespace smartdish \
  --values ./helm/smartdish/values-integration.yaml

# Comparer les valeurs
diff <(helm get values smartdish-parent -n smartdish) \
     <(helm show values ./helm/smartdish)
```

### Repos Helm

```bash
# Ajouter un repo
helm repo add bitnami https://charts.bitnami.com/bitnami

# Lister les repos
helm repo list

# Mettre à jour les repos
helm repo update

# Rechercher un chart
helm search repo mysql
```

## 🐳 Docker

### Build et Push vers OCIR

```bash
# Login à OCIR
docker login fra.ocir.io \
  --username <tenancy-namespace>/<username> \
  --password <auth-token>

# Build
docker build -t smartdish:local .

# Tag pour OCIR
docker tag smartdish:local fra.ocir.io/<tenancy-namespace>/smartdish/smartdish-parent:v1.0.0

# Push vers OCIR
docker push fra.ocir.io/<tenancy-namespace>/smartdish/smartdish-parent:v1.0.0

# Lister les images locales
docker images | grep smartdish

# Supprimer une image
docker rmi <image-id>
```

### Troubleshooting

```bash
# Inspecter une image
docker inspect fra.ocir.io/<tenancy-namespace>/smartdish/smartdish-parent:v1.0.0

# Vérifier qu'une image existe dans OCIR
docker manifest inspect fra.ocir.io/<tenancy-namespace>/smartdish/smartdish-parent:v1.0.0

# Exécuter localement
docker run -p 8090:8090 smartdish:local

# Logs d'un conteneur
docker logs <container-id>

# Se connecter à un conteneur
docker exec -it <container-id> /bin/bash
```

## 🔍 Troubleshooting Kubernetes

### Problème de démarrage des pods

```bash
# 1. Vérifier le status
kubectl get pods -n smartdish

# 2. Détails du pod
kubectl describe pod <pod-name> -n smartdish

# 3. Logs
kubectl logs <pod-name> -n smartdish

# 4. Logs du conteneur précédent (si crash)
kubectl logs <pod-name> -n smartdish --previous

# 5. Events du namespace
kubectl get events -n smartdish --sort-by='.lastTimestamp'
```

### Problème avec les images

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

# Tester manuellement le pull
docker pull fra.ocir.io/<tenancy-namespace>/smartdish/smartdish-parent:v1.0.0
```

### Problème avec MySQL

```bash
# Vérifier le secret
kubectl get secret mysql-secrets -n smartdish -o yaml

# Tester la connexion depuis un pod
kubectl run -it --rm debug --image=mysql:8 --restart=Never -n smartdish -- \
  mysql -h <mysql-host> -u admin -p

# Vérifier les variables d'environnement dans le pod
kubectl exec <pod-name> -n smartdish -- env | grep MYSQL
```

### Problème avec l'Ingress

```bash
# Vérifier NGINX Ingress Controller
kubectl get pods -n ingress-nginx
kubectl logs -n ingress-nginx deployment/ingress-nginx-controller

# Vérifier l'Ingress
kubectl get ingress -n smartdish
kubectl describe ingress smartdish-ingress -n smartdish

# Obtenir l'IP du Load Balancer
kubectl get svc -n ingress-nginx ingress-nginx-controller

# Tester avec curl
curl -H "Host: soa-smartdish-parent.smartdish.cloud" http://<EXTERNAL-IP>
```

## 📊 Monitoring

```bash
# CPU/Mémoire en temps réel
watch kubectl top pods -n smartdish

# Statistiques des nodes
kubectl top nodes

# Nombre de pods par node
kubectl get pods --all-namespaces -o wide | awk '{print $8}' | sort | uniq -c

# Pods qui utilisent le plus de ressources
kubectl top pods --all-namespaces --sort-by=memory
kubectl top pods --all-namespaces --sort-by=cpu
```

## 🔐 Sécurité

```bash
# Scan de sécurité avec Trivy
docker run --rm -v /var/run/docker.sock:/var/run/docker.sock \
  aquasec/trivy image fra.ocir.io/<tenancy-namespace>/smartdish/smartdish-parent:v1.0.0

# Vérifier les policies réseau
kubectl get networkpolicies -n smartdish

# Vérifier les RBAC
kubectl get rolebindings -n smartdish
kubectl get clusterrolebindings
```

## 💾 Backup et Restore

```bash
# Exporter toutes les ressources d'un namespace
kubectl get all -n smartdish -o yaml > smartdish-backup.yaml

# Exporter les secrets
kubectl get secrets -n smartdish -o yaml > smartdish-secrets-backup.yaml

# Exporter les ConfigMaps
kubectl get configmaps -n smartdish -o yaml > smartdish-configmaps-backup.yaml

# Restore
kubectl apply -f smartdish-backup.yaml
```

## 🔄 Automatisation courante

```bash
# Redéployer tous les microservices
for deployment in $(kubectl get deployments -n smartdish -o jsonpath='{.items[*].metadata.name}'); do
  kubectl rollout restart deployment/$deployment -n smartdish
done

# Supprimer tous les pods en erreur
kubectl delete pods --field-selector status.phase=Failed -n smartdish

# Supprimer tous les pods completed
kubectl delete pods --field-selector status.phase=Succeeded -n smartdish
```

---

**Aide rapide** : Pour plus de détails, consultez :
- `kubectl --help`
- `helm --help`
- `oci --help`
- [Documentation Kubernetes](https://kubernetes.io/docs/)
- [Documentation Helm](https://helm.sh/docs/)
- [Documentation OCI](https://docs.oracle.com/en-us/iaas/Content/home.htm)

