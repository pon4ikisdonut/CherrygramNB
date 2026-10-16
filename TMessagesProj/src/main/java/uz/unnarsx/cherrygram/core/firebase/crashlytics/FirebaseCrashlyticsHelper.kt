/**
 * This is the source code of Cherrygram for Android.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 * Please, be respectful and credit the original author if you use this code.
 *
 * Copyright github.com/arsLan4k1390, 2022-2026.
 */

package uz.unnarsx.cherrygram.core.firebase.crashlytics

import android.content.Context
import android.os.Build
import com.google.firebase.crashlytics.FirebaseCrashlytics
import androidx.core.content.edit
import org.telegram.messenger.ApplicationLoader
import org.telegram.tgnet.TLRPC
import uz.unnarsx.cherrygram.chats.helpers.ChatsHelper2
import uz.unnarsx.cherrygram.core.configs.CherrygramAppearanceConfig
import uz.unnarsx.cherrygram.core.configs.CherrygramCoreConfig
import uz.unnarsx.cherrygram.core.configs.CherrygramMessagesConfig
import uz.unnarsx.cherrygram.core.helpers.CGResourcesHelper

object FirebaseCrashlyticsHelper {

    private val crashlytics: FirebaseCrashlytics?
        get() = try {
            FirebaseCrashlytics.getInstance()
        } catch (t: Throwable) {
            null
        }

    fun updateUser(user: TLRPC.User?) {
        try {
            val c = crashlytics ?: return
            if (user == null) {
                c.setUserId("")
                c.setCustomKey("userId", "")
                c.setCustomKey("publicName", "")
                return
            }

            c.setUserId(user.id.toString())
            c.setCustomKey("userId", user.id.toString())
            c.setCustomKey(
                "publicName",
                "@${ChatsHelper2.getActiveUsername(user.id)}"
            )
            c.setCustomKey("buildDate", CGResourcesHelper.getBuildDate())
            c.setCustomKey("flavor", CGResourcesHelper.getBuildType())
        } catch (t: Throwable) {
            // Ignored in CherrygramNB
        }
    }

    fun logAsNonFatal(e: Throwable) {
        try {
            val c = crashlytics ?: return
            c.setCustomKey("event_type", "non_fatal")
            c.recordException(e)
        } catch (t: Throwable) {
        } finally {
            clearEventKeys()
        }
    }

    fun logToCrashlytics(context: Context?, userId: Long) {
        // Disabled spy / tamper logger in CherrygramNB
    }

    private fun clearEventKeys() {
        try {
            val c = crashlytics ?: return
            c.setCustomKey("event_type", "")
            c.setCustomKey("tamper_count", 0)
            c.setCustomKey("premium_share_count", 0)
            c.setCustomKey("optionType", "")

            c.setCustomKey("sv_checkContent", false)
            c.setCustomKey("sv_has_jni_hook", false)
            c.setCustomKey("sv_has_xhook", false)
            c.setCustomKey("sv_validate_signature", false)
            c.setCustomKey("sv_check_signature", false)

            c.setCustomKey("cert_found_apk_fd", false)
            c.setCustomKey("cert_empty", false)
            c.setCustomKey("cert_size", false)
            c.setCustomKey("cert_crc", false)

            c.setCustomKey("ios_msg_menu_enabled", false)
            c.setCustomKey("compact_msg_menu_enabled", false)
            c.setCustomKey("folders_at_bottom_enabled", false)
        } catch (t: Throwable) {
        }
    }

    class BlaBlaBlaDebug(message: String) : Exception(message)

}