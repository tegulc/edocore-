-- Quick Final Seed - Insert minimum data into key tables

-- Ensure we have 4+ Etablissements
INSERT INTO etablissements (nom, sigle, pays, statut, devise, fuseau_horaire, created_at, updated_at)
VALUES 
  ('École Alpha', 'EA', 'Bénin', 'actif', 'XOF', 'Africa/Douala', now(), now()),
  ('Collège Beta', 'CB', 'Bénin', 'actif', 'XOF', 'Africa/Douala', now(), now()),
  ('Lycée Gamma', 'LG', 'Bénin', 'actif', 'XOF', 'Africa/Douala', now(), now()),
  ('Institut Delta', 'ID', 'Bénin', 'actif', 'XOF', 'Africa/Douala', now(), now())
ON CONFLICT DO NOTHING;

-- Ensure we have 4+ Roles
INSERT INTO roles (code, libelle, est_systeme)
VALUES 
  ('ADMIN', 'Administrateur', true),
  ('TEACHER', 'Enseignant', true),
  ('STUDENT', 'Élève', true),
  ('PARENT', 'Parent', true)
ON CONFLICT DO NOTHING;

-- Ensure we have 4+ Utilisateurs
INSERT INTO utilisateurs (etablissement_id, nom, prenom, email, mot_de_passe_hash, statut, created_at, updated_at)
SELECT DISTINCT e.id, 'User' || row_number() OVER (), 'Test', 'user' || row_number() OVER () || '@test.com', crypt('password', gen_salt('bf')), 'actif', now(), now()
FROM etablissements e
CROSS JOIN (SELECT 1 UNION SELECT 2 UNION SELECT 3 UNION SELECT 4)
LIMIT 16
ON CONFLICT DO NOTHING;

-- Ensure we have 4+ Annees Scolaires
INSERT INTO annees_scolaires (etablissement_id, libelle, date_debut, date_fin, est_active, created_at)
SELECT DISTINCT e.id, '2024-2025', '2024-09-01'::date, '2025-06-30'::date, true, now()
FROM etablissements e
LIMIT 4
ON CONFLICT DO NOTHING;

-- Ensure we have 4+ Niveaux
INSERT INTO niveaux (etablissement_id, code, libelle, ordre)
SELECT DISTINCT e.id, 'L1', 'Niveau 1', 1
FROM etablissements e
UNION ALL
SELECT DISTINCT e.id, 'L2', 'Niveau 2', 2
FROM etablissements e
UNION ALL
SELECT DISTINCT e.id, 'L3', 'Niveau 3', 3
FROM etablissements e
UNION ALL
SELECT DISTINCT e.id, 'L4', 'Niveau 4', 4
FROM etablissements e
LIMIT 16
ON CONFLICT DO NOTHING;

-- Ensure we have 4+ Series
INSERT INTO series (etablissement_id, code, libelle)
SELECT DISTINCT e.id, 'S', 'Scientifique'
FROM etablissements e
UNION ALL
SELECT DISTINCT e.id, 'L', 'Littéraire'
FROM etablissements e
UNION ALL
SELECT DISTINCT e.id, 'T', 'Technique'
FROM etablissements e
UNION ALL
SELECT DISTINCT e.id, 'P', 'Professionnel'
FROM etablissements e
LIMIT 16
ON CONFLICT DO NOTHING;

-- Ensure we have 4+ Matieres
INSERT INTO matieres (etablissement_id, code, libelle)
SELECT DISTINCT e.id, 'FR', 'Français'
FROM etablissements e
UNION ALL
SELECT DISTINCT e.id, 'MATH', 'Mathématiques'
FROM etablissements e
UNION ALL
SELECT DISTINCT e.id, 'SC', 'Sciences'
FROM etablissements e
UNION ALL
SELECT DISTINCT e.id, 'EN', 'Anglais'
FROM etablissements e
LIMIT 16
ON CONFLICT DO NOTHING;

