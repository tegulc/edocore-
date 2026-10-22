-- ============================================================================
-- STRUCTURE DU SYSTÈME ÉDUCATIF CAMEROUNAIS
-- ============================================================================
-- Cette migration ajoute la structure complète du système éducatif camerounais
-- conformément aux spécifications du MINESEC
-- ============================================================================

-- 0. Permettre des données de référence globales (etablissement_id NULL)
ALTER TABLE niveaux ALTER COLUMN etablissement_id DROP NOT NULL;

-- 1. Table des sous-systèmes éducatifs
CREATE TABLE sous_systemes (
    id                  BIGSERIAL PRIMARY KEY,
    code                VARCHAR(20) NOT NULL UNIQUE, -- FRANCOPHONE, ANGLOPHONE, BILINGUE
    libelle             VARCHAR(50) NOT NULL,
    description         TEXT
);

-- 2. Table des cycles (1er cycle, 2nd cycle)
CREATE TABLE cycles (
    id                  BIGSERIAL PRIMARY KEY,
    code                VARCHAR(20) NOT NULL UNIQUE, -- PREMIER_CYCLE, SECOND_CYCLE
    libelle             VARCHAR(50) NOT NULL,
    description         TEXT,
    duree_annees        SMALLINT NOT NULL
);

-- 3. Amélioration de la table series pour inclure les sous-séries
ALTER TABLE series ALTER COLUMN etablissement_id DROP NOT NULL; -- Permettre des séries de référence globales
ALTER TABLE series ADD COLUMN IF NOT EXISTS sous_serie_code VARCHAR(20); -- A1, A2, A3, etc.
ALTER TABLE series ADD COLUMN IF NOT EXISTS serie_parent_id BIGINT REFERENCES series(id);
ALTER TABLE series ADD COLUMN IF NOT EXISTS sous_systeme_id BIGINT REFERENCES sous_systemes(id);
ALTER TABLE series ADD COLUMN IF NOT EXISTS description TEXT;
ALTER TABLE series ADD COLUMN IF NOT EXISTS orientation VARCHAR(100); -- LITTERAIRE, SCIENTIFIQUE, TECHNOLOGIQUE, BILINGUE

-- 4. Amélioration de la table matieres pour inclure les types
ALTER TABLE matieres ALTER COLUMN etablissement_id DROP NOT NULL; -- Permettre des matières de référence globales
ALTER TABLE matieres ADD COLUMN IF NOT EXISTS type_matiere VARCHAR(30); -- GENERALE, LANGUE_VIVANTE, OPTION, SPECIALITE
ALTER TABLE matieres ADD COLUMN IF NOT EXISTS langue_vivante_niveau SMALLINT; -- 1, 2, 3 pour LV1, LV2, LV3
ALTER TABLE matieres ADD COLUMN IF NOT EXISTS is_option BOOLEAN DEFAULT FALSE;
ALTER TABLE matieres ADD COLUMN IF NOT EXISTS description TEXT;

-- 5. Table de configuration des matières par niveau et série
CREATE TABLE matiere_niveau_serie (
    id                  BIGSERIAL PRIMARY KEY,
    etablissement_id    BIGINT NOT NULL REFERENCES etablissements(id) ON DELETE CASCADE,
    matiere_id          BIGINT NOT NULL REFERENCES matieres(id) ON DELETE CASCADE,
    niveau_id           BIGINT NOT NULL REFERENCES niveaux(id) ON DELETE CASCADE,
    serie_id            BIGINT REFERENCES series(id) ON DELETE CASCADE, -- NULL si matière commune à toutes les séries
    coefficient         NUMERIC(4,2) NOT NULL DEFAULT 1,
    volume_horaire_hebdo NUMERIC(4,2) NOT NULL DEFAULT 1,
    est_obligatoire     BOOLEAN NOT NULL DEFAULT TRUE
);

