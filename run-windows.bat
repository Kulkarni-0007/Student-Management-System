@echo off
setlocal
cd /d "%~dp0"
echo ==============================================
echo   CampusDesk - Student Management System
echo ==============================================
where mvn >nul 2>nul
if errorlevel 1 (
  echo ERROR: Maven was not found. Install Maven 3.8+ and add it to PATH.
  echo You also need JDK 17 or later.
  pause
  exit /b 1
)
echo Building application...
call mvn clean package
if errorlevel 1 (
  echo Build failed. Check the Maven error above.
  pause
  exit /b 1
)
echo Launching CampusDesk...
java -jar target\student-management-system-1.0.0.jar
if errorlevel 1 (
  echo Application exited with an error.
  pause
  exit /b 1
)
endlocal
