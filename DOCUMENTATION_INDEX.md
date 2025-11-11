# 📚 SmartDish - Index de la Documentation

Bienvenue dans le projet SmartDish ! Ce fichier vous guide vers la documentation appropriée selon vos besoins.

## 🎯 Vous débutez ?

**Commencez ici** : [QUICKSTART.md](./QUICKSTART.md)
- Guide de démarrage en 5 étapes
- Configuration minimale en 30-45 minutes
- Premier déploiement en 15-20 minutes

## 📖 Documentation par profil

### 👨‍💻 Développeur

| Document | Description | Quand l'utiliser |
|----------|-------------|------------------|
| [README.md](./README.md) | Documentation principale du projet | Pour comprendre l'architecture globale |
| [QUICKSTART.md](./QUICKSTART.md) | Démarrage rapide | Pour débuter rapidement |
| [COMMANDS_CHEATSHEET.md](./COMMANDS_CHEATSHEET.md) | Aide-mémoire des commandes | Au quotidien pour les commandes courantes |

### 🔧 DevOps / Infrastructure

| Document | Description | Quand l'utiliser |
|----------|-------------|------------------|
| [OCI_CONFIGURATION.md](./OCI_CONFIGURATION.md) | Configuration OCI complète | Pour configurer l'infrastructure |
| [TODO_DEPLOYMENT.md](./TODO_DEPLOYMENT.md) | Liste des actions à réaliser | Pour suivre la configuration étape par étape |
| [helm/smartdish/README.md](./helm/smartdish/README.md) | Documentation Helm Chart | Pour déployer avec Helm |

### 📊 Chef de projet / Product Owner

| Document | Description | Quand l'utiliser |
|----------|-------------|------------------|
| [MIGRATION_SUMMARY.md](./MIGRATION_SUMMARY.md) | Résumé de la migration Azure→OCI | Pour comprendre ce qui a été fait |
| [README.md](./README.md) | Vue d'ensemble | Pour présenter le projet |

## 🗂️ Documentation par sujet

### Infrastructure & Cloud

- **[OCI_CONFIGURATION.md](./OCI_CONFIGURATION.md)** - Configuration Oracle Cloud Infrastructure
  - Création du cluster OKE
  - Configuration OCIR (Container Registry)
  - Base de données MySQL
  - Vault et secrets
  - DNS et domaines

- **[TODO_DEPLOYMENT.md](./TODO_DEPLOYMENT.md)** - Actions de déploiement
  - Configuration OCI CLI
  - Secrets GitHub à créer
  - Installation des composants Kubernetes
  - Checklist complète

### CI/CD & Déploiement

- **[README.md](./README.md)** - Pipeline CI/CD
  - Workflow automatique
  - Environnements (Integration/Production)
  - Structure des branches

- **[helm/smartdish/README.md](./helm/smartdish/README.md)** - Déploiement Helm
  - Installation des charts
  - Configuration par environnement
  - Commandes de gestion

### Développement

- **[QUICKSTART.md](./QUICKSTART.md)** - Démarrage rapide
  - Configuration initiale
  - Premier déploiement
  - Tests de l'application

- **[COMMANDS_CHEATSHEET.md](./COMMANDS_CHEATSHEET.md)** - Commandes utiles
  - OCI CLI
  - kubectl (Kubernetes)
  - Helm
  - Docker
  - Troubleshooting

### Migration & Historique

- **[MIGRATION_SUMMARY.md](./MIGRATION_SUMMARY.md)** - Résumé de la migration
  - Ce qui a été fait
  - Architecture finale
  - Comparaison Azure vs OCI

## 🚀 Parcours recommandés

### Parcours 1 : Premier déploiement (débutant)

1. ✅ Lisez [QUICKSTART.md](./QUICKSTART.md) - Vue d'ensemble
2. ✅ Suivez [TODO_DEPLOYMENT.md](./TODO_DEPLOYMENT.md) - Configuration étape par étape
3. ✅ Consultez [COMMANDS_CHEATSHEET.md](./COMMANDS_CHEATSHEET.md) - Commandes courantes
4. ✅ Testez votre premier déploiement

### Parcours 2 : Configuration complète (avancé)

