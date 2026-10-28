package uz.unnarsx.cherrygram.preferences

import android.content.Context
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.FrameLayout
import org.telegram.messenger.AndroidUtilities
import org.telegram.messenger.LocaleController
import org.telegram.messenger.LocaleController.getString
import org.telegram.messenger.NotificationCenter
import org.telegram.messenger.R
import org.telegram.ui.ActionBar.AlertDialog
import org.telegram.ui.ActionBar.Theme
import org.telegram.ui.Components.BulletinFactory
import org.telegram.ui.Components.EditTextBoldCursor
import org.telegram.ui.Components.LayoutHelper
import org.telegram.ui.Components.UItem
import org.telegram.ui.Components.UniversalAdapter
import uz.unnarsx.cherrygram.core.configs.CherrygramAppearanceConfig
import uz.unnarsx.cherrygram.core.configs.CherrygramNBConfig
import uz.unnarsx.cherrygram.helpers.BotTokenAuthDialog
import uz.unnarsx.cherrygram.preferences.helpers.SettingsHelper

class CherrygramNBSettingsActivity : BaseCGPreferencesEntry() {

    private val ROW_HEADER_DEVTOOLS = 100
    private val ROW_DEV_RAW_JSON = 101
    private val ROW_DEV_COPY_ID = 102
    private val ROW_DEV_BOT_TOKEN_LOGIN = 103

    private val ROW_HEADER_ANTISPAM = 200
    private val ROW_ANTISPAM_REGEX = 201
    private val ROW_ANTISPAM_REGEX_EDIT = 202
    private val ROW_ANTISPAM_AUTOMUTE = 203

    private val ROW_HEADER_DEBLOAT = 300
    private val ROW_DEBLOAT_STORIES = 301
    private val ROW_DEBLOAT_SPONSORED = 302
    private val ROW_DEBLOAT_MIN_ATTACH = 303

    private val ROW_HEADER_CONTENT = 400
    private val ROW_CONTENT_BYPASS_COPY = 401
    private val ROW_CONTENT_LOCAL_VOICE = 402
    private val ROW_CONTENT_ANTI_DELETE = 403

    private val ROW_HEADER_PRIVACY = 500
    private val ROW_PRIVACY_STREAMER = 501
    private val ROW_PRIVACY_HIDDEN_CHATS = 502
    private val ROW_PRIVACY_FAKE_UA = 503
    private val ROW_PRIVACY_HIDDEN_CHATS_PIN = 504

    private val ROW_HEADER_INTERFACE = 600
    private val ROW_UI_ALWAYS_SHOW_FOLDERS = 601
    private val ROW_UI_BOTTOM_FOLDERS = 602
    private val ROW_UI_SHOW_MAIN_TABS = 603
    private val ROW_UI_MATERIAL_YOU = 604
    private val ROW_UI_CUSTOM_FONT = 605

    private val ROW_HEADER_PRODUCTIVITY = 700
    private val ROW_PROD_CANNED = 701
    private val ROW_PROD_CANNED_DATA = 705
    private val ROW_PROD_AUTORESPONDER = 702
    private val ROW_PROD_AUTORESPONDER_TEXT = 703
    private val ROW_PROD_UNLIMITED_ACCOUNTS = 704

    override fun getTitle(): CharSequence {
        return getString(R.string.CGNB_SettingsTitle)
    }

