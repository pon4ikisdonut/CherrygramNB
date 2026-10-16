@echo off
setlocal
echo ===================================================
echo     CherrygramNB Docker Builder
echo ===================================================

echo Starting Docker container build on E:...
wsl -d Ubuntu-22.04 -- bash /mnt/e/prjects/CherrygramNB/docker_build.sh

if %ERRORLEVEL% EQU 0 (
    echo.
    echo [+] Docker build completed successfully!
) else (
    echo.
    echo [-] Docker build failed with error code %ERRORLEVEL%
)

pause
