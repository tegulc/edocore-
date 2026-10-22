DO $$
DECLARE
    tbl record;
    col record;
    current_count integer;
    missing_count integer;
    insert_cols text[] := ARRAY[]::text[];
    insert_vals text[] := ARRAY[]::text[];
    value_expr text;
    i integer;
    ref_table text;
    ref_id bigint;
    col_name text;
BEGIN
    FOR tbl IN
        SELECT table_name
        FROM information_schema.tables
        WHERE table_schema = 'public'
          AND table_type = 'BASE TABLE'
          AND table_name <> 'flyway_schema_history'
        ORDER BY table_name
    LOOP
        EXECUTE format('SELECT COUNT(*) FROM public.%I', tbl.table_name) INTO current_count;
        missing_count := 4 - current_count;

        IF missing_count <= 0 THEN
            CONTINUE;
        END IF;

        FOR i IN 1..missing_count LOOP
            insert_cols := ARRAY[]::text[];
            insert_vals := ARRAY[]::text[];

            FOR col IN
                SELECT column_name, data_type
                FROM information_schema.columns
                WHERE table_schema = 'public'
                  AND table_name = tbl.table_name
                ORDER BY ordinal_position
            LOOP
                col_name := lower(col.column_name);

                IF col.column_name = 'id' THEN
                    CONTINUE;
                END IF;

                value_expr := 'NULL';

                IF col_name = 'etablissement_id' THEN
                    SELECT id INTO ref_id FROM public.etablissements ORDER BY id LIMIT 1;
                    value_expr := ref_id::text;
                ELSIF col_name = 'annee_scolaire_id' THEN
                    SELECT id INTO ref_id FROM public.annees_scolaires ORDER BY id LIMIT 1;
                    value_expr := ref_id::text;
                ELSIF col_name = 'periode_id' THEN
                    SELECT id INTO ref_id FROM public.periodes ORDER BY id LIMIT 1;
                    value_expr := ref_id::text;
                ELSIF col_name = 'niveau_id' THEN
                    SELECT id INTO ref_id FROM public.niveaux ORDER BY id LIMIT 1;
                    value_expr := ref_id::text;
                ELSIF col_name = 'serie_id' THEN
                    SELECT id INTO ref_id FROM public.series ORDER BY id LIMIT 1;
                    value_expr := ref_id::text;
                ELSIF col_name = 'salle_id' THEN
                    SELECT id INTO ref_id FROM public.salles ORDER BY id LIMIT 1;
                    value_expr := ref_id::text;
                ELSIF col_name = 'matiere_id' THEN
                    SELECT id INTO ref_id FROM public.matieres ORDER BY id LIMIT 1;
                    value_expr := ref_id::text;
                ELSIF col_name = 'classe_id' THEN
                    SELECT id INTO ref_id FROM public.classes ORDER BY id LIMIT 1;
                    value_expr := ref_id::text;
                ELSIF col_name = 'eleve_id' THEN
                    SELECT id INTO ref_id FROM public.eleves ORDER BY id LIMIT 1;
                    value_expr := ref_id::text;
                ELSIF col_name = 'utilisateur_id' THEN
                    SELECT id INTO ref_id FROM public.utilisateurs ORDER BY id LIMIT 1;
                    value_expr := ref_id::text;
                ELSIF col_name = 'enseignant_id' THEN
                    SELECT id INTO ref_id FROM public.enseignants ORDER BY id LIMIT 1;
                    value_expr := ref_id::text;
                ELSIF col_name = 'parent_id' THEN
                    SELECT id INTO ref_id FROM public.parents ORDER BY id LIMIT 1;
                    value_expr := ref_id::text;
                ELSIF col_name = 'role_id' THEN
                    SELECT id INTO ref_id FROM public.roles ORDER BY id LIMIT 1;
                    value_expr := ref_id::text;
                ELSIF col_name = 'type_evaluation_id' THEN
                    SELECT id INTO ref_id FROM public.types_evaluation ORDER BY id LIMIT 1;
                    value_expr := ref_id::text;
                ELSIF col_name = 'type_sanction_id' THEN
                    SELECT id INTO ref_id FROM public.types_sanction ORDER BY id LIMIT 1;
                    value_expr := ref_id::text;
                ELSIF col_name = 'moyen_paiement_id' THEN
                    SELECT id INTO ref_id FROM public.moyens_paiement ORDER BY id LIMIT 1;
                    value_expr := ref_id::text;
                ELSIF col_name = 'paiement_id' THEN
                    SELECT id INTO ref_id FROM public.paiements ORDER BY id LIMIT 1;
                    value_expr := ref_id::text;
                ELSIF col_name = 'devoir_id' THEN
                    SELECT id INTO ref_id FROM public.devoirs ORDER BY id LIMIT 1;
                    value_expr := ref_id::text;
                ELSIF col_name = 'evaluation_id' THEN
                    SELECT id INTO ref_id FROM public.evaluations ORDER BY id LIMIT 1;
                    value_expr := ref_id::text;
                ELSIF col_name = 'bulletin_id' THEN
                    SELECT id INTO ref_id FROM public.bulletins ORDER BY id LIMIT 1;
                    value_expr := ref_id::text;
                ELSIF col_name = 'conversation_id' THEN
                    SELECT id INTO ref_id FROM public.conversations ORDER BY id LIMIT 1;
                    value_expr := ref_id::text;
                ELSIF col_name = 'reunion_id' THEN
                    SELECT id INTO ref_id FROM public.reunions ORDER BY id LIMIT 1;
                    value_expr := ref_id::text;
                ELSIF col_name = 'api_client_id' THEN
                    SELECT id INTO ref_id FROM public.api_clients ORDER BY id LIMIT 1;
                    value_expr := ref_id::text;
                ELSIF col_name = 'compte_id' THEN
                    SELECT id INTO ref_id FROM public.comptes_comptables ORDER BY id LIMIT 1;
                    value_expr := ref_id::text;
                ELSIF col_name = 'livre_id' THEN
                    SELECT id INTO ref_id FROM public.livres ORDER BY id LIMIT 1;
                    value_expr := ref_id::text;
                ELSIF col_name = 'bus_id' THEN
                    SELECT id INTO ref_id FROM public.bus ORDER BY id LIMIT 1;
                    value_expr := ref_id::text;
                ELSIF col_name = 'itineraire_id' THEN
                    SELECT id INTO ref_id FROM public.itineraires ORDER BY id LIMIT 1;
                    value_expr := ref_id::text;
                ELSIF col_name = 'classe_matiere_id' THEN
                    SELECT id INTO ref_id FROM public.classe_matiere ORDER BY id LIMIT 1;
                    value_expr := ref_id::text;
                ELSIF col_name = 'activation_id' THEN
                    value_expr := '1';
                ELSIF col_name = 'uuid' THEN
                    value_expr := 'gen_random_uuid()';
                ELSIF col_name = 'mot_de_passe_hash' OR col_name LIKE '%mot_de_passe%' THEN
                    value_expr := 'crypt(''Password123!'', gen_salt(''bf'', 12))';
                ELSIF col_name LIKE '%email%' THEN
                    value_expr := format('''demo_%s_%s@example.com''', tbl.table_name, i);
                ELSIF col_name LIKE '%telephone%' OR col_name LIKE '%phone%' THEN
                    value_expr := format('''+237600000%s''', i);
                ELSIF col_name in ('created_at','updated_at','date_consentement','date_tentative','date_connexion','date_expiration','date_inscription','date_reunion','date_ajout','date_emission','date_paiement','date_soumission','date_evaluation','date_jour','horodatage','date_urgence','date_consultation','envoye_le','lu_le','date_debut','date_fin') OR col_name LIKE 'date_%' OR col_name LIKE '%_date' OR col_name LIKE '%_at' THEN
                    value_expr := 'CURRENT_TIMESTAMP';
                ELSIF col_name LIKE 'heure_%' OR col_name LIKE '%_time' THEN
                    value_expr := '''08:00:00''::time';
                ELSIF col_name IN ('sexe') THEN
                    value_expr := CASE WHEN i % 2 = 0 THEN '''M''' ELSE '''F''' END;
                ELSIF col_name IN ('statut','statut_paiement','statut_envoi','statut_reponse','type_presence','type_contrat','type_document','type_periode','type_conversation','type_etablissement','plan_abonnement','cycle','langue_defaut','langue_preferee') THEN
                    value_expr := '''actif''';
                ELSIF col_name IN ('pays') THEN
                    value_expr := '''Cameroun''';
                ELSIF col_name IN ('ville') THEN
                    value_expr := '''Douala''';
                ELSIF col_name IN ('adresse') THEN
                    value_expr := '''Rue Demo 1''';
                ELSIF col_name in ('code','matricule') THEN
                    value_expr := format('''%s-%s''', upper(tbl.table_name), i);
                ELSIF col_name IN ('libelle','titre','nom','prenom','description','motif','question','reponse','contenu','categorie','type_chauffeur','type_conge','fonction','departement','specialite') THEN
                    value_expr := format('''%s_%s''', replace(col.column_name, '_', '-'), i);
                ELSIF col.data_type = 'boolean' THEN
                    value_expr := CASE WHEN i % 2 = 0 THEN 'TRUE' ELSE 'FALSE' END;
                ELSIF col.data_type IN ('smallint','integer','bigint') THEN
                    value_expr := (i * 10)::text;
                ELSIF col.data_type IN ('numeric','decimal','real','double precision') THEN
                    value_expr := (100.0 + i)::text;
                ELSIF col.data_type = 'json' OR col.data_type = 'jsonb' THEN
                    value_expr := '''{"demo": true}''::jsonb';
                ELSIF col.data_type IN ('character varying','text') THEN
                    value_expr := format('''%s_%s''', lower(tbl.table_name), i);
                ELSE
                    value_expr := format('''%s_%s''', lower(tbl.table_name), i);
                END IF;

                insert_cols := insert_cols || ARRAY[format('%I', col.column_name)];
                insert_vals := insert_vals || ARRAY[value_expr];
            END LOOP;

            EXECUTE format(
                'INSERT INTO public.%I (%s) VALUES (%s) ON CONFLICT DO NOTHING',
                tbl.table_name,
                array_to_string(insert_cols, ', '),
                array_to_string(insert_vals, ', ')
            );
        END LOOP;
    END LOOP;
END $$;

SELECT table_name, count(*) AS nb_lignes
FROM information_schema.tables
WHERE table_schema='public' AND table_type='BASE TABLE' AND table_name <> 'flyway_schema_history'
GROUP BY table_name
ORDER BY table_name;
