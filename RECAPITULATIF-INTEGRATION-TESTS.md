# 📋 Récapitulatif - Tests d'Intégration Newman

## 🔍 **Comment fonctionne le baseUrl**

### **1. Génération de l'URL du service (dans deploy-kubernetes.yml)**

```yaml
- name: Get service endpoint
  id: endpoint
  run: |
    # Minikube génère une URL NodePort unique
    SERVICE_URL=$(minikube service univ-soa --url -n soa-integration)
    
    # Si échec, on construit manuellement : http://<MINIKUBE_IP>:<NODEPORT>
    if [ -z "$SERVICE_URL" ]; then
      MINIKUBE_IP=$(minikube ip)                    # Ex: 192.168.49.2
      NODEPORT=$(kubectl get svc univ-soa ... )     # Ex: 31813
      SERVICE_URL="http://$MINIKUBE_IP:$NODEPORT"  # → http://192.168.49.2:31813
    fi
    
    echo "service_url=$SERVICE_URL" >> $GITHUB_OUTPUT
```

**Résultat :**
- L'URL est sauvegardée dans `service-url.txt` 
- Exemple : `http://192.168.49.2:31813`

---

### **2. Utilisation dans les tests Newman (integration-tests.yml)**

```yaml
- name: Run Newman integration tests
  run: |
    # Lecture de l'URL générée par Kubernetes
    SERVICE_URL=$(cat ../../service-url.txt)
    echo "🎯 Testing against: $SERVICE_URL"
    
    # Injection dans le fichier d'environnement Newman
    jq --arg url "$SERVICE_URL" \
      '(.values[] | select(.key == "baseUrl") | .value) = $url' \
      env.json > env.tmp.json
    
    # Newman utilise cette URL pour toutes les requêtes
    npx newman run ./collection.json --environment ./env.tmp.json ...
```

**Ce qui se passe :**
1. ✅ Le `baseUrl` dans `env.json` est remplacé par l'URL réelle Minikube
2. ✅ Toutes les requêtes Newman utilisent `{{baseUrl}}/api/items/...`
3. ✅ Exemple : `POST http://192.168.49.2:31813/api/items`

---

### **3. Structure des requêtes Newman (collection.json)**

```json
{
  "name": "Create item",
  "request": {
    "method": "POST",
    "url": "{{baseUrl}}/api/items",
    "body": { "raw": "{ \"id\": {{id}}, \"name\": \"{{name}}\" }" }
  }
}
```

**Variables utilisées :**
- `{{baseUrl}}` → Injecté depuis `env.tmp.json` → `http://192.168.49.2:31813`
- `{{id}}`, `{{name}}` → Proviennent de `dataset.json` (données de test)

---

## 🔧 **Corrections apportées**

### ❌ **Avant** (problèmes)
```yaml
# Reporter htmlextra causait des erreurs de module
--reporters cli,json,htmlextra
--reporter-htmlextra-export ./newman-results/newman-report.html

# Pas de vérification de connectivité
# Pas de gestion d'erreur explicite
```

### ✅ **Après** (corrigé)
```yaml
# Uniquement reporters stables (cli + json)
--reporters cli,json
--reporter-json-export ./newman-results/newman-report.json

# Test de connectivité avant les tests
curl -v --connect-timeout 10 --max-time 30 "${SERVICE_URL}/" || ...

# Gestion d'erreur avec diagnostic
--bail || {
  echo "❌ Newman tests failed!"
  curl -v "${SERVICE_URL}/actuator/health" || true
  exit 1
}
```

---

## 📊 **Flux complet**

```
┌─────────────────────────────────────────────────────────────┐
│ 1. deploy-kubernetes.yml                                    │
│    → Déploie sur Minikube                                   │
│    → Génère l'URL : http://192.168.49.2:31813              │
│    → Sauvegarde dans service-url.txt                        │
└─────────────────┬───────────────────────────────────────────┘
                  │
                  ↓ (artifact: service-url)
┌─────────────────────────────────────────────────────────────┐
│ 2. integration-tests.yml                                    │
│    → Télécharge service-url.txt                             │
│    → Injecte l'URL dans env.json → env.tmp.json            │
│    → Lance Newman avec le bon baseUrl                       │
│                                                              │
│    Newman exécute :                                         │
│    ├─ POST http://192.168.49.2:31813/api/items            │
│    ├─ GET  http://192.168.49.2:31813/api/items/1          │
│    ├─ PUT  http://192.168.49.2:31813/api/items/1          │
│    └─ DELETE http://192.168.49.2:31813/api/items/1        │
└─────────────────────────────────────────────────────────────┘
```

---

## 🎯 **Pourquoi ça échouait avant**

### **Problème 1 : Reporter htmlextra**
```
newman: could not find "htmlextra" reporter
```
- Le module `newman-reporter-htmlextra` n'était pas correctement chargé
- Solution : **Retiré**, on utilise uniquement `cli,json`

### **Problème 2 : Pas de vérification de connectivité**
```
ETIMEOUT at request
```
- Newman essayait de se connecter sans vérifier si le service était accessible
- Solution : **Ajout de `curl` avant les tests**

### **Problème 3 : Pas de diagnostic en cas d'erreur**
- Quand les tests échouaient, on ne savait pas pourquoi
- Solution : **Ajout de logs et vérification du health endpoint**

---

## ✅ **État actuel**

### **Déploiement Kubernetes** ✅ Fonctionne
- Minikube démarre correctement
- MySQL prêt
- phpMyAdmin déployé
- univ-soa déployé
- URL générée : `http://192.168.49.2:31813`

### **Tests Newman** 🔧 Corrigé
- Installation correcte des dépendances
- Vérification de connectivité ajoutée
- Reporter htmlextra retiré
- Meilleure gestion des erreurs

### **Logs des composants** ✅ Fonctionne
- Affiche l'URL de l'API
- Affiche les informations MySQL
- Affiche les instructions pour phpMyAdmin

---

## 🚀 **Prochaine exécution**

Lors du prochain push, le pipeline devrait :
1. ✅ Builder l'application
2. ✅ Créer l'image Docker
3. ✅ Déployer sur Minikube
4. ✅ Attendre que MySQL et univ-soa soient ready
5. ✅ **Exécuter les tests Newman avec succès**
6. ✅ Logger toutes les URLs des composants

---

## 📝 **Commandes utiles pour debug local**

### Tester Newman localement
```bash
cd tests/newman
npm install
SERVICE_URL="http://192.168.49.2:31813"
jq --arg url "$SERVICE_URL" '(.values[] | select(.key == "baseUrl") | .value) = $url' env.json > env.tmp.json
npx newman run collection.json -e env.tmp.json -d dataset.json --reporters cli,json
```

### Vérifier le service Kubernetes
```bash
# Status des pods
kubectl get pods -n soa-integration

# URL du service
minikube service univ-soa --url -n soa-integration

# Tester le health endpoint
curl http://192.168.49.2:31813/actuator/health
```

---

## 🎉 **Résumé**

- **baseUrl** → Généré dynamiquement par Minikube (NodePort)
- **Newman** → Utilise ce baseUrl pour toutes les requêtes HTTP
- **Corrections** → Reporter htmlextra retiré, vérifications ajoutées
- **Résultat attendu** → Tests Newman passent maintenant ✅

