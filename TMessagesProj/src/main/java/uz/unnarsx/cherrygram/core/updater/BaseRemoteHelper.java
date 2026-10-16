/**
 * This is the source code of Cherrygram for Android.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 * Please, be respectful and credit the original author if you use this code.
 *
 * Copyright github.com/arsLan4k1390, 2022-2026.
 */

package uz.unnarsx.cherrygram.core.updater;

import android.app.Activity;
import android.content.SharedPreferences;
import android.text.TextUtils;

import org.json.JSONException;
import org.json.JSONObject;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.FileLoader;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.MessagesStorage;
import org.telegram.messenger.UserConfig;
import org.telegram.tgnet.ConnectionsManager;
import org.telegram.tgnet.TLObject;
import org.telegram.tgnet.TLRPC;

import java.util.ArrayList;

import uz.unnarsx.cherrygram.core.CherrygramLogger;
import uz.unnarsx.cherrygram.core.configs.CherrygramCoreConfig;
import uz.unnarsx.cherrygram.misc.Constants;

public abstract class BaseRemoteHelper {

    protected static final SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("cherryremoteconfig", Activity.MODE_PRIVATE);

    protected MessagesController getMessagesController() {
        return MessagesController.getInstance(UserConfig.selectedAccount);
    }

    protected ConnectionsManager getConnectionsManager() {
        return ConnectionsManager.getInstance(UserConfig.selectedAccount);
    }

    protected MessagesStorage getMessagesStorage() {
        return MessagesStorage.getInstance(UserConfig.selectedAccount);
    }

    protected FileLoader getFileLoader() {
        return FileLoader.getInstance(UserConfig.selectedAccount);
    }

    abstract protected void onError(String text, Delegate delegate);

    abstract protected String getTag();

    protected void onLoadSuccess(ArrayList<JSONObject> responses, Delegate delegate) {
        var tag = getTag();
        var json = !responses.isEmpty() ? responses.get(0) : null;
        if (json == null) {
            preferences.edit()
                    .remove(tag + "_update_time")
                    .remove(tag)
                    .apply();
        } else {
            preferences.edit()
                    .putLong(tag + "_update_time", System.currentTimeMillis())
                    .putString(tag, json.toString())
                    .apply();
        }
    }

    private void onGetMessageSuccess(TLObject response, Delegate delegate) {
        var tag = "#" + getTag();
        final var res = (TLRPC.messages_Messages) response;
        getMessagesController().removeDeletedMessagesFromArray(getUpdateChannelID(), res.messages);
        ArrayList<JSONObject> responses = new ArrayList<>();
        for (var message : res.messages) {
            if (TextUtils.isEmpty(message.message) || !message.message.contains(tag)) {
                continue;
            }
            try {
                String text = message.message;
                int tagIdx = text.indexOf(tag);
                String afterTag = text.substring(tagIdx + tag.length()).trim();

                JSONObject json = null;
                // 1. Try finding JSON within markdown code blocks
                if (afterTag.contains("```")) {
                    int startCode = afterTag.indexOf("```");
                    int endCode = afterTag.indexOf("```", startCode + 3);
                    if (endCode != -1) {
                        String block = afterTag.substring(startCode + 3, endCode).trim();
                        if (block.startsWith("json")) {
                            block = block.substring(4).trim();
                        }
                        if (block.startsWith("{") && block.endsWith("}")) {
                            json = new JSONObject(block);
                        }
                    }
                }

                // 2. Try finding JSON starting with { and ending with }
                if (json == null) {
                    int startBrace = afterTag.indexOf("{");
                    int endBrace = afterTag.lastIndexOf("}");
                    if (startBrace != -1 && endBrace != -1 && endBrace > startBrace) {
                        json = new JSONObject(afterTag.substring(startBrace, endBrace + 1));
                    }
                }

                // 3. Fallback: if message starts directly with tag
                if (json == null && text.startsWith(tag)) {
                    try {
                        json = new JSONObject(afterTag);
                    } catch (Exception ignored) {}
                }

                // 4. Auto-derive update metadata if message has an attached APK document and the tag
                if (json == null && message.media != null && message.media.document != null) {
                    json = new JSONObject();
                    json.put("build_flavor", tag.toLowerCase().contains("beta") ? "Beta" : "Release");
                    json.put("release_date", "");
                    json.put("can_not_skip", false);
                    json.put("sticker", 0);
                    json.put("message", message.id);
                    json.put("url", Constants.CG_CHANNEL_URL != null ? Constants.CG_CHANNEL_URL : "https://t.me/CherrygramNB");

                    JSONObject docs = new JSONObject();
                    docs.put("universal", message.id);
                    docs.put("arm64-v8a", message.id);
                    docs.put("armeabi-v7a", message.id);
                    json.put("document", docs);

                    String version = null;
                    int versionCode = 0;

                    java.util.regex.Pattern pVer = java.util.regex.Pattern.compile("v?([0-9]+\\.[0-9]+\\.[0-9]+)");
                    java.util.regex.Matcher mVer = pVer.matcher(text);
                    if (mVer.find()) {
                        version = mVer.group(1);
                    }

                    java.util.regex.Pattern pCode = java.util.regex.Pattern.compile("\\(([0-9]{4,})\\)");
                    java.util.regex.Matcher mCode = pCode.matcher(text);
                    if (mCode.find()) {
                        try {
                            versionCode = Integer.parseInt(mCode.group(1));
                        } catch (Exception ignored) {}
                    }

                    if (version == null) {
                        for (TLRPC.DocumentAttribute attr : message.media.document.attributes) {
                            if (attr instanceof TLRPC.TL_documentAttributeFilename) {
                                String fn = ((TLRPC.TL_documentAttributeFilename) attr).file_name;
                                if (fn != null) {
                                    java.util.regex.Matcher mFn = pVer.matcher(fn);
                                    if (mFn.find()) {
                                        version = mFn.group(1);
                                    }
                                }
                            }
                        }
                    }

                    if (version != null) {
                        json.put("version", version);
                        json.put("version_code", versionCode);
                    } else {
                        json = null;
                    }
                }

                if (json != null) {
                    // Auto-fill message and document IDs if the post itself has an attached APK
                    if (message.media != null && message.media.document != null) {
                        if (!json.has("message")) {
                            json.put("message", message.id);
                        }
                        if (!json.has("document")) {
                            JSONObject docs = new JSONObject();
                            docs.put("universal", message.id);
                            docs.put("arm64-v8a", message.id);
                            docs.put("armeabi-v7a", message.id);
                            json.put("document", docs);
                        }
                    }
                    responses.add(json);
                }
            } catch (Exception e) {
                CherrygramLogger.e(e);
            }
        }
        onLoadSuccess(responses, delegate);
    }

