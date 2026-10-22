-- ============================================================================
-- EDUCORE / SIGES - Systeme ERP Intelligent de Gestion d'Etablissements Scolaires
-- Script de creation de base de donnees complet - PostgreSQL 14+
-- Architecture : multi-tenant (multi-etablissements) via colonne etablissement_id
-- ============================================================================

-- ============================================================================
-- EXTENSIONS
-- ============================================================================
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- ============================================================================
-- PARTIE 0 : MULTI-TENANT / ANNEES SCOLAIRES / STRUCTURE TEMPORELLE
-- ============================================================================

CREATE TABLE etablissements (
    id                  BIGSERIAL PRIMARY KEY,
    uuid                UUID NOT NULL DEFAULT uuid_generate_v4(),
    nom                 VARCHAR(200) NOT NULL,
    sigle               VARCHAR(20),
    logo_url            TEXT,
    pays                VARCHAR(100) NOT NULL,
    ville               VARCHAR(100),
    adresse             TEXT,
    telephone           VARCHAR(30),
    email               VARCHAR(150),
    type_etablissement  VARCHAR(50), -- primaire, college, lycee, universite
    statut              VARCHAR(20) DEFAULT 'actif', -- actif, suspendu, resilie
    plan_abonnement     VARCHAR(30) DEFAULT 'standard', -- essai, standard, premium
    date_debut_abonnement DATE,
    date_fin_abonnement DATE,
    devise              VARCHAR(10) DEFAULT 'XAF',
    langue_defaut       VARCHAR(10) DEFAULT 'fr',
    fuseau_horaire      VARCHAR(50) DEFAULT 'Africa/Douala',
    created_at          TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE annees_scolaires (
    id                  BIGSERIAL PRIMARY KEY,
    etablissement_id    BIGINT NOT NULL REFERENCES etablissements(id) ON DELETE CASCADE,
    libelle             VARCHAR(20) NOT NULL, -- ex: 2025-2026
    date_debut          DATE NOT NULL,
    date_fin            DATE NOT NULL,
    est_active          BOOLEAN NOT NULL DEFAULT false,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE (etablissement_id, libelle)
);

CREATE TABLE periodes ( -- trimestres / sequences / semestres
    id                  BIGSERIAL PRIMARY KEY,
    annee_scolaire_id   BIGINT NOT NULL REFERENCES annees_scolaires(id) ON DELETE CASCADE,
    type_periode        VARCHAR(20) NOT NULL, -- trimestre, sequence, semestre
    numero              SMALLINT NOT NULL, -- 1,2,3...
    libelle             VARCHAR(50) NOT NULL,
    date_debut          DATE NOT NULL,
    date_fin            DATE NOT NULL,
    est_cloturee        BOOLEAN NOT NULL DEFAULT false,
    UNIQUE (annee_scolaire_id, type_periode, numero)
);

CREATE TABLE jours_feries (
    id                  BIGSERIAL PRIMARY KEY,
    etablissement_id    BIGINT NOT NULL REFERENCES etablissements(id) ON DELETE CASCADE,
    libelle             VARCHAR(150) NOT NULL,
    date_jour           DATE NOT NULL,
    recurrent_annuel    BOOLEAN DEFAULT false
);

CREATE TABLE vacances_scolaires (
    id                  BIGSERIAL PRIMARY KEY,
    annee_scolaire_id   BIGINT NOT NULL REFERENCES annees_scolaires(id) ON DELETE CASCADE,
    libelle             VARCHAR(100) NOT NULL,
    date_debut          DATE NOT NULL,
    date_fin            DATE NOT NULL
);

-- ============================================================================
-- PARTIE 1 : MODULE 1 - GESTION DES UTILISATEURS / ROLES / PERMISSIONS
-- ============================================================================

CREATE TABLE roles (
    id                  BIGSERIAL PRIMARY KEY,
    code                VARCHAR(50) NOT NULL UNIQUE, -- ADMIN, DIRECTEUR, CENSEUR, SURVEILLANT_GENERAL, ECONOME,
                                                       -- COMPTABLE, SECRETAIRE, PROFESSEUR, ELEVE, PARENT,
                                                       -- BIBLIOTHECAIRE, INFIRMIER, CHAUFFEUR, PERSONNEL_ADMIN, SECURITE
    libelle             VARCHAR(100) NOT NULL,
    description         TEXT,
    est_systeme         BOOLEAN NOT NULL DEFAULT true
);

CREATE TABLE permissions (
    id                  BIGSERIAL PRIMARY KEY,
    code                VARCHAR(100) NOT NULL UNIQUE, -- ex: NOTES_SAISIR, NOTES_VALIDER, ELEVE_CREER...
    module               VARCHAR(50) NOT NULL,
    libelle             VARCHAR(150) NOT NULL,
    description         TEXT
);

CREATE TABLE role_permissions (
    id                  BIGSERIAL PRIMARY KEY,
    role_id             BIGINT NOT NULL REFERENCES roles(id) ON DELETE CASCADE,
    permission_id       BIGINT NOT NULL REFERENCES permissions(id) ON DELETE CASCADE,
    UNIQUE (role_id, permission_id)
);

CREATE TABLE utilisateurs (
    id                  BIGSERIAL PRIMARY KEY,
    uuid                UUID NOT NULL DEFAULT uuid_generate_v4(),
    etablissement_id    BIGINT NOT NULL REFERENCES etablissements(id) ON DELETE CASCADE,
    role_id             BIGINT NOT NULL REFERENCES roles(id),
    matricule           VARCHAR(50),
    nom                 VARCHAR(100) NOT NULL,
    prenom              VARCHAR(100) NOT NULL,
    email               VARCHAR(150),
    telephone           VARCHAR(30),
    mot_de_passe_hash   TEXT NOT NULL,
    photo_url           TEXT,
    langue_preferee     VARCHAR(10) DEFAULT 'fr',
    statut              VARCHAR(20) NOT NULL DEFAULT 'actif', -- actif, inactif, suspendu, archive
    deux_fa_active      BOOLEAN NOT NULL DEFAULT false,
    deux_fa_secret      TEXT,
    derniere_connexion  TIMESTAMPTZ,
    doit_changer_mdp    BOOLEAN NOT NULL DEFAULT false,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE (etablissement_id, email)
);

CREATE INDEX idx_utilisateurs_etablissement ON utilisateurs(etablissement_id);
CREATE INDEX idx_utilisateurs_role ON utilisateurs(role_id);

CREATE TABLE utilisateur_sessions (
    id                  BIGSERIAL PRIMARY KEY,
    utilisateur_id      BIGINT NOT NULL REFERENCES utilisateurs(id) ON DELETE CASCADE,
    token_hash          TEXT NOT NULL,
    adresse_ip          VARCHAR(45),
    user_agent          TEXT,
    date_connexion      TIMESTAMPTZ NOT NULL DEFAULT now(),
    date_expiration     TIMESTAMPTZ NOT NULL,
    revoque             BOOLEAN NOT NULL DEFAULT false
);

CREATE TABLE historique_connexions (
    id                  BIGSERIAL PRIMARY KEY,
    utilisateur_id      BIGINT NOT NULL REFERENCES utilisateurs(id) ON DELETE CASCADE,
    adresse_ip          VARCHAR(45),
    user_agent          TEXT,
    succes              BOOLEAN NOT NULL,
    date_tentative      TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- ============================================================================

-- ============================================================================
-- PARTIE 4a : MODULE 4 - ENSEIGNANTS (cree avant les classes pour la ref. prof. principal)
-- ============================================================================

CREATE TABLE enseignants (
    id                  BIGSERIAL PRIMARY KEY,
    etablissement_id    BIGINT NOT NULL REFERENCES etablissements(id) ON DELETE CASCADE,
    utilisateur_id      BIGINT NOT NULL REFERENCES utilisateurs(id) ON DELETE CASCADE,
    matricule           VARCHAR(50),
    specialite           VARCHAR(150),
    diplome_principal    VARCHAR(150),
    date_embauche        DATE,
    type_contrat         VARCHAR(30), -- permanent, vacataire, contractuel
    salaire_base          NUMERIC(12,2),
    taux_horaire_supp     NUMERIC(10,2),
    statut                VARCHAR(20) NOT NULL DEFAULT 'actif',
    UNIQUE (etablissement_id, matricule)
);

-- PARTIE 5 : MODULE 5 - GESTION DES CLASSES
-- ============================================================================

CREATE TABLE niveaux (
    id                  BIGSERIAL PRIMARY KEY,
    etablissement_id    BIGINT NOT NULL REFERENCES etablissements(id) ON DELETE CASCADE,
    code                VARCHAR(20) NOT NULL, -- 6e, 5e, 4e, 3e, 2nde, 1ere, Tle
    libelle             VARCHAR(50) NOT NULL,
    ordre               SMALLINT NOT NULL,
    cycle               VARCHAR(30), -- premier_cycle, second_cycle
    UNIQUE (etablissement_id, code)
);

CREATE TABLE series ( -- A, C, D, Espagnol, Allemand, Italien, Chinois...
    id                  BIGSERIAL PRIMARY KEY,
    etablissement_id    BIGINT NOT NULL REFERENCES etablissements(id) ON DELETE CASCADE,
    code                VARCHAR(20) NOT NULL,
    libelle             VARCHAR(100) NOT NULL,
    UNIQUE (etablissement_id, code)
);

CREATE TABLE salles (
    id                  BIGSERIAL PRIMARY KEY,
    etablissement_id    BIGINT NOT NULL REFERENCES etablissements(id) ON DELETE CASCADE,
    nom                 VARCHAR(50) NOT NULL,
    capacite            INT NOT NULL,
    type_salle          VARCHAR(30) DEFAULT 'classe', -- classe, labo, informatique, sport
    UNIQUE (etablissement_id, nom)
);

CREATE TABLE classes (
    id                  BIGSERIAL PRIMARY KEY,
    etablissement_id    BIGINT NOT NULL REFERENCES etablissements(id) ON DELETE CASCADE,
    annee_scolaire_id   BIGINT NOT NULL REFERENCES annees_scolaires(id),
    niveau_id           BIGINT NOT NULL REFERENCES niveaux(id),
    serie_id            BIGINT REFERENCES series(id),
    libelle             VARCHAR(50) NOT NULL, -- ex: 3eme A1
    salle_id            BIGINT REFERENCES salles(id),
    professeur_principal_id BIGINT REFERENCES enseignants(id),
    capacite            INT NOT NULL DEFAULT 60,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE (etablissement_id, annee_scolaire_id, libelle)
);

CREATE INDEX idx_classes_annee ON classes(annee_scolaire_id);

-- (l'effectif est calcule dynamiquement via COUNT(inscriptions) - vue plus bas)

-- ============================================================================
-- PARTIE 2 : MODULE 2 - GESTION DES ELEVES
-- ============================================================================

CREATE TABLE eleves (
    id                      BIGSERIAL PRIMARY KEY,
    uuid                    UUID NOT NULL DEFAULT uuid_generate_v4(),
    etablissement_id        BIGINT NOT NULL REFERENCES etablissements(id) ON DELETE CASCADE,
    utilisateur_id          BIGINT REFERENCES utilisateurs(id),
    matricule               VARCHAR(50) NOT NULL,
    nom                     VARCHAR(100) NOT NULL,
    prenom                  VARCHAR(100) NOT NULL,
    sexe                    CHAR(1) NOT NULL CHECK (sexe IN ('M','F')),
    date_naissance          DATE NOT NULL,
    lieu_naissance          VARCHAR(150),
    nationalite             VARCHAR(80),
    religion                VARCHAR(80),
    groupe_sanguin          VARCHAR(5),
    allergies               TEXT,
    photo_url               TEXT,
    acte_naissance_url      TEXT,
    certificat_medical_url  TEXT,
    qr_code                 TEXT UNIQUE,
    statut                  VARCHAR(20) NOT NULL DEFAULT 'actif', -- actif, transfere, exclu, diplome, abandonne
    date_inscription        DATE NOT NULL DEFAULT CURRENT_DATE,
    created_at              TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at              TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE (etablissement_id, matricule)
);

CREATE INDEX idx_eleves_etablissement ON eleves(etablissement_id);

CREATE TABLE inscriptions ( -- une ligne par annee scolaire = reinscription geree naturellement
    id                  BIGSERIAL PRIMARY KEY,
    eleve_id            BIGINT NOT NULL REFERENCES eleves(id) ON DELETE CASCADE,
    annee_scolaire_id   BIGINT NOT NULL REFERENCES annees_scolaires(id),
    classe_id           BIGINT NOT NULL REFERENCES classes(id),
    type_inscription    VARCHAR(20) NOT NULL DEFAULT 'inscription', -- inscription, reinscription, transfert
    date_inscription    DATE NOT NULL DEFAULT CURRENT_DATE,
    statut              VARCHAR(20) NOT NULL DEFAULT 'validee', -- en_attente, validee, annulee
    created_at          TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE (eleve_id, annee_scolaire_id)
);

CREATE TABLE transferts (
    id                  BIGSERIAL PRIMARY KEY,
    eleve_id            BIGINT NOT NULL REFERENCES eleves(id) ON DELETE CASCADE,
    etablissement_origine_id BIGINT REFERENCES etablissements(id),
    etablissement_destination_id BIGINT REFERENCES etablissements(id),
    date_transfert      DATE NOT NULL,
    motif               TEXT,
    document_url        TEXT
);

CREATE TABLE historique_scolaire (
    id                  BIGSERIAL PRIMARY KEY,
    eleve_id            BIGINT NOT NULL REFERENCES eleves(id) ON DELETE CASCADE,
    annee_scolaire_id   BIGINT NOT NULL REFERENCES annees_scolaires(id),
    classe_id           BIGINT REFERENCES classes(id),
    moyenne_generale    NUMERIC(5,2),
    rang                INT,
    decision_finale     VARCHAR(50), -- admis, redouble, exclu
    observations        TEXT
);

CREATE TABLE dossiers_numeriques (
    id                  BIGSERIAL PRIMARY KEY,
    eleve_id            BIGINT NOT NULL REFERENCES eleves(id) ON DELETE CASCADE,
    type_document        VARCHAR(50) NOT NULL, -- acte_naissance, certificat_medical, photo, diplome, autre
    fichier_url          TEXT NOT NULL,
    date_ajout           TIMESTAMPTZ NOT NULL DEFAULT now(),
    ajoute_par_id        BIGINT REFERENCES utilisateurs(id)
);

-- ============================================================================
-- PARTIE 3 : MODULE 3 - GESTION DES PARENTS
-- ============================================================================

CREATE TABLE parents (
    id                  BIGSERIAL PRIMARY KEY,
    etablissement_id    BIGINT NOT NULL REFERENCES etablissements(id) ON DELETE CASCADE,
    utilisateur_id      BIGINT NOT NULL REFERENCES utilisateurs(id) ON DELETE CASCADE,
    profession           VARCHAR(100),
    adresse              TEXT,
    created_at           TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE eleve_parent (
    id                  BIGSERIAL PRIMARY KEY,
    eleve_id            BIGINT NOT NULL REFERENCES eleves(id) ON DELETE CASCADE,
    parent_id           BIGINT NOT NULL REFERENCES parents(id) ON DELETE CASCADE,
    lien_parente        VARCHAR(30) NOT NULL, -- pere, mere, tuteur, autre
    est_contact_principal BOOLEAN NOT NULL DEFAULT false,
    autorise_recuperer_eleve BOOLEAN NOT NULL DEFAULT true,
    UNIQUE (eleve_id, parent_id)
);

CREATE TABLE convocations (
    id                  BIGSERIAL PRIMARY KEY,
    eleve_id            BIGINT NOT NULL REFERENCES eleves(id) ON DELETE CASCADE,
    parent_id           BIGINT REFERENCES parents(id),
    motif               TEXT NOT NULL,
    date_convocation    TIMESTAMPTZ NOT NULL,
    lieu                VARCHAR(150),
    statut              VARCHAR(20) DEFAULT 'envoyee', -- envoyee, confirmee, honoree, annulee
    cree_par_id         BIGINT REFERENCES utilisateurs(id),
    created_at          TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- ============================================================================

-- ============================================================================
-- PARTIE 4b : MODULE 4 (suite) - PERSONNEL / CONTRATS / CONGES / HEURES / SALAIRES
-- ============================================================================

CREATE TABLE personnel ( -- personnel non-enseignant generique (econome, secretaire, securite, etc.)
    id                  BIGSERIAL PRIMARY KEY,
    etablissement_id    BIGINT NOT NULL REFERENCES etablissements(id) ON DELETE CASCADE,
    utilisateur_id      BIGINT NOT NULL REFERENCES utilisateurs(id) ON DELETE CASCADE,
    matricule           VARCHAR(50),
    fonction             VARCHAR(100),
    departement          VARCHAR(100),
    date_embauche        DATE,
    type_contrat         VARCHAR(30),
    salaire_base          NUMERIC(12,2),
    statut                VARCHAR(20) NOT NULL DEFAULT 'actif',
    UNIQUE (etablissement_id, matricule)
);

CREATE TABLE contrats (
    id                  BIGSERIAL PRIMARY KEY,
    utilisateur_id      BIGINT NOT NULL REFERENCES utilisateurs(id) ON DELETE CASCADE,
    type_contrat        VARCHAR(30) NOT NULL,
    date_debut          DATE NOT NULL,
    date_fin            DATE,
    document_url        TEXT,
    numero_cnps         VARCHAR(50),
    statut              VARCHAR(20) DEFAULT 'actif'
);

CREATE TABLE demandes_conges (
    id                  BIGSERIAL PRIMARY KEY,
    utilisateur_id      BIGINT NOT NULL REFERENCES utilisateurs(id) ON DELETE CASCADE,
    type_conge          VARCHAR(50) NOT NULL, -- annuel, maladie, maternite, exceptionnel
    date_debut          DATE NOT NULL,
    date_fin            DATE NOT NULL,
    motif               TEXT,
    statut              VARCHAR(20) NOT NULL DEFAULT 'en_attente', -- en_attente, approuve, refuse
    valide_par_id        BIGINT REFERENCES utilisateurs(id),
    created_at           TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE heures_travail ( -- pointage, heures effectuees, heures supplementaires
    id                  BIGSERIAL PRIMARY KEY,
    utilisateur_id      BIGINT NOT NULL REFERENCES utilisateurs(id) ON DELETE CASCADE,
    date_jour            DATE NOT NULL,
    heure_arrivee        TIME,
    heure_depart          TIME,
    heures_normales       NUMERIC(5,2) DEFAULT 0,
    heures_supplementaires NUMERIC(5,2) DEFAULT 0,
    UNIQUE (utilisateur_id, date_jour)
);

CREATE TABLE salaires (
    id                  BIGSERIAL PRIMARY KEY,
    utilisateur_id      BIGINT NOT NULL REFERENCES utilisateurs(id) ON DELETE CASCADE,
    periode_mois         DATE NOT NULL, -- premier jour du mois
    salaire_base          NUMERIC(12,2) NOT NULL,
    heures_supp_montant   NUMERIC(12,2) DEFAULT 0,
    primes                NUMERIC(12,2) DEFAULT 0,
    retenues               NUMERIC(12,2) DEFAULT 0,
    net_a_payer            NUMERIC(12,2) NOT NULL,
    statut_paiement        VARCHAR(20) DEFAULT 'en_attente', -- en_attente, paye
    date_paiement           DATE,
    UNIQUE (utilisateur_id, periode_mois)
);

-- ============================================================================
-- PARTIE 6 : MODULE 6 - GESTION DES MATIERES
-- ============================================================================

CREATE TABLE matieres (
    id                  BIGSERIAL PRIMARY KEY,
    etablissement_id    BIGINT NOT NULL REFERENCES etablissements(id) ON DELETE CASCADE,
    code                VARCHAR(20) NOT NULL,
    libelle             VARCHAR(100) NOT NULL,
    couleur             VARCHAR(7) DEFAULT '#3498db',
    UNIQUE (etablissement_id, code)
);

CREATE TABLE classe_matiere ( -- coefficient et volume horaire par classe (peut varier par niveau/serie)
    id                  BIGSERIAL PRIMARY KEY,
    classe_id           BIGINT NOT NULL REFERENCES classes(id) ON DELETE CASCADE,
    matiere_id          BIGINT NOT NULL REFERENCES matieres(id) ON DELETE CASCADE,
    enseignant_id        BIGINT REFERENCES enseignants(id),
    coefficient           NUMERIC(4,2) NOT NULL DEFAULT 1,
    volume_horaire_hebdo   NUMERIC(4,2) NOT NULL DEFAULT 1,
    UNIQUE (classe_id, matiere_id)
);

CREATE TABLE programmes_matiere (
    id                  BIGSERIAL PRIMARY KEY,
    matiere_id          BIGINT NOT NULL REFERENCES matieres(id) ON DELETE CASCADE,
    niveau_id           BIGINT NOT NULL REFERENCES niveaux(id) ON DELETE CASCADE,
    contenu              TEXT
);

-- ============================================================================
-- PARTIE 7 : MODULE 7 - EMPLOI DU TEMPS
-- ============================================================================

CREATE TABLE creneaux_horaires (
    id                  BIGSERIAL PRIMARY KEY,
    etablissement_id    BIGINT NOT NULL REFERENCES etablissements(id) ON DELETE CASCADE,
    libelle             VARCHAR(30) NOT NULL, -- ex: "07h30-08h25"
    heure_debut          TIME NOT NULL,
    heure_fin             TIME NOT NULL,
    ordre                 SMALLINT NOT NULL
);

CREATE TABLE emploi_du_temps (
    id                  BIGSERIAL PRIMARY KEY,
    etablissement_id    BIGINT NOT NULL REFERENCES etablissements(id) ON DELETE CASCADE,
    classe_id           BIGINT NOT NULL REFERENCES classes(id) ON DELETE CASCADE,
    matiere_id          BIGINT NOT NULL REFERENCES matieres(id),
    enseignant_id        BIGINT NOT NULL REFERENCES enseignants(id),
    salle_id             BIGINT REFERENCES salles(id),
    creneau_id            BIGINT NOT NULL REFERENCES creneaux_horaires(id),
    jour_semaine           SMALLINT NOT NULL CHECK (jour_semaine BETWEEN 1 AND 7), -- 1=lundi
    annee_scolaire_id       BIGINT NOT NULL REFERENCES annees_scolaires(id),
    created_at              TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_edt_classe ON emploi_du_temps(classe_id);
CREATE INDEX idx_edt_enseignant ON emploi_du_temps(enseignant_id);
-- Contraintes anti-conflits (empeche prof/salle d'etre pris 2 fois sur le meme creneau/jour)
CREATE UNIQUE INDEX uq_edt_salle_creneau ON emploi_du_temps(salle_id, creneau_id, jour_semaine, annee_scolaire_id) WHERE salle_id IS NOT NULL;
CREATE UNIQUE INDEX uq_edt_enseignant_creneau ON emploi_du_temps(enseignant_id, creneau_id, jour_semaine, annee_scolaire_id);
CREATE UNIQUE INDEX uq_edt_classe_creneau ON emploi_du_temps(classe_id, creneau_id, jour_semaine, annee_scolaire_id);

-- ============================================================================
-- PARTIE 8 : MODULE 8 - GESTION DES NOTES / EVALUATIONS
-- ============================================================================

CREATE TABLE types_evaluation (
    id                  BIGSERIAL PRIMARY KEY,
    code                VARCHAR(20) NOT NULL UNIQUE, -- DEVOIR, SEQUENCE, COMPOSITION, EXAMEN, PROJET, TP
    libelle             VARCHAR(50) NOT NULL,
    ponderation          NUMERIC(4,2) NOT NULL DEFAULT 1
);

CREATE TABLE evaluations (
    id                  BIGSERIAL PRIMARY KEY,
    etablissement_id    BIGINT NOT NULL REFERENCES etablissements(id) ON DELETE CASCADE,
    classe_matiere_id   BIGINT NOT NULL REFERENCES classe_matiere(id) ON DELETE CASCADE,
    type_evaluation_id  BIGINT NOT NULL REFERENCES types_evaluation(id),
    periode_id           BIGINT NOT NULL REFERENCES periodes(id),
    titre                 VARCHAR(150) NOT NULL,
    date_evaluation        DATE NOT NULL,
    bareme                 NUMERIC(5,2) NOT NULL DEFAULT 20,
    statut                 VARCHAR(20) DEFAULT 'planifiee', -- planifiee, saisie, validee, publiee
    created_by_id           BIGINT REFERENCES utilisateurs(id),
    created_at               TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE notes (
    id                  BIGSERIAL PRIMARY KEY,
    evaluation_id       BIGINT NOT NULL REFERENCES evaluations(id) ON DELETE CASCADE,
    eleve_id            BIGINT NOT NULL REFERENCES eleves(id) ON DELETE CASCADE,
    valeur               NUMERIC(5,2),
    absent                BOOLEAN NOT NULL DEFAULT false,
    observation           VARCHAR(255),
    saisi_par_id           BIGINT REFERENCES utilisateurs(id),
    valide                 BOOLEAN NOT NULL DEFAULT false,
    valide_par_id           BIGINT REFERENCES utilisateurs(id),
    date_saisie             TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE (evaluation_id, eleve_id)
);

CREATE INDEX idx_notes_eleve ON notes(eleve_id);

CREATE TABLE moyennes_matiere ( -- moyenne calculee par eleve/matiere/periode
    id                  BIGSERIAL PRIMARY KEY,
    eleve_id            BIGINT NOT NULL REFERENCES eleves(id) ON DELETE CASCADE,
    classe_matiere_id   BIGINT NOT NULL REFERENCES classe_matiere(id) ON DELETE CASCADE,
    periode_id           BIGINT NOT NULL REFERENCES periodes(id),
    moyenne               NUMERIC(5,2),
    rang_matiere            INT,
    appreciation             VARCHAR(100),
    calcule_le                TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE (eleve_id, classe_matiere_id, periode_id)
);

CREATE TABLE moyennes_generales (
    id                  BIGSERIAL PRIMARY KEY,
    eleve_id            BIGINT NOT NULL REFERENCES eleves(id) ON DELETE CASCADE,
    classe_id           BIGINT NOT NULL REFERENCES classes(id) ON DELETE CASCADE,
    periode_id           BIGINT NOT NULL REFERENCES periodes(id),
    moyenne_generale       NUMERIC(5,2),
    rang_classe              INT,
    decision                  VARCHAR(50), -- felicitations, encouragements, avertissement, blame
    calcule_le                 TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE (eleve_id, periode_id)
);

-- ============================================================================
-- PARTIE 9 : MODULE 9 - BULLETINS
-- ============================================================================

CREATE TABLE bulletins (
    id                  BIGSERIAL PRIMARY KEY,
    eleve_id            BIGINT NOT NULL REFERENCES eleves(id) ON DELETE CASCADE,
    classe_id           BIGINT NOT NULL REFERENCES classes(id),
    periode_id           BIGINT NOT NULL REFERENCES periodes(id),
    moyenne_generale       NUMERIC(5,2),
    rang                     INT,
    appreciation_generale     TEXT,
    decision_conseil          VARCHAR(100),
    nombre_absences            INT DEFAULT 0,
    fichier_pdf_url             TEXT,
    signature_numerique_url      TEXT,
    statut                       VARCHAR(20) DEFAULT 'brouillon', -- brouillon, valide, publie
    genere_le                     TIMESTAMPTZ,
    UNIQUE (eleve_id, periode_id)
);

-- ============================================================================
-- PARTIE 10 & 11 : CAHIER DE TEXTE NUMERIQUE / DEVOIRS
-- ============================================================================

CREATE TABLE cahier_de_texte (
    id                  BIGSERIAL PRIMARY KEY,
    classe_matiere_id   BIGINT NOT NULL REFERENCES classe_matiere(id) ON DELETE CASCADE,
    date_cours            DATE NOT NULL,
    chapitre               VARCHAR(200),
    lecon                    VARCHAR(200),
    contenu                    TEXT,
    exercices                   TEXT,
    created_by_id                 BIGINT REFERENCES utilisateurs(id),
    created_at                     TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE devoirs (
    id                  BIGSERIAL PRIMARY KEY,
    classe_matiere_id   BIGINT NOT NULL REFERENCES classe_matiere(id) ON DELETE CASCADE,
    titre                 VARCHAR(200) NOT NULL,
    description             TEXT,
    date_publication         DATE NOT NULL DEFAULT CURRENT_DATE,
    date_limite               DATE NOT NULL,
    fichier_sujet_url           TEXT,
    created_by_id                 BIGINT REFERENCES utilisateurs(id),
    created_at                     TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE devoirs_rendus (
    id                  BIGSERIAL PRIMARY KEY,
    devoir_id           BIGINT NOT NULL REFERENCES devoirs(id) ON DELETE CASCADE,
    eleve_id            BIGINT NOT NULL REFERENCES eleves(id) ON DELETE CASCADE,
    fichier_reponse_url    TEXT,
    date_soumission          TIMESTAMPTZ NOT NULL DEFAULT now(),
    note                       NUMERIC(5,2),
    correction_commentaire       TEXT,
    corrige_par_id                 BIGINT REFERENCES utilisateurs(id),
    corrige_le                       TIMESTAMPTZ,
    statut                             VARCHAR(20) DEFAULT 'rendu', -- rendu, en_retard, corrige
    UNIQUE (devoir_id, eleve_id)
);

-- ============================================================================
-- PARTIE 12 : MODULE 12 - PRESENCE
-- ============================================================================

CREATE TABLE dispositifs_pointage (
    id                  BIGSERIAL PRIMARY KEY,
    etablissement_id    BIGINT NOT NULL REFERENCES etablissements(id) ON DELETE CASCADE,
    type_dispositif      VARCHAR(30) NOT NULL, -- qrcode, nfc, empreinte, reconnaissance_faciale
    localisation           VARCHAR(150),
    statut                   VARCHAR(20) DEFAULT 'actif'
);

CREATE TABLE presences_eleves (
    id                  BIGSERIAL PRIMARY KEY,
    eleve_id            BIGINT NOT NULL REFERENCES eleves(id) ON DELETE CASCADE,
    classe_matiere_id   BIGINT REFERENCES classe_matiere(id), -- NULL = pointage general d'entree
    date_jour             DATE NOT NULL,
    heure_pointage          TIME,
    type_presence             VARCHAR(20) NOT NULL DEFAULT 'present', -- present, absent, retard, exclu_cours
    methode_pointage           VARCHAR(20), -- qrcode, nfc, empreinte, faciale, manuel
    justifie                     BOOLEAN DEFAULT false,
    motif_justification            TEXT,
    enregistre_par_id                BIGINT REFERENCES utilisateurs(id),
    created_at                         TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_presences_eleve_date ON presences_eleves(eleve_id, date_jour);

CREATE TABLE presences_personnel (
    id                  BIGSERIAL PRIMARY KEY,
    utilisateur_id      BIGINT NOT NULL REFERENCES utilisateurs(id) ON DELETE CASCADE,
    date_jour             DATE NOT NULL,
    heure_arrivee           TIME,
    heure_depart              TIME,
    type_presence               VARCHAR(20) DEFAULT 'present',
    methode_pointage              VARCHAR(20)
);

-- ============================================================================
-- PARTIE 13 : MODULE 13 - DISCIPLINE
-- ============================================================================

CREATE TABLE types_sanction (
    id                  BIGSERIAL PRIMARY KEY,
    code                VARCHAR(30) NOT NULL UNIQUE, -- avertissement, blame, exclusion_temp, exclusion_def
    libelle             VARCHAR(100) NOT NULL,
    gravite              SMALLINT DEFAULT 1
);

CREATE TABLE incidents_disciplinaires (
    id                  BIGSERIAL PRIMARY KEY,
    eleve_id            BIGINT NOT NULL REFERENCES eleves(id) ON DELETE CASCADE,
    date_incident          DATE NOT NULL,
    description               TEXT NOT NULL,
    signale_par_id              BIGINT REFERENCES utilisateurs(id),
    created_at                    TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE sanctions (
    id                  BIGSERIAL PRIMARY KEY,
    incident_id          BIGINT REFERENCES incidents_disciplinaires(id) ON DELETE SET NULL,
    eleve_id             BIGINT NOT NULL REFERENCES eleves(id) ON DELETE CASCADE,
    type_sanction_id       BIGINT NOT NULL REFERENCES types_sanction(id),
    date_sanction             DATE NOT NULL DEFAULT CURRENT_DATE,
    duree_jours                  INT,
    motif                          TEXT,
    decidee_par_id                  BIGINT REFERENCES utilisateurs(id),
    created_at                        TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE conseils_discipline (
    id                  BIGSERIAL PRIMARY KEY,
    eleve_id            BIGINT NOT NULL REFERENCES eleves(id) ON DELETE CASCADE,
    date_conseil          TIMESTAMPTZ NOT NULL,
    membres_presents         TEXT,
    decision                    TEXT,
    compte_rendu_url              TEXT,
    created_at                      TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- ============================================================================
-- PARTIE 14 : MODULE 14 - PAIEMENT
-- ============================================================================

CREATE TABLE structures_frais ( -- grille tarifaire par niveau/annee
    id                  BIGSERIAL PRIMARY KEY,
    etablissement_id    BIGINT NOT NULL REFERENCES etablissements(id) ON DELETE CASCADE,
    annee_scolaire_id   BIGINT NOT NULL REFERENCES annees_scolaires(id),
    niveau_id           BIGINT NOT NULL REFERENCES niveaux(id),
    libelle             VARCHAR(100) NOT NULL, -- ex: "Frais de scolarite", "Frais d'examen"
    montant             NUMERIC(12,2) NOT NULL,
    obligatoire          BOOLEAN DEFAULT true
);

CREATE TABLE echeanciers_paiement (
    id                  BIGSERIAL PRIMARY KEY,
    eleve_id            BIGINT NOT NULL REFERENCES eleves(id) ON DELETE CASCADE,
    structure_frais_id  BIGINT NOT NULL REFERENCES structures_frais(id),
    montant_total        NUMERIC(12,2) NOT NULL,
    nombre_tranches        INT NOT NULL DEFAULT 1,
    created_at               TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE tranches_paiement (
    id                  BIGSERIAL PRIMARY KEY,
    echeancier_id        BIGINT NOT NULL REFERENCES echeanciers_paiement(id) ON DELETE CASCADE,
    numero_tranche          INT NOT NULL,
    montant_du                 NUMERIC(12,2) NOT NULL,
    date_echeance                 DATE NOT NULL,
    statut                          VARCHAR(20) DEFAULT 'impaye' -- impaye, partiel, paye, en_retard
);

CREATE TABLE moyens_paiement (
    id                  BIGSERIAL PRIMARY KEY,
    code                VARCHAR(30) NOT NULL UNIQUE -- orange_money, mtn_momo, carte_bancaire, virement, especes
);

CREATE TABLE paiements (
    id                  BIGSERIAL PRIMARY KEY,
    uuid                UUID NOT NULL DEFAULT uuid_generate_v4(),
    etablissement_id    BIGINT NOT NULL REFERENCES etablissements(id) ON DELETE CASCADE,
    eleve_id            BIGINT NOT NULL REFERENCES eleves(id) ON DELETE CASCADE,
    tranche_id           BIGINT REFERENCES tranches_paiement(id),
    moyen_paiement_id    BIGINT NOT NULL REFERENCES moyens_paiement(id),
    montant                NUMERIC(12,2) NOT NULL,
    reference_transaction     VARCHAR(100), -- id transaction Orange Money / MTN / banque
    statut                      VARCHAR(20) NOT NULL DEFAULT 'en_attente', -- en_attente, confirme, echoue, rembourse
    date_paiement                  TIMESTAMPTZ NOT NULL DEFAULT now(),
    encaisse_par_id                  BIGINT REFERENCES utilisateurs(id)
);

CREATE INDEX idx_paiements_eleve ON paiements(eleve_id);

CREATE TABLE recus (
    id                  BIGSERIAL PRIMARY KEY,
    paiement_id          BIGINT NOT NULL REFERENCES paiements(id) ON DELETE CASCADE,
    numero_recu             VARCHAR(50) NOT NULL UNIQUE,
    fichier_pdf_url             TEXT,
    genere_le                     TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE factures (
    id                  BIGSERIAL PRIMARY KEY,
    eleve_id            BIGINT NOT NULL REFERENCES eleves(id) ON DELETE CASCADE,
    numero_facture         VARCHAR(50) NOT NULL UNIQUE,
    montant_total              NUMERIC(12,2) NOT NULL,
    date_emission                 DATE NOT NULL DEFAULT CURRENT_DATE,
    fichier_pdf_url                  TEXT,
    statut                             VARCHAR(20) DEFAULT 'emise'
);

CREATE TABLE relances_paiement (
    id                  BIGSERIAL PRIMARY KEY,
    tranche_id           BIGINT NOT NULL REFERENCES tranches_paiement(id) ON DELETE CASCADE,
    canal                  VARCHAR(20) NOT NULL, -- sms, email, whatsapp, push
    date_envoi                TIMESTAMPTZ NOT NULL DEFAULT now(),
    statut                      VARCHAR(20) DEFAULT 'envoyee'
);

-- ============================================================================
-- PARTIE 15 : MODULE 15 - COMPTABILITE
-- ============================================================================

CREATE TABLE comptes_comptables (
    id                  BIGSERIAL PRIMARY KEY,
    etablissement_id    BIGINT NOT NULL REFERENCES etablissements(id) ON DELETE CASCADE,
    code                VARCHAR(20) NOT NULL,
    libelle             VARCHAR(150) NOT NULL,
    type_compte          VARCHAR(20) NOT NULL, -- actif, passif, charge, produit
    UNIQUE (etablissement_id, code)
);

CREATE TABLE journal_comptable (
    id                  BIGSERIAL PRIMARY KEY,
    etablissement_id    BIGINT NOT NULL REFERENCES etablissements(id) ON DELETE CASCADE,
    compte_id           BIGINT NOT NULL REFERENCES comptes_comptables(id),
    date_ecriture          DATE NOT NULL,
    libelle                   VARCHAR(255) NOT NULL,
    debit                       NUMERIC(14,2) DEFAULT 0,
    credit                        NUMERIC(14,2) DEFAULT 0,
    piece_justificative_url         TEXT,
    reference_source                  VARCHAR(50), -- ex: paiement:1234, salaire:456
    saisi_par_id                        BIGINT REFERENCES utilisateurs(id),
    created_at                            TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE budgets (
    id                  BIGSERIAL PRIMARY KEY,
    etablissement_id    BIGINT NOT NULL REFERENCES etablissements(id) ON DELETE CASCADE,
    annee_scolaire_id   BIGINT NOT NULL REFERENCES annees_scolaires(id),
    compte_id           BIGINT NOT NULL REFERENCES comptes_comptables(id),
    montant_prevu       NUMERIC(14,2) NOT NULL,
    montant_realise      NUMERIC(14,2) DEFAULT 0
);

CREATE TABLE depenses (
    id                  BIGSERIAL PRIMARY KEY,
    etablissement_id    BIGINT NOT NULL REFERENCES etablissements(id) ON DELETE CASCADE,
    categorie            VARCHAR(100) NOT NULL, -- achat, salaire, maintenance, transport...
    libelle                 VARCHAR(255) NOT NULL,
    montant                    NUMERIC(14,2) NOT NULL,
    date_depense                  DATE NOT NULL,
    justificatif_url                 TEXT,
    approuve_par_id                    BIGINT REFERENCES utilisateurs(id),
    created_at                            TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- ============================================================================
-- PARTIE 16 : MODULE 16 - BIBLIOTHEQUE
-- ============================================================================

CREATE TABLE livres (
    id                  BIGSERIAL PRIMARY KEY,
    etablissement_id    BIGINT NOT NULL REFERENCES etablissements(id) ON DELETE CASCADE,
    isbn                VARCHAR(30),
    titre               VARCHAR(255) NOT NULL,
    auteur                VARCHAR(150),
    editeur                 VARCHAR(150),
    categorie                 VARCHAR(100),
    nombre_exemplaires           INT NOT NULL DEFAULT 1,
    exemplaires_disponibles         INT NOT NULL DEFAULT 1,
    qr_code                           TEXT UNIQUE,
    couverture_url                      TEXT
);

CREATE TABLE emprunts (
    id                  BIGSERIAL PRIMARY KEY,
    livre_id             BIGINT NOT NULL REFERENCES livres(id) ON DELETE CASCADE,
    utilisateur_id        BIGINT NOT NULL REFERENCES utilisateurs(id) ON DELETE CASCADE, -- eleve ou personnel
    date_emprunt             DATE NOT NULL DEFAULT CURRENT_DATE,
    date_retour_prevue           DATE NOT NULL,
    date_retour_effective            DATE,
    statut                             VARCHAR(20) DEFAULT 'en_cours' -- en_cours, retourne, perdu, en_retard
);

CREATE TABLE amendes_bibliotheque (
    id                  BIGSERIAL PRIMARY KEY,
    emprunt_id           BIGINT NOT NULL REFERENCES emprunts(id) ON DELETE CASCADE,
    montant                 NUMERIC(10,2) NOT NULL,
    motif                      VARCHAR(150), -- retard, perte, degradation
    statut_paiement               VARCHAR(20) DEFAULT 'impaye'
);

-- ============================================================================
-- PARTIE 17 : MODULE 17 - INFIRMERIE
-- ============================================================================

CREATE TABLE consultations_medicales (
    id                  BIGSERIAL PRIMARY KEY,
    eleve_id            BIGINT NOT NULL REFERENCES eleves(id) ON DELETE CASCADE,
    date_consultation      TIMESTAMPTZ NOT NULL DEFAULT now(),
    motif                     TEXT NOT NULL,
    diagnostic                  TEXT,
    traitement                    TEXT,
    infirmier_id                    BIGINT REFERENCES utilisateurs(id)
);

CREATE TABLE vaccins (
    id                  BIGSERIAL PRIMARY KEY,
    eleve_id            BIGINT NOT NULL REFERENCES eleves(id) ON DELETE CASCADE,
    nom_vaccin           VARCHAR(150) NOT NULL,
    date_administration     DATE NOT NULL,
    rappel_prevu               DATE
);

CREATE TABLE ordonnances (
    id                  BIGSERIAL PRIMARY KEY,
    consultation_id      BIGINT NOT NULL REFERENCES consultations_medicales(id) ON DELETE CASCADE,
    medicament               VARCHAR(150) NOT NULL,
    posologie                  VARCHAR(150),
    duree_traitement              VARCHAR(50)
);

CREATE TABLE urgences_medicales (
    id                  BIGSERIAL PRIMARY KEY,
    eleve_id            BIGINT NOT NULL REFERENCES eleves(id) ON DELETE CASCADE,
    date_urgence           TIMESTAMPTZ NOT NULL DEFAULT now(),
    description               TEXT NOT NULL,
    action_prise                TEXT,
    hopital_transfert              VARCHAR(150),
    parent_notifie                    BOOLEAN DEFAULT false
);

CREATE TABLE maladies_chroniques (
    id                  BIGSERIAL PRIMARY KEY,
    eleve_id            BIGINT NOT NULL REFERENCES eleves(id) ON DELETE CASCADE,
    nom_maladie           VARCHAR(150) NOT NULL,
    details                  TEXT,
    traitement_permanent        TEXT
);

-- ============================================================================
-- PARTIE 18 : MODULE 18 - TRANSPORT SCOLAIRE
-- ============================================================================

CREATE TABLE bus (
    id                  BIGSERIAL PRIMARY KEY,
    etablissement_id    BIGINT NOT NULL REFERENCES etablissements(id) ON DELETE CASCADE,
    immatriculation      VARCHAR(30) NOT NULL,
    capacite               INT NOT NULL,
    chauffeur_id              BIGINT REFERENCES utilisateurs(id),
    dispositif_gps_id            VARCHAR(100),
    statut                          VARCHAR(20) DEFAULT 'actif'
);

CREATE TABLE itineraires (
    id                  BIGSERIAL PRIMARY KEY,
    bus_id               BIGINT NOT NULL REFERENCES bus(id) ON DELETE CASCADE,
    libelle                 VARCHAR(150) NOT NULL,
    heure_depart_matin          TIME,
    heure_retour_soir              TIME
);

CREATE TABLE arrets_bus (
    id                  BIGSERIAL PRIMARY KEY,
    itineraire_id         BIGINT NOT NULL REFERENCES itineraires(id) ON DELETE CASCADE,
    libelle                  VARCHAR(150) NOT NULL,
    latitude                    NUMERIC(10,7),
    longitude                     NUMERIC(10,7),
    ordre                           SMALLINT NOT NULL
);

CREATE TABLE eleve_transport (
    id                  BIGSERIAL PRIMARY KEY,
    eleve_id            BIGINT NOT NULL REFERENCES eleves(id) ON DELETE CASCADE,
    itineraire_id         BIGINT NOT NULL REFERENCES itineraires(id),
    arret_id                BIGINT REFERENCES arrets_bus(id),
    UNIQUE (eleve_id)
);

CREATE TABLE presences_bus (
    id                  BIGSERIAL PRIMARY KEY,
    eleve_id            BIGINT NOT NULL REFERENCES eleves(id) ON DELETE CASCADE,
    bus_id               BIGINT NOT NULL REFERENCES bus(id),
    date_jour              DATE NOT NULL,
    trajet                    VARCHAR(10) NOT NULL, -- aller, retour
    statut                       VARCHAR(20) DEFAULT 'monte' -- monte, absent
);

CREATE TABLE positions_gps (
    id                  BIGSERIAL PRIMARY KEY,
    bus_id               BIGINT NOT NULL REFERENCES bus(id) ON DELETE CASCADE,
    latitude                NUMERIC(10,7) NOT NULL,
    longitude                 NUMERIC(10,7) NOT NULL,
    horodatage                   TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- ============================================================================
-- PARTIE 19 : MODULE 19 - CANTINE
-- ============================================================================

CREATE TABLE menus_cantine (
    id                  BIGSERIAL PRIMARY KEY,
    etablissement_id    BIGINT NOT NULL REFERENCES etablissements(id) ON DELETE CASCADE,
    date_jour            DATE NOT NULL,
    libelle_plat            VARCHAR(255) NOT NULL,
    informations_nutritionnelles TEXT
);

CREATE TABLE abonnements_cantine (
    id                  BIGSERIAL PRIMARY KEY,
    eleve_id            BIGINT NOT NULL REFERENCES eleves(id) ON DELETE CASCADE,
    periode_id           BIGINT NOT NULL REFERENCES periodes(id),
    montant                 NUMERIC(10,2) NOT NULL,
    statut_paiement            VARCHAR(20) DEFAULT 'impaye'
);

CREATE TABLE presences_cantine (
    id                  BIGSERIAL PRIMARY KEY,
    eleve_id            BIGINT NOT NULL REFERENCES eleves(id) ON DELETE CASCADE,
    date_jour             DATE NOT NULL,
    statut                    VARCHAR(20) DEFAULT 'present'
);

CREATE TABLE stock_cantine (
    id                  BIGSERIAL PRIMARY KEY,
    etablissement_id    BIGINT NOT NULL REFERENCES etablissements(id) ON DELETE CASCADE,
    article               VARCHAR(150) NOT NULL,
    quantite                 NUMERIC(10,2) NOT NULL,
    unite                       VARCHAR(20),
    seuil_alerte                   NUMERIC(10,2)
);

-- ============================================================================
-- PARTIE 21 : MODULE 21 - GESTION DOCUMENTAIRE
-- ============================================================================

CREATE TABLE documents (
    id                  BIGSERIAL PRIMARY KEY,
    etablissement_id    BIGINT NOT NULL REFERENCES etablissements(id) ON DELETE CASCADE,
    proprietaire_type    VARCHAR(30) NOT NULL, -- eleve, utilisateur, etablissement
    proprietaire_id       BIGINT NOT NULL,
    type_document           VARCHAR(50) NOT NULL, -- acte, diplome, bulletin, attestation, contrat
    titre                      VARCHAR(200) NOT NULL,
    fichier_url                   TEXT NOT NULL,
    archive                          BOOLEAN DEFAULT false,
    date_ajout                         TIMESTAMPTZ NOT NULL DEFAULT now(),
    ajoute_par_id                         BIGINT REFERENCES utilisateurs(id)
);

CREATE INDEX idx_documents_proprietaire ON documents(proprietaire_type, proprietaire_id);

-- ============================================================================
-- PARTIE 22 : MODULE 22 - CHAT / MESSAGERIE
-- ============================================================================

CREATE TABLE conversations (
    id                  BIGSERIAL PRIMARY KEY,
    etablissement_id    BIGINT NOT NULL REFERENCES etablissements(id) ON DELETE CASCADE,
    type_conversation    VARCHAR(20) NOT NULL DEFAULT 'prive', -- prive, groupe
    titre                 VARCHAR(150),
    created_by_id           BIGINT REFERENCES utilisateurs(id),
    created_at                TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE conversation_participants (
    id                  BIGSERIAL PRIMARY KEY,
    conversation_id      BIGINT NOT NULL REFERENCES conversations(id) ON DELETE CASCADE,
    utilisateur_id         BIGINT NOT NULL REFERENCES utilisateurs(id) ON DELETE CASCADE,
    date_ajout                TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE (conversation_id, utilisateur_id)
);

CREATE TABLE messages (
    id                  BIGSERIAL PRIMARY KEY,
    conversation_id      BIGINT NOT NULL REFERENCES conversations(id) ON DELETE CASCADE,
    expediteur_id           BIGINT NOT NULL REFERENCES utilisateurs(id),
    contenu                    TEXT,
    type_contenu                  VARCHAR(20) DEFAULT 'texte', -- texte, photo, document
    fichier_url                      TEXT,
    lu                                  BOOLEAN DEFAULT false,
    envoye_le                             TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_messages_conversation ON messages(conversation_id, envoye_le);

-- ============================================================================
-- PARTIE 23 : MODULE 23 - REUNIONS
-- ============================================================================

CREATE TABLE reunions (
    id                  BIGSERIAL PRIMARY KEY,
    etablissement_id    BIGINT NOT NULL REFERENCES etablissements(id) ON DELETE CASCADE,
    titre                 VARCHAR(200) NOT NULL,
    description             TEXT,
    date_reunion               TIMESTAMPTZ NOT NULL,
    lieu                          VARCHAR(150),
    lien_visioconference             TEXT,
    organisateur_id                     BIGINT REFERENCES utilisateurs(id),
    created_at                             TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE reunion_invites (
    id                  BIGSERIAL PRIMARY KEY,
    reunion_id            BIGINT NOT NULL REFERENCES reunions(id) ON DELETE CASCADE,
    utilisateur_id           BIGINT NOT NULL REFERENCES utilisateurs(id) ON DELETE CASCADE,
    statut_reponse              VARCHAR(20) DEFAULT 'en_attente', -- en_attente, accepte, decline
    present_effectif                BOOLEAN,
    UNIQUE (reunion_id, utilisateur_id)
);

CREATE TABLE comptes_rendus (
    id                  BIGSERIAL PRIMARY KEY,
    reunion_id            BIGINT NOT NULL REFERENCES reunions(id) ON DELETE CASCADE,
    contenu                  TEXT,
    fichier_url                 TEXT,
    redige_par_id                  BIGINT REFERENCES utilisateurs(id)
);

-- ============================================================================
-- PARTIE 24 : MODULE 24 - NOTIFICATIONS
-- ============================================================================

CREATE TABLE notifications (
    id                  BIGSERIAL PRIMARY KEY,
    etablissement_id    BIGINT NOT NULL REFERENCES etablissements(id) ON DELETE CASCADE,
    destinataire_id      BIGINT NOT NULL REFERENCES utilisateurs(id) ON DELETE CASCADE,
    categorie              VARCHAR(30) NOT NULL, -- paiement, retard, absence, reunion, notes, discipline, autre
    titre                     VARCHAR(200) NOT NULL,
    contenu                     TEXT,
    canal                          VARCHAR(20) NOT NULL, -- sms, email, whatsapp, push
    statut_envoi                      VARCHAR(20) DEFAULT 'en_attente', -- en_attente, envoye, echoue, lu
    envoye_le                            TIMESTAMPTZ,
    lu_le                                    TIMESTAMPTZ,
    created_at                                 TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_notifications_destinataire ON notifications(destinataire_id, statut_envoi);

CREATE TABLE appareils_mobiles ( -- pour push notifications
    id                  BIGSERIAL PRIMARY KEY,
    utilisateur_id      BIGINT NOT NULL REFERENCES utilisateurs(id) ON DELETE CASCADE,
    plateforme            VARCHAR(20) NOT NULL, -- android, ios
    push_token               TEXT NOT NULL,
    date_enregistrement          TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- ============================================================================
-- PARTIE 25 : MODULE 25 - INTELLIGENCE ARTIFICIELLE
-- ============================================================================

CREATE TABLE ia_recommandations_orientation (
    id                  BIGSERIAL PRIMARY KEY,
    eleve_id            BIGINT NOT NULL REFERENCES eleves(id) ON DELETE CASCADE,
    filieres_suggerees    JSONB, -- ex: [{"filiere":"C","score":0.87}, ...]
    centres_interet_detectes TEXT,
    genere_le              TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE ia_alertes_risque_echec (
    id                  BIGSERIAL PRIMARY KEY,
    eleve_id            BIGINT NOT NULL REFERENCES eleves(id) ON DELETE CASCADE,
    periode_id           BIGINT NOT NULL REFERENCES periodes(id),
    score_risque           NUMERIC(4,3) NOT NULL, -- 0 a 1
    facteurs_identifies      JSONB,
    genere_le                 TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE ia_exercices_personnalises (
    id                  BIGSERIAL PRIMARY KEY,
    eleve_id            BIGINT NOT NULL REFERENCES eleves(id) ON DELETE CASCADE,
    matiere_id           BIGINT NOT NULL REFERENCES matieres(id),
    contenu_genere          TEXT,
    niveau_difficulte          VARCHAR(20),
    genere_le                     TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE ia_commentaires_bulletin (
    id                  BIGSERIAL PRIMARY KEY,
    bulletin_id          BIGINT NOT NULL REFERENCES bulletins(id) ON DELETE CASCADE,
    commentaire_genere      TEXT NOT NULL,
    valide_par_enseignant       BOOLEAN DEFAULT false
);

CREATE TABLE ia_predictions_examens (
    id                  BIGSERIAL PRIMARY KEY,
    eleve_id            BIGINT NOT NULL REFERENCES eleves(id) ON DELETE CASCADE,
    examen_cible           VARCHAR(100) NOT NULL, -- BEPC, Probatoire, Bac...
    probabilite_reussite       NUMERIC(4,3),
    genere_le                     TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE ia_conversations_assistant (
    id                  BIGSERIAL PRIMARY KEY,
    utilisateur_id      BIGINT NOT NULL REFERENCES utilisateurs(id) ON DELETE CASCADE,
    question              TEXT NOT NULL,
    reponse                 TEXT,
    created_at                 TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- ============================================================================
-- PARTIE 28 : MODULE 28 - SECURITE / AUDIT
-- ============================================================================

CREATE TABLE journal_audit (
    id                  BIGSERIAL PRIMARY KEY,
    etablissement_id    BIGINT REFERENCES etablissements(id) ON DELETE CASCADE,
    utilisateur_id      BIGINT REFERENCES utilisateurs(id),
    action                VARCHAR(50) NOT NULL, -- CREATE, UPDATE, DELETE, LOGIN, EXPORT...
    table_cible             VARCHAR(100),
    enregistrement_id          BIGINT,
    anciennes_valeurs             JSONB,
    nouvelles_valeurs                JSONB,
    adresse_ip                          VARCHAR(45),
    horodatage                             TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_audit_etablissement ON journal_audit(etablissement_id, horodatage);

CREATE TABLE sauvegardes (
    id                  BIGSERIAL PRIMARY KEY,
    etablissement_id    BIGINT REFERENCES etablissements(id) ON DELETE CASCADE,
    type_sauvegarde       VARCHAR(20) NOT NULL, -- complete, incrementale
    fichier_url              TEXT,
    taille_octets                BIGINT,
    statut                          VARCHAR(20) DEFAULT 'en_cours',
    date_sauvegarde                    TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE consentements_rgpd (
    id                  BIGSERIAL PRIMARY KEY,
    utilisateur_id      BIGINT NOT NULL REFERENCES utilisateurs(id) ON DELETE CASCADE,
    type_consentement     VARCHAR(50) NOT NULL, -- donnees_personnelles, photo, communication_marketing
    accorde                 BOOLEAN NOT NULL,
    date_consentement          TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- ============================================================================
-- PARTIE 30 : MODULE 30 - API / INTEGRATIONS EXTERNES
-- ============================================================================

CREATE TABLE api_clients ( -- consommateurs de l'API (app mobile, partenaires)
    id                  BIGSERIAL PRIMARY KEY,
    etablissement_id    BIGINT REFERENCES etablissements(id) ON DELETE CASCADE,
    nom                 VARCHAR(150) NOT NULL,
    client_id            VARCHAR(100) NOT NULL UNIQUE,
    client_secret_hash     TEXT NOT NULL,
    statut                   VARCHAR(20) DEFAULT 'actif',
    created_at                  TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE integrations_externes (
    id                  BIGSERIAL PRIMARY KEY,
    etablissement_id    BIGINT NOT NULL REFERENCES etablissements(id) ON DELETE CASCADE,
    fournisseur           VARCHAR(50) NOT NULL, -- orange_money, mtn_momo, banque, moodle, microsoft365, google_workspace
    configuration            JSONB, -- cles API, endpoints, credentials chiffres
    statut                      VARCHAR(20) DEFAULT 'actif',
    created_at                     TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE api_logs (
    id                  BIGSERIAL PRIMARY KEY,
    api_client_id        BIGINT REFERENCES api_clients(id) ON DELETE SET NULL,
    endpoint                VARCHAR(255) NOT NULL,
    methode_http                VARCHAR(10) NOT NULL,
    code_reponse                   INT,
    duree_ms                          INT,
    horodatage                           TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- ============================================================================
-- VUES UTILES (MODULE 26 : DASHBOARD / MODULE 27 : RAPPORTS)
-- ============================================================================

CREATE VIEW vue_effectifs_classe AS
SELECT c.id AS classe_id, c.libelle, COUNT(i.id) AS effectif, c.capacite
FROM classes c
LEFT JOIN inscriptions i ON i.classe_id = c.id AND i.statut = 'validee'
GROUP BY c.id, c.libelle, c.capacite;

CREATE VIEW vue_solde_eleve AS
SELECT e.id AS eleve_id,
       COALESCE(SUM(t.montant_du), 0) AS total_du,
       COALESCE(SUM(p.montant) FILTER (WHERE p.statut = 'confirme'), 0) AS total_paye,
       COALESCE(SUM(t.montant_du), 0) - COALESCE(SUM(p.montant) FILTER (WHERE p.statut = 'confirme'), 0) AS solde_restant
FROM eleves e
LEFT JOIN echeanciers_paiement ec ON ec.eleve_id = e.id
LEFT JOIN tranches_paiement t ON t.echeancier_id = ec.id
LEFT JOIN paiements p ON p.tranche_id = t.id
GROUP BY e.id;

CREATE VIEW vue_taux_presence_eleve AS
SELECT eleve_id,
       COUNT(*) FILTER (WHERE type_presence = 'present') AS jours_presents,
       COUNT(*) FILTER (WHERE type_presence = 'absent') AS jours_absents,
       ROUND(100.0 * COUNT(*) FILTER (WHERE type_presence = 'present') / NULLIF(COUNT(*),0), 2) AS taux_presence
FROM presences_eleves
GROUP BY eleve_id;

-- ============================================================================
-- DONNEES DE REFERENCE (SEED DATA)
-- ============================================================================

-- Roles (Module 1)
INSERT INTO roles (code, libelle, description) VALUES
('ADMIN', 'Administrateur', 'Administrateur systeme, acces complet'),
('DIRECTEUR', 'Directeur', 'Direction de l''etablissement'),
('CENSEUR', 'Censeur', 'Responsable pedagogique'),
('SURVEILLANT_GENERAL', 'Surveillant General', 'Discipline et vie scolaire'),
('ECONOME', 'Econome', 'Gestion economique'),
('COMPTABLE', 'Comptable', 'Comptabilite et finances'),
('SECRETAIRE', 'Secretaire', 'Secretariat administratif'),
('PROFESSEUR', 'Professeur', 'Enseignant'),
('ELEVE', 'Eleve', 'Apprenant'),
('PARENT', 'Parent', 'Parent ou tuteur d''eleve'),
('BIBLIOTHECAIRE', 'Bibliothecaire', 'Gestion de la bibliotheque'),
('INFIRMIER', 'Infirmier', 'Personnel medical'),
('CHAUFFEUR', 'Chauffeur', 'Conducteur de bus scolaire'),
('PERSONNEL_ADMIN', 'Personnel administratif', 'Personnel administratif divers'),
('SECURITE', 'Personnel de securite', 'Agent de securite');

-- Types d'evaluation (Module 8)
INSERT INTO types_evaluation (code, libelle, ponderation) VALUES
('DEVOIR', 'Devoir', 1.0),
('SEQUENCE', 'Sequence', 1.0),
('COMPOSITION', 'Composition', 2.0),
('EXAMEN', 'Examen', 2.0),
('PROJET', 'Projet', 1.5),
('TP', 'Travaux pratiques', 1.0);

-- Types de sanction (Module 13)
INSERT INTO types_sanction (code, libelle, gravite) VALUES
('avertissement', 'Avertissement', 1),
('blame', 'Blame', 2),
('exclusion_temp', 'Exclusion temporaire', 3),
('exclusion_def', 'Exclusion definitive', 4);

-- Moyens de paiement (Module 14)
INSERT INTO moyens_paiement (code) VALUES
('orange_money'), ('mtn_momo'), ('carte_bancaire'), ('virement'), ('especes');

-- ---- Exemple d'un etablissement complet pour demo/tests ----
-- Aucun id n'est jamais code en dur : chaque insertion recupere la cle
-- generee (BIGSERIAL) via une sous-requete sur une colonne naturelle
-- (nom, code, libelle...), donc ce bloc est rejouable a l'identique
-- quel que soit l'ordre d'execution ou les donnees deja presentes.

INSERT INTO etablissements (nom, sigle, pays, ville, type_etablissement, plan_abonnement)
VALUES ('Lycee Bilingue de la Reussite', 'LBR', 'Cameroun', 'Douala', 'lycee', 'premium');

INSERT INTO annees_scolaires (etablissement_id, libelle, date_debut, date_fin, est_active)
SELECT id, '2025-2026', '2025-09-01', '2026-06-30', true
FROM etablissements WHERE sigle = 'LBR';

INSERT INTO periodes (annee_scolaire_id, type_periode, numero, libelle, date_debut, date_fin)
SELECT a.id, v.type_periode, v.numero, v.libelle, v.date_debut, v.date_fin
FROM annees_scolaires a
JOIN etablissements e ON e.id = a.etablissement_id AND e.sigle = 'LBR'
CROSS JOIN (VALUES
    ('trimestre', 1, '1er Trimestre', '2025-09-01'::date, '2025-12-20'::date),
    ('trimestre', 2, '2eme Trimestre', '2026-01-05'::date, '2026-03-27'::date),
    ('trimestre', 3, '3eme Trimestre', '2026-04-06'::date, '2026-06-30'::date)
) AS v(type_periode, numero, libelle, date_debut, date_fin)
WHERE a.libelle = '2025-2026';

INSERT INTO niveaux (etablissement_id, code, libelle, ordre, cycle)
SELECT e.id, v.code, v.libelle, v.ordre, v.cycle
FROM etablissements e
CROSS JOIN (VALUES
    ('6e',   'Sixieme',   1, 'premier_cycle'),
    ('5e',   'Cinquieme', 2, 'premier_cycle'),
    ('4e',   'Quatrieme', 3, 'premier_cycle'),
    ('3e',   'Troisieme', 4, 'premier_cycle'),
    ('2nde', 'Seconde',   5, 'second_cycle'),
    ('1ere', 'Premiere',  6, 'second_cycle'),
    ('Tle',  'Terminale', 7, 'second_cycle')
) AS v(code, libelle, ordre, cycle)
WHERE e.sigle = 'LBR';

INSERT INTO series (etablissement_id, code, libelle)
SELECT e.id, v.code, v.libelle
FROM etablissements e
CROSS JOIN (VALUES
    ('A',   'Serie A (Litteraire)'),
    ('C',   'Serie C (Mathematiques et Sciences physiques)'),
    ('D',   'Serie D (Sciences de la vie et de la terre)'),
    ('ESP', 'Espagnol LV2'),
    ('ALL', 'Allemand LV2'),
    ('ITA', 'Italien LV2'),
    ('CHN', 'Chinois LV2')
) AS v(code, libelle)
WHERE e.sigle = 'LBR';

INSERT INTO matieres (etablissement_id, code, libelle, couleur)
SELECT e.id, v.code, v.libelle, v.couleur
FROM etablissements e
CROSS JOIN (VALUES
    ('MATH',  'Mathematiques', '#e74c3c'),
    ('SVT',   'Sciences de la Vie et de la Terre', '#27ae60'),
    ('PHYS',  'Physique-Chimie', '#2980b9'),
    ('EPS',   'Education Physique et Sportive', '#f39c12'),
    ('FR',    'Francais', '#8e44ad'),
    ('PHILO', 'Philosophie', '#34495e')
) AS v(code, libelle, couleur)
WHERE e.sigle = 'LBR';

-- ============================================================================
-- FIN DU SCRIPT
-- Notes : ce schema couvre les 30 modules du cahier des charges EDUCORE.
-- Pour la production, ajouter : partitionnement des tables volumineuses
-- (journal_audit, notifications, positions_gps), politiques RLS PostgreSQL
-- par etablissement_id pour renforcer l'isolation multi-tenant, et des
-- triggers pour la mise a jour automatique de updated_at.
-- ============================================================================
