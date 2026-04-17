package ch.uzh.ifi.hase.soprafs26.rest.dto;

public class GameRoundDTO {

    private Long gameSessionId;
    private Long playerSessionId;
    private Long playerId;
    private Integer currentScore;
    private Integer numOfSkippedProblems;

    private Long problemId;
    private String title;
    private String description;
    private String inputFormat;
    private String outputFormat;
    private String constraints;

    public Long getGameSessionId() {
        return gameSessionId;
    }
    public void setGameSessionId(Long gameSessionId) {
        this.gameSessionId = gameSessionId;
    }
    public Long getPlayerSessionId() {
        return playerSessionId;
    }
    public void setPlayerSessionId(Long playerSessionId) {
        this.playerSessionId = playerSessionId;
    }
    public Long getPlayerId() {
        return playerId;
    }
    public void setPlayerId(Long playerId) {
        this.playerId = playerId;
    }
    public Integer getCurrentScore() {
        return currentScore;
    }
    public void setCurrentScore(Integer currentScore) {
        this.currentScore = currentScore;
    }
    public Integer getNumOfSkippedProblems() {
        return numOfSkippedProblems;
    }
    public void setNumOfSkippedProblems(Integer numOfSkippedProblems) {
        this.numOfSkippedProblems = numOfSkippedProblems;
    }
    public Long getProblemId() {
        return problemId;
    }
    public void setProblemId(Long problemId) {
        this.problemId = problemId;
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
    public String getInputFormat() {
        return inputFormat;
    }
    public void setInputFormat(String inputFormat) {
        this.inputFormat = inputFormat;
    }
    public String getOutputFormat() {
        return outputFormat;
    }
    public void setOutputFormat(String outputFormat) {
        this.outputFormat = outputFormat;
    }
    public String getConstraints() {
        return constraints;
    }
    public void setConstraints(String constraints) {
        this.constraints = constraints;
    }
}
