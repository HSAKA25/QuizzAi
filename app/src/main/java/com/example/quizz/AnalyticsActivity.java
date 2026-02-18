package com.example.quizz;

import android.animation.ValueAnimator;
import android.graphics.Color;
import android.os.Bundle;
import android.view.animation.DecelerateInterpolator;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.*;
import com.github.mikephil.charting.components.*;
import com.github.mikephil.charting.data.*;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.firebase.auth.FirebaseAuth;

import java.text.SimpleDateFormat;
import java.util.*;

public class AnalyticsActivity extends AppCompatActivity {

    // ── Views ─────────────────────────────────────────────────
    private LineChart  lineChart;
    private BarChart   barChart;
    private RadarChart radarChart;
    private PieChart   pieChart;
    private BarChart   weeklyBarChart;
    private Button     backBtn;
    private CardView   btn1Week, btn2Week, btnAllTime;

    // Stat pills
    private TextView bestScore, avgScore, totalAttempts;
    private TextView winRateTv, trendTv, streakTv;
    private TextView aiInsightText;

    // Topic progress bars
    private ProgressBar pbJava, pbPython, pbKotlin, pbCSharp, pbC, pbCpp;
    private TextView    tvJava, tvPython, tvKotlin, tvCSharp, tvC, tvCpp;

    // Filter state
    private String currentFilter = "ALL"; // "1W", "2W", "ALL"

