-- Ultra-Simple Final Seed - Direct inserts only
-- Focus: Get 4+ rows into key tables without complex logic

-- 1. Clear and repopulate base tables with simple inserts

-- Etablissements (4 rows)
DELETE FROM etablissements WHERE nom LIKE 'Test%' OR sigle IN ('T1','T2','T3','T4');

INSERT INTO etablissements (nom, sigle, pays, statut, devise, fuseau_horaire, created_at, updated_at)
VALUES 
  ('Test School 1', 'T1', 'Bénin', 'actif', 'XOF', 'Africa/Douala', now(), now()),
  ('Test School 2', 'T2', 'Bénin', 'actif', 'XOF', 'Africa/Douala', now(), now()),
  ('Test School 3', 'T3', 'Bénin', 'actif', 'XOF', 'Africa/Douala', now(), now()),
  ('Test School 4', 'T4', 'Bénin', 'actif', 'XOF', 'Africa/Douala', now(), now());

-- Roles (insert only if missing, ensure 4+)
INSERT INTO roles (code, libelle, est_systeme)
VALUES 
  ('ADMIN', 'Admin', true),
  ('PROF', 'Professor', true),
  ('ELEV', 'Student', true),
  ('PAREN', 'Parent', true),
  ('SECRET', 'Secretary', true)
ON CONFLICT (code) DO NOTHING;

-- Utilisateurs (use subqueries to get etablissement IDs)
INSERT INTO utilisateurs (etablissement_id, nom, prenom, email, mot_de_passe_hash, statut, created_at, updated_at)
SELECT id, 'Admin', 'User', 'admin1@test.com', crypt('pass123', gen_salt('bf')), 'actif', now(), now()
FROM etablissements WHERE sigle='T1' LIMIT 1
UNION ALL
SELECT id, 'Prof', 'User', 'prof1@test.com', crypt('pass123', gen_salt('bf')), 'actif', now(), now()
FROM etablissements WHERE sigle='T2' LIMIT 1
UNION ALL
SELECT id, 'Eleve', 'User', 'elev1@test.com', crypt('pass123', gen_salt('bf')), 'actif', now(), now()
FROM etablissements WHERE sigle='T3' LIMIT 1
UNION ALL
SELECT id, 'Parent', 'User', 'par1@test.com', crypt('pass123', gen_salt('bf')), 'actif', now(), now()
FROM etablissements WHERE sigle='T4' LIMIT 1
ON CONFLICT DO NOTHING;

-- Annees Scolaires
INSERT INTO annees_scolaires (etablissement_id, libelle, date_debut, date_fin, est_active, created_at)
SELECT id, '2024-2025', '2024-09-01'::date, '2025-06-30'::date, true, now()
FROM etablissements WHERE sigle IN ('T1','T2','T3','T4')
ON CONFLICT DO NOTHING;

-- Niveaux (4 levels per etablissement = 16 total)
INSERT INTO niveaux (etablissement_id, code, libelle, ordre)
SELECT e.id, '6', 'Sixth', 1 FROM etablissements e WHERE e.sigle IN ('T1','T2','T3','T4')
UNION ALL
SELECT e.id, '5', 'Fifth', 2 FROM etablissements e WHERE e.sigle IN ('T1','T2','T3','T4')
UNION ALL
SELECT e.id, '4', 'Fourth', 3 FROM etablissements e WHERE e.sigle IN ('T1','T2','T3','T4')
UNION ALL
SELECT e.id, '3', 'Third', 4 FROM etablissements e WHERE e.sigle IN ('T1','T2','T3','T4')
ON CONFLICT DO NOTHING;

-- Series (4 series per etablissement = 16 total)
INSERT INTO series (etablissement_id, code, libelle)
SELECT e.id, 'S', 'Science' FROM etablissements e WHERE e.sigle IN ('T1','T2','T3','T4')
UNION ALL
SELECT e.id, 'L', 'Literature' FROM etablissements e WHERE e.sigle IN ('T1','T2','T3','T4')
UNION ALL
SELECT e.id, 'T', 'Technical' FROM etablissements e WHERE e.sigle IN ('T1','T2','T3','T4')
UNION ALL
SELECT e.id, 'C', 'Commercial' FROM etablissements e WHERE e.sigle IN ('T1','T2','T3','T4')
ON CONFLICT DO NOTHING;

-- Matieres (4 subjects per etablissement = 16 total)
INSERT INTO matieres (etablissement_id, code, libelle)
SELECT e.id, 'FR', 'French' FROM etablissements e WHERE e.sigle IN ('T1','T2','T3','T4')
UNION ALL
SELECT e.id, 'MATH', 'Math' FROM etablissements e WHERE e.sigle IN ('T1','T2','T3','T4')
UNION ALL
SELECT e.id, 'SCI', 'Science' FROM etablissements e WHERE e.sigle IN ('T1','T2','T3','T4')
UNION ALL
SELECT e.id, 'EN', 'English' FROM etablissements e WHERE e.sigle IN ('T1','T2','T3','T4')
ON CONFLICT DO NOTHING;

