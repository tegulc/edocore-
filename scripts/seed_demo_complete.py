import os
from collections import defaultdict, deque

import psycopg

DSN = os.environ.get("EDUCORE_DSN", "dbname=educore user=educore password=educore host=localhost port=5432")
TARGET_ROWS = 4


def fetch_all(conn, query, params=()):
    with conn.cursor() as cur:
        cur.execute(query, params)
        return cur.fetchall()


def fetch_one(conn, query, params=()):
    with conn.cursor() as cur:
        cur.execute(query, params)
        return cur.fetchone()


def get_table_names(conn):
    rows = fetch_all(
        conn,
        """
        SELECT table_name
        FROM information_schema.tables
        WHERE table_schema = 'public'
          AND table_type = 'BASE TABLE'
          AND table_name <> 'flyway_schema_history'
        ORDER BY table_name
        """,
    )
    return [row[0] for row in rows]


def get_fk_map(conn):
    rows = fetch_all(
        conn,
        """
        SELECT kcu.table_name AS child_table,
               kcu.column_name AS child_column,
               ccu.table_name AS parent_table
        FROM information_schema.key_column_usage kcu
        JOIN information_schema.referential_constraints rc
          ON kcu.constraint_name = rc.constraint_name AND kcu.table_schema = rc.constraint_schema
        JOIN information_schema.constraint_column_usage ccu
          ON rc.unique_constraint_name = ccu.constraint_name AND rc.unique_constraint_schema = ccu.table_schema
        WHERE kcu.table_schema = 'public'
        ORDER BY kcu.table_name, kcu.column_name
        """,
    )
    fk_map = defaultdict(dict)
    for child_table, child_column, parent_table in rows:
        if child_table == 'flyway_schema_history' or parent_table == 'flyway_schema_history':
            continue
        fk_map[child_table][child_column] = parent_table
    return fk_map


def get_table_order(conn):
    tables = get_table_names(conn)
    fk_map = get_fk_map(conn)
    indegree = {table: 0 for table in tables}
    children = defaultdict(list)

    for table in tables:
        parents = {parent for parent in fk_map.get(table, {}).values() if parent in tables}
        indegree[table] = len(parents)
        for parent in parents:
            children[parent].append(table)

    queue = deque(sorted(table for table in tables if indegree[table] == 0))
    ordered = []
    while queue:
        current = queue.popleft()
        ordered.append(current)
        for child in sorted(children.get(current, [])):
            indegree[child] -= 1
            if indegree[child] == 0:
                queue.append(child)

    for table in sorted(set(tables) - set(ordered)):
        ordered.append(table)
    return ordered


def get_columns(conn, table_name):
    rows = fetch_all(
        conn,
        """
        SELECT column_name, data_type
        FROM information_schema.columns
        WHERE table_schema = 'public' AND table_name = %s
        ORDER BY ordinal_position
        """,
        (table_name,),
    )
    return rows


def choose_parent_id(conn, parent_table):
    row = fetch_one(conn, f'SELECT id FROM public."{parent_table}" ORDER BY id LIMIT 1;')
    return row[0] if row else None


