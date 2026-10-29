@echo off
setlocal
cd /d "%~dp0"
call mvn clean package
if errorlevel 1 exit /b 1
if not exist target\test-classes mkdir target\test-classes
javac -encoding UTF-8 -d target\test-classes -cp target\student-management-system-1.0.0.jar src\test\java\com\pavan\sms\SelfTest.java
if errorlevel 1 exit /b 1
java -cp "target\test-classes;target\student-management-system-1.0.0.jar" com.pavan.sms.SelfTest
endlocal