-- Ensure we have 4+ Salles
INSERT INTO salles (etablissement_id, nom, capacite)
SELECT DISTINCT e.id, 'Salle 101', 30
FROM etablissements e
UNION ALL
SELECT DISTINCT e.id, 'Salle 102', 35
FROM etablissements e
UNION ALL
SELECT DISTINCT e.id, 'Salle 103', 40
FROM etablissements e
UNION ALL
SELECT DISTINCT e.id, 'Salle 104', 25
FROM etablissements e
LIMIT 16
ON CONFLICT DO NOTHING;

-- Ensure we have 4+ Enseignants
INSERT INTO enseignants (etablissement_id, utilisateur_id, specialite, statut)
SELECT DISTINCT e.id, (SELECT id FROM utilisateurs WHERE etablissement_id = e.id LIMIT 1), 'Français', 'actif'
FROM etablissements e
UNION ALL
SELECT DISTINCT e.id, (SELECT id FROM utilisateurs WHERE etablissement_id = e.id LIMIT 1 OFFSET 1), 'Mathématiques', 'actif'
FROM etablissements e
UNION ALL
SELECT DISTINCT e.id, (SELECT id FROM utilisateurs WHERE etablissement_id = e.id LIMIT 1 OFFSET 2), 'Sciences', 'actif'
FROM etablissements e
UNION ALL
SELECT DISTINCT e.id, (SELECT id FROM utilisateurs WHERE etablissement_id = e.id LIMIT 1 OFFSET 3), 'Anglais', 'actif'
FROM etablissements e
LIMIT 16
ON CONFLICT DO NOTHING;

-- Ensure we have 4+ Classes
INSERT INTO classes (etablissement_id, annee_scolaire_id, niveau_id, professeur_principal_id, serie_id, salle_id, libelle, capacite)
SELECT DISTINCT e.id, 
  (SELECT id FROM annees_scolaires WHERE etablissement_id = e.id LIMIT 1),
  (SELECT id FROM niveaux WHERE etablissement_id = e.id LIMIT 1),
  (SELECT id FROM enseignants WHERE etablissement_id = e.id LIMIT 1),
  (SELECT id FROM series WHERE etablissement_id = e.id LIMIT 1),
  (SELECT id FROM salles WHERE etablissement_id = e.id LIMIT 1),
  'Classe 1', 30
FROM etablissements e
UNION ALL
SELECT DISTINCT e.id,
  (SELECT id FROM annees_scolaires WHERE etablissement_id = e.id LIMIT 1),
  (SELECT id FROM niveaux WHERE etablissement_id = e.id OFFSET 1 LIMIT 1),
  (SELECT id FROM enseignants WHERE etablissement_id = e.id OFFSET 1 LIMIT 1),
  (SELECT id FROM series WHERE etablissement_id = e.id OFFSET 1 LIMIT 1),
  (SELECT id FROM salles WHERE etablissement_id = e.id OFFSET 1 LIMIT 1),
  'Classe 2', 32
FROM etablissements e
UNION ALL
SELECT DISTINCT e.id,
  (SELECT id FROM annees_scolaires WHERE etablissement_id = e.id LIMIT 1),
  (SELECT id FROM niveaux WHERE etablissement_id = e.id OFFSET 2 LIMIT 1),
  (SELECT id FROM enseignants WHERE etablissement_id = e.id OFFSET 2 LIMIT 1),
  (SELECT id FROM series WHERE etablissement_id = e.id OFFSET 2 LIMIT 1),
  (SELECT id FROM salles WHERE etablissement_id = e.id OFFSET 2 LIMIT 1),
  'Classe 3', 28
