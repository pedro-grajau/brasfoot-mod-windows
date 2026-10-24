# Como contribuir

Prepare sua cópia do jogo seguindo o [README](README.md). O código fica em `mod/src/mod/`, os testes em `mod/test/mod/` e as regras em `patches/`.

Antes de propor uma mudança, execute **Preparar-Brasfoot-Mod.cmd** e **Testar-Brasfoot-Mod.cmd**. Para alterações visuais, confira também a interface do jogo e registre uma captura que mostre o comportamento final.

Para testar somente a navegação em Tabelas com uma cópia local de save:

```powershell
python -X utf8 tools/test_mod.py --tables-only --save "D:\Backups\carreira.s22"
```

O comando precisa de Java/Javac 21 no PATH. Ele carrega o save em memória e confere o hash ao final. O replay completo com `--save`, sem `--tables-only`, tem pressupostos próprios de temporada e não serve para todas as carreiras; prefira os testes direcionados ao problema investigado.

Não inclua instaladores, JARs, classes originais, saves, preferências, logs ou relatórios de engenharia reversa nos commits. Confira `git status --short` e `python tools/check_repository.py` antes de enviar.

Para reportar um bug, informe Windows, Java e Python usados, ano da carreira, competição/fase, passos para reproduzir e mensagem de erro. Remova informações pessoais de capturas e logs. Não publique uma cópia do jogo ou do save em uma issue pública.

Descreva no pull request o problema, a mudança e os testes executados. Mantenha as atribuições e a licença GPL-3.0-only do código.
