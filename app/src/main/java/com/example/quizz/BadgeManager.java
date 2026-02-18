package com.example.quizz;

import android.content.Context;
import android.content.SharedPreferences;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.*;

public class BadgeManager {

    private static final String PREF_NAME  = "quiz_badge_prefs";
    private static final String KEY_BADGES = "earned_badges";

    // ── Badge definitions ────────────────────────────────────────
    public static class Badge {
        public String id;
        public String icon;
        public String title;
        public String description;
        public String color;   // hex without #
        public boolean earned;
        public String earnedDate;

        public Badge(String id, String icon, String title,
                     String description, String color) {
            this.id          = id;
            this.icon        = icon;
            this.title       = title;
            this.description = description;
            this.color       = color;
            this.earned      = false;
        }
    }

    // ── All possible badges ──────────────────────────────────────
    public static List<Badge> getAllBadges() {
        List<Badge> badges = new ArrayList<>();
        badges.add(new Badge("first_quiz",    "🎯", "First Step",      "Complete your first quiz",                 "6C3AED"));
        badges.add(new Badge("perfect_score", "💯", "Perfect Score",   "Score 100% on any quiz",                   "FFD700"));
        badges.add(new Badge("speed_demon",   "⚡", "Speed Demon",     "Finish a quiz in under 60 seconds",        "F59E0B"));
        badges.add(new Badge("java_master",   "☕", "Java Master",     "Score 100% on Java Hard",                  "EF4444"));
        badges.add(new Badge("python_pro",    "🐍", "Python Pro",      "Score 100% on Python Hard",                "3B82F6"));
        badges.add(new Badge("kotlin_king",   "📱", "Kotlin King",     "Score 100% on Kotlin Hard",                "8B5CF6"));
        badges.add(new Badge("csharp_champ",  "✨", "C# Champion",     "Score 100% on C# Hard",                   "10B981"));
        badges.add(new Badge("quiz_5",        "🔥", "On Fire",         "Complete 5 quizzes",                       "F97316"));
        badges.add(new Badge("quiz_10",       "🌟", "Quiz Veteran",    "Complete 10 quizzes",                      "FBBF24"));
        badges.add(new Badge("quiz_25",       "🏅", "Quiz Expert",     "Complete 25 quizzes",                      "6366F1"));
        badges.add(new Badge("all_topics",    "🌐", "All-Rounder",     "Complete quizzes in all 4 topics",         "059669"));
        badges.add(new Badge("hard_mode",     "💪", "Hard Mode Hero",  "Complete a Hard quiz",                     "DC2626"));
        badges.add(new Badge("comeback",      "🔄", "Comeback Kid",    "Score 80%+ after scoring below 50%",       "0284C7"));
        badges.add(new Badge("consistent",    "📈", "Consistent",      "Score 70%+ in 3 quizzes in a row",         "7C3AED"));
        return badges;
    }

    // ── Check and award badges after a quiz ─────────────────────
    public static List<Badge> checkAndAward(Context ctx,
                                            QuizHistoryManager.QuizResult result) {

        List<Badge> newlyEarned = new ArrayList<>();
        List<String> earned     = getEarnedIds(ctx);
        List<QuizHistoryManager.QuizResult> history =
                QuizHistoryManager.getAll(ctx);

        // helper checks
        boolean perfect   = result.getPercentage() == 100;
        boolean isHard    = "Hard".equals(result.difficulty);
        int     totalDone = history.size(); // already saved before calling this

        // First quiz
        awardIf(ctx, earned, newlyEarned, "first_quiz", totalDone >= 1);
        // Perfect score
        awardIf(ctx, earned, newlyEarned, "perfect_score", perfect);
        // Speed demon — under 60s
        awardIf(ctx, earned, newlyEarned, "speed_demon", result.timeTaken < 60);
        // Topic masters (100% on Hard)
        awardIf(ctx, earned, newlyEarned, "java_master",
                "Java".equals(result.topic)   && perfect && isHard);
        awardIf(ctx, earned, newlyEarned, "python_pro",
                "Python".equals(result.topic) && perfect && isHard);
        awardIf(ctx, earned, newlyEarned, "kotlin_king",
                "Kotlin".equals(result.topic) && perfect && isHard);
        awardIf(ctx, earned, newlyEarned, "csharp_champ",
                "C#".equals(result.topic)     && perfect && isHard);
        // Quiz count milestones
        awardIf(ctx, earned, newlyEarned, "quiz_5",  totalDone >= 5);
        awardIf(ctx, earned, newlyEarned, "quiz_10", totalDone >= 10);
        awardIf(ctx, earned, newlyEarned, "quiz_25", totalDone >= 25);
        // Hard mode
        awardIf(ctx, earned, newlyEarned, "hard_mode", isHard);
        // All-rounder — has played all 4 topics
        Set<String> topics = new HashSet<>();
        for (QuizHistoryManager.QuizResult r : history) topics.add(r.topic);
        awardIf(ctx, earned, newlyEarned, "all_topics",
                topics.containsAll(Arrays.asList("Java","Python","Kotlin","C#")));
        // Consistent — last 3 quizzes all >= 70%
        if (history.size() >= 3) {
            boolean ok = true;
            for (int i = 0; i < 3; i++)
                if (history.get(i).getPercentage() < 70) { ok = false; break; }
            awardIf(ctx, earned, newlyEarned, "consistent", ok);
        }
        // Comeback kid
        if (history.size() >= 2) {
            boolean prevBad  = history.get(1).getPercentage() < 50;
            boolean nowGood  = result.getPercentage() >= 80;
            awardIf(ctx, earned, newlyEarned, "comeback", prevBad && nowGood);
        }

        return newlyEarned;
    }

    private static void awardIf(Context ctx, List<String> earned,
                                List<Badge> newList, String id, boolean condition) {
        if (condition && !earned.contains(id)) {
            earned.add(id);
            saveEarnedIds(ctx, earned);
            // Add date to the matching badge
            for (Badge b : getAllBadges()) {
                if (b.id.equals(id)) {
                    b.earned = true;
                    newList.add(b);
                    break;
                }
            }
        }
    }

    // ── Get earned badge IDs ─────────────────────────────────────
    public static List<String> getEarnedIds(Context ctx) {
        SharedPreferences prefs = ctx.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String json = prefs.getString(KEY_BADGES, null);
        if (json == null) return new ArrayList<>();
        Type type = new TypeToken<List<String>>(){}.getType();
        List<String> list = new Gson().fromJson(json, type);
        return list != null ? list : new ArrayList<>();
    }

    private static void saveEarnedIds(Context ctx, List<String> ids) {
        ctx.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
                .edit().putString(KEY_BADGES, new Gson().toJson(ids)).apply();
    }

    public static int getEarnedCount(Context ctx) {
        return getEarnedIds(ctx).size();
    }

    // ── Used by QuizHistoryManager ───────────────────────────────
    public static String getBadgeForResult(int score, int total, String difficulty) {
        int pct = total > 0 ? (score * 100) / total : 0;
        if (pct == 100) return "💯 Perfect!";
        if (pct >= 80 && "Hard".equals(difficulty)) return "🌟 Excellent";
        if (pct >= 80)  return "🌟 Great";
        if (pct >= 60)  return "✅ Good";
        if (pct >= 40)  return "📚 Keep Going";
        return "💪 Try Again";
    }
}