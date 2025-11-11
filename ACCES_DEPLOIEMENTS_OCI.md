# 🌐 Accéder à vos déploiements OCI via la Console Web

## 🎯 Vous voulez voir ce qui est réellement déployé sur OCI (pas juste en local)

Il y a **3 façons** d'accéder à vos déploiements :

---

## 1️⃣ Console OCI - Kubernetes Clusters (Ce qui est déployé)

### Accéder à votre cluster OKE

1. **Ouvrir la console OCI** : https://cloud.oracle.com/
2. **Menu hamburger** (☰) en haut à gauche
3. **Developer Services** > **Kubernetes Clusters (OKE)**
4. Cliquez sur votre cluster : **quick-K3s-cluster-42186fdb7**

### Voir les workloads déployés

Dans la page du cluster :

#### Onglet "Workload" (à gauche)
- **Deployments** : Vos applications déployées
- **Pods** : Instances en cours d'exécution
- **Services** : Points d'accès réseau
- **Ingress** : Routes HTTP/HTTPS

#### Onglet "Node pools"
- Voir les serveurs (nodes) qui exécutent vos pods
- État des nodes

#### Onglet "Monitoring"
- CPU et mémoire utilisés
- Nombre de pods
- Trafic réseau

### 🔍 Voir les détails d'un pod

1. Onglet **Workload**
2. Cliquez sur **Pods**
3. Filtrez par **Namespace** : `smartdish`
4. Cliquez sur un pod pour voir :
   - État (Running, Pending, Error)
   - Logs
   - Événements
   - Utilisation CPU/RAM

---

## 2️⃣ OCI Cloud Shell - Ligne de commande dans le navigateur

### Accéder au Cloud Shell

1. **Console OCI** : https://cloud.oracle.com/
2. **Icône Cloud Shell** en haut à droite (icône de terminal)
3. Cliquez dessus pour ouvrir un terminal dans le navigateur

### Configurer kubectl dans Cloud Shell

```bash
# Obtenir le kubeconfig
oci ce cluster create-kubeconfig \
  --cluster-id ocid1.cluster.oc1.eu-paris-1.aaaaaaaaafkhi5vnahyc14ozq2ulnwstf3t6hslnvmomgeq5ulqsvca3gwzgw3tq \
  --file ~/.kube/config \
  --region eu-paris-1 \
  --token-version 2.0.0

# Vérifier
kubectl get nodes
kubectl get pods -n smartdish
kubectl get svc -n ingress-nginx
```

### Avantages du Cloud Shell

- ✅ Aucune installation locale nécessaire
- ✅ OCI CLI déjà configuré
- ✅ kubectl déjà installé
- ✅ Accès direct à votre cluster
- ✅ Fonctionne depuis n'importe où

---

## 3️⃣ OCIR - Container Registry (Vos images Docker)

### Voir vos images déployées

1. **Console OCI** : https://cloud.oracle.com/
2. **Menu** (☰) > **Developer Services** > **Container Registry**
3. Vous verrez vos repositories :
   - `smartdish/smartdish-parent`
   - Autres microservices...

### Détails d'une image

Cliquez sur un repository pour voir :
- **Tags** : Versions déployées (ex: `feat-ci-cd-71ae300`)
- **Taille** : Taille de l'image
- **Date de création**
- **Scan de sécurité** : Vulnérabilités détectées

---

## 4️⃣ Load Balancer - Accès public à vos applications

### Voir le Load Balancer

1. **Console OCI** : https://cloud.oracle.com/
2. **Menu** (☰) > **Networking** > **Load Balancers**
3. Vous verrez le Load Balancer créé par NGINX Ingress
4. **IP publique** : `141.145.216.180`

### Tester l'accès

```powershell
# Depuis PowerShell
$LB_IP = "141.145.216.180"
curl "http://soa-smartdish-parent.$LB_IP.nip.io"
```

Ou dans le navigateur :
```
http://soa-smartdish-parent.141.145.216.180.nip.io
```

---

## 5️⃣ Monitoring avec OCI Logging

### Voir les logs de votre application

1. **Console OCI** : https://cloud.oracle.com/
2. **Menu** (☰) > **Observability & Management** > **Logging**
3. **Log Groups** > Sélectionnez votre compartment
4. Logs de votre cluster OKE

---

## 📊 Résumé : Où voir quoi ?

| Ce que vous voulez voir | Où aller |
|-------------------------|----------|
| **Pods déployés** | Console OCI > Kubernetes > Cluster > Workload |
| **État des pods** | Console OCI > Kubernetes > Cluster > Pods |
| **Logs des pods** | Console OCI > Kubernetes > Pod > Logs OU Cloud Shell + kubectl |
| **Images Docker** | Console OCI > Container Registry |
| **Load Balancer IP** | Console OCI > Networking > Load Balancers |
| **Utilisation CPU/RAM** | Console OCI > Kubernetes > Cluster > Monitoring |
| **Exécuter des commandes** | Cloud Shell (icône en haut à droite) |

---

## 🚀 Accès rapide

### Console OCI
```
https://cloud.oracle.com/
```

### Votre cluster OKE
```
Console > Kubernetes Clusters (OKE) > quick-K3s-cluster-42186fdb7
```

### Cloud Shell (Terminal dans le navigateur)
```
Icône terminal en haut à droite de la console OCI
```

### Container Registry
```
Console > Container Registry > smartdish/*
```

---

## 🔧 Commandes Cloud Shell utiles

Une fois dans le Cloud Shell :

```bash
# Configurer kubectl
oci ce cluster create-kubeconfig \
  --cluster-id ocid1.cluster.oc1.eu-paris-1.aaaaaaaaafkhi5vnahyc14ozq2ulnwstf3t6hslnvmomgeq5ulqsvca3gwzgw3tq \
  --file ~/.kube/config \
  --region eu-paris-1

# Voir tous les pods
kubectl get pods --all-namespaces

# Voir les pods dans smartdish
kubectl get pods -n smartdish

# Voir les logs
kubectl logs -f <pod-name> -n smartdish

# Voir les services
kubectl get svc -n smartdish

# Voir l'IP du Load Balancer
kubectl get svc -n ingress-nginx ingress-nginx-controller

# Voir les déploiements
kubectl get deployments -n smartdish

# Voir tout
kubectl get all -n smartdish
```

---

## 💡 Recommandation

**Pour voir en temps réel ce qui est déployé** :

1. **Utilisez Lens** (que vous êtes en train de télécharger) :
   - Se connecte directement au cluster OKE
   - Affiche tout en temps réel
   - Interface graphique complète

2. **Ou utilisez Cloud Shell** :
   - Cliquez sur l'icône terminal dans la console OCI
   - Tapez les commandes kubectl ci-dessus
   - Pas besoin de configuration locale

3. **Ou utilisez la Console OCI** :
   - Menu > Kubernetes Clusters > Votre cluster
   - Onglet Workload > Pods
   - Visualisation web de tout ce qui est déployé

---

## ✅ Ce qui est actuellement déployé sur votre cluster

D'après votre installation :

```
Namespace: smartdish
├── Deployment: mysql
│   └── Pod: mysql-xxxxx (1 replica)
│
└── Service: mysql (ClusterIP)

Namespace: ingress-nginx
├── Deployment: ingress-nginx-controller
│   └── Pod: ingress-nginx-controller-xxxxx (1 replica)
│
└── Service: ingress-nginx-controller (LoadBalancer)
    └── EXTERNAL-IP: 141.145.216.180
```

---

**Une fois Lens installé, il détectera automatiquement votre cluster OKE et vous pourrez voir tout cela en temps réel !** 🚀

