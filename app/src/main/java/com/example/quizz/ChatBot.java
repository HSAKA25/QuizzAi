package com.example.quizz;

import android.content.Context;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class ChatBot {

    private static final Random random = new Random();
    private static final Map<String, String[]> intentMap   = new HashMap<>();
    private static final Map<String, String[]> responseMap = new HashMap<>();

    static {
        intentMap.put("greet", new String[]{
                "hi","hello","hey","hola","namaste","yo","sup",
                "morning","evening","afternoon","welcome","howdy"
        });
        responseMap.put("greet", new String[]{
                "Hello! Ready to quiz? 🎯 Ask me about Java, Python, Math or Science!",
                "Hey there! 👋 Pick a topic and let's get started!",
                "Hi! I'm your Quiz Assistant. What would you like to know?"
        });

        intentMap.put("java", new String[]{
                "java","jvm","oop","object oriented","inheritance","polymorphism",
                "abstraction","encapsulation","spring","maven","gradle"
        });
        responseMap.put("java", new String[]{
                "Java is a classic! ☕ Our Java quiz covers Easy, Medium and Hard levels. Ready to test your skills?",
                "Great choice! Java OOP, JVM internals, Collections — we cover it all. Pick a difficulty!",
                "Java developer? 💪 Our quiz has 45+ Java questions across 3 difficulty levels!"
        });

        intentMap.put("python", new String[]{
                "python","django","flask","pandas","numpy","pip",
                "scripting","machine learning","ml","data science"
        });
        responseMap.put("python", new String[]{
                "Python is powerful! 🐍 From basics to advanced — our Python quiz has it all.",
                "Pythonista detected! 🎯 We have Easy, Medium and Hard Python quizzes waiting for you.",
                "Great language choice! Our Python quiz covers syntax, OOP, libraries and more!"
        });

        intentMap.put("kotlin", new String[]{
                "kotlin","android","coroutine","jetpack","compose",
                "kotlin android","mobile dev","android dev"
        });
        responseMap.put("kotlin", new String[]{
                "Kotlin is the future of Android! 📱 Our Kotlin quiz covers coroutines, extensions and more.",
                "Modern Android developer! 🚀 Test your Kotlin skills across 3 difficulty levels.",
                "Kotlin fan? Check out our Kotlin quiz — coroutines, null safety and Android patterns!"
        });

        intentMap.put("csharp", new String[]{
                "c#","csharp","c sharp","dotnet",".net","unity","asp","linq","xamarin"
        });
        responseMap.put("csharp", new String[]{
                "C# is elegant! ✨ Our C# quiz covers OOP, LINQ, async/await and more.",
                "Great choice! C# powers everything from games to enterprise apps. Ready to quiz?",
                ".NET developer? 💼 Our C# quiz has challenges for all skill levels!"
        });

        intentMap.put("science", new String[]{
                "physics","chemistry","biology","science","atom","molecule",
                "einstein","newton","gravity","space","planet","galaxy",
                "evolution","dna","cell","acid","element","periodic"
        });
        responseMap.put("science", new String[]{
                "Science is fascinating! 🔬 We have Physics, Chemistry and Biology questions.",
                "Ready for some lab-tested questions? 🧪 Our science quiz covers everything from atoms to galaxies.",
                "Science quiz time! 🌌 From Newton's laws to DNA structure — pick your challenge!"
        });

        intentMap.put("math", new String[]{
                "math","mathematics","algebra","geometry","calculus",
                "trigonometry","numbers","fraction","decimal",
                "equation","formula","pi","theorem","arithmetic"
        });
        responseMap.put("math", new String[]{
                "Calculators ready? 🔢 Our Math quiz is tough but fair!",
                "Numbers don't lie. 📊 Let's test your math skills — from Algebra to Calculus!",
                "Math challenge accepted! 🎯 Easy warm-ups to Hard brain-teasers await you."
        });

        intentMap.put("rules", new String[]{
                "rule","instruction","how to","how do i","guide","points",
                "score","grading","rank","leaderboard","time","timer",
                "how to play","what are the rules","explain"
        });
        responseMap.put("rules", new String[]{
                "📋 Rules: Easy=10 questions, Medium=15, Hard=20. Pick your language and difficulty to begin!",
                "⏱️ Each question is timed. Accuracy and speed both affect your final rank on the leaderboard!",
                "Simple rules: Pick a topic → Choose difficulty → Answer questions → Check your rank! 🏆"
        });

        intentMap.put("help", new String[]{
                "help","support","stuck","error","bug","crash",
                "fix","not working","slow","lag","login","password",
                "account","issue","problem","how","what"
        });
        responseMap.put("help", new String[]{
                "I'm here to help! 🛠️ If the app is slow, try restarting it.",
                "Having trouble? 🤔 Make sure you have a stable internet connection for leaderboard features.",
                "Need help? I can tell you about quiz topics, rules, scoring or navigation. Just ask! 💬"
        });

        intentMap.put("score", new String[]{
                "score","point","rank","ranking","position","leaderboard",
                "top","best","high score","my score","result","performance"
        });
        responseMap.put("score", new String[]{
                "Check the 🏆 Leaderboard button on the home screen to see top 10 players!",
                "Your score is saved after each quiz. Tap Rankings on the home screen to see where you stand!",
                "Top players are ranked by accuracy and speed. Keep practicing to climb the leaderboard! 📈"
        });

        intentMap.put("topics", new String[]{
                "topic","subject","category","what can","what quiz","available",
                "options","choose","which","language","languages"
        });
        responseMap.put("topics", new String[]{
                "We have 4 quiz topics: ☕ Java, 🐍 Python, 🎮 Kotlin and ✨ C#. Which one interests you?",
                "Available quizzes: Java, Python, Kotlin and C#. Each has Easy, Medium and Hard levels!",
                "Pick from Java, Python, Kotlin or C# — each with 3 difficulty levels and 10-20 questions! 🎯"
        });
    }

    // ✅ Called from background thread — no network needed, instant response
    public static String reply(Context context, String userMessage) {
        return reply(userMessage);
    }

    // ✅ Also works without Context for simplicity
    public static String reply(String msg) {
        if (msg == null || msg.trim().isEmpty())
            return "Please type something 😊";

        msg = msg.toLowerCase().trim();

        for (Map.Entry<String, String[]> entry : intentMap.entrySet()) {
            for (String keyword : entry.getValue()) {
                if (msg.contains(keyword)) {
                    return pick(responseMap.get(entry.getKey()));
                }
            }
        }

        return "Hmm, I'm not sure about that! 🤔 Try asking about Java, Python, Math, Science or Quiz Rules!";
    }

    private static String pick(String[] replies) {
        return replies[random.nextInt(replies.length)];
    }
}