package com.example.quizz;

import android.util.Log;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * FirestoreQuestionLoader
 *
 * Replaces asset JSON loading with Firestore.
 *
 * FIRESTORE STRUCTURE:
 * questions/
 *   java/
 *     easy/      <-- collection
 *       doc1: { question, optionA, optionB, optionC, optionD, answer, explanation }
 *       doc2: { ... }
 *     medium/
 *       ...
 *     hard/
 *       ...
 *   kotlin/
 *     easy/ ...
 *   c/
 *     easy/ ...
 *   cpp/
 *     easy/ ...
 *   python/
 *     easy/ ...
 *   csharp/
 *     easy/ ...
 */
public class FirestoreQuestionLoader {

    private static final String TAG = "FirestoreQuestionLoader";
    private static final String ROOT_COLLECTION = "questions";

    private final FirebaseFirestore db;

    // Callback interface - replaces your old JSON loading callback
    public interface QuestionLoadCallback {
        void onQuestionsLoaded(List<Question> questions);
        void onError(String errorMessage);
    }

    public FirestoreQuestionLoader() {
        db = FirebaseFirestore.getInstance();

        // Online-only mode (no offline caching)
        // Internet connection required to load questions
    }

    /**
     * Load questions from Firestore.
     *
     * @param topic     e.g. "java", "kotlin", "c", "cpp", "python", "csharp"
     * @param difficulty e.g. "easy", "medium", "hard"
     * @param limit     max number of questions to load (0 = all)
     * @param shuffle   whether to shuffle the questions
     * @param callback  result callback
     *
     * USAGE:
     *   loader.loadQuestions("java", "easy", 10, true, new QuestionLoadCallback() { ... });
     */
    public void loadQuestions(String topic, String difficulty,
                              int limit, boolean shuffle,
                              QuestionLoadCallback callback) {

        // Path: questions/{topic}/{difficulty}  (subcollection)
        CollectionReference ref = db
                .collection(ROOT_COLLECTION)
                .document(topic.toLowerCase())
                .collection(difficulty.toLowerCase());

        Query query = (limit > 0) ? ref.limit(limit) : ref;

        query.get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Question> questions = new ArrayList<>();

                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        try {
                            Question q = doc.toObject(Question.class);
                            if (q != null && q.getQuestion() != null) {
                                questions.add(q);
                            }
                        } catch (Exception e) {
                            Log.w(TAG, "Skipping malformed document: " + doc.getId(), e);
                        }
                    }

                    if (questions.isEmpty()) {
                        callback.onError("No questions found for " + topic + "/" + difficulty);
                        return;
                    }

                    if (shuffle) {
                        Collections.shuffle(questions);
                    }

                    Log.d(TAG, "Loaded " + questions.size() + " questions for " + topic + "/" + difficulty);
                    callback.onQuestionsLoaded(questions);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to load questions", e);
                    callback.onError("Failed to load questions: " + e.getMessage());
                });
    }

    /**
     * Convenience method - load with default settings (shuffle, no limit)
     */
    public void loadQuestions(String topic, String difficulty, QuestionLoadCallback callback) {
        loadQuestions(topic, difficulty, 0, true, callback);
    }

    /**
     * Get the Firestore path for a topic/difficulty (useful for debugging)
     */
    public static String getPath(String topic, String difficulty) {
        return ROOT_COLLECTION + "/" + topic + "/" + difficulty;
    }
}