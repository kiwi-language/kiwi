@echo off
SET "BASEDIR=%~dp0"
SET "CONFIG_PATH=%BASEDIR%..\config\kiwi.yml"
java -jar %BASEDIR%server.jar -config "%CONFIG_PATH%"
SET BASEDIR=
SET CONFIG_PATH=