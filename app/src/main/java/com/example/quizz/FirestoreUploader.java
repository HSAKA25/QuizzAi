package com.example.quizz;

import android.content.Context;
import android.util.Log;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * FirestoreUploader
 *
 * ONE-TIME USE UTILITY: Run this ONCE to upload your existing JSON files
 * from assets to Firestore. After uploading, you can delete this class.
 *
 * Your current JSON files in assets:
 *   java_easy.json, java_medium.json, java_hard.json
 *   kotlin_easy.json, kotlin_medium.json, kotlin_hard.json
 *   c_easy.json, c_medium.json, c_hard.json
 *   cpp_easy.json, cpp_medium.json, cpp_hard.json
 *   python_easy.json, python_medium.json, python_hard.json
 *   csharp_easy.json, csharp_medium.json, csharp_hard.json
 *   pythonmini.json  (will be uploaded under python/mini)
 *
 * HOW TO USE:
 *   1. In MainActivity or any Activity, call:
 *        new FirestoreUploader(this).uploadAll();
 *   2. Check Logcat for "UPLOAD" tag to see progress
 *   3. Verify in Firebase Console that data is uploaded
 *   4. Remove the uploadAll() call and delete this file
 *
 * EXPECTED JSON FORMAT in your assets files:
 *   [
 *     {
 *       "question": "What is...?",
 *       "optionA": "...",
 *       "optionB": "...",
 *       "optionC": "...",
 *       "optionD": "...",
 *       "answer": "A",
 *       "explanation": "..." // optional
 *     },
 *     ...
 *   ]
 *
 * If your JSON uses different field names, update parseQuestion() below.
 */
public class FirestoreUploader {

    private static final String TAG = "UPLOAD";
    private final Context context;
    private final FirebaseFirestore db;

    // Map: assetFileName -> [topic, difficulty]
    // Update this map to match your actual file names
    private static final String[][] FILE_MAPPINGS = {
            // {filename_without_extension, topic, difficulty}
            {"java_easy",    "java",    "easy"},
            {"java_medium",  "java",    "medium"},
            {"java_hard",    "java",    "hard"},
            {"kotlin_easy",  "kotlin",  "easy"},
            {"kotlin_medium","kotlin",  "medium"},
            {"kotlin_hard",  "kotlin",  "hard"},
            {"c_easy",       "c",       "easy"},
            {"c_medium",     "c",       "medium"},
            {"c_hard",       "c",       "hard"},
            {"cpp_easy",     "cpp",     "easy"},
            {"cpp_medium",   "cpp",     "medium"},
            {"cpp_hard",     "cpp",     "hard"},
            {"python_easy",  "python",  "easy"},
            {"python_medium","python",  "medium"},
            {"python_hard",  "python",  "hard"},
            {"csharp_easy",  "csharp",  "easy"},
            {"csharp_medium","csharp",  "medium"},
            {"csharp_hard",  "csharp",  "hard"},
            {"pythonmini",   "python",  "mini"},  // your extra file
    };

    public FirestoreUploader(Context context) {
        this.context = context;
        this.db = FirebaseFirestore.getInstance();
    }

    /**
     * Upload all JSON files to Firestore.
     * Call this once from MainActivity or a debug button.
     */
    public void uploadAll() {
        Log.d(TAG, "Starting upload of " + FILE_MAPPINGS.length + " files...");
        for (String[] mapping : FILE_MAPPINGS) {
            String fileName = mapping[0];
            String topic = mapping[1];
            String difficulty = mapping[2];
            uploadFile(fileName, topic, difficulty);
        }
    }

    private void uploadFile(String assetFileName, String topic, String difficulty) {
        new Thread(() -> {
            try {
                // Read JSON from assets
                InputStream is = context.getAssets().open(assetFileName + ".json");
                byte[] buffer = new byte[is.available()];
                is.read(buffer);
                is.close();
                String jsonStr = new String(buffer, StandardCharsets.UTF_8);

                JSONArray jsonArray = new JSONArray(jsonStr);
                Log.d(TAG, "Uploading " + jsonArray.length() + " questions → " + topic + "/" + difficulty);

                // Upload in batches of 500 (Firestore limit)
                int batchSize = 500;
                for (int start = 0; start < jsonArray.length(); start += batchSize) {
                    WriteBatch batch = db.batch();
                    int end = Math.min(start + batchSize, jsonArray.length());

                    for (int i = start; i < end; i++) {
                        JSONObject json = jsonArray.getJSONObject(i);
                        Map<String, Object> data = parseQuestion(json);

                        // Path: questions/{topic}/{difficulty}/{auto_id}
                        db.collection("questions")
                                .document(topic)
                                .collection(difficulty)
                                .add(data); // Use add() for auto-generated IDs
                    }

                    // Note: batch.commit() not needed since we use .add() directly
                    // If you want atomic writes, use batch.set() with batch.commit()
                    Log.d(TAG, "✓ Uploaded " + topic + "/" + difficulty +
                            " (" + (end - start) + " questions)");
                }

            } catch (Exception e) {
                Log.e(TAG, "✗ Failed to upload " + assetFileName + ": " + e.getMessage(), e);
            }
        }).start();
    }

    /**
     * Parse a JSON question object into a Firestore map.
     *
     * UPDATE THIS METHOD if your JSON uses different field names.
     * Common alternatives: "option1"/"option2", "opt_a"/"opt_b", "choices", etc.
     */
    private Map<String, Object> parseQuestion(JSONObject json) throws Exception {
        Map<String, Object> data = new HashMap<>();

        // Try multiple field name formats for flexibility
        data.put("question",    getField(json, "question", "ques", "q"));
        data.put("optionA",     getField(json, "optionA", "option1", "opt_a", "a"));
        data.put("optionB",     getField(json, "optionB", "option2", "opt_b", "b"));
        data.put("optionC",     getField(json, "optionC", "option3", "opt_c", "c"));
        data.put("optionD",     getField(json, "optionD", "option4", "opt_d", "d"));
        data.put("answer",      getField(json, "answer", "correct", "ans"));

        // Optional explanation field
        if (json.has("explanation") || json.has("explain")) {
            data.put("explanation", getField(json, "explanation", "explain", "desc"));
        }

        return data;
    }

    /** Try multiple field names, return first match */
    private String getField(JSONObject json, String... keys) throws Exception {
        for (String key : keys) {
            if (json.has(key) && !json.isNull(key)) {
                return json.getString(key);
            }
        }
        return ""; // Return empty string if no field found
    }
}