1. ✅ Lisez [README.md](./README.md) - Architecture globale
2. ✅ Étudiez [OCI_CONFIGURATION.md](./OCI_CONFIGURATION.md) - Infrastructure détaillée
3. ✅ Configurez selon [TODO_DEPLOYMENT.md](./TODO_DEPLOYMENT.md)
4. ✅ Déployez avec [helm/smartdish/README.md](./helm/smartdish/README.md)
5. ✅ Utilisez [COMMANDS_CHEATSHEET.md](./COMMANDS_CHEATSHEET.md) au quotidien

### Parcours 3 : Développement microservice (développeur)

1. ✅ Lisez [README.md](./README.md) - Comprendre l'architecture
2. ✅ Suivez [QUICKSTART.md](./QUICKSTART.md) - Configuration rapide
3. ✅ Forkez le repository
4. ✅ Développez votre microservice dans `src/`
5. ✅ Utilisez [COMMANDS_CHEATSHEET.md](./COMMANDS_CHEATSHEET.md) pour débugger

## 📋 Checklist avant de commencer

### Accès et comptes
- [ ] Compte Oracle Cloud Infrastructure (OCI)
- [ ] Compte GitHub
- [ ] Accès au cluster OKE

### Outils installés localement
- [ ] Git
- [ ] OCI CLI
- [ ] kubectl
- [ ] Helm (optionnel mais recommandé)
- [ ] Docker (optionnel, pour tests locaux)

### Configuration
- [ ] Kubeconfig du cluster OKE
- [ ] Auth Token OCIR créé
- [ ] Secrets GitHub configurés
- [ ] Variables GitHub configurées

## 🔍 Recherche rapide

### Je veux...

**...déployer l'application rapidement**
→ [QUICKSTART.md](./QUICKSTART.md)

**...comprendre l'architecture**
→ [README.md](./README.md)

**...configurer l'infrastructure OCI**
→ [OCI_CONFIGURATION.md](./OCI_CONFIGURATION.md)

**...voir la liste complète des tâches**
→ [TODO_DEPLOYMENT.md](./TODO_DEPLOYMENT.md)

**...trouver une commande kubectl/helm**
→ [COMMANDS_CHEATSHEET.md](./COMMANDS_CHEATSHEET.md)

**...déployer avec Helm**
→ [helm/smartdish/README.md](./helm/smartdish/README.md)

**...comprendre la migration Azure→OCI**
→ [MIGRATION_SUMMARY.md](./MIGRATION_SUMMARY.md)

**...débugger un problème**
→ [COMMANDS_CHEATSHEET.md](./COMMANDS_CHEATSHEET.md) section Troubleshooting

**...créer un microservice**
→ [README.md](./README.md) section "Forker pour un microservice"

**...configurer la CI/CD**
→ [README.md](./README.md) section "CI/CD Pipeline"

## 🆘 Support

### Problèmes courants

1. **Les pods ne démarrent pas**
   - Consultez [COMMANDS_CHEATSHEET.md](./COMMANDS_CHEATSHEET.md) > Troubleshooting > Problème de démarrage des pods

2. **Erreur dans la CI/CD**
   - Vérifiez les secrets GitHub dans [TODO_DEPLOYMENT.md](./TODO_DEPLOYMENT.md)

3. **Connexion MySQL impossible**
   - Voir [COMMANDS_CHEATSHEET.md](./COMMANDS_CHEATSHEET.md) > Troubleshooting > Problème avec MySQL

4. **Image Docker non trouvée**
   - Consultez [COMMANDS_CHEATSHEET.md](./COMMANDS_CHEATSHEET.md) > Troubleshooting > Problème avec les images

## 📊 État du projet

- ✅ Migration Azure → OCI : **Terminée**
- ✅ Workflows CI/CD : **Configurés et fonctionnels**
- ✅ Helm Charts : **Créés**
- ✅ Documentation : **Complète**
- ⬜ Configuration initiale : **À faire** (voir [TODO_DEPLOYMENT.md](./TODO_DEPLOYMENT.md))
- ⬜ Premier déploiement : **À tester**

## 📅 Dernière mise à jour

**Date** : 11 novembre 2025  
**Version** : 1.0.0  
**Statut** : ✅ Prêt pour la configuration et le déploiement

---

## 🎯 Commencer maintenant

**Nouveau sur le projet ?** → Commencez par [QUICKSTART.md](./QUICKSTART.md)

**Prêt à configurer ?** → Suivez [TODO_DEPLOYMENT.md](./TODO_DEPLOYMENT.md)

**Besoin d'aide ?** → Consultez [COMMANDS_CHEATSHEET.md](./COMMANDS_CHEATSHEET.md)

**Bon développement !** 🚀