def default_value(column_name, data_type, idx, table_name):
    lower = column_name.lower()

    if lower == 'uuid':
        return 'gen_random_uuid()'
    if 'mot_de_passe' in lower or lower.endswith('hash'):
        return "crypt('Password123!', gen_salt('bf', 12))"
    if 'email' in lower:
        return "'" + f"demo_{table_name}_{column_name}_{idx}@example.com" + "'"
    if 'telephone' in lower or 'phone' in lower:
        return "'" + f"+237600000{idx}" + "'"
    if 'adresse' in lower:
        return "'Rue Demo 1'"
    if 'ville' in lower:
        return "'Douala'"
    if 'pays' in lower:
        return "'Cameroun'"
    if 'langue' in lower:
        return "'fr'"
    if 'sexe' in lower:
        return "'M'" if idx % 2 == 0 else "'F'"
    if lower in {
        'created_at', 'updated_at', 'date_consentement', 'date_tentative', 'date_connexion',
        'date_expiration', 'date_inscription', 'date_reunion', 'date_ajout', 'date_emission',
        'date_paiement', 'date_soumission', 'date_evaluation', 'date_jour', 'date_debut',
        'date_fin', 'date_echeance', 'date_urgence', 'date_consultation', 'horodatage', 'envoye_le', 'lu_le'
    } or lower.startswith('date_') or lower.endswith('_date') or lower.endswith('_at') or lower.endswith('_le'):
        return 'CURRENT_TIMESTAMP'
    if lower.startswith('heure_') or lower.endswith('_time'):
        return "'08:00:00'::time"
    if lower in {
        'statut', 'statut_paiement', 'statut_envoi', 'statut_reponse', 'etat', 'type_presence',
        'type_contrat', 'type_document', 'type_periode', 'type_conversation', 'type_etablissement',
        'plan_abonnement', 'cycle'
    }:
        return "'actif'"
    if lower in {'code', 'matricule'}:
        return "'" + f"{table_name.upper()}-{idx}" + "'"
    if lower in {'libelle', 'titre', 'nom', 'prenom', 'description', 'motif', 'question', 'reponse',
                 'contenu', 'categorie', 'fonction', 'departement', 'specialite', 'type_chauffeur', 'type_conge'}:
        return "'" + f"{table_name}_{column_name}_{idx}" + "'"
    if 'montant' in lower or 'salaire' in lower or 'prix' in lower or 'solde' in lower:
        return str(1000 + idx * 100)
    if data_type in {'smallint', 'integer', 'bigint'}:
        return str(10 + idx)
    if data_type in {'numeric', 'decimal', 'real', 'double precision'}:
        return str(100.0 + idx)
    if data_type == 'boolean':
        return 'TRUE' if idx % 2 == 0 else 'FALSE'
    if data_type in {'json', 'jsonb'}:
        return "'{\"demo\": true}'::jsonb"
    return "'" + f"{table_name}_{column_name}_{idx}" + "'"


def seed_table(conn, table_name, target_rows=TARGET_ROWS):
    current_count = fetch_one(conn, f'SELECT COUNT(*) FROM public."{table_name}";')[0]
    if current_count >= target_rows:
        return

    fk_map = get_fk_map(conn)
    fk_by_column = fk_map.get(table_name, {})
    columns = get_columns(conn, table_name)

    while current_count < target_rows:
        col_names = []
        values = []
        for column_name, data_type in columns:
            if column_name == 'id':
                continue
            col_names.append(column_name)

            if column_name.lower().endswith('_id'):
                parent_table = fk_by_column.get(column_name)
                if parent_table:
                    parent_id = choose_parent_id(conn, parent_table)
                    if parent_id is not None:
                        values.append(str(parent_id))
                        continue
                values.append('1')
                continue

            values.append(default_value(column_name, data_type, current_count + 1, table_name))

        insert_sql = (
            f'INSERT INTO public."{table_name}" ({", ".join(f"\"{col}\"" for col in col_names)}) '
            f'VALUES ({", ".join(values)});'
        )
        with conn.cursor() as cur:
            cur.execute(insert_sql)
        conn.commit()
        current_count = fetch_one(conn, f'SELECT COUNT(*) FROM public."{table_name}";')[0]


def main():
    conn = psycopg.connect(DSN)
    try:
        for table_name in get_table_order(conn):
            seed_table(conn, table_name)

        print('FINAL_COUNTS')
        for table_name in get_table_names(conn):
            count = fetch_one(conn, f'SELECT COUNT(*) FROM public."{table_name}";')[0]
            print(f'{table_name}:{count}')
    finally:
        conn.close()


if __name__ == '__main__':
    main()
