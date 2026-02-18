package com.example.quizz;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import java.util.Calendar;
import java.util.Random;

public class NotificationHelper {

    public static final String CHANNEL_ID       = "quizzai_daily";
    public static final String CHANNEL_NAME     = "Daily Practice Reminder";
    public static final String CHANNEL_QUOTE_ID   = "quizzai_quote";
    public static final String CHANNEL_QUOTE_NAME = "Daily Quote";
    public static final int    NOTIF_ID         = 1001;
    public static final int    NOTIF_QUOTE_ID   = 1002;

    // ── 30 motivational coding quotes ─────────────────────────
    public static final String[] QUOTES = {
            "\"The best way to predict the future is to invent it.\" — Alan Kay",
            "\"Code is like humor. When you have to explain it, it's bad.\" — Cory House",
            "\"First, solve the problem. Then, write the code.\" — John Johnson",
            "\"Any fool can write code that a computer can understand. Good programmers write code that humans can understand.\" — Martin Fowler",
            "\"Experience is the name everyone gives to their mistakes.\" — Oscar Wilde",
            "\"In order to be irreplaceable, one must always be different.\" — Coco Chanel",
            "\"Java is to JavaScript what car is to carpet.\" — Chris Heilmann",
            "\"Knowledge is power. Sharing knowledge is the key to unlocking that power.\" — Martin Uzochukwu",
            "\"The only way to learn a new programming language is by writing programs in it.\" — Dennis Ritchie",
            "\"Programming isn't about what you know; it's about what you can figure out.\" — Chris Pine",
            "\"The function of good software is to make the complex appear simple.\" — Grady Booch",
            "\"Talk is cheap. Show me the code.\" — Linus Torvalds",
            "\"Make it work, make it right, make it fast.\" — Kent Beck",
            "\"Simplicity is the soul of efficiency.\" — Austin Freeman",
            "\"Before software can be reusable, it first has to be usable.\" — Ralph Johnson",
            "\"An idiot admires complexity, a genius admires simplicity.\" — Terry A. Davis",
            "\"Perfection is achieved not when there is nothing more to add, but when there is nothing left to take away.\" — Antoine de Saint-Exupéry",
            "\"Software is a great combination between artistry and engineering.\" — Bill Gates",
            "\"Every great developer you know got there by solving problems they were unqualified to solve.\" — Patrick McKenzie",
            "\"Without requirements or design, programming is the art of adding bugs to an empty text file.\" — Louis Srygley",
            "\"One of the best programming skills you can have is knowing when to walk away for a while.\" — Oscar Godson",
            "\"The most disastrous thing that you can ever learn is your first programming language.\" — Alan Kay",
            "\"Debugging is twice as hard as writing the code in the first place.\" — Brian Kernighan",
            "\"It's not a bug — it's an undocumented feature.\" — Anonymous",
            "\"A clever person solves a problem. A wise person avoids it.\" — Albert Einstein",
            "\"Programs must be written for people to read, and only incidentally for machines to execute.\" — Abelson & Sussman",
            "\"The most important property of a program is whether it accomplishes the intention of its user.\" — C.A.R. Hoare",
            "\"Consistency is the key. If you can't consistently do something small, how will you achieve something big?\"",
            "\"Learning never exhausts the mind.\" — Leonardo da Vinci",
            "\"Every expert was once a beginner. Keep practicing!\"",
    };

    // ── Get today's quote (consistent for the day) ─────────────
    public static String getDailyQuote() {
        Calendar cal = Calendar.getInstance();
        int dayOfYear = cal.get(Calendar.DAY_OF_YEAR);
        int year      = cal.get(Calendar.YEAR);
        int index     = (dayOfYear + year) % QUOTES.length;
        return QUOTES[index];
    }

    // ── Get a random quote ─────────────────────────────────────
    public static String getRandomQuote() {
        return QUOTES[new Random().nextInt(QUOTES.length)];
    }

