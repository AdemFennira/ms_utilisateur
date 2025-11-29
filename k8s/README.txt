# Kubernetes Configuration

Ce dossier contient tous les fichiers nécessaires pour déployer l'application sur Kubernetes (Minikube) et ArgoCD.

## Structure

```
k8s/
├── minikube/               # Manifests Kubernetes pour Minikube
│   ├── deployment.yaml     # Configuration du déploiement
│   └── service.yaml        # Service NodePort
├── argocd/                 # Configuration ArgoCD
│   └── applications/       # Applications ArgoCD
│       ├── app.yaml        # Application principale
│       └── parent-app.yaml # App of Apps pattern
├── setup-minikube.ps1      # Script d'installation Minikube
├── setup-argocd.ps1        # Script d'installation ArgoCD
├── deploy-local.ps1        # Script de déploiement local
└── run-integration-tests.ps1 # Script de tests Newman
```

## Scripts PowerShell

### setup-minikube.ps1
Configure et démarre Minikube avec les paramètres recommandés.

```powershell
.\setup-minikube.ps1
```

### setup-argocd.ps1
Installe ArgoCD sur le cluster Minikube.

```powershell
.\setup-argocd.ps1
```

### deploy-local.ps1
Déploie l'application sur Minikube localement.

```powershell
.\deploy-local.ps1
.\deploy-local.ps1 -ImageTag "v1.0.0"
```

### run-integration-tests.ps1
Execute les tests d'intégration Newman.

```powershell
.\run-integration-tests.ps1
.\run-integration-tests.ps1 -ServiceUrl "http://localhost:8080"
```

## Déploiement Rapide

```powershell
# 1. Setup Minikube
.\setup-minikube.ps1

# 2. Déployer l'application
.\deploy-local.ps1

# 3. Tester
.\run-integration-tests.ps1

# 4. (Optionnel) Setup ArgoCD
.\setup-argocd.ps1
```

## Manifests Kubernetes

### deployment.yaml
- 2 replicas
- Image: univ-soa:latest (pull policy: Never pour usage local)
- Probes: liveness et readiness sur /actuator/health
- Resources: 512Mi-1Gi RAM, 250m-500m CPU

### service.yaml
- Type: NodePort
- Port: 8080
- Selector: app=univ-soa

## ArgoCD

### app.yaml
Application ArgoCD qui déploie automatiquement depuis le repo Git.

- Source: https://github.com/EmilieHascoet/RecipeYouLove.git
- Path: k8s/minikube
- Namespace: soa-integration
- Sync: Automatique avec prune et selfHeal

### parent-app.yaml
Pattern "App of Apps" pour gérer plusieurs applications.

## Commandes Utiles

### Minikube
```bash
minikube start
minikube stop
minikube dashboard
minikube service univ-soa --url -n soa-integration
minikube ip
```

### Kubernetes
```bash
kubectl get pods -n soa-integration
kubectl get svc -n soa-integration
kubectl logs -f deployment/univ-soa -n soa-integration
kubectl describe deployment univ-soa -n soa-integration
kubectl port-forward svc/univ-soa 8080:8080 -n soa-integration
```

### ArgoCD
```bash
# Port-forward pour accéder à l'UI
kubectl port-forward svc/argocd-server -n argocd 8080:443

# Obtenir le mot de passe admin
kubectl -n argocd get secret argocd-initial-admin-secret -o jsonpath="{.data.password}" | base64 -d

# Lister les applications
kubectl get applications -n argocd

# Synchroniser une application
kubectl patch application univ-soa-app -n argocd --type merge -p '{"operation":{"sync":{}}}'
```

## Dépannage

### Pods ne démarrent pas
```bash
kubectl get pods -n soa-integration
kubectl describe pod <pod-name> -n soa-integration
kubectl logs <pod-name> -n soa-integration
```

### Service inaccessible
```bash
# Vérifier le service
kubectl get svc -n soa-integration

# Obtenir l'URL Minikube
minikube service univ-soa --url -n soa-integration

# Port-forward manuel
kubectl port-forward svc/univ-soa 8080:8080 -n soa-integration
```

### Minikube ne démarre pas
```powershell
# Supprimer et recréer le cluster
minikube delete
minikube start --memory=4096 --cpus=2
```

## Notes

- Les scripts PowerShell sont conçus pour Windows
- Pour Linux/Mac, utilisez les commandes kubectl/minikube directement
- La pipeline CI/CD utilise les mêmes manifests
- ArgoCD synchronise automatiquement depuis Git

