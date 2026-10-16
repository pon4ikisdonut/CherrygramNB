/**
 * This is the source code of Cherrygram for Android.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 * Please, be respectful and credit the original author if you use this code.
 *
 * Copyright github.com/arsLan4k1390, 2022-2026.
 */

package uz.unnarsx.cherrygram.core.helpers;

import static android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK;
import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Process;

import org.telegram.ui.LaunchActivity;

import java.util.ArrayList;
import java.util.Arrays;

public final class AppRestartHelper extends Activity {

    private static final String KEY_RESTART_INTENTS = "cherrygram_restart_intents";
    private static final String KEY_MAIN_PROCESS_PID = "cherrygram_main_process_pid";

    private static final String PREFS_RESTART = "cherrygram_restart_safety";
    private static final String KEY_LAST_RESTART_TIME = "last_restart_time";
    private static final long MIN_RESTART_INTERVAL_MS = 15000; // 15 seconds debounce

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Process.killProcess(getIntent().getIntExtra(KEY_MAIN_PROCESS_PID, -1));
        ArrayList<Intent> intents = getIntent().getParcelableArrayListExtra(KEY_RESTART_INTENTS);
        if (intents != null && !intents.isEmpty()) {
            startActivities(intents.toArray(new Intent[0]));
        }
        finish();
        Runtime.getRuntime().exit(0);
    }

    private static void triggerRebirth(Context context, Intent... nextIntents) {
        nextIntents[0].addFlags(FLAG_ACTIVITY_NEW_TASK | FLAG_ACTIVITY_CLEAR_TASK);
        Intent intent = new Intent(context, AppRestartHelper.class);
        intent.addFlags(FLAG_ACTIVITY_NEW_TASK);
        intent.putParcelableArrayListExtra(KEY_RESTART_INTENTS, new ArrayList<>(Arrays.asList(nextIntents)));
        intent.putExtra(KEY_MAIN_PROCESS_PID, Process.myPid());
        context.startActivity(intent);
    }

    public static void restartApp(Context context) {
        if (context == null) return;
        try {
            android.content.SharedPreferences prefs = context.getSharedPreferences(PREFS_RESTART, Context.MODE_PRIVATE);
            long now = System.currentTimeMillis();
            long lastRestart = prefs.getLong(KEY_LAST_RESTART_TIME, 0);
            if (now - lastRestart < MIN_RESTART_INTERVAL_MS) {
                android.util.Log.e("CherrygramNB", "Suppressed restartApp: called too rapidly (" + (now - lastRestart) + "ms since last restart)");
                return;
            }
            prefs.edit().putLong(KEY_LAST_RESTART_TIME, now).apply();
            android.util.Log.e("CherrygramNB", "restartApp triggered by: ", new Throwable());
            triggerRebirth(context, new Intent(context, LaunchActivity.class));
        } catch (Throwable t) {
            android.util.Log.e("CherrygramNB", "Error in restartApp", t);
        }
    }

    public static void killApp() {
        android.util.Log.e("CherrygramNB", "killApp called");
        System.exit(0);
    }

}

