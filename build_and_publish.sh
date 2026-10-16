#!/bin/bash
set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

if [ -f .env ]; then
    set -a
    . ./.env
    set +a
fi

if [ -z "$CHERRYGRAM_KEY_PASSWORD" ]; then
    echo "[-] Error: CHERRYGRAM_KEY_PASSWORD is not set in .env!"
    exit 1
fi

export GRADLE_USER_HOME="${GRADLE_USER_HOME:-$HOME/.gradle}"
chmod +x ./gradlew

BUILD_TYPE="${1:-release}"

# Generate Proguard dictionaries if missing
DICT_DIR="TMessagesProj_AppStandalone/build/tmp/dictionary"
if [ ! -f "$DICT_DIR/class_obfuscation_dictionary.txt" ]; then
    echo "[*] Generating Proguard dictionaries..."
    ./gradlew :TMessagesProj_AppStandalone:generateProguardDictAfatStandalone --console=plain
fi

echo "============================================="
echo "   Building CherrygramNB ($BUILD_TYPE)"
echo "============================================="

if [ "$BUILD_TYPE" = "beta" ]; then
    ./gradlew :TMessagesProj_App:assembleAfatBeta --console=plain
else
    ./gradlew :TMessagesProj_App:assembleAfatRelease --console=plain
fi

echo "[+] Build finished successfully!"

if [ -n "$TELEGRAM_BOT_TOKEN" ]; then
    echo "[*] Running Telegram Bot Publisher..."
    python3 scripts/bot_publisher.py
else
    echo "[!] TELEGRAM_BOT_TOKEN not found in .env. Skipping Telegram upload."
    echo "    To enable publishing to https://t.me/CherrygramNB, add TELEGRAM_BOT_TOKEN to .env"
fi