    override fun fillItems(items: ArrayList<UItem>, adapter: UniversalAdapter) {
        // 1. DevTools
        items.add(UItem.asHeader(getString(R.string.CGNB_HeaderDevTools)))
        items.add(
            SettingsHelper.asSwitchCG(
                ROW_DEV_RAW_JSON,
                getString(R.string.CGNB_DevRawJson),
                getString(R.string.CGNB_DevRawJsonDesc)
            ).setChecked(CherrygramNBConfig.showRawJson)
        )
        items.add(
            SettingsHelper.asSwitchCG(
                ROW_DEV_COPY_ID,
                getString(R.string.CGNB_DevCopyId),
                getString(R.string.CGNB_DevCopyIdDesc)
            ).setChecked(CherrygramNBConfig.quickCopyId)
        )
        items.add(
            UItem.asButton(
                ROW_DEV_BOT_TOKEN_LOGIN,
                getString(R.string.CGNB_DevBotTokenLogin),
                getString(R.string.CGNB_DevBotTokenLoginDesc)
            )
        )
        items.add(UItem.asShadow(null))

        // 2. AntiSpam
        items.add(UItem.asHeader(getString(R.string.CGNB_HeaderAntispam)))
        items.add(
            SettingsHelper.asSwitchCG(
                ROW_ANTISPAM_REGEX,
                getString(R.string.CGNB_AntispamRegex),
                getString(R.string.CGNB_AntispamRegexDesc)
            ).setChecked(CherrygramNBConfig.regexFiltersEnabled)
        )
        if (CherrygramNBConfig.regexFiltersEnabled) {
            items.add(
                UItem.asButton(
                    ROW_ANTISPAM_REGEX_EDIT,
                    getString(R.string.CGNB_AntispamRegexEdit),
                    CherrygramNBConfig.regexFilterPatterns
                )
            )
        }
        items.add(
            SettingsHelper.asSwitchCG(
                ROW_ANTISPAM_AUTOMUTE,
                getString(R.string.CGNB_AntispamAutomute),
                getString(R.string.CGNB_AntispamAutomuteDesc)
            ).setChecked(CherrygramNBConfig.autoMuteUnknown)
        )
        items.add(UItem.asShadow(null))

        // 3. Debloat
        items.add(UItem.asHeader(getString(R.string.CGNB_HeaderDebloat)))
        items.add(
            SettingsHelper.asSwitchCG(
                ROW_DEBLOAT_STORIES,
                getString(R.string.CGNB_DebloatStories),
                getString(R.string.CGNB_DebloatStoriesDesc)
            ).setChecked(CherrygramNBConfig.disableStoriesTotal)
        )
        items.add(
            SettingsHelper.asSwitchCG(
                ROW_DEBLOAT_SPONSORED,
                getString(R.string.CGNB_DebloatSponsored),
                getString(R.string.CGNB_DebloatSponsoredDesc)
            ).setChecked(CherrygramNBConfig.hideSponsoredMessages)
        )
        items.add(
            SettingsHelper.asSwitchCG(
                ROW_DEBLOAT_MIN_ATTACH,
                getString(R.string.CGNB_DebloatMinAttach),
                getString(R.string.CGNB_DebloatMinAttachDesc)
            ).setChecked(CherrygramNBConfig.minimalAttachMenu)
        )
        items.add(UItem.asShadow(null))

        // 4. Content
        items.add(UItem.asHeader(getString(R.string.CGNB_HeaderContent)))
        items.add(
            SettingsHelper.asSwitchCG(
                ROW_CONTENT_BYPASS_COPY,
                getString(R.string.CGNB_ContentBypassCopy),
                getString(R.string.CGNB_ContentBypassCopyDesc)
            ).setChecked(CherrygramNBConfig.bypassCopyProtection)
        )
        items.add(
            SettingsHelper.asSwitchCG(
                ROW_CONTENT_LOCAL_VOICE,
                getString(R.string.CGNB_ContentLocalVoice),
                getString(R.string.CGNB_ContentLocalVoiceDesc)
            ).setChecked(CherrygramNBConfig.localVoiceTranscription)
        )
        items.add(
            SettingsHelper.asSwitchCG(
                ROW_CONTENT_ANTI_DELETE,
                getString(R.string.CGNB_ContentAntiDelete),
                getString(R.string.CGNB_ContentAntiDeleteDesc)
            ).setChecked(CherrygramNBConfig.antiDeleteMessages)
        )
        items.add(UItem.asShadow(null))

        // 5. Privacy
        items.add(UItem.asHeader(getString(R.string.CGNB_HeaderPrivacy)))
        items.add(
            SettingsHelper.asSwitchCG(
                ROW_PRIVACY_STREAMER,
                getString(R.string.CGNB_PrivacyStreamer),
                getString(R.string.CGNB_PrivacyStreamerDesc)
            ).setChecked(CherrygramNBConfig.streamerMode)
        )
        items.add(
            SettingsHelper.asSwitchCG(
                ROW_PRIVACY_HIDDEN_CHATS,
                getString(R.string.CGNB_PrivacyHiddenChats),
                getString(R.string.CGNB_PrivacyHiddenChatsDesc)
            ).setChecked(CherrygramNBConfig.hiddenChatsEnabled)
        )
        if (CherrygramNBConfig.hiddenChatsEnabled) {
            items.add(
                UItem.asButton(
                    ROW_PRIVACY_HIDDEN_CHATS_PIN,
                    getString(R.string.CGNB_PrivacyHiddenChatsPin),
                    if (CherrygramNBConfig.hiddenChatsPin.isEmpty()) getString(R.string.CGNB_NotSet) else "••••"
                )
            )
        }
        items.add(
            UItem.asButton(
                ROW_PRIVACY_FAKE_UA,
                getString(R.string.CGNB_PrivacyFakeUa),
                if (CherrygramNBConfig.fakeDeviceModel.isEmpty()) getString(R.string.CGNB_Default) else CherrygramNBConfig.fakeDeviceModel
            )
        )
        items.add(UItem.asShadow(null))

        // 6. Interface
        items.add(UItem.asHeader(getString(R.string.CGNB_HeaderInterface)))
        items.add(
            SettingsHelper.asSwitchCG(
                ROW_UI_ALWAYS_SHOW_FOLDERS,
                getString(R.string.CGNB_UiAlwaysShowFolders),
                getString(R.string.CGNB_UiAlwaysShowFoldersDesc)
            ).setChecked(CherrygramNBConfig.alwaysShowFolderTabs)
        )
        items.add(
            SettingsHelper.asSwitchCG(
                ROW_UI_BOTTOM_FOLDERS,
                getString(R.string.CGNB_UiBottomFolders),
                getString(R.string.CGNB_UiBottomFoldersDesc)
            ).setChecked(CherrygramNBConfig.foldersAtBottom && !CherrygramAppearanceConfig.showMainTabs)
        )
        items.add(
            SettingsHelper.asSwitchCG(
                ROW_UI_SHOW_MAIN_TABS,
                getString(R.string.CGNB_UiShowMainTabs),
                getString(R.string.CGNB_UiShowMainTabsDesc)
            ).setChecked(CherrygramAppearanceConfig.showMainTabs)
        )
        items.add(
            SettingsHelper.asSwitchCG(
                ROW_UI_MATERIAL_YOU,
                getString(R.string.CGNB_UiMaterialYou),
                getString(R.string.CGNB_UiMaterialYouDesc)
            ).setChecked(CherrygramNBConfig.materialYouAccent)
        )
        items.add(
            UItem.asButton(
                ROW_UI_CUSTOM_FONT,
                getString(R.string.CGNB_UiCustomFont),
                if (CherrygramNBConfig.customFontPath.isEmpty()) getString(R.string.CGNB_System) else CherrygramNBConfig.customFontPath
            )
        )
        items.add(UItem.asShadow(null))

        // 7. Productivity
        items.add(UItem.asHeader(getString(R.string.CGNB_HeaderProductivity)))
        items.add(
            SettingsHelper.asSwitchCG(
                ROW_PROD_CANNED,
                getString(R.string.CGNB_ProdCanned),
                getString(R.string.CGNB_ProdCannedDesc)
            ).setChecked(CherrygramNBConfig.cannedResponsesEnabled)
        )
        if (CherrygramNBConfig.cannedResponsesEnabled) {
            items.add(
                UItem.asButton(
                    ROW_PROD_CANNED_DATA,
                    getString(R.string.CGNB_ProdCannedData),
                    CherrygramNBConfig.cannedResponsesData
                )
            )
        }
        items.add(
            SettingsHelper.asSwitchCG(
                ROW_PROD_AUTORESPONDER,
                getString(R.string.CGNB_ProdAutoresponder),
                getString(R.string.CGNB_ProdAutoresponderDesc)
            ).setChecked(CherrygramNBConfig.autoResponderEnabled)
        )
        if (CherrygramNBConfig.autoResponderEnabled) {
            items.add(
                UItem.asButton(
                    ROW_PROD_AUTORESPONDER_TEXT,
                    getString(R.string.CGNB_ProdAutoresponderText),
                    CherrygramNBConfig.autoResponderText
                )
            )
        }
        items.add(
            SettingsHelper.asSwitchCG(
                ROW_PROD_UNLIMITED_ACCOUNTS,
                getString(R.string.CGNB_ProdUnlimitedAccounts),
                getString(R.string.CGNB_ProdUnlimitedAccountsDesc)
            ).setChecked(CherrygramNBConfig.unlimitedAccounts)
        )
        items.add(UItem.asShadow(null))
    }

