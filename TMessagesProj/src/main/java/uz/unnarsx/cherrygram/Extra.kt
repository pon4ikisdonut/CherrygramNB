/**
 * This is the source code of Cherrygram for Android.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 * Please, be respectful and credit the original author if you use this code.
 *
 * Copyright github.com/arsLan4k1390, 2022-2026.
 */

package uz.unnarsx.cherrygram

import android.app.Activity
import org.telegram.messenger.AndroidUtilities
import org.telegram.messenger.ChatObject
import org.telegram.messenger.LocaleController
import org.telegram.messenger.LocaleController.getString
import org.telegram.messenger.MessagesController
import org.telegram.messenger.R
import org.telegram.messenger.UserConfig
import org.telegram.tgnet.TLRPC
import org.telegram.ui.ActionBar.BaseFragment
import uz.unnarsx.cherrygram.core.helpers.CGResourcesHelper
import uz.unnarsx.cherrygram.helpers.UserHelper

object Extra {

    // https://core.telegram.org/api/obtaining_api_id

    fun getAppID() : Int {
        return 27827667
    }

    fun getAppHash() : String {
        return "8f885d1bebe32da81c42677c42aed659"
    }

    // https://developers.google.com/identity/sms-retriever/verify#computing_your_apps_hash_string
    const val SMS_HASH = ""

    /*
     * Added for Telegram v12.10.6 / Cherrygram 11.3.0.
     *
     * Upstream's main branch (commit 2cad8055b) references these from
     * KotlinFragmentsManager.kt (anti-tamper), SamsungDatastore.kt and the
     * Cherrygram*Config files, but the Extra.kt that ships those values is
     * gitignored and therefore not in the repo (nor in main_Reproducible_Builds).
     *
     * Values below are inert placeholders. The `contains(...)`-based ones MUST
     * stay non-empty, otherwise an empty needle makes the check always true and
     * the app restarts/kills itself on launch:
     *   Name_ArrTwo6 -> vreg42r2r2r1r3q1rq3() -> nfweioufwehr117() (app restart)
     *   Name_Arr/Name_Arr2/Name_Arr3/Name_ArrTwo -> killAppAndSendEvent()
     * The rest are only read by dead code (checkConnection() has its calls
     * commented out), so empty placeholders are fine there.
     */
    val HASH_ARRAY = arrayOf("")
    val APP_ARRAY = arrayOf("")
    val SMS_ARRAY = arrayOf("")

    val Name_Arr = arrayOf("cherrygram-placeholder-name-arr-1")
    val Name_Arr2 = arrayOf("cherrygram-placeholder-name-arr-2")
    val Name_Arr3 = arrayOf("cherrygram-placeholder-name-arr-3")
    val Name_ArrTwo = arrayOf("cherrygram-placeholder-name-arr-two")
    val Name_ArrTwo1 = arrayOf("")
    val Name_ArrTwo2 = arrayOf("")
    val Name_ArrTwo3 = arrayOf("")
    val Name_ArrTwo4 = arrayOf("")
    val Name_ArrTwo5 = arrayOf("")
    val Name_ArrTwo6 = arrayOf("cherrygram-placeholder-integrity-6")

    val XName_Arr = arrayOf("cherrygram-placeholder-xname-arr")
    val XName_ArrTwo = arrayOf("cherrygram-placeholder-xname-arr-two")

    val VERIF_LOG = arrayOf("")
    val TG_BLOCKED_URL = arrayOf("")

    /** Donates links start */
    val FILE_NAME_HASH = arrayOf("")
    val GITLAB_RAW_URL_HASH = arrayOf("")

    val FILE_NAME_MARKETPLACE_HASH = arrayOf("")
    val GITLAB_RAW_URL_MARKETPLACE_HASH = arrayOf("")

    val FILE_NAME_BLOCKED_HASH = arrayOf("")
    val GITLAB_RAW_URL_BLOCKED_HASH = arrayOf("")

    val FILE_NAME_BADGE_COLORS_HASH = arrayOf("")
    val GITLAB_RAW_URL_BADGE_COLORS_HASH = arrayOf("")

    val FILE_NAME_TON_RATE_HASH = arrayOf("dG9u", "X3Vz", "ZHRf", "cmF0", "ZS5q", "c29u")
    val TON_RATE_URL_HASH = arrayOf(
        "aHR0", "cHM6", "Ly9j", "ZG4u", "anNk", "ZWxp", "dnIu",
        "bmV0", "L25w", "bS9A", "ZmF3", "YXph", "aG1l", "ZDAv",
        "Y3Vy", "cmVu", "Y3kt", "YXBp", "QGxh", "dGVz", "dC92",
        "MS9j", "dXJy", "ZW5j", "aWVz", "L3Rv", "bi5q", "c29u"
    )
    /** Donates links finish */

    fun getRegistrationDate(fragment: BaseFragment?, userID: Long, chatId: Long): CharSequence {
        if (fragment == null) return ""

        if (chatId != 0L) {
            val chat = fragment.messagesController?.getChat(chatId)
            if (chat != null) {
                return if (ChatObject.isInChat(chat)) {
                    AndroidUtilities.replaceTags(
                        LocaleController.formatString(
                            R.string.CG_JoinedDate, chat.title,
                            LocaleController.formatDateTime(chat.date.toLong(), true)
                        )
                    )
                } else {
                    AndroidUtilities.replaceTags(
                        LocaleController.formatString(
                            R.string.CG_CreatedDate, chat.title,
                            LocaleController.formatDateTime(chat.date.toLong(), true)
                        )
                    )
                }
            }
        }

        val regDateFromTelegram = fragment.messagesController?.getPeerSettings(userID)?.registration_month
        return UserHelper.getUserTime(userID, regDateFromTelegram)
    }

    fun addBirthdayToCalendar(parentActivity: Activity, userId: Long) {
        UserHelper.addBirthdayEvent(parentActivity, userId)
    }

    fun getProfileDC(user: TLRPC.User?, chat: TLRPC.Chat?): StringBuilder {
        val sb = StringBuilder()

        val dcId: Int = if (chat?.photo != null && chat.photo.dc_id > 0) {
            chat.photo.dc_id
        } else if (user != null) {
            user.photo?.dc_id?.takeIf { it > 0 }
                ?: run {
                    val userFull = MessagesController.getInstance(UserConfig.selectedAccount).getUserFull(user.id)
                    userFull?.profile_photo?.dc_id?.takeIf { it > 0 }
                        ?: userFull?.personal_photo?.dc_id?.takeIf { it > 0 }
                        ?: userFull?.fallback_photo?.dc_id?.takeIf { it > 0 }
                        ?: 0
                }
        } else {
            0
        }

        if (dcId > 0) {
            sb.append("DC: ")
                .append(dcId)
                .append(", ")
                .append(CGResourcesHelper.getDCName(dcId))
                .append(", ")
                .append(CGResourcesHelper.getDCGeo(dcId))
        } else {
            sb.append("DC: ").append(getString(R.string.NumberUnknown))
        }

        return sb
    }

}