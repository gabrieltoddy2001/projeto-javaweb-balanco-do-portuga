@echo off
setlocal disabledelayedexpansion
set "JAVA_HOME=C:\Program Files\Java\jdk-25"
setlocal enabledelayedexpansion
cd /d "c:\Users\Antonino\Documents\NetBeansProjects\BalancoDoPortugaWeb"
echo JAVA_HOME is: !JAVA_HOME!
echo Checking Java:
"!JAVA_HOME!\bin\java.exe" -version
call mvnw.cmd clean compile
