#!/usr/bin/env python3
"""
CherrygramNB Telegram Bot Publishe
Sends built APKs to https://t.me/CherrygramNB (-1004391377976)
and publishes update metadata for client in-app updater.
"""

import sys
import os
import glob
import json
import time
import uuid
import mimetypes
import urllib.request
import urllib.error
import urllib.parse
from datetime import datetime

DEFAULT_CHANNEL_ID = "-1004391377976"
PROJECT_DIR = os.path.abspath(os.path.join(os.path.dirname(__file__), ".."))

def load_env_file(filepath):
    config = {}
    if os.path.exists(filepath):
        with open(filepath, "r", encoding="utf-8", errors="ignore") as f:
            for line in f:
                line = line.strip()
                if line and not line.startswith("#") and "=" in line:
                    k, v = line.split("=", 1)
                    config[k.strip()] = v.strip().strip("\"'")
    return config

def get_gradle_properties():
    props_path = os.path.join(PROJECT_DIR, "gradle.properties")
    return load_env_file(props_path)

def send_telegram_request(token, method, data=None, headers=None):
    url = f"https://api.telegram.org/bot{token}/{method}"
    if headers is None:
        headers = {}
    req = urllib.request.Request(url, data=data, headers=headers)
    try:
        with urllib.request.urlopen(req, timeout=120) as resp:
            return json.loads(resp.read().decode("utf-8"))
    except urllib.error.HTTPError as e:
        err_body = e.read().decode("utf-8", errors="ignore")
        return {"ok": False, "error_code": e.code, "description": err_body}
    except Exception as e:
        return {"ok": False, "description": str(e)}

def send_message(token, chat_id, text, parse_mode="Markdown"):
    payload = json.dumps({
        "chat_id": chat_id,
        "text": text,
        "parse_mode": parse_mode,
        "disable_web_page_preview": True
    }).encode("utf-8")
    return send_telegram_request(token, "sendMessage", data=payload, headers={"Content-Type": "application/json"})

def send_document(token, chat_id, filepath, caption=None, parse_mode="Markdown"):
    boundary = "----WebKitFormBoundary" + uuid.uuid4().hex
    filename = os.path.basename(filepath)
    content_type = mimetypes.guess_type(filepath)[0] or "application/vnd.android.package-archive"
    
    file_size = os.path.getsize(filepath)
    print(f"[*] Uploading {filename} ({file_size / (1024*1024):.2f} MB)...")
    
    body = bytearray()
    
    def add_field(name, value):
        nonlocal body
        body.extend(f"--{boundary}\r\n".encode("utf-8"))
        body.extend(f'Content-Disposition: form-data; name="{name}"\r\n\r\n'.encode("utf-8"))
        body.extend(f"{value}\r\n".encode("utf-8"))
        
    add_field("chat_id", str(chat_id))
    if caption:
        add_field("caption", caption)
        add_field("parse_mode", parse_mode)
        
    body.extend(f"--{boundary}\r\n".encode("utf-8"))
    body.extend(f'Content-Disposition: form-data; name="document"; filename="{filename}"\r\n'.encode("utf-8"))
    body.extend(f"Content-Type: {content_type}\r\n\r\n".encode("utf-8"))
    
    with open(filepath, "rb") as f:
        body.extend(f.read())
    body.extend(b"\r\n")
    body.extend(f"--{boundary}--\r\n".encode("utf-8"))
    
    headers = {
        "Content-Type": f"multipart/form-data; boundary={boundary}",
        "Content-Length": str(len(body))
    }
    
    return send_telegram_request(token, "sendDocument", data=body, headers=headers)

def find_latest_apks(build_type="release"):
    pattern = os.path.join(PROJECT_DIR, "TMessagesProj_App", "build", "outputs", "apk", "afat", build_type, "*.apk")
    apks = glob.glob(pattern)
    if not apks:
        pattern2 = os.path.join(PROJECT_DIR, "TMessagesProj_App", "build", "outputs", "apk", "**", "*.apk")
        apks = glob.glob(pattern2, recursive=True)
    apks.sort(key=os.path.getmtime, reverse=True)
    return apks