-- Créer un index unique avec expression pour gérer les valeurs NULL
CREATE UNIQUE INDEX idx_matiere_niveau_serie_unique 
ON matiere_niveau_serie (etablissement_id, matiere_id, niveau_id, COALESCE(serie_id, 0));

-- 6. Table des langues/options disponibles par établissement
CREATE TABLE etablissement_langue_option (
    id                  BIGSERIAL PRIMARY KEY,
    etablissement_id    BIGINT NOT NULL REFERENCES etablissements(id) ON DELETE CASCADE,
    matiere_id          BIGINT NOT NULL REFERENCES matieres(id) ON DELETE CASCADE,
    type_offre          VARCHAR(20) NOT NULL, -- LANGUE_VIVANTE, OPTION
    niveau_min_id       BIGINT REFERENCES niveaux(id), -- NULL si disponible dès la 6e
    UNIQUE (etablissement_id, matiere_id, type_offre)
);

-- ============================================================================
-- DONNÉES PAR DÉFAUT - SOUS-SYSTÈMES
-- ============================================================================
INSERT INTO sous_systemes (code, libelle, description) VALUES
('FRANCOPHONE', 'Francophone', 'Sous-système éducatif francophone du Cameroun'),
('ANGLOPHONE', 'Anglophone', 'Sous-système éducatif anglophone du Cameroun'),
('BILINGUE', 'Bilingue', 'Système bilingue français-anglais');

-- ============================================================================
-- DONNÉES PAR DÉFAUT - CYCLES
-- ============================================================================
INSERT INTO cycles (code, libelle, description, duree_annees) VALUES
('PREMIER_CYCLE', '1er Cycle', 'Cycle d''orientation (6e, 5e, 4e, 3e)', 4),
('SECOND_CYCLE', '2nd Cycle', 'Cycle de spécialisation (2nde, 1ère, Terminale)', 3);

-- ============================================================================
-- DONNÉES PAR DÉFAUT - NIVEAUX (7 niveaux principaux)
-- ============================================================================
-- Ces données seront insérées par établissement via le service d'initialisation
-- Voici les codes standards :
-- 6e, 5e, 4e, 3e, 2nde, 1ere, Tle

-- ============================================================================
-- DONNÉES PAR DÉFAUT - SÉRIES FRANCOPHONES
-- ============================================================================
-- Série A - Littéraire
INSERT INTO series (code, libelle, sous_systeme_id, orientation, description) VALUES
('A', 'Série A - Littéraire', (SELECT id FROM sous_systemes WHERE code = 'FRANCOPHONE'), 'LITTERAIRE', 'Série littéraire du second cycle francophone');

-- Sous-séries A
INSERT INTO series (code, sous_serie_code, libelle, serie_parent_id, sous_systeme_id, orientation, description) VALUES
('A1', 'A1', 'Lettres + Latin + Grec', (SELECT id FROM series WHERE code = 'A' AND sous_systeme_id = (SELECT id FROM sous_systemes WHERE code = 'FRANCOPHONE')), (SELECT id FROM sous_systemes WHERE code = 'FRANCOPHONE'), 'LITTERAIRE', 'Lettres avec Latin et Grec'),
('A2', 'A2', 'Lettres + Latin + LV2', (SELECT id FROM series WHERE code = 'A' AND sous_systeme_id = (SELECT id FROM sous_systemes WHERE code = 'FRANCOPHONE')), (SELECT id FROM sous_systemes WHERE code = 'FRANCOPHONE'), 'LITTERAIRE', 'Lettres avec Latin et Langue Vivante II'),
('A3', 'A3', 'Lettres + Latin', (SELECT id FROM series WHERE code = 'A' AND sous_systeme_id = (SELECT id FROM sous_systemes WHERE code = 'FRANCOPHONE')), (SELECT id FROM sous_systemes WHERE code = 'FRANCOPHONE'), 'LITTERAIRE', 'Lettres avec Latin uniquement'),
('A4', 'A4', 'Lettres + LV2 + Philosophie', (SELECT id FROM series WHERE code = 'A' AND sous_systeme_id = (SELECT id FROM sous_systemes WHERE code = 'FRANCOPHONE')), (SELECT id FROM sous_systemes WHERE code = 'FRANCOPHONE'), 'LITTERAIRE', 'Lettres avec LV2 et Philosophie'),
('A5', 'A5', 'LV2 + LV3', (SELECT id FROM series WHERE code = 'A' AND sous_systeme_id = (SELECT id FROM sous_systemes WHERE code = 'FRANCOPHONE')), (SELECT id FROM sous_systemes WHERE code = 'FRANCOPHONE'), 'LITTERAIRE', 'Langue Vivante II + Langue Vivante III'),
('AC', 'AC', 'Art Cinématographique', (SELECT id FROM series WHERE code = 'A' AND sous_systeme_id = (SELECT id FROM sous_systemes WHERE code = 'FRANCOPHONE')), (SELECT id FROM sous_systemes WHERE code = 'FRANCOPHONE'), 'LITTERAIRE', 'Option Art cinématographique');

