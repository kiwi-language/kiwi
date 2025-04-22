@echo off
SET "BASEDIR=%~dp0"
java -jar --enable-preview "%BASEDIR%compiler.jar" %*
SET BASEDIR=