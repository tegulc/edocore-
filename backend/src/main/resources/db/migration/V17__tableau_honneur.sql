-- Création de la table tableaux_honneur
CREATE TABLE tableaux_honneur (
    id BIGSERIAL PRIMARY KEY,
    etablissement_id BIGINT NOT NULL,
    eleve_id BIGINT NOT NULL,
    classe_id BIGINT NOT NULL,
    periode_id BIGINT NOT NULL,
    moyenne_generale DECIMAL(5,2) NOT NULL,
    rang INTEGER,
    mention VARCHAR(50),
    bulletin_id BIGINT,
    genere_le TIMESTAMP NOT NULL,
    publie_le TIMESTAMP NOT NULL,
    statut VARCHAR(20) DEFAULT 'actif',
    CONSTRAINT unique_eleve_periode UNIQUE (eleve_id, periode_id),
    CONSTRAINT fk_tableau_honneur_bulletin FOREIGN KEY (bulletin_id) REFERENCES bulletins(id) ON DELETE CASCADE,
    CONSTRAINT fk_tableau_honneur_eleve FOREIGN KEY (eleve_id) REFERENCES eleves(id) ON DELETE CASCADE,
    CONSTRAINT fk_tableau_honneur_classe FOREIGN KEY (classe_id) REFERENCES classes(id) ON DELETE CASCADE,
    CONSTRAINT fk_tableau_honneur_periode FOREIGN KEY (periode_id) REFERENCES periodes(id) ON DELETE CASCADE
);

CREATE INDEX idx_tableau_honneur_classe_periode ON tableaux_honneur(classe_id, periode_id);
CREATE INDEX idx_tableau_honneur_etablissement_periode ON tableaux_honneur(etablissement_id, periode_id);