-- Série C - Scientifique (Maths + Physique)
INSERT INTO series (code, libelle, sous_systeme_id, orientation, description) VALUES
('C', 'Série C - Scientifique', (SELECT id FROM sous_systemes WHERE code = 'FRANCOPHONE'), 'SCIENTIFIQUE', 'Série scientifique orientée Mathématiques et Physique');

-- Série D - Scientifique (Maths + SVT)
INSERT INTO series (code, libelle, sous_systeme_id, orientation, description) VALUES
('D', 'Série D - Scientifique', (SELECT id FROM sous_systemes WHERE code = 'FRANCOPHONE'), 'SCIENTIFIQUE', 'Série scientifique orientée Mathématiques et SVT');

-- Série TI - Technologie de l'Information
INSERT INTO series (code, libelle, sous_systeme_id, orientation, description) VALUES
('TI', 'Série TI - Technologie', (SELECT id FROM sous_systemes WHERE code = 'FRANCOPHONE'), 'TECHNOLOGIQUE', 'Série technologique en Informatique');

-- Série BIL - Bilingue
INSERT INTO series (code, libelle, sous_systeme_id, orientation, description) VALUES
('BIL', 'Série BIL - Bilingue', (SELECT id FROM sous_systemes WHERE code = 'FRANCOPHONE'), 'BILINGUE', 'Série bilingue Français-Anglais');

-- ============================================================================
-- DONNÉES PAR DÉFAUT - SÉRIES ANGLOPHONES
-- ============================================================================
-- Séries artistiques anglophones
INSERT INTO series (code, libelle, sous_systeme_id, orientation, description) VALUES
('A', 'Arts - Anglophone', (SELECT id FROM sous_systemes WHERE code = 'ANGLOPHONE'), 'LITTERAIRE', 'Série Arts du sous-système anglophone');

