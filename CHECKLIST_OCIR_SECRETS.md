# ✅ Checklist de configuration des secrets GitHub pour OCIR

## Secrets à configurer dans GitHub

Allez dans : **Settings → Secrets and variables → Actions → Secrets**

### 1. OCI_TENANCY_NAMESPACE ⚠️ CRITIQUE

**Comment l'obtenir :**
```bash
oci os ns get
```

OU via la console OCI :
- Menu → Administration → Tenancy Details
- Copiez le "Object Storage Namespace"

**Exemple de valeur :**
```
axgbvr6e8mzp
```

**Format :**
- [ ] Contient uniquement des caractères alphanumériques minuscules
- [ ] Pas d'espaces
- [ ] Pas de slash (/)
- [ ] Longueur typique : 8-12 caractères

---

### 2. OCI_USERNAME ⚠️ CRITIQUE

**Format OBLIGATOIRE :** `<tenancy-namespace>/<username>`

**Exemples corrects :**
```
axgbvr6e8mzp/oracleidentitycloudservice/john.doe@example.com
axgbvr6e8mzp/john.doe
```

**❌ Exemples incorrects :**
```
john.doe@example.com
oracleidentitycloudservice/john.doe@example.com
john.doe
```

**Comment construire la valeur :**

**Option A - Utilisateur fédéré (recommandé) :**
```
<tenancy-namespace>/oracleidentitycloudservice/<votre-email>
```

**Option B - Utilisateur local OCI :**
```
<tenancy-namespace>/<username>
```

**Checklist :**
- [ ] Commence par le tenancy namespace
- [ ] Contient au moins un slash (/)
- [ ] Si utilisateur fédéré, contient `oracleidentitycloudservice/`
- [ ] Se termine par votre email ou username OCI

---

### 3. OCI_AUTH_TOKEN ⚠️ CRITIQUE

**Comment le générer :**

1. Connectez-vous à la console OCI
2. Cliquez sur votre profil (en haut à droite)
3. Sélectionnez "User Settings"
4. Dans le menu de gauche, cliquez sur "Auth Tokens"
5. Cliquez sur "Generate Token"
6. Donnez un nom : `github-actions-ocir`
7. Cliquez sur "Generate Token"
8. **⚠️ COPIEZ LE TOKEN IMMÉDIATEMENT** (visible une seule fois !)

