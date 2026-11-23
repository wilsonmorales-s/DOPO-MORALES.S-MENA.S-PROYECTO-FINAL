@echo off
rem Compila y corre todas las pruebas de unidad (ejecutar desde la carpeta HeadSoccer)
javac -encoding UTF-8 -cp lib\junit-platform-console-standalone-1.14.4.jar -d out src\domain\*.java src\presentation\*.java test\domain\*.java
java -jar lib\junit-platform-console-standalone-1.14.4.jar execute --class-path out --scan-class-path
pause
