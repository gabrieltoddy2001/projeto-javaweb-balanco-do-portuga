@echo off
REM Script simplificado para compilar o BalancoDoPortugaWeb com Maven
REM Contorna problemas de espaços em JAVA_HOME

setlocal enabledelayedexpansion

echo.
echo ===============================================
echo BalancoDoPortugaWeb - Compilacao Maven
echo ===============================================
echo.

REM Configurar JAVA_HOME sem espaços (usando 8.3 format)
for %%A in ("C:\Program Files\Java\jdk-25") do set "JDK_PATH=%%~sA"

if not exist "!JDK_PATH!\bin\java.exe" (
  echo Erro: JDK nao encontrado em !JDK_PATH!
  echo Tentando com jdk-21...
  for %%A in ("C:\Program Files\Java\jdk-21.0.10") do set "JDK_PATH=%%~sA"
)

if not exist "!JDK_PATH!\bin\java.exe" (
  echo ERRO CRITICO: Nenhum JDK encontrado!
  pause
  exit /b 1
)

echo Usando Java de: !JDK_PATH!
"!JDK_PATH!\bin\java.exe" -version
echo.

REM Configurar Maven Wrapper
set "MAVEN_WRAPPER=%CD%\.mvn\wrapper\maven-wrapper.jar"

if not exist "!MAVEN_WRAPPER!" (
  echo ERRO: Maven wrapper nao encontrado em !MAVEN_WRAPPER!
  pause
  exit /b 1
)

echo Compilando projeto...
echo.

REM Executar Maven com caminho sem espaços
"!JDK_PATH!\bin\java.exe" ^
  -classpath "!MAVEN_WRAPPER!" ^
  "-Dmaven.multiModuleProjectDirectory=%CD%" ^
  org.apache.maven.wrapper.MavenWrapperMain clean compile %*

if errorlevel 1 (
  echo.
  echo ERRO na compilacao!
  pause
  exit /b 1
)

echo.
echo ===============================================
echo Compilacao concluida com sucesso!
echo ===============================================
echo.
echo Proximos passos:
echo 1. Abra NetBeans
echo 2. Arquivo ^> Abrir Projeto
echo 3. Selecione: %CD%
echo 4. Right-click no projeto ^> Run ^(F6^)
echo.
pause
