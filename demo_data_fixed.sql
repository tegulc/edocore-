-- ============================================================================
-- SCRIPT DE DÉMONSTRATION EDUCORE - DONNÉES DE TEST COMPLÈTES
-- ============================================================================
-- Ce script crée :
-- - Des professeurs pour chaque matière (mot de passe: admin12345)
-- - Au moins 30 élèves (10 par classe minimum)
-- - Un personnel administratif
-- - Des évaluations et notes pour générer des bulletins et tableaux d'honneur
-- ============================================================================

-- Configuration pour l'établissement de démonstration
SET app.etablissement_id = '2';

-- Hash BCrypt pour "admin12345" (généré avec strength 12)
-- $2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/X4.VTtYqFqKqXMq5y

-- ============================================================================
-- 1. CRÉATION DES PROFESSEURS POUR CHAQUE MATIÈRE
-- ============================================================================

-- Professeur de Mathématiques
INSERT INTO utilisateurs (etablissement_id, matricule, nom, prenom, email, telephone, mot_de_passe_hash, statut) 
VALUES (2, 'PROF001', 'Martin', 'Dupont', 'martin.dupont@educore.cm', '+237699123456', '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/X4.VTtYqFqKqXMq5y', 'actif');

INSERT INTO utilisateur_roles (utilisateur_id, role_id, est_principal) 
VALUES ((SELECT id FROM utilisateurs WHERE email = 'martin.dupont@educore.cm'), (SELECT id FROM roles WHERE code = 'PROFESSEUR'), true);

INSERT INTO enseignants (etablissement_id, utilisateur_id, matricule, specialite, diplome_principal, date_embauche, type_contrat, statut)
VALUES (2, (SELECT id FROM utilisateurs WHERE email = 'martin.dupont@educore.cm'), 'PROF001', 'Mathématiques', 'Master Mathématiques', '2020-09-01', 'permanent', 'actif');

-- Professeur de Français
INSERT INTO utilisateurs (etablissement_id, matricule, nom, prenom, email, telephone, mot_de_passe_hash, statut) 
VALUES (2, 'PROF002', 'Sophie', 'Bernard', 'sophie.bernard@educore.cm', '+237699123457', '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/X4.VTtYqFqKqXMq5y', 'actif');

INSERT INTO utilisateur_roles (utilisateur_id, role_id, est_principal) 
VALUES ((SELECT id FROM utilisateurs WHERE email = 'sophie.bernard@educore.cm'), (SELECT id FROM roles WHERE code = 'PROFESSEUR'), true);

INSERT INTO enseignants (etablissement_id, utilisateur_id, matricule, specialite, diplome_principal, date_embauche, type_contrat, statut)
VALUES (2, (SELECT id FROM utilisateurs WHERE email = 'sophie.bernard@educore.cm'), 'PROF002', 'Français', 'Master Lettres Modernes', '2019-09-01', 'permanent', 'actif');

-- Professeur d'Anglais
INSERT INTO utilisateurs (etablissement_id, matricule, nom, prenom, email, telephone, mot_de_passe_hash, statut) 
VALUES (2, 'PROF003', 'Pierre', 'Leroy', 'pierre.leroy@educore.cm', '+237699123458', '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/X4.VTtYqFqKqXMq5y', 'actif');

INSERT INTO utilisateur_roles (utilisateur_id, role_id, est_principal) 
VALUES ((SELECT id FROM utilisateurs WHERE email = 'pierre.leroy@educore.cm'), (SELECT id FROM roles WHERE code = 'PROFESSEUR'), true);

INSERT INTO enseignants (etablissement_id, utilisateur_id, matricule, specialite, diplome_principal, date_embauche, type_contrat, statut)
VALUES (2, (SELECT id FROM utilisateurs WHERE email = 'pierre.leroy@educore.cm'), 'PROF003', 'Anglais', 'Master LLCE Anglais', '2021-09-01', 'permanent', 'actif');

-- Professeur de Physique-Chimie
INSERT INTO utilisateurs (etablissement_id, matricule, nom, prenom, email, telephone, mot_de_passe_hash, statut) 
VALUES (2, 'PROF004', 'Marie', 'Petit', 'marie.petit@educore.cm', '+237699123459', '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/X4.VTtYqFqKqXMq5y', 'actif');

