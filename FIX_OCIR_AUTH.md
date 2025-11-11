# 🔧 Fix OCIR Authentication Error

## Erreur rencontrée

```
ERROR: failed to push fra.ocir.io/***/smartdish/univ.soa:feat-test-deployment-oke-3ef097a: 
failed to authorize: failed to fetch oauth token: 
denied: Tenant with namespace *** not authorized or not found
```

## Causes possibles

1. ❌ **OCI_USERNAME** mal formaté
2. ❌ **OCI_TENANCY_NAMESPACE** incorrect
3. ❌ **OCI_AUTH_TOKEN** invalide ou expiré
4. ❌ Le repository n'existe pas dans OCIR

## Solution étape par étape

### Étape 1 : Vérifier le Tenancy Namespace

```bash
# Avec OCI CLI installé
oci os ns get

# Résultat exemple : axgbvr6e8mzp
```

OU via la console OCI :
1. Allez sur https://cloud.oracle.com
2. Menu hamburger → Administration → Tenancy Details
3. Copiez le **Object Storage Namespace** (c'est votre Tenancy Namespace)

### Étape 2 : Format du Username pour OCIR

Le username DOIT être au format : `<tenancy-namespace>/<username>`

**Exemples corrects :**
```
axgbvr6e8mzp/oracleidentitycloudservice/john.doe@example.com
axgbvr6e8mzp/john.doe
```

**⚠️ Format incorrect :**
```
john.doe@example.com  ❌
oracleidentitycloudservice/john.doe@example.com  ❌
```

### Étape 3 : Créer/Régénérer un Auth Token

1. Console OCI → User Settings (icône profil en haut à droite)
2. Resources → Auth Tokens
3. Cliquer sur "Generate Token"
4. Donner un nom : `github-actions-token`
5. **COPIER IMMÉDIATEMENT** le token (visible une seule fois !)

### Étape 4 : Créer le repository dans OCIR (si nécessaire)

Le repository doit exister dans OCIR avant le premier push.

**Option 1 : Via console OCI**
1. Menu → Developer Services → Container Registry
2. Cliquer "Create Repository"
3. Nom : `smartdish/univ.soa` (correspond à votre microservice)
4. Access : Private
5. Créer

**Option 2 : Le repository sera créé automatiquement au premier push si vous avez les droits**

### Étape 5 : Configurer les secrets GitHub

Allez dans votre repository GitHub :
```
Settings → Secrets and variables → Actions → Secrets
```

Créez/Mettez à jour ces secrets :

#### 1. OCI_TENANCY_NAMESPACE
```
Exemple: axgbvr6e8mzp
```

#### 2. OCI_USERNAME
```
Format: <tenancy-namespace>/<username>
Exemple: axgbvr6e8mzp/oracleidentitycloudservice/john.doe@example.com
```

#### 3. OCI_AUTH_TOKEN
```
Le token généré à l'étape 3 (commence généralement par des caractères alphanumériques)
```

### Étape 6 : Vérifier que votre workflow utilise bien ces secrets

Le fichier `.github/workflows/build-docker-image.yml` doit contenir :

```yaml
secrets:
  OCI_USERNAME:
    required: true
  OCI_AUTH_TOKEN:
    required: true
  OCI_TENANCY_NAMESPACE:
    required: true
```

## Test manuel (optionnel)

Vous pouvez tester l'authentification localement :

```bash
# Remplacez les valeurs par vos vraies valeurs
TENANCY_NAMESPACE="axgbvr6e8mzp"
USERNAME="oracleidentitycloudservice/john.doe@example.com"
AUTH_TOKEN="votre-auth-token"

# Format complet du username
FULL_USERNAME="${TENANCY_NAMESPACE}/${USERNAME}"

# Login
echo "$AUTH_TOKEN" | docker login fra.ocir.io -u "$FULL_USERNAME" --password-stdin

# Si succès, vous verrez : "Login Succeeded"
```

## Vérification des droits IAM

Assurez-vous que votre utilisateur OCI a les permissions suivantes :

```
Allow group <votre-groupe> to manage repos in tenancy
Allow group <votre-groupe> to read repos in tenancy
```

Via OCI Console :
1. Menu → Identity & Security → Policies
2. Vérifiez la policy de votre compartiment
3. Ajoutez si nécessaire :
```
Allow group Developers to manage repos in compartment <compartment-name>
```

## Checklist finale

- [ ] Tenancy Namespace récupéré et ajouté dans les secrets GitHub
- [ ] OCI_USERNAME au format `<tenancy-namespace>/<username>` 
- [ ] Auth Token régénéré et ajouté dans les secrets
- [ ] Repository créé dans OCIR (ou droits pour auto-création)
- [ ] Permissions IAM vérifiées
- [ ] Workflow GitHub mis à jour
- [ ] Test de push (nouveau commit)

## Commandes utiles

### Lister vos images dans OCIR
```bash
# Liste les repositories
oci artifacts container repository list --compartment-id <compartment-ocid>

# Détails d'un repository
oci artifacts container image list \
  --compartment-id <compartment-ocid> \
  --repository-name smartdish/univ.soa
```

### Tester l'authentification OCIR
```bash
# Test simple
docker login fra.ocir.io -u "<tenancy-namespace>/<username>"
# Entrez le auth token quand demandé
```

## Logs utiles dans GitHub Actions

Dans votre workflow, vous verrez maintenant :
```
✅ OCI_USERNAME est défini
✅ OCI_AUTH_TOKEN est défini
✅ OCI_TENANCY_NAMESPACE est défini: axgbvr6e8mzp
Format attendu pour OCI_USERNAME: <tenancy-namespace>/<username>
Registry: fra.ocir.io
Username: axgbvr6e8mzp/oracleidentitycloudservice/john.doe@example.com
✅ Username déjà au bon format
✅ Connexion réussie à OCIR
```

## Ressources

- [OCIR Documentation](https://docs.oracle.com/en-us/iaas/Content/Registry/home.htm)
- [Pushing Images to OCIR](https://docs.oracle.com/en-us/iaas/Content/Registry/Tasks/registrypushingimagesusingthedockercli.htm)
- [Managing Auth Tokens](https://docs.oracle.com/en-us/iaas/Content/Identity/Tasks/managingcredentials.htm#Working)

## Support

Si l'erreur persiste après ces étapes :
1. Vérifiez les logs détaillés dans GitHub Actions
2. Testez l'authentification en local avec Docker CLI
3. Vérifiez que votre région OCIR est correcte (`fra.ocir.io` = Frankfurt, devrait être Paris)
4. Contactez le support OCI si problème de permissions

