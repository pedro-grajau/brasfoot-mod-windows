# Build e estrutura

O preparador `tools/prepare_game.py` extrai o instalador com 7-Zip e recupera o ZIP Java de `bf22-23.exe`. A normalização confere CRC, nomes e conteúdo de todas as entradas. O build recusa um JAR cujo hash não corresponda à edição suportada.

`tools/build_mod.py` valida as configurações, compila o mod e aplica 19 pontos de integração em onze classes originais com o ASM interno do JDK 21. O código do mod é compilado com `--release 11`; o build e os atalhos deste projeto exigem JDK 21. Nenhum decompilador adicional precisa ser baixado.

| Diretório local | Conteúdo |
| --- | --- |
| `artifacts/original/` | Conteúdo extraído do instalador |
| `artifacts/game/` | JAR original normalizado, JARs do mod e indicação da versão ativa |
| `artifacts/mod-build/` | Classes, bibliotecas e ferramentas de compilação |
| `runtime/competitions/game/` | Instalação executada pelo atalho do mod |
| `runtime/competitions/game/sav/` | Saves padrão dessa instalação |
| `analysis/` | Relatórios e imagens gerados pelos testes |

O atalho escolhe primeiro um JDK portátil em `.tools/jdk21/<pasta-do-jdk>/`, depois `JAVA_HOME` e depois `java.exe` no PATH. O 7-Zip pode estar em `.tools/7zip/`, na instalação padrão em Program Files ou no PATH. Esses diretórios portáteis são opcionais e não são distribuídos.

## Comandos

Com Java/Javac 21, Python e 7-Zip no PATH, na raiz do projeto:

```powershell
python -X utf8 tools/prepare_game.py --installer "D:\Jogos\brasfoot22-23.exe"
python -X utf8 tools/build_mod.py
python -X utf8 tools/validate_competitions.py
python -X utf8 tools/test_mod.py --without-save
python -X utf8 tools/verify_patch.py
python tools/check_repository.py
```

Não há dependências Python externas. O teste de integridade compara cada entrada do JAR modificado com o original, permitindo apenas as classes declaradas pelo build e entradas novas em `mod/`.

As alterações da interface ficam principalmente em `UnifiedTables.java` e `NativeUi.java`. A lógica das competições está em `CompetitionMod.java` e a integração financeira em `Rewards.java`. O patch não altera a estrutura serializada das classes originais.

## Publicação

O repositório inclui apenas fontes, regras, imagens finais do mod e documentação. A verificação automática no GitHub valida essa composição e a sintaxe Python; o build completo e os testes de integração precisam do instalador fornecido localmente pelo usuário e não são executados no CI público.

Crie um repositório vazio no GitHub, copie a URL e, após revisar os arquivos, execute:

```powershell
git add .
git commit -m "Organiza mod do Brasfoot para Windows"
git remote add origin <URL-DO-REPOSITORIO>
git push -u origin main
```

Se já existir um remote `origin`, confira `git remote -v` antes de alterá-lo. Não envie `upstream/`, que é somente uma cópia local de trabalho anterior nesta máquina e está ignorada.
