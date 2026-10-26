"""Ejecuta las consultas del codelab de SQL sobre la base de ejemplo de Google."""
import json
import sqlite3
from pathlib import Path

directory = Path(__file__).resolve().parent
source = sqlite3.connect(f"file:{(directory / 'Email.db').as_posix()}?mode=ro", uri=True)
database = sqlite3.connect(':memory:')
source.backup(database)
source.close()

queries = [
    ('Todos los correos', 'SELECT * FROM email'),
    ('Asuntos', 'SELECT subject FROM email'),
    ('Asunto y remitente', 'SELECT subject, sender FROM email'),
    ('Cantidad total', 'SELECT COUNT(*) FROM email'),
    ('Fecha más reciente', 'SELECT MAX(received) FROM email'),
    ('Remitentes', 'SELECT sender FROM email'),
    ('Remitentes distintos', 'SELECT DISTINCT sender FROM email'),
    ('Cantidad de remitentes distintos', 'SELECT COUNT(DISTINCT sender) FROM email'),
    ('Recibidos', "SELECT * FROM email WHERE folder = 'inbox'"),
    ('Recibidos sin leer', "SELECT * FROM email WHERE folder = 'inbox' AND read = false"),
    ('Importantes o destacados', "SELECT * FROM email WHERE folder = 'important' OR starred = true"),
    ('Asuntos que contienen fool', "SELECT COUNT(*) FROM email WHERE subject LIKE '%fool%'"),
    ('Asuntos terminados en fool', "SELECT * FROM email WHERE subject LIKE '%fool'"),
    ('Remitentes que empiezan con h', "SELECT DISTINCT sender FROM email WHERE sender LIKE 'h%'"),
    ('Cantidad por carpeta', 'SELECT folder, COUNT(*) FROM email GROUP BY folder'),
    ('Más recientes primero', 'SELECT * FROM email ORDER BY received DESC'),
    ('Búsqueda de fool por fecha', "SELECT * FROM email WHERE subject LIKE '%fool%' ORDER BY received ASC"),
    ('Primera página', "SELECT * FROM email WHERE folder = 'inbox' ORDER BY received DESC LIMIT 10"),
    ('Segunda página', "SELECT * FROM email WHERE folder = 'inbox' ORDER BY received DESC LIMIT 10 OFFSET 10"),
]

results = []
for title, query in queries:
    cursor = database.execute(query)
    rows = cursor.fetchall()
    results.append({'actividad': title, 'consulta': query, 'columnas': [c[0] for c in cursor.description], 'filas': rows})

cursor = database.execute("INSERT INTO email VALUES (NULL, 'Lorem ipsum dolor sit amet', 'sender@example.com', 'inbox', false, false, CURRENT_TIMESTAMP)")
inserted_id = cursor.lastrowid
results.append({'actividad': 'Insertar', 'id': inserted_id, 'filas': database.execute('SELECT * FROM email WHERE id = ?', (inserted_id,)).fetchall()})
database.execute('UPDATE email SET read = true WHERE id = ?', (inserted_id,))
updated = database.execute('SELECT read FROM email WHERE id = ?', (inserted_id,)).fetchone()
assert updated == (1,)
results.append({'actividad': 'Actualizar', 'id': inserted_id, 'filas': [updated]})
database.execute('DELETE FROM email WHERE id = ?', (inserted_id,))
remaining = database.execute('SELECT * FROM email WHERE id = ?', (inserted_id,)).fetchall()
assert not remaining
results.append({'actividad': 'Eliminar', 'id': inserted_id, 'filas': remaining})
(directory / 'resultados.json').write_text(json.dumps(results, ensure_ascii=False, indent=2), encoding='utf-8')
print(f'{len(results)} actividades ejecutadas; INSERT, UPDATE y DELETE verificados. ID: {inserted_id}')
