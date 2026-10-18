package uz.unnarsx.cherrygram.core.configs

import android.app.Activity
import android.content.SharedPreferences
import org.telegram.messenger.ApplicationLoader
import uz.unnarsx.cherrygram.preferences.boolean
import uz.unnarsx.cherrygram.preferences.int
import uz.unnarsx.cherrygram.preferences.string

object CherrygramNBConfig {

    private val sharedPreferences: SharedPreferences = ApplicationLoader.applicationContext.getSharedPreferences("cherrygramnb_config", Activity.MODE_PRIVATE)

    // 1. DevTools
    var showRawJson by sharedPreferences.boolean("nb_show_raw_json", true)
    var quickCopyId by sharedPreferences.boolean("nb_quick_copy_id", true)
    var botTokenAuthEnabled by sharedPreferences.boolean("nb_bot_token_auth", true)

    // 2. AntiSpam & Filters
    var regexFiltersEnabled by sharedPreferences.boolean("nb_regex_filters_enabled", false)
    var regexFilterPatterns by sharedPreferences.string("nb_regex_filter_patterns", "(?i)(крипт|сигнал|инвест|casino|казино|1xbet|ставк|giveaway|airdrop)")
    var autoMuteUnknown by sharedPreferences.boolean("nb_auto_mute_unknown", false)

    // 3. Debloat
    var disableStoriesTotal by sharedPreferences.boolean("nb_disable_stories_total", false)
    var hideSponsoredMessages by sharedPreferences.boolean("nb_hide_sponsored_messages", true)
    var minimalAttachMenu by sharedPreferences.boolean("nb_minimal_attach_menu", false)

    // 4. Content & Restrictions
    var bypassCopyProtection by sharedPreferences.boolean("nb_bypass_copy_protection", true)
    var localVoiceTranscription by sharedPreferences.boolean("nb_local_voice_transcription", true)
    var antiDeleteMessages by sharedPreferences.boolean("nb_anti_delete_messages", false)

    // 5. Privacy & Security
    var streamerMode by sharedPreferences.boolean("nb_streamer_mode", false)
    var hiddenChatsEnabled by sharedPreferences.boolean("nb_hidden_chats_enabled", false)
    var hiddenChatsPin by sharedPreferences.string("nb_hidden_chats_pin", "")
    var fakeDeviceModel by sharedPreferences.string("nb_fake_device_model", "")
    var fakeSessionLocationEnabled by sharedPreferences.boolean("nb_fake_session_location_enabled", false)
    var fakeSessionLocation by sharedPreferences.string("nb_fake_session_location", "Германия, Франкфурт")
    var fakeSessionIp by sharedPreferences.string("nb_fake_session_ip", "185.220.101.5")

    @JvmField
    var isHiddenChatsUnlocked = false

    private val hiddenDialogsSet: MutableSet<Long> by lazy {
        val set = mutableSetOf<Long>()
        val saved = sharedPreferences.getString("nb_hidden_dialogs_list", "") ?: ""
        if (saved.isNotEmpty()) {
            saved.split(",").mapNotNull { it.trim().toLongOrNull() }.forEach { set.add(it) }
        }
        set
    }

    fun isDialogHidden(dialogId: Long): Boolean {
        return hiddenChatsEnabled && hiddenDialogsSet.contains(dialogId)
    }

    fun toggleDialogHidden(dialogId: Long): Boolean {
        val willHide = !hiddenDialogsSet.contains(dialogId)
        if (willHide) {
            hiddenDialogsSet.add(dialogId)
        } else {
            hiddenDialogsSet.remove(dialogId)
        }
        sharedPreferences.edit().putString("nb_hidden_dialogs_list", hiddenDialogsSet.joinToString(",")).apply()
        return willHide
    }

    init {
        if (sharedPreferences.contains("nb_no_message_bubbles")) {
            sharedPreferences.edit().remove("nb_no_message_bubbles").apply()
        }
    }

    // 6. Interface & Customization
    var foldersAtBottom by sharedPreferences.boolean("nb_folders_at_bottom", false)
    var materialYouAccent by sharedPreferences.boolean("nb_material_you_accent", true)
    var customFontPath by sharedPreferences.string("nb_custom_font_path", "")

    // 7. Productivity & Automation
    var cannedResponsesEnabled by sharedPreferences.boolean("nb_canned_responses", false)
    var cannedResponsesData by sharedPreferences.string("nb_canned_responses_data", "Привет!;Буду позже;Договорились!;Супер! 👍")
    var autoResponderEnabled by sharedPreferences.boolean("nb_auto_responder_enabled", false)
    var autoResponderText by sharedPreferences.string("nb_auto_responder_text", "Здравствуйте! Сейчас не могу ответить, напишу позже.")
    var unlimitedAccounts by sharedPreferences.boolean("nb_unlimited_accounts", true)
}