    // ── Create notification channels (Android 8+) ─────────────
    public static void createChannel(Context ctx) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager nm = ctx.getSystemService(NotificationManager.class);
            if (nm == null) return;

            // Reminder channel
            NotificationChannel reminderChannel = new NotificationChannel(
                    CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT);
            reminderChannel.setDescription("Daily coding practice reminder");
            reminderChannel.enableLights(true);
            reminderChannel.enableVibration(true);
            nm.createNotificationChannel(reminderChannel);

            // Quote channel
            NotificationChannel quoteChannel = new NotificationChannel(
                    CHANNEL_QUOTE_ID, CHANNEL_QUOTE_NAME, NotificationManager.IMPORTANCE_DEFAULT);
            quoteChannel.setDescription("Daily motivational coding quote");
            quoteChannel.enableLights(true);
            nm.createNotificationChannel(quoteChannel);
        }
    }

    // ── Schedule daily quote at 9:00 AM (default) ─────────────
    public static void scheduleDailyQuote(Context ctx) {
        scheduleDailyQuote(ctx, 9, 0);
    }

    // ── Schedule daily quote at user-chosen time ───────────────
    public static void scheduleDailyQuote(Context ctx, int hour, int minute) {
        AlarmManager am = (AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE);
        if (am == null) return;

        Intent intent = new Intent(ctx, QuoteReceiver.class);
        PendingIntent pi = PendingIntent.getBroadcast(
                ctx, 1, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, hour);
        cal.set(Calendar.MINUTE,      minute);
        cal.set(Calendar.SECOND,      0);
        cal.set(Calendar.MILLISECOND, 0);
        if (cal.getTimeInMillis() <= System.currentTimeMillis()) {
            cal.add(Calendar.DAY_OF_YEAR, 1);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), pi);
        } else {
            am.setRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(),
                    AlarmManager.INTERVAL_DAY, pi);
        }
    }

    // ── Cancel daily quote ─────────────────────────────────────
    public static void cancelDailyQuote(Context ctx) {
        AlarmManager am = (AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE);
        if (am == null) return;
        Intent intent = new Intent(ctx, QuoteReceiver.class);
        PendingIntent pi = PendingIntent.getBroadcast(
                ctx, 1, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        am.cancel(pi);
    }

    // ── Schedule daily alarm at given hour:minute ──────────────
    public static void scheduleDailyReminder(Context ctx, int hour, int minute) {
        AlarmManager am = (AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE);
        if (am == null) return;

        Intent intent = new Intent(ctx, DailyReminderReceiver.class);
        PendingIntent pi = PendingIntent.getBroadcast(
                ctx, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, hour);
        cal.set(Calendar.MINUTE,      minute);
        cal.set(Calendar.SECOND,      0);
        cal.set(Calendar.MILLISECOND, 0);

        // If time already passed today, schedule for tomorrow
        if (cal.getTimeInMillis() <= System.currentTimeMillis()) {
            cal.add(Calendar.DAY_OF_YEAR, 1);
        }

        // Use setExactAndAllowWhileIdle for reliable delivery
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), pi);
        } else {
            am.setRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pi);
        }
    }

    // ── Cancel scheduled reminder ──────────────────────────────
    public static void cancelReminder(Context ctx) {
        AlarmManager am = (AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE);
        if (am == null) return;
        Intent intent = new Intent(ctx, DailyReminderReceiver.class);
        PendingIntent pi = PendingIntent.getBroadcast(
                ctx, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        am.cancel(pi);
    }

    // ── Check if reminder is scheduled ────────────────────────
    public static boolean isReminderScheduled(Context ctx) {
        Intent intent = new Intent(ctx, DailyReminderReceiver.class);
        PendingIntent pi = PendingIntent.getBroadcast(
                ctx, 0, intent,
                PendingIntent.FLAG_NO_CREATE | PendingIntent.FLAG_IMMUTABLE
        );
        return pi != null;
    }
}