# 🚀 Guide de Démarrage Rapide SmartDish

Ce guide vous permet de démarrer rapidement avec le projet SmartDish sur Oracle Cloud Infrastructure (OCI).

## ⏱️ Temps estimé

- Configuration initiale : **30-45 minutes**
- Premier déploiement : **15-20 minutes**

## 📋 Prérequis minimaux

- ✅ Compte Oracle Cloud (Free Tier suffit)
- ✅ Cluster OKE créé (déjà fait : `quick-K3s-cluster-42186fdb7`)
- ✅ Compte GitHub
- ✅ Git installé localement
- ✅ kubectl installé

## 🎯 Démarrage en 5 étapes

### Étape 1️⃣ : Configuration OCI CLI (10 min)

```powershell
# Windows PowerShell (en administrateur)
Invoke-WebRequest -Uri https://raw.githubusercontent.com/oracle/oci-cli/master/scripts/install/install.ps1 -OutFile install.ps1
.\install.ps1

# Configurer OCI CLI (suivez les instructions)
oci setup config

# Vérifier
oci iam region list
```

**Ce dont vous avez besoin** :
- User OCID (Profile > User Settings > OCID)
- Tenancy OCID (Profile > Tenancy > OCID)
- Region : `eu-paris-1`

### Étape 2️⃣ : Récupérer le Kubeconfig (5 min)

```powershell
# Votre Cluster OCID
$CLUSTER_ID = "ocid1.cluster.oc1.eu-paris-1.aaaaaaaaafkhi5vnahyc14ozq2ulnwstf3t6hslnvmomgeq5ulqsvca3gwzgw3tq"

# Obtenir le kubeconfig
oci ce cluster create-kubeconfig `
  --cluster-id $CLUSTER_ID `
  --file "$env:USERPROFILE\.kube\config" `
  --region eu-paris-1 `
  --token-version 2.0.0

# Tester
kubectl cluster-info
kubectl get nodes
```

### Étape 3️⃣ : Créer un Auth Token OCIR (2 min)

1. Console OCI > **Profile Icon** (en haut à droite) > **User Settings**
2. Dans le menu de gauche : **Auth Tokens**
3. Cliquer sur **Generate Token**
4. Nom : `github-actions`
5. **Copier le token** immédiatement (vous ne le reverrez plus)

```powershell
# Obtenir le Tenancy Namespace
oci os ns get
# Exemple de résultat : axgbvr6e8mzp
```

### Étape 4️⃣ : Configurer GitHub Secrets (10 min)

Allez sur : `https://github.com/<votre-username>/RecipeYouLove/settings/secrets/actions`

#### A. Encoder le kubeconfig

```powershell
# Windows PowerShell
[Convert]::ToBase64String([System.IO.File]::ReadAllBytes("$env:USERPROFILE\.kube\config")) | Set-Clipboard
# Le résultat est dans le presse-papier, collez-le dans GitHub
```

#### B. Créer ces secrets dans GitHub

| Secret | Où le trouver | Exemple |
|--------|---------------|---------|
| `OCI_USERNAME` | `<tenancy-namespace>/<votre-email>` | `axgbvr6e8mzp/user@example.com` |
| `OCI_AUTH_TOKEN` | Token créé à l'étape 3 | `VhJ8Q~x...` |
| `OCI_TENANCY_NAMESPACE` | `oci os ns get` | `axgbvr6e8mzp` |
| `OCI_KUBECONFIG` | Base64 du kubeconfig | `YXBpVmVyc2lvbjog...` |
| `MYSQL_HOST` | Pour commencer : `mysql.smartdish.svc.cluster.local` | `mysql.smartdish.svc.cluster.local` |
| `MYSQL_USER` | `admin` | `admin` |
| `MYSQL_PASSWORD` | Créez un mot de passe | `SecurePass123!` |
| `MYSQL_ROOT_PASSWORD` | Créez un mot de passe root | `RootPass123!` |

#### C. Créer ces variables

| Variable | Valeur |
|----------|--------|
| `MICROSERVICE_NAME` | `smartdish-parent` |
| `COVERAGE_THRESHOLD` | `60` |

### Étape 5️⃣ : Installer les composants Kubernetes (15 min)

**Option 1 : Script automatique (recommandé)**

