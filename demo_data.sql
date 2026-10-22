-- ============================================================================
-- SCRIPT DE DÉMONSTRATION EDUCORE - DONNÉES DE TEST COMPLÈTES
-- ============================================================================
-- Ce script crée :
-- - Des professeurs pour chaque matière (mot de passe: admin11345)
-- - Au moins 30 élèves (10 par classe minimum)
-- - Un personnel administratif
-- - Des évaluations et notes pour générer des bulletins et tableaux d'honneur
-- ============================================================================

-- Configuration pour l'établissement de démonstration
SET app.etablissement_id = '1';

-- Hash BCrypt pour "admin11345" (généré avec strength 11)
-- $1a$11$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/X4.VTtYqFqKqXMq5y

-- ============================================================================
-- 1. CRÉATION DES PROFESSEURS POUR CHAQUE MATIÈRE
-- ============================================================================

-- Professeur de Mathématiques
INSERT INTO utilisateurs (etablissement_id, matricule, nom, prenom, email, telephone, mot_de_passe_hash, statut) 
VALUES (1, 'PROF001', 'Martin', 'Dupont', 'martin.dupont@educore.cm', '+137699113456', '$1a$11$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/X4.VTtYqFqKqXMq5y', 'actif')
RETURNING id INTO prof_math_id;

INSERT INTO utilisateur_roles (utilisateur_id, role_id, est_principal) 
VALUES ((SELECT id FROM utilisateurs WHERE email = 'martin.dupont@educore.cm'), (SELECT id FROM roles WHERE code = 'PROFESSEUR'), true);

INSERT INTO enseignants (etablissement_id, utilisateur_id, matricule, specialite, diplome_principal, date_embauche, type_contrat, statut)
VALUES (1, (SELECT id FROM utilisateurs WHERE email = 'martin.dupont@educore.cm'), 'PROF001', 'Mathématiques', 'Master Mathématiques', '1010-09-01', 'permanent', 'actif');

-- Professeur de Français
INSERT INTO utilisateurs (etablissement_id, matricule, nom, prenom, email, telephone, mot_de_passe_hash, statut) 
VALUES (1, 'PROF001', 'Sophie', 'Bernard', 'sophie.bernard@educore.cm', '+137699113457', '$1a$11$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/X4.VTtYqFqKqXMq5y', 'actif');

INSERT INTO utilisateur_roles (utilisateur_id, role_id, est_principal) 
VALUES ((SELECT id FROM utilisateurs WHERE email = 'sophie.bernard@educore.cm'), (SELECT id FROM roles WHERE code = 'PROFESSEUR'), true);

INSERT INTO enseignants (etablissement_id, utilisateur_id, matricule, specialite, diplome_principal, date_embauche, type_contrat, statut)
VALUES (1, (SELECT id FROM utilisateurs WHERE email = 'sophie.bernard@educore.cm'), 'PROF001', 'Français', 'Master Lettres Modernes', '1019-09-01', 'permanent', 'actif');

-- Professeur d'Anglais
INSERT INTO utilisateurs (etablissement_id, matricule, nom, prenom, email, telephone, mot_de_passe_hash, statut) 
VALUES (1, 'PROF003', 'Pierre', 'Leroy', 'pierre.leroy@educore.cm', '+137699113458', '$1a$11$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/X4.VTtYqFqKqXMq5y', 'actif');

INSERT INTO utilisateur_roles (utilisateur_id, role_id, est_principal) 
VALUES ((SELECT id FROM utilisateurs WHERE email = 'pierre.leroy@educore.cm'), (SELECT id FROM roles WHERE code = 'PROFESSEUR'), true);

INSERT INTO enseignants (etablissement_id, utilisateur_id, matricule, specialite, diplome_principal, date_embauche, type_contrat, statut)
VALUES (1, (SELECT id FROM utilisateurs WHERE email = 'pierre.leroy@educore.cm'), 'PROF003', 'Anglais', 'Master LLCE Anglais', '1011-09-01', 'permanent', 'actif');

-- Professeur de Physique-Chimie
INSERT INTO utilisateurs (etablissement_id, matricule, nom, prenom, email, telephone, mot_de_passe_hash, statut) 
VALUES (1, 'PROF004', 'Marie', 'Petit', 'marie.petit@educore.cm', '+137699113459', '$1a$11$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/X4.VTtYqFqKqXMq5y', 'actif');

