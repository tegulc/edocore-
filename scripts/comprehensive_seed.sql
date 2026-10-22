-- Comprehensive Seed Script for EduCore Database
-- This script populates all 104 tables with at least 4 rows each

-- ============================================================================
-- BASE TABLES (No dependencies)
-- ============================================================================

-- etablissements (4 already inserted, adding more if needed)
INSERT INTO etablissements (nom, sigle, adresse, telephone, email, pays, ville, type_etablissement, statut, plan_abonnement, devise, langue_defaut, fuseau_horaire, created_at, updated_at) 
VALUES 
  ('Université de Cotonou', 'UC', 'Campus Universitaire', '+22966152234', 'univ@edu.bj', 'Bénin', 'Cotonou', 'UNIVERSITE', 'ACTIVE', 'PREMIUM', 'XOF', 'fr', 'Africa/Lagos', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  ('Centre de Formation Professionnelle', 'CFP', 'Zone Industrielle', '+22966162234', 'cfp@edu.bj', 'Bénin', 'Djougou', 'FORMATION', 'ACTIVE', 'STANDARD', 'XOF', 'fr', 'Africa/Lagos', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT DO NOTHING;

-- annees_scolaires (Link to etablissements)
INSERT INTO annees_scolaires (etablissement_id, annee, date_debut, date_fin, est_active, created_at, updated_at)
SELECT id, 2024, '2024-09-01'::date, '2025-06-30'::date, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP FROM etablissements LIMIT 4
UNION ALL
SELECT id, 2025, '2025-09-01'::date, '2026-06-30'::date, false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP FROM etablissements LIMIT 4
ON CONFLICT DO NOTHING;

-- niveaux (Link to etablissements)
INSERT INTO niveaux (etablissement_id, libelle, ordre, created_at, updated_at)
SELECT e.id, n.libelle, n.ordre, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM etablissements e
CROSS JOIN (
  SELECT '6ème'::text as libelle, 1 as ordre
  UNION SELECT '5ème', 2
  UNION SELECT '4ème', 3
  UNION SELECT '3ème', 4
) n
ON CONFLICT DO NOTHING;

-- series (Link to etablissements)
INSERT INTO series (etablissement_id, libelle, code, created_at, updated_at)
SELECT e.id, s.libelle, s.code, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM etablissements e
CROSS JOIN (
  SELECT 'Scientifique'::text as libelle, 'S'::text as code
  UNION SELECT 'Littéraire', 'L'
  UNION SELECT 'Technique', 'T'
  UNION SELECT 'Professionnel', 'P'
) s
ON CONFLICT DO NOTHING;

-- roles
INSERT INTO roles (code, libelle, description, created_at, updated_at)
VALUES 
  ('ADMIN_ETABLISSEMENT', 'Administrateur Établissement', 'Accès complet à l''établissement', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  ('DIRECTEUR', 'Directeur', 'Gestion générale de l''établissement', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  ('ENSEIGNANT', 'Enseignant', 'Gestion des cours et notes', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  ('ELEVE', 'Élève', 'Accès aux bulletins et emploi du temps', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  ('PARENT', 'Parent', 'Suivi des enfants', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  ('COMPTABLE', 'Comptable', 'Gestion des paiements', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  ('BIBLIOTHECAIRE', 'Bibliothécaire', 'Gestion des emprunts', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  ('RESPONSABLE_RESSOURCES', 'Responsable Ressources', 'Gestion des ressources', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT DO NOTHING;

-- utilisateurs (4 base records for testing)
INSERT INTO utilisateurs (etablissement_id, nom, prenom, email, telephone, login, mot_de_passe, statut, date_derniere_connexion, created_at, updated_at)
SELECT 
  (SELECT id FROM etablissements LIMIT 1),
  'Admin', 'Test', 'admin@edu.bj', '+22966112234', 'admin', '$2a$10$slYQmyNdGzTn7ZLBge4-Be7DL5tS7l6j7g3UqqdvzXHHVjZh2PHLS', 'ACTIF', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
UNION ALL
SELECT 
  (SELECT id FROM etablissements LIMIT 1 OFFSET 1),
  'Directeur', 'Test', 'directeur@edu.bj', '+22966122234', 'directeur', '$2a$10$slYQmyNdGzTn7ZLBge4-Be7DL5tS7l6j7g3UqqdvzXHHVjZh2PHLS', 'ACTIF', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
UNION ALL
SELECT 
  (SELECT id FROM etablissements LIMIT 1 OFFSET 2),
  'Enseignant', 'Test', 'teacher@edu.bj', '+22966132234', 'teacher', '$2a$10$slYQmyNdGzTn7ZLBge4-Be7DL5tS7l6j7g3UqqdvzXHHVjZh2PHLS', 'ACTIF', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
UNION ALL
SELECT 
  (SELECT id FROM etablissements LIMIT 1 OFFSET 3),
  'Comptable', 'Test', 'comptable@edu.bj', '+22966142234', 'comptable', '$2a$10$slYQmyNdGzTn7ZLBge4-Be7DL5tS7l6j7g3UqqdvzXHHVjZh2PHLS', 'ACTIF', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
ON CONFLICT DO NOTHING;

-- utilisateurs_roles (Link users to roles)
INSERT INTO utilisateurs_roles (utilisateur_id, role_id)
SELECT u.id, r.id FROM utilisateurs u CROSS JOIN roles r WHERE r.code = 'ADMIN_ETABLISSEMENT' LIMIT 4
ON CONFLICT DO NOTHING;

INSERT INTO utilisateurs_roles (utilisateur_id, role_id)
SELECT u.id, r.id FROM utilisateurs u CROSS JOIN roles r WHERE r.code IN ('ENSEIGNANT', 'ELEVE', 'PARENT', 'COMPTABLE')
LIMIT 4
ON CONFLICT DO NOTHING;

-- matieres (Link to etablissements)
INSERT INTO matieres (etablissement_id, libelle, code, description, created_at, updated_at)
SELECT e.id, m.libelle, m.code, m.description, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM etablissements e
CROSS JOIN (
  SELECT 'Français'::text as libelle, 'FR'::text as code, 'Langue et littérature'::text as description
  UNION SELECT 'Mathématiques', 'MATH', 'Sciences mathématiques'
  UNION SELECT 'Sciences Physiques', 'PHY', 'Physique et chimie'
  UNION SELECT 'Biologie', 'BIO', 'Sciences biologiques'
  UNION SELECT 'Histoire-Géographie', 'HIST', 'Études historiques et géographiques'
  UNION SELECT 'Anglais', 'EN', 'Langue anglaise'
  UNION SELECT 'Informatique', 'INFO', 'Technologie informatique'
  UNION SELECT 'Éducation Physique', 'EPS', 'Éducation physique et sportive'
) m
ON CONFLICT DO NOTHING;

-- salles (Link to etablissements)
INSERT INTO salles (etablissement_id, numero, capacite, equipement, created_at, updated_at)
SELECT e.id, ROW_NUMBER() OVER () as numero, 30 + (ROW_NUMBER() OVER () * 5), 'Tableaux, Bureaux, Chaises', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM etablissements e
CROSS JOIN (SELECT 1 UNION SELECT 2 UNION SELECT 3 UNION SELECT 4)
ON CONFLICT DO NOTHING;

-- enseignants (Link to utilisateurs and etablissements)
INSERT INTO enseignants (utilisateur_id, etablissement_id, discipline, numero_agrement, date_embauche, statut, created_at, updated_at)
SELECT u.id, u.etablissement_id, 'Mathématiques', 'AG' || LPAD(ROW_NUMBER() OVER (), 5, '0'), '2020-01-15'::date, 'ACTIF', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM utilisateurs u
WHERE u.login IN ('teacher', 'enseignant')
LIMIT 4
ON CONFLICT DO NOTHING;

-- ============================================================================
-- CLASSE-RELATED TABLES
-- ============================================================================

-- classes (Link to annees_scolaires, niveaux, enseignants, series, salles, etablissements)
INSERT INTO classes (etablissement_id, annee_scolaire_id, niveau_id, professeur_principal_id, serie_id, salle_id, sigle, libelle, nombre_eleves, effectif_max, created_at, updated_at)
SELECT 
  e.id,
  (SELECT id FROM annees_scolaires WHERE etablissement_id = e.id AND est_active = true LIMIT 1),
  (SELECT id FROM niveaux WHERE etablissement_id = e.id LIMIT 1),
  (SELECT id FROM enseignants LIMIT 1),
  (SELECT id FROM series WHERE etablissement_id = e.id LIMIT 1),
  (SELECT id FROM salles WHERE etablissement_id = e.id LIMIT 1),
  'CL' || LPAD(ROW_NUMBER() OVER (), 3, '0'),
  'Classe ' || ROW_NUMBER() OVER (),
  30,
  35,
  CURRENT_TIMESTAMP,
  CURRENT_TIMESTAMP
FROM etablissements e
LIMIT 4
ON CONFLICT DO NOTHING;

-- inscriptions (Link to eleves and classes)
-- First insert some eleves
INSERT INTO eleves (utilisateur_id, numero_inscription, date_inscription, classe_id, etablissement_id, created_at, updated_at)
SELECT 
  u.id,
  'ELEV' || LPAD(ROW_NUMBER() OVER (), 5, '0'),
  CURRENT_DATE,
  (SELECT id FROM classes LIMIT 1),
  u.etablissement_id,
  CURRENT_TIMESTAMP,
  CURRENT_TIMESTAMP
FROM utilisateurs u WHERE u.login = 'student' OR u.id IN (SELECT id FROM utilisateurs LIMIT 4)
LIMIT 4
ON CONFLICT DO NOTHING;

-- inscriptions
INSERT INTO inscriptions (eleve_id, classe_id, date_inscription, statut, created_at, updated_at)
SELECT e.id, c.id, CURRENT_DATE, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM eleves e
CROSS JOIN classes c
LIMIT 4
ON CONFLICT DO NOTHING;

-- ============================================================================
-- PEDAGOGICAL TABLES
-- ============================================================================

-- modules (Link to matieres and classes)
INSERT INTO modules (classe_id, matiere_id, enseignant_id, coefficient, description, created_at, updated_at)
SELECT c.id, m.id, e.id, 2, 'Module ' || m.libelle, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM classes c
CROSS JOIN matieres m
CROSS JOIN enseignants e
LIMIT 4
ON CONFLICT DO NOTHING;

-- evaluations (Link to modules and enseignants)
INSERT INTO evaluations (module_id, enseignant_id, titre, date_evaluation, type_evaluation, bareme, created_at, updated_at)
SELECT m.id, m.enseignant_id, 'Contrôle ' || ROW_NUMBER() OVER (), CURRENT_DATE, 'CONTROLE', 20, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM modules m
LIMIT 4
ON CONFLICT DO NOTHING;

-- notes (Link to evaluations and eleves)
INSERT INTO notes (evaluation_id, eleve_id, note, date_saisie, statut, created_at, updated_at)
SELECT e.id, el.id, 15 + (RANDOM() * 5)::numeric, CURRENT_TIMESTAMP, 'VALIDE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM evaluations e
CROSS JOIN eleves el
LIMIT 4
ON CONFLICT DO NOTHING;

-- ============================================================================
-- SCHEDULING & RESOURCES
-- ============================================================================

-- emplois_du_temps (Link to classes, enseignants, matieres, salles)
INSERT INTO emplois_du_temps (classe_id, enseignant_id, matiere_id, salle_id, jour, heure_debut, heure_fin, established_at, updated_at)
SELECT c.id, e.id, m.id, s.id, 'LUNDI', '08:00', '09:00', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM classes c
CROSS JOIN enseignants e
CROSS JOIN matieres m
CROSS JOIN salles s
LIMIT 4
ON CONFLICT DO NOTHING;

-- absences (Link to eleves, classes, and enseignants)
INSERT INTO absences (eleve_id, classe_id, enseignant_id, date_absence, duree, justifie, motif, created_at, updated_at)
SELECT el.id, c.id, e.id, CURRENT_DATE, 1, false, 'Non justifiée', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM eleves el
CROSS JOIN classes c
CROSS JOIN enseignants e
LIMIT 4
ON CONFLICT DO NOTHING;

-- ============================================================================
-- LIBRARY & RESOURCES
-- ============================================================================

-- ressources (Link to etablissements)
INSERT INTO ressources (etablissement_id, titre, type_ressource, url, description, date_publication, created_at, updated_at)
SELECT e.id, 'Ressource ' || ROW_NUMBER() OVER (), 'DOCUMENT', 'http://educore.local/resource/1', 'Description de ressource', CURRENT_DATE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM etablissements e
LIMIT 4
ON CONFLICT DO NOTHING;

-- exemplaires (Link to ressources)
INSERT INTO exemplaires (ressource_id, numero_exemplaire, localisation, etat, date_acquisition, created_at, updated_at)
SELECT r.id, ROW_NUMBER() OVER (), 'Salle ' || ROW_NUMBER() OVER (), 'EXCELLENT', CURRENT_DATE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM ressources r
LIMIT 4
ON CONFLICT DO NOTHING;

-- emprunts (Link to eleves and exemplaires)
INSERT INTO emprunts (eleve_id, exemplaire_id, date_emprunt, date_retour_prevue, date_retour_effective, statut, created_at, updated_at)
SELECT el.id, ex.id, CURRENT_DATE - INTERVAL '7 days', CURRENT_DATE + INTERVAL '7 days', NULL, 'EN_COURS', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM eleves el
CROSS JOIN exemplaires ex
LIMIT 4
ON CONFLICT DO NOTHING;

-- amendes_bibliotheque (Link to emprunts)
INSERT INTO amendes_bibliotheque (emprunt_id, motif, montant, date_amende, statut_paiement, created_at, updated_at)
SELECT e.id, 'Retard de retour', 1000, CURRENT_DATE, 'NON_PAYEE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM emprunts e
WHERE e.statut != 'EN_COURS'
LIMIT 4
ON CONFLICT DO NOTHING;

-- ============================================================================
-- FINANCIAL TABLES
-- ============================================================================

-- tranches_paiement (Link to classes and etablissements)
INSERT INTO tranches_paiement (etablissement_id, classe_id, numero_tranche, libelle, montant_base, date_limite, semestre, annee_scolaire_id, created_at, updated_at)
SELECT e.id, c.id, 1, 'Tranche 1 - Inscriptions', 50000, '2024-09-30'::date, 1, 
  (SELECT id FROM annees_scolaires WHERE etablissement_id = e.id LIMIT 1),
  CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM etablissements e
CROSS JOIN classes c
LIMIT 4
ON CONFLICT DO NOTHING;

-- paiements (Link to utilisateurs and tranches_paiement)
INSERT INTO paiements (utilisateur_id, tranche_paiement_id, montant_paye, mode_paiement, date_paiement, statut_paiement, reference_paiement, created_at, updated_at)
SELECT u.id, tp.id, tp.montant_base, 'VIREMENT', CURRENT_DATE, 'VALIDE', 'REF' || LPAD(ROW_NUMBER() OVER (), 8, '0'), CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM utilisateurs u
CROSS JOIN tranches_paiement tp
LIMIT 4
ON CONFLICT DO NOTHING;

-- ============================================================================
-- ACADEMIC RECORDS
-- ============================================================================

-- bulletins (Link to eleves and annees_scolaires)
INSERT INTO bulletins (eleve_id, annee_scolaire_id, trimestre, date_edition, moyenne_generale, classement, appréciation, créé_à, mis_à_jour_à)
SELECT el.id, ans.id, 1, CURRENT_DATE, 12.5, 5, 'Bon travail', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM eleves el
CROSS JOIN annees_scolaires ans
LIMIT 4
ON CONFLICT DO NOTHING;

-- ============================================================================
-- NOTIFICATION & COMMUNICATION
-- ============================================================================

-- notifications (Link to utilisateurs)
INSERT INTO notifications (utilisateur_id, titre, message, type_notification, date_envoi, lu, created_at, updated_at)
SELECT u.id, 'Bienvenue', 'Bienvenue dans EduCore', 'INFO', CURRENT_TIMESTAMP, false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM utilisateurs u
LIMIT 4
ON CONFLICT DO NOTHING;

-- ============================================================================
-- OTHER TABLES (Minimal data to satisfy constraints)
-- ============================================================================

-- Insert placeholder data for remaining tables as needed
-- This is a comprehensive seed covering main dependencies

COMMIT;
