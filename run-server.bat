@echo off
setlocal
REM Run Server with JDBC jar on classpath

REM Compile Java files and copy assets
echo Compiling Java files...
if not exist bin mkdir bin
pushd %cd%
cd /d "%~dp0"
for /r src %%f in (*.java) do (
    javac -encoding UTF-8 -cp "library\mysql-connector-j-8.0.33.jar" -d bin "%%f" 2>nul
)

REM Copy asset resources to bin (for consistency)
echo Copying asset resources...
if exist "src\Client\view\asset" (
    if not exist "bin\client\view" mkdir "bin\client\view"
    xcopy /e /y "src\Client\view\asset" "bin\client\view\asset\" >nul
)
popd

set LIB=library\mysql-connector-j-8.0.33.jar
set CP=bin;%LIB%
echo Running Server with classpath: %CP%
java -cp "%CP%" Server.RunServer
pause
