package com.example.quizz;

import android.os.Handler;
import android.os.Looper;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import org.json.JSONArray;
import org.json.JSONObject;

public class DialogflowClient {

    // 🔹 Dialogflow REST endpoint (ES – v2)
    private static final String DIALOGFLOW_URL =
            "https://dialogflow.googleapis.com/v2/projects/YOUR_PROJECT_ID/agent/sessions/123456:detectIntent";

    // 🔹 Replace with OAuth token (TEMPORARY for testing)
    private static final String ACCESS_TOKEN = "Bearer YOUR_ACCESS_TOKEN";

    private static final MediaType JSON
            = MediaType.get("application/json; charset=utf-8");

    private static final OkHttpClient client = new OkHttpClient();

    // 🔹 Callback interface
    public interface BotCallback {
        void onReply(String reply);
    }

    // 🔹 Send message to Dialogflow
    public static void sendMessage(String userText, BotCallback callback) {

        try {
            // 🔹 Build JSON request
            JSONObject root = new JSONObject();
            JSONObject queryInput = new JSONObject();
            JSONObject text = new JSONObject();

            text.put("text", userText);
            text.put("languageCode", "en");

            queryInput.put("text", text);
            root.put("queryInput", queryInput);

            RequestBody body = RequestBody.create(root.toString(), JSON);

            Request request = new Request.Builder()
                    .url(DIALOGFLOW_URL)
                    .addHeader("Authorization", ACCESS_TOKEN)
                    .post(body)
                    .build();

            // 🔹 Network call
            client.newCall(request).enqueue(new Callback() {

                @Override
                public void onFailure(Call call, IOException e) {
                    new Handler(Looper.getMainLooper()).post(() ->
                            callback.onReply("⚠️ Network error. Please try again.")
                    );
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {

                    if (!response.isSuccessful()) {
                        new Handler(Looper.getMainLooper()).post(() ->
                                callback.onReply("⚠️ Bot error occurred.")
                        );
                        return;
                    }

                    try {
                        String res = response.body().string();
                        JSONObject json = new JSONObject(res);

                        JSONArray responses =
                                json.getJSONObject("queryResult")
                                        .getJSONArray("fulfillmentMessages");

                        String reply = responses
                                .getJSONObject(0)
                                .getJSONObject("text")
                                .getJSONArray("text")
                                .getString(0);

                        new Handler(Looper.getMainLooper()).post(() ->
                                callback.onReply(reply)
                        );

                    } catch (Exception e) {
                        new Handler(Looper.getMainLooper()).post(() ->
                                callback.onReply("🤖 I didn't understand that.")
                        );
                    }
                }
            });

        } catch (Exception e) {
            callback.onReply("⚠️ Error processing request.");
        }
    }
}
