# 🔧 Troubleshooting - Erreur OCIR "Tenant not authorized"

## Symptôme

Lors du push d'une image Docker vers OCIR, vous recevez l'erreur :

```
ERROR: failed to push fra.ocir.io/***/smartdish/univ.soa:feat-test-deployment-oke-3ef097a: 
failed to authorize: failed to fetch oauth token: 
denied: Tenant with namespace *** not authorized or not found
```

---

## 🎯 Solution rapide

### 1. Vérifiez vos 3 secrets GitHub

Dans **Settings → Secrets and variables → Actions → Secrets**, vérifiez :

| Secret | Format attendu | Exemple |
|--------|----------------|---------|
| `OCI_TENANCY_NAMESPACE` | Namespace seul | `axgbvr6e8mzp` |
| `OCI_USERNAME` | `<tenancy>/<user>` | `axgbvr6e8mzp/oracleidentitycloudservice/john@example.com` |
| `OCI_AUTH_TOKEN` | Token OCI | `[votre-token-oci]` |

### 2. Comment obtenir ces valeurs

#### OCI_TENANCY_NAMESPACE

```bash
# Avec OCI CLI
oci os ns get
```

OU Console OCI → Administration → Tenancy Details → Object Storage Namespace

#### OCI_USERNAME

**Format requis :** `<tenancy-namespace>/<username>`

Exemples valides :
```
axgbvr6e8mzp/oracleidentitycloudservice/john.doe@example.com
axgbvr6e8mzp/john.doe
```

❌ Formats INCORRECTS :
```
john.doe@example.com
oracleidentitycloudservice/john.doe@example.com
```

#### OCI_AUTH_TOKEN

1. Console OCI → User Settings (icône profil)
2. Auth Tokens → Generate Token
3. Nom : `github-actions-ocir`
4. **Copiez immédiatement** le token (visible une seule fois)

### 3. Testez localement

Exécutez le script de test :

```powershell
# Windows
.\test-ocir-auth.ps1
```

```bash
# Linux/Mac
./check-ocir-config.sh
```

### 4. Poussez un commit

```bash
git add .
git commit -m "fix: configure OCIR authentication"
git push
```

---

## 📚 Documentation complète

Pour plus de détails :

| Fichier | Description |
|---------|-------------|
| **[FIX_OCIR_AUTH.md](./FIX_OCIR_AUTH.md)** | Guide complet de résolution |
| **[CHECKLIST_OCIR_SECRETS.md](./CHECKLIST_OCIR_SECRETS.md)** | Checklist détaillée des secrets |
| **[test-ocir-auth.ps1](./test-ocir-auth.ps1)** | Script de test Windows |
| **[check-ocir-config.sh](./check-ocir-config.sh)** | Script de test Linux/Mac |

---

## ✅ Comment savoir que c'est résolu

### Dans les logs GitHub Actions

**Avant (❌) :**
```
ERROR: failed to authorize: failed to fetch oauth token
```

**Après (✅) :**
```
✅ OCI_USERNAME est défini
✅ OCI_AUTH_TOKEN est défini  
✅ OCI_TENANCY_NAMESPACE est défini: axgbvr6e8mzp
✅ Connexion réussie à OCIR
#12 pushing layers
#12 done
```

---

## 🆘 Toujours bloqué ?

1. **Vérifiez les permissions IAM** dans OCI
2. **Créez le repository** manuellement dans OCIR
3. **Vérifiez la région** : `fra.ocir.io` = Frankfurt, `par.ocir.io` = Paris
4. **Consultez** [FIX_OCIR_AUTH.md](./FIX_OCIR_AUTH.md) pour plus de détails

---

## 🔗 Liens utiles

- [Documentation OCIR](https://docs.oracle.com/en-us/iaas/Content/Registry/home.htm)
- [Auth Tokens OCI](https://docs.oracle.com/en-us/iaas/Content/Identity/Tasks/managingcredentials.htm)
- [IAM Policies OCIR](https://docs.oracle.com/en-us/iaas/Content/Registry/Concepts/registrypolicyrepoaccess.htm)

