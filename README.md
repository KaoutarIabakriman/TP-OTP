Ce projet consiste en la réalisation d'une application web complète, conteneurisée via Docker, gérant le **CRUD d'utilisateurs** et intégrant un **mécanisme d’authentification OTP (One-Time Password)** envoyé par SMS.

---

## 1. ⚙️ Architecture du Projet

L’application est orchestrée via **Docker Compose** et repose sur une **architecture en couches (N-Tiers)** dans le backend Java (Spring Boot), assurant modularité et maintenabilité.

---

### 1.1. Services Conteneurisés

| Service   | Technologie / Rôle        | Rôle principal |
|-----------|----------------------------|----------------|
| frontend  | React / Node.js           | Interface utilisateur (Web App) |
| backend   | Java (Spring Boot)        | Logique métier, API REST, gestion BDD & OTP |
| database  | BDD (Schéma DOSI)         | Stockage persistant (User & OTP) |
| nginx     | Serveur Web (Proxy)       | Sert le frontend et route les appels API |

---

### 1.2. Architecture Interne du Backend : Modèle N-Tiers

Le code Java est strictement structuré en couches, selon le principe de responsabilité unique (SRP).

| Couche     | Rôle | Responsabilités principales | Communication |
|------------|------|-----------------------------|---------------|
| **Controller** | Point d’entrée API | Gestion des requêtes HTTP, validation | → Appelle Service |
| **Service** | Logique métier | Règles d’affaires, orchestration | → Appelle DAO ou Utils |
| **Dao (Repository)** | Accès aux données | CRUD, interaction directe avec la BDD | → BDD |
| **Model** | Objets métier | Représentation des tables (User, OTP) | → Partout |
| **Utils** | Outils transversaux | Services externes (ex : SMSService 172.19.28.37) | ← Appelé par Service |

---

## 2. 🚀 Prérequis & Lancement

### 2.1. Prérequis

- Docker & Docker Compose  
- Git  

---

### 2.2. Configuration et Démarrage

#### 📥 Clonage du dépôt

```bash
git clone https://github.com/KaoutarIabakriman/TP-OTP.git
cd TP-OTP/TP3