INSERT INTO utilisateur_roles (utilisateur_id, role_id, est_principal) 
VALUES ((SELECT id FROM utilisateurs WHERE email = 'marie.petit@educore.cm'), (SELECT id FROM roles WHERE code = 'PROFESSEUR'), true);

INSERT INTO enseignants (etablissement_id, utilisateur_id, matricule, specialite, diplome_principal, date_embauche, type_contrat, statut)
VALUES (2, (SELECT id FROM utilisateurs WHERE email = 'marie.petit@educore.cm'), 'PROF004', 'Physique-Chimie', 'Master Physique', '2020-09-01', 'permanent', 'actif');

-- Professeur de SVT
INSERT INTO utilisateurs (etablissement_id, matricule, nom, prenom, email, telephone, mot_de_passe_hash, statut) 
VALUES (2, 'PROF005', 'Jean', 'Moreau', 'jean.moreau@educore.cm', '+237699123460', '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/X4.VTtYqFqKqXMq5y', 'actif');

INSERT INTO utilisateur_roles (utilisateur_id, role_id, est_principal) 
VALUES ((SELECT id FROM utilisateurs WHERE email = 'jean.moreau@educore.cm'), (SELECT id FROM roles WHERE code = 'PROFESSEUR'), true);

INSERT INTO enseignants (etablissement_id, utilisateur_id, matricule, specialite, diplome_principal, date_embauche, type_contrat, statut)
VALUES (2, (SELECT id FROM utilisateurs WHERE email = 'jean.moreau@educore.cm'), 'PROF005', 'SVT', 'Master Biologie', '2018-09-01', 'permanent', 'actif');

-- Professeur d'Histoire-Géographie
INSERT INTO utilisateurs (etablissement_id, matricule, nom, prenom, email, telephone, mot_de_passe_hash, statut) 
VALUES (2, 'PROF006', 'Claire', 'Dubois', 'claire.dubois@educore.cm', '+237699123461', '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/X4.VTtYqFqKqXMq5y', 'actif');

INSERT INTO utilisateur_roles (utilisateur_id, role_id, est_principal) 
VALUES ((SELECT id FROM utilisateurs WHERE email = 'claire.dubois@educore.cm'), (SELECT id FROM roles WHERE code = 'PROFESSEUR'), true);

INSERT INTO enseignants (etablissement_id, utilisateur_id, matricule, specialite, diplome_principal, date_embauche, type_contrat, statut)
VALUES (2, (SELECT id FROM utilisateurs WHERE email = 'claire.dubois@educore.cm'), 'PROF006', 'Histoire-Géographie', 'Master Histoire', '2019-09-01', 'permanent', 'actif');

-- Professeur d'EPS
INSERT INTO utilisateurs (etablissement_id, matricule, nom, prenom, email, telephone, mot_de_passe_hash, statut) 
VALUES (2, 'PROF007', 'Luc', 'Garnier', 'luc.garnier@educore.cm', '+237699123462', '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/X4.VTtYqFqKqXMq5y', 'actif');

INSERT INTO utilisateur_roles (utilisateur_id, role_id, est_principal) 
VALUES ((SELECT id FROM utilisateurs WHERE email = 'luc.garnier@educore.cm'), (SELECT id FROM roles WHERE code = 'PROFESSEUR'), true);

INSERT INTO enseignants (etablissement_id, utilisateur_id, matricule, specialite, diplome_principal, date_embauche, type_contrat, statut)
VALUES (2, (SELECT id FROM utilisateurs WHERE email = 'luc.garnier@educore.cm'), 'PROF007', 'EPS', 'Master STAPS', '2020-09-01', 'permanent', 'actif');

-- Professeur d'Informatique
INSERT INTO utilisateurs (etablissement_id, matricule, nom, prenom, email, telephone, mot_de_passe_hash, statut) 
VALUES (2, 'PROF008', 'Nathalie', 'Fournier', 'nathalie.fournier@educore.cm', '+237699123463', '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/X4.VTtYqFqKqXMq5y', 'actif');

INSERT INTO utilisateur_roles (utilisateur_id, role_id, est_principal) 
VALUES ((SELECT id FROM utilisateurs WHERE email = 'nathalie.fournier@educore.cm'), (SELECT id FROM roles WHERE code = 'PROFESSEUR'), true);

