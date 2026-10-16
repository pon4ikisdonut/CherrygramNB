package org.telegram.messenger

import android.app.Activity
import android.os.Handler
import android.os.Looper
import org.telegram.ui.ActionBar.AlertDialog
import java.lang.ref.WeakReference

object BlockedNotice {

    private val mainHandler = Handler(Looper.getMainLooper())

    private var currentActivity: WeakReference<Activity>? = null
    private var pending = false
    private var shown = false

    @JvmStatic
    fun request() {
        mainHandler.post {
            if (!shown) {
                pending = true
                showIfPossible()
            }
        }
    }

    @JvmStatic
    fun attach(activity: Activity) {
        currentActivity = WeakReference(activity)

        mainHandler.post {
            showIfPossible()
        }
    }

    @JvmStatic
    fun detach(activity: Activity) {
        if (currentActivity?.get() === activity) {
            currentActivity = null
        }
    }

    private fun showIfPossible() {
        if (!pending || shown) {
            return
        }

        val activity = currentActivity?.get() ?: return

        if (activity.isFinishing || activity.isDestroyed) {
            return
        }

        shown = true
        pending = false

        try {
            AlertDialog.Builder(activity)
                .setTitle("CherryGram Ban")
                .setMessage(
                    "Вы заблокированы в оригинальном CherryGram, но в форке CherrygramNB (автор pon4ikisdonut) " +
                        "кик из сессии полностью вырезан. Можете свободно пользоваться клиентом!"
                )
                .setPositiveButton("Понятно", null)
                .show()
        } catch (t: Throwable) {
            t.printStackTrace()
        }
    }
}