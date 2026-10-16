package uz.unnarsx.cherrygram.omnicore

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.widget.Toast
import org.telegram.messenger.AndroidUtilities
import org.telegram.messenger.ApplicationLoader
import org.telegram.messenger.SendMessagesHelper
import org.telegram.messenger.UserConfig
import org.telegram.ui.ActionBar.AlertDialog
import org.telegram.ui.ActionBar.BaseFragment
import org.telegram.ui.Components.BulletinFactory
import java.util.concurrent.ConcurrentHashMap

object OmniAPI {

    object Chat {
        @JvmStatic
        fun sendMessage(chatId: Long, text: String) {
            val account = UserConfig.selectedAccount
            val params = SendMessagesHelper.SendMessageParams.of(text, chatId)
            SendMessagesHelper.getInstance(account).sendMessage(params)
        }

        @JvmStatic
        fun reply(chatId: Long, replyToMsgId: Int, text: String) {
            val account = UserConfig.selectedAccount
            val params = SendMessagesHelper.SendMessageParams.of(text, chatId)
            SendMessagesHelper.getInstance(account).sendMessage(params)
        }
    }

    object UI {
        private val contextMenuActions = ConcurrentHashMap<String, (Int, String?) -> Unit>()

        @JvmStatic
        fun showToast(message: String) {
            AndroidUtilities.runOnUIThread {
                Toast.makeText(ApplicationLoader.applicationContext, message, Toast.LENGTH_SHORT).show()
            }
        }

        @JvmStatic
        fun showAlert(title: String, message: String, fragment: BaseFragment? = null) {
            AndroidUtilities.runOnUIThread {
                val ctx = fragment?.context ?: ApplicationLoader.applicationContext
                val builder = AlertDialog.Builder(ctx)
                builder.setTitle(title)
                builder.setMessage(message)
                builder.setPositiveButton("OK", null)
                builder.show()
            }
        }

        @JvmStatic
        fun addMessageContextButton(title: String, action: (Int, String?) -> Unit) {
            contextMenuActions[title] = action
        }

        @JvmStatic
        fun getCustomMenuActions(): Map<String, (Int, String?) -> Unit> {
            return contextMenuActions
        }
    }

    class Storage(pluginName: String) {
        private val prefs: SharedPreferences = ApplicationLoader.applicationContext.getSharedPreferences(
            "omni_plugin_" + pluginName.replace("[^a-zA-Z0-9_]".toRegex(), "_"),
            Activity.MODE_PRIVATE
        )

        fun set(key: String, value: String) {
            prefs.edit().putString(key, value).apply()
        }

        fun get(key: String, defaultVal: String = ""): String {
            return prefs.getString(key, defaultVal) ?: defaultVal
        }

        fun setBool(key: String, value: Boolean) {
            prefs.edit().putBoolean(key, value).apply()
        }

        fun getBool(key: String, defaultVal: Boolean = false): Boolean {
            return prefs.getBoolean(key, defaultVal)
        }
    }

    object Events {
        private val listeners = ConcurrentHashMap<String, MutableList<OmniEventHandler>>()

        @JvmStatic
        fun on(eventName: String, handler: OmniEventHandler) {
            listeners.getOrPut(eventName) { ArrayList() }.add(handler)
        }

        @JvmStatic
        fun off(eventName: String, handler: OmniEventHandler) {
            listeners[eventName]?.remove(handler)
        }

        @JvmStatic
        fun dispatch(eventName: String, event: OmniEvent): Boolean {
            val handlers = listeners[eventName] ?: return false
            var handled = false
            for (handler in handlers) {
                try {
                    if (handler(event)) {
                        handled = true
                    }
                } catch (e: Throwable) {
                    OmniSandbox.handlePluginError("Handler:$eventName", e)
                }
            }
            return handled
        }
    }
}
