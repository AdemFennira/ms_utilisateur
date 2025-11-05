# 🚀 RecipeYouLove - Template Parent Microservices

**Template parent pour tous les microservices RecipeYouLove avec CI/CD complet**

---

## 📋 Table des Matières

1. [Vue d'ensemble](#vue-densemble)
2. [Quick Start](#quick-start)
3. [Structure des environnements](#structure-des-environnements)
4. [Configuration initiale](#configuration-initiale)
5. [Créer un nouveau microservice](#créer-un-nouveau-microservice)
6. [Déploiement](#déploiement)
7. [Troubleshooting](#troubleshooting)

---

## 🎯 Vue d'ensemble

Ce repository est le **template parent** pour tous les microservices RecipeYouLove. Il fournit :

- ✅ Configuration Vault pour la gestion sécurisée des secrets
- ✅ Configuration ArgoCD pour le GitOps
- ✅ Structure complète pour Integration et Production
- ✅ Scripts de déploiement automatiques
- ✅ Workflow GitHub Actions CI/CD
- ✅ Monitoring avec SLF4J et Actuator

### Architecture CI/CD

```
┌─────────────┐     ┌──────────────┐     ┌─────────────┐
│   GitHub    │────▶│ GitHub       │────▶│   Docker    │
│  Repository │     │  Actions     │     │   Registry  │
└─────────────┘     └──────────────┘     └─────────────┘
                           │                      │
                           ▼                      ▼
                    ┌──────────────┐     ┌─────────────┐
                    │   ArgoCD     │────▶│ Kubernetes  │
                    │   (GitOps)   │     │  (Minikube) │
                    └──────────────┘     └─────────────┘
                                                │
                                                ▼
                                         ┌─────────────┐
                                         │    Vault    │
                                         │  (Secrets)  │
                                         └─────────────┘
```

---

## ⚡ Quick Start

### Démarrer l'infrastructure

```powershell
# 1. Démarrer Minikube
minikube start

# 2. Installer Vault
cd k8s\vault
.\install-vault.sh

# 3. Configurer les secrets
cd k8s\environments\integration\vault
.\setup-secrets.ps1

cd k8s\environments\production\vault
.\setup-secrets.ps1

# 4. Installer ArgoCD
cd k8s\argocd
.\install-argocd.sh

# 5. Déployer sur Integration
cd k8s\environments\integration
.\scripts\deploy-all.ps1
```

### Accéder aux interfaces

```powershell
# ArgoCD UI
kubectl port-forward svc/argocd-server -n argocd 8080:443
# http://localhost:8080 (admin / voir logs du pod)

# Vault UI
kubectl port-forward -n vault vault-0 8200:8200
# http://localhost:8200 (token: root en dev)

# Application
kubectl port-forward -n soa-integration svc/recipeyoulove-api-service 8080:8080
# http://localhost:8080/actuator/health
```

---

## 📁 Structure des Environnements

**Principe** : Chaque environnement a **son propre dossier** avec **TOUT ce qui le concerne**.

```
k8s/
└── environments/
    ├── integration/                    # 🧪 TOUT Integration
    │   ├── deployment.yaml             # Manifeste K8s principal
    │   ├── microservices/              # Autres microservices
    │   │   └── <service>/
    │   │       └── deployment.yaml
    │   ├── argocd/                     # Applications ArgoCD
    │   │   └── application.yaml
    │   ├── scripts/                    # Scripts déploiement
    │   │   ├── deploy-all.ps1
    │   │   └── deploy-all.sh
    │   └── vault/                      # Config secrets
    │       └── setup-secrets.ps1
    │
    └── production/                     # 🚀 TOUT Production
        ├── deployment.yaml
        ├── microservices/
        ├── argocd/
        ├── scripts/
        └── vault/
```

### Différences Integration vs Production

| Aspect | Integration | Production |
|--------|-------------|------------|
| **Namespace** | soa-integration | soa-production |
| **Replicas** | 2 | 3 (HA) |
| **RAM** | 512Mi → 1Gi | 1Gi → 2Gi |
| **CPU** | 250m → 500m | 500m → 1000m |
| **Logs** | DEBUG | INFO |
| **JPA DDL** | update | validate |
| **Sync ArgoCD** | Auto | Manuel |
| **Branch Git** | develop | main |

---

## ⚙️ Configuration Initiale

### 1. Infrastructure Kubernetes

```powershell
# Démarrer Minikube
minikube start

# Vérifier
kubectl get nodes
minikube status
```

### 2. Installer et Configurer Vault

```powershell
# Installer Vault
cd k8s\vault
.\install-vault.sh

# Vérifier que Vault est running
kubectl get pods -n vault

# Configurer les secrets Integration
cd ..\environments\integration\vault
.\setup-secrets.ps1

# Configurer les secrets Production
cd ..\..\production\vault
.\setup-secrets.ps1
```

**Secrets créés** :
- `secret/integration/mysql` - Credentials MySQL Integration
- `secret/integration/mongodb` - Credentials MongoDB Integration
- `secret/integration/application` - Config application Integration
- `secret/production/mysql` - Credentials MySQL Production
- `secret/production/mongodb` - Credentials MongoDB Production
- `secret/production/application` - Config application Production

### 3. Installer ArgoCD

```powershell
cd k8s\argocd
.\install-argocd.sh

# Accéder à l'UI
kubectl port-forward svc/argocd-server -n argocd 8080:443

# Récupérer le mot de passe
kubectl -n argocd get secret argocd-initial-admin-secret -o jsonpath="{.data.password}" | base64 -d
```

### 4. Configurer GitHub Container Registry

Pour que vos images Docker soient publiées sur GitHub :

1. Aller dans **Settings** → **Developer settings** → **Personal access tokens**
2. Créer un token avec scope `write:packages`
3. Ajouter le secret `GITHUB_TOKEN` dans votre repo (déjà fait automatiquement par GitHub Actions)

---

## 🆕 Créer un Nouveau Microservice

### Étape 1 : Créer le Repository

```bash
# Cloner ce template
git clone https://github.com/EmilieHascoet/RecipeYouLove.git mon-service
cd mon-service

# Adapter le code pour votre microservice
# Modifier pom.xml, les classes Java, etc.

# Créer un nouveau repo sur GitHub et pousser
git remote set-url origin https://github.com/<org>/mon-service.git
git push -u origin main
```

### Étape 2 : Créer la Structure Integration

```powershell
# Dans le repo parent (RecipeYouLove)
cd k8s\environments\integration\microservices
mkdir mon-service
```

**Créer** `mon-service/deployment.yaml` :

```yaml
---
apiVersion: v1
kind: ServiceAccount
metadata:
  name: mon-service-sa
  namespace: soa-integration
---
apiVersion: apps/v1
kind: Deployment
metadata:
  name: mon-service
  namespace: soa-integration
  labels:
    app: mon-service
spec:
  replicas: 2
  selector:
    matchLabels:
      app: mon-service
  template:
    metadata:
      labels:
        app: mon-service
      annotations:
        vault.hashicorp.com/agent-inject: "true"
        vault.hashicorp.com/role: "integration-role"
        vault.hashicorp.com/agent-inject-secret-config: "secret/data/integration/mon-service"
        vault.hashicorp.com/agent-inject-template-config: |
          {{- with secret "secret/data/integration/mon-service" -}}
          export SERVICE_PORT="{{ .Data.data.port }}"
          export DATABASE_URL="{{ .Data.data.database_url }}"
          {{- end }}
    spec:
      serviceAccountName: mon-service-sa
      containers:
      - name: mon-service
        image: ghcr.io/<org>/mon-service:latest
        ports:
        - containerPort: 8080
        command: ["/bin/sh", "-c"]
        args:
          - |
            source /vault/secrets/config
            java -jar /app.jar
        livenessProbe:
          httpGet:
            path: /actuator/health/liveness
            port: 8080
          initialDelaySeconds: 60
          periodSeconds: 10
        readinessProbe:
          httpGet:
            path: /actuator/health/readiness
            port: 8080
          initialDelaySeconds: 30
          periodSeconds: 5
        resources:
          requests:
            memory: "512Mi"
            cpu: "250m"
          limits:
            memory: "1Gi"
            cpu: "500m"
---
apiVersion: v1
kind: Service
metadata:
  name: mon-service
  namespace: soa-integration
spec:
  type: ClusterIP
  ports:
  - port: 8080
  selector:
    app: mon-service
---
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: mon-service
  namespace: soa-integration
spec:
  ingressClassName: nginx
  rules:
  - host: mon-service-integration.recipeyoulove.app
    http:
      paths:
      - path: /
        pathType: Prefix
        backend:
          service:
            name: mon-service
            port:
              number: 8080
```

### Étape 3 : Créer l'Application ArgoCD

**Créer** `k8s/environments/integration/argocd/mon-service-app.yaml` :

```yaml
apiVersion: argoproj.io/v1alpha1
kind: Application
metadata:
  name: mon-service-integration
  namespace: argocd
spec:
  project: default
  source:
    repoURL: https://github.com/<org>/mon-service.git
    targetRevision: develop
    path: k8s/environments/integration/microservices/mon-service
  destination:
    server: https://kubernetes.default.svc
    namespace: soa-integration
  syncPolicy:
    automated:
      prune: true
      selfHeal: true
    syncOptions:
      - CreateNamespace=true
```

### Étape 4 : Répéter pour Production

Créer les mêmes fichiers dans `k8s/environments/production/` avec :
- `replicas: 3`
- `memory: "1Gi"` requests, `"2Gi"` limits
- `cpu: "500m"` requests, `"1000m"` limits
- `targetRevision: main`
- `selfHeal: false`

### Étape 5 : Configurer les Secrets Vault

```powershell
# Integration
kubectl exec -n vault vault-0 -- vault kv put secret/integration/mon-service \
  port="8080" \
  database_url="jdbc:mysql://mysql-service.databases:3306/mydb"

# Production
kubectl exec -n vault vault-0 -- vault kv put secret/production/mon-service \
  port="8080" \
  database_url="jdbc:mysql://mysql-service.databases:3306/mydb"
```

### Étape 6 : Déployer

```powershell
# Déployer automatiquement tous les services Integration
cd k8s\environments\integration
.\scripts\deploy-all.ps1

# Le script détecte et déploie automatiquement mon-service !
```

---

## 🚀 Déploiement

### Déploiement Automatique (Recommandé)

```powershell
# Integration - Déploie TOUS les microservices
cd k8s\environments\integration
.\scripts\deploy-all.ps1

# Production - Déploie TOUS les microservices (avec confirmation)
cd k8s\environments\production
.\scripts\deploy-all.ps1
```

Le script `deploy-all.ps1` :
- ✅ Vérifie Minikube et Vault
- ✅ Crée le namespace
- ✅ Déploie le service principal (`deployment.yaml`)
- ✅ Détecte et déploie TOUS les services dans `microservices/`
- ✅ Affiche l'état final (pods, services, ingress)

### Déploiement Manuel

```powershell
# Déployer un service spécifique
kubectl apply -f k8s\environments\integration\microservices\mon-service\deployment.yaml

# Vérifier le déploiement
kubectl rollout status deployment/mon-service -n soa-integration

# Voir les pods
kubectl get pods -n soa-integration -l app=mon-service
```

### Déploiement via GitHub Actions

Le workflow `.github/workflows/ci-cd-pipeline.yml` déploie automatiquement :

**Branch `develop`** → Integration
```bash
git checkout develop
git push origin develop
```

**Branch `main`** → Production
```bash
git checkout main
git merge develop
git push origin main
```

### Accéder aux Applications

#### Via Port-Forward
```powershell
# Integration
kubectl port-forward -n soa-integration svc/<service-name> 8080:8080

# Production
kubectl port-forward -n soa-production svc/<service-name> 8081:8080

# Tester
curl http://localhost:8080/actuator/health
curl http://localhost:8080/actuator/metrics
```

#### Via Ingress (avec minikube tunnel)
```powershell
# Terminal 1 : Démarrer le tunnel
minikube tunnel

# Terminal 2 : Éditer hosts (Admin)
# C:\Windows\System32\drivers\etc\hosts
127.0.0.1 mon-service-integration.recipeyoulove.app
127.0.0.1 mon-service.recipeyoulove.app

# Accéder
# http://mon-service-integration.recipeyoulove.app
# http://mon-service.recipeyoulove.app
```

---

## 🔍 Monitoring et Vérification

### État des Pods

```powershell
# Tous les pods Integration
kubectl get pods -n soa-integration

# Tous les pods Production
kubectl get pods -n soa-production

# Un service spécifique
kubectl get pods -n soa-integration -l app=mon-service

# Détails d'un pod
kubectl describe pod <pod-name> -n soa-integration
```

### Logs

```powershell
# Logs en temps réel
kubectl logs -f deployment/<service-name> -n soa-integration

# Logs d'un pod spécifique
kubectl logs <pod-name> -n soa-integration

# Logs du Vault Agent (injection secrets)
kubectl logs <pod-name> -n soa-integration -c vault-agent

# Logs de tous les pods d'un service
kubectl logs -l app=<service-name> -n soa-integration --prefix=true
```

### Health Checks

```powershell
# Health
curl http://localhost:8080/actuator/health

# Liveness
curl http://localhost:8080/actuator/health/liveness

# Readiness
curl http://localhost:8080/actuator/health/readiness

# Metrics
curl http://localhost:8080/actuator/metrics

# Prometheus
curl http://localhost:8080/actuator/prometheus
```

### Monitoring SLF4J

Votre application Spring Boot inclut maintenant SLF4J pour le monitoring :
- ✅ Temps d'exécution des requêtes SQL
- ✅ Temps de réponse des endpoints API
- ✅ Logs détaillés des controllers et DAOs
- ✅ Métriques de performance

---

## 🐛 Troubleshooting

### Pod en CrashLoopBackOff

```powershell
# Voir les logs
kubectl logs <pod-name> -n soa-integration

# Logs précédents (si redémarré)
kubectl logs <pod-name> -n soa-integration --previous

# Décrire le pod
kubectl describe pod <pod-name> -n soa-integration

# Voir les events
kubectl get events -n soa-integration --sort-by='.lastTimestamp'
```

**Causes communes** :
- Image Docker non trouvée → Vérifier GitHub Container Registry
- Secrets Vault non accessibles → Vérifier les annotations et politiques
- Erreur application → Vérifier les logs

### Secrets Vault non chargés

```powershell
# Vérifier les secrets
kubectl exec -n vault vault-0 -- vault kv get secret/integration/<service-name>

# Vérifier que Vault Agent est injecté
kubectl get pod <pod-name> -n soa-integration -o yaml | findstr vault

# Logs du Vault Agent
kubectl logs <pod-name> -n soa-integration -c vault-agent
```

### Application non accessible

```powershell
# Vérifier le Service
kubectl get svc -n soa-integration

# Vérifier l'Ingress
kubectl get ingress -n soa-integration
kubectl describe ingress <ingress-name> -n soa-integration

# Vérifier Ingress NGINX
kubectl get pods -n ingress-nginx

# Test en interne
kubectl run debug --rm -it --image=curlimages/curl -- \
  curl http://<service-name>.soa-integration:8080/actuator/health
```

### Base de données inaccessible

```powershell
# Vérifier MySQL
kubectl get pods -n databases -l app=mysql

# Tester la connexion
kubectl run -it --rm mysql-test --image=mysql:8.0 --restart=Never -n databases -- \
  mysql -h mysql-service.databases.svc.cluster.local -u root -p
```

### Rollback Production

```powershell
# Rollback immédiat
kubectl rollout undo deployment/<service-name> -n soa-production

# Voir l'historique
kubectl rollout history deployment/<service-name> -n soa-production

# Rollback vers une version spécifique
kubectl rollout undo deployment/<service-name> -n soa-production --to-revision=2

# Vérifier
kubectl get pods -n soa-production -l app=<service-name>
```

---

## 📋 Checklist Nouveau Microservice

### Préparation
- [ ] Repository Git créé
- [ ] Code développé et testé localement
- [ ] Image Docker buildée
- [ ] Image poussée sur GitHub Container Registry

### Configuration Integration
- [ ] Dossier `k8s/environments/integration/microservices/<nom>/` créé
- [ ] Fichier `deployment.yaml` créé
- [ ] Application ArgoCD créée dans `integration/argocd/`
- [ ] Secrets Vault Integration configurés
- [ ] Déployé avec `deploy-all.ps1`
- [ ] Tests passent

### Configuration Production
- [ ] Dossier `k8s/environments/production/microservices/<nom>/` créé
- [ ] Fichier `deployment.yaml` créé (3 replicas, plus de resources)
- [ ] Application ArgoCD créée dans `production/argocd/`
- [ ] Secrets Vault Production configurés
- [ ] Testé en Integration ✓
- [ ] Déployé en Production
- [ ] Monitoring actif 15 min
- [ ] Plan de rollback prêt

---

## 🎯 Bonnes Pratiques

### Nommage
- **Microservices** : `kebab-case` (recipe-service, user-service)
- **Branches** : `develop` (Integration), `main` (Production)
- **Images** : `ghcr.io/<org>/<service>:latest` ou avec version tag

### Secrets
- ❌ **JAMAIS** de secrets en clair dans le code
- ✅ **TOUJOURS** utiliser Vault
- ✅ Secrets différents entre Integration et Production
- ✅ Rotation régulière des mots de passe

### Déploiement
- ✅ **TOUJOURS** tester en Integration d'abord
- ✅ Avoir un plan de rollback pour Production
- ✅ Surveiller les logs pendant 15 min après déploiement
- ✅ Vérifier les health checks avant de passer à l'étape suivante

### Resources
- **Integration** : Léger (512Mi-1Gi RAM, 2 replicas)
- **Production** : Robuste (1Gi-2Gi RAM, 3 replicas minimum)
- Ajuster selon la charge observée

---

## 🔧 Commandes Utiles

### Gestion des Déploiements

```powershell
# Redémarrer un déploiement
kubectl rollout restart deployment/<service-name> -n soa-integration

# Voir l'historique
kubectl rollout history deployment/<service-name> -n soa-integration

# Scaler manuellement
kubectl scale deployment/<service-name> --replicas=5 -n soa-integration

# Pause/Resume rollout
kubectl rollout pause deployment/<service-name> -n soa-integration
kubectl rollout resume deployment/<service-name> -n soa-integration
```

### Gestion des Secrets

```powershell
# Lister les secrets Vault
kubectl exec -n vault vault-0 -- vault kv list secret/integration/

# Ajouter un secret
kubectl exec -n vault vault-0 -- vault kv put secret/integration/<service> key="value"

# Mettre à jour un secret
kubectl exec -n vault vault-0 -- vault kv patch secret/integration/<service> key="new-value"

# Supprimer un secret
kubectl exec -n vault vault-0 -- vault kv delete secret/integration/<service>
```

### Debug

```powershell
# Shell dans un pod
kubectl exec -it <pod-name> -n soa-integration -- /bin/sh

# Copier un fichier depuis un pod
kubectl cp soa-integration/<pod-name>:/path/to/file ./local-file

# Port-forward vers un pod spécifique
kubectl port-forward <pod-name> -n soa-integration 8080:8080

# Voir les ressources utilisées
kubectl top pods -n soa-integration
kubectl top nodes
```

### Nettoyage

```powershell
# Supprimer un déploiement
kubectl delete -f k8s\environments\integration\microservices\<service>\deployment.yaml

# Supprimer tous les pods d'un namespace
kubectl delete pods --all -n soa-integration

# Supprimer un namespace complet
kubectl delete namespace soa-integration
```

---

## 📚 Ressources

### Infrastructure
- **Minikube** : https://minikube.sigs.k8s.io/
- **Kubernetes** : https://kubernetes.io/docs/
- **Docker** : https://docs.docker.com/

### Outils
- **ArgoCD** : https://argo-cd.readthedocs.io/
- **Vault** : https://www.vaultproject.io/docs
- **GitHub Actions** : https://docs.github.com/en/actions

### Monitoring
- **Spring Boot Actuator** : https://docs.spring.io/spring-boot/docs/current/reference/html/actuator.html
- **SLF4J** : http://www.slf4j.org/manual.html
- **Prometheus** : https://prometheus.io/docs/

---

## 🎉 Félicitations !

Vous avez maintenant :
- ✅ Une infrastructure Kubernetes complète
- ✅ Un système de gestion des secrets sécurisé (Vault)
- ✅ Un déploiement GitOps automatisé (ArgoCD)
- ✅ Une CI/CD complète (GitHub Actions)
- ✅ Des environnements Integration et Production séparés
- ✅ Un monitoring avec SLF4J et Actuator
- ✅ Des scripts de déploiement automatiques

**Vous êtes prêt à déployer vos microservices ! 🚀**

---

## 📞 Support

Pour toute question ou problème :
1. Consultez la section [Troubleshooting](#troubleshooting)
2. Vérifiez les logs des pods
3. Consultez la documentation officielle des outils
4. Vérifiez les events Kubernetes

**Bon déploiement !** 🎊

