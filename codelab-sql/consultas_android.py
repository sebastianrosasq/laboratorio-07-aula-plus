"""Comprueba consultas SQL contra la base de la app SQL Basics en ejecución."""
import os
import shlex
import subprocess
from pathlib import Path

adb = Path(os.environ['LOCALAPPDATA']) / 'Android/Sdk/platform-tools/adb.exe'
marker = 'LAB_SQL_DEMO_C9'

def query(sql):
    remote = 'run-as com.example.sqldemo sqlite3 -header -column databases/app_database ' + shlex.quote(sql)
    result = subprocess.run([str(adb), 'shell', remote], capture_output=True, text=True, check=True)
    if result.stderr.strip():
        raise RuntimeError(result.stderr.strip())
    return result.stdout.strip()

checks = [
    ('Correos de la bandeja', "SELECT id, sender, folder FROM email WHERE folder = 'inbox' LIMIT 3;"),
    ('Remitentes distintos en spam', "SELECT COUNT(DISTINCT sender) AS remitentes FROM email WHERE folder = 'spam';"),
    ('Correos por carpeta', 'SELECT folder, COUNT(*) AS cantidad FROM email GROUP BY folder ORDER BY cantidad DESC;'),
]
for title, sql in checks:
    print('\n' + title + '\n' + sql)
    print(query(sql))

existing = query(f"SELECT COUNT(*) FROM email WHERE subject = '{marker}';")
if existing.splitlines()[-1].strip() != '0':
    raise RuntimeError('Ya existe un correo de prueba con ese identificador')

try:
    query(f"INSERT INTO email VALUES (NULL, '{marker}', 'ejemplo@example.com', 'inbox', 0, 0, CURRENT_TIMESTAMP);")
    print('\nINSERT')
    print(query(f"SELECT subject, read FROM email WHERE subject = '{marker}';"))
    query(f"UPDATE email SET read = 1 WHERE subject = '{marker}';")
    print('\nUPDATE')
    print(query(f"SELECT subject, read FROM email WHERE subject = '{marker}';"))
finally:
    query(f"DELETE FROM email WHERE subject = '{marker}';")
print('\nDELETE')
print(query(f"SELECT COUNT(*) AS restantes FROM email WHERE subject = '{marker}';"))
