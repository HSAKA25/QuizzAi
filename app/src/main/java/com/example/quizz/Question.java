package com.example.quizz;

import com.google.firebase.firestore.PropertyName;

/**
 * Question model - works with both local JSON and Firestore.
 * Firestore document structure:
 * {
 *   "question": "What is JVM?",
 *   "optionA": "Java Virtual Machine",
 *   "optionB": "Java Variable Method",
 *   "optionC": "Java Volatile Memory",
 *   "optionD": "None of the above",
 *   "answer": "A",
 *   "explanation": "JVM stands for Java Virtual Machine" // optional
 * }
 */
public class Question {

    private String question;
    private String optionA;
    private String optionB;
    private String optionC;
    private String optionD;
    private String answer;
    private String explanation;

    // Required empty constructor for Firestore deserialization
    public Question() {}

    public Question(String question, String optionA, String optionB,
                    String optionC, String optionD, String answer, String explanation) {
        this.question = question;
        this.optionA = optionA;
        this.optionB = optionB;
        this.optionC = optionC;
        this.optionD = optionD;
        this.answer = answer;
        this.explanation = explanation;
    }

    // Getters and Setters
    public String getQuestion() { return question; }
    public void setQuestion(String question) { this.question = question; }

    public String getOptionA() { return optionA; }
    public void setOptionA(String optionA) { this.optionA = optionA; }

    public String getOptionB() { return optionB; }
    public void setOptionB(String optionB) { this.optionB = optionB; }

    public String getOptionC() { return optionC; }
    public void setOptionC(String optionC) { this.optionC = optionC; }

    public String getOptionD() { return optionD; }
    public void setOptionD(String optionD) { this.optionD = optionD; }

    public String getAnswer() { return answer; }
    public void setAnswer(String answer) { this.answer = answer; }

    public String getExplanation() { return explanation; }
    public void setExplanation(String explanation) { this.explanation = explanation; }
}