    override fun onClick(item: UItem, view: View, position: Int, x: Float, y: Float) {
        when (item.id) {
            ROW_DEV_RAW_JSON -> {
                CherrygramNBConfig.showRawJson = !CherrygramNBConfig.showRawJson
                SettingsHelper.updateCheckState(view, CherrygramNBConfig.showRawJson)
            }
            ROW_DEV_COPY_ID -> {
                CherrygramNBConfig.quickCopyId = !CherrygramNBConfig.quickCopyId
                SettingsHelper.updateCheckState(view, CherrygramNBConfig.quickCopyId)
            }
            ROW_DEV_BOT_TOKEN_LOGIN -> {
                BotTokenAuthDialog.show(this)
            }
            ROW_ANTISPAM_REGEX -> {
                CherrygramNBConfig.regexFiltersEnabled = !CherrygramNBConfig.regexFiltersEnabled
                SettingsHelper.updateCheckState(view, CherrygramNBConfig.regexFiltersEnabled)
                updateRows(true)
            }
            ROW_ANTISPAM_REGEX_EDIT -> {
                showTextInputDialog(getString(R.string.CGNB_DialogRegexPatterns), CherrygramNBConfig.regexFilterPatterns) { newText ->
                    CherrygramNBConfig.regexFilterPatterns = newText
                    updateRows(true)
                }
            }
            ROW_ANTISPAM_AUTOMUTE -> {
                CherrygramNBConfig.autoMuteUnknown = !CherrygramNBConfig.autoMuteUnknown
                SettingsHelper.updateCheckState(view, CherrygramNBConfig.autoMuteUnknown)
            }
            ROW_DEBLOAT_STORIES -> {
                CherrygramNBConfig.disableStoriesTotal = !CherrygramNBConfig.disableStoriesTotal
                SettingsHelper.updateCheckState(view, CherrygramNBConfig.disableStoriesTotal)
            }
            ROW_DEBLOAT_SPONSORED -> {
                CherrygramNBConfig.hideSponsoredMessages = !CherrygramNBConfig.hideSponsoredMessages
                SettingsHelper.updateCheckState(view, CherrygramNBConfig.hideSponsoredMessages)
            }
            ROW_DEBLOAT_MIN_ATTACH -> {
                CherrygramNBConfig.minimalAttachMenu = !CherrygramNBConfig.minimalAttachMenu
                SettingsHelper.updateCheckState(view, CherrygramNBConfig.minimalAttachMenu)
            }
            ROW_CONTENT_BYPASS_COPY -> {
                CherrygramNBConfig.bypassCopyProtection = !CherrygramNBConfig.bypassCopyProtection
                SettingsHelper.updateCheckState(view, CherrygramNBConfig.bypassCopyProtection)
            }
            ROW_CONTENT_LOCAL_VOICE -> {
                CherrygramNBConfig.localVoiceTranscription = !CherrygramNBConfig.localVoiceTranscription
                SettingsHelper.updateCheckState(view, CherrygramNBConfig.localVoiceTranscription)
            }
            ROW_CONTENT_ANTI_DELETE -> {
                CherrygramNBConfig.antiDeleteMessages = !CherrygramNBConfig.antiDeleteMessages
                SettingsHelper.updateCheckState(view, CherrygramNBConfig.antiDeleteMessages)
            }
            ROW_PRIVACY_STREAMER -> {
                CherrygramNBConfig.streamerMode = !CherrygramNBConfig.streamerMode
                SettingsHelper.updateCheckState(view, CherrygramNBConfig.streamerMode)
            }
            ROW_PRIVACY_HIDDEN_CHATS -> {
                if (!CherrygramNBConfig.hiddenChatsEnabled && CherrygramNBConfig.hiddenChatsPin.isEmpty()) {
                    showTextInputDialog(getString(R.string.CGNB_DialogSetPin), "") { pin ->
                        if (pin.isNotEmpty()) {
                            CherrygramNBConfig.hiddenChatsPin = pin
                            CherrygramNBConfig.hiddenChatsEnabled = true
                            SettingsHelper.updateCheckState(view, true)
                            updateRows(true)
                            BulletinFactory.of(this).createSimpleBulletin(
                                R.raw.done,
                                LocaleController.formatString(R.string.CGNB_PinSet, pin)
                            ).show()
                        }
                    }
                } else {
                    CherrygramNBConfig.hiddenChatsEnabled = !CherrygramNBConfig.hiddenChatsEnabled
                    SettingsHelper.updateCheckState(view, CherrygramNBConfig.hiddenChatsEnabled)
                    updateRows(true)
                }
            }
            ROW_PRIVACY_HIDDEN_CHATS_PIN -> {
                showTextInputDialog(getString(R.string.CGNB_DialogPin), CherrygramNBConfig.hiddenChatsPin) { newPin ->
                    CherrygramNBConfig.hiddenChatsPin = newPin
                    updateRows(true)
                }
            }
            ROW_PRIVACY_FAKE_UA -> {
                showTextInputDialog(getString(R.string.CGNB_DialogFakeDeviceModel), CherrygramNBConfig.fakeDeviceModel) { newModel ->
                    CherrygramNBConfig.fakeDeviceModel = newModel
                    updateRows(true)
                }
            }
            ROW_UI_ALWAYS_SHOW_FOLDERS -> {
                CherrygramNBConfig.alwaysShowFolderTabs = !CherrygramNBConfig.alwaysShowFolderTabs
                SettingsHelper.updateCheckState(view, CherrygramNBConfig.alwaysShowFolderTabs)
                NotificationCenter.getGlobalInstance().postNotificationName(NotificationCenter.dialogFiltersUpdated)
                parentLayout?.rebuildAllFragmentViews(false, false)
            }
            ROW_UI_BOTTOM_FOLDERS -> {
                val newState = !(CherrygramNBConfig.foldersAtBottom && !CherrygramAppearanceConfig.showMainTabs)
                CherrygramNBConfig.foldersAtBottom = newState
                CherrygramAppearanceConfig.foldersAtBottom = newState
                if (newState) {
                    CherrygramAppearanceConfig.showMainTabs = false
                }
                updateRows(true)
                parentLayout?.rebuildAllFragmentViews(true, true)
                showRestartBulletin()
            }
            ROW_UI_SHOW_MAIN_TABS -> {
                val newState = !CherrygramAppearanceConfig.showMainTabs
                CherrygramAppearanceConfig.showMainTabs = newState
                if (newState) {
                    CherrygramNBConfig.foldersAtBottom = false
                    CherrygramAppearanceConfig.foldersAtBottom = false
                }
                updateRows(true)
                parentLayout?.rebuildAllFragmentViews(true, true)
                showRestartBulletin()
            }
            ROW_UI_MATERIAL_YOU -> {
                CherrygramNBConfig.materialYouAccent = !CherrygramNBConfig.materialYouAccent
                SettingsHelper.updateCheckState(view, CherrygramNBConfig.materialYouAccent)
            }
            ROW_UI_CUSTOM_FONT -> {
                showTextInputDialog(getString(R.string.CGNB_DialogCustomFont), CherrygramNBConfig.customFontPath) { path ->
                    CherrygramNBConfig.customFontPath = path
                    updateRows(true)
                }
            }
            ROW_PROD_CANNED -> {
                CherrygramNBConfig.cannedResponsesEnabled = !CherrygramNBConfig.cannedResponsesEnabled
                SettingsHelper.updateCheckState(view, CherrygramNBConfig.cannedResponsesEnabled)
                updateRows(true)
            }
            ROW_PROD_CANNED_DATA -> {
                showTextInputDialog(getString(R.string.CGNB_DialogCannedResponses), CherrygramNBConfig.cannedResponsesData) { data ->
                    CherrygramNBConfig.cannedResponsesData = data
                    updateRows(true)
                }
            }
            ROW_PROD_AUTORESPONDER -> {
                CherrygramNBConfig.autoResponderEnabled = !CherrygramNBConfig.autoResponderEnabled
                SettingsHelper.updateCheckState(view, CherrygramNBConfig.autoResponderEnabled)
                updateRows(true)
            }
            ROW_PROD_AUTORESPONDER_TEXT -> {
                showTextInputDialog(getString(R.string.CGNB_DialogAutoresponderText), CherrygramNBConfig.autoResponderText) { newText ->
                    CherrygramNBConfig.autoResponderText = newText
                    updateRows(true)
                }
            }
            ROW_PROD_UNLIMITED_ACCOUNTS -> {
                CherrygramNBConfig.unlimitedAccounts = !CherrygramNBConfig.unlimitedAccounts
                SettingsHelper.updateCheckState(view, CherrygramNBConfig.unlimitedAccounts)
            }
        }
    }

