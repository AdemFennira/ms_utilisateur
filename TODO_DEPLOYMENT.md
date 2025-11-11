# Actions à réaliser pour finaliser le déploiement

## ✅ Déjà fait

- [x] Migration d'Azure vers Oracle Cloud Infrastructure (OCI)
- [x] Suppression des fichiers et scripts Azure
- [x] Création de la structure Helm Chart
- [x] Configuration des workflows CI/CD pour OCI
- [x] Mise à jour du Dockerfile
- [x] Configuration des manifests Kubernetes pour OCI
- [x] Documentation complète

## 🔧 À faire maintenant

### 1. Configuration OCI CLI (sur votre machine locale)

```bash
# Installer OCI CLI
# Windows PowerShell (en tant qu'administrateur)
Invoke-WebRequest -Uri https://raw.githubusercontent.com/oracle/oci-cli/master/scripts/install/install.ps1 -OutFile install.ps1
.\install.ps1

# Configurer OCI CLI
oci setup config

# Vous devrez fournir :
# - User OCID
# - Tenancy OCID
# - Region (eu-paris-1)
# - Générer une paire de clés API
```

### 2. Obtenir le kubeconfig pour votre cluster OKE

```bash
# Exécuter cette commande avec votre Cluster ID
oci ce cluster create-kubeconfig \
  --cluster-id ocid1.cluster.oc1.eu-paris-1.aaaaaaaaafkhi5vnahyc14ozq2ulnwstf3t6hslnvmomgeq5ulqsvca3gwzgw3tq \
  --file %USERPROFILE%\.kube\config \
  --region eu-paris-1 \
  --token-version 2.0.0

# Tester la connexion
kubectl cluster-info
kubectl get nodes
```

### 3. Créer un Auth Token pour OCIR

1. Accédez à la console OCI
2. Allez dans **User Settings** (icône utilisateur en haut à droite)
3. Cliquez sur **Auth Tokens** dans le menu de gauche
4. Cliquez sur **Generate Token**
5. Donnez un nom (ex: "github-actions")
6. **Copiez le token immédiatement** (il ne sera plus visible après)

### 4. Obtenir votre Tenancy Namespace

```bash
oci os ns get
```

Exemple de résultat : `axgbvr6e8mzp`

### 5. Créer une base de données MySQL

#### Option A : MySQL Database Service sur OCI (recommandé)

1. Console OCI > **Databases** > **MySQL** > **DB Systems**
2. Cliquez sur **Create DB System**
3. Configuration :
   - **Name** : smartdish-mysql
   - **Shape** : MySQL.VM.Standard.E3.1.8GB ou VM.Standard.E2.1 (Free Tier si disponible)
   - **Storage** : 50GB
   - **Username** : admin
   - **Password** : [Créez un mot de passe sécurisé]
   - **VCN** : Sélectionnez le même VCN que votre cluster OKE (`oke-vcn-quick-K3s-cluster-42186fdb7`)
   - **Subnet** : Private subnet
4. Cliquez sur **Create**
5. Attendez que le status devienne **Active**
6. Notez l'**endpoint** (ex: `smartdish-mysql.mysql.eu-paris-1.oraclecloud.com`)

#### Option B : MySQL dans Kubernetes (dev/test uniquement)

```bash
# À faire après l'étape 8
kubectl apply -f - <<EOF
apiVersion: v1
kind: Service
metadata:
  name: mysql
  namespace: smartdish
spec:
  ports:
  - port: 3306
  selector:
    app: mysql
---
apiVersion: apps/v1
kind: Deployment
metadata:
  name: mysql
  namespace: smartdish
spec:
  selector:
    matchLabels:
      app: mysql
  template:
    metadata:
      labels:
        app: mysql
    spec:
      containers:
      - name: mysql
        image: mysql:8.0
        env:
        - name: MYSQL_ROOT_PASSWORD
          value: "rootpassword"
        - name: MYSQL_DATABASE
          value: "smartdish"
        - name: MYSQL_USER
          value: "admin"
        - name: MYSQL_PASSWORD
          value: "password"
        ports:
        - containerPort: 3306
        volumeMounts:
        - name: mysql-storage
          mountPath: /var/lib/mysql
      volumes:
      - name: mysql-storage
        emptyDir: {}
EOF
```

