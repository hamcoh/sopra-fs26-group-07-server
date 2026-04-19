package ch.uzh.ifi.hase.soprafs26.rest.dto;

public class PlayerScoreDTO {
    
    private Long playerSessionId;

    private Long userId;

    private String username;

    private int score;

    private int problemsSolved;

    public Long getPlayerSessionId() {
        return playerSessionId;
    }
    public void setPlayerSessionId(Long playerSessionId) {
        this.playerSessionId = playerSessionId;
    }
    public Long getUserId() {
        return userId;
    }
    public void setUserId(Long userId) {
        this.userId = userId;
    }
    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }
    public int getScore() {
        return score;
    }
    public void setScore(int score) {
        this.score = score;
    }
    public int getProblemsSolved() {
        return problemsSolved;
    }
    public void setProblemsSolved(int problemsSolved) {
        this.problemsSolved = problemsSolved;
    }
}