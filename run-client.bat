@echo off
setlocal
REM Run Client with JDBC jar on classpath (client doesn't need DB but include jar for consistency)

REM Compile Java files and copy assets
echo Compiling Java files...
if not exist bin mkdir bin
pushd %cd%
cd /d "%~dp0"
for /r src %%f in (*.java) do (
    javac -encoding UTF-8 -cp "library\mysql-connector-j-8.0.33.jar" -d bin "%%f" 2>nul
)

REM Copy asset resources to bin
echo Copying asset resources...
if exist "src\Client\view\asset" (
    if not exist "bin\client\view" mkdir "bin\client\view"
    xcopy /e /y "src\Client\view\asset" "bin\client\view\asset\" >nul
)
popd

set LIB=library\mysql-connector-j-8.0.33.jar
set CP=bin;%LIB%
echo Running Client with classpath: %CP%
java -cp "%CP%" Client.RunClient
pause
