# TP OTP
Ce projet consiste en la réalisation d'une application web complète,
conteneurisée via Docker, gérant le **CRUD d'utilisateurs** et intégrant
un **mécanisme d'authentification OTP (One-Time Password)** envoyé par
SMS.

------------------------------------------------------------------------

## 1. Architecture du Projet

L'application est orchestrée via **Docker Compose** et repose sur une
**architecture en couches (N-Tiers)** dans le backend Java / Spring, assurant modularité, maintenabilité et séparation claire des
responsabilités.

Voici le fonctionnement de chaque couche :

------------------------------------------------------------------------

## Controller (API Layer)

### Rôle

Point d'entrée du backend : reçoit et traite les requêtes HTTP.

### Responsabilités

-   Validation du format des données\
-   Conversion JSON ↔ Java\
-   Appelle la couche **Service**\
-   Retourne une réponse HTTP

### Ne contient jamais

-   de logique métier\
-   d'accès direct à la BDD

------------------------------------------------------------------------

## Service (Business Layer)

### Rôle

Cœur de l'application : gère **toute la logique métier**.

### Responsabilités

-   Règles (OTP expiré, délai, validité)\
-   Orchestration des opérations (BDD + SMS)\
-   Gestion des exceptions métier\
-   Interaction avec :
    -   **DAO** (accès BDD)\

------------------------------------------------------------------------

## DAO / Repository (Data Access Layer)

### Rôle

Accès direct à la base.

### Responsabilités

-   Opérations CRUD\
-   Requêtes personnalisées

### Ne contient jamais

-   de logique métier

------------------------------------------------------------------------

## Model (Entities / Data Model)

### Rôle

Représentation des tables BDD sous forme de classes Java.

------------------------------------------------------------------------



# 2. Prérequis & Lancement

### 2.1. Prérequis

-   Docker & Docker Compose\
-   Git

------------------------------------------------------------------------

## 2.2. Configuration et Démarrage

### 1. Cloner le dépôt

``` bash
git clone https://github.com/KaoutarIabakriman/TP-OTP.git
cd TP-OTP/TP3
```

------------------------------------------------------------------------

### 2. Configurer les variables d'environnement

Créer ou compléter un fichier **.env** :

    DB_HOST=database
    DB_PORT=5432
    BACKEND_PORT=8080
    NGINX_PORT=80

------------------------------------------------------------------------

### 3. Lancer l'application

``` bash
docker compose up --build 
```

------------------------------------------------------------------------