-- Salles (4 rooms per etablissement = 16 total)
INSERT INTO salles (etablissement_id, nom, capacite)
SELECT e.id, 'Room101', 30 FROM etablissements e WHERE e.sigle IN ('T1','T2','T3','T4')
UNION ALL
SELECT e.id, 'Room102', 35 FROM etablissements e WHERE e.sigle IN ('T1','T2','T3','T4')
UNION ALL
SELECT e.id, 'Lab', 25 FROM etablissements e WHERE e.sigle IN ('T1','T2','T3','T4')
UNION ALL
SELECT e.id, 'Gym', 50 FROM etablissements e WHERE e.sigle IN ('T1','T2','T3','T4')
ON CONFLICT DO NOTHING;

-- Enseignants
INSERT INTO enseignants (etablissement_id, utilisateur_id, specialite, statut)
SELECT e.id, u.id, 'French', 'actif' FROM utilisateurs u JOIN etablissements e ON u.etablissement_id=e.id WHERE e.sigle='T1' LIMIT 1
UNION ALL
SELECT e.id, u.id, 'Math', 'actif' FROM utilisateurs u JOIN etablissements e ON u.etablissement_id=e.id WHERE e.sigle='T2' LIMIT 1
UNION ALL
SELECT e.id, u.id, 'Science', 'actif' FROM utilisateurs u JOIN etablissements e ON u.etablissement_id=e.id WHERE e.sigle='T3' LIMIT 1
UNION ALL
SELECT e.id, u.id, 'English', 'actif' FROM utilisateurs u JOIN etablissements e ON u.etablissement_id=e.id WHERE e.sigle='T4' LIMIT 1
ON CONFLICT DO NOTHING;

-- Classes - now easier with direct IDs
INSERT INTO classes (etablissement_id, annee_scolaire_id, niveau_id, professeur_principal_id, serie_id, salle_id, libelle, capacite)
SELECT 
  e.id,
  (SELECT id FROM annees_scolaires WHERE etablissement_id=e.id LIMIT 1),
  (SELECT id FROM niveaux WHERE etablissement_id=e.id LIMIT 1),
  (SELECT id FROM enseignants WHERE etablissement_id=e.id LIMIT 1),
  (SELECT id FROM series WHERE etablissement_id=e.id LIMIT 1),
  (SELECT id FROM salles WHERE etablissement_id=e.id LIMIT 1),
  'Class_1A',
  30
FROM etablissements e
WHERE e.sigle IN ('T1','T2','T3','T4')
ON CONFLICT DO NOTHING;

INSERT INTO classes (etablissement_id, annee_scolaire_id, niveau_id, professeur_principal_id, serie_id, salle_id, libelle, capacite)
SELECT 
  e.id,
  (SELECT id FROM annees_scolaires WHERE etablissement_id=e.id LIMIT 1),
  (SELECT id FROM niveaux WHERE etablissement_id=e.id OFFSET 1 LIMIT 1),
  (SELECT id FROM enseignants WHERE etablissement_id=e.id LIMIT 1),
  (SELECT id FROM series WHERE etablissement_id=e.id OFFSET 1 LIMIT 1),
  (SELECT id FROM salles WHERE etablissement_id=e.id OFFSET 1 LIMIT 1),
  'Class_2B',
  32
FROM etablissements e
WHERE e.sigle IN ('T1','T2','T3','T4')
ON CONFLICT DO NOTHING;

INSERT INTO classes (etablissement_id, annee_scolaire_id, niveau_id, professeur_principal_id, serie_id, salle_id, libelle, capacite)
SELECT 
  e.id,
  (SELECT id FROM annees_scolaires WHERE etablissement_id=e.id LIMIT 1),
  (SELECT id FROM niveaux WHERE etablissement_id=e.id OFFSET 2 LIMIT 1),
  (SELECT id FROM enseignants WHERE etablissement_id=e.id LIMIT 1),
  (SELECT id FROM series WHERE etablissement_id=e.id OFFSET 2 LIMIT 1),
  (SELECT id FROM salles WHERE etablissement_id=e.id OFFSET 2 LIMIT 1),
  'Class_3C',
  28
FROM etablissements e
WHERE e.sigle IN ('T1','T2','T3','T4')
ON CONFLICT DO NOTHING;

