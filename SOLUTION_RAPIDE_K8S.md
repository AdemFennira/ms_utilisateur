# 🚀 SOLUTION RAPIDE - Désactiver les Déploiements Kubernetes

## ⚡ Action Immédiate (2 minutes)

Vous n'avez PAS de cluster Kubernetes et c'est NORMAL ! Voici comment résoudre le problème :

### Étape 1 : Créer la Variable GitHub

1. **Allez sur votre repository GitHub** :
   ```
   https://github.com/EmilieHascoet/RecipeYouLove
   ```

2. **Cliquez sur "Settings"** (en haut à droite)

3. **Dans le menu de gauche, cliquez sur** :
   ```
   Secrets and variables → Actions
   ```

4. **Cliquez sur l'onglet "Variables"** (pas "Secrets")

5. **Cliquez sur "New repository variable"**

6. **Remplissez le formulaire** :
   - **Name** : `ENABLE_K8S_DEPLOY`
   - **Value** : `false`

7. **Cliquez sur "Add variable"**

### Étape 2 : Poussez vos Changements

```bash
cd C:\Users\lenovo\git\RecipeYouLove
git add .
git commit -m "fix: Corriger erreurs Kubernetes et rendre déploiements optionnels"
git push
```

### ✅ Résultat Attendu

Après avoir fait cela, votre pipeline GitHub Actions :
- ✅ **Build and Test** → Réussira
- ✅ **Build Docker Image** → Réussira
- ⏭️ **Deploy to Integration** → Sera **SAUTÉ** (c'est normal !)
- ⏭️ **Deploy to Production** → Sera **SAUTÉ** (c'est normal !)

**Votre application sera construite et testée, mais PAS déployée sur Kubernetes.**

---

## 🤔 Et Si Je Veux Vraiment Déployer sur Kubernetes ?

### Option 1 : Utiliser Minikube Localement (Pour tester en local)

```bash
# Sur votre machine Windows
cd C:\Users\lenovo\git\RecipeYouLove
powershell -ExecutionPolicy Bypass -File .\k8s\setup-local-cluster.ps1

# Puis déployer
cd k8s\environments\integration\scripts
powershell -ExecutionPolicy Bypass -File .\deploy-all.ps1
```

✅ Cela fonctionne **UNIQUEMENT sur votre machine locale**, pas dans GitHub Actions.

### Option 2 : Utiliser un Cluster Cloud (Pour la production)

Si vous voulez déployer automatiquement depuis GitHub Actions, vous avez besoin d'un **vrai cluster Kubernetes** :

#### A. Azure Kubernetes Service (AKS) - Recommandé

```bash
# 1. Créer un cluster (via Azure Portal ou CLI)
az aks create --resource-group myResourceGroup --name myAKSCluster --node-count 1 --enable-addons monitoring --generate-ssh-keys

# 2. Récupérer les credentials
az aks get-credentials --resource-group myResourceGroup --name myAKSCluster

# 3. Encoder en base64 pour GitHub
[Convert]::ToBase64String([System.Text.Encoding]::UTF8.GetBytes((Get-Content $env:USERPROFILE\.kube\config -Raw)))
```

#### B. AWS EKS

```bash
# 1. Créer un cluster
eksctl create cluster --name myEKSCluster --region us-west-2

# 2. Le kubeconfig est automatiquement configuré
aws eks update-kubeconfig --region us-west-2 --name myEKSCluster

# 3. Encoder en base64
cat ~/.kube/config | base64 -w 0
```

#### C. Google GKE

```bash
# 1. Créer un cluster
gcloud container clusters create myGKECluster --num-nodes=1

# 2. Récupérer les credentials
gcloud container clusters get-credentials myGKECluster --region=us-central1

# 3. Encoder en base64
cat ~/.kube/config | base64 -w 0
```

**Ensuite :**
1. Copiez la valeur base64
2. Allez dans GitHub → Settings → Secrets and variables → Actions → Secrets
3. Créez un secret `KUBE_CONFIG` avec cette valeur
4. Changez `ENABLE_K8S_DEPLOY` à `true`

---

## 💡 Recommandation

**Pour le moment**, utilisez la **Solution Rapide** (désactiver K8s) :
- ✅ Votre CI/CD fonctionnera immédiatement
- ✅ Vous pouvez continuer à développer
- ✅ Vous ajouterez Kubernetes plus tard quand vous en aurez besoin

**Plus tard**, quand vous voudrez déployer :
- Utilisez **Minikube en local** pour tester
- Utilisez **un cluster cloud** pour la production

---

## 🆘 Besoin d'Aide ?

Si vous suivez la Solution Rapide et que ça ne fonctionne toujours pas :
1. Vérifiez que vous avez bien créé la **variable** (pas le secret) `ENABLE_K8S_DEPLOY`
2. Vérifiez que la valeur est bien `false` (en minuscules)
3. Regardez les logs du workflow dans l'onglet "Actions"