    public void load() {
        load(false, null);
    }

    public void load(Delegate delegate) {
        load(false, delegate);
    }

    private void load(boolean forceRefreshAccessHash, Delegate delegate) {
        var tag = "#" + getTag();
        TLRPC.TL_messages_search req = new TLRPC.TL_messages_search();
        req.limit = 10;
        req.offset_id = 0;
        req.filter = new TLRPC.TL_inputMessagesFilterEmpty();
        req.q = tag;
        req.peer = getMessagesController().getInputPeer(-getMetadataChannelID());
        if (req.peer == null || req.peer.access_hash == 0 || forceRefreshAccessHash) {
            TLRPC.TL_contacts_resolveUsername req1 = new TLRPC.TL_contacts_resolveUsername();
            req1.username = getMetadataChannel();
            getConnectionsManager().sendRequest(req1, (response1, error1) -> {
                if (error1 != null) {
                    return;
                }
                if (!(response1 instanceof TLRPC.TL_contacts_resolvedPeer resolvedPeer)) {
                    return;
                }
                getMessagesController().putUsers(resolvedPeer.users, false);
                getMessagesController().putChats(resolvedPeer.chats, false);
                getMessagesStorage().putUsersAndChats(resolvedPeer.users, resolvedPeer.chats, false, true);
                if ((resolvedPeer.chats == null || resolvedPeer.chats.isEmpty())) {
                    return;
                }
                req.peer = new TLRPC.TL_inputPeerChannel();
                req.peer.channel_id = resolvedPeer.chats.get(0).id;
                req.peer.access_hash = resolvedPeer.chats.get(0).access_hash;
                getConnectionsManager().sendRequest(req, (response, error) -> {
                    if (error == null) {
                        onGetMessageSuccess(response, delegate);
                    } else {
                        onError(error.text, delegate);
                    }
                });
            });
        } else {
            getConnectionsManager().sendRequest(req, (response, error) -> {
                if (error == null) {
                    onGetMessageSuccess(response, delegate);
                } else {
                    load(true, delegate);
                }
            });
        }
    }

    public interface Delegate {
        void onTLResponse(TLRPC.TL_help_appUpdate res, String error);
    }

    public static String getUpdateChannelUsername() {
        if (CherrygramCoreConfig.INSTANCE.getInstallBetas() && !CherrygramCoreConfig.isPlayStoreBuild()) {
            return Constants.CG_BETA_APKS_CHANNEL_USERNAME;
        } else {
            return Constants.CG_APKS_CHANNEL_USERNAME;
        }
    }

    public static long getUpdateChannelID() {
        return 4391377976L;
    }

    public static String getMetadataChannel() {
        return Constants.CG_CHANNEL_USERNAME;
    }

    public static long getMetadataChannelID() {
        return 4391377976L;
    }

}