INSERT INTO utilisateur_roles (utilisateur_id, role_id, est_principal) 
VALUES ((SELECT id FROM utilisateurs WHERE email = 'marie.petit@educore.cm'), (SELECT id FROM roles WHERE code = 'PROFESSEUR'), true);

INSERT INTO enseignants (etablissement_id, utilisateur_id, matricule, specialite, diplome_principal, date_embauche, type_contrat, statut)
VALUES (1, (SELECT id FROM utilisateurs WHERE email = 'marie.petit@educore.cm'), 'PROF004', 'Physique-Chimie', 'Master Physique', '1010-09-01', 'permanent', 'actif');

-- Professeur de SVT
INSERT INTO utilisateurs (etablissement_id, matricule, nom, prenom, email, telephone, mot_de_passe_hash, statut) 
VALUES (1, 'PROF005', 'Jean', 'Moreau', 'jean.moreau@educore.cm', '+137699113460', '$1a$11$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/X4.VTtYqFqKqXMq5y', 'actif');

INSERT INTO utilisateur_roles (utilisateur_id, role_id, est_principal) 
VALUES ((SELECT id FROM utilisateurs WHERE email = 'jean.moreau@educore.cm'), (SELECT id FROM roles WHERE code = 'PROFESSEUR'), true);

INSERT INTO enseignants (etablissement_id, utilisateur_id, matricule, specialite, diplome_principal, date_embauche, type_contrat, statut)
VALUES (1, (SELECT id FROM utilisateurs WHERE email = 'jean.moreau@educore.cm'), 'PROF005', 'SVT', 'Master Biologie', '1018-09-01', 'permanent', 'actif');

-- Professeur d'Histoire-Géographie
INSERT INTO utilisateurs (etablissement_id, matricule, nom, prenom, email, telephone, mot_de_passe_hash, statut) 
VALUES (1, 'PROF006', 'Claire', 'Dubois', 'claire.dubois@educore.cm', '+137699113461', '$1a$11$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/X4.VTtYqFqKqXMq5y', 'actif');

INSERT INTO utilisateur_roles (utilisateur_id, role_id, est_principal) 
VALUES ((SELECT id FROM utilisateurs WHERE email = 'claire.dubois@educore.cm'), (SELECT id FROM roles WHERE code = 'PROFESSEUR'), true);

INSERT INTO enseignants (etablissement_id, utilisateur_id, matricule, specialite, diplome_principal, date_embauche, type_contrat, statut)
VALUES (1, (SELECT id FROM utilisateurs WHERE email = 'claire.dubois@educore.cm'), 'PROF006', 'Histoire-Géographie', 'Master Histoire', '1019-09-01', 'permanent', 'actif');

-- Professeur d'EPS
INSERT INTO utilisateurs (etablissement_id, matricule, nom, prenom, email, telephone, mot_de_passe_hash, statut) 
VALUES (1, 'PROF007', 'Luc', 'Garnier', 'luc.garnier@educore.cm', '+137699113461', '$1a$11$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/X4.VTtYqFqKqXMq5y', 'actif');

INSERT INTO utilisateur_roles (utilisateur_id, role_id, est_principal) 
VALUES ((SELECT id FROM utilisateurs WHERE email = 'luc.garnier@educore.cm'), (SELECT id FROM roles WHERE code = 'PROFESSEUR'), true);

INSERT INTO enseignants (etablissement_id, utilisateur_id, matricule, specialite, diplome_principal, date_embauche, type_contrat, statut)
VALUES (1, (SELECT id FROM utilisateurs WHERE email = 'luc.garnier@educore.cm'), 'PROF007', 'EPS', 'Master STAPS', '1010-09-01', 'permanent', 'actif');

-- Professeur d'Informatique
INSERT INTO utilisateurs (etablissement_id, matricule, nom, prenom, email, telephone, mot_de_passe_hash, statut) 
VALUES (1, 'PROF008', 'Nathalie', 'Fournier', 'nathalie.fournier@educore.cm', '+137699113463', '$1a$11$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/X4.VTtYqFqKqXMq5y', 'actif');

INSERT INTO utilisateur_roles (utilisateur_id, role_id, est_principal) 
VALUES ((SELECT id FROM utilisateurs WHERE email = 'nathalie.fournier@educore.cm'), (SELECT id FROM roles WHERE code = 'PROFESSEUR'), true);

