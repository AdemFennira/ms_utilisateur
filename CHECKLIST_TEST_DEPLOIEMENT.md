# ✅ CHECKLIST FINALE - Prêt pour le déploiement

## 📊 Vérification des configurations

### ✅ Infrastructure OKE (Oracle Kubernetes)

- ✅ **Cluster OKE** : Actif (`quick-K3s-cluster-42186fdb7`)
- ✅ **MySQL** : Running (pod `mysql-6dcfbf6bb5-z55wv`)
- ✅ **Service MySQL** : ClusterIP `10.96.38.73:3306`
- ✅ **NGINX Ingress** : Running avec LoadBalancer `141.145.216.180`
- ✅ **Namespaces** : `smartdish` + `smartdish-prod`

### ✅ Secrets GitHub (Vérifiés)

| Secret | Valeur | Status |
|--------|--------|--------|
| `OCI_TENANCY_NAMESPACE` | `axtiowvuxa7` | ✅ |
| `OCI_USERNAME` | `axtiowvuxa7/abdelmoughitbouchid4@gmail...` | ✅ |
| `OCI_AUTH_TOKEN` | Masqué | ✅ |
| `OCI_KUBECONFIG` | Masqué | ✅ |
| `MYSQL_HOST` | (à vérifier) | ⚠️ |
| `MYSQL_PORT` | (à vérifier) | ⚠️ |
| `MYSQL_USER` | (à vérifier) | ⚠️ |
| `MYSQL_PASSWORD` | Masqué | ✅ |
| `MYSQL_ROOT_PASSWORD` | Masqué | ✅ |
| `MYSQL_DATABASE` | (à vérifier) | ⚠️ |

### ⚠️ Variables GitHub à corriger

| Variable | Valeur actuelle | Valeur correcte | Status |
|----------|----------------|----------------|--------|
| `MICROSERVICE_NAME` | `ms-template` | `smartdish-parent` | ❌ **À CORRIGER** |
| `COVERAGE_THRESHOLD` | `60` | `60` | ✅ |

---

## 🔧 Actions à faire AVANT le test

### 1. Corriger la variable `MICROSERVICE_NAME`

```
1. Allez sur : https://github.com/AbdBoutchichi/RecipeYouLove/settings/variables/actions
2. Cliquez sur MICROSERVICE_NAME
3. Changez : ms-template → smartdish-parent
4. Cliquez Update variable
```

### 2. Vérifier/Corriger les secrets MySQL

Les secrets MySQL doivent correspondre à ce qui est déployé sur Kubernetes.

**Valeurs correctes à utiliser** :

```
MYSQL_HOST = mysql.smartdish.svc.cluster.local
MYSQL_PORT = 3306
MYSQL_USER = smartdish_user
MYSQL_PASSWORD = smartdish_pass_2024
MYSQL_ROOT_PASSWORD = smartdish_root_2024
MYSQL_DATABASE = smartdishdb
```

**Actions** :

```
1. Allez sur : https://github.com/AbdBoutchichi/RecipeYouLove/settings/secrets/actions
2. Vérifiez/Corrigez chaque secret MySQL
```

---

## 🚀 Test de déploiement

Une fois les corrections faites, testez le déploiement :

### Étape 1 : Créer une branche de test

```bash
cd C:\Users\lenovo\git\RecipeYouLove
git checkout -b feat/test-deployment-oke
```

### Étape 2 : Faire un changement

```bash
echo "# Test déploiement OKE - $(Get-Date)" >> TEST_DEPLOYMENT.md
git add TEST_DEPLOYMENT.md
git commit -m "test: Premier déploiement sur OKE avec MySQL"
```

### Étape 3 : Push vers GitHub

```bash
git push origin feat/test-deployment-oke
```

### Étape 4 : Suivre le déploiement

1. **GitHub Actions** : https://github.com/AbdBoutchichi/RecipeYouLove/actions
2. Cliquez sur le workflow en cours
3. Suivez les étapes :
   - ✅ Configuration
   - ✅ Build Maven & Tests
   - ✅ Check Coverage (60%)
   - ✅ Build Docker → OCIR
   - ✅ Security Scan (Trivy)
   - ✅ Deploy Kubernetes → OKE

---

## 📊 Ce qui va être déployé

```
OKE Cluster (Kubernetes)
└── Namespace: smartdish
    ├── MySQL (déjà déployé) ✅
    │   └── Service: mysql (ClusterIP 10.96.38.73:3306)
    │
    ├── Application Spring Boot (sera déployé)
    │   ├── Deployment: smartdish-parent
    │   ├── Pod(s): smartdish-parent-xxxxx
    │   └── Service: smartdish-parent (ClusterIP)
    │
    └── Ingress
        └── URL: http://soa-smartdish-parent.141.145.216.180.nip.io
```

---

