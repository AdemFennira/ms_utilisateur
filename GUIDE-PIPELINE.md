# 🚀 Guide Rapide - Pipeline CI/CD RecipeYouLove

> **Dernière mise à jour** : 29 Novembre 2025  
> **Stratégie** : Déploiement local sur Minikube avec MySQL

---

## 📋 Table des Matières

1. [Vue d'ensemble](#vue-densemble)
2. [Architecture](#architecture)
3. [Lancer la Pipeline](#lancer-la-pipeline)
4. [Accéder aux Composants](#accéder-aux-composants)
5. [Troubleshooting](#troubleshooting)

---

## 🎯 Vue d'ensemble

La pipeline CI/CD complète comprend **8 jobs** :

```
1️⃣ Config Vars         → Génère IMAGE_TAG unique
2️⃣ Build Maven         → Compile + Tests unitaires
3️⃣ Check Coverage      → Vérifie couverture >= 80%
4️⃣ Build Docker        → Crée l'image Docker
5️⃣ Check Security      → Scan vulnérabilités (Trivy)
6️⃣ Deploy Kubernetes   → Déploie sur Minikube
7️⃣ Integration Tests   → Tests API avec Newman
8️⃣ Log URLs            → Affiche les URLs d'accès
```

**Durée totale** : ~12 minutes ⏱️

---

## 🏗️ Architecture

### Stack Technique

| Composant | Version | Port | Accès |
|-----------|---------|------|-------|
| **Spring Boot** | 3.x | 8080 | NodePort 30080 |
| **MySQL** | 8.0 | 3306 | Internal |
| **phpMyAdmin** | 5.2 | 80 | NodePort 30081 |
| **Minikube** | Latest | - | Local |

### Diagramme

```
┌─────────────────────────────────────────┐
│          GitHub Actions                 │
│                                          │
│  Build → Test → Docker → Security       │
│             ↓                            │
│       Deploy to Minikube                │
└─────────────────────────────────────────┘
                ↓
┌─────────────────────────────────────────┐
│          Kubernetes (Minikube)          │
│                                          │
│  ┌──────────┐      ┌──────────┐        │
│  │  MySQL   │◄─────┤ univ-soa │        │
│  │  :3306   │      │  :8080   │        │
│  └──────────┘      └──────────┘        │
│       ▲                                  │
│       │                                  │
│  ┌──────────┐                           │
│  │phpMyAdmin│                           │
│  │  :30081  │                           │
│  └──────────┘                           │
└─────────────────────────────────────────┘
```

---

## 🚀 Lancer la Pipeline

### Option 1 : Push Automatique

```bash
git add .
git commit -m "feat: votre description"
git push origin feat/manual-pipeline
```

La pipeline se déclenche automatiquement sur `push`.

### Option 2 : Déclenchement Manuel

1. Aller sur GitHub → **Actions**
2. Sélectionner **"CI/CD Pipeline - Orchestrator"**
3. Cliquer sur **"Run workflow"**
4. Sélectionner la branche `feat/manual-pipeline`
5. Cliquer sur **"Run workflow"** (bouton vert)

---

## 🌐 Accéder aux Composants

### 1️⃣ Récupérer l'IP Minikube

Dans les logs du **Job 8 (Log URLs)**, vous trouverez :

```
📦 Minikube IP: 192.168.49.2
```

### 2️⃣ URLs d'Accès

| Service | URL | Description |
|---------|-----|-------------|
| **API** | `http://192.168.49.2:30080` | Application principale |
| **Health Check** | `http://192.168.49.2:30080/actuator/health` | État de l'app |
| **DB Test** | `http://192.168.49.2:30080/api/database/test` | Test connexion MySQL |
| **phpMyAdmin** | `http://192.168.49.2:30081` | Interface MySQL |

### 3️⃣ Credentials MySQL

| Paramètre | Valeur |
|-----------|--------|
| **Host** | `mysql` (internal) ou `192.168.49.2:3306` (external) |
| **Database** | `testdb` |
| **Username** | `root` |
| **Password** | `password` |

---

## 🐛 Troubleshooting

### ❌ Problème : MySQL pas prêt

**Symptôme** :
```
java.net.UnknownHostException: mysql: Try again
```

**Solution** :
- Vérifier que l'InitContainer `wait-for-mysql` attend MySQL
- Augmenter `initialDelaySeconds` dans `readinessProbe`

**Commandes de debug** :
```bash
kubectl get pods -n soa-integration
kubectl logs -f -l app=mysql -n soa-integration
kubectl describe pod <mysql-pod-name> -n soa-integration
```

---

### ❌ Problème : Tests Newman échouent

**Symptôme** :
```
npm install fails with ERESOLVE
```

**Solution** :
- Utiliser `npm install --legacy-peer-deps`
- Vérifier que `tests/newman/package.json` existe

**Commandes de debug** :
```bash
cd tests/newman
npm install --legacy-peer-deps
npm test
```

---

### ❌ Problème : Image Docker pas trouvée dans Minikube

**Symptôme** :
```
ErrImageNeverPull or ImagePullBackOff
```

**Solution** :
- Vérifier que `imagePullPolicy: Never` est dans `deployment.yaml`
- Vérifier le chargement avec `minikube image ls`

**Commandes de debug** :
```bash
minikube image ls | grep univ-soa
minikube ssh
docker images | grep univ-soa
```

---

### ❌ Problème : phpMyAdmin n'affiche rien

**Symptôme** :
```
Cannot connect to MySQL server
```

**Solution** :
- Vérifier que MySQL est Ready : `kubectl get pods -n soa-integration`
- Vérifier les variables d'environnement dans `phpmyadmin.yaml`

**Commandes de debug** :
```bash
kubectl logs -f -l app=phpmyadmin -n soa-integration
kubectl port-forward svc/phpmyadmin 8081:80 -n soa-integration
# Puis accéder à http://localhost:8081
```

---

## 📊 Vérification du Déploiement

### Commandes Utiles

```bash
# Statut des pods
kubectl get pods -n soa-integration -w

# Logs de l'application
kubectl logs -f -l app=univ-soa -n soa-integration

# Logs MySQL
kubectl logs -f -l app=mysql -n soa-integration

# Décrire un pod
kubectl describe pod <pod-name> -n soa-integration

# Port-forward local
kubectl port-forward svc/univ-soa 8080:8080 -n soa-integration
kubectl port-forward svc/phpmyadmin 8081:80 -n soa-integration
kubectl port-forward svc/mysql 3306:3306 -n soa-integration

# Tester l'endpoint DB
curl http://192.168.49.2:30080/api/database/test
```

---

## 📁 Fichiers Importants

### Configuration Kubernetes

```
k8s/minikube/
├── deployment.yaml      # Déploiement de l'app
├── service.yaml         # Service NodePort 30080
├── configmap.yaml       # Variables d'environnement
├── mysql.yaml           # MySQL Deployment + Service
└── phpmyadmin.yaml      # phpMyAdmin Deployment + Service
```

### Configuration Pipeline

```
.github/workflows/
├── pipeline-orchestrator.yml      # Orchestrateur principal
├── config-vars.yml               # Job 1
├── build-maven.yml               # Job 2
├── check-coverage.yml            # Job 3
├── build-docker-image.yml        # Job 4
├── check-conformity-image.yml    # Job 5
├── deploy-kubernetes.yml         # Job 6
├── integration-tests.yml         # Job 7
└── log-components.yml            # Job 8
```

---

## 🎓 Bonnes Pratiques

### ✅ À Faire

- Toujours vérifier les logs avant de commit/push
- Utiliser `--legacy-peer-deps` pour Newman
- Attendre que MySQL soit Ready avant de tester l'app
- Consulter le Job 8 pour les URLs d'accès

### ❌ À Éviter

- Ne pas modifier `imagePullPolicy` (doit rester `Never`)
- Ne pas pusher sans tester localement
- Ne pas supprimer l'InitContainer `wait-for-mysql`
- Ne pas utiliser MongoDB (complètement supprimé)

---

## 📚 Documentation Complète

Pour plus de détails, consulter :

- **RESUME-COMPLET.md** : Guide complet de A à Z
- **FIX-MONGODB-TESTS.md** : Correction des tests MongoDB
- **RECAPITULATIF-FINAL.md** : Récapitulatif des modifications

---

## 🎉 Checklist de Validation

Avant de considérer le déploiement réussi :

- [ ] Tous les 8 jobs passent au vert
- [ ] L'API répond sur `http://MINIKUBE_IP:30080`
- [ ] phpMyAdmin affiche l'interface web
- [ ] MySQL contient la base `testdb`
- [ ] Tests d'intégration Newman passent
- [ ] Rapport JaCoCo >= 80% de couverture
- [ ] Rapport Trivy ne montre pas de vulnérabilités critiques

---

**🚀 La pipeline est prête ! Bon déploiement !**

*Pour toute question, consulter les logs GitHub Actions ou les fichiers de documentation.*