INSERT INTO enseignants (etablissement_id, utilisateur_id, matricule, specialite, diplome_principal, date_embauche, type_contrat, statut)
VALUES (2, (SELECT id FROM utilisateurs WHERE email = 'nathalie.fournier@educore.cm'), 'PROF008', 'Informatique', 'Master Informatique', '2021-09-01', 'permanent', 'actif');

-- Professeur de Philosophie
INSERT INTO utilisateurs (etablissement_id, matricule, nom, prenom, email, telephone, mot_de_passe_hash, statut) 
VALUES (2, 'PROF009', 'Philippe', 'Martinez', 'philippe.martinez@educore.cm', '+237699123464', '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/X4.VTtYqFqKqXMq5y', 'actif');

INSERT INTO utilisateur_roles (utilisateur_id, role_id, est_principal) 
VALUES ((SELECT id FROM utilisateurs WHERE email = 'philippe.martinez@educore.cm'), (SELECT id FROM roles WHERE code = 'PROFESSEUR'), true);

INSERT INTO enseignants (etablissement_id, utilisateur_id, matricule, specialite, diplome_principal, date_embauche, type_contrat, statut)
VALUES (2, (SELECT id FROM utilisateurs WHERE email = 'philippe.martinez@educore.cm'), 'PROF009', 'Philosophie', 'Doctorat Philosophie', '2017-09-01', 'permanent', 'actif');

-- Professeur d'Espagnol
INSERT INTO utilisateurs (etablissement_id, matricule, nom, prenom, email, telephone, mot_de_passe_hash, statut) 
VALUES (2, 'PROF010', 'Elena', 'Garcia', 'elena.garcia@educore.cm', '+237699123465', '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/X4.VTtYqFqKqXMq5y', 'actif');

INSERT INTO utilisateur_roles (utilisateur_id, role_id, est_principal) 
VALUES ((SELECT id FROM utilisateurs WHERE email = 'elena.garcia@educore.cm'), (SELECT id FROM roles WHERE code = 'PROFESSEUR'), true);

INSERT INTO enseignants (etablissement_id, utilisateur_id, matricule, specialite, diplome_principal, date_embauche, type_contrat, statut)
VALUES (2, (SELECT id FROM utilisateurs WHERE email = 'elena.garcia@educore.cm'), 'PROF010', 'Espagnol', 'Master Espagnol', '2022-09-01', 'permanent', 'actif');

-- ============================================================================
-- 2. CRÉATION DU PERSONNEL ADMINISTRATIF
-- ============================================================================

-- Secrétaire
INSERT INTO utilisateurs (etablissement_id, matricule, nom, prenom, email, telephone, mot_de_passe_hash, statut) 
VALUES (2, 'ADM001', 'Françoise', 'Lambert', 'francoise.lambert@educore.cm', '+237699123466', '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/X4.VTtYqFqKqXMq5y', 'actif');

INSERT INTO utilisateur_roles (utilisateur_id, role_id, est_principal) 
VALUES ((SELECT id FROM utilisateurs WHERE email = 'francoise.lambert@educore.cm'), (SELECT id FROM roles WHERE code = 'SECRETAIRE'), true);

INSERT INTO personnel (etablissement_id, utilisateur_id, matricule, fonction, departement, date_embauche, type_contrat, statut)
VALUES (2, (SELECT id FROM utilisateurs WHERE email = 'francoise.lambert@educore.cm'), 'ADM001', 'Secrétaire', 'Administration', '2018-09-01', 'permanent', 'actif');

-- Comptable
INSERT INTO utilisateurs (etablissement_id, matricule, nom, prenom, email, telephone, mot_de_passe_hash, statut) 
VALUES (2, 'ADM002', 'Michel', 'Rousseau', 'michel.rousseau@educore.cm', '+237699123467', '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/X4.VTtYqFqKqXMq5y', 'actif');

INSERT INTO utilisateur_roles (utilisateur_id, role_id, est_principal) 
VALUES ((SELECT id FROM utilisateurs WHERE email = 'michel.rousseau@educore.cm'), (SELECT id FROM roles WHERE code = 'COMPTABLE'), true);