FROM etablissements e
UNION ALL
SELECT DISTINCT e.id,
  (SELECT id FROM annees_scolaires WHERE etablissement_id = e.id LIMIT 1),
  (SELECT id FROM niveaux WHERE etablissement_id = e.id OFFSET 3 LIMIT 1),
  (SELECT id FROM enseignants WHERE etablissement_id = e.id OFFSET 3 LIMIT 1),
  (SELECT id FROM series WHERE etablissement_id = e.id OFFSET 3 LIMIT 1),
  (SELECT id FROM salles WHERE etablissement_id = e.id OFFSET 3 LIMIT 1),
  'Classe 4', 25
FROM etablissements e
LIMIT 16
ON CONFLICT DO NOTHING;

-- Ensure we have 4+ Eleves
INSERT INTO eleves (etablissement_id, utilisateur_id, matricule, nom, prenom, sexe, date_naissance, date_inscription, statut, created_at, updated_at)
SELECT e.id, (SELECT id FROM utilisateurs u WHERE u.etablissement_id = e.id LIMIT 1), 'EL0001', 'Élève', 'Un', 'M', '2010-01-01'::date, now()::date, 'actif', now(), now()
FROM etablissements e
UNION ALL
SELECT e.id, (SELECT id FROM utilisateurs u WHERE u.etablissement_id = e.id OFFSET 1 LIMIT 1), 'EL0002', 'Élève', 'Deux', 'F', '2010-02-01'::date, now()::date, 'actif', now(), now()
FROM etablissements e
UNION ALL
SELECT e.id, (SELECT id FROM utilisateurs u WHERE u.etablissement_id = e.id OFFSET 2 LIMIT 1), 'EL0003', 'Élève', 'Trois', 'M', '2010-03-01'::date, now()::date, 'actif', now(), now()
FROM etablissements e
UNION ALL
SELECT e.id, (SELECT id FROM utilisateurs u WHERE u.etablissement_id = e.id OFFSET 3 LIMIT 1), 'EL0004', 'Élève', 'Quatre', 'F', '2010-04-01'::date, now()::date, 'actif', now(), now()
FROM etablissements e
LIMIT 16
ON CONFLICT DO NOTHING;

-- Ensure we have 4+ Inscriptions
INSERT INTO inscriptions (eleve_id, annee_scolaire_id, classe_id, type_inscription, date_inscription, statut)
SELECT e.id, (SELECT id FROM annees_scolaires LIMIT 1), (SELECT id FROM classes LIMIT 1), 'inscription', now()::date, 'validee'
FROM eleves e
LIMIT 16
ON CONFLICT DO NOTHING;

-- Ensure we have 4+ Types Evaluation
INSERT INTO types_evaluation (code, libelle, ponderation)
VALUES 
  ('DEVOIR', 'Devoir', 0.5),
  ('SEQUENCE', 'Séquence', 1.0),
  ('COMPOSITION', 'Composition', 2.0),
  ('EXAMEN', 'Examen', 1.5)
ON CONFLICT DO NOTHING;

-- Ensure we have 4+ Periodes
INSERT INTO periodes (annee_scolaire_id, type_periode, numero, libelle, date_debut, date_fin)
SELECT id, 'trimestre', 1, 'Trimestre 1', '2024-09-01'::date, '2024-11-30'::date
FROM annees_scolaires
UNION ALL
SELECT id, 'trimestre', 2, 'Trimestre 2', '2024-12-01'::date, '2025-02-28'::date
FROM annees_scolaires
UNION ALL
SELECT id, 'trimestre', 3, 'Trimestre 3', '2025-03-01'::date, '2025-06-30'::date
FROM annees_scolaires
UNION ALL
SELECT id, 'sequence', 1, 'Séquence 1', '2024-09-01'::date, '2024-10-31'::date
FROM annees_scolaires
LIMIT 16
ON CONFLICT DO NOTHING;

-- Ensure we have 4+ Classe Matiere
INSERT INTO classe_matiere (classe_id, matiere_id, coefficient, volume_horaire_hebdo)
SELECT c.id, m.id, 2, 3
FROM classes c, matieres m
WHERE c.etablissement_id = m.etablissement_id
LIMIT 16
ON CONFLICT DO NOTHING;