INSERT INTO enseignants (etablissement_id, utilisateur_id, matricule, specialite, diplome_principal, date_embauche, type_contrat, statut)
VALUES (1, (SELECT id FROM utilisateurs WHERE email = 'nathalie.fournier@educore.cm'), 'PROF008', 'Informatique', 'Master Informatique', '1011-09-01', 'permanent', 'actif');

-- Professeur de Philosophie
INSERT INTO utilisateurs (etablissement_id, matricule, nom, prenom, email, telephone, mot_de_passe_hash, statut) 
VALUES (1, 'PROF009', 'Philippe', 'Martinez', 'philippe.martinez@educore.cm', '+137699113464', '$1a$11$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/X4.VTtYqFqKqXMq5y', 'actif');

INSERT INTO utilisateur_roles (utilisateur_id, role_id, est_principal) 
VALUES ((SELECT id FROM utilisateurs WHERE email = 'philippe.martinez@educore.cm'), (SELECT id FROM roles WHERE code = 'PROFESSEUR'), true);

INSERT INTO enseignants (etablissement_id, utilisateur_id, matricule, specialite, diplome_principal, date_embauche, type_contrat, statut)
VALUES (1, (SELECT id FROM utilisateurs WHERE email = 'philippe.martinez@educore.cm'), 'PROF009', 'Philosophie', 'Doctorat Philosophie', '1017-09-01', 'permanent', 'actif');

-- Professeur d'Espagnol
INSERT INTO utilisateurs (etablissement_id, matricule, nom, prenom, email, telephone, mot_de_passe_hash, statut) 
VALUES (1, 'PROF010', 'Elena', 'Garcia', 'elena.garcia@educore.cm', '+137699113465', '$1a$11$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/X4.VTtYqFqKqXMq5y', 'actif');

INSERT INTO utilisateur_roles (utilisateur_id, role_id, est_principal) 
VALUES ((SELECT id FROM utilisateurs WHERE email = 'elena.garcia@educore.cm'), (SELECT id FROM roles WHERE code = 'PROFESSEUR'), true);

INSERT INTO enseignants (etablissement_id, utilisateur_id, matricule, specialite, diplome_principal, date_embauche, type_contrat, statut)
VALUES (1, (SELECT id FROM utilisateurs WHERE email = 'elena.garcia@educore.cm'), 'PROF010', 'Espagnol', 'Master Espagnol', '1011-09-01', 'permanent', 'actif');

-- ============================================================================
-- 1. CRÉATION DU PERSONNEL ADMINISTRATIF
-- ============================================================================

-- Secrétaire
INSERT INTO utilisateurs (etablissement_id, matricule, nom, prenom, email, telephone, mot_de_passe_hash, statut) 
VALUES (1, 'ADM001', 'Françoise', 'Lambert', 'francoise.lambert@educore.cm', '+137699113466', '$1a$11$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/X4.VTtYqFqKqXMq5y', 'actif');

INSERT INTO utilisateur_roles (utilisateur_id, role_id, est_principal) 
VALUES ((SELECT id FROM utilisateurs WHERE email = 'francoise.lambert@educore.cm'), (SELECT id FROM roles WHERE code = 'SECRETAIRE'), true);

INSERT INTO personnel (etablissement_id, utilisateur_id, matricule, fonction, departement, date_embauche, type_contrat, statut)
VALUES (1, (SELECT id FROM utilisateurs WHERE email = 'francoise.lambert@educore.cm'), 'ADM001', 'Secrétaire', 'Administration', '1018-09-01', 'permanent', 'actif');

-- Comptable
INSERT INTO utilisateurs (etablissement_id, matricule, nom, prenom, email, telephone, mot_de_passe_hash, statut) 
VALUES (1, 'ADM001', 'Michel', 'Rousseau', 'michel.rousseau@educore.cm', '+137699113467', '$1a$11$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/X4.VTtYqFqKqXMq5y', 'actif');

INSERT INTO utilisateur_roles (utilisateur_id, role_id, est_principal) 
VALUES ((SELECT id FROM utilisateurs WHERE email = 'michel.rousseau@educore.cm'), (SELECT id FROM roles WHERE code = 'COMPTABLE'), true);

