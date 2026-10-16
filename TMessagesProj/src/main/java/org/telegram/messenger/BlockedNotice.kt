package org.telegram.messenger

import android.app.Activity
import android.app.AlertDialog
import android.os.Handler
import android.os.Looper
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

        try {
            AlertDialog.Builder(activity)
                .setTitle("CherryGram Ban")
                .setMessage(
                    "Вы заблокированы в CherryGram, но я (pon4ikisdonut) " +
                        "вырезал кик из сессии, " +
                        "так что можете пользоваться, " +
                        "если нравится клиент. Но задумайтесь " +
                        "над тем, кто разработчик, если " +
                        "он так поступает."
                )
                .setPositiveButton("Спасибо") { dialog, _ ->
                    dialog.dismiss()
                }
                .setCancelable(true)
                .show()

            shown = true
            pending = false
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}