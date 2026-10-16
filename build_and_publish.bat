@echo off
setlocal
echo ===================================================
echo     CherrygramNB Build and Telegram Publisher
echo ===================================================

set TARGET=%1
if "%TARGET%"=="" set TARGET=release
set ABI=%2

echo Launching build in WSL (%TARGET% %ABI%)...
wsl -d Ubuntu-22.04 -- bash /mnt/e/prjects/CherrygramNB/build_and_publish.sh %TARGET% %ABI%

if %ERRORLEVEL% EQU 0 (
    echo.
    echo [+] Build and publish completed successfully!
) else (
    echo.
    echo [-] Build failed with error code %ERRORLEVEL%
)

pause
