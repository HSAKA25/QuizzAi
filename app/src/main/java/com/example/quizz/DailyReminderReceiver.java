package com.example.quizz;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;

import androidx.core.app.NotificationCompat;

public class DailyReminderReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context ctx, Intent intent) {

        // Reschedule for next day
        SharedPreferences prefs =
                ctx.getSharedPreferences("quizzai_prefs", Context.MODE_PRIVATE);
        int hour   = prefs.getInt("reminder_hour",   20);
        int minute = prefs.getInt("reminder_minute",  0);
        NotificationHelper.scheduleDailyReminder(ctx, hour, minute);

        // Tap → open app
        Intent openApp = new Intent(ctx, page3.class);
        openApp.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                ctx, 0, openApp,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // Practice reminder only — no quote here
        NotificationCompat.Builder builder = new NotificationCompat.Builder(
                ctx, NotificationHelper.CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_popup_reminder)
                .setContentTitle("🧠 Time to Practice!")
                .setContentText("Keep your streak alive — quiz yourself today!")
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText("Keep your streak alive — quiz yourself today!\n\nOpen the app and take a quick quiz! 🚀"))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setDefaults(NotificationCompat.DEFAULT_ALL);

        NotificationManager nm =
                (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
        if (nm != null) nm.notify(NotificationHelper.NOTIF_ID, builder.build());
    }
}