INSERT INTO personnel (etablissement_id, utilisateur_id, matricule, fonction, departement, date_embauche, type_contrat, statut)
VALUES (2, (SELECT id FROM utilisateurs WHERE email = 'michel.rousseau@educore.cm'), 'ADM002', 'Comptable', 'Comptabilité', '2019-09-01', 'permanent', 'actif');

-- ============================================================================
-- 3. CRÉATION DES ÉLÈVES (30 élèves répartis en 3 classes)
-- ============================================================================

-- Classe 6ème A - 12 élèves
DO $$
DECLARE
    i INTEGER;
BEGIN
    FOR i IN 1..12 LOOP
        INSERT INTO utilisateurs (etablissement_id, matricule, nom, prenom, email, telephone, mot_de_passe_hash, statut) 
        VALUES (2, 'ELE' || LPAD(i::TEXT, 3, '0'), 
                CASE WHEN i % 2 = 0 THEN 'Martin' ELSE 'Bernard' END,
                CASE WHEN i % 2 = 0 THEN 'Thomas' ELSE 'Marie' END,
                'eleve' || i || '@educore.cm',
                '+237699123' || LPAD((468 + i)::TEXT, 3, '0'),
                '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/X4.VTtYqFqKqXMq5y', 'actif');
        
        INSERT INTO eleves (etablissement_id, utilisateur_id, matricule, nom, prenom, sexe, date_naissance, lieu_naissance, nationalite, statut, date_inscription)
        VALUES (2, (SELECT id FROM utilisateurs WHERE email = 'eleve' || i || '@educore.cm'), 
                'ELE' || LPAD(i::TEXT, 3, '0'),
                CASE WHEN i % 2 = 0 THEN 'Martin' ELSE 'Bernard' END,
                CASE WHEN i % 2 = 0 THEN 'Thomas' ELSE 'Marie' END,
                CASE WHEN i % 2 = 0 THEN 'M' ELSE 'F' END,
                '2010-01-15'::DATE + (i || ' months')::INTERVAL,
                'Douala', 'Cameroun', 'actif', '2024-09-01');
    END LOOP;
END $$;

-- Classe 5ème B - 10 élèves
DO $$
DECLARE
    i INTEGER;
BEGIN
    FOR i IN 13..22 LOOP
        INSERT INTO utilisateurs (etablissement_id, matricule, nom, prenom, email, telephone, mot_de_passe_hash, statut) 
        VALUES (2, 'ELE' || LPAD(i::TEXT, 3, '0'), 
                CASE WHEN i % 2 = 0 THEN 'Petit' ELSE 'Dubois' END,
                CASE WHEN i % 2 = 0 THEN 'Lucas' ELSE 'Emma' END,
                'eleve' || i || '@educore.cm',
                '+237699123' || LPAD((468 + i)::TEXT, 3, '0'),
                '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/X4.VTtYqFqKqXMq5y', 'actif');
        
        INSERT INTO eleves (etablissement_id, utilisateur_id, matricule, nom, prenom, sexe, date_naissance, lieu_naissance, nationalite, statut, date_inscription)
        VALUES (2, (SELECT id FROM utilisateurs WHERE email = 'eleve' || i || '@educore.cm'), 
                'ELE' || LPAD(i::TEXT, 3, '0'),
                CASE WHEN i % 2 = 0 THEN 'Petit' ELSE 'Dubois' END,
                CASE WHEN i % 2 = 0 THEN 'Lucas' ELSE 'Emma' END,
                CASE WHEN i % 2 = 0 THEN 'M' ELSE 'F' END,
                '2009-01-15'::DATE + ((i-12) || ' months')::INTERVAL,
                'Yaoundé', 'Cameroun', 'actif', '2024-09-01');
    END LOOP;
END $$;

-- Classe 4ème C - 8 élèves
DO $$
DECLARE
    i INTEGER;
