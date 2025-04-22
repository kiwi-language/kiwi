@echo off
setlocal

REM === Configuration ===
REM Descriptive name for the service
set APP_NAME=KiwiServer

REM Get the absolute directory where the script resides
set SCRIPT_DIR=%~dp0

REM Get the absolute root directory (parent of script dir)
pushd "%SCRIPT_DIR%.."
set APP_ROOT_DIR=%CD%
popd

REM Paths to the JAR file and configuration file
set JAR_FILE=%SCRIPT_DIR%server.jar
set CONFIG_FILE=%APP_ROOT_DIR%\config\kiwi.yml"

REM Path for logs
set LOG_DIR=%APP_ROOT_DIR%\logs

REM Ensure log directory exists
if not exist "%LOG_DIR%\" (
    mkdir "%LOG_DIR%"
    if errorlevel 1 (
        echo Error: Could not create log directory: %LOG_DIR% >&2
        exit /b 1
    )
)

REM Path for the PID file
set PID_FILE=%SCRIPT_DIR%%APP_NAME%.pid

REM Path for the log file (basic redirection, Java app logging preferred)
set LOG_FILE=%LOG_DIR%\%APP_NAME%.log

REM Java command - adjust if 'java' is not in PATH or you need a specific version
set JAVA_CMD=java

REM Optional Java Virtual Machine arguments (e.g., memory settings)
REM Example: set JAVA_OPTS=-Xms512m -Xmx1024m
set JAVA_OPTS=

REM === Subroutines (Simulating Functions) ===

goto :ProcessCommand

REM --- show_help ---
:show_help
echo Usage: %~nx0 {start^|stop^|restart^|status^|help}
echo   start    : Start the %APP_NAME%
echo   stop     : Stop the %APP_NAME%
echo   restart  : Restart the %APP_NAME%
echo   status   : Check if %APP_NAME% is running
echo   help     : Display this help message
goto :EOF

REM --- is_running ---
REM Returns errorlevel 0 if running, 1 otherwise
:is_running
set "CURRENT_PID="
if not exist "%PID_FILE%" (
    REM echo Debug: PID file not found. >&2
    exit /b 1
)

REM Read PID from file, handle potential empty file
set /p CURRENT_PID=<"%PID_FILE%"
if not defined CURRENT_PID (
    echo Warning: PID file (%PID_FILE%) is empty. Removing... >&2
    del "%PID_FILE%" > nul 2>&1
    exit /b 1
)

REM Check if a process with that PID exists using tasklist
tasklist /FI "PID eq %CURRENT_PID%" /NH | findstr . > nul 2>&1
if errorlevel 1 (
    REM Process not found, stale PID file
    echo Warning: Stale PID file found (%PID_FILE% for PID %CURRENT_PID%). Removing... >&2
    del "%PID_FILE%" > nul 2>&1
    exit /b 1
) else (
    REM Process found
    REM echo Debug: Process %CURRENT_PID% found running. >&2
    exit /b 0
)
goto :EOF


REM --- start_server ---
:start_server
call :is_running
if %errorlevel% equ 0 (
    set /p RUNNING_PID=<"%PID_FILE%"
    echo %APP_NAME% is already running (PID: %RUNNING_PID%).
    exit /b 1
)

REM Check if JAR file exists
if not exist "%JAR_FILE%" (
    echo Error: JAR file not found at %JAR_FILE% >&2
    exit /b 1
)

REM Check if config file exists (optional)
REM if not exist "%CONFIG_FILE%" (
REM     echo Warning: Config file not found at %CONFIG_FILE% >&2
REM )

echo Starting %APP_NAME%...

REM Construct the command for WMIC (needs careful quoting)
REM Note: Redirection via WMIC is complex. App should handle logging.
REM We attempt basic redirection to LOG_FILE AFTER process creation, but it's less reliable.
set "COMMAND_LINE=%JAVA_CMD% %JAVA_OPTS% -jar \"%JAR_FILE%\" -config \"%CONFIG_FILE%\""

REM Use WMIC to start the process and capture the PID
set "NEW_PID="
echo Executing: %COMMAND_LINE% >> "%LOG_FILE%" 2>&1
for /f "tokens=2 delims==; " %%i in ('wmic process call create "%COMMAND_LINE%" ^| findstr /B ProcessId') do set NEW_PID=%%i

REM Check if PID was obtained
if not defined NEW_PID (
    echo Error: Failed to start %APP_NAME% using WMIC. Check permissions or command syntax. >&2
    echo Error: Command was: %COMMAND_LINE% >&2
    echo Check %LOG_FILE% for potential Java errors if the process briefly started. >&2
    exit /b 1
)

REM Brief pause to allow process to stabilize
timeout /t 1 /nobreak > nul