**Caractéristiques :**
- [ ] Longueur : environ 50-100 caractères
- [ ] Contient des lettres majuscules, minuscules et chiffres
- [ ] Généré dans les dernières 24h (si c'est votre premier test)
- [ ] N'a PAS expiré

**⚠️ IMPORTANT :**
- Le token n'est visible qu'UNE SEULE FOIS à la génération
- Si vous l'avez perdu, générez-en un nouveau
- Maximum 2 Auth Tokens actifs par utilisateur

---

### 4. OCI_KUBECONFIG (pour le déploiement K8s)

**Comment l'obtenir :**

```bash
# Générer le kubeconfig pour votre cluster OKE
oci ce cluster create-kubeconfig \
  --cluster-id <votre-cluster-ocid> \
  --file ~/.kube/config \
  --region eu-paris-1 \
  --token-version 2.0.0
```

**Encoder en base64 :**

**Windows (PowerShell) :**
```powershell
[Convert]::ToBase64String([System.IO.File]::ReadAllBytes("$env:USERPROFILE\.kube\config"))
```

**Linux/Mac :**
```bash
cat ~/.kube/config | base64 -w 0
```

**Checklist :**
- [ ] Le fichier kubeconfig fonctionne en local (`kubectl get nodes`)
- [ ] Encodé en base64
- [ ] Pas de retours à la ligne dans la valeur base64
- [ ] Le cluster est accessible depuis Internet (ou configuré pour GitHub Actions)

---

## Variables GitHub (optionnelles mais recommandées)

Allez dans : **Settings → Secrets and variables → Actions → Variables**

### 1. BASE_DOMAIN
```
smartdish.app
```

### 2. COVERAGE_THRESHOLD
```
60
```

### 3. MICROSERVICE_NAME (si différent du pom.xml)
```
univ.soa
```

---

## 🧪 Test de configuration

### Test 1 : Vérifier localement

Utilisez le script PowerShell fourni :
```powershell
.\test-ocir-auth.ps1
```

### Test 2 : Test manuel Docker

```bash
# Remplacez par vos vraies valeurs
TENANCY_NAMESPACE="axgbvr6e8mzp"
USERNAME="oracleidentitycloudservice/john.doe@example.com"
AUTH_TOKEN="votre-token"

# Login OCIR
echo "$AUTH_TOKEN" | docker login fra.ocir.io -u "$TENANCY_NAMESPACE/$USERNAME" --password-stdin
```

**Résultat attendu :**
```
Login Succeeded
```

### Test 3 : Vérifier dans GitHub Actions

Poussez un commit et vérifiez les logs du workflow "Build Docker Image" :

**✅ Succès :**
```
✅ OCI_USERNAME est défini
✅ OCI_AUTH_TOKEN est défini
✅ OCI_TENANCY_NAMESPACE est défini: axgbvr6e8mzp
✅ Username déjà au bon format
✅ Connexion réussie à OCIR
```

**❌ Échec typique :**
```
ERROR: failed to authorize: failed to fetch oauth token: 
denied: Tenant with namespace *** not authorized or not found
```
→ Revérifiez vos secrets !

---

## 📝 Format récapitulatif

Copiez ce template et remplissez-le :

```yaml
# Dans GitHub Secrets

OCI_TENANCY_NAMESPACE: _______________
# Exemple: axgbvr6e8mzp

OCI_USERNAME: _______________/_______________
# Format: <tenancy-namespace>/<username>
# Exemple: axgbvr6e8mzp/oracleidentitycloudservice/john.doe@example.com

OCI_AUTH_TOKEN: _______________
# Token généré dans la console OCI (User Settings → Auth Tokens)

OCI_KUBECONFIG: _______________
# Kubeconfig encodé en base64
```

---

## ❓ Problèmes fréquents

### Erreur : "Tenant not authorized"
- ✅ Vérifiez que `OCI_USERNAME` contient bien le tenancy namespace
- ✅ Format exact : `<tenancy-namespace>/<username>`
- ✅ Régénérez un Auth Token

### Erreur : "Invalid credentials"
- ✅ Auth Token expiré ou incorrect
- ✅ Régénérez un nouveau token

### Erreur : "Repository not found"
- ✅ Créez le repository dans OCIR d'abord
- ✅ Ou assurez-vous d'avoir les droits de création

### Login réussit mais push échoue
- ✅ Vérifiez les permissions IAM
- ✅ Policy requise : `Allow group <groupe> to manage repos in compartment <compartment>`

---

## 🔗 Ressources

- [Documentation OCIR](https://docs.oracle.com/en-us/iaas/Content/Registry/home.htm)
- [Managing Auth Tokens](https://docs.oracle.com/en-us/iaas/Content/Identity/Tasks/managingcredentials.htm)
- [IAM Policies for OCIR](https://docs.oracle.com/en-us/iaas/Content/Registry/Concepts/registrypolicyrepoaccess.htm)

---

## ✅ Checklist finale

Avant de pusher un commit, vérifiez :

- [ ] `OCI_TENANCY_NAMESPACE` est défini et correct
- [ ] `OCI_USERNAME` est au format `<tenancy-namespace>/<username>`
- [ ] `OCI_AUTH_TOKEN` est récent et valide
- [ ] Test local réussi avec `test-ocir-auth.ps1`
- [ ] Repository existe dans OCIR (ou droits de création)
- [ ] Permissions IAM configurées
- [ ] Workflow GitHub mis à jour

**Prêt à pusher ! 🚀**

