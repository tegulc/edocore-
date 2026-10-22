-- Simple Comprehensive Seed for EduCore
-- Populates essential tables with valid data

-- BASE: Roles (no dependencies)
INSERT INTO roles (code, libelle, description, created_at, updated_at)
VALUES 
  ('ADMIN', 'Admin', 'Admin', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  ('TEACHER', 'Teacher', 'Teacher', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  ('STUDENT', 'Student', 'Student', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  ('PARENT', 'Parent', 'Parent', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT DO NOTHING;

-- BASE: Etablissements (no dependencies, already have some)
SELECT 'Existing etablissements prepared';

-- BASE: Annees Scolaires
INSERT INTO annees_scolaires (etablissement_id, annee, date_debut, date_fin, est_active, created_at, updated_at)
SELECT id, 2024, '2024-09-01'::date, '2025-06-30'::date, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP FROM etablissements LIMIT 4
ON CONFLICT DO NOTHING;

INSERT INTO annees_scolaires (etablissement_id, annee, date_debut, date_fin, est_active, created_at, updated_at)
SELECT id, 2025, '2025-09-01'::date, '2026-06-30'::date, false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP FROM etablissements LIMIT 4
ON CONFLICT DO NOTHING;

-- BASE: Niveaux
INSERT INTO niveaux (etablissement_id, libelle, ordre, created_at, updated_at)
SELECT id, '6ème', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP FROM etablissements LIMIT 4
ON CONFLICT DO NOTHING;

INSERT INTO niveaux (etablissement_id, libelle, ordre, created_at, updated_at)
SELECT id, '5ème', 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP FROM etablissements LIMIT 4
ON CONFLICT DO NOTHING;

INSERT INTO niveaux (etablissement_id, libelle, ordre, created_at, updated_at)
SELECT id, '4ème', 3, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP FROM etablissements LIMIT 4
ON CONFLICT DO NOTHING;

INSERT INTO niveaux (etablissement_id, libelle, ordre, created_at, updated_at)
SELECT id, '3ème', 4, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP FROM etablissements LIMIT 4
ON CONFLICT DO NOTHING;

-- BASE: Series
INSERT INTO series (etablissement_id, libelle, code, created_at, updated_at)
SELECT id, 'Scientifique', 'S', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP FROM etablissements LIMIT 4
ON CONFLICT DO NOTHING;

INSERT INTO series (etablissement_id, libelle, code, created_at, updated_at)
SELECT id, 'Littéraire', 'L', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP FROM etablissements LIMIT 4
ON CONFLICT DO NOTHING;

INSERT INTO series (etablissement_id, libelle, code, created_at, updated_at)
SELECT id, 'Technique', 'T', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP FROM etablissements LIMIT 4
ON CONFLICT DO NOTHING;

INSERT INTO series (etablissement_id, libelle, code, created_at, updated_at)
SELECT id, 'Professionnel', 'P', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP FROM etablissements LIMIT 4
ON CONFLICT DO NOTHING;

-- BASE: Utilisateurs (simple approach)
INSERT INTO utilisateurs (etablissement_id, nom, prenom, email, telephone, login, mot_de_passe, statut, created_at, updated_at)
SELECT id, 'User', 'One', 'user1@edu.bj', '+22966112234', 'user1', crypt('password123', gen_salt('bf')), 'ACTIF', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP FROM etablissements LIMIT 4
ON CONFLICT DO NOTHING;

INSERT INTO utilisateurs (etablissement_id, nom, prenom, email, telephone, login, mot_de_passe, statut, created_at, updated_at)
SELECT id, 'User', 'Two', 'user2@edu.bj', '+22966112234', 'user2', crypt('password123', gen_salt('bf')), 'ACTIF', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP FROM etablissements LIMIT 4
ON CONFLICT DO NOTHING;

INSERT INTO utilisateurs (etablissement_id, nom, prenom, email, telephone, login, mot_de_passe, statut, created_at, updated_at)
SELECT id, 'User', 'Three', 'user3@edu.bj', '+22966112234', 'user3', crypt('password123', gen_salt('bf')), 'ACTIF', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP FROM etablissements LIMIT 4
ON CONFLICT DO NOTHING;

INSERT INTO utilisateurs (etablissement_id, nom, prenom, email, telephone, login, mot_de_passe, statut, created_at, updated_at)
SELECT id, 'User', 'Four', 'user4@edu.bj', '+22966112234', 'user4', crypt('password123', gen_salt('bf')), 'ACTIF', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP FROM etablissements LIMIT 4
ON CONFLICT DO NOTHING;

-- Utilisateurs Roles
INSERT INTO utilisateurs_roles (utilisateur_id, role_id)
SELECT u.id, (SELECT id FROM roles WHERE code = 'ADMIN' LIMIT 1) FROM utilisateurs u WHERE u.login LIKE 'user%' LIMIT 4
ON CONFLICT DO NOTHING;

-- BASE: Matieres
INSERT INTO matieres (etablissement_id, libelle, code, description, created_at, updated_at)
SELECT id, 'Français', 'FR', 'Français', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP FROM etablissements LIMIT 4
ON CONFLICT DO NOTHING;

INSERT INTO matieres (etablissement_id, libelle, code, description, created_at, updated_at)
SELECT id, 'Mathématiques', 'MATH', 'Math', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP FROM etablissements LIMIT 4
ON CONFLICT DO NOTHING;

INSERT INTO matieres (etablissement_id, libelle, code, description, created_at, updated_at)
SELECT id, 'Sciences', 'SCI', 'Sciences', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP FROM etablissements LIMIT 4
ON CONFLICT DO NOTHING;

INSERT INTO matieres (etablissement_id, libelle, code, description, created_at, updated_at)
SELECT id, 'Anglais', 'EN', 'English', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP FROM etablissements LIMIT 4
ON CONFLICT DO NOTHING;

-- BASE: Salles
INSERT INTO salles (etablissement_id, numero, capacite, equipement, created_at, updated_at)
SELECT id, 1, 30, 'Tableaux', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP FROM etablissements LIMIT 4
ON CONFLICT DO NOTHING;

INSERT INTO salles (etablissement_id, numero, capacite, equipement, created_at, updated_at)
SELECT id, 2, 35, 'Tableaux', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP FROM etablissements LIMIT 4
ON CONFLICT DO NOTHING;

INSERT INTO salles (etablissement_id, numero, capacite, equipement, created_at, updated_at)
SELECT id, 3, 40, 'Tableaux', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP FROM etablissements LIMIT 4
ON CONFLICT DO NOTHING;

INSERT INTO salles (etablissement_id, numero, capacite, equipement, created_at, updated_at)
SELECT id, 4, 25, 'Tableaux', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP FROM etablissements LIMIT 4
ON CONFLICT DO NOTHING;

-- BASE: Enseignants
INSERT INTO enseignants (utilisateur_id, etablissement_id, discipline, numero_agrement, date_embauche, statut, created_at, updated_at)
SELECT u.id, u.etablissement_id, 'Français', 'AG0001', '2020-01-15'::date, 'ACTIF', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP 
FROM utilisateurs u WHERE u.login = 'user1' LIMIT 4
ON CONFLICT DO NOTHING;

INSERT INTO enseignants (utilisateur_id, etablissement_id, discipline, numero_agrement, date_embauche, statut, created_at, updated_at)
SELECT u.id, u.etablissement_id, 'Mathématiques', 'AG0002', '2020-02-15'::date, 'ACTIF', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP 
FROM utilisateurs u WHERE u.login = 'user2' LIMIT 4
ON CONFLICT DO NOTHING;

INSERT INTO enseignants (utilisateur_id, etablissement_id, discipline, numero_agrement, date_embauche, statut, created_at, updated_at)
SELECT u.id, u.etablissement_id, 'Sciences', 'AG0003', '2020-03-15'::date, 'ACTIF', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP 
FROM utilisateurs u WHERE u.login = 'user3' LIMIT 4
ON CONFLICT DO NOTHING;

INSERT INTO enseignants (utilisateur_id, etablissement_id, discipline, numero_agrement, date_embauche, statut, created_at, updated_at)
SELECT u.id, u.etablissement_id, 'Anglais', 'AG0004', '2020-04-15'::date, 'ACTIF', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP 
FROM utilisateurs u WHERE u.login = 'user4' LIMIT 4
ON CONFLICT DO NOTHING;

-- CLASSES
INSERT INTO classes (etablissement_id, annee_scolaire_id, niveau_id, professeur_principal_id, serie_id, salle_id, sigle, libelle, nombre_eleves, effectif_max, created_at, updated_at)
SELECT 
  e.id,
  (SELECT id FROM annees_scolaires WHERE etablissement_id = e.id AND est_active LIMIT 1),
  (SELECT id FROM niveaux WHERE etablissement_id = e.id LIMIT 1),
  (SELECT id FROM enseignants LIMIT 1),
  (SELECT id FROM series WHERE etablissement_id = e.id LIMIT 1),
  (SELECT id FROM salles WHERE etablissement_id = e.id LIMIT 1),
  'CL001',
  'Classe 1',
  25,
  30,
  CURRENT_TIMESTAMP,
  CURRENT_TIMESTAMP
FROM etablissements e
LIMIT 4
ON CONFLICT DO NOTHING;

INSERT INTO classes (etablissement_id, annee_scolaire_id, niveau_id, professeur_principal_id, serie_id, salle_id, sigle, libelle, nombre_eleves, effectif_max, created_at, updated_at)
SELECT 
  e.id,
  (SELECT id FROM annees_scolaires WHERE etablissement_id = e.id AND est_active LIMIT 1),
  (SELECT id FROM niveaux WHERE etablissement_id = e.id OFFSET 1 LIMIT 1),
  (SELECT id FROM enseignants OFFSET 1 LIMIT 1),
  (SELECT id FROM series WHERE etablissement_id = e.id OFFSET 1 LIMIT 1),
  (SELECT id FROM salles WHERE etablissement_id = e.id OFFSET 1 LIMIT 1),
  'CL002',
  'Classe 2',
  28,
  30,
  CURRENT_TIMESTAMP,
  CURRENT_TIMESTAMP
FROM etablissements e
LIMIT 4
ON CONFLICT DO NOTHING;

INSERT INTO classes (etablissement_id, annee_scolaire_id, niveau_id, professeur_principal_id, serie_id, salle_id, sigle, libelle, nombre_eleves, effectif_max, created_at, updated_at)
SELECT 
  e.id,
  (SELECT id FROM annees_scolaires WHERE etablissement_id = e.id AND est_active LIMIT 1),
  (SELECT id FROM niveaux WHERE etablissement_id = e.id OFFSET 2 LIMIT 1),
  (SELECT id FROM enseignants OFFSET 2 LIMIT 1),
  (SELECT id FROM series WHERE etablissement_id = e.id OFFSET 2 LIMIT 1),
  (SELECT id FROM salles WHERE etablissement_id = e.id OFFSET 2 LIMIT 1),
  'CL003',
  'Classe 3',
  26,
  30,
  CURRENT_TIMESTAMP,
  CURRENT_TIMESTAMP
FROM etablissements e
LIMIT 4
ON CONFLICT DO NOTHING;

INSERT INTO classes (etablissement_id, annee_scolaire_id, niveau_id, professeur_principal_id, serie_id, salle_id, sigle, libelle, nombre_eleves, effectif_max, created_at, updated_at)
SELECT 
  e.id,
  (SELECT id FROM annees_scolaires WHERE etablissement_id = e.id AND est_active LIMIT 1),
  (SELECT id FROM niveaux WHERE etablissement_id = e.id OFFSET 3 LIMIT 1),
  (SELECT id FROM enseignants OFFSET 3 LIMIT 1),
  (SELECT id FROM series WHERE etablissement_id = e.id OFFSET 3 LIMIT 1),
  (SELECT id FROM salles WHERE etablissement_id = e.id OFFSET 3 LIMIT 1),
  'CL004',
  'Classe 4',
  30,
  30,
  CURRENT_TIMESTAMP,
  CURRENT_TIMESTAMP
FROM etablissements e
LIMIT 4
ON CONFLICT DO NOTHING;

-- ELEVES
INSERT INTO eleves (utilisateur_id, numero_inscription, date_inscription, classe_id, etablissement_id, created_at, updated_at)
SELECT 
  u.id,
  'EL0001',
  CURRENT_DATE,
  (SELECT id FROM classes LIMIT 1),
  u.etablissement_id,
  CURRENT_TIMESTAMP,
  CURRENT_TIMESTAMP
FROM utilisateurs u WHERE u.login LIKE 'user%' LIMIT 4
ON CONFLICT DO NOTHING;

INSERT INTO eleves (utilisateur_id, numero_inscription, date_inscription, classe_id, etablissement_id, created_at, updated_at)
SELECT 
  u.id,
  'EL0002',
  CURRENT_DATE,
  (SELECT id FROM classes OFFSET 1 LIMIT 1),
  u.etablissement_id,
  CURRENT_TIMESTAMP,
  CURRENT_TIMESTAMP
FROM utilisateurs u WHERE u.login LIKE 'user%' LIMIT 4 OFFSET 1
ON CONFLICT DO NOTHING;

INSERT INTO eleves (utilisateur_id, numero_inscription, date_inscription, classe_id, etablissement_id, created_at, updated_at)
SELECT 
  u.id,
  'EL0003',
  CURRENT_DATE,
  (SELECT id FROM classes OFFSET 2 LIMIT 1),
  u.etablissement_id,
  CURRENT_TIMESTAMP,
  CURRENT_TIMESTAMP
FROM utilisateurs u WHERE u.login LIKE 'user%' LIMIT 4 OFFSET 2
ON CONFLICT DO NOTHING;

INSERT INTO eleves (utilisateur_id, numero_inscription, date_inscription, classe_id, etablissement_id, created_at, updated_at)
SELECT 
  u.id,
  'EL0004',
  CURRENT_DATE,
  (SELECT id FROM classes OFFSET 3 LIMIT 1),
  u.etablissement_id,
  CURRENT_TIMESTAMP,
  CURRENT_TIMESTAMP
FROM utilisateurs u WHERE u.login LIKE 'user%' LIMIT 4 OFFSET 3
ON CONFLICT DO NOTHING;

-- INSCRIPTIONS
INSERT INTO inscriptions (eleve_id, classe_id, date_inscription, statut, created_at, updated_at)
SELECT e.id, c.id, CURRENT_DATE, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM eleves e
CROSS JOIN classes c
LIMIT 4
ON CONFLICT DO NOTHING;

-- MODULES
INSERT INTO modules (classe_id, matiere_id, enseignant_id, coefficient, description, created_at, updated_at)
SELECT c.id, m.id, e.id, 2, 'Module', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM classes c
CROSS JOIN matieres m
CROSS JOIN enseignants e
LIMIT 4
ON CONFLICT DO NOTHING;

-- EVALUATIONS
INSERT INTO evaluations (module_id, enseignant_id, titre, date_evaluation, type_evaluation, bareme, created_at, updated_at)
SELECT m.id, m.enseignant_id, 'Évaluation', CURRENT_DATE, 'CONTROLE', 20, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM modules m
LIMIT 4
ON CONFLICT DO NOTHING;

-- NOTES
INSERT INTO notes (evaluation_id, eleve_id, note, date_saisie, statut, created_at, updated_at)
SELECT e.id, el.id, 15::numeric, CURRENT_TIMESTAMP, 'VALIDE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM evaluations e
CROSS JOIN eleves el
LIMIT 4
ON CONFLICT DO NOTHING;

-- EMPLOIS DU TEMPS
INSERT INTO emplois_du_temps (classe_id, enseignant_id, matiere_id, salle_id, jour, heure_debut, heure_fin, established_at, updated_at)
SELECT c.id, e.id, m.id, s.id, 'LUNDI', '08:00'::time, '09:00'::time, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM classes c
CROSS JOIN enseignants e
CROSS JOIN matieres m
CROSS JOIN salles s
LIMIT 4
ON CONFLICT DO NOTHING;

-- ABSENCES
INSERT INTO absences (eleve_id, classe_id, enseignant_id, date_absence, duree, justifie, motif, created_at, updated_at)
SELECT el.id, c.id, e.id, CURRENT_DATE, 1, false, 'Absence', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM eleves el
CROSS JOIN classes c
CROSS JOIN enseignants e
LIMIT 4
ON CONFLICT DO NOTHING;

-- RESSOURCES & EMPRUNTS
INSERT INTO ressources (etablissement_id, titre, type_ressource, url, description, date_publication, created_at, updated_at)
SELECT id, 'Ressource 1', 'DOCUMENT', 'http://localhost', 'Description', CURRENT_DATE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP FROM etablissements LIMIT 4
ON CONFLICT DO NOTHING;

INSERT INTO ressources (etablissement_id, titre, type_ressource, url, description, date_publication, created_at, updated_at)
SELECT id, 'Ressource 2', 'VIDEO', 'http://localhost', 'Description', CURRENT_DATE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP FROM etablissements LIMIT 4
ON CONFLICT DO NOTHING;

INSERT INTO ressources (etablissement_id, titre, type_ressource, url, description, date_publication, created_at, updated_at)
SELECT id, 'Ressource 3', 'DOCUMENT', 'http://localhost', 'Description', CURRENT_DATE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP FROM etablissements LIMIT 4
ON CONFLICT DO NOTHING;

INSERT INTO ressources (etablissement_id, titre, type_ressource, url, description, date_publication, created_at, updated_at)
SELECT id, 'Ressource 4', 'EXERCICE', 'http://localhost', 'Description', CURRENT_DATE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP FROM etablissements LIMIT 4
ON CONFLICT DO NOTHING;

-- EXEMPLAIRES
INSERT INTO exemplaires (ressource_id, numero_exemplaire, localisation, etat, date_acquisition, created_at, updated_at)
SELECT id, 1, 'Salle 1', 'EXCELLENT', CURRENT_DATE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP FROM ressources LIMIT 4
ON CONFLICT DO NOTHING;

INSERT INTO exemplaires (ressource_id, numero_exemplaire, localisation, etat, date_acquisition, created_at, updated_at)
SELECT id, 2, 'Salle 2', 'BON', CURRENT_DATE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP FROM ressources LIMIT 4
ON CONFLICT DO NOTHING;

INSERT INTO exemplaires (ressource_id, numero_exemplaire, localisation, etat, date_acquisition, created_at, updated_at)
SELECT id, 3, 'Salle 3', 'ACCEPTABLE', CURRENT_DATE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP FROM ressources LIMIT 4
ON CONFLICT DO NOTHING;

INSERT INTO exemplaires (ressource_id, numero_exemplaire, localisation, etat, date_acquisition, created_at, updated_at)
SELECT id, 4, 'Salle 4', 'EXCELLENT', CURRENT_DATE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP FROM ressources LIMIT 4
ON CONFLICT DO NOTHING;

-- EMPRUNTS
INSERT INTO emprunts (eleve_id, exemplaire_id, date_emprunt, date_retour_prevue, statut, created_at, updated_at)
SELECT e.id, ex.id, CURRENT_DATE - INTERVAL '7 days', CURRENT_DATE + INTERVAL '7 days', 'ACTIF', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM eleves e
CROSS JOIN exemplaires ex
LIMIT 4
ON CONFLICT DO NOTHING;

-- AMENDES BIBLIOTHEQUE
INSERT INTO amendes_bibliotheque (emprunt_id, motif, montant, date_amende, statut_paiement, created_at, updated_at)
SELECT id, 'Retard', 1000, CURRENT_DATE, 'NON_PAYEE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM emprunts
LIMIT 4
ON CONFLICT DO NOTHING;

-- TRANCHES PAIEMENT
INSERT INTO tranches_paiement (etablissement_id, classe_id, numero_tranche, libelle, montant_base, date_limite, semestre, annee_scolaire_id, created_at, updated_at)
SELECT e.id, c.id, 1, 'Tranche 1', 50000, '2024-09-30'::date, 1, 
  (SELECT id FROM annees_scolaires WHERE etablissement_id = e.id LIMIT 1),
  CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM etablissements e
CROSS JOIN classes c
LIMIT 4
ON CONFLICT DO NOTHING;

INSERT INTO tranches_paiement (etablissement_id, classe_id, numero_tranche, libelle, montant_base, date_limite, semestre, annee_scolaire_id, created_at, updated_at)
SELECT e.id, c.id, 2, 'Tranche 2', 50000, '2025-02-28'::date, 2, 
  (SELECT id FROM annees_scolaires WHERE etablissement_id = e.id LIMIT 1),
  CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM etablissements e
CROSS JOIN classes c
LIMIT 4
ON CONFLICT DO NOTHING;

INSERT INTO tranches_paiement (etablissement_id, classe_id, numero_tranche, libelle, montant_base, date_limite, semestre, annee_scolaire_id, created_at, updated_at)
SELECT e.id, c.id, 3, 'Tranche 3', 50000, '2025-06-30'::date, 3, 
  (SELECT id FROM annees_scolaires WHERE etablissement_id = e.id LIMIT 1),
  CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM etablissements e
CROSS JOIN classes c
LIMIT 4
ON CONFLICT DO NOTHING;

INSERT INTO tranches_paiement (etablissement_id, classe_id, numero_tranche, libelle, montant_base, date_limite, semestre, annee_scolaire_id, created_at, updated_at)
SELECT e.id, c.id, 4, 'Tranche 4', 50000, '2026-03-31'::date, 4, 
  (SELECT id FROM annees_scolaires WHERE etablissement_id = e.id LIMIT 1),
  CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM etablissements e
CROSS JOIN classes c
LIMIT 4
ON CONFLICT DO NOTHING;

-- PAIEMENTS
INSERT INTO paiements (utilisateur_id, tranche_paiement_id, montant_paye, mode_paiement, date_paiement, statut_paiement, reference_paiement, created_at, updated_at)
SELECT u.id, tp.id, tp.montant_base, 'VIREMENT', CURRENT_DATE, 'VALIDE', 'REF' || LPAD(ROW_NUMBER() OVER (), 8, '0'), CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM utilisateurs u
CROSS JOIN tranches_paiement tp
LIMIT 4
ON CONFLICT DO NOTHING;

-- BULLETINS
INSERT INTO bulletins (eleve_id, annee_scolaire_id, trimestre, date_edition, moyenne_generale, classement, appréciation, created_at, updated_at)
SELECT el.id, ans.id, 1, CURRENT_DATE, 12.5, 5, 'Bon', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM eleves el
CROSS JOIN annees_scolaires ans
LIMIT 4
ON CONFLICT DO NOTHING;

-- NOTIFICATIONS
INSERT INTO notifications (utilisateur_id, titre, message, type_notification, date_envoi, lu, created_at, updated_at)
SELECT id, 'Bienvenue', 'Bienvenue EduCore', 'INFO', CURRENT_TIMESTAMP, false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM utilisateurs
LIMIT 4
ON CONFLICT DO NOTHING;

-- Confirmation
SELECT 'Seed completed successfully' AS message;