def main():
    env_file = os.path.join(PROJECT_DIR, ".env")
    env = load_env_file(env_file)
    props = get_gradle_properties()
    
    bot_token = os.environ.get("TELEGRAM_BOT_TOKEN") or env.get("TELEGRAM_BOT_TOKEN") or env.get("BOT_TOKEN")
    chat_id = os.environ.get("TELEGRAM_CHAT_ID") or env.get("TELEGRAM_CHAT_ID") or DEFAULT_CHANNEL_ID
    
    if not bot_token:
        print("[-] Error: TELEGRAM_BOT_TOKEN is not set in .env or environment!")
        print("    Add TELEGRAM_BOT_TOKEN=your_bot_token to e:\\prjects\\CherrygramNB\\.env")
        sys.exit(1)
        
    cherry_version = props.get("APP_VERSION_NAME_CHERRY", "11.3.0")
    tg_version = props.get("APP_VERSION_NAME", "12.10.6")
    version_code = int(props.get("APP_VERSION_CODE", "7112"))
    build_flavor = "Release"
    
    print(f"[*] CherrygramNB Publisher initialized")
    print(f"    Version: {cherry_version} (TG {tg_version}), code {version_code}")
    print(f"    Target channel: {chat_id}")
    
    # Test bot connectivity
    me = send_telegram_request(bot_token, "getMe")
    if not me.get("ok"):
        print(f"[-] Bot token error: {me.get('description')}")
        sys.exit(1)
    print(f"[+] Bot connected: @{me['result'].get('username')}")
    
    target_apks = []
    if len(sys.argv) > 1 and not sys.argv[1].startswith("-"):
        for arg in sys.argv[1:]:
            if os.path.isfile(arg):
                target_apks.append(arg)
    
    if not target_apks:
        found = find_latest_apks("release")
        if found:
            target_apks = [found[0]]
            
    if not target_apks:
        print("[-] No APKs found to upload! Please compile first.")
        sys.exit(1)
        
    for apk_path in target_apks:
        filename = os.path.basename(apk_path)
        abi = "universal"
        if "arm64-v8a" in filename:
            abi = "arm64-v8a"
        elif "armeabi-v7a" in filename:
            abi = "armeabi-v7a"
        elif "x86_64" in filename:
            abi = "x86_64"
        elif "x86" in filename:
            abi = "x86"
            
        caption = (
            f"🍒 *CherrygramNB v{cherry_version} ({version_code}) {build_flavor}*\n\n"
            f"📦 *Файл:* `{filename}`\n"
            f"📱 *Архитектура:* `{abi.capitalize()}`\n"
            f"⚡ *База:* Telegram v{tg_version}\n"
            f"👤 *Автор форка:* @pon4ikisdonut\n"
            f"📢 *Канал:* @CherrygramNB\n\n"
            f"#CherrygramNB #{build_flavor} #Update"
        )
        
        print(f"[*] Sending {filename} to {chat_id}...")
        resp = send_document(bot_token, chat_id, apk_path, caption=caption)
        
        if not resp.get("ok"):
            print(f"[-] Failed to upload {filename}: {resp.get('description')}")
            # If 50MB Bot API limit is hit
            if "file is too large" in resp.get("description", "").lower() or "413" in str(resp.get("error_code")):
                print("[!] Note: Telegram standard Bot API has a 50MB file size limit for direct bot uploads.")
                print("[!] For files >50MB, use a local telegram-bot-api server or MTProto client.")
            continue
            
        msg_id = resp["result"]["message_id"]
        print(f"[+] APK sent successfully! Message ID: {msg_id}")
        
        # Now publish the client in-app updater metadata post
        release_date = datetime.now().strftime("%d.%m.%Y")
        tag = "#updateRelease" if build_flavor.lower() == "release" else "#updateBeta"
        metadata = {
            "build_flavor": build_flavor,
            "release_date": release_date,
            "can_not_skip": False,
            "version": cherry_version,
            "version_code": version_code,
            "sticker": 0,
            "message": msg_id,
            "document": {
                abi: msg_id,
                "universal": msg_id,
                "arm64-v8a": msg_id,
                "armeabi-v7a": msg_id
            },
            "url": "https://t.me/CherrygramNB"
        }
        
        meta_post = f"{tag}\n" + json.dumps(metadata, indent=2)
        print(f"[*] Posting in-app update metadata to {chat_id}...")
        meta_resp = send_message(bot_token, chat_id, meta_post, parse_mode=None)
        if meta_resp.get("ok"):
            print(f"[+] In-app update metadata posted successfully! Message ID: {meta_resp['result']['message_id']}")
        else:
            print(f"[-] Failed to post update metadata: {meta_resp.get('description')}")

if __name__ == "__main__":
    main()
