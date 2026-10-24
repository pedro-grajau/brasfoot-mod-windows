#!/usr/bin/env python3
# SPDX-License-Identifier: GPL-3.0-only
"""Run actual game-fixture and Kryo integration tests without opening a GUI."""
from pathlib import Path
import hashlib
import subprocess
import argparse
import os

ROOT=Path(__file__).resolve().parents[1]
parser=argparse.ArgumentParser(description=__doc__)
selection=parser.add_mutually_exclusive_group()
selection.add_argument("--save",type=Path,help="Save local opcional para testar elencos reais, sem alterá-lo")
selection.add_argument("--without-save",action="store_true",help="Executar somente a bateria com carreiras controladas")
parser.add_argument("--tables-only",action="store_true",help="Verificar Tabelas no save informado, sem replay de temporadas")
options=parser.parse_args()
if options.tables_only and options.save is None:parser.error("--tables-only exige --save")
if options.save is not None and not options.save.expanduser().is_file():parser.error("Save não encontrado: "+str(options.save))
build=ROOT/"artifacts/mod-build"
classes=build/"tests"
classes.mkdir(parents=True,exist_ok=True)
active=ROOT/"artifacts/game/active-mod.txt"
jar=ROOT/"artifacts/game"/(active.read_text(encoding="utf-8").strip() if active.exists() else "brasfoot-competitions.jar")
cp=os.pathsep.join(map(str,[jar,*sorted((build/"libs").glob("*.jar"))]))
subprocess.run(["javac","--release","11","-encoding","UTF-8","-cp",cp,"-d",str(classes),
                str(ROOT/"mod/test/mod/CompetitionIntegrationTest.java")],cwd=ROOT,check=True)
command=["java","--add-exports=java.desktop/sun.java2d=ALL-UNNAMED",
         "--add-opens=java.base/java.util=ALL-UNNAMED","--add-opens=java.base/java.lang=ALL-UNNAMED",
         "--add-opens=java.desktop/java.awt=ALL-UNNAMED","-Djava.awt.headless=true",
         "-Djava.util.prefs.userRoot="+str(build/"test-preferences"),
         "-Dmod.test.output="+str(ROOT/"analysis"),
         "-cp",str(classes)+os.pathsep+cp,"mod.CompetitionIntegrationTest"]
legacy=ROOT/"artifacts/original/sav/save_100_anos.s22"
if options.save is not None:legacy=options.save.expanduser().resolve()
use_save=not options.without_save and legacy.is_file()
if options.tables_only:command.append("--tables-save")
if use_save:command.append(str(legacy))
else:print("Sem save real: testes de elencos reais e resultado/bilheteria ao vivo serão omitidos.",flush=True)
legacy_hash=hashlib.sha256(legacy.read_bytes()).hexdigest() if use_save else None
try:
    subprocess.run(command,cwd=ROOT/"runtime/competitions/game",check=True)
finally:
    if legacy_hash is not None and legacy_hash!=hashlib.sha256(legacy.read_bytes()).hexdigest():
        raise RuntimeError("Save original alterado durante o teste")
