package com.example.quizz;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context ctx, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            SharedPreferences prefs =
                    ctx.getSharedPreferences("quizzai_prefs", Context.MODE_PRIVATE);
            boolean enabled = prefs.getBoolean("reminder_enabled", false);
            if (enabled) {
                int hour   = prefs.getInt("reminder_hour",   20);
                int minute = prefs.getInt("reminder_minute",  0);
                NotificationHelper.createChannel(ctx);
                NotificationHelper.scheduleDailyReminder(ctx, hour, minute);
            }
        }
    }
}