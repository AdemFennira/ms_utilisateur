# 🚀 PRÊT POUR LE TEST DE DÉPLOIEMENT

## ✅ État actuel

- ✅ **Infrastructure OKE** : Active
- ✅ **MySQL** : Running (pod `mysql-6dcfbf6bb5-z55wv`)
- ✅ **NGINX Ingress** : LoadBalancer `141.145.216.180`
- ✅ **Secrets GitHub** : Configurés

---

## ⚠️ 2 ACTIONS REQUISES AVANT LE TEST

### 1️⃣ Corriger la variable GitHub (30 secondes)

**URL** : https://github.com/AbdBoutchichi/RecipeYouLove/settings/variables/actions

**Action** :
1. Cliquez sur `MICROSERVICE_NAME`
2. Changez : `ms-template` → `smartdish-parent`
3. Cliquez **Update variable**

### 2️⃣ Vérifier les secrets MySQL (1 minute)

**URL** : https://github.com/AbdBoutchichi/RecipeYouLove/settings/secrets/actions

**Vérifiez que ces secrets ont les bonnes valeurs** :

| Secret | Valeur correcte |
|--------|----------------|
| `MYSQL_HOST` | `mysql.smartdish.svc.cluster.local` |
| `MYSQL_PORT` | `3306` |
| `MYSQL_USER` | `smartdish_user` |
| `MYSQL_PASSWORD` | `smartdish_pass_2024` |
| `MYSQL_ROOT_PASSWORD` | `smartdish_root_2024` |
| `MYSQL_DATABASE` | `smartdishdb` |

**Si les valeurs sont différentes, corrigez-les !**

---

## 🚀 LANCER LE TEST (Après les corrections)

### Option 1 : Script automatique ⭐ (Recommandé)

```powershell
cd C:\Users\lenovo\git\RecipeYouLove
.\test-deployment.ps1
```

Le script va :
- ✅ Vérifier votre environnement
- ✅ Créer la branche `feat/test-deployment-oke`
- ✅ Créer un commit de test
- ✅ Vous demander confirmation avant le push
- ✅ Ouvrir GitHub Actions dans le navigateur

### Option 2 : Commandes manuelles

```powershell
cd C:\Users\lenovo\git\RecipeYouLove
git checkout -b feat/test-deployment-oke
echo "# Test déploiement OKE - $(Get-Date)" >> TEST_DEPLOYMENT.md
git add TEST_DEPLOYMENT.md
git commit -m "test: Premier déploiement OKE avec MySQL"
git push origin feat/test-deployment-oke
```

---

## 📊 SUIVRE LE DÉPLOIEMENT

### GitHub Actions

**URL** : https://github.com/AbdBoutchichi/RecipeYouLove/actions

Vous verrez la pipeline s'exécuter avec ces étapes :

1. ✅ **Configuration** - Détection environnement
2. ✅ **Build Maven & Tests** - Compilation Java 21
3. ✅ **Check Coverage** - Vérification ≥ 60%
4. ✅ **Build Docker** - Création image + Push OCIR
5. ✅ **Security Scan** - Scan Trivy
6. ✅ **Deploy Kubernetes** - Déploiement sur OKE

### Cloud Shell (Vérification en temps réel)

```bash
# Voir les pods
kubectl get pods -n smartdish -w

# Voir les logs de l'application
kubectl logs -f deployment/smartdish-parent -n smartdish

# Voir l'ingress
kubectl get ingress -n smartdish

# Tester l'URL
curl http://soa-smartdish-parent.141.145.216.180.nip.io/actuator/health
```

---

## 🎯 RÉSULTAT ATTENDU

### Déploiement réussi

```
Namespace: smartdish
├── MySQL
│   ├── Pod: mysql-6dcfbf6bb5-z55wv (Running) ✅
│   └── Service: mysql (ClusterIP 10.96.38.73:3306)
│
└── Application Spring Boot
    ├── Pod: smartdish-parent-xxxxx (Running) ✅
    ├── Service: smartdish-parent (ClusterIP)
    └── Ingress: http://soa-smartdish-parent.141.145.216.180.nip.io
```

### URL accessible

```
http://soa-smartdish-parent.141.145.216.180.nip.io
http://soa-smartdish-parent.141.145.216.180.nip.io/actuator/health
http://soa-smartdish-parent.141.145.216.180.nip.io/actuator/info
```

---

## ⚠️ PROBLÈMES POTENTIELS

### ❌ Coverage < 60%

**Erreur** : `Check Coverage` échoue

**Solution** : Le déploiement ne se fera pas. C'est normal si la couverture est insuffisante.

### ❌ Image pull error

**Erreur** : Impossible de télécharger l'image depuis OCIR

**Solution** :
- Vérifiez `OCI_USERNAME` : `axtiowvuxa7/<email>`
- Vérifiez `OCI_AUTH_TOKEN` : Doit être valide

### ❌ Application ne démarre pas

**Erreur** : Pod en `CrashLoopBackOff`

**Solution** : Vérifiez les logs
```bash
kubectl logs deployment/smartdish-parent -n smartdish
```

Causes fréquentes :
- Connexion MySQL échouée → Vérifier `MYSQL_HOST`
- Port déjà utilisé → Vérifier `SERVER_PORT`

---

## 📝 CHECKLIST

- [ ] Variable `MICROSERVICE_NAME` corrigée (`smartdish-parent`)
- [ ] Secrets MySQL vérifiés
- [ ] Branche `feat/test-deployment-oke` créée
- [ ] Commit fait
- [ ] Push vers GitHub
- [ ] Pipeline en cours sur GitHub Actions
- [ ] Pod `smartdish-parent` Running
- [ ] URL accessible

---

## 🔗 LIENS RAPIDES

| Service | URL |
|---------|-----|
| **Variables GitHub** | https://github.com/AbdBoutchichi/RecipeYouLove/settings/variables/actions |
| **Secrets GitHub** | https://github.com/AbdBoutchichi/RecipeYouLove/settings/secrets/actions |
| **GitHub Actions** | https://github.com/AbdBoutchichi/RecipeYouLove/actions |
| **Console OCI** | https://cloud.oracle.com/ |

---

## 🎉 C'EST TOUT !

**Corrigez les 2 configurations GitHub, puis lancez le script `test-deployment.ps1` !** 🚀

