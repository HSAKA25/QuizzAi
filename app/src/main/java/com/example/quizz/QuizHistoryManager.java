package com.example.quizz;

import android.content.Context;
import android.content.SharedPreferences;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.*;

public class QuizHistoryManager {

    private static final String PREF_NAME   = "quiz_history_prefs";
    private static final String KEY_HISTORY = "history_list";
    private static final int    MAX_ENTRIES = 50;

    // ── Data model ───────────────────────────────────────────────
    public static class QuizResult {
        public String topic;        // "Java", "Python", "Kotlin", "C#"
        public String difficulty;   // "Easy", "Medium", "Hard"
        public int    score;        // correct answers
        public int    total;        // total questions
        public int    timeTaken;    // seconds
        public String date;         // "Feb 13, 2026  9:41 PM"
        public String badge;        // earned badge if any

        public QuizResult(String topic, String difficulty,
                          int score, int total, int timeTaken) {
            this.topic      = topic;
            this.difficulty = difficulty;
            this.score      = score;
            this.total      = total;
            this.timeTaken  = timeTaken;
            this.date       = new SimpleDateFormat(
                    "MMM dd, yyyy  h:mm a", Locale.getDefault())
                    .format(new Date());
            this.badge      = BadgeManager.getBadgeForResult(score, total, difficulty);
        }

        public int getPercentage() {
            return total > 0 ? (score * 100) / total : 0;
        }

        public String getScoreLabel() {
            return score + " / " + total + "  (" + getPercentage() + "%)";
        }

        public String getEmoji() {
            int p = getPercentage();
            if (p == 100) return "🏆";
            if (p >= 80)  return "🌟";
            if (p >= 60)  return "✅";
            if (p >= 40)  return "📚";
            return "💪";
        }

        public String getTopicEmoji() {
            switch (topic) {
                case "Java":   return "☕";
                case "Python": return "🐍";
                case "Kotlin": return "📱";
                case "C#":     return "✨";
                case "Math":   return "🔢";
                case "Science":return "🔬";
                default:       return "📝";
            }
        }
    }

    // ── Save a result ────────────────────────────────────────────
    public static void save(Context ctx, QuizResult result) {
        List<QuizResult> list = getAll(ctx);
        list.add(0, result); // newest first
        if (list.size() > MAX_ENTRIES) list = list.subList(0, MAX_ENTRIES);
        SharedPreferences prefs = ctx.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit().putString(KEY_HISTORY, new Gson().toJson(list)).apply();
    }

    // ── Get all results ──────────────────────────────────────────
    public static List<QuizResult> getAll(Context ctx) {
        SharedPreferences prefs = ctx.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String json = prefs.getString(KEY_HISTORY, null);
        if (json == null) return new ArrayList<>();
        Type type = new TypeToken<List<QuizResult>>(){}.getType();
        List<QuizResult> list = new Gson().fromJson(json, type);
        return list != null ? list : new ArrayList<>();
    }

    // ── Stats helpers ────────────────────────────────────────────
    public static int getTotalQuizzes(Context ctx) {
        return getAll(ctx).size();
    }

    public static int getBestScore(Context ctx, String topic) {
        int best = 0;
        for (QuizResult r : getAll(ctx)) {
            if (r.topic.equals(topic) && r.getPercentage() > best)
                best = r.getPercentage();
        }
        return best;
    }

    public static double getAverageScore(Context ctx) {
        List<QuizResult> list = getAll(ctx);
        if (list.isEmpty()) return 0;
        int sum = 0;
        for (QuizResult r : list) sum += r.getPercentage();
        return (double) sum / list.size();
    }

    public static void clear(Context ctx) {
        ctx.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
                .edit().remove(KEY_HISTORY).apply();
    }
}