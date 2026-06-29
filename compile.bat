@echo off
setlocal enabledelayedexpansion

echo.
echo ===============================================
echo Compilacao do BalancoDoPortugaWeb
echo ===============================================
echo.

set "JAVA_HOME=C:\Program Files\Java\jdk-25"
set "JAVA_BIN=!JAVA_HOME!\bin"

REM Verificar Java
echo Verificando Java...
if not exist "!JAVA_BIN!\java.exe" (
    echo ERRO: Java nao encontrado em !JAVA_BIN!
    pause
    exit /b 1
)

"!JAVA_BIN!\java.exe" -version
echo.

REM Tentar descarregar Maven se nao existir
set "MAVEN_HOME=!CD!\.mvn"
set "MAVEN_JAR=!MAVEN_HOME!\wrapper\maven-wrapper.jar"

if not exist "!MAVEN_JAR!" (
    echo Downloading Maven wrapper JAR...
    powershell -Command ^
        "if (-not (Test-Path '!MAVEN_HOME!\wrapper')) { New-Item -ItemType Directory -Path '!MAVEN_HOME!\wrapper' -Force ^| Out-Null }; " ^
        "(New-Object System.Net.WebClient).DownloadFile('https://repo.maven.apache.org/maven2/org/apache/maven/wrapper/maven-wrapper/3.2.0/maven-wrapper-3.2.0.jar', '!MAVEN_JAR!')"
)

if not exist "!MAVEN_JAR!" (
    echo.
    echo AVISO: Maven wrapper nao pude ser baixado.
    echo.
    echo Como usar: Abra NetBeans e faca Clean and Build
    echo.
    pause
    exit /b 1
)

REM Executar Maven
echo.
echo Iniciando compilacao com Maven...
echo.

set "CLASSPATH=!MAVEN_JAR!"
"!JAVA_BIN!\java.exe" ^
    -classpath "!CLASSPATH!" ^
    -Dmaven.home="!MAVEN_HOME!" ^
    org.apache.maven.wrapper.MavenWrapperMain clean compile

if errorlevel 1 (
    echo.
    echo ERRO na compilacao!
    echo.
    pause
    exit /b 1
)

echo.
echo ===============================================
echo Compilacao concluida com sucesso!
echo ===============================================
echo.
echo Proximo passo: Abra NetBeans e execute Run Project (F6)
echo.
pause
