@echo off
rem frc: one entry point for tests, headless simulation, replay and log queries (Windows).
rem Usage: frc.cmd test ^| sim <scenario> ^| sim --list ^| replay <log> ^| log <keys^|stats^|diff> ...
cd /d "%~dp0"
if "%JAVA_HOME%"=="" if exist "%USERPROFILE%\wpilib\2027_alpha7\jdk" set "JAVA_HOME=%USERPROFILE%\wpilib\2027_alpha7\jdk"
set "CMD=%~1"
if "%CMD%"=="" goto usage
if "%CMD%"=="test" (
  call gradlew.bat test
  exit /b %ERRORLEVEL%
)
if "%CMD%"=="sim" (
  if "%~2"=="" goto usage
  if "%~2"=="--list" (
    call gradlew.bat -q --console=plain listScenarios
    exit /b %ERRORLEVEL%
  )
  if exist "build\sim\%~2\summary.json" del "build\sim\%~2\summary.json"
  if not exist build mkdir build
  set "SIM_SCENARIO=%~2"
  call gradlew.bat -q --console=plain run > build\frc-sim.log 2>&1
  if not exist "build\sim\%~2\summary.json" (
    echo frc: no summary produced for scenario %~2; see build\frc-sim.log 1>&2
    exit /b 2
  )
  type "build\sim\%~2\summary.json"
  findstr /C:"\"pass\":true" "build\sim\%~2\summary.json" > nul
  exit /b %ERRORLEVEL%
)
if "%CMD%"=="replay" (
  if "%~2"=="" goto usage
  if not exist "%~2" (
    echo frc: log not found: %~2 1>&2
    exit /b 2
  )
  set "AKIT_LOG_PATH=%~f2"
  call gradlew.bat -q --console=plain run > build\frc-replay.log 2>&1
  if errorlevel 1 (
    echo frc: replay failed; see build\frc-replay.log 1>&2
    exit /b 1
  )
  echo {"replayLog":"%~dpn2_sim.wpilog"}
  exit /b 0
)
if "%CMD%"=="log" (
  shift
  call gradlew.bat -q --console=plain frcLog --args="%*"
  exit /b %ERRORLEVEL%
)
:usage
echo usage: frc.cmd test ^| sim ^<scenario^> ^| sim --list ^| replay ^<log^> ^| log ^<keys^|stats^|diff^> ... 1>&2
exit /b 2