INSERT INTO series (code, sous_serie_code, libelle, serie_parent_id, sous_systeme_id, orientation, description) VALUES
('A1', 'A1', 'Arts A1', (SELECT id FROM series WHERE code = 'A' AND sous_systeme_id = (SELECT id FROM sous_systemes WHERE code = 'ANGLOPHONE')), (SELECT id FROM sous_systemes WHERE code = 'ANGLOPHONE'), 'LITTERAIRE', 'Arts A1'),
('A2', 'A2', 'Arts A2', (SELECT id FROM series WHERE code = 'A' AND sous_systeme_id = (SELECT id FROM sous_systemes WHERE code = 'ANGLOPHONE')), (SELECT id FROM sous_systemes WHERE code = 'ANGLOPHONE'), 'LITTERAIRE', 'Arts A2'),
('A3', 'A3', 'Arts A3', (SELECT id FROM series WHERE code = 'A' AND sous_systeme_id = (SELECT id FROM sous_systemes WHERE code = 'ANGLOPHONE')), (SELECT id FROM sous_systemes WHERE code = 'ANGLOPHONE'), 'LITTERAIRE', 'Arts A3'),
('A4', 'A4', 'Arts A4', (SELECT id FROM series WHERE code = 'A' AND sous_systeme_id = (SELECT id FROM sous_systemes WHERE code = 'ANGLOPHONE')), (SELECT id FROM sous_systemes WHERE code = 'ANGLOPHONE'), 'LITTERAIRE', 'Arts A4'),
('A5', 'A5', 'Arts A5', (SELECT id FROM series WHERE code = 'A' AND sous_systeme_id = (SELECT id FROM sous_systemes WHERE code = 'ANGLOPHONE')), (SELECT id FROM sous_systemes WHERE code = 'ANGLOPHONE'), 'LITTERAIRE', 'Arts A5'),
('A6', 'A6', 'Arts A6', (SELECT id FROM series WHERE code = 'A' AND sous_systeme_id = (SELECT id FROM sous_systemes WHERE code = 'ANGLOPHONE')), (SELECT id FROM sous_systemes WHERE code = 'ANGLOPHONE'), 'LITTERAIRE', 'Arts A6'),
('A7', 'A7', 'Arts A7', (SELECT id FROM series WHERE code = 'A' AND sous_systeme_id = (SELECT id FROM sous_systemes WHERE code = 'ANGLOPHONE')), (SELECT id FROM sous_systemes WHERE code = 'ANGLOPHONE'), 'LITTERAIRE', 'Arts A7'),
('A8', 'A8', 'Arts A8', (SELECT id FROM series WHERE code = 'A' AND sous_systeme_id = (SELECT id FROM sous_systemes WHERE code = 'ANGLOPHONE')), (SELECT id FROM sous_systemes WHERE code = 'ANGLOPHONE'), 'LITTERAIRE', 'Arts A8');

-- Séries scientifiques anglophones
INSERT INTO series (code, libelle, sous_systeme_id, orientation, description) VALUES
('S', 'Sciences - Anglophone', (SELECT id FROM sous_systemes WHERE code = 'ANGLOPHONE'), 'SCIENTIFIQUE', 'Série Sciences du sous-système anglophone');

INSERT INTO series (code, sous_serie_code, libelle, serie_parent_id, sous_systeme_id, orientation, description) VALUES
('S1', 'S1', 'Chemistry + Physics + Mathematics', (SELECT id FROM series WHERE code = 'S' AND sous_systeme_id = (SELECT id FROM sous_systemes WHERE code = 'ANGLOPHONE')), (SELECT id FROM sous_systemes WHERE code = 'ANGLOPHONE'), 'SCIENTIFIQUE', 'Chimie + Physique + Mathématiques'),
('S2', 'S2', 'Chemistry + Physics + Biology', (SELECT id FROM series WHERE code = 'S' AND sous_systeme_id = (SELECT id FROM sous_systemes WHERE code = 'ANGLOPHONE')), (SELECT id FROM sous_systemes WHERE code = 'ANGLOPHONE'), 'SCIENTIFIQUE', 'Chimie + Physique + Biologie'),
('S3', 'S3', 'Biology + Chemistry + Physics', (SELECT id FROM series WHERE code = 'S' AND sous_systeme_id = (SELECT id FROM sous_systemes WHERE code = 'ANGLOPHONE')), (SELECT id FROM sous_systemes WHERE code = 'ANGLOPHONE'), 'SCIENTIFIQUE', 'Biologie + Chimie + Physique'),
('S4', 'S4', 'Biology + Chemistry + Geology/Geography', (SELECT id FROM series WHERE code = 'S' AND sous_systeme_id = (SELECT id FROM sous_systemes WHERE code = 'ANGLOPHONE')), (SELECT id FROM sous_systemes WHERE code = 'ANGLOPHONE'), 'SCIENTIFIQUE', 'Biologie + Chimie + Géologie/Géographie');

