# ✅ RÉSOLUTION : kubectl ne trouve pas OCI CLI

## 🔴 Problème

```
Unable to connect to the server: getting credentials: exec: executable oci not found
```

**Cause** : Le kubeconfig essaie d'utiliser `oci` CLI pour s'authentifier, mais `oci.exe` n'est pas dans le PATH de PowerShell.

---

## 🎯 3 SOLUTIONS (choisissez celle qui vous convient)

### ✅ Solution 1 : Utiliser OCI Cloud Shell (RECOMMANDÉ - Aucune config locale)

**Le plus simple : utilisez le terminal dans le navigateur**

1. **Ouvrir la console OCI** : https://cloud.oracle.com/
2. **Cliquer sur l'icône Cloud Shell** en haut à droite (icône de terminal)
3. **Configurer kubectl** :

```bash
oci ce cluster create-kubeconfig \
  --cluster-id ocid1.cluster.oc1.eu-paris-1.aaaaaaaaafkhi5vnahyc14ozq2ulnwstf3t6hslnvmomgeq5ulqsvca3gwzgw3tq \
  --file ~/.kube/config \
  --region eu-paris-1 \
  --token-version 2.0.0

# Vérifier
kubectl get nodes
kubectl get pods -n smartdish
```

**Avantages** :
- ✅ Aucune installation locale
- ✅ OCI CLI déjà configuré
- ✅ kubectl déjà installé
- ✅ Fonctionne immédiatement

---

### ✅ Solution 2 : Utiliser Lens (Interface graphique - Vous êtes en train de télécharger)

**Lens détecte automatiquement le problème et s'authentifie différemment**

1. Installez Lens : https://k8slens.dev/
2. Lancez Lens
3. Lens détectera votre cluster OKE automatiquement
4. Cliquez sur le cluster pour vous connecter

**Lens gère l'authentification automatiquement** sans dépendre du PATH OCI CLI.

---

### ✅ Solution 3 : Corriger le PATH OCI CLI localement

#### Étape 1 : Trouver où est installé OCI CLI

```powershell
# Chercher oci.exe
Get-ChildItem -Path "C:\Users\lenovo\AppData\Local\Programs" -Filter "oci.exe" -Recurse -ErrorAction SilentlyContinue

# Ou chercher dans Python
Get-ChildItem -Path "$env:USERPROFILE\AppData\Local\Programs\Python" -Filter "oci.exe" -Recurse -ErrorAction SilentlyContinue
```

#### Étape 2 : Ajouter au PATH

Une fois trouvé (par exemple : `C:\Users\lenovo\AppData\Local\Programs\Python\Python39\Scripts`) :

```powershell
# Ajouter au PATH de la session actuelle
$ociPath = "C:\Users\lenovo\AppData\Local\Programs\Python\Python39\Scripts"
$env:Path = "$ociPath;$env:Path"

# Ajouter au PATH permanent
$currentPath = [Environment]::GetEnvironmentVariable("Path", "User")
[Environment]::SetEnvironmentVariable("Path", "$ociPath;$currentPath", "User")
```

#### Étape 3 : Relancer PowerShell

Fermez et rouvrez PowerShell pour recharger le PATH.

#### Étape 4 : Vérifier

```powershell
oci --version
kubectl get nodes
```

---

### ✅ Solution 4 : Script automatique

```powershell
# Exécuter le script de correction
.\oci-scripts\fix-oci-path.ps1

# Puis relancer PowerShell
```

---

## 🌐 Pendant que Lens télécharge : Utilisez la Console OCI Web

### Voir vos déploiements dans le navigateur

1. **Console OCI** : https://cloud.oracle.com/
2. **Menu** (☰) > **Developer Services** > **Kubernetes Clusters (OKE)**
3. Cliquez sur **quick-K3s-cluster-42186fdb7**
4. Onglet **Workload** :
   - Pods déployés
   - Deployments
   - Services

### Ou utilisez Cloud Shell

1. **Icône Cloud Shell** en haut à droite de la console OCI
2. Terminal s'ouvre dans le navigateur
3. Tapez :

```bash
# Configurer
oci ce cluster create-kubeconfig \
  --cluster-id ocid1.cluster.oc1.eu-paris-1.aaaaaaaaafkhi5vnahyc14ozq2ulnwstf3t6hslnvmomgeq5ulqsvca3gwzgw3tq \
  --file ~/.kube/config \
  --region eu-paris-1

# Voir tout
kubectl get all -n smartdish
kubectl get all -n ingress-nginx

# Logs
kubectl logs -f deployment/mysql -n smartdish
```

---

## 🎯 Recommandation MAINTENANT

Pendant que **Lens télécharge** :

1. ✅ **Ouvrez la Console OCI** : https://cloud.oracle.com/
2. ✅ **Allez dans Kubernetes Clusters** : Menu > Developer Services > Kubernetes Clusters (OKE)
3. ✅ **Cliquez sur votre cluster** : quick-K3s-cluster-42186fdb7
4. ✅ **Onglet Workload** : Vous verrez tout ce qui est déployé

**OU**

1. ✅ **Cliquez sur l'icône Cloud Shell** (en haut à droite)
2. ✅ **Configurez kubectl** (commandes ci-dessus)
3. ✅ **Utilisez kubectl** directement dans le navigateur

---

## 📊 Ce qui est déployé actuellement sur OKE

```
Namespace: smartdish
├── MySQL
│   ├── Deployment: mysql
│   ├── Pod: mysql-6ddb8cf77-l96bf
│   ├── Service: mysql (ClusterIP 10.96.38.73)
│   └── Secrets: mysql-secrets
│
└── ConfigMap: app-config

Namespace: smartdish-prod
└── Secrets: mysql-secrets

Namespace: ingress-nginx
├── Deployment: ingress-nginx-controller
├── Pod: ingress-nginx-controller-xxxxx
└── Service: ingress-nginx-controller
    └── LoadBalancer: 141.145.216.180

Namespace: kubernetes-dashboard (si installé)
└── Dashboard UI
```

---

## ✅ Une fois Lens installé

**Lens résoudra automatiquement le problème de PATH** et vous verrez :

- 📊 Vue d'ensemble du cluster
- 🔵 Tous les pods en temps réel
- 📝 Logs des pods
- 💻 Terminal dans les pods
- 📈 Graphiques CPU/RAM
- ⚙️ Édition des déploiements

**Lens = La meilleure solution pour voir et gérer vos déploiements !**

---

## 🆘 Si vous voulez réparer kubectl localement maintenant

```powershell
# 1. Fermer ce PowerShell
# 2. Ouvrir un NOUVEAU PowerShell en tant qu'administrateur
# 3. Exécuter :
.\oci-scripts\fix-oci-path.ps1

# 4. Fermer et rouvrir PowerShell normalement
# 5. Tester :
kubectl get nodes
```

---

## 📚 Documentation complète

- **[ACCES_DEPLOIEMENTS_OCI.md](./ACCES_DEPLOIEMENTS_OCI.md)** - Guide complet pour accéder à vos déploiements via console OCI
- **[LENS_GUIDE.md](./LENS_GUIDE.md)** - Guide Lens
- **[GUI_KUBERNETES.md](./GUI_KUBERNETES.md)** - Toutes les interfaces graphiques

---

**EN ATTENDANT LENS : Utilisez la Console OCI Web ou Cloud Shell !** 🚀

**Lien rapide** : https://cloud.oracle.com/