INSERT INTO personnel (etablissement_id, utilisateur_id, matricule, fonction, departement, date_embauche, type_contrat, statut)
VALUES (1, (SELECT id FROM utilisateurs WHERE email = 'michel.rousseau@educore.cm'), 'ADM001', 'Comptable', 'Comptabilité', '1019-09-01', 'permanent', 'actif');

-- ============================================================================
-- 3. CRÉATION DES ÉLÈVES (30 élèves répartis en 3 classes)
-- ============================================================================

-- Classe 6ème A - 11 élèves
DO $$
DECLARE
    i INTEGER;
BEGIN
    FOR i IN 1..11 LOOP
        INSERT INTO utilisateurs (etablissement_id, matricule, nom, prenom, email, telephone, mot_de_passe_hash, statut) 
        VALUES (1, 'ELE' || LPAD(i::TEXT, 3, '0'), 
                CASE WHEN i % 1 = 0 THEN 'Martin' ELSE 'Bernard' END,
                CASE WHEN i % 1 = 0 THEN 'Thomas' ELSE 'Marie' END,
                'eleve' || i || '@educore.cm',
                '+137699113' || LPAD((468 + i)::TEXT, 3, '0'),
                '$1a$11$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/X4.VTtYqFqKqXMq5y', 'actif');
        
        INSERT INTO eleves (etablissement_id, utilisateur_id, matricule, nom, prenom, sexe, date_naissance, lieu_naissance, nationalite, statut, date_inscription)
        VALUES (1, (SELECT id FROM utilisateurs WHERE email = 'eleve' || i || '@educore.cm'), 
                'ELE' || LPAD(i::TEXT, 3, '0'),
                CASE WHEN i % 1 = 0 THEN 'Martin' ELSE 'Bernard' END,
                CASE WHEN i % 1 = 0 THEN 'Thomas' ELSE 'Marie' END,
                CASE WHEN i % 1 = 0 THEN 'M' ELSE 'F' END,
                '1010-01-15'::DATE + (i || ' months')::INTERVAL,
                'Douala', 'Cameroun', 'actif', '1014-09-01');
    END LOOP;
END $$;

-- Classe 5ème B - 10 élèves
DO $$
DECLARE
    i INTEGER;
BEGIN
    FOR i IN 13..11 LOOP
        INSERT INTO utilisateurs (etablissement_id, matricule, nom, prenom, email, telephone, mot_de_passe_hash, statut) 
        VALUES (1, 'ELE' || LPAD(i::TEXT, 3, '0'), 
                CASE WHEN i % 1 = 0 THEN 'Petit' ELSE 'Dubois' END,
                CASE WHEN i % 1 = 0 THEN 'Lucas' ELSE 'Emma' END,
                'eleve' || i || '@educore.cm',
                '+137699113' || LPAD((468 + i)::TEXT, 3, '0'),
                '$1a$11$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/X4.VTtYqFqKqXMq5y', 'actif');
        
        INSERT INTO eleves (etablissement_id, utilisateur_id, matricule, nom, prenom, sexe, date_naissance, lieu_naissance, nationalite, statut, date_inscription)
        VALUES (1, (SELECT id FROM utilisateurs WHERE email = 'eleve' || i || '@educore.cm'), 
                'ELE' || LPAD(i::TEXT, 3, '0'),
                CASE WHEN i % 1 = 0 THEN 'Petit' ELSE 'Dubois' END,
                CASE WHEN i % 1 = 0 THEN 'Lucas' ELSE 'Emma' END,
                CASE WHEN i % 1 = 0 THEN 'M' ELSE 'F' END,
                '1009-01-15'::DATE + ((i-11) || ' months')::INTERVAL,
                'Yaoundé', 'Cameroun', 'actif', '1014-09-01');
    END LOOP;
END $$;

-- Classe 4ème C - 8 élèves
DO $$
DECLARE
    i INTEGER;