-- ============================================================================
-- DONNÉES PAR DÉFAUT - MATIÈRES
-- ============================================================================
INSERT INTO matieres (code, libelle, type_matiere, description) VALUES
-- Matières générales
('FRA', 'Français', 'GENERALE', 'Langue et littérature française'),
('LIT', 'Littérature', 'GENERALE', 'Littérature française'),
('ANG', 'Anglais', 'GENERALE', 'Langue anglaise'),
('MATH', 'Mathématiques', 'GENERALE', 'Mathématiques générales'),
('HIST', 'Histoire', 'GENERALE', 'Histoire'),
('GEO', 'Géographie', 'GENERALE', 'Géographie'),
('HISTGEO', 'Histoire-Géographie', 'GENERALE', 'Histoire et Géographie'),
('ECM', 'Éducation à la citoyenneté et à la morale', 'GENERALE', 'Éducation civique et morale'),
('SCI', 'Sciences', 'GENERALE', 'Sciences générales'),
('SVT', 'Sciences de la Vie et de la Terre', 'GENERALE', 'Biologie et géologie'),
('PHY', 'Physique', 'GENERALE', 'Physique'),
('CHIM', 'Chimie', 'GENERALE', 'Chimie'),
('PHYCHIM', 'Physique-Chimie', 'GENERALE', 'Physique et Chimie'),
('INFO', 'Informatique', 'GENERALE', 'Informatique'),
('PHILO', 'Philosophie', 'GENERALE', 'Philosophie'),
('EPS', 'Éducation physique et sportive', 'GENERALE', 'Éducation sportive'),
('EDUART', 'Éducation artistique', 'GENERALE', 'Éducation artistique'),
('MUS', 'Musique', 'GENERALE', 'Éducation musicale'),
('ARTPLA', 'Arts plastiques', 'GENERALE', 'Arts plastiques');

-- Langues vivantes
INSERT INTO matieres (code, libelle, type_matiere, langue_vivante_niveau, is_option, description) VALUES
('ALL', 'Allemand', 'LANGUE_VIVANTE', 2, TRUE, 'Langue allemande'),
('ESP', 'Espagnol', 'LANGUE_VIVANTE', 2, TRUE, 'Langue espagnole'),
('ARA', 'Arabe', 'LANGUE_VIVANTE', 2, TRUE, 'Langue arabe'),
('ITA', 'Italien', 'LANGUE_VIVANTE', 3, TRUE, 'Langue italienne'),
('RUS', 'Russe', 'LANGUE_VIVANTE', 3, TRUE, 'Langue russe'),
('CHIN', 'Chinois', 'LANGUE_VIVANTE', 3, TRUE, 'Langue chinoise');

-- Langues anciennes (options)
INSERT INTO matieres (code, libelle, type_matiere, is_option, description) VALUES
('LAT', 'Latin', 'OPTION', TRUE, 'Langue et culture latines'),
('GREC', 'Grec', 'OPTION', TRUE, 'Langue et culture grecques anciennes');

-- ============================================================================
-- INDEX POUR LES NOUVELLES TABLES
-- ============================================================================
CREATE INDEX idx_matiere_niveau_serie_etablissement ON matiere_niveau_serie(etablissement_id);
CREATE INDEX idx_matiere_niveau_serie_niveau ON matiere_niveau_serie(niveau_id);
CREATE INDEX idx_matiere_niveau_serie_serie ON matiere_niveau_serie(serie_id);
CREATE INDEX idx_matiere_niveau_serie_matiere ON matiere_niveau_serie(matiere_id);
CREATE INDEX idx_etablissement_langue_option_etablissement ON etablissement_langue_option(etablissement_id);
CREATE INDEX idx_series_sous_systeme ON series(sous_systeme_id);
CREATE INDEX idx_series_serie_parent ON series(serie_parent_id);
CREATE INDEX idx_matieres_type ON matieres(type_matiere);