## 🔍 Vérification après déploiement

### Via Cloud Shell

```bash
# 1. Voir les pods déployés
kubectl get pods -n smartdish

# 2. Voir les logs de l'application
kubectl logs -f deployment/smartdish-parent -n smartdish

# 3. Voir les services
kubectl get svc -n smartdish

# 4. Voir l'ingress
kubectl get ingress -n smartdish

# 5. Tester l'application
curl http://soa-smartdish-parent.141.145.216.180.nip.io/actuator/health
```

### Via Lens (si installé)

```
1. Ouvrir Lens
2. Se connecter au cluster OKE
3. Sélectionner namespace: smartdish
4. Voir Workloads > Pods
5. Cliquer sur le pod smartdish-parent
6. Voir les logs en temps réel
```

---

## ⚠️ Problèmes potentiels et solutions

### Problème 1 : Coverage < 60%

**Solution** : Le déploiement ne se fera pas. Ajoutez plus de tests unitaires.

### Problème 2 : Image pull error

**Vérifier** :
- Secret `OCI_USERNAME` correct (`axtiowvuxa7/<email>`)
- Secret `OCI_AUTH_TOKEN` valide

**Solution** :
```bash
# Dans Cloud Shell
kubectl describe pod -n smartdish <pod-name>
```

### Problème 3 : Application ne démarre pas

**Vérifier les logs** :
```bash
kubectl logs -f deployment/smartdish-parent -n smartdish
```

**Causes fréquentes** :
- Connexion MySQL échouée → Vérifier `MYSQL_HOST`
- Port déjà utilisé → Vérifier `SERVER_PORT`

### Problème 4 : URL non accessible

**Vérifier l'ingress** :
```bash
kubectl get ingress -n smartdish
kubectl describe ingress -n smartdish
```

---

## 📝 Workflow de déploiement

```
Push sur feat/** ou fix/**
    ↓
GitHub Actions détecte le push
    ↓
1. Configuration (détection environnement)
    ↓
2. Build Maven + Tests (Java 21)
    ↓
3. Check Coverage (doit être ≥ 60%)
    ↓
4. Build Docker Image
    ├─ Build avec Dockerfile
    ├─ Tag: feat-test-deployment-oke-<commit>
    └─ Push vers OCIR (fra.ocir.io/axtiowvuxa7/smartdish/smartdish-parent)
    ↓
5. Security Scan (Trivy)
    ├─ Scan de l'image Docker
    └─ Upload résultats vers GitHub Security
    ↓
6. Deploy Kubernetes
    ├─ Setup kubectl avec OCI_KUBECONFIG
    ├─ Créer namespace si nécessaire
    ├─ Créer secret OCIR pour pull l'image
    ├─ Appliquer secrets MySQL
    ├─ Appliquer ConfigMaps
    ├─ Déployer via Helm Chart
    └─ Vérifier le déploiement
    ↓
7. Résumé Pipeline
    └─ Affichage du résumé dans GitHub Actions
```

---

## ✅ Résumé des actions

### Actions OBLIGATOIRES avant le test

1. ✅ **Corriger `MICROSERVICE_NAME`** : `ms-template` → `smartdish-parent`
2. ✅ **Vérifier secrets MySQL** : Correspondent aux valeurs Kubernetes

### Actions pour tester

1. ✅ Créer branche `feat/test-deployment-oke`
2. ✅ Faire un commit
3. ✅ Push vers GitHub
4. ✅ Suivre sur GitHub Actions

### Vérifications après déploiement

1. ✅ Pod `smartdish-parent` Running
2. ✅ Logs de l'application OK
3. ✅ URL accessible : `http://soa-smartdish-parent.141.145.216.180.nip.io`

---

## 🎯 Commandes rapides

### Corriger MICROSERVICE_NAME (GitHub Web)
```
https://github.com/AbdBoutchichi/RecipeYouLove/settings/variables/actions
Cliquer sur MICROSERVICE_NAME → Changer en "smartdish-parent"
```

### Tester le déploiement (PowerShell)
```powershell
cd C:\Users\lenovo\git\RecipeYouLove
git checkout -b feat/test-deployment-oke
echo "# Test $(Get-Date)" >> TEST_DEPLOYMENT.md
git add TEST_DEPLOYMENT.md
git commit -m "test: Premier déploiement OKE"
git push origin feat/test-deployment-oke
```

### Suivre le déploiement
```
https://github.com/AbdBoutchichi/RecipeYouLove/actions
```

### Vérifier dans Cloud Shell
```bash
kubectl get all -n smartdish
kubectl logs -f deployment/smartdish-parent -n smartdish
curl http://soa-smartdish-parent.141.145.216.180.nip.io/actuator/health
```

---

**Tout est prêt ! Corrigez la variable `MICROSERVICE_NAME` et lancez le test !** 🚀