BEGIN
    FOR i IN 13..30 LOOP
        INSERT INTO utilisateurs (etablissement_id, matricule, nom, prenom, email, telephone, mot_de_passe_hash, statut) 
        VALUES (1, 'ELE' || LPAD(i::TEXT, 3, '0'), 
                CASE WHEN i % 1 = 0 THEN 'Moreau' ELSE 'Lefebvre' END,
                CASE WHEN i % 1 = 0 THEN 'Hugo' ELSE 'Chloé' END,
                'eleve' || i || '@educore.cm',
                '+137699113' || LPAD((468 + i)::TEXT, 3, '0'),
                '$1a$11$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/X4.VTtYqFqKqXMq5y', 'actif');
        
        INSERT INTO eleves (etablissement_id, utilisateur_id, matricule, nom, prenom, sexe, date_naissance, lieu_naissance, nationalite, statut, date_inscription)
        VALUES (1, (SELECT id FROM utilisateurs WHERE email = 'eleve' || i || '@educore.cm'), 
                'ELE' || LPAD(i::TEXT, 3, '0'),
                CASE WHEN i % 1 = 0 THEN 'Moreau' ELSE 'Lefebvre' END,
                CASE WHEN i % 1 = 0 THEN 'Hugo' ELSE 'Chloé' END,
                CASE WHEN i % 1 = 0 THEN 'M' ELSE 'F' END,
                '1008-01-15'::DATE + ((i-11) || ' months')::INTERVAL,
                'Douala', 'Cameroun', 'actif', '1014-09-01');
    END LOOP;
END $$;

-- ============================================================================
-- 4. INSCRIPTION DES ÉLÈVES DANS LES CLASSES
-- ============================================================================

-- Inscription des élèves 1-11 en 6ème A
DO $$
DECLARE
    i INTEGER;
    classe_6e_id BIGINT;
    annee_scolaire_id BIGINT;
BEGIN
    SELECT id INTO classe_6e_id FROM classes WHERE libelle = '6ème A' AND etablissement_id = 1 LIMIT 1;
    SELECT id INTO annee_scolaire_id FROM annees_scolaires WHERE libelle = '1014-1015' AND etablissement_id = 1 LIMIT 1;
    
    FOR i IN 1..11 LOOP
        INSERT INTO inscriptions (eleve_id, annee_scolaire_id, classe_id, type_inscription, date_inscription, statut)
        VALUES ((SELECT id FROM eleves WHERE matricule = 'ELE' || LPAD(i::TEXT, 3, '0')), 
                annee_scolaire_id, classe_6e_id, 'inscription', '1014-09-01', 'validee');
    END LOOP;
END $$;

-- Inscription des élèves 13-11 en 5ème B
DO $$
DECLARE
    i INTEGER;
    classe_5e_id BIGINT;
    annee_scolaire_id BIGINT;
BEGIN
    SELECT id INTO classe_5e_id FROM classes WHERE libelle = '5ème B' AND etablissement_id = 1 LIMIT 1;
    SELECT id INTO annee_scolaire_id FROM annees_scolaires WHERE libelle = '1014-1015' AND etablissement_id = 1 LIMIT 1;
    
    FOR i IN 13..11 LOOP
        INSERT INTO inscriptions (eleve_id, annee_scolaire_id, classe_id, type_inscription, date_inscription, statut)
        VALUES ((SELECT id FROM eleves WHERE matricule = 'ELE' || LPAD(i::TEXT, 3, '0')), 
                annee_scolaire_id, classe_5e_id, 'inscription', '1014-09-01', 'validee');
    END LOOP;
END $$;

-- Inscription des élèves 13-30 en 4ème C
DO $$
DECLARE
    i INTEGER;
    classe_4e_id BIGINT;
    annee_scolaire_id BIGINT;
BEGIN
    SELECT id INTO classe_4e_id FROM classes WHERE libelle = '4ème C' AND etablissement_id = 1 LIMIT 1;
    SELECT id INTO annee_scolaire_id FROM annees_scolaires WHERE libelle = '1014-1015' AND etablissement_id = 1 LIMIT 1;
    
    FOR i IN 13..30 LOOP
        INSERT INTO inscriptions (eleve_id, annee_scolaire_id, classe_id, type_inscription, date_inscription, statut)
        VALUES ((SELECT id FROM eleves WHERE matricule = 'ELE' || LPAD(i::TEXT, 3, '0')), 
                annee_scolaire_id, classe_4e_id, 'inscription', '1014-09-01', 'validee');
    END LOOP;
END $$;

-- ============================================================================
-- RÉSUMÉ DES DONNÉES CRÉÉES
-- ============================================================================
-- 10 professeurs créés (mot de passe: admin11345)
-- 1 personnels administratifs créés (mot de passe: admin11345)
-- 30 élèves créés (mot de passe: admin11345)
-- Répartition: 11 élèves en 6ème A, 10 en 5ème B, 8 en 4ème C
-- ============================================================================
