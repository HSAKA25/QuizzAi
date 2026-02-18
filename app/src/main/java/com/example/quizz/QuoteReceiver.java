package com.example.quizz;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import androidx.core.app.NotificationCompat;

public class QuoteReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context ctx, Intent intent) {

        // Reschedule for next day
        SharedPreferences prefs =
                ctx.getSharedPreferences("quizzai_prefs", Context.MODE_PRIVATE);
        int hour   = prefs.getInt("quote_hour",   9);
        int minute = prefs.getInt("quote_minute", 0);
        NotificationHelper.scheduleDailyQuote(ctx, hour, minute);

        // Tap → open app
        Intent openApp = new Intent(ctx, page3.class);
        openApp.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                ctx, 1, openApp,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // Today's quote
        String quote = NotificationHelper.getDailyQuote();

        NotificationCompat.Builder builder = new NotificationCompat.Builder(
                ctx, NotificationHelper.CHANNEL_QUOTE_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle("💬 Quote of the Day")
                .setContentText(quote)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(quote))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setDefaults(NotificationCompat.DEFAULT_SOUND);

        NotificationManager nm =
                (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
        if (nm != null) nm.notify(NotificationHelper.NOTIF_QUOTE_ID, builder.build());
    }
}