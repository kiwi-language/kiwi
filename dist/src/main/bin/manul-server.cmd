@echo off
SET "BASEDIR=%~dp0"
java -jar --enable-preview "%BASEDIR%server.jar" %*
SET BASEDIR=