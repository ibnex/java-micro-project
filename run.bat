@echo off
setlocal
cd /d "%~dp0"

if not exist "src\Main.java" (
    echo ERROR: src\Main.java was not found.
    pause
    exit /b 1
)

set "JDBC_JAR="
for %%F in ("mysql-connector-j-*.jar") do (
    if exist "%%~F" set "JDBC_JAR=%%~fF"
)

if "%JDBC_JAR%"=="" (
    echo ERROR: MySQL Connector/J JAR was not found.
    echo Download it and place the JAR file in this project folder.
    pause
    exit /b 1
)

where javac >nul 2>nul
if errorlevel 1 (
    echo ERROR: javac was not found. Install the JDK and add its bin folder to PATH.
    pause
    exit /b 1
)

if not exist "bin" mkdir "bin"
echo Compiling...
javac -cp "%JDBC_JAR%" -d bin src\Main.java
if errorlevel 1 (
    echo ERROR: Compilation failed. Check the JDK and Connector/J JAR.
    pause
    exit /b 1
)

echo Starting application...
java -cp "bin;%JDBC_JAR%" Main
if errorlevel 1 (
    echo.
    echo ERROR: Application stopped. Check that MySQL is running and that
    echo the password in src\Main.java is correct.
    pause
    exit /b 1
)
endlocal
