#!/bin/bash
set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

if [ -f secrets_for_ci.env ]; then
    set -a
    . ./secrets_for_ci.env
    set +a
fi

if [ -f .env ]; then
    set -a
    . ./.env
    set +a
fi

export CHERRYGRAM_KEY_PASSWORD="$(echo -n "$CHERRYGRAM_KEY_PASSWORD" | tr -d '\r')"
export TELEGRAM_BOT_TOKEN="$(echo -n "$TELEGRAM_BOT_TOKEN" | tr -d '\r')"
export TELEGRAM_CHAT_ID="$(echo -n "$TELEGRAM_CHAT_ID" | tr -d '\r')"
export TG_APP_ID="$(echo -n "$TG_APP_ID" | tr -d '\r')"
export TG_APP_HASH="$(echo -n "$TG_APP_HASH" | tr -d '\r')"

if [ -z "$CHERRYGRAM_KEY_PASSWORD" ]; then
    echo "[-] Error: CHERRYGRAM_KEY_PASSWORD is not set in .env!"
    exit 1
fi

export GRADLE_USER_HOME="${GRADLE_USER_HOME:-$HOME/.gradle}"
chmod +x ./gradlew

BUILD_TYPE="${1:-release}"
TARGET_ABI="$2"

ABI_PARAM=""
if [ -n "$TARGET_ABI" ]; then
    ABI_PARAM="-PtargetAbi=$TARGET_ABI"
    echo "[*] Fast build mode enabled for ABI: $TARGET_ABI"
fi

# Generate Proguard dictionaries if missing
DICT_DIR="TMessagesProj_AppStandalone/build/tmp/dictionary"
if [ ! -f "$DICT_DIR/class_obfuscation_dictionary.txt" ]; then
    echo "[*] Generating Proguard dictionaries..."
    ./gradlew :TMessagesProj_AppStandalone:generateProguardDictAfatStandalone --console=plain
fi

echo "============================================="
echo "   Building CherrygramNB ($BUILD_TYPE $TARGET_ABI)"
echo "============================================="

if [ "$BUILD_TYPE" = "beta" ]; then
    ./gradlew :TMessagesProj_App:assembleAfatRelease -PIS_BETA=true $ABI_PARAM --console=plain
    EXTRA_FLAG="--beta"
else
    ./gradlew :TMessagesProj_App:assembleAfatRelease $ABI_PARAM --console=plain
    EXTRA_FLAG=""
fi

echo "[+] Build finished successfully!"

if [ -n "$TELEGRAM_BOT_TOKEN" ]; then
    echo "[*] Running Telegram Bot Publisher..."
    python3 scripts/bot_publisher.py $EXTRA_FLAG
else
    echo "[!] TELEGRAM_BOT_TOKEN not found in .env. Skipping Telegram upload."
    echo "    To enable publishing to https://t.me/CherrygramNB, add TELEGRAM_BOT_TOKEN to .env"
fi