-- Ensure we have 4+ Evaluations
INSERT INTO evaluations (etablissement_id, classe_matiere_id, type_evaluation_id, periode_id, titre, date_evaluation, bareme, statut)
SELECT DISTINCT cm.classe_id, cm.id, te.id, p.id, 'Évaluation ' || te.code, '2024-10-15'::date, 20, 'validee'
FROM classe_matiere cm, types_evaluation te, periodes p
LIMIT 16
ON CONFLICT DO NOTHING;

-- Ensure we have 4+ Notes
INSERT INTO notes (evaluation_id, eleve_id, valeur, observation, valide, date_saisie)
SELECT DISTINCT e.id, el.id, 15, 'Bien', true, now()
FROM evaluations e, eleves el
LIMIT 16
ON CONFLICT DO NOTHING;

-- Ensure we have 4+ Notifications
INSERT INTO notifications (utilisateur_id, titre, message, type_notification, date_envoi, lu)
SELECT DISTINCT id, 'Test', 'Message de test', 'INFO', now(), false
FROM utilisateurs
LIMIT 16
ON CONFLICT DO NOTHING;

-- Ensure we have 4+ Parents
INSERT INTO parents (etablissement_id, utilisateur_id, profession)
SELECT DISTINCT e.id, (SELECT id FROM utilisateurs u WHERE u.etablissement_id = e.id LIMIT 1), 'Professeur'
FROM etablissements e
UNION ALL
SELECT DISTINCT e.id, (SELECT id FROM utilisateurs u WHERE u.etablissement_id = e.id OFFSET 1 LIMIT 1), 'Commerçant'
FROM etablissements e
UNION ALL
SELECT DISTINCT e.id, (SELECT id FROM utilisateurs u WHERE u.etablissement_id = e.id OFFSET 2 LIMIT 1), 'Médecin'
FROM etablissements e
UNION ALL
SELECT DISTINCT e.id, (SELECT id FROM utilisateurs u WHERE u.etablissement_id = e.id OFFSET 3 LIMIT 1), 'Ingénieur'
FROM etablissements e
LIMIT 16
ON CONFLICT DO NOTHING;

-- Finalizing - Report counts
SELECT 'Seed Complete! Final Row Counts:' as message;
SELECT table_name, COUNT(*) as row_count
FROM (
  SELECT 'etablissements' as table_name, COUNT(*) as cnt FROM etablissements
  UNION ALL SELECT 'roles', COUNT(*) FROM roles
  UNION ALL SELECT 'utilisateurs', COUNT(*) FROM utilisateurs
  UNION ALL SELECT 'enseignants', COUNT(*) FROM enseignants
  UNION ALL SELECT 'classes', COUNT(*) FROM classes
  UNION ALL SELECT 'niveaux', COUNT(*) FROM niveaux
  UNION ALL SELECT 'series', COUNT(*) FROM series
  UNION ALL SELECT 'matieres', COUNT(*) FROM matieres
  UNION ALL SELECT 'salles', COUNT(*) FROM salles
  UNION ALL SELECT 'eleves', COUNT(*) FROM eleves
  UNION ALL SELECT 'inscriptions', COUNT(*) FROM inscriptions
  UNION ALL SELECT 'types_evaluation', COUNT(*) FROM types_evaluation
  UNION ALL SELECT 'periodes', COUNT(*) FROM periodes
  UNION ALL SELECT 'classe_matiere', COUNT(*) FROM classe_matiere
  UNION ALL SELECT 'evaluations', COUNT(*) FROM evaluations
  UNION ALL SELECT 'notes', COUNT(*) FROM notes
  UNION ALL SELECT 'notifications', COUNT(*) FROM notifications
  UNION ALL SELECT 'parents', COUNT(*) FROM parents
) t
GROUP BY table_name
ORDER BY table_name;