### 6. Encoder le kubeconfig en base64

```bash
# Windows PowerShell
[Convert]::ToBase64String([System.IO.File]::ReadAllBytes("$env:USERPROFILE\.kube\config"))
```

Copiez le résultat (longue chaîne de caractères).

### 7. Configurer les Secrets GitHub

Allez sur GitHub : `https://github.com/<votre-username>/RecipeYouLove/settings/secrets/actions`

#### Créer ces Secrets :

| Nom | Valeur | Exemple |
|-----|--------|---------|
| `OCI_USERNAME` | `<tenancy-namespace>/<votre-email>` | `axgbvr6e8mzp/oracleidentitycloudservice/user@example.com` |
| `OCI_AUTH_TOKEN` | Le token généré à l'étape 3 | `VhJ8...x3K=` |
| `OCI_TENANCY_NAMESPACE` | Le namespace obtenu à l'étape 4 | `axgbvr6e8mzp` |
| `OCI_KUBECONFIG` | Le base64 du kubeconfig (étape 6) | `YXBpVmVyc2lvbjogdjEKY2x1c3Rlcn...` |
| `MYSQL_HOST` | L'endpoint MySQL | `smartdish-mysql.mysql.eu-paris-1.oraclecloud.com` ou `mysql.smartdish.svc.cluster.local` |
| `MYSQL_USER` | Utilisateur MySQL | `admin` |
| `MYSQL_PASSWORD` | Mot de passe MySQL | `VotreMotDePasse` |
| `MYSQL_ROOT_PASSWORD` | Mot de passe root MySQL | `VotreMotDePasseRoot` |
| `SONAR_TOKEN` | Token SonarQube (si vous utilisez SonarQube) | `squ_...` |
| `SONAR_HOST_URL` | URL SonarQube | `https://sonarcloud.io` |

#### Créer ces Variables :

| Nom | Valeur |
|-----|--------|
| `MICROSERVICE_NAME` | `smartdish-parent` |
| `COVERAGE_THRESHOLD` | `60` |

### 8. Installer les composants Kubernetes

```bash
# Exécuter le script automatique
chmod +x oci-scripts/setup-oke.sh
./oci-scripts/setup-oke.sh
```

OU manuellement :

```bash
# 1. Créer les namespaces
kubectl apply -f k8s/oci/namespace.yaml

# 2. Installer NGINX Ingress Controller
kubectl apply -f https://raw.githubusercontent.com/kubernetes/ingress-nginx/controller-v1.9.4/deploy/static/provider/cloud/deploy.yaml

# 3. Attendre que NGINX soit prêt
kubectl wait --namespace ingress-nginx \
  --for=condition=ready pod \
  --selector=app.kubernetes.io/component=controller \
  --timeout=120s

# 4. Installer Cert-Manager
kubectl apply -f https://github.com/cert-manager/cert-manager/releases/download/v1.13.0/cert-manager.yaml

# 5. Attendre que Cert-Manager soit prêt
kubectl wait --namespace cert-manager \
  --for=condition=ready pod \
  --selector=app.kubernetes.io/instance=cert-manager \
  --timeout=120s

# 6. Créer les ConfigMaps
kubectl apply -f k8s/oci/configmap.yaml

# 7. Créer les secrets MySQL
# Remplacez les valeurs entre <>
kubectl create secret generic mysql-secrets \
  --from-literal=MYSQL_HOST=<mysql-host> \
  --from-literal=MYSQL_PORT=3306 \
  --from-literal=MYSQL_DATABASE=smartdish \
  --from-literal=MYSQL_USER=<mysql-user> \
  --from-literal=MYSQL_PASSWORD=<mysql-password> \
  --from-literal=MYSQL_ROOT_PASSWORD=<mysql-root-password> \
  --namespace=smartdish

kubectl create secret generic mysql-secrets \
  --from-literal=MYSQL_HOST=<mysql-host> \
  --from-literal=MYSQL_PORT=3306 \
  --from-literal=MYSQL_DATABASE=smartdish \
  --from-literal=MYSQL_USER=<mysql-user> \
  --from-literal=MYSQL_PASSWORD=<mysql-password> \
  --from-literal=MYSQL_ROOT_PASSWORD=<mysql-root-password> \
  --namespace=smartdish-prod

# 8. Obtenir l'IP du Load Balancer
kubectl get svc -n ingress-nginx ingress-nginx-controller
```

