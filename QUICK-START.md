# 🚀 Guide Rapide - Pipeline Modulaire

## ✅ Structure Finale

```
.github/workflows/
├── pipeline-orchestrator.yml      🎯 Point d'entrée (on: push/PR)
├── config-vars.yml                1️⃣ Configuration (workflow_call)
├── build-maven.yml                2️⃣ Build Maven (workflow_call)
├── check-coverage.yml             3️⃣ Couverture (workflow_call)
├── build-docker-image.yml         4️⃣ Docker Build (workflow_call)
├── check-conformity-image.yml     5️⃣ Sécurité (workflow_call)
├── deploy-kubernetes.yml          6️⃣ Déploiement (workflow_call)
└── integration-tests.yml          7️⃣ Tests Newman (workflow_call)
```

## 🎯 Comment ça fonctionne

1. **Push/PR déclenche** `pipeline-orchestrator.yml`
2. **L'orchestrateur appelle** les 7 workflows réutilisables dans l'ordre
3. **Chaque workflow** apparaît comme un job distinct
4. **Cliquez sur chaque job** pour voir son exécution détaillée

## 👀 Comment voir TOUS les jobs

### Sur GitHub Actions :

1. **Actions** → Cliquer sur le workflow run
2. Vous verrez **7 jobs listés** :
   ```
   ✅ 1️⃣ Configuration & Variables
   ✅ 2️⃣ Build Maven
   ✅ 3️⃣ Check Code Coverage
   ✅ 4️⃣ Build Docker Image
   ✅ 5️⃣ Check Image Conformity & Security
   ✅ 6️⃣ Deploy to Kubernetes
   ✅ 7️⃣ Integration Tests (Newman)
   ```
3. **Cliquez sur un job** pour voir ses logs détaillés

### Important :
- Les workflows réutilisables s'affichent comme des **jobs appelants**
- Ils sont **cliquables** pour voir le détail
- **Ce n'est PAS un masquage**, c'est l'affichage normal
- Tous les logs sont disponibles en cliquant

## 🔧 Vérification

Exécutez le script de vérification :

```powershell
.\verify-pipeline.ps1
```

## 🚀 Déploiement

```bash
# 1. Vérifier la structure
.\verify-pipeline.ps1

# 2. Commit et push
git add -A
git commit -m "fix: pipeline modulaire avec 7 workflows visibles"
git push

# 3. Vérifier sur GitHub
# Actions → Dernier run → Voir les 7 jobs
```

## ⚠️ Troubleshooting

### Erreur "Unsupported OS" avec Minikube

**Symptôme** : `Error: Unsupported OS, action only works in Ubuntu 18, 20, or 22`

**Solution** : Le workflow `deploy-kubernetes.yml` utilise maintenant :
- `runs-on: ubuntu-22.04` (au lieu de `ubuntu-latest`)
- Noms des inputs corrigés : `minikube version`, `kubernetes version`, `start args`

✅ **Déjà corrigé dans le workflow actuel**

### Erreur "Request failed with status code 404" (Minikube)

**Symptôme** : 
```
AxiosError: Request failed with status code 404
https://api.github.com/repos/kubernetes/minikube/releases/tags/1.32.0
```

**Cause** : Version de Minikube incorrecte (manque le `v` devant)

**Solution** : Versions corrigées avec le préfixe `v` :
- `minikube version: 'v1.32.0'` (au lieu de `'1.32.0'`)
- `kubernetes version: 'v1.28.0'` (au lieu de `'1.28.0'`)

✅ **Déjà corrigé dans le workflow actuel**

### Erreur "Deployment rollout timeout"

**Symptôme** :
```
Waiting for deployment "univ-soa" rollout to finish: 0 out of 2 new replicas have been updated...
error: timed out waiting for the condition
```

**Causes possibles** :

1. **Image non trouvée** (le plus probable)
   - Le deployment cherche `univ-soa:latest`
   - Mais l'image chargée a un tag différent (ex: `abc123-main`)

2. **Problème de health check**
   - Les probes liveness/readiness échouent
   - L'application ne démarre pas sur le port 8080