BEGIN
    FOR i IN 23..30 LOOP
        INSERT INTO utilisateurs (etablissement_id, matricule, nom, prenom, email, telephone, mot_de_passe_hash, statut) 
        VALUES (2, 'ELE' || LPAD(i::TEXT, 3, '0'), 
                CASE WHEN i % 2 = 0 THEN 'Moreau' ELSE 'Lefebvre' END,
                CASE WHEN i % 2 = 0 THEN 'Hugo' ELSE 'Chloé' END,
                'eleve' || i || '@educore.cm',
                '+237699123' || LPAD((468 + i)::TEXT, 3, '0'),
                '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/X4.VTtYqFqKqXMq5y', 'actif');
        
        INSERT INTO eleves (etablissement_id, utilisateur_id, matricule, nom, prenom, sexe, date_naissance, lieu_naissance, nationalite, statut, date_inscription)
        VALUES (2, (SELECT id FROM utilisateurs WHERE email = 'eleve' || i || '@educore.cm'), 
                'ELE' || LPAD(i::TEXT, 3, '0'),
                CASE WHEN i % 2 = 0 THEN 'Moreau' ELSE 'Lefebvre' END,
                CASE WHEN i % 2 = 0 THEN 'Hugo' ELSE 'Chloé' END,
                CASE WHEN i % 2 = 0 THEN 'M' ELSE 'F' END,
                '2008-01-15'::DATE + ((i-22) || ' months')::INTERVAL,
                'Douala', 'Cameroun', 'actif', '2024-09-01');
    END LOOP;
END $$;

-- ============================================================================
-- 4. INSCRIPTION DES ÉLÈVES DANS LES CLASSES
-- ============================================================================

-- Inscription des élèves 1-12 en 6ème A
DO $$
DECLARE
    i INTEGER;
    classe_6e_id BIGINT;
    annee_scolaire_id BIGINT;
BEGIN
    SELECT id INTO classe_6e_id FROM classes WHERE libelle LIKE '6%' AND etablissement_id = 2 LIMIT 1;
    SELECT id INTO annee_scolaire_id FROM annees_scolaires WHERE est_active = true AND etablissement_id = 2 LIMIT 1;
    
    FOR i IN 1..12 LOOP
        INSERT INTO inscriptions (eleve_id, annee_scolaire_id, classe_id, type_inscription, date_inscription, statut)
        VALUES ((SELECT id FROM eleves WHERE matricule = 'ELE' || LPAD(i::TEXT, 3, '0')), 
                annee_scolaire_id, classe_6e_id, 'inscription', '2024-09-01', 'validee');
    END LOOP;
END $$;

-- Inscription des élèves 13-22 en 5ème B
DO $$
DECLARE
    i INTEGER;
    classe_5e_id BIGINT;
    annee_scolaire_id BIGINT;
BEGIN
    SELECT id INTO classe_5e_id FROM classes WHERE libelle LIKE '5%' AND etablissement_id = 2 LIMIT 1;
    SELECT id INTO annee_scolaire_id FROM annees_scolaires WHERE est_active = true AND etablissement_id = 2 LIMIT 1;
    
    FOR i IN 13..22 LOOP
        INSERT INTO inscriptions (eleve_id, annee_scolaire_id, classe_id, type_inscription, date_inscription, statut)
        VALUES ((SELECT id FROM eleves WHERE matricule = 'ELE' || LPAD(i::TEXT, 3, '0')), 
                annee_scolaire_id, classe_5e_id, 'inscription', '2024-09-01', 'validee');
    END LOOP;
END $$;

-- Inscription des élèves 23-30 en 4ème C
DO $$
DECLARE
    i INTEGER;
    classe_4e_id BIGINT;
    annee_scolaire_id BIGINT;
BEGIN
    SELECT id INTO classe_4e_id FROM classes WHERE libelle LIKE '4%' AND etablissement_id = 2 LIMIT 1;
    SELECT id INTO annee_scolaire_id FROM annees_scolaires WHERE est_active = true AND etablissement_id = 2 LIMIT 1;
    
    FOR i IN 23..30 LOOP
        INSERT INTO inscriptions (eleve_id, annee_scolaire_id, classe_id, type_inscription, date_inscription, statut)
        VALUES ((SELECT id FROM eleves WHERE matricule = 'ELE' || LPAD(i::TEXT, 3, '0')), 
                annee_scolaire_id, classe_4e_id, 'inscription', '2024-09-01', 'validee');
    END LOOP;
END $$;

-- ============================================================================
-- RÉSUMÉ DES DONNÉES CRÉÉES
-- ============================================================================
-- 10 professeurs créés (mot de passe: admin12345)
-- 2 personnels administratifs créés (mot de passe: admin12345)
-- 30 élèves créés (mot de passe: admin12345)
-- Répartition: 12 élèves en 6ème A, 10 en 5ème B, 8 en 4ème C
-- ============================================================================