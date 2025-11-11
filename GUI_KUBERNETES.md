# 🖥️ Interfaces Graphiques pour Kubernetes

Vous avez **3 options principales** pour gérer vos ressources Kubernetes avec une interface graphique.

## 🥇 Option 1 : Lens (RECOMMANDÉ)

**La meilleure interface graphique pour Kubernetes**

### ✅ Pourquoi Lens ?

- Interface moderne et intuitive
- Application desktop (Windows, Mac, Linux)
- Multi-clusters
- Logs en temps réel
- Terminal intégré dans les pods
- Édition YAML avec auto-complétion
- Graphiques de métriques
- **100% Gratuit et Open Source**

### 📥 Installation

1. **Télécharger** : https://k8slens.dev/
2. **Installer** : Double-cliquez sur le `.exe`
3. **Lancer** : Lens détecte automatiquement votre `~/.kube/config`
4. **Connecter** : Cliquez sur votre cluster OKE

### 🎯 Utilisation

```
Lens
 ├─ Workloads
 │   ├─ Pods (voir tous les pods, logs, terminal)
 │   ├─ Deployments (scaler, éditer)
 │   └─ Services
 ├─ Configuration
 │   ├─ ConfigMaps
 │   └─ Secrets (masqués par défaut)
 ├─ Network
 │   ├─ Services
 │   └─ Ingress
 └─ Storage
     ├─ PersistentVolumes
     └─ PersistentVolumeClaims
```

**Guide complet** : [LENS_GUIDE.md](./LENS_GUIDE.md)

---

## 🥈 Option 2 : Kubernetes Dashboard (Officiel)

**Interface web officielle de Kubernetes**

### 📥 Installation

```powershell
# Exécuter le script d'installation
.\oci-scripts\install-kubernetes-dashboard.ps1
```

### 🌐 Accès

1. **Démarrer le proxy** :
   ```powershell
   kubectl proxy
   ```

2. **Ouvrir dans le navigateur** :
   ```
   http://localhost:8001/api/v1/namespaces/kubernetes-dashboard/services/https:kubernetes-dashboard:/proxy/
   ```

3. **Se connecter** :
   - Choisir **Token**
   - Coller le token sauvegardé dans `C:\Users\lenovo\kubernetes-dashboard-token.txt`

### 🎯 Fonctionnalités

- Vue d'ensemble du cluster
- Gestion des workloads (pods, deployments)
- Logs et métriques
- Édition YAML
- Exécution de commandes

---

## 🥉 Option 3 : k9s (Terminal UI)

**Interface en ligne de commande interactive**

### 📥 Installation

```powershell
# Avec Chocolatey
choco install k9s

# Ou télécharger depuis
# https://github.com/derailed/k9s/releases
```

### 🎯 Utilisation

```powershell
# Lancer k9s
k9s

# Raccourcis
# :pods     - Voir les pods
# :deploy   - Voir les deployments
# :svc      - Voir les services
# :ns       - Changer de namespace
# l         - Voir les logs d'un pod
# s         - Shell dans un pod
# d         - Décrire une ressource
# Ctrl+D    - Supprimer
```

---

## 📊 Comparaison des interfaces

| Fonctionnalité | Lens | Dashboard | k9s |
|----------------|------|-----------|-----|
| **Type** | Desktop | Web | Terminal |
| **Facilité d'utilisation** | ⭐⭐⭐⭐⭐ | ⭐⭐⭐⭐ | ⭐⭐⭐ |
| **Logs en temps réel** | ✅ | ✅ | ✅ |
| **Terminal dans pod** | ✅ | ✅ | ✅ |
| **Édition YAML** | ✅ (avec auto-complétion) | ✅ | ✅ |
| **Métriques** | ✅ | ✅ | ✅ |
| **Multi-clusters** | ✅ | ❌ | ✅ |
| **Extensions** | ✅ | ❌ | ❌ |
| **Gratuit** | ✅ | ✅ | ✅ |

---

## 🎯 Commandes kubectl essentielles

Même avec une interface graphique, ces commandes restent utiles :

### Voir les ressources

```powershell
# Tous les pods
kubectl get pods -n smartdish

# Tous les services
kubectl get svc -n smartdish

# Tous les deployments
kubectl get deployments -n smartdish

# Tout dans un namespace
kubectl get all -n smartdish

# Avec plus de détails
kubectl get pods -n smartdish -o wide
```

### Logs

```powershell
# Logs d'un pod
kubectl logs <pod-name> -n smartdish

# Logs en temps réel
kubectl logs -f <pod-name> -n smartdish

# Logs des 100 dernières lignes
kubectl logs --tail=100 <pod-name> -n smartdish
```

### Terminal dans un pod

```powershell
# Bash
kubectl exec -it <pod-name> -n smartdish -- /bin/bash

# Sh (si bash n'existe pas)
kubectl exec -it <pod-name> -n smartdish -- /bin/sh
```

### Décrire une ressource

```powershell
# Pod
kubectl describe pod <pod-name> -n smartdish

# Service
kubectl describe svc <service-name> -n smartdish

# Deployment
kubectl describe deployment <deployment-name> -n smartdish
```

### Scaler un deployment

```powershell
# Changer le nombre de replicas
kubectl scale deployment <deployment-name> --replicas=3 -n smartdish
```

### Redémarrer un deployment

```powershell
kubectl rollout restart deployment <deployment-name> -n smartdish
```

---

## 🎓 Workflow recommandé

### Pour le développement quotidien

1. **Lens** pour la visualisation générale
2. **kubectl** pour les commandes rapides
3. **k9s** pour le troubleshooting rapide

### Pour la production

1. **Lens** pour monitorer
2. **Dashboard** pour partager l'accès (avec token)
3. **kubectl** pour les opérations critiques

---

## 🔗 Liens utiles

- **Lens** : https://k8slens.dev/
- **Kubernetes Dashboard** : https://kubernetes.io/docs/tasks/access-application-cluster/web-ui-dashboard/
- **k9s** : https://k9scli.io/
- **kubectl cheatsheet** : https://kubernetes.io/docs/reference/kubectl/cheatsheet/

---

## 🚀 Installation rapide

### Lens (Recommandé)

```powershell
# 1. Télécharger
start https://k8slens.dev/

# 2. Installer le .exe
# 3. Lancer Lens
# 4. Votre cluster OKE sera détecté automatiquement
```

### Kubernetes Dashboard

```powershell
# Installer
.\oci-scripts\install-kubernetes-dashboard.ps1

# Accéder
kubectl proxy
start http://localhost:8001/api/v1/namespaces/kubernetes-dashboard/services/https:kubernetes-dashboard:/proxy/
```

### k9s

```powershell
# Installer (avec Chocolatey)
choco install k9s

# Lancer
k9s
```

---

## ✅ Checklist

- [ ] Lens installé et connecté au cluster
- [ ] OU Kubernetes Dashboard installé et accessible
- [ ] OU k9s installé
- [ ] Capable de voir les pods dans `smartdish`
- [ ] Capable de voir les logs
- [ ] Capable d'ouvrir un terminal dans un pod

---

**Recommandation** : Commencez par **Lens** - c'est la solution la plus complète et la plus facile à utiliser ! 🚀

