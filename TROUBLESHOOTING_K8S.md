# 🔧 Guide de Résolution - Erreur Kubernetes "connection refused localhost:8080"

## 📋 Le Problème

L'erreur que vous voyez dans GitHub Actions :
```
E1105 13:04:03.181408 ... couldn't get current server API group list: Get "http://localhost:8080/api?timeout=32s": dial tcp [::1]:8080: connect: connection refused
The connection to the server localhost:8080 was refused - did you specify the right host or port?
Error: Process completed with exit code 1.
```

## 🔍 Les 3 Scénarios Possibles

### Scénario 1: Secret KUBE_CONFIG Non Défini ⚠️ (LE PLUS PROBABLE)

**Diagnostic:**
- Vous n'avez pas de cluster Kubernetes configuré
- Le secret `KUBE_CONFIG` n'existe pas dans GitHub Actions
- Le workflow essaie de démarrer Minikube mais échoue

**Solution 1A - Désactiver temporairement les déploiements K8s (RECOMMANDÉ pour le dev):**

1. Allez dans votre repository GitHub
2. Settings → Secrets and variables → Actions → Variables
3. Cliquez sur "New repository variable"
4. Nom: `ENABLE_K8S_DEPLOY`
5. Valeur: `false`
6. Cliquez sur "Add variable"

✅ **Résultat:** Les jobs de déploiement Kubernetes seront sautés, mais le build et les tests continueront de fonctionner.

**Solution 1B - Configurer un cluster Kubernetes externe:**

Si vous avez accès à un cluster Kubernetes (AKS, EKS, GKE, ou un cluster privé):

1. **Récupérez votre kubeconfig:**
   ```bash
   # Pour Azure AKS
   az aks get-credentials --resource-group myResourceGroup --name myAKSCluster
   
   # Pour AWS EKS
   aws eks update-kubeconfig --region region-code --name my-cluster
   
   # Pour Google GKE
   gcloud container clusters get-credentials my-cluster --region=us-central1
   ```

2. **Encodez votre kubeconfig en base64:**
   ```bash
   # Linux/Mac
   cat ~/.kube/config | base64 -w 0
   
   # Windows PowerShell
   [Convert]::ToBase64String([System.Text.Encoding]::UTF8.GetBytes((Get-Content $env:USERPROFILE\.kube\config -Raw)))
   ```

3. **Ajoutez le secret dans GitHub:**
   - Allez dans Settings → Secrets and variables → Actions
   - Cliquez sur "New repository secret"
   - Nom: `KUBE_CONFIG`
   - Valeur: Collez le texte base64
   - Cliquez sur "Add secret"

4. **Activez les déploiements:**
   - Settings → Secrets and variables → Actions → Variables
   - Créez ou modifiez `ENABLE_K8S_DEPLOY` à `true`

✅ **Résultat:** Le workflow utilisera votre cluster externe.

---

### Scénario 2: Minikube Échoue à Démarrer dans GitHub Actions

**Diagnostic:**
- Le secret `KUBE_CONFIG` n'existe pas
- Minikube essaie de démarrer mais échoue
- L'environnement GitHub Actions ne supporte pas bien Minikube

**Solution:**

Minikube dans GitHub Actions est **déconseillé** car :
- ❌ Ressources limitées (CPU, RAM)
- ❌ Temps de démarrage long (2-5 minutes)
- ❌ Instable dans les runners GitHub

**Recommandation:** Utilisez Solution 1A (désactiver K8s) ou 1B (cluster externe).

---

### Scénario 3: kubectl est Exécuté Trop Tôt

**Diagnostic:**
- Le cluster démarre mais kubectl est appelé avant qu'il soit prêt
- Problème de timing dans le script

**Solution:** J'ai déjà corrigé ce problème dans le workflow avec :
- ✅ Création du répertoire `.kube` avant toute commande kubectl
- ✅ Ajout d'un délai de stabilisation (`sleep 10`)
- ✅ Meilleure gestion des erreurs

---

## 🎯 Action Immédiate Recommandée

### Option A: Désactiver K8s (Pour continuer à développer rapidement)

```bash
# Via l'interface GitHub
1. Allez sur https://github.com/EmilieHascoet/RecipeYouLove/settings/variables/actions
2. Créez une variable "ENABLE_K8S_DEPLOY" avec la valeur "false"
3. Poussez vos changements
```

### Option B: Activer K8s avec Minikube (Expérimental)

```bash
# Via l'interface GitHub
1. Allez sur https://github.com/EmilieHascoet/RecipeYouLove/settings/variables/actions
2. Créez une variable "ENABLE_K8S_DEPLOY" avec la valeur "true"
3. Poussez vos changements
```

⚠️ **Attention:** L'Option B peut être lente et instable. Utilisez-la seulement pour tester.

---

## 📊 Que Font les Corrections Appliquées

J'ai modifié le workflow pour :

1. **Rendre les déploiements K8s optionnels:**
   ```yaml
   if: github.ref != 'refs/heads/main' && (vars.ENABLE_K8S_DEPLOY == 'true' || secrets.KUBE_CONFIG != '')
   ```
   - Si `ENABLE_K8S_DEPLOY` est `false` → Skip le déploiement ✅
   - Si `KUBE_CONFIG` existe → Utilise le cluster externe ✅
   - Si `ENABLE_K8S_DEPLOY` est `true` → Essaie Minikube ⚠️

2. **Améliorer la configuration Minikube:**
   - Créer `.kube` avant toute commande kubectl
   - Ajouter plus de ressources (2 CPU, 4GB RAM)
   - Ajouter un délai de stabilisation

3. **Meilleure gestion des erreurs:**
   - Exit immédiat en cas d'erreur (`set -e`)
   - Messages clairs à chaque étape

---

## ✅ Vérification que Tout Fonctionne

Après avoir suivi les étapes ci-dessus :

1. **Committez et poussez vos changements:**
   ```bash
   git add .
   git commit -m "fix: Corriger configuration Kubernetes et rendre déploiements optionnels"
   git push
   ```

2. **Vérifiez le workflow dans GitHub:**
   - Allez dans l'onglet "Actions"
   - Regardez le dernier workflow
   - Si `ENABLE_K8S_DEPLOY` est `false`, vous devriez voir :
     - ✅ Build and Test (succès)
     - ✅ Build Docker Image (succès)
     - ⏭️ Deploy to Integration (skipped)

3. **Les tests et builds continuent de fonctionner !**

---

## 🔄 Pour Plus Tard: Configurer un Vrai Cluster

Quand vous serez prêt à déployer sur un vrai cluster Kubernetes :

1. Choisissez un provider cloud (Azure AKS, AWS EKS, Google GKE)
2. Créez un cluster
3. Suivez la "Solution 1B" ci-dessus
4. Votre CI/CD déploiera automatiquement !

---

## 📞 Support

Si après avoir appliqué ces solutions, vous avez toujours des problèmes :

1. Vérifiez les logs dans GitHub Actions
2. Vérifiez que la variable `ENABLE_K8S_DEPLOY` est bien créée
3. Consultez `k8s/README.md` pour plus de détails

