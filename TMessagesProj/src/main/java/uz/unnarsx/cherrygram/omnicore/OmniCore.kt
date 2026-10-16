package uz.unnarsx.cherrygram.omnicore

import android.content.Context
import android.os.Environment
import dalvik.system.DexClassLoader
import org.telegram.messenger.ApplicationLoader
import org.telegram.messenger.FileLog
import uz.unnarsx.cherrygram.core.configs.CherrygramNBConfig
import java.io.File
import java.util.concurrent.CopyOnWriteArrayList

abstract class OmniPlugin {
    abstract val name: String
    abstract val version: String
    abstract val author: String

    open fun onInitialize(context: Context) {}
    open fun onEvent(event: OmniEvent): Boolean = false
    open fun onDestroy() {}
}

object OmniCore {

    private val loadedPlugins = CopyOnWriteArrayList<OmniPlugin>()
    private var isInitialized = false

    @JvmStatic
    fun init(context: Context) {
        if (isInitialized) return
        isInitialized = true

        if (!CherrygramNBConfig.omniCoreEnabled) return

        ensurePluginsDirectory()
        loadAllPlugins(context)
    }

    @JvmStatic
    fun getPluginsDirectory(): File {
        val extDir = File(Environment.getExternalStorageDirectory(), "CherrygramNB/Plugins")
        if (extDir.exists() || extDir.mkdirs()) {
            return extDir
        }
        val intDir = File(ApplicationLoader.applicationContext.filesDir, "plugins")
        if (!intDir.exists()) intDir.mkdirs()
        return intDir
    }

    private fun ensurePluginsDirectory() {
        try {
            val dir = getPluginsDirectory()
            val exampleFile = File(dir, "example_auto_reply.txt")
            if (!exampleFile.exists()) {
                exampleFile.writeText(
                    "# CherrygramNB OmniCore Quick Rule Example\n" +
                    "# Format: MATCH [regex] -> REPLY [text]\n" +
                    "MATCH ^/ping$ -> REPLY Pong из OmniCore! 🍒\n"
                )
            }
        } catch (ignored: Exception) {}
    }

    @JvmStatic
    fun loadAllPlugins(context: Context) {
        if (!CherrygramNBConfig.omniCoreEnabled) return

        val dir = getPluginsDirectory()
        val files = dir.listFiles() ?: return

        for (file in files) {
            try {
                if (file.name.endsWith(".dex") || file.name.endsWith(".apk")) {
                    loadDexPlugin(context, file)
                } else if (file.name.endsWith(".txt") || file.name.endsWith(".rule")) {
                    loadQuickRulePlugin(file)
                }
            } catch (t: Throwable) {
                OmniSandbox.handlePluginError(file.name, t)
            }
        }
    }

    private fun loadDexPlugin(context: Context, file: File) {
        val dexOptDir = File(context.cacheDir, "dex_opt")
        if (!dexOptDir.exists()) dexOptDir.mkdirs()

        val classLoader = DexClassLoader(file.absolutePath, dexOptDir.absolutePath, null, context.classLoader)
        val manifestName = file.nameWithoutExtension
        try {
            val clazz = classLoader.loadClass("uz.unnarsx.cherrygram.plugins.$manifestName")
            if (OmniPlugin::class.java.isAssignableFrom(clazz)) {
                val plugin = clazz.getDeclaredConstructor().newInstance() as OmniPlugin
                plugin.onInitialize(context)
                loadedPlugins.add(plugin)
                FileLog.d("[OmniCore] Loaded Dex Plugin: ${plugin.name} v${plugin.version}")
            }
        } catch (e: Exception) {
            OmniSandbox.handlePluginError(file.name, e)
        }
    }

    private fun loadQuickRulePlugin(file: File) {
        val rules = ArrayList<Pair<Regex, String>>()
        file.readLines().forEach { line ->
            val trimmed = line.trim()
            if (trimmed.startsWith("MATCH") && trimmed.contains("-> REPLY")) {
                val parts = trimmed.substring("MATCH".length).split("-> REPLY")
                if (parts.size == 2) {
                    val pattern = parts[0].trim()
                    val reply = parts[1].trim()
                    try {
                        rules.add(Pair(pattern.toRegex(), reply))
                    } catch (ignored: Exception) {}
                }
            }
        }

        if (rules.isNotEmpty()) {
            val rulePlugin = object : OmniPlugin() {
                override val name: String = file.nameWithoutExtension
                override val version: String = "1.0"
                override val author: String = "User"

                override fun onEvent(event: OmniEvent): Boolean {
                    if (event is OmniEvent.MessageReceived && !event.isOutgoing && !event.isChannel) {
                        for ((regex, reply) in rules) {
                            if (regex.containsMatchIn(event.text)) {
                                OmniAPI.Chat.reply(event.chatId, event.messageId, reply)
                                return true
                            }
                        }
                    }
                    return false
                }
            }
            loadedPlugins.add(rulePlugin)
            FileLog.d("[OmniCore] Loaded Rule Plugin: ${rulePlugin.name} with ${rules.size} rules")
        }
    }

    @JvmStatic
    fun onEvent(event: OmniEvent): Boolean {
        if (!CherrygramNBConfig.omniCoreEnabled) return false

        // 1. First dispatch to custom event bus
        val eventName = when (event) {
            is OmniEvent.MessageReceived -> "onMessageReceived"
            is OmniEvent.MessageSent -> "onMessageSent"
            is OmniEvent.ChatOpened -> "onChatOpened"
            is OmniEvent.MenuCreated -> "onMenuCreated"
        }
        if (OmniAPI.Events.dispatch(eventName, event)) {
            return true
        }

        // 2. Dispatch to loaded plugins
        var handled = false
        for (plugin in loadedPlugins) {
            try {
                if (CherrygramNBConfig.omniCoreSandboxStrict) {
                    val res = OmniSandbox.executeWithTimeout(plugin.name, 2000) {
                        plugin.onEvent(event)
                    }
                    if (res == true) handled = true
                } else {
                    if (plugin.onEvent(event)) handled = true
                }
            } catch (t: Throwable) {
                OmniSandbox.handlePluginError(plugin.name, t)
            }
        }
        return handled
    }

    @JvmStatic
    fun getLoadedPlugins(): List<OmniPlugin> = loadedPlugins
}