```powershell
# Exécuter le script d'installation complet
.\oci-scripts\install-kubernetes-components.ps1
```

**Option 2 : Commandes manuelles**

```powershell
# 1. Créer les namespaces
kubectl apply -f k8s/oci/namespace.yaml

# 2. Installer NGINX Ingress
kubectl apply -f https://raw.githubusercontent.com/kubernetes/ingress-nginx/controller-v1.9.4/deploy/static/provider/cloud/deploy.yaml

# 3. Attendre que NGINX soit prêt (2-3 minutes)
kubectl wait --namespace ingress-nginx --for=condition=ready pod --selector=app.kubernetes.io/component=controller --timeout=300s

# 4. Créer les ConfigMaps
kubectl apply -f k8s/oci/configmap.yaml

# 5. Déployer MySQL
kubectl apply -f k8s/oci/mysql-deployment.yaml

# 6. Créer les secrets MySQL pour smartdish
kubectl create secret generic mysql-secrets `
  --from-literal=MYSQL_HOST=mysql.smartdish.svc.cluster.local `
  --from-literal=MYSQL_PORT=3306 `
  --from-literal=MYSQL_DATABASE=smartdish `
  --from-literal=MYSQL_USER=admin `
  --from-literal=MYSQL_PASSWORD=SecurePass123! `
  --from-literal=MYSQL_ROOT_PASSWORD=RootPass123! `
  --namespace=smartdish `
  --dry-run=client -o yaml | kubectl apply -f -

# 7. Créer les secrets MySQL pour smartdish-prod
kubectl create secret generic mysql-secrets `
  --from-literal=MYSQL_HOST=mysql.smartdish.svc.cluster.local `
  --from-literal=MYSQL_PORT=3306 `
  --from-literal=MYSQL_DATABASE=smartdish `
  --from-literal=MYSQL_USER=admin `
  --from-literal=MYSQL_PASSWORD=SecurePass123! `
  --from-literal=MYSQL_ROOT_PASSWORD=RootPass123! `
  --namespace=smartdish-prod `
  --dry-run=client -o yaml | kubectl apply -f -

# 8. Vérifier
kubectl get all -n smartdish
kubectl get all -n ingress-nginx
```

## 🎉 Premier Déploiement

```bash
# 1. Cloner le repo (si pas encore fait)
git clone https://github.com/<votre-username>/RecipeYouLove.git
cd RecipeYouLove

# 2. Créer une branche de test
git checkout -b feat/test-deployment

# 3. Faire un changement (pour déclencher la CI/CD)
echo "# Test Deployment" >> TEST.md
git add TEST.md
git commit -m "test: Premier déploiement OCI"

# 4. Push vers GitHub
git push origin feat/test-deployment
```

### Suivre le déploiement

1. Allez sur GitHub Actions : `https://github.com/<votre-username>/RecipeYouLove/actions`
2. Cliquez sur le workflow en cours
3. Suivez les étapes :
   - ✅ Build Maven
   - ✅ Check Coverage
   - ✅ Build Docker Image
   - ✅ Security Scan
   - ✅ Deploy Kubernetes

### Vérifier le déploiement

```bash
# Pods
kubectl get pods -n smartdish
# Vous devriez voir : smartdish-parent-xxxxx-xxxxx Running

# Logs
kubectl logs -f deployment/smartdish-parent -n smartdish

# Services
kubectl get svc -n smartdish

# Obtenir l'IP du Load Balancer
kubectl get svc -n ingress-nginx ingress-nginx-controller
```

### Tester l'application

```bash
# Avec l'IP du Load Balancer
LB_IP=$(kubectl get svc -n ingress-nginx ingress-nginx-controller -o jsonpath='{.status.loadBalancer.ingress[0].ip}')

# Tester avec curl
curl http://$LB_IP -H "Host: soa-smartdish-parent.smartdish.cloud"

# Ou utiliser nip.io
curl http://soa-smartdish-parent.$LB_IP.nip.io
```

## ✅ Checklist de vérification

- [ ] OCI CLI installé et configuré
- [ ] Kubeconfig récupéré et testé
- [ ] Auth Token OCIR créé
- [ ] Tenancy Namespace obtenu
- [ ] Tous les secrets GitHub créés
- [ ] Variables GitHub créées
- [ ] Namespaces Kubernetes créés
- [ ] NGINX Ingress installé
- [ ] MySQL déployé dans Kubernetes
- [ ] ConfigMaps créés
- [ ] Secrets MySQL créés
- [ ] Premier push effectué
- [ ] Pipeline GitHub Actions réussie
- [ ] Pods running dans smartdish namespace
- [ ] Application accessible

