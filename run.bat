@echo off
set JAVA_HOME=C:\Users\Lenovo\.jdks\ms-17.0.17
cd /d "%~dp0"
mvn clean install -DskipTests
mvn spring-boot:run -pl sky-server
