"""Check that only the declared original classes changed in the JAR."""
import hashlib
import json
from pathlib import Path
from zipfile import ZipFile

ROOT = Path(__file__).resolve().parents[1]
source = ROOT / 'artifacts/game/brasfoot.jar'
patched = source.with_name('brasfoot-competitions.jar')
active = source.with_name('active-mod.txt')
if active.exists():
    patched = source.with_name(active.read_text(encoding='utf-8').strip())
build = json.loads((ROOT/'analysis/mod-build.json').read_text(encoding='utf-8'))
with ZipFile(source) as original, ZipFile(patched) as mod:
    assert mod.testzip() is None
    assert len(mod.namelist()) == len(set(mod.namelist())), 'Duplicate ZIP names'
    assert not any('\\' in n for n in mod.namelist()), 'Windows separators inside JAR'
    assert set(original.namelist()) <= set(mod.namelist()), 'Original entries removed'
    changed = sorted(n for n in original.namelist() if original.read(n) != mod.read(n))
    assert changed == sorted(build['patched_original_classes']), changed
    added = sorted(set(mod.namelist()) - set(original.namelist()))
    assert all(n.startswith('mod/') for n in added), added
    report = {'changed_original_entries': changed, 'added_entries': added,
              'crc_ok': True, 'duplicate_names': False, 'windows_zip_separators': False,
              'original_sha256': hashlib.sha256(source.read_bytes()).hexdigest(),
              'mod_sha256': hashlib.sha256(patched.read_bytes()).hexdigest()}
(ROOT/'analysis/patch-integrity.json').write_text(json.dumps(report, indent=2), encoding='utf-8')
print(f'OK: {len(changed)} original classes changed; {len(added)} mod entries added; all other entries unchanged.')
