DO $$
DECLARE
    tbl record;
    col record;
    col_name text;
    col_names text[] := ARRAY[]::text[];
    current_count integer;
    missing_count integer;
    insert_values text[] := ARRAY[]::text[];
    value_expr text;
    ref_id bigint;
    i integer;
    tbl_name text;
    ordered text[];
    q text;
    char_max_len integer;
BEGIN
    SELECT array_agg(table_name ORDER BY max_depth, table_name)
      INTO ordered
    FROM (
        WITH RECURSIVE fk_graph AS (
            SELECT t.table_name, 0 AS depth
            FROM information_schema.tables t
            WHERE t.table_schema='public' AND t.table_type='BASE TABLE' AND t.table_name <> 'flyway_schema_history'
            UNION
            SELECT kcu.table_name, fg.depth + 1
            FROM fk_graph fg
            JOIN information_schema.key_column_usage kcu ON kcu.table_schema = 'public'
            JOIN information_schema.referential_constraints rc ON rc.constraint_name = kcu.constraint_name AND rc.constraint_schema = kcu.table_schema
            JOIN information_schema.constraint_column_usage ccu ON ccu.constraint_name = rc.unique_constraint_name AND ccu.table_schema = rc.unique_constraint_schema
            WHERE ccu.table_name = fg.table_name
        )
        SELECT table_name, MAX(depth) AS max_depth FROM fk_graph GROUP BY table_name
    ) s;

    FOREACH tbl_name IN ARRAY ordered LOOP
        EXECUTE format('SELECT COUNT(*) FROM public.%I', tbl_name) INTO current_count;
        missing_count := 4 - current_count;
        IF missing_count <= 0 THEN
            CONTINUE;
        END IF;

        FOR i IN 1..missing_count LOOP
            col_names := ARRAY[]::text[];
            insert_values := ARRAY[]::text[];

            FOR col IN
                SELECT column_name, data_type, character_maximum_length
                FROM information_schema.columns
                WHERE table_schema='public' AND table_name = tbl_name
                ORDER BY ordinal_position
            LOOP
                IF col.column_name = 'id' THEN
                    CONTINUE;
                END IF;

                col_name := lower(col.column_name);
                value_expr := 'NULL';
                char_max_len := col.character_maximum_length;

                -- Foreign keys
                IF col_name = 'etablissement_id' THEN
                    EXECUTE 'SELECT id FROM public.etablissements ORDER BY id LIMIT 1' INTO ref_id;
                    value_expr := ref_id::text;
                ELSIF col_name = 'annee_scolaire_id' THEN
                    EXECUTE 'SELECT id FROM public.annees_scolaires ORDER BY id LIMIT 1' INTO ref_id;
                    value_expr := ref_id::text;
                ELSIF col_name = 'niveau_id' THEN
                    EXECUTE 'SELECT id FROM public.niveaux ORDER BY id LIMIT 1' INTO ref_id;
                    value_expr := ref_id::text;
                ELSIF col_name = 'serie_id' THEN
                    EXECUTE 'SELECT id FROM public.series ORDER BY id LIMIT 1' INTO ref_id;
                    value_expr := ref_id::text;
                ELSIF col_name = 'matiere_id' THEN
                    EXECUTE 'SELECT id FROM public.matieres ORDER BY id LIMIT 1' INTO ref_id;
                    value_expr := ref_id::text;
                ELSIF col_name = 'classe_id' THEN
                    EXECUTE 'SELECT id FROM public.classes ORDER BY id LIMIT 1' INTO ref_id;
                    value_expr := ref_id::text;
                ELSIF col_name = 'eleve_id' THEN
                    EXECUTE 'SELECT id FROM public.eleves ORDER BY id LIMIT 1' INTO ref_id;
                    value_expr := ref_id::text;
                ELSIF col_name = 'utilisateur_id' THEN
                    EXECUTE 'SELECT id FROM public.utilisateurs ORDER BY id LIMIT 1' INTO ref_id;
                    value_expr := ref_id::text;
                ELSIF col_name = 'enseignant_id' THEN
                    EXECUTE 'SELECT id FROM public.enseignants ORDER BY id LIMIT 1' INTO ref_id;
                    value_expr := ref_id::text;
                ELSIF col_name = 'role_id' THEN
                    EXECUTE 'SELECT id FROM public.roles ORDER BY id LIMIT 1' INTO ref_id;
                    value_expr := ref_id::text;
                ELSIF col_name = 'paiement_id' THEN
                    EXECUTE 'SELECT id FROM public.paiements ORDER BY id LIMIT 1' INTO ref_id;
                    value_expr := ref_id::text;
                ELSIF col_name = 'parent_id' THEN
                    EXECUTE 'SELECT id FROM public.parents ORDER BY id LIMIT 1' INTO ref_id;
                    value_expr := ref_id::text;
                ELSIF col_name = 'salle_id' THEN
                    EXECUTE 'SELECT id FROM public.salles ORDER BY id LIMIT 1' INTO ref_id;
                    value_expr := ref_id::text;
                ELSIF col_name = 'moyen_paiement_id' THEN
                    EXECUTE 'SELECT id FROM public.moyens_paiement ORDER BY id LIMIT 1' INTO ref_id;
                    value_expr := ref_id::text;
                ELSIF col_name = 'type_evaluation_id' THEN
                    EXECUTE 'SELECT id FROM public.types_evaluation ORDER BY id LIMIT 1' INTO ref_id;
                    value_expr := ref_id::text;
                ELSIF col_name = 'type_sanction_id' THEN
                    EXECUTE 'SELECT id FROM public.types_sanction ORDER BY id LIMIT 1' INTO ref_id;
                    value_expr := ref_id::text;
                ELSIF col_name = 'periode_id' THEN
                    EXECUTE 'SELECT id FROM public.periodes ORDER BY id LIMIT 1' INTO ref_id;
                    value_expr := ref_id::text;
                ELSIF col_name = 'conversation_id' THEN
                    EXECUTE 'SELECT id FROM public.conversations ORDER BY id LIMIT 1' INTO ref_id;
                    value_expr := ref_id::text;
                ELSIF col_name = 'livre_id' THEN
                    EXECUTE 'SELECT id FROM public.livres ORDER BY id LIMIT 1' INTO ref_id;
                    value_expr := ref_id::text;
                ELSIF col_name = 'devoir_id' THEN
                    EXECUTE 'SELECT id FROM public.devoirs ORDER BY id LIMIT 1' INTO ref_id;
                    value_expr := ref_id::text;
                ELSIF col_name = 'emprunt_id' THEN
                    EXECUTE 'SELECT id FROM public.emprunts ORDER BY id LIMIT 1' INTO ref_id;
                    value_expr := ref_id::text;
                ELSIF col_name = 'itineraire_id' THEN
                    EXECUTE 'SELECT id FROM public.itineraires ORDER BY id LIMIT 1' INTO ref_id;
                    value_expr := ref_id::text;
                ELSIF col_name = 'bus_id' THEN
                    EXECUTE 'SELECT id FROM public.bus ORDER BY id LIMIT 1' INTO ref_id;
                    value_expr := ref_id::text;
                ELSIF col_name = 'arrets_bus_id' THEN
                    EXECUTE 'SELECT id FROM public.arrets_bus ORDER BY id LIMIT 1' INTO ref_id;
                    value_expr := ref_id::text;
                ELSIF col_name = 'permission_id' THEN
                    EXECUTE 'SELECT id FROM public.permissions ORDER BY id LIMIT 1' INTO ref_id;
                    value_expr := ref_id::text;
                ELSIF col_name = 'classe_matiere_id' THEN
                    EXECUTE 'SELECT id FROM public.classe_matiere ORDER BY id LIMIT 1' INTO ref_id;
                    value_expr := ref_id::text;
                ELSIF col_name = 'structures_frais_id' THEN
                    EXECUTE 'SELECT id FROM public.structures_frais ORDER BY id LIMIT 1' INTO ref_id;
                    value_expr := ref_id::text;
                ELSIF col_name = 'tranche_id' THEN
                    EXECUTE 'SELECT id FROM public.tranches_paiement ORDER BY id LIMIT 1' INTO ref_id;
                    value_expr := ref_id::text;
                ELSIF col_name = 'echeancier_id' THEN
                    EXECUTE 'SELECT id FROM public.echeanciers_paiement ORDER BY id LIMIT 1' INTO ref_id;
                    value_expr := ref_id::text;
                ELSIF col_name = 'reunion_id' THEN
                    EXECUTE 'SELECT id FROM public.reunions ORDER BY id LIMIT 1' INTO ref_id;
                    value_expr := ref_id::text;
                ELSIF col_name = 'consultation_id' THEN
                    EXECUTE 'SELECT id FROM public.consultations_medicales ORDER BY id LIMIT 1' INTO ref_id;
                    value_expr := ref_id::text;
                ELSIF col_name = 'devoir_rendu_id' THEN
                    EXECUTE 'SELECT id FROM public.devoirs_rendus ORDER BY id LIMIT 1' INTO ref_id;
                    value_expr := ref_id::text;
                ELSIF col_name = 'evaluation_id' THEN
                    EXECUTE 'SELECT id FROM public.evaluations ORDER BY id LIMIT 1' INTO ref_id;
                    value_expr := ref_id::text;
                ELSIF col_name = 'bulletin_id' THEN
                    EXECUTE 'SELECT id FROM public.bulletins ORDER BY id LIMIT 1' INTO ref_id;
                    value_expr := ref_id::text;
                ELSIF col_name = 'incident_id' THEN
                    EXECUTE 'SELECT id FROM public.incidents_disciplinaires ORDER BY id LIMIT 1' INTO ref_id;
                    value_expr := ref_id::text;
                ELSIF col_name = 'sanction_id' THEN
                    EXECUTE 'SELECT id FROM public.sanctions ORDER BY id LIMIT 1' INTO ref_id;
                    value_expr := ref_id::text;
                -- Special types
                ELSIF col_name = 'uuid' THEN
                    value_expr := 'gen_random_uuid()';
                ELSIF col_name = 'mot_de_passe_hash' OR col_name LIKE '%mot_de_passe%' THEN
                    value_expr := 'crypt(''Password123!'', gen_salt(''bf'', 12))';
                ELSIF col_name LIKE '%email%' THEN
                    value_expr := format('''demo%s@example.com''', SUBSTR(tbl_name || i, 1, COALESCE(char_max_len, 30) - 16));
                ELSIF col_name LIKE '%telephone%' OR col_name LIKE '%phone%' THEN
                    value_expr := format('''+237600000%s''', i);
                ELSIF col_name IN ('created_at','updated_at','date_consentement','date_tentative','date_connexion','date_expiration','date_inscription','date_reunion','date_ajout','date_emission','date_paiement','date_soumission','date_evaluation','date_jour','horodatage','date_urgence','date_consultation','envoye_le','lu_le','date_debut','date_fin','date_naissance','date_embauche','date_publication','date_limite') OR col_name LIKE 'date_%' OR col_name LIKE '%_date' OR col_name LIKE '%_at' THEN
                    value_expr := 'CURRENT_TIMESTAMP';
                ELSIF col_name LIKE 'heure_%' OR col_name LIKE '%_time' THEN
                    value_expr := '''08:00:00''::time';
                ELSIF col_name = 'sexe' THEN
                    value_expr := CASE WHEN i % 2 = 0 THEN '''M''' ELSE '''F''' END;
                ELSIF col_name IN ('statut','statut_paiement','statut_envoi','statut_reponse','type_presence','type_contrat','type_document','type_periode','type_conversation','type_etablissement','plan_abonnement','cycle','langue_defaut','langue_preferee','est_active','etat') THEN
                    value_expr := '''actif''';
                ELSIF col_name = 'pays' THEN
                    value_expr := '''CM''';
                ELSIF col_name = 'ville' THEN
                    value_expr := '''Douala''';
                ELSIF col_name = 'adresse' THEN
                    value_expr := '''Rue 1''';
                ELSIF col_name IN ('code','matricule','sigle') THEN
                    value_expr := format('''%s%s''', SUBSTR(UPPER(tbl_name), 1, COALESCE(char_max_len, 10) - 1), i);
                ELSIF col_name IN ('libelle','titre','nom','prenom','description','motif','question','reponse','contenu','categorie','fonction','departement','specialite','type_chauffeur','type_conge','couleur','devise','fuseau_horaire') THEN
                    value_expr := format('''%s%s''', SUBSTR(col_name, 1, COALESCE(char_max_len, 20) - 2), i);
                ELSIF col_name IN ('logo_url','photo_url','acte_naissance_url','certificat_medical_url','qr_code') THEN
                    value_expr := format('''https://example.com/%s/%s''', SUBSTR(tbl_name, 1, 10), i);
                ELSIF col.data_type = 'boolean' THEN
                    value_expr := CASE WHEN i % 2 = 0 THEN 'TRUE' ELSE 'FALSE' END;
                ELSIF col.data_type IN ('smallint','integer','bigint') THEN
                    value_expr := (i * 10)::text;
                ELSIF col.data_type IN ('numeric','decimal','real','double precision') THEN
                    value_expr := (100.0 + i)::text;
                ELSIF col.data_type IN ('json','jsonb') THEN
                    value_expr := '''{"demo": true}''::jsonb';
                ELSIF col.data_type IN ('character varying','text') THEN
                    value_expr := format('''%s%s''', SUBSTR(tbl_name, 1, COALESCE(char_max_len, 30) - 2), i);
                ELSE
                    value_expr := format('''%s%s''', SUBSTR(tbl_name, 1, COALESCE(char_max_len, 30) - 2), i);
                END IF;

                col_names := col_names || ARRAY[format('%I', col.column_name)];
                insert_values := insert_values || ARRAY[value_expr];
            END LOOP;

            q := format(
                'INSERT INTO public.%I (%s) VALUES (%s) ON CONFLICT DO NOTHING',
                tbl_name,
                array_to_string(col_names, ', '),
                array_to_string(insert_values, ', ')
            );
            EXECUTE q;
        END LOOP;
    END LOOP;
END $$;

SELECT table_name, COUNT(*) AS nb_lignes
FROM information_schema.tables
WHERE table_schema='public' AND table_type='BASE TABLE' AND table_name <> 'flyway_schema_history'
GROUP BY table_name
ORDER BY table_name;