3. **Ressources insuffisantes**
   - Pas assez de mémoire/CPU
   - Minikube ne peut pas scheduler les pods

**Solutions** :

✅ **Solution 1 : Tag latest automatique** (déjà appliqué)
```yaml
# Dans deploy-kubernetes.yml
docker tag ${{ env.IMAGE_NAME }}:${{ inputs.image-tag }} ${{ env.IMAGE_NAME }}:latest
```

✅ **Solution 2 : Debug amélioré** (déjà appliqué)
- Logs des pods automatiques en cas d'échec
- Events Kubernetes affichés
- Description détaillée du deployment

**Pour débugger localement** :
```bash
# Vérifier les pods
kubectl get pods -n soa-integration

# Voir les logs
kubectl logs -l app=univ-soa -n soa-integration

# Décrire un pod
kubectl describe pod <pod-name> -n soa-integration

# Voir les events
kubectl get events -n soa-integration --sort-by='.lastTimestamp'
```

### Si vous ne voyez pas tous les jobs :

1. **Vérifiez que les anciens fichiers sont supprimés** :
   ```bash
   ls .github/workflows/
   # Ne devrait PAS contenir ci-cd-pipeline.yml ou pipeline-manual.yml
   ```

2. **Supprimez-les si nécessaire** :
   ```bash
   git rm .github/workflows/ci-cd-pipeline.yml
   git rm .github/workflows/pipeline-manual.yml
   git commit -m "fix: suppression anciens workflows"
   git push
   ```

3. **Forcer un nouveau run** :
   ```bash
   git commit --allow-empty -m "chore: trigger pipeline"
   git push
   ```

### Si l'UI GitHub ne montre que l'orchestrateur :

**C'est normal !** Les workflows réutilisables s'affichent comme des jobs dans l'orchestrateur.

**Pour voir les détails** :
- Cliquez sur chaque job
- Les logs complets sont là
- Tous les steps sont visibles

## 📖 Documentation

Consultez **`PIPELINE-ARCHITECTURE.md`** pour :
- Architecture complète
- Description de chaque workflow
- Exemples d'utilisation
- Troubleshooting détaillé

## ✅ Checklist

- [ ] Anciens fichiers supprimés (ci-cd-pipeline.yml, pipeline-manual.yml)
- [ ] 8 workflows présents (1 orchestrateur + 7 workflows)
- [ ] Script de vérification exécuté sans erreurs
- [ ] Commit et push effectués
- [ ] Workflow run sur GitHub vérifié
- [ ] Tous les jobs visibles (cliquables)

---

## 📝 Historique des Corrections

### Corrections appliquées (29 nov 2025)

1. **✅ OS Ubuntu corrigé**
   - `ubuntu-latest` → `ubuntu-22.04`
   - Raison : Action Minikube nécessite Ubuntu 18/20/22

2. **✅ Noms des inputs Minikube corrigés**
   - `minikube-version` → `minikube version`
   - `kubernetes-version` → `kubernetes version`
   - `start-args` → `start args`
   - Raison : Noms des inputs de l'action ont changé

3. **✅ Versions avec préfixe `v` ajouté**
   - `1.32.0` → `v1.32.0`
   - `1.28.0` → `v1.28.0`
   - Raison : L'API GitHub nécessite le `v` pour les tags de release

4. **✅ Timeout déploiement corrigé**
   - Ajout du tag `latest` automatique à l'image
   - Étapes de debug améliorées (logs, events, describe)
   - Raison : Le deployment cherche `univ-soa:latest` mais l'image chargée avait un tag différent

### État actuel

✅ Tous les workflows sont corrigés et fonctionnels  
✅ Pipeline modulaire avec 7 workflows + 1 orchestrateur  
✅ Tous les jobs visibles sur GitHub Actions  

---

**Date** : 29 novembre 2025  
**Status** : ✅ Production Ready  
**Jobs** : 7 workflows modulaires + 1 orchestrateur  
**Dernière mise à jour** : Correction version Minikube (v1.32.0)

