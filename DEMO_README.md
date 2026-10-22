# Guide de Création des Données de Démonstration EDUCORE

Ce guide explique comment créer les données de démonstration pour présenter le projet EDUCORE avec des données réalistes.

## 📋 Contenu des Données de Démonstration

### 👥 Utilisateurs Créés

#### Professeurs (10 professeurs, mot de passe: `admin12345`)
1. **Martin Dupont** - Mathématiques (martin.dupont@educore.cm)
2. **Sophie Bernard** - Français (sophie.bernard@educore.cm)
3. **Pierre Leroy** - Anglais (pierre.leroy@educore.cm)
4. **Marie Petit** - Physique-Chimie (marie.petit@educore.cm)
5. **Jean Moreau** - SVT (jean.moreau@educore.cm)
6. **Claire Dubois** - Histoire-Géographie (claire.dubois@educore.cm)
7. **Luc Garnier** - EPS (luc.garnier@educore.cm)
8. **Nathalie Fournier** - Informatique (nathalie.fournier@educore.cm)
9. **Philippe Martinez** - Philosophie (philippe.martinez@educore.cm)
10. **Elena Garcia** - Espagnol (elena.garcia@educore.cm)

#### Personnel Administratif (2 personnes, mot de passe: `admin12345`)
1. **Françoise Lambert** - Secrétaire (francoise.lambert@educore.cm)
2. **Michel Rousseau** - Comptable (michel.rousseau@educore.cm)

#### Élèves (30 élèves, mot de passe: `admin12345`)
- **Classe 6ème A**: 12 élèves (ELE001 à ELE012)
- **Classe 5ème B**: 10 élèves (ELE013 à ELE022)
- **Classe 4ème C**: 8 élèves (ELE023 à ELE030)

## 🚀 Méthodes d'Installation

### Méthode 1: Via Scripts SQL (Recommandée)

#### Prérequis
- PostgreSQL installé et accessible
- Base de données EDUCORE configurée

#### Étapes

1. **Démarrer PostgreSQL et se connecter à la base**
   ```bash
   # Sur Windows avec psql
   psql -U educore -d educore
   ```

2. **Exécuter le script principal de création des utilisateurs**
   ```bash
   \i C:/Users/prosper\ computer/Desktop/educore/demo_data.sql
   ```

3. **Exécuter le script des affectations et notes**
   ```bash
   \i C:/Users/prosper\ computer/Desktop/educore/demo_notes_evaluations.sql
   ```

### Méthode 2: Via l'Application Spring Boot

#### Prérequis
- Application Spring Boot démarrée
- Compte admin existant

#### Étapes

1. **Démarrer l'application backend**
   ```bash
   cd backend
   mvn spring-boot:run
   ```

2. **Utiliser l'endpoint de démonstration** (après implémentation complète)
   ```bash
   POST /api/v1/demo/generer-donnees-completes
   ```

3. **Calculer les moyennes**
   ```bash
   POST /api/v1/demo/calculer-moyennes
   ```

4. **Générer les bulletins**
   ```bash
   POST /api/v1/demo/generer-bulletins
   ```

## 📊 Structure des Données Créées

### Matières affectées par classe
- **6ème A**: Mathématiques, Français, Anglais, Physique-Chimie, SVT, Histoire-Géo, EPS, Informatique
- **5ème B**: Mathématiques, Français, Anglais, Physique-Chimie, SVT, Histoire-Géo, EPS, Informatique, Espagnol
- **4ème C**: Mathématiques, Français, Anglais, Physique-Chimie, SVT, Histoire-Géo, EPS, Informatique, Philosophie

### Évaluations créées
- 1 évaluation par matière par classe
- Type: DEVOIR
- Barème: 20
- Date: 2024-10-15

### Notes générées
- Notes aléatoires entre 8 et 18
- Distribution pour permettre d'avoir des élèves avec moyenne ≥ 12
- Commentaire: "Note de démonstration"

## 🎯 Objectifs Pédagogiques Atteints

### Workflow Pédagogique Complet
1. ✅ Création des professeurs par matière
2. ✅ Affectation des professeurs aux classes-matières
3. ✅ Création des élèves et inscription dans les classes
4. ✅ Création des évaluations
5. ✅ Saisie des notes par les professeurs
6. ✅ Validation des évaluations par les censeurs/directeurs
7. ⏳ Calcul automatique des moyennes
8. ⏳ Génération des bulletins
9. ⏳ Tableau d'honneur (élèves avec moyenne ≥ 12)

## 🔧 Vérification dans le Frontend

### Test des connexions
1. **Professeurs**: Connectez-vous avec les comptes professeurs (martin.dupont@educore.cm / admin12345)
2. **Personnel**: Connectez-vous avec les comptes administratifs (francoise.lambert@educore.cm / admin12345)
3. **Vérifiez** que vous pouvez accéder aux fonctionnalités selon vos rôles

### Test de la gestion des notes
1. Connectez-vous en tant que professeur
2. Allez dans la section "Notes"
3. Sélectionnez une classe et une matière
4. Vérifiez que les évaluations sont visibles
5. Vérifiez que les notes sont saisies

### Test des bulletins
1. Connectez-vous en tant qu'admin ou directeur
2. Allez dans la section "Bulletins"
3. Sélectionnez une classe et une période
4. Générez les bulletins
5. Vérifiez le tableau d'honneur

## 🐛 Dépannage

### Problème: Connexion PostgreSQL refusée
**Solution**: Vérifiez que PostgreSQL est démarré et que les identifiants dans `application.yml` sont corrects

### Problème: Scripts SQL ne s'exécutent pas
**Solution**: Vérifiez que vous avez les droits nécessaires sur la base de données

### Problème: Notes non visibles dans le frontend
**Solution**: 
- Vérifiez que les évaluations sont validées
- Recalculez les moyennes via l'API
- Vérifiez les RLS (Row Level Security)

## 📝 Notes Importantes

- **Mot de passe par défaut**: `admin12345` pour tous les comptes créés
- **Hash BCrypt**: `$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/X4.VTtYqFqKqXMq5y`
- **Établissement ID**: 1 (à adapter selon votre configuration)
- **Année scolaire**: 2024-2025
- **Période**: 1er trimestre

## 🔄 Mise à jour des Données

Pour régénérer les données:
1. Supprimez les données existantes (attention aux données en production!)
2. Réexécutez les scripts SQL
3. Recalculez les moyennes
4. Régénérez les bulletins

## 📞 Support

En cas de problème, vérifiez:
1. Les logs de l'application Spring Boot
2. Les logs PostgreSQL
3. La configuration RLS dans les migrations V7, V12, V16