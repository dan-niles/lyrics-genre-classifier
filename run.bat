@echo off
echo Building backend with Maven...
call mvn clean package

if %ERRORLEVEL% NEQ 0 (
    echo Maven build failed. Exiting...
    pause
    exit /b %ERRORLEVEL%
)

echo Starting Spring Boot app in a new terminal window...
start "Spring Boot App" cmd /k java --add-exports java.base/sun.nio.ch=ALL-UNNAMED --add-opens java.base/sun.security.action=ALL-UNNAMED --add-opens java.base/java.nio=ALL-UNNAMED --add-opens java.base/java.lang.invoke=ALL-UNNAMED --add-opens java.base/java.util=ALL-UNNAMED -jar spring-api/target/spring-api-1.0-SNAPSHOT.jar

echo Waiting 30 seconds for server to start...
timeout /t 30 > nul

echo Opening browser to http://localhost:9090...
echo If the browser does not open, please navigate to http://localhost:9090 manually.
echo If you see a 404 error, please wait a bit longer for the server to start and refresh the page.
start "" http://localhost:9090

pause