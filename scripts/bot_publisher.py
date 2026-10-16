#!/usr/bin/env python3
"""
CherrygramNB Telegram Bot Publisher
Sends built APKs to https://t.me/CherrygramNB (-1004391377976)
and publishes update metadata for client in-app updater.
Supports both MTProto (via Telethon for large APK files >50MB) and standard Bot API.
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
    payload_dict = {
        "chat_id": chat_id,
        "text": text,
        "disable_web_page_preview": True
    }
    if parse_mode:
        payload_dict["parse_mode"] = parse_mode
    payload = json.dumps(payload_dict).encode("utf-8")
    return send_telegram_request(token, "sendMessage", data=payload, headers={"Content-Type": "application/json"})

def send_document_bot_api(token, chat_id, filepath, caption=None, parse_mode="Markdown"):
    boundary = "----WebKitFormBoundary" + uuid.uuid4().hex
    filename = os.path.basename(filepath)
    content_type = mimetypes.guess_type(filepath)[0] or "application/vnd.android.package-archive"
    
    file_size = os.path.getsize(filepath)
    print(f"[*] Uploading {filename} ({file_size / (1024*1024):.2f} MB) via HTTP Bot API...")
    
    body = bytearray()
    
    def add_field(name, value):
        nonlocal body
        body.extend(f"--{boundary}\r\n".encode("utf-8"))
        body.extend(f'Content-Disposition: form-data; name="{name}"\r\n\r\n'.encode("utf-8"))
        body.extend(f"{value}\r\n".encode("utf-8"))
        
    add_field("chat_id", str(chat_id))
    if caption:
        add_field("caption", caption)
        if parse_mode:
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

def send_document_telethon(api_id, api_hash, bot_token, chat_id, filepath, caption=None):
    from telethon.sync import TelegramClient
    session_name = f"/tmp/cg_bot_{uuid.uuid4().hex[:8]}" if os.name != "nt" else os.path.join(PROJECT_DIR, ".bot_upload.session")
    client = TelegramClient(session_name, int(api_id), api_hash)
    try:
        client.start(bot_token=bot_token)
        try:
            target = int(chat_id)
        except ValueError:
            target = chat_id

        filename = os.path.basename(filepath)
        file_size = os.path.getsize(filepath)
        print(f"[*] Uploading {filename} ({file_size / (1024*1024):.2f} MB) via Telethon MTProto...")

        last_pct = [-1]
        def progress_callback(current, total):
            pct = int(current * 100 / total)
            if pct % 10 == 0 and pct != last_pct[0]:
                last_pct[0] = pct
                print(f"    Progress: {pct}% ({current / (1024*1024):.1f}/{total / (1024*1024):.1f} MB)")

        msg = client.send_file(
            target,
            filepath,
            caption=caption,
            parse_mode="md",
            progress_callback=progress_callback
        )
        return {"ok": True, "result": {"message_id": msg.id}}
    except Exception as e:
        return {"ok": False, "description": str(e)}
    finally:
        try:
            client.disconnect()
        except Exception:
            pass
        if os.name == "nt" and os.path.exists(session_name):
            try:
                os.remove(session_name)
            except Exception:
                pass

def send_document(token, chat_id, filepath, caption=None, api_id=None, api_hash=None):
    # If Telethon is available and API credentials are provided, use MTProto (supports up to 2GB)
    if api_id and api_hash:
        try:
            import telethon
            return send_document_telethon(api_id, api_hash, token, chat_id, filepath, caption)
        except ImportError:
            print("[!] Telethon not installed, falling back to HTTP Bot API...")
        except Exception as e:
            print(f"[!] Telethon error: {e}, falling back to HTTP Bot API...")

    return send_document_bot_api(token, chat_id, filepath, caption)

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
    ci_env_file = os.path.join(PROJECT_DIR, "secrets_for_ci.env")
    ci_env = load_env_file(ci_env_file)
    props = get_gradle_properties()
    
    bot_token = os.environ.get("TELEGRAM_BOT_TOKEN") or env.get("TELEGRAM_BOT_TOKEN") or ci_env.get("TELEGRAM_BOT_TOKEN") or env.get("BOT_TOKEN")
    chat_id = os.environ.get("TELEGRAM_CHAT_ID") or env.get("TELEGRAM_CHAT_ID") or ci_env.get("TELEGRAM_CHAT_ID") or DEFAULT_CHANNEL_ID
    api_id = os.environ.get("TG_APP_ID") or env.get("TG_APP_ID") or ci_env.get("TG_APP_ID")
    api_hash = os.environ.get("TG_APP_HASH") or env.get("TG_APP_HASH") or ci_env.get("TG_APP_HASH")
    
    if not bot_token:
        print("[-] Error: TELEGRAM_BOT_TOKEN is not set in .env or environment!")
        print("    Add TELEGRAM_BOT_TOKEN=your_bot_token to e:\\prjects\\CherrygramNB\\.env")
        sys.exit(1)
        
    cherry_version = props.get("APP_VERSION_NAME_CHERRY", "11.3.0")
    tg_version = props.get("APP_VERSION_NAME", "12.10.6")
    version_code = int(props.get("APP_VERSION_CODE", "7112"))
    is_beta = "--beta" in sys.argv or "beta" in sys.argv
    build_flavor = "Beta" if is_beta else "Release"
    
    print(f"[*] CherrygramNB Publisher initialized")
    print(f"    Version: {cherry_version} (TG {tg_version}), code {version_code}")
    print(f"    Target channel: {chat_id}")
    print(f"    Flavor: {build_flavor}")
    
    # Test bot connectivity
    me = send_telegram_request(bot_token, "getMe")
    if not me.get("ok"):
        print(f"[-] Bot token error: {me.get('description')}")
        sys.exit(1)
    print(f"[+] Bot connected: @{me['result'].get('username')}")
    
    target_apks = []
    if len(sys.argv) > 1:
        for arg in sys.argv[1:]:
            if not arg.startswith("-") and arg != "beta" and arg != "release" and os.path.isfile(arg):
                target_apks.append(arg)
    
    if not target_apks:
        found = find_latest_apks("release")
        if is_beta:
            beta_apks = [a for a in found if "-beta" in os.path.basename(a).lower()]
            if beta_apks:
                target_apks = [beta_apks[0]]
            elif found:
                target_apks = [found[0]]
        else:
            rel_apks = [a for a in found if "-beta" not in os.path.basename(a).lower()]
            if rel_apks:
                target_apks = [rel_apks[0]]
            elif found:
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
        resp = send_document(bot_token, chat_id, apk_path, caption=caption, api_id=api_id, api_hash=api_hash)
        
        if not resp.get("ok"):
            print(f"[-] Failed to upload {filename}: {resp.get('description')}")
            continue
            
        msg_id = resp["result"]["message_id"]
        print(f"[+] APK sent successfully! Message ID: {msg_id}")
        
        # Now publish the client in-app updater metadata post
        release_date = datetime.now().strftime("%d.%m.%Y")
        tag = "#updateBeta" if is_beta else "#updateRelease"
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