## 🎯 Prochaines étapes

### 1. Configuration DNS (optionnel)

Si vous avez un domaine :
```bash
# Obtenir l'IP
kubectl get svc -n ingress-nginx ingress-nginx-controller

# Créer des enregistrements DNS A :
# *.smartdish-integration.cloud → <EXTERNAL-IP>
# *.smartdish.cloud → <EXTERNAL-IP>
```

### 2. Installer Cert-Manager pour HTTPS

```bash
kubectl apply -f https://github.com/cert-manager/cert-manager/releases/download/v1.13.0/cert-manager.yaml
```

### 3. Créer des microservices

```bash
# Forker le repository pour chaque microservice
# Changer la variable MICROSERVICE_NAME dans GitHub
# Développer dans src/
```

### 4. Déployer en production

```bash
# Créer une Pull Request de feat/** vers main
# Vérifier SonarQube
# Merger pour déployer en production
```

## 📚 Documentation complète

- **[README.md](./README.md)** - Documentation principale
- **[OCI_CONFIGURATION.md](./OCI_CONFIGURATION.md)** - Configuration détaillée OCI
- **[TODO_DEPLOYMENT.md](./TODO_DEPLOYMENT.md)** - Liste complète des actions
- **[COMMANDS_CHEATSHEET.md](./COMMANDS_CHEATSHEET.md)** - Aide-mémoire des commandes
- **[MIGRATION_SUMMARY.md](./MIGRATION_SUMMARY.md)** - Résumé de la migration

## 🆘 Problèmes courants

### Le pipeline échoue à "Build Docker Image"

```bash
# Vérifier les secrets OCIR
# GitHub > Settings > Secrets > Actions
# OCI_USERNAME, OCI_AUTH_TOKEN, OCI_TENANCY_NAMESPACE doivent être corrects
```

### Les pods ne démarrent pas

```bash
# Vérifier les secrets OCIR dans Kubernetes
kubectl get secret ocir-secret -n smartdish

# Si absent, le créer :
kubectl create secret docker-registry ocir-secret \
  --docker-server=fra.ocir.io \
  --docker-username=<tenancy-namespace>/<username> \
  --docker-password=<auth-token> \
  --namespace=smartdish
```

### Erreur de connexion MySQL

```bash
# Vérifier que MySQL est running
kubectl get pods -n smartdish | grep mysql

# Vérifier les secrets
kubectl get secret mysql-secrets -n smartdish -o yaml
```

### L'application n'est pas accessible

```bash
# Vérifier l'Ingress
kubectl get ingress -n smartdish

# Vérifier NGINX
kubectl get pods -n ingress-nginx

# Vérifier les logs NGINX
kubectl logs -n ingress-nginx deployment/ingress-nginx-controller
```

## 💡 Conseils

1. **Commencez simple** : MySQL dans Kubernetes suffit pour débuter
2. **Testez progressivement** : Une branche feat/** à la fois
3. **Surveillez les logs** : `kubectl logs -f` est votre ami
4. **Utilisez nip.io** : Pas besoin de DNS pour tester
5. **Consultez la doc** : Tous les guides sont dans le projet

## 🎓 Apprentissage

### Comprendre le workflow

```
feat/test → Push
    ↓
GitHub Actions
    ↓
1. Build Maven + Tests
2. Vérification couverture (60%)
3. Build Docker → OCIR
4. Scan sécurité (Trivy)
5. Deploy Helm → OKE
    ↓
Application accessible !
```

### Explorer Kubernetes

```bash
# Voir tout
kubectl get all -n smartdish

# Suivre les logs
kubectl logs -f deployment/smartdish-parent -n smartdish

# Voir les événements
kubectl get events -n smartdish

# Métriques
kubectl top pods -n smartdish
```

---

**Félicitations !** 🎉 Votre environnement SmartDish est prêt !

**Besoin d'aide ?** Consultez la documentation ou vérifiez les logs des pods.

**Prêt pour la suite ?** Créez votre premier microservice en forkant ce repository.

