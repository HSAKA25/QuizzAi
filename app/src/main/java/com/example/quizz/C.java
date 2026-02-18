package com.example.quizz;

import android.animation.*;
import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.os.*;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.example.quizz.model.pythonques;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.*;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class C extends AppCompatActivity {

    TextView    q1, qnoText, timerText;
    RadioGroup  radioGroup;
    RadioButton r1, r2, r3, r4;
    Button      submitBtn, skipBtn, prevBtn;
    View        timerBar, timerBarTrack;

    List<pythonques> questionList    = new ArrayList<>();
    int  currentIndex                = 0;
    int  correctAnswersCount         = 0;
    int  skippedCount                = 0;
    String difficulty                = "easy"; // normalised to lowercase

    private static final int SECONDS_PER_QUESTION = 15;
    private CountDownTimer   questionTimer;
    private int              timeLeftSeconds       = SECONDS_PER_QUESTION;
    private long             quizStartTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_python);

        // ── Read difficulty — handles BOTH "EASY" and "easy" ──
        if (getIntent() != null) {
            String d = getIntent().getStringExtra("DIFFICULTY");
            if (d == null) d = getIntent().getStringExtra("difficulty");
            if (d != null) difficulty = d.toLowerCase(); // normalise
        }

        bindViews();

        // Override hardcoded title in shared layout
        TextView quizTitle = findViewById(R.id.quizTitle);
        if (quizTitle != null) quizTitle.setText("C Quiz");

        loadQuestionsFromJsonFile();
        quizStartTime = System.currentTimeMillis();
        submitBtn.setOnClickListener(v -> handleSubmit());
        skipBtn.setOnClickListener(v -> handleSkip());
        prevBtn.setOnClickListener(v -> handlePrev());
    }

    private void bindViews() {
        qnoText       = findViewById(R.id.qnoText);
        q1            = findViewById(R.id.q1);
        radioGroup    = findViewById(R.id.radioGroup);
        r1            = findViewById(R.id.r1);
        r2            = findViewById(R.id.r2);
        r3            = findViewById(R.id.r3);
        r4            = findViewById(R.id.r4);
        submitBtn     = findViewById(R.id.b);
        skipBtn       = findViewById(R.id.skipBtn);
        prevBtn       = findViewById(R.id.prevBtn);
        timerText     = findViewById(R.id.timerText);
        timerBar      = findViewById(R.id.timerBar);
        timerBarTrack = findViewById(R.id.timerBarTrack);
    }

    // ── Timer ──────────────────────────────────────────────
    private void startQuestionTimer() {
        cancelTimer();
        timeLeftSeconds = SECONDS_PER_QUESTION;
        updateTimerUI(timeLeftSeconds);
        questionTimer = new CountDownTimer(SECONDS_PER_QUESTION * 1000L, 1000) {
            @Override public void onTick(long ms) {
                timeLeftSeconds = (int)(ms / 1000);
                updateTimerUI(timeLeftSeconds);
            }
            @Override public void onFinish() {
                updateTimerUI(0);
                animateTimerExpired();
                new Handler(Looper.getMainLooper()).postDelayed(() -> handleSkip(), 600);
            }
        }.start();
    }

    private void updateTimerUI(int secs) {
        if (timerText != null) timerText.setText(secs + "s");
        int color = secs > 10 ? 0xFF059669 : secs > 5 ? 0xFFF59E0B : 0xFFDC2626;
        if (timerText != null) timerText.setTextColor(color);
        if (timerBar != null && timerBarTrack != null) {
            timerBarTrack.post(() -> {
                int trackW = timerBarTrack.getWidth();
                if (trackW > 0) {
                    timerBar.getLayoutParams().width =
                            Math.max((int)(trackW * (float)secs / SECONDS_PER_QUESTION), 0);
                    timerBar.requestLayout();
                    GradientDrawable bg = new GradientDrawable();
                    bg.setColor(color); bg.setCornerRadius(4f);
                    timerBar.setBackground(bg);
                }
            });
        }
    }

    private void animateTimerExpired() {
        if (timerText != null)
            ObjectAnimator.ofFloat(timerText, "translationX", 0f,-10f,10f,-10f,10f,0f)
                    .setDuration(400).start();
    }

    private void cancelTimer() {
        if (questionTimer != null) { questionTimer.cancel(); questionTimer = null; }
    }

    // ── Load questions ─────────────────────────────────────
    private String getJsonFileName() {
        switch (difficulty) {
            case "medium": return "c_medium.json";
            case "hard":   return "c_hard.json";
            default:       return "c_easy.json";
        }
    }

    private void loadQuestionsFromJsonFile() {
        try {
            InputStream is = getAssets().open(getJsonFileName());
            byte[] buf = new byte[is.available()]; is.read(buf); is.close();
            Type type = new TypeToken<List<pythonques>>(){}.getType();
            questionList = new Gson().fromJson(new String(buf, StandardCharsets.UTF_8), type);
            if (questionList != null && !questionList.isEmpty()) {
                Collections.shuffle(questionList);
                displayQuestion();
            } else {
                Toast.makeText(this, "No questions found!", Toast.LENGTH_SHORT).show();
            }
        } catch (IOException e) {
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    // ── Display ────────────────────────────────────────────
    private void displayQuestion() {
        if (currentIndex >= questionList.size()) { finishQuiz(); return; }

        pythonques q    = questionList.get(currentIndex);
        List<String> opts = new ArrayList<>(q.getOptions());
        Collections.shuffle(opts); // shuffle options each time

        radioGroup.clearCheck();
        // Reset radio button colors to default before showing new question
        r1.setTextColor(0xFFDEDEFF);
        r2.setTextColor(0xFFDEDEFF);
        r3.setTextColor(0xFFDEDEFF);
        r4.setTextColor(0xFFDEDEFF);
        r1.setEnabled(true);
        r2.setEnabled(true);
        r3.setEnabled(true);
        r4.setEnabled(true);

        qnoText.setText("Q " + (currentIndex + 1) + " / " + questionList.size()
                + "  •  " + capitalize(difficulty));
        q1.setText(q.getQuestion());
        r1.setText(opts.size() > 0 ? opts.get(0) : "");
        r2.setText(opts.size() > 1 ? opts.get(1) : "");
        r3.setText(opts.size() > 2 ? opts.get(2) : "");
        r4.setText(opts.size() > 3 ? opts.get(3) : "");

        q1.setAlpha(0f);
        q1.animate().alpha(1f).setDuration(250).start();
        startQuestionTimer();
    }

    // ── Answer handling ────────────────────────────────────
    private void handleSubmit() {
        int id = radioGroup.getCheckedRadioButtonId();
        if (id == -1) {
            Toast.makeText(this, "Select an answer!", Toast.LENGTH_SHORT).show();
            return;
        }
        cancelTimer();
        RadioButton sel  = findViewById(id);
        String answer    = sel.getText().toString().trim();
        String correct   = questionList.get(currentIndex).getAnswer().trim();

        if (answer.equalsIgnoreCase(correct)) {
            correctAnswersCount++;
            sel.setTextColor(0xFF34D399);
            Toast.makeText(this, "✅ Correct!", Toast.LENGTH_SHORT).show();
        } else {
            sel.setTextColor(0xFFF87171);
            Toast.makeText(this, "❌ Wrong! Ans: " + correct, Toast.LENGTH_SHORT).show();
        }
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            currentIndex++; displayQuestion();
        }, 700);
    }

    private void handlePrev() {
        if (currentIndex > 0) {
            cancelTimer();
            currentIndex--;
            displayQuestion();
        }
    }

    private void handleSkip() {
        cancelTimer(); skippedCount++; currentIndex++; displayQuestion();
    }

    // ── Finish ─────────────────────────────────────────────
    private void finishQuiz() {
        cancelTimer();
        int timeTaken = (int)((System.currentTimeMillis() - quizStartTime) / 1000);

        QuizHistoryManager.QuizResult r = new QuizHistoryManager.QuizResult(
                "C", capitalize(difficulty),
                correctAnswersCount, questionList.size(), timeTaken);
        QuizHistoryManager.save(this, r);

        List<BadgeManager.Badge> newBadges = BadgeManager.checkAndAward(this, r);

        Intent intent = new Intent(this, result.class);
        intent.putExtra("totalQuestions", questionList.size());
        intent.putExtra("correctAnswers", correctAnswersCount);
        intent.putExtra("skippedAnswers", skippedCount);
        intent.putExtra("difficulty",     capitalize(difficulty));
        intent.putExtra("topic",          "C");
        intent.putExtra("timeTaken",      timeTaken);
        if (!newBadges.isEmpty())
            intent.putExtra("newBadge",
                    newBadges.get(0).icon + " " + newBadges.get(0).title);
        startActivity(intent);
        finish();
    }

    private String capitalize(String s) {
        return (s == null || s.isEmpty()) ? s
                : s.substring(0,1).toUpperCase() + s.substring(1);
    }

    @Override protected void onDestroy() { super.onDestroy(); cancelTimer(); }
}