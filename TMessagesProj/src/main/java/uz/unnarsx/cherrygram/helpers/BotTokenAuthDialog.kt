package uz.unnarsx.cherrygram.helpers

import android.content.Context
import android.text.InputType
import android.widget.EditText
import android.widget.LinearLayout
import org.telegram.messenger.AndroidUtilities
import org.telegram.messenger.ApplicationLoader
import org.telegram.messenger.LocaleController
import org.telegram.messenger.NotificationCenter
import org.telegram.messenger.R
import org.telegram.messenger.UserConfig
import org.telegram.tgnet.ConnectionsManager
import org.telegram.tgnet.TLObject
import org.telegram.tgnet.TLRPC
import org.telegram.ui.ActionBar.AlertDialog
import org.telegram.ui.ActionBar.BaseFragment
import org.telegram.ui.ActionBar.Theme
import org.telegram.ui.Components.BulletinFactory
import org.telegram.ui.LaunchActivity
import uz.unnarsx.cherrygram.Extra

object BotTokenAuthDialog {

    fun show(fragment: BaseFragment) {
        val context = fragment.context ?: return
        val builder = AlertDialog.Builder(context, fragment.resourceProvider)
        builder.setTitle("Вход по токену бота")

        val container = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(AndroidUtilities.dp(24f), AndroidUtilities.dp(12f), AndroidUtilities.dp(24f), AndroidUtilities.dp(8f))
        }

        val input = EditText(context).apply {
            hint = "123456:ABC-DEF1234ghIkl-zyx57W2v1u123ew11"
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            textSize = 16f
            setTextColor(Theme.getColor(Theme.key_dialogTextBlack, fragment.resourceProvider))
            setHintTextColor(Theme.getColor(Theme.key_dialogTextHint, fragment.resourceProvider))
        }
        container.addView(input)
        builder.setView(container)

        builder.setPositiveButton("Войти") { _, _ ->
            val token = input.text.toString().trim()
            if (token.isEmpty()) {
                BulletinFactory.of(fragment).createErrorBulletin("Токен не может быть пустым").show()
                return@setPositiveButton
            }

            performBotLogin(fragment, token)
        }

        builder.setNegativeButton(LocaleController.getString("Cancel", R.string.Cancel), null)
        fragment.showDialog(builder.create())
    }

    private fun performBotLogin(fragment: BaseFragment, botToken: String) {
        val progressDialog = AlertDialog(fragment.context, 3)
        progressDialog.setCanCancel(false)
        progressDialog.show()

        val req = TLRPC.TL_auth_importBotAuthorization().apply {
            flags = 0
            api_id = Extra.getAppID()
            api_hash = Extra.getAppHash()
            bot_auth_token = botToken
        }

        val account = fragment.currentAccount
        ConnectionsManager.getInstance(account).sendRequest(req) { response: TLObject?, error: TLRPC.TL_error? ->
            AndroidUtilities.runOnUIThread {
                try {
                    progressDialog.dismiss()
                } catch (ignored: Exception) {}

                if (error != null) {
                    BulletinFactory.of(fragment).createErrorBulletin("Ошибка: " + error.text).show()
                } else if (response is TLRPC.TL_auth_authorization) {
                    val userConfig = UserConfig.getInstance(account)
                    userConfig.currentUser = response.user
                    userConfig.clientUserId = response.user.id
                    userConfig.saveConfig(true)

                    fragment.messagesController.putUser(response.user, false)
                    NotificationCenter.getInstance(account).postNotificationName(NotificationCenter.mainUserInfoChanged)

                    BulletinFactory.of(fragment).createSimpleBulletin(R.raw.done, "Успешный вход как @" + (response.user.username ?: "bot")).show()

                    (fragment.parentActivity as? LaunchActivity)?.let {
                        it.switchToAccount(account, true)
                    }
                }
            }
        }
    }
}
