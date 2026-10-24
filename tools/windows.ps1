param([ValidateSet('Play','Build','Test','Check')][string]$Action = 'Play', [string]$Installer)
$ErrorActionPreference = 'Stop'
$projectRoot = Split-Path $PSScriptRoot -Parent
$env:PYTHONUTF8 = '1'
$javaBin = $null
$portableRoot = Join-Path $projectRoot '.tools\jdk21'
if (Test-Path -LiteralPath $portableRoot) {
    $portable = Get-ChildItem -LiteralPath $portableRoot -Directory | Where-Object { Test-Path (Join-Path $_.FullName 'bin\javac.exe') } | Select-Object -First 1
    if ($portable) { $javaBin = Join-Path $portable.FullName 'bin' }
}
if (-not $javaBin -and $env:JAVA_HOME -and (Test-Path (Join-Path $env:JAVA_HOME 'bin\java.exe'))) { $javaBin = Join-Path $env:JAVA_HOME 'bin' }
if (-not $javaBin) {
    $javaCommand = Get-Command java.exe -ErrorAction SilentlyContinue
    if ($javaCommand) { $javaBin = Split-Path $javaCommand.Source -Parent }
}
if (-not $javaBin) { throw 'Instale o JDK 21 e configure JAVA_HOME ou PATH. Consulte README.md.' }
$env:PATH = "$javaBin;$env:PATH"
try { $javaVersion = (& (Join-Path $javaBin 'java.exe') --version | Out-String) }
catch { throw 'Java 21 necessario. Configure JAVA_HOME para o JDK 21 e abra outro terminal.' }
if ($javaVersion -notmatch '(?:openjdk|java)\s+21(?:[.\s-]|$)') { throw 'Java 21 necessario. Configure JAVA_HOME para o JDK 21 e abra outro terminal.' }
if ($Action -in @('Build','Test','Check')) {
    if (-not (Test-Path (Join-Path $javaBin 'javac.exe'))) { throw 'Instale o JDK 21 completo, incluindo javac.' }
    if (-not (Get-Command python.exe -ErrorAction SilentlyContinue)) { throw 'Instale Python 3.9+ com python no PATH.' }
    & python -c 'import sys; sys.exit(0 if sys.version_info >= (3,9) else 1)'
    if ($LASTEXITCODE -ne 0) { throw 'Python 3.9+ necessario. Confira python --version e os aliases do Windows.' }
}
if ($Action -in @('Build','Check')) {
    foreach ($extractorPath in @((Join-Path $projectRoot '.tools\7zip'),(Join-Path $env:ProgramFiles '7-Zip'))) {
        if (Test-Path (Join-Path $extractorPath '7z.exe')) { $env:PATH = "$extractorPath;$env:PATH"; break }
    }
    if (-not (Get-Command 7z.exe -ErrorAction SilentlyContinue) -and -not (Get-Command 7zz.exe -ErrorAction SilentlyContinue)) { throw 'Instale 7-Zip em Program Files\7-Zip ou adicione 7z.exe ao PATH.' }
}
if ($Action -eq 'Check') { Write-Host 'OK: Java 21, compilador, Python 3.9+ e 7-Zip encontrados.'; exit 0 }
if ($Action -eq 'Build') {
    if (-not $Installer) { $Installer = Join-Path $projectRoot 'brasfoot22-23.exe' }
    & python -X utf8 (Join-Path $PSScriptRoot 'prepare_game.py') --installer $Installer
    if ($LASTEXITCODE -ne 0) { throw 'Falha ao extrair o instalador.' }
    & python -X utf8 (Join-Path $PSScriptRoot 'build_mod.py')
    if ($LASTEXITCODE -ne 0) { throw 'Falha ao compilar o mod.' }
    Write-Host 'Pronto! Abra Jogar-Brasfoot-Mod.cmd.'
    exit 0
}
$jarDirectory = Join-Path $projectRoot 'artifacts\game'
$activePath = Join-Path $jarDirectory 'active-mod.txt'
$activeName = 'brasfoot-competitions.jar'
if (Test-Path -LiteralPath $activePath) { $activeName = (Get-Content -LiteralPath $activePath -Raw -Encoding UTF8).Trim() }
if ($activeName -notmatch '^brasfoot-competitions(?:-[0-9-]+)?\.jar$') { throw 'Nome do JAR ativo invalido.' }
$jarPath = Join-Path $jarDirectory $activeName
if (-not (Test-Path -LiteralPath $jarPath)) { throw 'Execute Preparar-Brasfoot-Mod.cmd primeiro.' }
if ($Action -eq 'Test') {
    foreach ($tool in @('validate_competitions.py','test_mod.py','verify_patch.py')) {
        $options = @()
        if ($tool -eq 'test_mod.py') { $options = @('--without-save') }
        & python -X utf8 (Join-Path $PSScriptRoot $tool) @options
        if ($LASTEXITCODE -ne 0) { throw "Falha em $tool." }
    }
    exit 0
}
$gamePath = Join-Path $projectRoot 'runtime\competitions\game'
$tempPath = Join-Path $projectRoot 'runtime\competitions\tmp'
New-Item -ItemType Directory -Force -Path $tempPath | Out-Null
Push-Location -LiteralPath $gamePath
try {
    & (Join-Path $javaBin 'java.exe') '-Dfile.encoding=UTF-8' '--add-exports=java.desktop/sun.java2d=ALL-UNNAMED' '--add-opens=java.base/java.util=ALL-UNNAMED' '--add-opens=java.base/java.lang=ALL-UNNAMED' '--add-opens=java.desktop/java.awt=ALL-UNNAMED' "-Djava.io.tmpdir=$tempPath" -jar $jarPath
    if ($LASTEXITCODE -ne 0) { throw "O jogo encerrou com codigo $LASTEXITCODE." }
} finally { Pop-Location }
