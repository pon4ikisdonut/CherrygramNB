package uz.unnarsx.cherrygram.preferences

import android.app.AlertDialog
import android.content.Context
import android.text.InputType
import android.view.View
import android.widget.EditText
import android.widget.Toast
import org.telegram.messenger.AndroidUtilities
import org.telegram.messenger.LocaleController
import org.telegram.messenger.R
import org.telegram.ui.ActionBar.Theme
import org.telegram.ui.Components.BulletinFactory
import org.telegram.ui.Components.UItem
import org.telegram.ui.Components.UniversalAdapter
import uz.unnarsx.cherrygram.core.configs.CherrygramNBConfig
import uz.unnarsx.cherrygram.helpers.BotTokenAuthDialog
import uz.unnarsx.cherrygram.omnicore.OmniCore
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
    private val ROW_UI_BOTTOM_FOLDERS = 601
    private val ROW_UI_NO_BUBBLES = 602
    private val ROW_UI_MATERIAL_YOU = 603
    private val ROW_UI_CUSTOM_FONT = 604

    private val ROW_HEADER_PRODUCTIVITY = 700
    private val ROW_PROD_CANNED = 701
    private val ROW_PROD_CANNED_DATA = 705
    private val ROW_PROD_AUTORESPONDER = 702
    private val ROW_PROD_AUTORESPONDER_TEXT = 703
    private val ROW_PROD_UNLIMITED_ACCOUNTS = 704

    private val ROW_HEADER_OMNICORE = 800
    private val ROW_OMNICORE_TOGGLE = 801
    private val ROW_OMNICORE_SANDBOX = 802
    private val ROW_OMNICORE_RELOAD = 803

    override fun getTitle(): CharSequence {
        return "Настройки CherrygramNB"
    }

    override fun fillItems(items: ArrayList<UItem>, adapter: UniversalAdapter) {
        // 1. DevTools
        items.add(UItem.asHeader("🛠️ Инструменты разработчика (DevTools)"))
        items.add(
            SettingsHelper.asSwitchCG(
                ROW_DEV_RAW_JSON,
                "Raw JSON в контекстном меню",
                "Кнопка в меню сообщения для просмотра сырого объекта TL/JSON"
            ).setChecked(CherrygramNBConfig.showRawJson)
        )
        items.add(
            SettingsHelper.asSwitchCG(
                ROW_DEV_COPY_ID,
                "Быстрый копипаст ID",
                "Отображать кликабельный ID и DC в профиле пользователей, каналов и чатов"
            ).setChecked(CherrygramNBConfig.quickCopyId)
        )
        items.add(
            UItem.asButton(
                ROW_DEV_BOT_TOKEN_LOGIN,
                "Токен-менеджер: Войти по бот-токену",
                "Авторизация в аккаунт бота через токен для тестирования"
            )
        )
        items.add(UItem.asShadow(null))

        // 2. AntiSpam
        items.add(UItem.asHeader("🛡️ Фильтрация и антиспам"))
        items.add(
            SettingsHelper.asSwitchCG(
                ROW_ANTISPAM_REGEX,
                "Regex-фильтры для ленты",
                "Локально скрывать посты в каналах по регулярным выражениям"
            ).setChecked(CherrygramNBConfig.regexFiltersEnabled)
        )
        if (CherrygramNBConfig.regexFiltersEnabled) {
            items.add(
                UItem.asButton(
                    ROW_ANTISPAM_REGEX_EDIT,
                    "Настроить паттерны Regex",
                    CherrygramNBConfig.regexFilterPatterns
                )
            )
        }
        items.add(
            SettingsHelper.asSwitchCG(
                ROW_ANTISPAM_AUTOMUTE,
                "Авто-мут неизвестных чатов",
                "Автоматически отправлять в архив и глушить сообщения от неизвестных контактов"
            ).setChecked(CherrygramNBConfig.autoMuteUnknown)
        )
        items.add(UItem.asShadow(null))

        // 3. Debloat
        items.add(UItem.asHeader("🧹 Вырезание мусора (Де-блоат)"))
        items.add(
            SettingsHelper.asSwitchCG(
                ROW_DEBLOAT_STORIES,
                "Тотальное отключение Историй",
                "Скрыть верхнюю панель историй и цветные кружки вокруг аватарок"
            ).setChecked(CherrygramNBConfig.disableStoriesTotal)
        )
        items.add(
            SettingsHelper.asSwitchCG(
                ROW_DEBLOAT_SPONSORED,
                "Отключение спонсорских сообщений",
                "Полностью вырезать рекламные сообщения (Sponsored Ads) из каналов"
            ).setChecked(CherrygramNBConfig.hideSponsoredMessages)
        )
        items.add(
            SettingsHelper.asSwitchCG(
                ROW_DEBLOAT_MIN_ATTACH,
                "Минималистичное меню вложений",
                "Убрать лишние элементы из меню прикрепления файлов"
            ).setChecked(CherrygramNBConfig.minimalAttachMenu)
        )
        items.add(UItem.asShadow(null))

        // 4. Content
        items.add(UItem.asHeader("⚡ Работа с контентом и ограничениями"))
        items.add(
            SettingsHelper.asSwitchCG(
                ROW_CONTENT_BYPASS_COPY,
                "Обход запрета на сохранение (Bypass)",
                "Разрешить копирование, пересылку и скриншоты в защищенных чатах и каналах"
            ).setChecked(CherrygramNBConfig.bypassCopyProtection)
        )
        items.add(
            SettingsHelper.asSwitchCG(
                ROW_CONTENT_LOCAL_VOICE,
                "Локальная расшифровка голосовых",
                "Использовать встроенный движок распознавания речи без Telegram Premium"
            ).setChecked(CherrygramNBConfig.localVoiceTranscription)
        )
        items.add(
            SettingsHelper.asSwitchCG(
                ROW_CONTENT_ANTI_DELETE,
                "Сохранение удаленных сообщений",
                "Не удалять сообщения, если их удалил собеседник, а помечать как [Удалено]"
            ).setChecked(CherrygramNBConfig.antiDeleteMessages)
        )
        items.add(UItem.asShadow(null))

        // 5. Privacy
        items.add(UItem.asHeader("🔒 Приватность и Безопасность"))
        items.add(
            SettingsHelper.asSwitchCG(
                ROW_PRIVACY_STREAMER,
                "Режим стримера",
                "Скрывать номер телефона, юзернейм и аватарку при записи экрана"
            ).setChecked(CherrygramNBConfig.streamerMode)
        )
        items.add(
            SettingsHelper.asSwitchCG(
                ROW_PRIVACY_HIDDEN_CHATS,
                "Скрытые чаты под паролем",
                "Скрывать выбранные диалоги из общего списка"
            ).setChecked(CherrygramNBConfig.hiddenChatsEnabled)
        )
        if (CherrygramNBConfig.hiddenChatsEnabled) {
            items.add(
                UItem.asButton(
                    ROW_PRIVACY_HIDDEN_CHATS_PIN,
                    "Изменить PIN для скрытых чатов",
                    if (CherrygramNBConfig.hiddenChatsPin.isEmpty()) "Не задан" else "••••"
                )
            )
        }
        items.add(
            UItem.asButton(
                ROW_PRIVACY_FAKE_UA,
                "Фейковый статус устройства (User-Agent)",
                if (CherrygramNBConfig.fakeDeviceModel.isEmpty()) "По умолчанию" else CherrygramNBConfig.fakeDeviceModel
            )
        )
        items.add(UItem.asShadow(null))

        // 6. Interface
        items.add(UItem.asHeader("🎨 Интерфейс и Кастомизация"))
        items.add(
            SettingsHelper.asSwitchCG(
                ROW_UI_BOTTOM_FOLDERS,
                "Нижняя панель папок (Bottom Bar)",
                "Перенести вкладки папок в нижнюю часть экрана для удобства одной руки"
            ).setChecked(CherrygramNBConfig.foldersAtBottom)
        )
        items.add(
            SettingsHelper.asSwitchCG(
                ROW_UI_NO_BUBBLES,
                "Режим без пузырей сообщений",
                "Отображать сообщения сплошным текстом (IRC / Discord стиль)"
            ).setChecked(CherrygramNBConfig.noMessageBubbles)
        )
        items.add(
            SettingsHelper.asSwitchCG(
                ROW_UI_MATERIAL_YOU,
                "Material You акцент",
                "Динамические акценты в цвета системной темы Android"
            ).setChecked(CherrygramNBConfig.materialYouAccent)
        )
        items.add(
            UItem.asButton(
                ROW_UI_CUSTOM_FONT,
                "Пользовательский шрифт (.ttf / .otf)",
                if (CherrygramNBConfig.customFontPath.isEmpty()) "Системный" else CherrygramNBConfig.customFontPath
            )
        )
        items.add(UItem.asShadow(null))

        // 7. Productivity
        items.add(UItem.asHeader("🚀 Продуктивность и Автоматизация"))
        items.add(
            SettingsHelper.asSwitchCG(
                ROW_PROD_CANNED,
                "Шаблоны быстрых ответов",
                "Быстрый доступ к заготовленным фразам при вводе текста"
            ).setChecked(CherrygramNBConfig.cannedResponsesEnabled)
        )
        if (CherrygramNBConfig.cannedResponsesEnabled) {
            items.add(
                UItem.asButton(
                    ROW_PROD_CANNED_DATA,
                    "Настроить шаблоны ответов",
                    CherrygramNBConfig.cannedResponsesData
                )
            )
        }
        items.add(
            SettingsHelper.asSwitchCG(
                ROW_PROD_AUTORESPONDER,
                "Локальный автоответчик",
                "Автоматически отвечать на входящие сообщения заготовленным текстом"
            ).setChecked(CherrygramNBConfig.autoResponderEnabled)
        )
        if (CherrygramNBConfig.autoResponderEnabled) {
            items.add(
                UItem.asButton(
                    ROW_PROD_AUTORESPONDER_TEXT,
                    "Текст автоответчика",
                    CherrygramNBConfig.autoResponderText
                )
            )
        }
        items.add(
            SettingsHelper.asSwitchCG(
                ROW_PROD_UNLIMITED_ACCOUNTS,
                "Мультиаккаунт без лимитов",
                "Поддержка до 20 аккаунтов одновременно"
            ).setChecked(CherrygramNBConfig.unlimitedAccounts)
        )
        items.add(UItem.asShadow(null))

        // 8. OmniCore Plugins
        items.add(UItem.asHeader("🔌 Система плагинов OmniCore"))
        items.add(
            SettingsHelper.asSwitchCG(
                ROW_OMNICORE_TOGGLE,
                "Включить движок OmniCore",
                "Загрузка и выполнение внешних плагинов (.dex, .rule, .lua)"
            ).setChecked(CherrygramNBConfig.omniCoreEnabled)
        )
        items.add(
            SettingsHelper.asSwitchCG(
                ROW_OMNICORE_SANDBOX,
                "Строгая изоляция (Sandbox)",
                "Ограничение времени выполнения и изоляция крашей"
            ).setChecked(CherrygramNBConfig.omniCoreSandboxStrict)
        )
        items.add(
            UItem.asButton(
                ROW_OMNICORE_RELOAD,
                "Перезагрузить плагины",
                "Папка: " + OmniCore.getPluginsDirectory().path
            )
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
                showTextInputDialog("Паттерны Regex (разделитель |)", CherrygramNBConfig.regexFilterPatterns) { newText ->
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
                    showTextInputDialog("Задайте PIN для скрытых чатов", "") { pin ->
                        if (pin.isNotEmpty()) {
                            CherrygramNBConfig.hiddenChatsPin = pin
                            CherrygramNBConfig.hiddenChatsEnabled = true
                            SettingsHelper.updateCheckState(view, true)
                            updateRows(true)
                            BulletinFactory.of(this).createSimpleBulletin(R.raw.done, "PIN установлен: $pin").show()
                        }
                    }
                } else {
                    CherrygramNBConfig.hiddenChatsEnabled = !CherrygramNBConfig.hiddenChatsEnabled
                    SettingsHelper.updateCheckState(view, CherrygramNBConfig.hiddenChatsEnabled)
                    updateRows(true)
                }
            }
            ROW_PRIVACY_HIDDEN_CHATS_PIN -> {
                showTextInputDialog("PIN для скрытых чатов", CherrygramNBConfig.hiddenChatsPin) { newPin ->
                    CherrygramNBConfig.hiddenChatsPin = newPin
                    updateRows(true)
                }
            }
            ROW_PRIVACY_FAKE_UA -> {
                showTextInputDialog("Фейковая модель устройства", CherrygramNBConfig.fakeDeviceModel) { newModel ->
                    CherrygramNBConfig.fakeDeviceModel = newModel
                    updateRows(true)
                }
            }
            ROW_UI_BOTTOM_FOLDERS -> {
                CherrygramNBConfig.foldersAtBottom = !CherrygramNBConfig.foldersAtBottom
                SettingsHelper.updateCheckState(view, CherrygramNBConfig.foldersAtBottom)
            }
            ROW_UI_NO_BUBBLES -> {
                CherrygramNBConfig.noMessageBubbles = !CherrygramNBConfig.noMessageBubbles
                SettingsHelper.updateCheckState(view, CherrygramNBConfig.noMessageBubbles)
            }
            ROW_UI_MATERIAL_YOU -> {
                CherrygramNBConfig.materialYouAccent = !CherrygramNBConfig.materialYouAccent
                SettingsHelper.updateCheckState(view, CherrygramNBConfig.materialYouAccent)
            }
            ROW_UI_CUSTOM_FONT -> {
                showTextInputDialog("Абсолютный путь к файлу шрифта (.ttf / .otf)", CherrygramNBConfig.customFontPath) { path ->
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
                showTextInputDialog("Шаблоны ответов (разделитель ;)", CherrygramNBConfig.cannedResponsesData) { data ->
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
                showTextInputDialog("Текст автоответчика", CherrygramNBConfig.autoResponderText) { newText ->
                    CherrygramNBConfig.autoResponderText = newText
                    updateRows(true)
                }
            }
            ROW_PROD_UNLIMITED_ACCOUNTS -> {
                CherrygramNBConfig.unlimitedAccounts = !CherrygramNBConfig.unlimitedAccounts
                SettingsHelper.updateCheckState(view, CherrygramNBConfig.unlimitedAccounts)
            }
            ROW_OMNICORE_TOGGLE -> {
                CherrygramNBConfig.omniCoreEnabled = !CherrygramNBConfig.omniCoreEnabled
                SettingsHelper.updateCheckState(view, CherrygramNBConfig.omniCoreEnabled)
            }
            ROW_OMNICORE_SANDBOX -> {
                CherrygramNBConfig.omniCoreSandboxStrict = !CherrygramNBConfig.omniCoreSandboxStrict
                SettingsHelper.updateCheckState(view, CherrygramNBConfig.omniCoreSandboxStrict)
            }
            ROW_OMNICORE_RELOAD -> {
                context?.let { OmniCore.loadAllPlugins(it) }
                val count = OmniCore.getLoadedPlugins().size
                BulletinFactory.of(this).createSimpleBulletin(R.raw.done, "Загружено плагинов: $count").show()
            }
        }
    }

    private fun showTextInputDialog(title: String, initialValue: String, onConfirm: (String) -> Unit) {
        val ctx = context ?: return
        val builder = AlertDialog.Builder(ctx)
        builder.setTitle(title)

        val input = EditText(ctx).apply {
            setText(initialValue)
            inputType = InputType.TYPE_CLASS_TEXT
        }
        builder.setView(input)

        builder.setPositiveButton("Сохранить") { _, _ ->
            onConfirm(input.text.toString().trim())
        }
        builder.setNegativeButton(LocaleController.getString("Cancel", R.string.Cancel), null)
        builder.show()
    }
}
