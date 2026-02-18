package com.example.quizz;

import android.animation.ValueAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class result extends AppCompatActivity {

    TextView totalQuesText, correctText, wrongText, skippedText;
    TextView scoreBigText, scoreText, messageText;
    Button analyticsBtn, retryBtn, homeBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        // ── Bind views ──────────────────────────────────────────
        totalQuesText = findViewById(R.id.totalQuesText);
        correctText   = findViewById(R.id.correctText);
        wrongText     = findViewById(R.id.wrongText);
        skippedText   = findViewById(R.id.skippedText);
        scoreBigText  = findViewById(R.id.scoreBigText);
        scoreText     = findViewById(R.id.scoreText);
        messageText   = findViewById(R.id.messageText);
        analyticsBtn  = findViewById(R.id.analyticsBtn);
        retryBtn      = findViewById(R.id.retryBtn);
        homeBtn       = findViewById(R.id.homeBtn);

        // ── Get intent data ─────────────────────────────────────
        int total   = getIntent().getIntExtra("totalQuestions", 0);
        int correct = getIntent().getIntExtra("correctAnswers", 0);
        int skipped = getIntent().getIntExtra("skippedAnswers", 0);
        int wrong   = total - correct - skipped;
        int score   = total > 0 ? (int) ((correct * 100.0) / total) : 0;

        // ── Fill stat cards ─────────────────────────────────────
        totalQuesText.setText(String.valueOf(total));
        correctText.setText(String.valueOf(correct));
        wrongText.setText(String.valueOf(wrong));
        skippedText.setText(String.valueOf(skipped));
        scoreText.setText(correct + " out of " + total + " correct");

        // ── Animate score counter 0 → score% ────────────────────
        animateScore(score);

        // ── Color the big score circle by performance ───────────
        applyScoreColor(scoreBigText, score);

        // ── Dynamic motivational message ────────────────────────
        messageText.setText(getMotivationalMessage(score));

        // ── Save to Firebase ────────────────────────────────────
        updateLeaderboard(score);
        saveScoreHistory(score);

        // ── Button listeners ─────────────────────────────────────
        analyticsBtn.setOnClickListener(v ->
                startActivity(new Intent(result.this, AnalyticsActivity.class))
        );

        retryBtn.setOnClickListener(v -> {
            // Go back to quiz — adjust target Activity as needed
            Intent intent = new Intent(result.this, Python.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });

        homeBtn.setOnClickListener(v -> {
            Intent intent = new Intent(result.this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });
    }

    // ── Animate the big % counter ─────────────────────────────
    private void animateScore(int targetScore) {
        ValueAnimator animator = ValueAnimator.ofInt(0, targetScore);
        animator.setDuration(1200);
        animator.setInterpolator(new DecelerateInterpolator());
        animator.addUpdateListener(anim -> {
            int val = (int) anim.getAnimatedValue();
            scoreBigText.setText(val + "%");
        });
        animator.start();
    }

    // ── Colour the big circle based on score ─────────────────
    private void applyScoreColor(TextView view, int score) {
        if (score >= 80) {
            view.setTextColor(android.graphics.Color.parseColor("#22C55E")); // green
        } else if (score >= 50) {
            view.setTextColor(android.graphics.Color.parseColor("#F59E0B")); // amber
        } else {
            view.setTextColor(android.graphics.Color.parseColor("#EF4444")); // red
        }
    }

    // ── Dynamic message based on score ───────────────────────
    private String getMotivationalMessage(int score) {
        if (score == 100) return "🏆 Perfect score! You're a Python master!";
        if (score >= 80)  return "🎉 Excellent work! Almost flawless!";
        if (score >= 60)  return "👍 Good job! A bit more practice and you'll ace it!";
        if (score >= 40)  return "📚 Not bad! Review the topics and try again!";
        return "💪 Keep going! Every attempt makes you stronger!";
    }

    // ── Save to Leaderboard ───────────────────────────────────
    private void updateLeaderboard(int score) {
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null) return;

        DatabaseReference userRef = FirebaseDatabase.getInstance()
                .getReference("Users").child(uid);

        userRef.child("name").get().addOnSuccessListener(snapshot -> {
            String name = snapshot.getValue(String.class);
            if (name == null) return;

            DatabaseReference leaderRef = FirebaseDatabase.getInstance()
                    .getReference("Leaderboard").child(uid);

            leaderRef.child("name").setValue(name);
            leaderRef.child("score").setValue(score);
        });
    }

    // ── Save score to history ─────────────────────────────────
    private void saveScoreHistory(int score) {
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null) return;

        FirebaseDatabase.getInstance()
                .getReference("Users")
                .child(uid)
                .child("scores")
                .push()
                .setValue(score);
    }
}