INSERT INTO classes (etablissement_id, annee_scolaire_id, niveau_id, professeur_principal_id, serie_id, salle_id, libelle, capacite)
SELECT 
  e.id,
  (SELECT id FROM annees_scolaires WHERE etablissement_id=e.id LIMIT 1),
  (SELECT id FROM niveaux WHERE etablissement_id=e.id OFFSET 3 LIMIT 1),
  (SELECT id FROM enseignants WHERE etablissement_id=e.id LIMIT 1),
  (SELECT id FROM series WHERE etablissement_id=e.id OFFSET 3 LIMIT 1),
  (SELECT id FROM salles WHERE etablissement_id=e.id OFFSET 3 LIMIT 1),
  'Class_4D',
  25
FROM etablissements e
WHERE e.sigle IN ('T1','T2','T3','T4')
ON CONFLICT DO NOTHING;

-- Eleves
INSERT INTO eleves (etablissement_id, utilisateur_id, matricule, nom, prenom, sexe, date_naissance, date_inscription, statut, created_at, updated_at)
SELECT e.id, u.id, 'EL001', 'Student', 'One', 'M', '2010-01-01'::date, now()::date, 'actif', now(), now()
FROM utilisateurs u JOIN etablissements e ON u.etablissement_id=e.id WHERE e.sigle='T1' LIMIT 1
UNION ALL
SELECT e.id, u.id, 'EL002', 'Student', 'Two', 'F', '2010-02-01'::date, now()::date, 'actif', now(), now()
FROM utilisateurs u JOIN etablissements e ON u.etablissement_id=e.id WHERE e.sigle='T2' LIMIT 1
UNION ALL
SELECT e.id, u.id, 'EL003', 'Student', 'Three', 'M', '2010-03-01'::date, now()::date, 'actif', now(), now()
FROM utilisateurs u JOIN etablissements e ON u.etablissement_id=e.id WHERE e.sigle='T3' LIMIT 1
UNION ALL
SELECT e.id, u.id, 'EL004', 'Student', 'Four', 'F', '2010-04-01'::date, now()::date, 'actif', now(), now()
FROM utilisateurs u JOIN etablissements e ON u.etablissement_id=e.id WHERE e.sigle='T4' LIMIT 1
ON CONFLICT DO NOTHING;

-- Inscriptions
INSERT INTO inscriptions (eleve_id, annee_scolaire_id, classe_id, type_inscription, date_inscription, statut)
SELECT e.id, (SELECT id FROM annees_scolaires LIMIT 1), (SELECT id FROM classes LIMIT 1), 'inscription', now()::date, 'validee'
FROM eleves e
LIMIT 4
ON CONFLICT DO NOTHING;

-- Types Evaluation
INSERT INTO types_evaluation (code, libelle, ponderation)
VALUES 
  ('DEVR', 'Homework', 0.5),
  ('SEQ', 'Sequence', 1.0),
  ('COMP', 'Composition', 2.0),
  ('EXAM', 'Exam', 1.5)
ON CONFLICT (code) DO NOTHING;

-- Periodes
INSERT INTO periodes (annee_scolaire_id, type_periode, numero, libelle, date_debut, date_fin)
SELECT id, 'trimestre', 1, 'Term 1', '2024-09-01'::date, '2024-11-30'::date
FROM annees_scolaires
UNION ALL
SELECT id, 'trimestre', 2, 'Term 2', '2024-12-01'::date, '2025-02-28'::date
FROM annees_scolaires
UNION ALL
SELECT id, 'trimestre', 3, 'Term 3', '2025-03-01'::date, '2025-06-30'::date
FROM annees_scolaires
UNION ALL
SELECT id, 'sequence', 1, 'Sequence 1', '2024-09-01'::date, '2024-10-31'::date
FROM annees_scolaires
ON CONFLICT DO NOTHING;

-- Classe Matiere
INSERT INTO classe_matiere (classe_id, matiere_id, coefficient, volume_horaire_hebdo)
SELECT c.id, m.id, 2, 3
FROM classes c, matieres m
WHERE c.etablissement_id = m.etablissement_id
LIMIT 4
ON CONFLICT DO NOTHING;

-- Evaluations (with etablissement_id via classe join)
INSERT INTO evaluations (etablissement_id, classe_matiere_id, type_evaluation_id, periode_id, titre, date_evaluation, bareme, statut)
SELECT c.etablissement_id, cm.id, te.id, p.id, 'Eval_' || te.code, '2024-10-15'::date, 20, 'validee'
FROM classe_matiere cm
JOIN classes c ON cm.classe_id = c.id
CROSS JOIN types_evaluation te
CROSS JOIN periodes p
WHERE p.annee_scolaire_id = c.annee_scolaire_id
LIMIT 4
ON CONFLICT DO NOTHING;

-- Notes
INSERT INTO notes (evaluation_id, eleve_id, valeur, observation, valide, date_saisie)
SELECT e.id, el.id, 15::numeric, 'Good', true, now()
FROM evaluations e
CROSS JOIN eleves el
LIMIT 4
ON CONFLICT DO NOTHING;

-- Done! Report results
SELECT '✓ Seed Complete!';
