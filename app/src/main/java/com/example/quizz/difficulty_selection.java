package com.example.quizz;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

public class difficulty_selection extends AppCompatActivity {

    CardView easyCard, mediumCard, hardCard, backButton;
    String selectedTopic;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.difficulty_selection);

        // Get the topic that was selected (e.g., "Python", "Java", "C", "Cpp", etc.)
        selectedTopic = getIntent().getStringExtra("TOPIC");

        // Initialize views
        easyCard = findViewById(R.id.easyCard);
        mediumCard = findViewById(R.id.mediumCard);
        hardCard = findViewById(R.id.hardCard);
        backButton = findViewById(R.id.backButton);

        // Set click listeners
        easyCard.setOnClickListener(v -> openQuiz("EASY"));
        mediumCard.setOnClickListener(v -> openQuiz("MEDIUM"));
        hardCard.setOnClickListener(v -> openQuiz("HARD"));

        backButton.setOnClickListener(v -> finish());
    }

    private void openQuiz(String difficulty) {
        Intent intent = null;

        // ✅ FIXED: Route to the correct quiz activity based on selected topic
        switch (selectedTopic) {
            case "Python":
                intent = new Intent(difficulty_selection.this, Python.class);
                break;
            case "C":
                intent = new Intent(difficulty_selection.this, C.class);
                break;
            case "Cpp":
                intent = new Intent(difficulty_selection.this, Cpp.class);
                break;
            case "Java":
                intent = new Intent(difficulty_selection.this, java.class);
                break;
            case "Kotlin":
                intent = new Intent(difficulty_selection.this, kotlin.class);
                break;
            case "C#":
                intent = new Intent(difficulty_selection.this, csharp.class);
                break;
            default:
                // Fallback to Python if topic not recognized
                intent = new Intent(difficulty_selection.this, Python.class);
                break;
        }

        if (intent != null) {
            // Pass the selected difficulty and topic to the quiz activity
            intent.putExtra("DIFFICULTY", difficulty);
            intent.putExtra("TOPIC", selectedTopic);
            startActivity(intent);
            finish();
        }
    }
}