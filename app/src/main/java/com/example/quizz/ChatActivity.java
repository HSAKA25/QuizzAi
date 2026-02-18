package com.example.quizz;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class ChatActivity extends AppCompatActivity {

    ListView chatList;
    EditText inputMsg;
    Button sendBtn;
    LinearLayout typingIndicator;

    ArrayList<String> messages;
    ArrayAdapter<String> adapter;

    // ✅ Typing delay in milliseconds — adjust to taste
    // 800ms = feels like bot is "thinking"
    private static final int REPLY_DELAY_MS = 1200;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        chatList        = findViewById(R.id.chatList);
        inputMsg        = findViewById(R.id.inputMsg);
        sendBtn         = findViewById(R.id.sendBtn);
        typingIndicator = findViewById(R.id.typingIndicator);

        messages = new ArrayList<>();
        adapter  = new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_1,
                messages
        );
        chatList.setAdapter(adapter);

        // Welcome message
        messages.add("🤖 Bot: Hi! I'm your Quiz Assistant 🎯 Ask me about Java, Python, Math, Science or Quiz Rules!");
        adapter.notifyDataSetChanged();

        sendBtn.setOnClickListener(v -> sendMessage());

        inputMsg.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEND ||
                    (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                sendMessage();
                return true;
            }
            return false;
        });
    }

    private void sendMessage() {
        String text = inputMsg.getText().toString().trim();
        if (text.isEmpty()) return;

        // Show user message instantly
        messages.add("🧑 You: " + text);
        adapter.notifyDataSetChanged();
        scrollToBottom();
        inputMsg.setText("");

        // Show typing indicator immediately
        typingIndicator.setVisibility(android.view.View.VISIBLE);

        // ✅ Delay the bot reply to feel natural
        new Handler(Looper.getMainLooper()).postDelayed(() -> {

            // Get reply from keyword bot
            String response = ChatBot.reply(getApplicationContext(), text);

            // Hide typing indicator and show reply
            typingIndicator.setVisibility(android.view.View.GONE);
            messages.add("🤖 Bot: " + response);
            adapter.notifyDataSetChanged();
            scrollToBottom();

        }, REPLY_DELAY_MS);
    }

    private void scrollToBottom() {
        chatList.post(() ->
                chatList.setSelection(adapter.getCount() - 1)
        );
    }
}