### 9. Configuration DNS

#### Option A : Avec votre propre domaine

1. Notez l'**EXTERNAL-IP** du Load Balancer (étape 8)
2. Dans votre fournisseur DNS, créez ces enregistrements :

| Type | Nom | Valeur |
|------|-----|--------|
| A | `*.smartdish-integration.cloud` | `<EXTERNAL-IP>` |
| A | `*.smartdish.cloud` | `<EXTERNAL-IP>` |

#### Option B : Avec nip.io (pour les tests)

Utilisez directement l'IP dans vos URLs :
- `https://soa-api.<EXTERNAL-IP>.nip.io`

Pas besoin de configuration DNS supplémentaire.

### 10. Tester le déploiement

```bash
# 1. Créer une branche de test
git checkout -b feat/test-deployment

# 2. Faire un changement (ex: modifier le README)
echo "Test deployment" >> README.md

# 3. Commit et push
git add .
git commit -m "test: Test OCI deployment"
git push origin feat/test-deployment

# 4. Suivre l'exécution dans GitHub Actions
# Allez sur : https://github.com/<votre-username>/RecipeYouLove/actions

# 5. Une fois déployé, vérifier les pods
kubectl get pods -n smartdish

# 6. Vérifier les logs
kubectl logs -f deployment/smartdish-parent -n smartdish

# 7. Tester l'URL
# Si vous avez configuré le DNS :
curl https://soa-smartdish-parent-integration.smartdish-integration.cloud

# Avec nip.io :
curl https://soa-smartdish-parent.<EXTERNAL-IP>.nip.io
```

## 📋 Checklist finale

- [ ] OCI CLI installé et configuré
- [ ] Kubeconfig OKE obtenu et testé
- [ ] Auth Token OCIR créé
- [ ] Tenancy Namespace obtenu
- [ ] Base de données MySQL créée et accessible
- [ ] Tous les secrets GitHub configurés
- [ ] Toutes les variables GitHub configurées
- [ ] NGINX Ingress Controller installé
- [ ] Cert-Manager installé
- [ ] Namespaces Kubernetes créés
- [ ] ConfigMaps créés
- [ ] Secrets MySQL créés dans les namespaces
- [ ] DNS configuré (ou nip.io prêt)
- [ ] Premier déploiement testé avec succès

## 🎯 Prochaines étapes

1. **Pour les microservices** :
   - Forkez ce repository pour chaque microservice
   - Renommez le fork avec le nom du microservice
   - Modifiez la variable `MICROSERVICE_NAME` dans GitHub
   - Développez votre microservice
   - Le CI/CD fonctionnera automatiquement

2. **Pour la production** :
   - Créez une Pull Request de `feat/**` vers `main`
   - SonarQube analysera le code
   - Mergez vers `main` pour déployer en production

3. **Monitoring** :
   - Installez Prometheus et Grafana (optionnel)
   - Configurez les alertes
   - Ajoutez des dashboards de monitoring

## 📚 Documentation

- [README principal](./README.md)
- [Guide de configuration OCI complet](./OCI_CONFIGURATION.md)
- [Documentation Helm Chart](./helm/smartdish/README.md)

## ❓ Besoin d'aide ?

Si vous rencontrez des problèmes :
1. Vérifiez les logs des pods : `kubectl logs <pod-name> -n smartdish`
2. Vérifiez le status des secrets : `kubectl get secrets -n smartdish`
3. Consultez la documentation OCI
4. Vérifiez les GitHub Actions logs

Bon déploiement ! 🚀

