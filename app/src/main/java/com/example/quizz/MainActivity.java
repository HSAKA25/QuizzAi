package com.example.quizz;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

public class MainActivity extends AppCompatActivity {

    TextView tv1;
    android.widget.EditText t1, t2;
    Button b1, b2, b4;
    FloatingActionButton cb;
    CheckBox rememberMe;
    CardView googleBtn, appleBtn;
    ProgressBar loadingSpinner;

    FirebaseAuth mAuth;
    GoogleSignInClient googleSignInClient;
    SharedPreferences sharedPreferences;

    private static final int    RC_SIGN_IN   = 9001;
    private static final String PREF_NAME    = "UserPrefs";
    private static final String KEY_EMAIL    = "email";
    private static final String KEY_REMEMBER = "isRemembered";
    private static final String WEB_CLIENT_ID =
            "258703474460-ppmtt49qoe04t7qemqmh0pjn25s07e86.apps.googleusercontent.com";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();

        // Auto-login if already signed in
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) { goToHome(); return; }

        initViews();
        setupGoogleSignIn();
        loadSavedPreferences();
        setupClickListeners();
        setupCheckboxAnimation();
    }

    // ── Init views ─────────────────────────────────────────
    private void initViews() {
        tv1           = findViewById(R.id.tv1);
        t1            = findViewById(R.id.t1);
        t2            = findViewById(R.id.t2);
        b1            = findViewById(R.id.b1);
        b2            = findViewById(R.id.b2);
        b4            = findViewById(R.id.b4);
        cb            = findViewById(R.id.cb);
        rememberMe    = findViewById(R.id.rememberMe);
        googleBtn     = findViewById(R.id.googleBtn);
        appleBtn      = findViewById(R.id.appleBtn);
        loadingSpinner= findViewById(R.id.loadingSpinner);
        sharedPreferences = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
    }

    // ── Google Sign-In setup ───────────────────────────────
    private void setupGoogleSignIn() {
        GoogleSignInOptions gso = new GoogleSignInOptions
                .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(WEB_CLIENT_ID)
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(this, gso);
    }

    // ── Click listeners ────────────────────────────────────
    private void setupClickListeners() {

        // Email login
        b1.setOnClickListener(v -> loginUser());

        // Sign up
        b2.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, page2.class)));

        // Forgot password
        b4.setOnClickListener(v -> {
            String email = t1.getText().toString().trim();
            if (!email.isEmpty()) {
                mAuth.sendPasswordResetEmail(email)
                        .addOnSuccessListener(u ->
                                Toast.makeText(this, "✅ Reset link sent to email",
                                        Toast.LENGTH_SHORT).show())
                        .addOnFailureListener(e ->
                                Toast.makeText(this, "Error: " + e.getMessage(),
                                        Toast.LENGTH_SHORT).show());
            } else {
                t1.setError("Enter your email first");
                t1.requestFocus();
            }
        });

        // Help FAB
        cb.setOnClickListener(v ->
                Toast.makeText(this, "Help & Support coming soon!",
                        Toast.LENGTH_SHORT).show());

        // Google login — press animation then launch picker
        googleBtn.setOnClickListener(v -> {
            // Tactile press animation
            v.animate().scaleX(0.95f).scaleY(0.95f).setDuration(80)
                    .withEndAction(() -> {
                        v.animate().scaleX(1f).scaleY(1f).setDuration(80).start();
                        Intent signInIntent = googleSignInClient.getSignInIntent();
                        startActivityForResult(signInIntent, RC_SIGN_IN);
                    }).start();
        });

        // Apple — not available on Android
        appleBtn.setOnClickListener(v ->
                Toast.makeText(this, "Apple login not available on Android",
                        Toast.LENGTH_SHORT).show());
    }

    // ── Handle Google Sign-In result ───────────────────────
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task =
                    GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                setLoading(false);
                Toast.makeText(this,
                        "Google sign-in failed: " + e.getStatusCode(),
                        Toast.LENGTH_LONG).show();
            }
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnSuccessListener(result -> {
                    Toast.makeText(this, "✅ Signed in with Google!",
                            Toast.LENGTH_SHORT).show();
                    goToHome();
                })
                .addOnFailureListener(e -> {
                    setLoading(false);
                    Toast.makeText(this,
                            "Firebase auth failed: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                });
    }

    // ── Email login ────────────────────────────────────────
    private void loginUser() {
        String email = t1.getText().toString().trim();
        String pass  = t2.getText().toString().trim();

        if (email.isEmpty()) { t1.setError("Email required"); return; }
        if (pass.isEmpty())  { t2.setError("Password required"); return; }

        setLoading(true);
        mAuth.signInWithEmailAndPassword(email, pass)
                .addOnSuccessListener(authResult -> {
                    handleRememberMe(email);
                    goToHome();
                })
                .addOnFailureListener(e -> {
                    setLoading(false);
                    Toast.makeText(this,
                            "Login Failed: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                });
    }

    // ── Checkbox bounce animation ──────────────────────────
    private void setupCheckboxAnimation() {
        rememberMe.setOnCheckedChangeListener((view, isChecked) -> {
            view.animate()
                    .scaleX(isChecked ? 1.25f : 0.9f)
                    .scaleY(isChecked ? 1.25f : 0.9f)
                    .setDuration(120)
                    .withEndAction(() ->
                            view.animate()
                                    .scaleX(1f).scaleY(1f)
                                    .setDuration(100).start()
                    ).start();

            view.setTextColor(isChecked ? 0xFF818CF8 : 0xFF64748B);

            if (isChecked)
                Toast.makeText(this, "✅ Email will be remembered",
                        Toast.LENGTH_SHORT).show();
        });
    }

    // ── Remember Me ────────────────────────────────────────
    private void handleRememberMe(String email) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        if (rememberMe.isChecked()) {
            editor.putString(KEY_EMAIL, email);
            editor.putBoolean(KEY_REMEMBER, true);
        } else {
            editor.remove(KEY_EMAIL);
            editor.remove(KEY_REMEMBER);
        }
        editor.apply();
    }

    private void loadSavedPreferences() {
        if (sharedPreferences.getBoolean(KEY_REMEMBER, false)) {
            t1.setText(sharedPreferences.getString(KEY_EMAIL, ""));
            rememberMe.setChecked(true);
            rememberMe.setTextColor(0xFF818CF8);
        }
    }

    // ── Navigation ─────────────────────────────────────────
    private void goToHome() {
        Intent intent = new Intent(MainActivity.this, page3.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        finish();
    }

    // ── Loading state ──────────────────────────────────────
    private void setLoading(boolean isLoading) {
        if (loadingSpinner != null)
            loadingSpinner.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        b1.setEnabled(!isLoading);
        b1.setAlpha(isLoading ? 0.6f : 1f);
        b1.setText(isLoading ? "" : "Sign In  \u2192");
    }
}