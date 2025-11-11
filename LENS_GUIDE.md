# Guide d'installation de Lens - The Kubernetes IDE

## 🎯 Lens - L'interface graphique la plus complète pour Kubernetes

**Lens** est considéré comme le meilleur IDE pour Kubernetes. C'est une application desktop gratuite et open-source.

### ✅ Avantages de Lens

- ✅ Interface moderne et intuitive
- ✅ Multi-clusters (gérez plusieurs clusters)
- ✅ Visualisation en temps réel
- ✅ Terminal intégré
- ✅ Édition YAML avec auto-complétion
- ✅ Logs en temps réel
- ✅ Graphiques de métriques (CPU, RAM)
- ✅ Gestion des secrets, ConfigMaps, etc.
- ✅ Compatible Windows, Mac, Linux

### 📥 Installation

1. **Télécharger Lens** :
   - Allez sur : https://k8slens.dev/
   - Cliquez sur **Download for Windows**
   - Ou : https://github.com/lensapp/lens/releases

2. **Installer** :
   - Double-cliquez sur le fichier `.exe` téléchargé
   - Suivez l'assistant d'installation
   - Lancez Lens

3. **Connecter à votre cluster** :
   - Lens détecte automatiquement le fichier `~/.kube/config`
   - Votre cluster OKE apparaît dans la liste
   - Cliquez sur le cluster pour vous connecter

### 🎨 Fonctionnalités principales

#### 1. Vue d'ensemble (Overview)
- CPU et mémoire des nodes
- Nombre de pods, services, deployments
- État général du cluster

#### 2. Workloads
- **Pods** : Voir tous les pods, leurs logs, terminal
- **Deployments** : Gérer les déploiements, scaler
- **StatefulSets** : Pour les applications stateful
- **DaemonSets** : Pour les agents système
- **Jobs & CronJobs** : Tâches planifiées

#### 3. Configuration
- **ConfigMaps** : Variables d'environnement
- **Secrets** : Mots de passe, tokens (affichage sécurisé)
- **Resource Quotas** : Limites de ressources
- **HPA** : Autoscaling

#### 4. Network
- **Services** : Voir tous les services
- **Ingress** : Configuration des routes HTTP/HTTPS
- **Network Policies** : Règles de sécurité réseau

#### 5. Storage
- **PersistentVolumes** : Stockage persistant
- **PersistentVolumeClaims** : Demandes de stockage
- **StorageClasses** : Types de stockage

### 🔧 Utilisation de Lens

#### Voir les pods
1. Cliquez sur **Workloads** > **Pods**
2. Filtrez par namespace : `smartdish`
3. Cliquez sur un pod pour voir ses détails

#### Voir les logs
1. Sélectionnez un pod
2. Cliquez sur l'onglet **Logs**
3. Logs en temps réel avec auto-refresh

#### Ouvrir un terminal dans un pod
1. Sélectionnez un pod
2. Cliquez sur l'onglet **Shell**
3. Terminal interactif dans le conteneur

#### Scaler un deployment
1. **Workloads** > **Deployments**
2. Trouvez votre deployment
3. Cliquez sur les **3 points** > **Scale**
4. Modifiez le nombre de replicas

#### Éditer un deployment
1. Sélectionnez le deployment
2. Cliquez sur **Edit**
3. Modifiez le YAML
4. **Save**

### 📊 Dashboard Lens - Fonctionnalités avancées

#### Métriques (avec Prometheus)
- CPU usage par pod
- Memory usage par pod
- Network traffic
- Disk I/O

#### Extensions Lens
- **@alebcay/openlens-node-pod-menu** : Actions rapides
- **lens-extension-cc** : Cost analysis
- **@nevalla/kube-hunter** : Security scanning

### 🎯 Raccourcis clavier

| Raccourci | Action |
|-----------|--------|
| `Ctrl + K` | Recherche rapide |
| `Ctrl + Shift + K` | Changer de cluster |
| `Ctrl + T` | Nouveau terminal |
| `Ctrl + W` | Fermer l'onglet |
| `F5` | Rafraîchir |

### 🔐 Sécurité

Lens stocke vos kubeconfig en local de manière sécurisée. Les secrets sont masqués par défaut.

### 💡 Alternatives à Lens

#### 1. **Kubernetes Dashboard** (Officiel)
- Web-based (http://localhost:8001)
- Plus basique que Lens
- Installation avec le script fourni

#### 2. **k9s** (Terminal UI)
- Interface en ligne de commande
- Très rapide et léger
- Installation : `choco install k9s`

#### 3. **Portainer** (Docker + Kubernetes)
- Interface web pour Docker ET Kubernetes
- Gestion multi-environnements

#### 4. **Octant** (VMware)
- Interface web open-source
- Visualisation avancée

## 🚀 Commencer avec Lens

```powershell
# 1. Télécharger depuis https://k8slens.dev/
# 2. Installer
# 3. Lancer Lens
# 4. Votre cluster OKE sera automatiquement détecté
# 5. Cliquez sur le cluster pour vous connecter
```

## 📋 Checklist première utilisation

- [ ] Lens installé
- [ ] Cluster OKE connecté
- [ ] Namespace `smartdish` sélectionné
- [ ] Pods visibles
- [ ] Logs accessibles
- [ ] Terminal fonctionnel dans un pod

## 🎓 Tutoriel vidéo

Recherchez "Lens Kubernetes Tutorial" sur YouTube pour des tutoriels complets.

---

**Lens est l'outil idéal pour gérer votre cluster Kubernetes de manière visuelle et intuitive !**

