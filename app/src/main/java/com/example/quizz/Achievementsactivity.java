package com.example.quizz;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.*;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import java.util.List;

public class Achievementsactivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_badges);

        findViewById(R.id.backBtn).setOnClickListener(v -> finish());

        List<String> earnedIds          = BadgeManager.getEarnedIds(this);
        List<BadgeManager.Badge> all    = BadgeManager.getAllBadges();
        int earned = earnedIds.size(), total = all.size();

        ((TextView) findViewById(R.id.badgeCountText))
                .setText(earned + " / " + total + " Badges Earned");

        // Progress bar
        View track = findViewById(R.id.badgeProgressTrack);
        View fill  = findViewById(R.id.badgeProgressFill);
        if (track != null && fill != null) {
            track.post(() -> {
                fill.getLayoutParams().width =
                        (int)(track.getWidth() * (float) earned / total);
                fill.requestLayout();
            });
        }

        GridView grid = findViewById(R.id.badgesGrid);
        grid.setAdapter(new BadgeGridAdapter(this, all, earnedIds));
    }

    static class BadgeGridAdapter extends ArrayAdapter<BadgeManager.Badge> {
        final List<String> earnedIds;

        BadgeGridAdapter(Context ctx, List<BadgeManager.Badge> badges,
                         List<String> earnedIds) {
            super(ctx, 0, badges);
            this.earnedIds = earnedIds;
        }

        @Override
        public View getView(int pos, View cv, ViewGroup parent) {
            BadgeManager.Badge badge = getItem(pos);
            cv = LayoutInflater.from(getContext())
                    .inflate(R.layout.item_badge, parent, false);

            boolean earned = earnedIds.contains(badge.id);

            TextView icon  = cv.findViewById(R.id.badgeIcon);
            TextView title = cv.findViewById(R.id.badgeTitle);
            TextView desc  = cv.findViewById(R.id.badgeDesc);
            View     card  = cv.findViewById(R.id.badgeCard);

            icon.setText(earned ? badge.icon : "🔒");
            title.setText(badge.title);
            desc.setText(badge.description);

            GradientDrawable bg = new GradientDrawable();
            bg.setCornerRadius(16f);

            if (earned) {
                int c = (int)(Long.parseLong(badge.color, 16) | 0xFF000000);
                bg.setColor(mixColor(c, 0xFF111827, 0.82f));
                bg.setStroke(2, c);
                title.setTextColor(0xFFF0F6FC);
                icon.setAlpha(1f);
            } else {
                bg.setColor(0xFF0D1117);
                bg.setStroke(1, 0xFF1E293B);
                title.setTextColor(0xFF374151);
                desc.setTextColor(0xFF1F2937);
                icon.setAlpha(0.3f);
            }
            card.setBackground(bg);
            return cv;
        }

        private int mixColor(int c, int base, float ratio) {
            return Color.rgb(
                    (int)(Color.red(base)  * ratio + Color.red(c)   * (1-ratio)),
                    (int)(Color.green(base)* ratio + Color.green(c) * (1-ratio)),
                    (int)(Color.blue(base) * ratio + Color.blue(c)  * (1-ratio)));
        }
    }
}