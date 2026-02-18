package com.example.quizz;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.OvershootInterpolator;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.Insets;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;

import java.util.List;

public class page3 extends AppCompatActivity {

    // ── Main screen views ──────────────────────────────────
    DrawerLayout         drawerLayout;
    GridView             gridView;
    FloatingActionButton chatBtn;
    ImageView            avatarImage;
    TextView             greetingText;
    FrameLayout          avatarContainer;
    View                 menuBtn;

    // ── Drawer views ───────────────────────────────────────
    ImageView  drawerAvatar;
    TextView   drawerName, drawerEmail, drawerBadgeText;
    TextView   drawerQuizCount, drawerBadgeCount;
    CardView   drawerBadge;
    LinearLayout drawerHistory, drawerBadges, drawerRanks, drawerAnalytics, drawerContact, drawerLogout;
    ImageView   drawerProviderIcon;
    // Reminder toggle
    androidx.appcompat.widget.SwitchCompat reminderSwitch;
    androidx.appcompat.widget.SwitchCompat quoteSwitch;

    String[] languages = {"Python", "C", "Cpp", "Java", "Kotlin", "C#"};
    String[] icons     = {"🐍", "🔧", "⚙️", "☕", "🤖", "💠"};
    String[] subtitles = {"10 Questions", "10 Questions", "10 Questions",
            "10 Questions", "10 Questions", "10 Questions"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_page3);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets sys = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(sys.left, sys.top, sys.right, sys.bottom);
            return insets;
        });

        bindViews();
        requestNotificationPermission();
        setupUserAvatar();
        setupDrawer();

        gridView.setAdapter(new LanguageAdapter());
        gridView.setOnItemClickListener((parent, view, position, id) ->
                openDifficultySelection(languages[position]));

        chatBtn.setOnClickListener(v ->
                startActivity(new Intent(this, ChatActivity.class)));

        // Avatar tap → open drawer
        avatarContainer.setOnClickListener(v ->
                drawerLayout.openDrawer(GravityCompat.START));

        // Hamburger → open drawer
        menuBtn.setOnClickListener(v ->
                drawerLayout.openDrawer(GravityCompat.START));
    }

    // ── Bind all views ─────────────────────────────────────
    private void bindViews() {
        drawerLayout      = findViewById(R.id.drawerLayout);
        gridView          = findViewById(R.id.gv);
        chatBtn           = findViewById(R.id.cb);
        avatarImage       = findViewById(R.id.avatarImage);
        greetingText      = findViewById(R.id.greetingText);
        avatarContainer   = findViewById(R.id.avatarContainer);
        menuBtn           = findViewById(R.id.menuBtn);

        // Drawer views
        drawerAvatar     = findViewById(R.id.drawerAvatar);
        drawerName       = findViewById(R.id.drawerName);
        drawerEmail      = findViewById(R.id.drawerEmail);
        drawerBadgeText  = findViewById(R.id.drawerBadgeText);
        drawerBadge      = findViewById(R.id.drawerBadge);
        drawerQuizCount  = findViewById(R.id.drawerQuizCount);
        drawerBadgeCount = findViewById(R.id.drawerBadgeCount);
        drawerAnalytics  = findViewById(R.id.drawerAnalytics);
        drawerProviderIcon = findViewById(R.id.drawerProviderIcon);
        drawerHistory    = findViewById(R.id.drawerHistory);
        drawerBadges     = findViewById(R.id.drawerBadges);
        drawerRanks      = findViewById(R.id.drawerRanks);
        drawerContact    = findViewById(R.id.drawerContact);
        reminderSwitch   = findViewById(R.id.reminderSwitch);
        quoteSwitch      = findViewById(R.id.quoteSwitch);
        drawerLogout     = findViewById(R.id.drawerLogout);
    }

    // ── Setup drawer content + click listeners ─────────────
    private void setupDrawer() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        String displayName = user.getDisplayName();
        String email       = user.getEmail();
        final boolean isGoogle = isGoogleUser(user);

        // Name + email
        drawerName.setText((displayName != null && !displayName.isEmpty())
                ? displayName : (email != null ? email.split("@")[0] : "User"));
        drawerEmail.setText(email != null ? email : "");

        // Badge text + color
        drawerBadgeText.setText(isGoogle ? "Google Account" : "Email Login");
        drawerBadgeText.setTextColor(isGoogle ? 0xFF4285F4 : 0xFF818CF8);

        // Small provider icon next to badge
        drawerProviderIcon.setImageBitmap(
                isGoogle ? makeGoogleProviderIcon() : makeEmailProviderIcon());

        // ── LARGE AVATAR: Google image OR guest icon ─────────
        if (isGoogle) {
            Glide.with(this)
                    .load(R.drawable.google_img)
                    .transform(new CircleCrop())
                    .into(drawerAvatar);
        } else {
            drawerAvatar.setImageBitmap(makeGuestIcon());
        }

        // ── Stats from QuizHistoryManager ──
        List<QuizHistoryManager.QuizResult> history = QuizHistoryManager.getAll(this);
        int quizCount  = history != null ? history.size() : 0;
        int badgeCount = BadgeManager.getEarnedCount(this);
        drawerQuizCount.setText(String.valueOf(quizCount));
        drawerBadgeCount.setText(String.valueOf(badgeCount));

        // ── Quick link clicks ──
        drawerHistory.setOnClickListener(v -> {
            drawerLayout.closeDrawer(GravityCompat.START);
            startActivity(new Intent(this, Historyactivity.class));
        });
        drawerBadges.setOnClickListener(v -> {
            drawerLayout.closeDrawer(GravityCompat.START);
            startActivity(new Intent(this, Achievementsactivity.class));
        });
        drawerRanks.setOnClickListener(v -> {
            drawerLayout.closeDrawer(GravityCompat.START);
            startActivity(new Intent(this, LeaderboardActivity.class));
        });
        drawerAnalytics.setOnClickListener(v -> {
            drawerLayout.closeDrawer(GravityCompat.START);
            startActivity(new Intent(this, AnalyticsActivity.class));
        });
        drawerContact.setOnClickListener(v -> {
            drawerLayout.closeDrawer(GravityCompat.START);
            // Opens email client
            Intent email2 = new Intent(Intent.ACTION_SENDTO);
            email2.setData(Uri.parse("mailto:as524610@gmail.com"));
            email2.putExtra(Intent.EXTRA_SUBJECT, "QuizzAI Support");
            startActivity(Intent.createChooser(email2, "Contact Us"));
        });

        // ── Reminder Toggle ────────────────────────────────
        NotificationHelper.createChannel(page3.this);
        NotificationHelper.scheduleDailyQuote(page3.this); // quote at 9 AM daily
        SharedPreferences prefs =
                getSharedPreferences("quizzai_prefs", MODE_PRIVATE);
        boolean reminderOn = prefs.getBoolean("reminder_enabled", false);
        // Show saved reminder time in subtitle
        if (reminderOn) {
            int h = prefs.getInt("reminder_hour", 20);
            int m = prefs.getInt("reminder_minute", 0);
            updateReminderSubtitle(h, m);
        }

        if (reminderSwitch != null) {
            reminderSwitch.setChecked(reminderOn);
            reminderSwitch.setOnCheckedChangeListener((btn, isChecked) -> {
                if (isChecked) {
                    // Show time picker — user sets their own time
                    int savedHour   = prefs.getInt("reminder_hour",   20);
                    int savedMinute = prefs.getInt("reminder_minute",  0);
                    new android.app.TimePickerDialog(page3.this,
                            (view, hour, minute) -> {
                                prefs.edit()
                                        .putBoolean("reminder_enabled", true)
                                        .putInt("reminder_hour",   hour)
                                        .putInt("reminder_minute", minute)
                                        .apply();
                                NotificationHelper.scheduleDailyReminder(page3.this, hour, minute);
                                String amPm  = hour >= 12 ? "PM" : "AM";
                                int   h12    = hour % 12 == 0 ? 12 : hour % 12;
                                String timeStr = String.format("%d:%02d %s", h12, minute, amPm);
                                Toast.makeText(page3.this,
                                        "✅ Reminder set for " + timeStr + " daily",
                                        Toast.LENGTH_SHORT).show();
                                // Update subtitle in drawer
                                updateReminderSubtitle(hour, minute);
                            },
                            savedHour, savedMinute, false
                    ).show();
                } else {
                    prefs.edit().putBoolean("reminder_enabled", false).apply();
                    NotificationHelper.cancelReminder(page3.this);
                    Toast.makeText(page3.this,
                            "Reminder cancelled", Toast.LENGTH_SHORT).show();
                    updateReminderSubtitle(-1, -1);
                }
            });
        }

        // ── Quote Switch ───────────────────────────────────
        boolean quoteOn = prefs.getBoolean("quote_enabled", false);
        if (quoteSwitch != null) {
            quoteSwitch.setChecked(quoteOn);
            if (quoteOn) {
                int qh = prefs.getInt("quote_hour",   9);
                int qm = prefs.getInt("quote_minute", 0);
                updateQuoteSubtitle(qh, qm);
            }
            quoteSwitch.setOnCheckedChangeListener((btn, isChecked) -> {
                if (isChecked) {
                    int savedH = prefs.getInt("quote_hour",   9);
                    int savedM = prefs.getInt("quote_minute", 0);
                    new android.app.TimePickerDialog(page3.this,
                            (view, hour, minute) -> {
                                prefs.edit()
                                        .putBoolean("quote_enabled", true)
                                        .putInt("quote_hour",   hour)
                                        .putInt("quote_minute", minute)
                                        .apply();
                                NotificationHelper.scheduleDailyQuote(page3.this, hour, minute);
                                String amPm  = hour >= 12 ? "PM" : "AM";
                                int h12      = hour % 12 == 0 ? 12 : hour % 12;
                                String time  = String.format("%d:%02d %s", h12, minute, amPm);
                                Toast.makeText(page3.this,
                                        "💬 Daily quote set for " + time,
                                        Toast.LENGTH_SHORT).show();
                                updateQuoteSubtitle(hour, minute);
                            },
                            savedH, savedM, false
                    ).show();
                } else {
                    prefs.edit().putBoolean("quote_enabled", false).apply();
                    NotificationHelper.cancelDailyQuote(page3.this);
                    Toast.makeText(page3.this,
                            "Quote notification cancelled", Toast.LENGTH_SHORT).show();
                    updateQuoteSubtitle(-1, -1);
                }
            });
        }

        drawerLogout.setOnClickListener(v -> {
            drawerLayout.closeDrawer(GravityCompat.START);
            performLogout();
        });
    }

    private void updateReminderSubtitle(int hour, int minute) {
        View drawerView = drawerLayout.findViewById(R.id.reminderSubtitle);
        if (drawerView instanceof TextView) {
            if (hour < 0) {
                ((TextView) drawerView).setText("Tap to set time");
            } else {
                String amPm = hour >= 12 ? "PM" : "AM";
                int h12     = hour % 12 == 0 ? 12 : hour % 12;
                ((TextView) drawerView).setText(
                        String.format("%d:%02d %s every day", h12, minute, amPm));
            }
        }
    }

    private void updateQuoteSubtitle(int hour, int minute) {
        View v = drawerLayout.findViewById(R.id.quoteSubtitle);
        if (v instanceof TextView) {
            if (hour < 0) {
                ((TextView) v).setText("Tap to set time");
            } else {
                String amPm = hour >= 12 ? "PM" : "AM";
                int h12     = hour % 12 == 0 ? 12 : hour % 12;
                ((TextView) v).setText(
                        String.format("%d:%02d %s every day", h12, minute, amPm));
            }
        }
    }

    // ── Request POST_NOTIFICATIONS on Android 13+ ──────────────
    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this,
                    android.Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, 101);
            }
        }
    }

    private void performLogout() {
        FirebaseAuth.getInstance().signOut();
        startActivity(new Intent(this, MainActivity.class)
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
        finish();
    }

    // ── Load avatar into any ImageView ────────────────────
    //    Google login  → real Google profile photo
    //    Email login   → guest icon (person silhouette)
    private void loadAvatarInto(FirebaseUser user, ImageView target, int sizeDp) {
        boolean isGoogle = isGoogleUser(user);

        if (isGoogle) {
            // Collect photo URL — try user object first, then provider data
            String photoUrl = null;
            if (user.getPhotoUrl() != null) {
                photoUrl = user.getPhotoUrl().toString()
                        .replace("s96-c", "s400-c")
                        .replace("=s96", "=s400");
            }
            if (photoUrl == null) {
                for (UserInfo profile : user.getProviderData()) {
                    if ("google.com".equals(profile.getProviderId())
                            && profile.getPhotoUrl() != null) {
                        photoUrl = profile.getPhotoUrl().toString()
                                .replace("s96-c", "s400-c");
                        break;
                    }
                }
            }

            android.graphics.drawable.BitmapDrawable placeholder =
                    new android.graphics.drawable.BitmapDrawable(
                            getResources(), makeGooglePlaceholder());

            if (photoUrl != null) {
                Glide.with(this)
                        .load(photoUrl)
                        .transform(new CircleCrop())
                        .placeholder(placeholder)
                        .error(placeholder)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .into(target);
            } else {
                // No URL yet — force reload Firebase profile then retry
                user.reload().addOnCompleteListener(task -> {
                    FirebaseUser refreshed = FirebaseAuth.getInstance().getCurrentUser();
                    if (refreshed != null && refreshed.getPhotoUrl() != null) {
                        String url = refreshed.getPhotoUrl().toString()
                                .replace("s96-c", "s400-c");
                        Glide.with(this)
                                .load(url)
                                .transform(new CircleCrop())
                                .placeholder(placeholder)
                                .error(placeholder)
                                .into(target);
                    } else {
                        target.setImageBitmap(makeGooglePlaceholder());
                    }
                });
            }

        } else {
            // Email/password login → show guest icon
            target.setImageBitmap(makeGuestIcon());
        }
    }

    // ── Top bar avatar + greeting ──────────────────────────
    private void setupUserAvatar() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        // ── DEBUG: log all providers so we can see what Firebase reports ──
        StringBuilder providers = new StringBuilder();
        for (UserInfo p : user.getProviderData()) {
            providers.append(p.getProviderId()).append(" | ");
        }





        // Declare FIRST before using in lambda
        boolean isGoogle   = isGoogleUser(user);
        String displayName = user.getDisplayName();
        String email       = user.getEmail();

        // Top bar avatar: Google image or guest icon
        if (isGoogle) {
            Glide.with(this)
                    .load(R.drawable.google_img)
                    .transform(new CircleCrop())
                    .into(avatarImage);
        } else {
            avatarImage.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            avatarImage.setImageBitmap(makeGuestIcon());
        }

        // Greeting
        if (isGoogle && displayName != null && !displayName.isEmpty()) {
            greetingText.setText("Hey, " + displayName.split(" ")[0] + " 👋");
        } else if (email != null && !email.isEmpty()) {
            String prefix = email.split("@")[0];
            String name   = prefix.substring(0, 1).toUpperCase()
                    + (prefix.length() > 1 ? prefix.substring(1) : "");
            greetingText.setText("Hey, " + name + " 👋");
        } else {
            greetingText.setText("Hey there 👋");
        }

        // Pop-in animation
        avatarContainer.setScaleX(0f);
        avatarContainer.setScaleY(0f);
        avatarContainer.animate()
                .scaleX(1f).scaleY(1f)
                .setDuration(450).setStartDelay(200)
                .setInterpolator(new OvershootInterpolator(2f))
                .start();
    }

    // ── Large email icon for drawer avatar ────────────────
    private Bitmap makeEmailBigIcon() {
        int size = 160, h = size / 2;
        Bitmap bmp = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bmp);
        // Indigo background
        Paint bg = new Paint(Paint.ANTI_ALIAS_FLAG);
        bg.setColor(Color.parseColor("#4338CA"));
        canvas.drawCircle(h, h, h, bg);
        // Envelope body (white)
        Paint env = new Paint(Paint.ANTI_ALIAS_FLAG);
        env.setColor(Color.WHITE);
        android.graphics.RectF rect = new android.graphics.RectF(28, 52, size - 28, size - 48);
        canvas.drawRoundRect(rect, 8, 8, env);
        // Flap V shape
        Paint flap = new Paint(Paint.ANTI_ALIAS_FLAG);
        flap.setColor(Color.parseColor("#6366F1"));
        Path triangle = new Path();
        triangle.moveTo(28, 52);
        triangle.lineTo(h, 90);
        triangle.lineTo(size - 28, 52);
        triangle.close();
        canvas.drawPath(triangle, flap);
        // Clip to circle
        Bitmap result = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Canvas out = new Canvas(result);
        Paint clip = new Paint(Paint.ANTI_ALIAS_FLAG);
        clip.setColor(Color.WHITE);
        out.drawCircle(h, h, h, clip);
        clip.setXfermode(new android.graphics.PorterDuffXfermode(
                android.graphics.PorterDuff.Mode.SRC_IN));
        out.drawBitmap(bmp, 0, 0, clip);
        return result;
    }

    // ── Big Google G icon for avatar slots (top bar + drawer) ──
    private Bitmap makeGoogleBigIcon() {
        int size = 160, h = size / 2;
        Bitmap bmp = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bmp);
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);

        // White circle background
        p.setColor(Color.WHITE);
        canvas.drawCircle(h, h, h, p);

        // 4 color quadrants clipped to circle
        p.setColor(Color.parseColor("#4285F4")); canvas.drawRect(0, 0, h, h, p);      // top-left blue
        p.setColor(Color.parseColor("#EA4335")); canvas.drawRect(h, 0, size, h, p);   // top-right red
        p.setColor(Color.parseColor("#34A853")); canvas.drawRect(0, h, h, size, p);   // bottom-left green
        p.setColor(Color.parseColor("#FBBC05")); canvas.drawRect(h, h, size, size, p);// bottom-right yellow

        // White center donut hole
        p.setColor(Color.WHITE);
        canvas.drawCircle(h, h, h * 0.40f, p);

        // White notch on right side to form the G bar
        p.setColor(Color.WHITE);
        canvas.drawRect(h, h - 10f, h + 46f, h + 10f, p);

        // Clip entire thing to a circle shape
        Bitmap result = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Canvas out = new Canvas(result);
        Paint clip = new Paint(Paint.ANTI_ALIAS_FLAG);
        clip.setColor(Color.WHITE);
        out.drawCircle(h, h, h, clip);
        clip.setXfermode(new android.graphics.PorterDuffXfermode(
                android.graphics.PorterDuff.Mode.SRC_IN));
        out.drawBitmap(bmp, 0, 0, clip);
        return result;
    }

    // ── Draw guest icon: dark circle + white person silhouette ──
    private Bitmap makeGuestIcon() {
        int size = 160, h = size / 2;
        Bitmap bmp = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bmp);

        // Dark indigo background circle
        Paint bg = new Paint(Paint.ANTI_ALIAS_FLAG);
        bg.setColor(Color.parseColor("#1E1B4B"));
        canvas.drawCircle(h, h, h, bg);

        // Subtle ring
        Paint ring = new Paint(Paint.ANTI_ALIAS_FLAG);
        ring.setStyle(Paint.Style.STROKE);
        ring.setColor(Color.parseColor("#3730A3"));
        ring.setStrokeWidth(5f);
        canvas.drawCircle(h, h, h - 4, ring);

        Paint person = new Paint(Paint.ANTI_ALIAS_FLAG);
        person.setColor(Color.parseColor("#818CF8"));
        person.setStyle(Paint.Style.FILL);

        // Head circle
        canvas.drawCircle(h, 52f, 22f, person);

        // Body (trapezoid shape - shoulders)
        Path body = new Path();
        body.moveTo(h - 38f, size - 18f);   // bottom-left
        body.lineTo(h + 38f, size - 18f);   // bottom-right
        body.lineTo(h + 28f, 88f);           // shoulder-right
        body.cubicTo(h + 20f, 78f, h - 20f, 78f, h - 28f, 88f); // neck curve
        body.close();
        canvas.drawPath(body, person);

        // Clip everything to circle
        Bitmap result = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Canvas out = new Canvas(result);
        Paint clip = new Paint(Paint.ANTI_ALIAS_FLAG);
        clip.setColor(Color.WHITE);
        out.drawCircle(h, h, h, clip);
        clip.setXfermode(new android.graphics.PorterDuffXfermode(
                android.graphics.PorterDuff.Mode.SRC_IN));
        out.drawBitmap(bmp, 0, 0, clip);
        return result;
    }

    // ── Draw letter avatar ─────────────────────────────────
    private Bitmap makeLetterAvatar(String letter, String hexColor) {
        int size = 160;
        Bitmap bmp = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bmp);
        Paint bg = new Paint(Paint.ANTI_ALIAS_FLAG);
        bg.setColor(Color.parseColor(hexColor));
        canvas.drawCircle(size / 2f, size / 2f, size / 2f, bg);
        Paint ring = new Paint(Paint.ANTI_ALIAS_FLAG);
        ring.setStyle(Paint.Style.STROKE);
        ring.setColor(Color.parseColor("#818CF8"));
        ring.setStrokeWidth(4f);
        ring.setAlpha(80);
        canvas.drawCircle(size / 2f, size / 2f, size / 2f - 6, ring);
        Paint txt = new Paint(Paint.ANTI_ALIAS_FLAG);
        txt.setColor(Color.WHITE);
        txt.setTextSize(68f);
        txt.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        txt.setTextAlign(Paint.Align.CENTER);
        Rect bounds = new Rect();
        txt.getTextBounds(letter, 0, letter.length(), bounds);
        canvas.drawText(letter, size / 2f, size / 2f + bounds.height() / 2f, txt);
        return bmp;
    }

    // ── Draw small Google colored G icon (for badge row) ──
    private Bitmap makeGoogleProviderIcon() {
        int size = 80, h = size / 2;
        Bitmap bmp = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bmp);
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        // White circle bg
        p.setColor(Color.WHITE); canvas.drawCircle(h, h, h, p);
        // 4 quadrants
        p.setColor(Color.parseColor("#4285F4")); canvas.drawRect(0, 0, h, h, p);
        p.setColor(Color.parseColor("#EA4335")); canvas.drawRect(h, 0, size, h, p);
        p.setColor(Color.parseColor("#34A853")); canvas.drawRect(0, h, h, size, p);
        p.setColor(Color.parseColor("#FBBC05")); canvas.drawRect(h, h, size, size, p);
        // White center circle
        p.setColor(Color.WHITE); canvas.drawCircle(h, h, h * 0.42f, p);
        // Clip to circle
        Bitmap result = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Canvas out = new Canvas(result);
        Paint clip = new Paint(Paint.ANTI_ALIAS_FLAG);
        clip.setColor(Color.WHITE);
        out.drawCircle(h, h, h, clip);
        clip.setXfermode(new android.graphics.PorterDuffXfermode(android.graphics.PorterDuff.Mode.SRC_IN));
        out.drawBitmap(bmp, 0, 0, clip);
        return result;
    }

    // ── Draw email envelope icon (indigo) ──────────────────
    private Bitmap makeEmailProviderIcon() {
        int size = 80, h = size / 2;
        Bitmap bmp = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bmp);
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        // Indigo circle bg
        p.setColor(Color.parseColor("#1E1B4B"));
        canvas.drawCircle(h, h, h, p);
        // Envelope body (white rounded rect)
        p.setColor(Color.parseColor("#818CF8"));
        p.setStyle(Paint.Style.FILL);
        android.graphics.RectF env = new android.graphics.RectF(12, 22, size - 12, size - 22);
        canvas.drawRoundRect(env, 6, 6, p);
        // Envelope flap (V shape)
        p.setColor(Color.parseColor("#4338CA"));
        Path path = new Path();
        path.moveTo(12, 22);
        path.lineTo(h, h + 4);
        path.lineTo(size - 12, 22);
        path.close();
        canvas.drawPath(path, p);
        // Clip to circle
        Bitmap result = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Canvas out = new Canvas(result);
        Paint clip = new Paint(Paint.ANTI_ALIAS_FLAG);
        clip.setColor(Color.WHITE);
        out.drawCircle(h, h, h, clip);
        clip.setXfermode(new android.graphics.PorterDuffXfermode(android.graphics.PorterDuff.Mode.SRC_IN));
        out.drawBitmap(bmp, 0, 0, clip);
        return result;
    }

    // ── Draw Google G placeholder ──────────────────────────
    private Bitmap makeGooglePlaceholder() {
        int size = 160, h = size / 2;
        Bitmap bmp = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bmp);
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        p.setColor(Color.WHITE);         canvas.drawCircle(h, h, h, p);
        p.setColor(Color.parseColor("#4285F4")); canvas.drawRect(0, 0, h, h, p);
        p.setColor(Color.parseColor("#EA4335")); canvas.drawRect(h, 0, size, h, p);
        p.setColor(Color.parseColor("#34A853")); canvas.drawRect(0, h, h, size, p);
        p.setColor(Color.parseColor("#FBBC05")); canvas.drawRect(h, h, size, size, p);
        p.setColor(Color.WHITE); canvas.drawCircle(h, h, h * 0.42f, p);
        Bitmap result = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Canvas out = new Canvas(result);
        Paint clip = new Paint(Paint.ANTI_ALIAS_FLAG);
        clip.setColor(Color.WHITE);
        out.drawCircle(h, h, h, clip);
        clip.setXfermode(new android.graphics.PorterDuffXfermode(android.graphics.PorterDuff.Mode.SRC_IN));
        out.drawBitmap(bmp, 0, 0, clip);
        return result;
    }

    private boolean isGoogleUser(FirebaseUser user) {
        for (UserInfo p : user.getProviderData())
            if ("google.com".equals(p.getProviderId())) return true;
        return false;
    }

    private void openDifficultySelection(String topic) {
        Intent intent = new Intent(this, difficulty_selection.class);
        intent.putExtra("TOPIC", topic);
        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    class LanguageAdapter extends BaseAdapter {
        @Override public int getCount()          { return languages.length; }
        @Override public Object getItem(int pos) { return languages[pos]; }
        @Override public long getItemId(int pos) { return pos; }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = convertView;
            if (view == null)
                view = getLayoutInflater().inflate(R.layout.page2xmlfile, parent, false);
            ((TextView) view.findViewById(R.id.iconView)).setText(icons[position]);
            ((TextView) view.findViewById(R.id.textView)).setText(languages[position]);
            ((TextView) view.findViewById(R.id.subtitleView)).setText(subtitles[position]);
            view.setAlpha(0f);
            view.animate().alpha(1f).setDuration(300).setStartDelay(position * 50L).start();
            return view;
        }
    }
}