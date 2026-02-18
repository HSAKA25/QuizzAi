package com.example.quizz;

import android.animation.ValueAnimator;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.Collections;

public class LeaderboardActivity extends AppCompatActivity {

    ListView listView;
    ArrayList<Player> players = new ArrayList<>();
    PlayerAdapter adapter;
    DatabaseReference ref;
    String currentUid = "";   // logged-in user's UID

    TextView tvTotalPlayers, tvTopScore, tvAvgScore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leaderboard);

        listView       = findViewById(R.id.listView);
        tvTotalPlayers = findViewById(R.id.tvTotalPlayers);
        tvTopScore     = findViewById(R.id.tvTopScore);
        tvAvgScore     = findViewById(R.id.tvAvgScore);

        // Grab current user UID once
        FirebaseUser me = FirebaseAuth.getInstance().getCurrentUser();
        if (me != null) currentUid = me.getUid();

        adapter = new PlayerAdapter();
        listView.setAdapter(adapter);
        loadLeaderboardRealtime();
    }

    private void loadLeaderboardRealtime() {
        ref = FirebaseDatabase.getInstance().getReference("Leaderboard");
        ref.orderByChild("score").limitToLast(10)
                .addChildEventListener(new ChildEventListener() {
                    @Override public void onChildAdded(DataSnapshot ds, String prev) {
                        Player p = parsePlayer(ds);
                        if (p != null) { players.add(p); refreshList(); }
                    }
                    @Override public void onChildChanged(DataSnapshot ds, String prev) {
                        Player updated = parsePlayer(ds);
                        if (updated == null) return;
                        for (int i = 0; i < players.size(); i++) {
                            if (players.get(i).uid.equals(updated.uid)) {
                                players.set(i, updated); break;
                            }
                        }
                        refreshList();
                    }
                    @Override public void onChildRemoved(DataSnapshot ds) {
                        String uid = ds.getKey();
                        players.removeIf(p -> p.uid.equals(uid));
                        refreshList();
                    }
                    @Override public void onChildMoved(DataSnapshot ds, String prev) {}
                    @Override public void onCancelled(DatabaseError e) {}
                });
    }

    private Player parsePlayer(DataSnapshot ds) {
        String name   = ds.child("name").getValue(String.class);
        Integer score = ds.child("score").getValue(Integer.class);
        if (name == null || score == null) return null;
        return new Player(ds.getKey(), maskName(name), score);
    }

    // ── Privacy masking ───────────────────────────────────
    private String maskName(String raw) {
        if (raw == null || raw.isEmpty()) return "Player";
        if (raw.contains("@")) {
            String[] parts      = raw.split("@");
            String[] domainParts= parts[1].split("\\.", 2);
            String maskedDomain = maskString(domainParts[0], 1)
                    + (domainParts.length > 1 ? "." + domainParts[1] : "");
            return maskString(parts[0], 2) + "@" + maskedDomain;
        }
        String[] words = raw.trim().split("\\s+");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < words.length; i++) {
            sb.append(maskString(words[i], 2));
            if (i < words.length - 1) sb.append(" ");
        }
        return sb.toString();
    }

    private String maskString(String s, int show) {
        if (s == null || s.isEmpty()) return s;
        return s.length() <= show ? s : s.substring(0, show) + "***";
    }

    // ── Refresh ───────────────────────────────────────────
    private void refreshList() {
        Collections.sort(players, (a, b) -> b.score - a.score);
        for (int i = 0; i < players.size(); i++) players.get(i).rank = i + 1;
        adapter.notifyDataSetChanged();
        updateStats();
    }

    private void updateStats() {
        if (players.isEmpty()) return;
        animateCount(tvTotalPlayers, players.size(), "");
        animateCount(tvTopScore, players.get(0).score, "%");
        int sum = 0;
        for (Player p : players) sum += p.score;
        animateCount(tvAvgScore, sum / players.size(), "%");
    }

    private void animateCount(TextView tv, int target, String suffix) {
        ValueAnimator anim = ValueAnimator.ofInt(0, target);
        anim.setDuration(700);
        anim.setInterpolator(new DecelerateInterpolator());
        anim.addUpdateListener(a -> tv.setText(a.getAnimatedValue() + suffix));
        anim.start();
    }

    // ── Adapter ───────────────────────────────────────────
    class PlayerAdapter extends BaseAdapter {

        @Override public int getCount()          { return players.size(); }
        @Override public Object getItem(int pos) { return players.get(pos); }
        @Override public long getItemId(int pos) { return pos; }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null)
                convertView = LayoutInflater.from(LeaderboardActivity.this)
                        .inflate(R.layout.item_leaderboard, parent, false);

            Player p     = players.get(position);
            boolean isMe = p.uid.equals(currentUid);  // ← is this the logged-in user?

            TextView tvRank  = convertView.findViewById(R.id.tvRank);
            TextView tvName  = convertView.findViewById(R.id.tvName);
            TextView tvScore = convertView.findViewById(R.id.tvScore);
            View     rowCard = convertView.findViewById(R.id.rowCard);
            View     rankBar = convertView.findViewById(R.id.rankAccentBar);

            // ── Name + (You) tag ──────────────────────────
            if (isMe) {
                tvName.setText(p.name + "  (You)");
                tvName.setTextColor(0xFF7C3AED);        // purple
                rowCard.setBackgroundColor(0xFF180D35); // purple tinted row
            } else {
                tvName.setText(p.name);
                tvName.setTextColor(0xFFCDD5E0);
            }

            tvScore.setText(p.score + "%");

            switch (p.rank) {
                case 1:
                    tvRank.setText("🥇");
                    tvRank.setTextSize(22);
                    if (!isMe) rowCard.setBackgroundColor(0xFF1A1500);
                    rankBar.setBackgroundColor(0xFFFFD700);
                    tvScore.setTextColor(0xFFFFD700);
                    break;
                case 2:
                    tvRank.setText("🥈");
                    tvRank.setTextSize(20);
                    if (!isMe) rowCard.setBackgroundColor(0xFF111820);
                    rankBar.setBackgroundColor(0xFFC0C0C0);
                    tvScore.setTextColor(0xFFC0C0C0);
                    break;
                case 3:
                    tvRank.setText("🥉");
                    tvRank.setTextSize(20);
                    if (!isMe) rowCard.setBackgroundColor(0xFF150D08);
                    rankBar.setBackgroundColor(0xFFCD7F32);
                    tvScore.setTextColor(0xFFCD7F32);
                    break;
                default:
                    tvRank.setText(String.valueOf(p.rank));
                    tvRank.setTextSize(15);
                    if (!isMe) rowCard.setBackgroundColor(0xFF0A1221);
                    rankBar.setBackgroundColor(0xFF1E3A5F);
                    tvScore.setTextColor(0xFF00E5FF);
                    break;
            }

            convertView.setAlpha(0f);
            convertView.setTranslationX(40f);
            convertView.animate()
                    .alpha(1f).translationX(0f)
                    .setDuration(250).setStartDelay(position * 40L)
                    .setInterpolator(new DecelerateInterpolator())
                    .start();

            return convertView;
        }
    }

    // ── Player model ──────────────────────────────────────
    static class Player {
        String uid, name;
        int score, rank;
        Player(String uid, String name, int score) {
            this.uid = uid; this.name = name; this.score = score;
        }
    }
}