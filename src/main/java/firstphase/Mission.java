package firstphase;

import java.io.Serializable;

/**
 * Mission - JavaBean representing a programming challenge in AnimeQuest.
 * Notice: For student security, correct_answer is never exposed to student-facing views.
 */
public class Mission implements Serializable {
    private static final long serialVersionUID = 1L;

    private int id;
    private String title;
    private String description;
    private String category;
    private String difficulty;
    private String optionA;
    private String optionB;
    private String optionC;
    private String optionD;
    private int xpReward;
    
    // Student progression tracking flags
    private boolean attempted;
    private Boolean userCorrect; // null if not attempted, true if correct, false if incorrect
    private String userAnswer;
    
    // Optional administrative-only field (populated solely in Admin views)
    private String adminCorrectAnswer;

    public Mission() {
    }

    public Mission(int id, String title, String description, String category, String difficulty,
                   String optionA, String optionB, String optionC, String optionD, int xpReward) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.category = category;
        this.difficulty = difficulty;
        this.optionA = optionA;
        this.optionB = optionB;
        this.optionC = optionC;
        this.optionD = optionD;
        this.xpReward = xpReward;
        this.attempted = false;
        this.userCorrect = null;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }

    public String getOptionA() {
        return optionA;
    }

    public void setOptionA(String optionA) {
        this.optionA = optionA;
    }

    public String getOptionB() {
        return optionB;
    }

    public void setOptionB(String optionB) {
        this.optionB = optionB;
    }

    public String getOptionC() {
        return optionC;
    }

    public void setOptionC(String optionC) {
        this.optionC = optionC;
    }

    public String getOptionD() {
        return optionD;
    }

    public void setOptionD(String optionD) {
        this.optionD = optionD;
    }

    public int getXpReward() {
        return xpReward;
    }

    public void setXpReward(int xpReward) {
        this.xpReward = xpReward;
    }

    public boolean isAttempted() {
        return attempted;
    }

    public void setAttempted(boolean attempted) {
        this.attempted = attempted;
    }

    public Boolean getUserCorrect() {
        return userCorrect;
    }

    public void setUserCorrect(Boolean userCorrect) {
        this.userCorrect = userCorrect;
    }

    public String getUserAnswer() {
        return userAnswer;
    }

    public void setUserAnswer(String userAnswer) {
        this.userAnswer = userAnswer;
    }

    public String getAdminCorrectAnswer() {
        return adminCorrectAnswer;
    }

    public void setAdminCorrectAnswer(String adminCorrectAnswer) {
        this.adminCorrectAnswer = adminCorrectAnswer;
    }
}
