package com.example.quizz;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;

import com.example.quizz.model.pythonques;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.InputStream;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class QuizActivity extends AppCompatActivity {

    // ── Views ─────────────────────────────────────────────────
    TextView  qText, qNoText;
    RadioGroup radioGroup;
    RadioButton r1, r2, r3, r4;
    Button    submitBtn, skipBtn, prevBtn;

    // Timer views (replacing old ProgressBar)
    TextView  timerText;
    View      timerBar;
    View      timerBarTrack;

    // ── State ─────────────────────────────────────────────────
    List<pythonques> questionList = new ArrayList<>();
    int currentIndex = 0;
    int correctCount = 0;
    int skippedCount = 0;
    String topic      = "python";
    String difficulty = "easy";

    // ── Timer ─────────────────────────────────────────────────
    private CountDownTimer countDownTimer;
    private static final int TIMER_SECONDS = 15;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_python);

        topic      = getIntent().getStringExtra("TOPIC");
        difficulty = getIntent().getStringExtra("DIFFICULTY");
        if (topic == null)      topic      = "python";
        if (difficulty == null) difficulty = "easy";
        topic      = topic.toLowerCase();
        difficulty = difficulty.toLowerCase();

        bindViews();
        loadQuestions();

        submitBtn.setOnClickListener(v -> submitAnswer());
        skipBtn.setOnClickListener(v  -> handleSkip());
        prevBtn.setOnClickListener(v  -> handlePrev());
    }

    // ── Bind views ────────────────────────────────────────────
    private void bindViews() {
        qNoText        = findViewById(R.id.qnoText);
        qText          = findViewById(R.id.q1);
        radioGroup     = findViewById(R.id.radioGroup);
        r1             = findViewById(R.id.r1);
        r2             = findViewById(R.id.r2);
        r3             = findViewById(R.id.r3);
        r4             = findViewById(R.id.r4);
        submitBtn      = findViewById(R.id.b);
        skipBtn        = findViewById(R.id.skipBtn);
        prevBtn        = findViewById(R.id.prevBtn);
        timerText      = findViewById(R.id.timerText);
        timerBar       = findViewById(R.id.timerBar);
        timerBarTrack  = findViewById(R.id.timerBarTrack);
    }

    // ── Load questions from JSON ───────────────────────────────
    private void loadQuestions() {
        try {
            String fileName = topic + "_" + difficulty + ".json";
            InputStream is = getAssets().open(fileName);
            byte[] buffer = new byte[is.available()];
            is.read(buffer);
            is.close();
            String json = new String(buffer, StandardCharsets.UTF_8);
            Type type = new TypeToken<ArrayList<pythonques>>(){}.getType();
            questionList = new Gson().fromJson(json, type);
            Collections.shuffle(questionList);
            showQuestion(0);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Questions not found for: " + topic + "_" + difficulty, Toast.LENGTH_LONG).show();
            finish();
        }
    }

    // ── Show question ─────────────────────────────────────────
    private void showQuestion(int index) {
        if (index >= questionList.size()) { finishQuiz(); return; }

        pythonques q = questionList.get(index);
        List<String> options = new ArrayList<>(q.getOptions());
        Collections.shuffle(options);

        // Reset option colors
        r1.setTextColor(0xFFDEDEFF); r2.setTextColor(0xFFDEDEFF);
        r3.setTextColor(0xFFDEDEFF); r4.setTextColor(0xFFDEDEFF);
        r1.setEnabled(true); r2.setEnabled(true);
        r3.setEnabled(true); r4.setEnabled(true);

        radioGroup.clearCheck();

        qNoText.setText("Q " + (index + 1) + " / " + questionList.size()
                + "  •  " + capitalize(difficulty));
        qText.setText(q.getQuestion());
        r1.setText(options.get(0));
        r2.setText(options.get(1));
        r3.setText(options.get(2));
        r4.setText(options.get(3));

        startTimer();
    }

    // ── Timer ─────────────────────────────────────────────────
    private void startTimer() {
        cancelTimer();
        countDownTimer = new CountDownTimer(TIMER_SECONDS * 1000L, 100) {
            @Override public void onTick(long ms) {
                int secs = (int) Math.ceil(ms / 1000.0);
                if (timerText != null) timerText.setText(secs + "s");

                // Shrink bar
                timerBarTrack.post(() -> {
                    int trackW = timerBarTrack.getWidth();
                    float frac = ms / (TIMER_SECONDS * 1000f);
                    android.view.ViewGroup.LayoutParams lp = timerBar.getLayoutParams();
                    lp.width = (int)(trackW * frac);
                    timerBar.setLayoutParams(lp);
                });

                // Color transitions
                int color;
                if (ms > TIMER_SECONDS * 1000L * 0.6f)      color = 0xFF059669; // green
                else if (ms > TIMER_SECONDS * 1000L * 0.3f) color = 0xFFF59E0B; // yellow
                else                                          color = 0xFFEF4444; // red
                if (timerText != null) timerText.setTextColor(color);
                timerBar.setBackgroundColor(color);
            }
            @Override public void onFinish() {
                if (timerText != null) { timerText.setText("0s"); timerText.setTextColor(0xFFEF4444); }
                handleSkip();
            }
        }.start();
    }

    private void cancelTimer() {
        if (countDownTimer != null) { countDownTimer.cancel(); countDownTimer = null; }
    }

    // ── Submit answer ─────────────────────────────────────────
    private void submitAnswer() {
        int id = radioGroup.getCheckedRadioButtonId();
        if (id == -1) { Toast.makeText(this, "Select an answer", Toast.LENGTH_SHORT).show(); return; }

        cancelTimer();
        RadioButton selected = findViewById(id);
        String selectedAns = selected.getText().toString();
        String correctAns  = questionList.get(currentIndex).getAnswer().trim();

        if (selectedAns.equalsIgnoreCase(correctAns)) {
            correctCount++;
            selected.setTextColor(0xFF34D399); // green
        } else {
            selected.setTextColor(0xFFF87171); // red
            // Highlight correct answer
            for (RadioButton rb : new RadioButton[]{r1, r2, r3, r4}) {
                if (rb.getText().toString().equalsIgnoreCase(correctAns)) {
                    rb.setTextColor(0xFF34D399);
                    break;
                }
            }
        }

        // Delay before moving to next
        new android.os.Handler().postDelayed(() -> {
            currentIndex++;
            showQuestion(currentIndex);
        }, 800);
    }

    // ── Prev ──────────────────────────────────────────────────
    private void handlePrev() {
        if (currentIndex > 0) {
            cancelTimer();
            currentIndex--;
            showQuestion(currentIndex);
        }
    }

    // ── Skip ──────────────────────────────────────────────────
    private void handleSkip() {
        cancelTimer();
        skippedCount++;
        currentIndex++;
        showQuestion(currentIndex);
    }

    // ── Finish quiz ───────────────────────────────────────────
    private void finishQuiz() {
        cancelTimer();
        Intent i = new Intent(this, result.class);
        i.putExtra("totalQuestions",  questionList.size());
        i.putExtra("correctAnswers",  correctCount);
        i.putExtra("skippedAnswers",  skippedCount);
        i.putExtra("difficulty",      difficulty);
        i.putExtra("topic",           topic);
        startActivity(i);
        finish();
    }

    private String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }

    @Override
    protected void onDestroy() { super.onDestroy(); cancelTimer(); }
}