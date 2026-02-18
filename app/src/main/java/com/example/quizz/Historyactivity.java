package com.example.quizz;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.*;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import java.util.List;

public class Historyactivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz_history);

        findViewById(R.id.backBtn).setOnClickListener(v -> finish());

        List<QuizHistoryManager.QuizResult> results = QuizHistoryManager.getAll(this);

        // Stats
        ((TextView)findViewById(R.id.totalQuizzesText))
                .setText(String.valueOf(results.size()));
        ((TextView)findViewById(R.id.avgScoreText))
                .setText(String.format("%.0f%%", QuizHistoryManager.getAverageScore(this)));
        ((TextView)findViewById(R.id.badgesEarnedText))
                .setText(BadgeManager.getEarnedCount(this)
                        + " / " + BadgeManager.getAllBadges().size());

        ListView list   = findViewById(R.id.historyList);
        TextView empty  = findViewById(R.id.emptyText);

        if (results.isEmpty()) {
            empty.setVisibility(View.VISIBLE);
            list.setVisibility(View.GONE);
        } else {
            empty.setVisibility(View.GONE);
            list.setVisibility(View.VISIBLE);
            list.setAdapter(new HistoryAdapter(this, results));
        }
    }

    static class HistoryAdapter extends ArrayAdapter<QuizHistoryManager.QuizResult> {
        HistoryAdapter(Context ctx, List<QuizHistoryManager.QuizResult> list) {
            super(ctx, 0, list);
        }

        @Override
        public View getView(int pos, View cv, ViewGroup parent) {
            QuizHistoryManager.QuizResult r = getItem(pos);
            cv = LayoutInflater.from(getContext())
                    .inflate(R.layout.item_history, parent, false);

            ((TextView) cv.findViewById(R.id.historyEmoji)).setText(r.getEmoji());
            ((TextView) cv.findViewById(R.id.historyTopic)).setText(r.getTopicEmoji()+" "+r.topic);
            ((TextView) cv.findViewById(R.id.historyScore)).setText(r.getScoreLabel());
            ((TextView) cv.findViewById(R.id.historyDate)).setText(r.date);
            ((TextView) cv.findViewById(R.id.historyBadge)).setText(r.badge);

            int mins = r.timeTaken / 60, secs = r.timeTaken % 60;
            ((TextView) cv.findViewById(R.id.historyTime)).setText(
                    mins > 0 ? String.format("⏱ %dm %ds", mins, secs)
                            : String.format("⏱ %ds", secs));

            // Difficulty chip
            TextView diffText = cv.findViewById(R.id.historyDiff);
            diffText.setText(r.difficulty);
            GradientDrawable diffBg = new GradientDrawable();
            diffBg.setCornerRadius(24f);
            switch (r.difficulty) {
                case "Easy":   diffBg.setColor(0xFF065F46); diffText.setTextColor(0xFF34D399); break;
                case "Medium": diffBg.setColor(0xFF78350F); diffText.setTextColor(0xFFFBBF24); break;
                case "Hard":   diffBg.setColor(0xFF7F1D1D); diffText.setTextColor(0xFFF87171); break;
            }
            diffText.setBackground(diffBg);

            // Score bar
            View track = cv.findViewById(R.id.scoreTrack);
            View fill  = cv.findViewById(R.id.scoreBar);
            if (track != null && fill != null) {
                track.post(() -> {
                    int tw = track.getWidth();
                    fill.getLayoutParams().width = (int)(tw * r.getPercentage() / 100.0);
                    fill.requestLayout();
                    GradientDrawable fb = new GradientDrawable();
                    fb.setCornerRadius(4f);
                    int p = r.getPercentage();
                    fb.setColor(p >= 80 ? 0xFF059669 : p >= 60 ? 0xFFF59E0B : 0xFFDC2626);
                    fill.setBackground(fb);
                });
            }
            return cv;
        }
    }
}