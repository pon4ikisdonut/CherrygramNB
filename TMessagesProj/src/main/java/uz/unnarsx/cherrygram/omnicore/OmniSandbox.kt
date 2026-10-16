package uz.unnarsx.cherrygram.omnicore

import android.os.Handler
import android.os.Looper
import android.widget.Toast
import org.telegram.messenger.AndroidUtilities
import org.telegram.messenger.ApplicationLoader
import org.telegram.messenger.FileLog
import java.util.concurrent.Callable
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

object OmniSandbox {

    private val executor = Executors.newCachedThreadPool()
    private val mainHandler = Handler(Looper.getMainLooper())

    @JvmStatic
    fun <T> executeWithTimeout(pluginName: String, timeoutMs: Long = 2000, block: () -> T): T? {
        val future = executor.submit(Callable {
            block()
        })

        return try {
            future.get(timeoutMs, TimeUnit.MILLISECONDS)
        } catch (e: TimeoutException) {
            future.cancel(true)
            handlePluginError(pluginName, RuntimeException("Execution timed out (> $timeoutMs ms)"))
            null
        } catch (t: Throwable) {
            handlePluginError(pluginName, t.cause ?: t)
            null
        }
    }

    @JvmStatic
    fun handlePluginError(pluginName: String, error: Throwable) {
        FileLog.e("[OmniCore Plugin Error: $pluginName] " + error.message, error)
        AndroidUtilities.runOnUIThread {
            Toast.makeText(
                ApplicationLoader.applicationContext,
                "⚠️ Ошибка в плагине $pluginName: ${error.localizedMessage ?: error.javaClass.simpleName}",
                Toast.LENGTH_LONG
            ).show()
        }
    }
}