    private fun showTextInputDialog(title: String, initialValue: String, onConfirm: (String) -> Unit) {
        val ctx = context ?: return
        val builder = AlertDialog.Builder(ctx, resourceProvider)
        builder.setTitle(title)

        val container = FrameLayout(ctx)
        container.setPadding(AndroidUtilities.dp(24f), 0, AndroidUtilities.dp(24f), 0)

        val field = EditTextBoldCursor(ctx).apply {
            setTextSize(TypedValue.COMPLEX_UNIT_DIP, 18f)
            setTextColor(Theme.getColor(Theme.key_dialogTextBlack, resourceProvider))
            setHintColor(Theme.getColor(Theme.key_windowBackgroundWhiteHintText, resourceProvider))
            setHeaderHintColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlueHeader, resourceProvider))
            setCursorColor(Theme.getColor(Theme.key_windowBackgroundWhiteInputFieldActivated, resourceProvider))
            setLineColors(
                Theme.getColor(Theme.key_windowBackgroundWhiteInputField, resourceProvider),
                Theme.getColor(Theme.key_windowBackgroundWhiteInputFieldActivated, resourceProvider),
                Theme.getColor(Theme.key_text_RedRegular, resourceProvider)
            )
            setText(initialValue)
            setSingleLine(true)
            isFocusable = true
            imeOptions = EditorInfo.IME_ACTION_DONE
            background = null
            setPadding(0, 0, 0, 0)
        }
        val params = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT)
        params.gravity = Gravity.TOP
        params.topMargin = AndroidUtilities.dp(10f)
        params.bottomMargin = AndroidUtilities.dp(10f)
        container.addView(field, params)
        builder.setView(container)

        builder.setPositiveButton(LocaleController.getString("Save", R.string.Save)) { _, _ ->
            onConfirm(field.text.toString().trim())
        }
        builder.setNegativeButton(LocaleController.getString("Cancel", R.string.Cancel), null)

        val dialog = builder.create()
        dialog.setOnShowListener {
            field.requestFocus()
            AndroidUtilities.showKeyboard(field)
            field.setSelection(0, field.length())
        }
        dialog.showDelayed(250)
    }
}
