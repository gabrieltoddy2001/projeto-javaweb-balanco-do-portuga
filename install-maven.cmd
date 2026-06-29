@echo off
setlocal enabledelayedexpansion

set MAVEN_HOME=C:\Users\Antonino\AppData\Local\apache-maven\apache-maven-3.9.14
set MAVEN_ZIP=%USERPROFILE%\AppData\Local\apache-maven-3.9.14.zip
set MAVEN_BASE=%USERPROFILE%\AppData\Local\apache-maven

REM Criar diretório se não existir
if not exist "%MAVEN_BASE%" (
  mkdir "%MAVEN_BASE%"
)

REM Verificar se Maven já está instalado
if exist "%MAVEN_HOME%\bin\mvn.cmd" (
  echo Maven ja esta instalado em: %MAVEN_HOME%
  echo Configurando JAVA_HOME e adicionando ao PATH...
  
  if "!JAVA_HOME!" == "" (
    echo Erro: JAVA_HOME nao esta definido
    exit /b 1
  )
  
  set PATH=!JAVA_HOME!\bin;%MAVEN_HOME%\bin;!PATH!
  
  echo Verificando versao do Maven...
  call "%MAVEN_HOME%\bin\mvn.cmd" -version
  
  echo.
  echo Maven instalado com sucesso!
  echo MAVEN_HOME: %MAVEN_HOME%
  echo JAVA_HOME: !JAVA_HOME!
  echo.
  echo Agora voce pode usar Maven diretamente:
  echo   mvn clean compile
  echo   mvn clean package
  
  endlocal
  exit /b 0
)

echo Instalando Maven 3.9.14...
echo Baixando de: https://archive.apache.org/dist/maven/maven-3/3.9.14/binaries/apache-maven-3.9.14-bin.zip

REM Tentar baixar com PowerShell
powershell -Command "if (-not (Test-Path '%MAVEN_BASE%')) { New-Item -ItemType Directory -Path '%MAVEN_BASE%' -Force | Out-Null }; $ProgressPreference = 'SilentlyContinue'; try { Invoke-WebRequest -Uri 'https://archive.apache.org/dist/maven/maven-3/3.9.14/binaries/apache-maven-3.9.14-bin.zip' -OutFile '%MAVEN_ZIP%' -UseBasicParsing; Write-Host 'Download concluido'; exit 0 } catch { Write-Host 'Erro no download: $_'; exit 1 }"

if errorlevel 1 (
  echo Erro ao fazer download do Maven
  exit /b 1
)

echo Extraindo Maven...
powershell -Command "Expand-Archive -Path '%MAVEN_ZIP%' -DestinationPath '%MAVEN_BASE%' -Force"

if errorlevel 1 (
  echo Erro ao extrair Maven
  exit /b 1
)

echo Limpando arquivo ZIP...
del "%MAVEN_ZIP%"

if exist "%MAVEN_HOME%\bin\mvn.cmd" (
  echo.
  echo Maven instalado com sucesso em: %MAVEN_HOME%
  echo.
  echo IMPORTANTE: Adicionar ao PATH ou usar diretamente:
  echo   set PATH=%MAVEN_HOME%\bin;%%PATH%%
  echo.
  exit /b 0
) else (
  echo Erro: Maven nao foi extraido corretamente
  exit /b 1
)
