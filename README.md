# 🍒 CherrygramNB

<p align="center">
  <img src="TMessagesProj_App/src/main/res/mipmap-xxxhdpi/ic_launcher.png" width="128" height="128" alt="CherrygramNB Logo"/>
</p>

<h3 align="center">Clean, Fast & Independent Telegram Client</h3>

<p align="center">
  <a href="https://t.me/CherrygramNB"><img src="https://img.shields.io/badge/Channel-%40CherrygramNB-blue?style=flat&logo=telegram" alt="Telegram Channel"></a>
  <a href="https://t.me/cherrygramnbchat"><img src="https://img.shields.io/badge/Chat-%40cherrygramnbchat-blue?style=flat&logo=telegram" alt="Telegram Chat"></a>
  <a href="https://ko-fi.com/pon4ikisdonut"><img src="https://img.shields.io/badge/Support-Ko--fi-ff5f5f?style=flat&logo=ko-fi" alt="Ko-fi"></a>
  <a href="https://github.com/pon4ikisdonut/CherrygramNB"><img src="https://img.shields.io/badge/Source-GitHub-black?style=flat&logo=github" alt="GitHub"></a>
</p>

---

## 📖 About

**CherrygramNB** is an advanced third-party fork of Cherrygram for Android, built on top of the latest **Telegram v12.10.6** (source codebase) and Cherrygram v11.3.0.

CherrygramNB is completely decoupled from Google Play Store restrictions, removes all invasive monetization bloat, includes native developer tools, and bypasses artificial restrictions.

* **Fork Maintainer:** [@pon4ikisdonut](https://t.me/pon4ikisdonut)
* **Official Channel:** [@CherrygramNB](https://t.me/CherrygramNB)
* **Community Chat:** [@cherrygramnbchat](https://t.me/cherrygramnbchat)

---

## ⚡ Features & Enhancements

### 1. 🛠️ Developer Tools (DevTools)
* **Raw JSON Inspector:** Dedicated button in the message context menu to view raw `TLRPC.Message` JSON and fields in real time.
* **Quick Copy ID:** Display user, group, and channel IDs directly in profile headers with instant one-click copy to clipboard.
* **Bot Token Manager:** Built-in bot authorization (`TL_auth_importBotAuthorization`) allowing you to log in directly via Bot Token from `@BotFather` to test and debug bots from your phone.

### 2. 🛡️ Hardcore Filtering & Anti-Spam
* **Regex Channel Filters:** Locally filter out advertisements, cryptocurrency shills, and casino spam from your channel feed using custom regular expressions.
* **Auto-Mute Unknowns:** Automatically archive and mute private chats from non-contacts.

### 3. 🧹 De-bloat & Clean UI
* **Total Stories Removal:** Toggle to hide the top stories bar and colored rings around user avatars across dialogs and profiles.
* **No Sponsored Messages:** Completely neutralizes and removes Telegram sponsored/ad messages from channels.
* **Minimal Attachment Menu:** Option to declutter the attachment bottom sheet to only essential actions.
* **Zero Bloat:** SafeStars and SafePay mechanisms completely excised.

### 4. 🔓 Content & Restrictions Bypass
* **Copy Protection Bypass (No-Quote):** Copy text, save media, and forward messages even in channels and groups where copy protection (`noforwards`) is enforced.
* **Screenshot & Screen Recording Freedom:** Neutralizes `FLAG_SECURE` window locks.
* **Local Voice-to-Text:** Free voice message transcription using Android's on-device speech recognizer without requiring Telegram Premium.
* **Anti-Delete Messages:** Local caching marks deleted messages with an `[Удалено]` indicator rather than disappearing from your chat.

### 5. 🔒 Privacy & Security
* **Streamer Mode:** One-tap toggle that blurs/masks your phone number, username, and avatar in the drawer and settings for safe streaming and screenshot sharing.
* **Hidden Chats:** Hide sensitive dialogues with PIN/passcode access.
* **Fake Device Profile:** Spoof device model and client metadata in active sessions.
* **Session Location & IP Spoofing:** Spoof country, region, and IP address for active sessions in Settings > Devices to prevent leaking your real location.
* **Anti-Tamper Protection:** Neutralized upstream anti-tamper crashes, remote kick triggers, and forced session wipes.

### 6. 🎨 Interface & Ergonomics
* **Bottom Folder Tabs (Bottom Bar):** Ergonomic navigation bar placed at the bottom for effortless one-handed use on large screens.
* **Dynamic Material You:** System wallpaper accent color harmony across icons, buttons, and switches.
* **Custom Typography:** Full support for loading external `.ttf` and `.otf` fonts.
* **Fixed Header & Nicknames:** Guaranteed proper rendering of forwarded author headers and contact nicknames without displacement glitches.

### 7. 🚀 Productivity & Updates
* **Independent In-App Updater:** Autonomous background APK updater pulling releases and betas directly from [@CherrygramNB](https://t.me/CherrygramNB) with version intelligence.
* **Smart Quote Auto-Responder:** Automatically responds to incoming live direct messages with real quote replies and smart cooldowns (never triggers on historical messages).
* **Canned Responses:** Pre-configured quick-reply snippets in the message input bar.
* **Unlimited Multi-Account:** Expanded account limit supporting up to 20 active profiles simultaneously.

---

## 🏗️ Fast Compilation Guide

### Requirements
* **OS:** Linux or Windows with **WSL2 (Ubuntu 22.04)**
* **Android SDK:** Platform 36, Build-Tools 36.0.0, NDK 27.2.12479018, CMake 3.22.1
* **Java:** OpenJDK 17

### Environment Configuration (`.env`)
Create a `.env` file in the project root:
```bash
CHERRYGRAM_KEY_PASSWORD=your_keystore_password
TELEGRAM_BOT_TOKEN=your_bot_token
TELEGRAM_CHAT_ID=-1004391377976
TG_APP_ID=your_api_id
TG_APP_HASH=your_api_hash
```

### ⚡ Accelerated Single-ABI Build (~3–5 minutes)
Building all 4 ABIs (`arm64-v8a`, `armeabi-v7a`, `x86_64`, `x86`) sequentially takes ~25–40 minutes.
For development or testing on modern Android phones, build **`arm64-v8a` only**:

```bash
# In WSL:
cd /mnt/e/prjects/CherrygramNB
./build_and_publish.sh beta arm64-v8a

# Or from Windows:
build_and_publish.bat beta arm64-v8a
```

### 📦 Full Universal Release Build
```bash
./build_and_publish.sh release
```

Built APKs are output to `TMessagesProj_App/build/outputs/apk/afat/release/`.

---

## 🤖 Telegram Bot Publisher

CherrygramNB includes an automated publisher (`scripts/bot_publisher.py`):
* Uses **Telethon MTProto** to bypass Telegram Bot API's 50MB file size restriction (supporting files up to 2GB).
* Automatically uploads signed APKs to [@CherrygramNB](https://t.me/CherrygramNB).
* Posts `#updateRelease` / `#updateBeta` metadata posts enabling client in-app auto-updates.

---

## 📄 License & Credits
Licensed under the **GNU General Public License v2.0 or later**.

Special thanks to:
* Original Telegram Android Team ([DrKLO/Telegram](https://github.com/DrKLO/Telegram))
* Cherrygram upstream author ([arsLan4k1390](https://github.com/arsLan4k1390))
* [Catogram](https://github.com/Catogram/Catogram) & [Nekogram](https://gitlab.com/Nekogram/Nekogram)
