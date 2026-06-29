@echo off
REM Script para executar o BalancoDoPortugaWeb no NetBeans
REM Contorna problemas de caminhos com espaços

setlocal enabledelayedexpansion

cd /d "C:\Users\Antonino\Documents\NetBeansProjects\BalancoDoPortugaWeb"

REM Usar JDK 25 com nome 8.3 (sem espaços)
set "JAVA_BIN=C:\PROGRA~1\Java\jdk-25\bin"

REM Verificar Java
if not exist "!JAVA_BIN!\java.exe" (
  echo ERRO: JDK nao encontrado
  pause
  exit /b 1
)

echo Detectado Java em: !JAVA_BIN!
"!JAVA_BIN!\java.exe" -version
echo.

REM Executar Maven
echo Compilando projeto...
"!JAVA_BIN!\java.exe" ^
  -classpath ".mvn\wrapper\maven-wrapper.jar" ^
  "-Dmaven.multiModuleProjectDirectory=%CD%" ^
  org.apache.maven.wrapper.MavenWrapperMain package

if errorlevel 1 (
  echo ERRO na compilacao
  pause
  exit /b 1
)

echo.
echo Compilacao concluida com sucesso!
echo.
echo Abra NetBeans e execute o projeto...
pause