REM Verify the process actually started with the obtained PID
tasklist /FI "PID eq %NEW_PID%" /NH | findstr . > nul 2>&1
if errorlevel 1 (
    echo Error: Failed to start %APP_NAME% (PID %NEW_PID% not found after start). Check %LOG_FILE% for details. >&2
    exit /b 1
)

REM Save the PID to the PID file
echo %NEW_PID%>"%PID_FILE%"
echo %APP_NAME% started successfully (PID: %NEW_PID%). Basic output logged to %LOG_FILE%
exit /b 0
goto :EOF


REM --- stop_server ---
:stop_server
call :is_running
if %errorlevel% equ 1 (
    echo %APP_NAME% is not running.
    exit /b 1
)

REM is_running succeeded, so PID file exists and process is running
set /p PID_TO_STOP=<"%PID_FILE%"
echo Stopping %APP_NAME% (PID: %PID_TO_STOP%)...

REM Send graceful termination signal first
taskkill /PID %PID_TO_STOP% > nul 2>&1
if errorlevel 0 (
    echo Graceful termination signal sent to PID %PID_TO_STOP%. Waiting...
) else (
    echo Failed to send termination signal (maybe already stopped?). Checking...
)


REM Wait for the process to terminate
set WAIT_SECONDS=30
set COUNT=0
:StopLoop
if %COUNT% geq %WAIT_SECONDS% goto ForceKill

tasklist /FI "PID eq %PID_TO_STOP%" /NH | findstr . > nul 2>&1
if errorlevel 1 (
    REM Process has stopped
    echo.
    echo %APP_NAME% stopped successfully.
    del "%PID_FILE%" > nul 2>&1
    exit /b 0
)

REM Process still running, wait
echo | set /p =.
timeout /t 1 /nobreak > nul
set /a COUNT+=1
goto StopLoop

:ForceKill
echo.
echo %APP_NAME% (PID: %PID_TO_STOP%) did not stop gracefully after %WAIT_SECONDS% seconds. Sending force kill...
taskkill /F /PID %PID_TO_STOP% > nul 2>&1
timeout /t 1 /nobreak > nul REM Give OS time

REM Final check after force kill
tasklist /FI "PID eq %PID_TO_STOP%" /NH | findstr . > nul 2>&1
if errorlevel 1 (
    echo %APP_NAME% force-killed successfully.
    del "%PID_FILE%" > nul 2>&1
    exit /b 0
) else (
    echo Error: Failed to stop %APP_NAME% (PID: %PID_TO_STOP%) even with force kill. >&2
    exit /b 1
)
goto :EOF


REM --- show_status ---
:show_status
call :is_running
if %errorlevel% equ 0 (
    set /p RUNNING_PID=<"%PID_FILE%"
    echo %APP_NAME% is running (PID: %RUNNING_PID%).
) else (
    echo %APP_NAME% is not running.
)
exit /b 0
goto :EOF


REM --- restart_server ---
:restart_server
echo Restarting %APP_NAME%...
call :stop_server
set STOP_RESULT=%errorlevel%
if %STOP_RESULT% equ 1 (
    echo Previous instance was not running or failed to stop cleanly. Attempting start anyway...
)

REM Add a small delay
timeout /t 2 /nobreak > nul

call :start_server
set START_RESULT=%errorlevel%
exit /b %START_RESULT%
goto :EOF


REM === Main Logic ===
:ProcessCommand
REM Check if any argument is provided
if "%~1"=="" (
    call :show_help
    exit /b 1
)

REM Process the command argument (case-insensitive)
set "COMMAND=%~1"
set "COMMAND_HANDLED=0"

if /I "%COMMAND%"=="start"   ( call :start_server   & set COMMAND_HANDLED=1 & goto EndCommandProcessing )
if /I "%COMMAND%"=="stop"    ( call :stop_server    & set COMMAND_HANDLED=1 & goto EndCommandProcessing )
if /I "%COMMAND%"=="restart" ( call :restart_server & set COMMAND_HANDLED=1 & goto EndCommandProcessing )
if /I "%COMMAND%"=="status"  ( call :show_status    & set COMMAND_HANDLED=1 & goto EndCommandProcessing )
if /I "%COMMAND%"=="help"    ( call :show_help      & set COMMAND_HANDLED=1 & goto EndCommandProcessing )
if /I "%COMMAND%"=="--help"  ( call :show_help      & set COMMAND_HANDLED=1 & goto EndCommandProcessing )
if /I "%COMMAND%"=="-h"      ( call :show_help      & set COMMAND_HANDLED=1 & goto EndCommandProcessing )

:EndCommandProcessing
REM Store errorlevel from the called subroutine before any other commands run
set FINAL_ERRORLEVEL=%errorlevel%

if "%COMMAND_HANDLED%"=="0" (
    echo Error: Invalid command '%COMMAND%' >&2
    call :show_help
    set FINAL_ERRORLEVEL=1
)

REM Exit with the status code from the executed command
endlocal & exit /b %FINAL_ERRORLEVEL%