    // All loaded entries
    private ArrayList<Entry>  allEntries  = new ArrayList<>();
    private ArrayList<String> allLabels   = new ArrayList<>();
    private ArrayList<Long>   allTimestamps = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_analytics);

        bindViews();
        backBtn.setOnClickListener(v -> finish());

        setupChartStyles();
        loadData();

        // Time filter buttons
        btn1Week.setOnClickListener(v  -> { currentFilter = "1W";  applyFilter(); highlightBtn(btn1Week); });
        btn2Week.setOnClickListener(v  -> { currentFilter = "2W";  applyFilter(); highlightBtn(btn2Week); });
        btnAllTime.setOnClickListener(v-> { currentFilter = "ALL"; applyFilter(); highlightBtn(btnAllTime); });
        highlightBtn(btnAllTime);
    }

    private void bindViews() {
        lineChart      = findViewById(R.id.lineChart);
        barChart       = findViewById(R.id.barChart);
        radarChart     = findViewById(R.id.radarChart);
        pieChart       = findViewById(R.id.pieChart);
        weeklyBarChart = findViewById(R.id.weeklyBarChart);
        backBtn        = findViewById(R.id.backBtn);

        btn1Week   = findViewById(R.id.btn1Week);
        btn2Week   = findViewById(R.id.btn2Week);
        btnAllTime = findViewById(R.id.btnAllTime);

        bestScore     = findViewById(R.id.bestScore);
        avgScore      = findViewById(R.id.avgScore);
        totalAttempts = findViewById(R.id.totalAttempts);
        winRateTv     = findViewById(R.id.winRate);
        trendTv       = findViewById(R.id.trend);
        streakTv      = findViewById(R.id.streak);
        aiInsightText = findViewById(R.id.aiInsightText);

        pbJava   = findViewById(R.id.pbJava);
        pbPython = findViewById(R.id.pbPython);
        pbKotlin = findViewById(R.id.pbKotlin);
        pbCSharp = findViewById(R.id.pbCSharp);
        pbC      = findViewById(R.id.pbC);
        pbCpp    = findViewById(R.id.pbCpp);
        tvJava   = findViewById(R.id.tvJava);
        tvPython = findViewById(R.id.tvPython);
        tvKotlin = findViewById(R.id.tvKotlin);
        tvCSharp = findViewById(R.id.tvCSharp);
        tvC      = findViewById(R.id.tvC);
        tvCpp    = findViewById(R.id.tvCpp);
    }

    private void highlightBtn(CardView active) {
        int off = Color.parseColor("#0A1628");
        int on  = Color.parseColor("#1E3A5F");
        btn1Week.setCardBackgroundColor(off);
        btn2Week.setCardBackgroundColor(off);
        btnAllTime.setCardBackgroundColor(off);
        active.setCardBackgroundColor(on);
    }

    // ── Load from local history ────────────────────────────────
    private void loadData() {
        List<QuizHistoryManager.QuizResult> localHistory = QuizHistoryManager.getAll(this);
        allEntries.clear(); allLabels.clear(); allTimestamps.clear();

        if (localHistory != null && !localHistory.isEmpty()) {
            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy  h:mm a", Locale.getDefault());
            for (int i = 0; i < localHistory.size(); i++) {
                QuizHistoryManager.QuizResult r = localHistory.get(i);
                float pct = r.total > 0 ? (r.score * 100f / r.total) : 0;
                // history is newest-first, so reverse index for chart
                allEntries.add(new Entry(i, pct));
                allLabels.add(r.topic);
                // parse date string to timestamp for filter
                long ts = System.currentTimeMillis();
                try { ts = sdf.parse(r.date).getTime(); } catch (Exception ignored) {}
                allTimestamps.add(ts);
            }
            // Reverse so oldest is first (left of chart)
            Collections.reverse(allEntries);
            Collections.reverse(allLabels);
            Collections.reverse(allTimestamps);
            // Re-index entries after reverse
            for (int i = 0; i < allEntries.size(); i++)
                allEntries.set(i, new Entry(i, allEntries.get(i).getY()));
        } else {
            injectDemo();
        }
        applyFilter();
    }

    private void injectDemo() {
        float[] demo = {45,52,48,63,71,68,74,80,77,85,90,72,88,65,95};
        String[] topics = {"Java","Python","Kotlin","C#","C","C++","Java","Python","Kotlin","C#","C","C++","Java","Python","C"};
        allEntries.clear(); allLabels.clear(); allTimestamps.clear();
        long now = System.currentTimeMillis();
        for (int i = 0; i < demo.length; i++) {
            allEntries.add(new Entry(i, demo[i]));
            allLabels.add(topics[i]);
            allTimestamps.add(now - (long)(demo.length - i) * 86400000L);
        }
    }

    // ── Filter by time ─────────────────────────────────────────
    private void applyFilter() {
        long cutoff = 0;
        if ("1W".equals(currentFilter))      cutoff = System.currentTimeMillis() - 7L  * 86400000L;
        else if ("2W".equals(currentFilter)) cutoff = System.currentTimeMillis() - 14L * 86400000L;

        ArrayList<Entry>  filtered = new ArrayList<>();
        ArrayList<String> labels   = new ArrayList<>();
        for (int i = 0; i < allEntries.size(); i++) {
            if (allTimestamps.isEmpty() || allTimestamps.get(i) >= cutoff) {
                filtered.add(new Entry(filtered.size(), allEntries.get(i).getY()));
                labels.add(allLabels.isEmpty() ? "R"+(i+1) : allLabels.get(i));
            }
        }
        if (filtered.isEmpty()) { filtered.addAll(allEntries); labels.addAll(allLabels); }

        populateDashboard(filtered, labels);
    }

    // ── Populate everything ────────────────────────────────────
    private void populateDashboard(ArrayList<Entry> entries, ArrayList<String> labels) {
        if (entries.isEmpty()) return;

        int count = entries.size(), sum = 0, best = 0, wins = 0;
        int[] distribution = new int[5];
        Map<String, int[]> topicStats = new LinkedHashMap<>();
        // topic -> [totalScore, count]
        String[] topicNames = {"Java","Python","Kotlin","C#","C","C++"};
        for (String t : topicNames) topicStats.put(t, new int[]{0,0});

        for (int i = 0; i < entries.size(); i++) {
            int s = (int) entries.get(i).getY();
            sum += s;
            if (s > best) best = s;
            if (s >= 60) wins++;
            distribution[Math.min(s / 20, 4)]++;
            String topic = i < labels.size() ? labels.get(i) : "Other";
            for (String t : topicNames) {
                if (topic.equalsIgnoreCase(t) || topic.equalsIgnoreCase(t.replace("+",""))) {
                    topicStats.get(t)[0] += s;
                    topicStats.get(t)[1]++;
                    break;
                }
            }
        }

        int avg     = sum / count;
        int winRate = wins * 100 / count;

        // Pills
        animateCounter(bestScore,     0, best,    "%");
        animateCounter(avgScore,      0, avg,      "%");
        animateCounter(totalAttempts, 0, count,    "");
        animateCounter(winRateTv,     0, winRate,  "%");

        // Trend
        String trendText = "→ Stable"; int trendColor = Color.parseColor("#FFC107");
        if (count >= 6) {
            float early = (entries.get(0).getY()+entries.get(1).getY()+entries.get(2).getY())/3f;
            float late  = (entries.get(count-1).getY()+entries.get(count-2).getY()+entries.get(count-3).getY())/3f;
            if (late-early > 5)  { trendText="↑ Improving"; trendColor=Color.parseColor("#00E5A0"); }
            else if (early-late>5){ trendText="↓ Declining"; trendColor=Color.parseColor("#FF5370"); }
        }
        trendTv.setText(trendText); trendTv.setTextColor(trendColor);

        // Streak
        int streak = 0;
        for (int i = count-1; i >= 0; i--) { if (entries.get(i).getY()>=60) streak++; else break; }
        streakTv.setText(streak + " 🔥");

        // AI Insight
        buildAiInsight(best, avg, winRate, trendText, streak, count);

        // Charts
        renderLineChart(entries, labels);
        renderBarChart(distribution);
        renderRadarChart(topicStats);
        renderPieChart(winRate, 100-winRate);
        renderWeeklyBar(entries, labels);
        renderTopicBars(topicStats);
    }

    // ═══════════════════════════════════════════════════════════
    //  CHART SETUP STYLES
    // ═══════════════════════════════════════════════════════════
    private void setupChartStyles() {
        // Line chart
        styleBarLine(lineChart);
        lineChart.setDragEnabled(true); lineChart.setScaleEnabled(true); lineChart.setPinchZoom(true);
        styleYAxis(lineChart.getAxisLeft());
        styleXAxis(lineChart.getXAxis());

        // Bar chart (distribution)
        styleBarLine(barChart);
        barChart.setDrawValueAboveBar(true);
        styleYAxis(barChart.getAxisLeft());
        styleXAxis(barChart.getXAxis());
        barChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(
                new String[]{"0-20","21-40","41-60","61-80","81-100"}));

        // Weekly bar
        styleBarLine(weeklyBarChart);
        weeklyBarChart.setDrawValueAboveBar(true);
        styleYAxis(weeklyBarChart.getAxisLeft());
        styleXAxis(weeklyBarChart.getXAxis());

        // Radar
        radarChart.setBackgroundColor(Color.TRANSPARENT);
        radarChart.setWebColor(Color.parseColor("#1A2A3A"));
        radarChart.setWebColorInner(Color.parseColor("#0D1E2E"));
        radarChart.setWebAlpha(200); radarChart.setWebLineWidth(1f); radarChart.setWebLineWidthInner(0.75f);
        Description d2 = new Description(); d2.setText(""); radarChart.setDescription(d2);
        radarChart.getXAxis().setTextColor(Color.parseColor("#00E5FF")); radarChart.getXAxis().setTextSize(10f);
        radarChart.getYAxis().setTextColor(Color.TRANSPARENT); radarChart.getYAxis().setLabelCount(4,false);
        radarChart.getLegend().setEnabled(false);

        // Pie chart
        pieChart.setBackgroundColor(Color.TRANSPARENT);
        pieChart.setDrawHoleEnabled(true); pieChart.setHoleRadius(52f); pieChart.setTransparentCircleRadius(57f);
        pieChart.setHoleColor(Color.parseColor("#040E1C"));
        pieChart.setTransparentCircleColor(Color.parseColor("#040E1C")); pieChart.setTransparentCircleAlpha(80);
        pieChart.setDrawCenterText(true); pieChart.setCenterTextColor(Color.WHITE); pieChart.setCenterTextSize(14f);
        Description d3 = new Description(); d3.setText(""); pieChart.setDescription(d3);
        pieChart.getLegend().setTextColor(Color.parseColor("#607D8B")); pieChart.getLegend().setTextSize(10f);
        pieChart.setEntryLabelColor(Color.TRANSPARENT);
        pieChart.setRotationEnabled(true); pieChart.setHighlightPerTapEnabled(true);
    }

    private void styleBarLine(BarLineChartBase<?> c) {
        c.setBackgroundColor(Color.TRANSPARENT); c.setDrawGridBackground(false);
        c.setDrawBorders(false); c.getAxisRight().setEnabled(false);
        Description d = new Description(); d.setText(""); c.setDescription(d);
        c.getLegend().setTextColor(Color.parseColor("#607D8B")); c.getLegend().setTextSize(10f);
    }
    private void styleYAxis(YAxis y) {
        y.setTextColor(Color.parseColor("#607D8B")); y.setGridColor(Color.parseColor("#0D1E2E"));
        y.setGridLineWidth(1f); y.setDrawAxisLine(false); y.setGranularity(1f); y.setTextSize(10f);
    }
    private void styleXAxis(XAxis x) {
        x.setTextColor(Color.parseColor("#607D8B")); x.setPosition(XAxis.XAxisPosition.BOTTOM);
        x.setDrawGridLines(false); x.setDrawAxisLine(false); x.setTextSize(9f); x.setLabelRotationAngle(-30f);
    }

    // ═══════════════════════════════════════════════════════════
    //  RENDER CHARTS
    // ═══════════════════════════════════════════════════════════

    private void renderLineChart(ArrayList<Entry> entries, ArrayList<String> labels) {
        LineDataSet ds = new LineDataSet(entries, "Score %");
        ds.setMode(LineDataSet.Mode.CUBIC_BEZIER); ds.setCubicIntensity(0.2f);
        ds.setColor(Color.parseColor("#00E5FF")); ds.setLineWidth(2.5f);
        ds.setCircleRadius(4.5f); ds.setCircleHoleRadius(2f);
        ds.setCircleColor(Color.parseColor("#00E5FF")); ds.setCircleHoleColor(Color.parseColor("#040E1C"));
        ds.setDrawValues(true); ds.setValueTextColor(Color.parseColor("#00E5FF")); ds.setValueTextSize(9f);
        ds.setDrawFilled(true); ds.setFillColor(Color.parseColor("#00E5FF")); ds.setFillAlpha(20);
        ds.setHighLightColor(Color.WHITE); ds.setDrawHighlightIndicators(true);
        lineChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(labels));
        lineChart.getXAxis().setLabelCount(Math.min(labels.size(), 8));
        lineChart.setData(new LineData(ds));
        lineChart.animateXY(1200, 800, Easing.EaseInOutQuart);
        lineChart.invalidate();
    }

    private void renderBarChart(int[] dist) {
        int[] colors = {Color.parseColor("#FF5370"),Color.parseColor("#FF9100"),
                Color.parseColor("#FFC107"),Color.parseColor("#00E5A0"),Color.parseColor("#00E5FF")};
        ArrayList<BarEntry> entries = new ArrayList<>();
        for (int i = 0; i < dist.length; i++) entries.add(new BarEntry(i, dist[i]));
        BarDataSet ds = new BarDataSet(entries, "Score Distribution");
        ds.setColors(colors); ds.setValueTextColor(Color.parseColor("#AABBCC")); ds.setValueTextSize(10f);
        BarData data = new BarData(ds); data.setBarWidth(0.6f);
        barChart.setData(data); barChart.animateY(1000, Easing.EaseOutBack); barChart.invalidate();
    }

    private void renderWeeklyBar(ArrayList<Entry> entries, ArrayList<String> labels) {
        // Group scores by day of week
        int[] dayScores = new int[7]; int[] dayCounts = new int[7];
        long now = System.currentTimeMillis();
        for (int i = 0; i < entries.size(); i++) {
            long ts = allTimestamps.isEmpty() ? now - (long)(entries.size()-i)*86400000L
                    : (i < allTimestamps.size() ? allTimestamps.get(i) : now);
            Calendar cal = Calendar.getInstance(); cal.setTimeInMillis(ts);
            int day = (cal.get(Calendar.DAY_OF_WEEK) - 1); // 0=Sun
            dayScores[day]  += (int) entries.get(i).getY();
            dayCounts[day]++;
        }
        ArrayList<BarEntry> bars = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            bars.add(new BarEntry(i, dayCounts[i] > 0 ? (float)dayScores[i]/dayCounts[i] : 0));
        }
        BarDataSet ds = new BarDataSet(bars, "Avg Score by Day");
        ds.setColors(Color.parseColor("#6366F1"),Color.parseColor("#8B5CF6"),
                Color.parseColor("#A78BFA"),Color.parseColor("#818CF8"),
                Color.parseColor("#6366F1"),Color.parseColor("#4F46E5"),Color.parseColor("#4338CA"));
        ds.setValueTextColor(Color.parseColor("#A5B4FC")); ds.setValueTextSize(9f);
        BarData data = new BarData(ds); data.setBarWidth(0.5f);
        weeklyBarChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(
                new String[]{"Sun","Mon","Tue","Wed","Thu","Fri","Sat"}));
        weeklyBarChart.setData(data);
        weeklyBarChart.animateY(1000, Easing.EaseOutBack); weeklyBarChart.invalidate();
    }

    private void renderRadarChart(Map<String,int[]> topicStats) {
        ArrayList<RadarEntry> entries = new ArrayList<>();
        ArrayList<String> radarLabels = new ArrayList<>();
        for (Map.Entry<String,int[]> e : topicStats.entrySet()) {
            int avg2 = e.getValue()[1] > 0 ? e.getValue()[0] / e.getValue()[1] : 0;
            entries.add(new RadarEntry(avg2));
            radarLabels.add(e.getKey());
        }
        if (entries.isEmpty()) return;
        RadarDataSet ds = new RadarDataSet(entries, "Topic Mastery");
        ds.setColor(Color.parseColor("#00E5FF")); ds.setFillColor(Color.parseColor("#00E5FF"));
        ds.setFillAlpha(40); ds.setLineWidth(2f); ds.setDrawFilled(true); ds.setDrawValues(false);
        radarChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(radarLabels));
        radarChart.setData(new RadarData(ds));
        radarChart.animateXY(1400,1400, Easing.EaseInOutQuart); radarChart.invalidate();
    }

    private void renderPieChart(int wins, int losses) {
        ArrayList<PieEntry> entries = new ArrayList<>();
        entries.add(new PieEntry(wins,   "Passed"));
        entries.add(new PieEntry(losses, "Failed"));
        PieDataSet ds = new PieDataSet(entries, "");
        ds.setColors(Color.parseColor("#00E5A0"), Color.parseColor("#FF5370"));
        ds.setSliceSpace(3f); ds.setSelectionShift(6f);
        ds.setValueTextColor(Color.WHITE); ds.setValueTextSize(11f);
        PieData data = new PieData(ds);
        data.setValueFormatter(new PercentFormatter(pieChart));
        pieChart.setData(data);
        pieChart.setCenterText(wins + "%\nWin Rate");
        pieChart.setUsePercentValues(true);
        pieChart.animateY(1200, Easing.EaseInOutQuart); pieChart.invalidate();
    }

    private void renderTopicBars(Map<String,int[]> topicStats) {
        ProgressBar[] bars = {pbJava,pbPython,pbKotlin,pbCSharp,pbC,pbCpp};
        TextView[]    tvs  = {tvJava,tvPython,tvKotlin,tvCSharp,tvC,tvCpp};
        String[]      names= {"Java","Python","Kotlin","C#","C","C++"};
        int i = 0;
        for (String name : names) {
            int[] stat = topicStats.containsKey(name) ? topicStats.get(name) : new int[]{0,0};
            int avg2 = stat[1] > 0 ? stat[0]/stat[1] : 0;
            animateProgressBar(bars[i], avg2);
            tvs[i].setText(name + "  " + (stat[1]>0 ? avg2+"%" : "No data"));
            i++;
        }
    }

    private void buildAiInsight(int best, int avg, int winRate, String trend, int streak, int count) {
        String s;
        if (count < 3) s = "🤖 Complete at least 3 quizzes to unlock AI-powered performance insights.";
        else if (avg >= 80) s = "🤖 Outstanding! Your " + avg + "% avg puts you in the top tier. Push harder topics to achieve 100%.";
        else if (avg >= 60) s = "🤖 Solid at " + avg + "% avg. " + trend + " detected. Target weak topics in 10-min focused sessions to break 80%.";
        else s = "🤖 Your best is " + best + "% — that potential is real. Start with Easy difficulty daily to build a winning streak.";
        if (streak >= 3) s += " 🔥 " + streak + "-quiz win streak — keep it going!";
        if (winRate >= 80) s += " You're winning " + winRate + "% of quizzes — elite level!";
        aiInsightText.setText(s);
    }

    private void animateCounter(TextView tv, int from, int to, String suffix) {
        ValueAnimator a = ValueAnimator.ofInt(from, to);
        a.setDuration(1200); a.setInterpolator(new DecelerateInterpolator());
        a.addUpdateListener(x -> tv.setText(x.getAnimatedValue() + suffix));
        a.start();
    }

    private void animateProgressBar(ProgressBar pb, int target) {
        ValueAnimator a = ValueAnimator.ofInt(0, target);
        a.setDuration(1000); a.setInterpolator(new DecelerateInterpolator());
        a.addUpdateListener(x -> pb.setProgress((int) x.getAnimatedValue()));
        a.start();
    }
}