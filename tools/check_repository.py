"""Audit publishable files without requiring the Brasfoot installer."""
import json
from pathlib import Path
import re
import subprocess

ROOT = Path(__file__).resolve().parents[1]
listing = subprocess.run(
    ['git', 'ls-files', '--cached', '--others', '--exclude-standard', '-z'],
    cwd=ROOT, check=True, capture_output=True,
)
files = sorted(set(p for p in listing.stdout.decode('utf-8').split('\0') if p))
errors = []
for name in files:
    path = ROOT / name
    if name.split('/')[0] in {'artifacts', 'runtime', 'analysis', '.tools', 'upstream', '.aws', '.codex', '.agents'}:
        errors.append(f'Arquivo local selecionado para publicacao: {name}')
    if path.suffix.lower() in {'.exe', '.msi', '.jar', '.class', '.s22', '.sbck', '.log', '.tmp'}:
        errors.append(f'Binario/save/relatorio selecionado: {name}')
    if not path.is_file():
        errors.append(f'Arquivo ausente: {name}')
        continue
    if path.suffix in {'.py', '.java', '.ps1', '.md', '.json', '.yml'}:
        content = path.read_text(encoding='utf-8-sig')
        if re.search(r'[A-Za-z]:[/\\]Users[/\\]', content):
            errors.append(f'Caminho pessoal em: {name}')
        if path.suffix == '.json':
            json.loads(content)
        if path.suffix == '.py':
            compile(content, name, 'exec')
        if path.suffix == '.md':
            for link in re.findall(r'\]\(([^)]+)\)', content):
                if '://' not in link and not link.startswith('#'):
                    target = link.split('#', 1)[0]
                    if target and not (path.parent / target).exists():
                        errors.append(f'Link local quebrado em {name}: {link}')
for required in ('README.md', 'LICENSE', 'NOTICE.md', '.gitignore', 'tools/windows.ps1', 'tools/prepare_game.py', 'mod/src/mod/UnifiedTables.java'):
    if required not in files:
        errors.append(f'Arquivo necessario fora da selecao do Git: {required}')
if errors:
    raise SystemExit('\n'.join(errors))
print(f'OK: {len(files)} arquivos publicaveis; sem jogo, saves ou caminhos pessoais; links e sintaxe conferidos.')
