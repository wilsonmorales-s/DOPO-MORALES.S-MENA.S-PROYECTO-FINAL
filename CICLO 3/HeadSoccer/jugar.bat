@echo off
rem Compila y abre el juego (ejecutar desde la carpeta HeadSoccer)
javac -encoding UTF-8 -d out src\domain\*.java src\presentation\*.java
java -cp out presentation.HeadSoccerGUI
