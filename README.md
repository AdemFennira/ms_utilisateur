# 🔐 ms-utilisateur - Microservice d'Authentification

## 📖 Vue d'ensemble

Le **microservice Utilisateur** gère l'authentification et la gestion des utilisateurs de l'application **SmartDish**. Il communique avec le microservice Persistance pour stocker et récupérer les données.

### Responsabilités

- 🔐 Authentification JWT
- 👤 Gestion des utilisateurs
- 🔑 Inscription et connexion
- 🛡️ Hashage sécurisé des mots de passe (BCrypt)

## 🏗️ Architecture

```
┌─────────────────┐      HTTP REST      ┌────────────────────┐
│ ms-utilisateur  │ ──────────────────> │  ms-persistance    │
│   (Port 8092)   │                     │   (Port 8090)      │
└─────────────────┘                     └─────────┬──────────┘
                                                  │
                                                  ▼
                                        ┌────────────────────┐
                                        │   MySQL Database   │
                                        └────────────────────┘
```

### Stack Technologique

- **Framework** : Spring Boot 3.3.4
- **Langage** : Java 21
- **Sécurité** : Spring Security + JWT (jjwt 0.11.5)
- **Base de données** : MySQL (via ms-persistance)
- **Build** : Maven 3.8+
- **Documentation** : Swagger/OpenAPI

## 🚀 Installation

### Prérequis

- Java 21+
- Maven 3.8+
- ms-persistance démarré (**obligatoire**)

### Démarrage

#### 1. Cloner le projet

```bash
git clone https://github.com/AdemFennira/ms_utilisateur.git
cd ms-utilisateur
```

#### 2. Configurer l'environnement

Récupérer le fichier `.env` auprès de l'administrateur et le placer à la racine du projet.

#### 3. Démarrer ms-persistance

⚠️ **IMPORTANT** : Démarrer ms-persistance en premier !

```bash
cd ../ms-persistance
mvn spring-boot:run
```

#### 4. Compiler et lancer

```bash
cd ../ms-utilisateur
mvn clean install
mvn spring-boot:run
```

## 🔗 Accès aux services

| Service | URL                                           |
|---------|-----------------------------------------------|
| **Swagger UI** | http://localhost:8092/swagger-ui.html         |
| **Health Check** | http://localhost:8092/api/utilisateurs/health |

## 📡 API Endpoints

### Endpoints publics (sans authentification)

| Méthode | Endpoint | Description |
|---------|----------|-------------|
| `POST` | `/api/utilisateurs/register` | Inscription d'un nouvel utilisateur |
| `POST` | `/api/utilisateurs/login` | Connexion et génération du token JWT |

### Endpoints protégés (JWT requis)

| Méthode | Endpoint | Description | Autorisation |
|---------|----------|-------------|--------------|
| `GET` | `/api/utilisateurs/{id}` | Obtenir un utilisateur | Authentifié |
| `GET` | `/api/utilisateurs/email/{email}` | Obtenir un utilisateur par email | Authentifié |
| `GET` | `/api/utilisateurs` | Lister tous les utilisateurs | Admin |
| `PUT` | `/api/utilisateurs/{id}` | Mettre à jour un utilisateur | Authentifié |
| `DELETE` | `/api/utilisateurs/{id}` | Supprimer un utilisateur | Admin |

## 🗂️ Structure du projet

```
ms-utilisateur/
├── src/main/java/.../
│   ├── client/
│   │   └── PersistanceClient.java      • Communication HTTP avec ms-persistance
│   ├── config/
│   │   ├── RestTemplateConfig.java     • Configuration RestTemplate
│   │   ├── SecurityConfig.java         • Configuration Spring Security
│   │   ├── OpenApiConfig.java          • Configuration Swagger
|   |   └── DotenvConfig.java           • Chargement des variables d'environnement
│   ├── controller/
│   │   └── UtilisateurController.java
│   ├── dto/
│   │   ├── UtilisateurCreateDto.java
│   │   ├── UtilisateurUpdateDto.java
│   │   ├── UtilisateurResponseDto.java
│   │   └── LoginDto.java
│   ├── exception/
│   │   ├── GlobalExceptionHandler.java
│   │   ├── UtilisateurNotFoundException.java
│   │   └── EmailAlreadyExistsException.java
│   ├── security/
│   │   ├── JwtUtil.java                • Génération et validation JWT
│   │   └── JwtAuthenticationFilter.java • Filtre d'authentification
│   └── service/
│       ├── UtilisateurService.java
│       └── UtilisateurServiceImpl.java • Utilise PersistanceClient
├── .env                                 # Fourni par l'admin (non versionné)
└── pom.xml
```

## 🔐 Authentification JWT

### Inscription

```bash
POST /api/utilisateurs/register
Content-Type: application/json

{
  "email": "user@example.com",
  "motDePasse": "password123",
  "nom": "Dupont",
  "prenom": "Jean",
  "role": "USER"
}
```

**Réponse :**
```json
{
  "id": 1,
  "email": "user@example.com",
  "nom": "Dupont",
  "prenom": "Jean",
  "role": "USER",
  "actif": true,
  "dateCreation": "2025-12-01T10:00:00"
}
```

### Connexion

```bash
POST /api/utilisateurs/login
Content-Type: application/json

{
  "email": "user@example.com",
  "motDePasse": "password123"
}
```

**Réponse :**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "type": "Bearer"
}
```

### Utiliser le token

```bash
GET /api/utilisateurs/1
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

## 📋 Validation

### Inscription

- **email** : Obligatoire, format email valide, unique
- **motDePasse** : Obligatoire, min 8 caractères
- **nom** : Obligatoire, 2-100 caractères
- **prenom** : Obligatoire, 2-100 caractères
- **telephone** : Optionnel, format français `+33` ou `0`
- **role** : `USER` (défaut) ou `ADMIN`

### Connexion

- **email** : Obligatoire
- **motDePasse** : Obligatoire

### Mise à jour

- Tous les champs sont optionnels
- Le mot de passe sera hashé automatiquement

## ⚙️ Configuration

### Variables d'environnement (.env)

```env
# Port du microservice
SERVER_PORT=8092

# URL de ms-persistance
PERSISTANCE_SERVICE_URL=http://localhost:8090

# Configuration JWT
JWT_SECRET=maCleSuperSecreteDePlusDe32Octets123!
JWT_EXPIRATION=86400000

# Actuator
ACTUATOR_ENDPOINTS=health,info,metrics
ACTUATOR_HEALTH_DETAILS=always
```

## 🚀 Build production

```bash
# Créer le JAR
mvn clean package -DskipTests

# Lancer
java -jar target/ms-utilisateur-1.0.0.jar
```

## 🧪 Tests

```bash
# Tests unitaires
mvn test

# Tests avec coverage
mvn clean test jacoco:report
```

## 📚 Ressources

- [Documentation Spring Boot](https://docs.spring.io/spring-boot/docs/current/reference/html/)
- [Spring Security](https://docs.spring.io/spring-security/reference/)
- [JWT (jjwt)](https://github.com/jwtk/jjwt)
- [Swagger/OpenAPI](https://swagger.io/docs/)
- [Documentation ms-persistance](https://github.com/Sabine22-alt/ms-persistance)

---