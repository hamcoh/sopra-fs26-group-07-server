package ch.uzh.ifi.hase.soprafs26.rest.dto;

public class ArchiveProblemDTO {

    private Long problemId;
    private String title;
    private String description; 

    private Long sumPassedTestCases;
    private Long sumTotalTestCases;
    private Long totalSubmissionCount;
    private Double totalSuccessRate;

    private Integer playerSumPassedTestCases;
    private Integer playerSumTotalTestCases; 
    private Double playerSuccessRate;

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
    public Long getSumPassedTestCases() {
        return sumPassedTestCases;
    }
    public void setSumPassedTestCases(Long sumPassedTestCases) {
        this.sumPassedTestCases = sumPassedTestCases;
    }
    public Long getSumTotalTestCases() {
        return sumTotalTestCases;
    }
    public void setSumTotalTestCases(Long sumTotalTestCases) {
        this.sumTotalTestCases = sumTotalTestCases;
    }

    public Long getTotalSubmissionCount() {
        return totalSubmissionCount;
    }
    public void setTotalSubmissionCount(Long totalSubmissionCount) {
        this.totalSubmissionCount = totalSubmissionCount;
    }
    public Double getTotalSuccessRate() {
        return totalSuccessRate;
    }
    public void setTotalSuccessRate(Double totalSuccessRate) {
        this.totalSuccessRate = totalSuccessRate;
    }
    public Integer getPlayerSumPassedTestCases() {
        return playerSumPassedTestCases;
    }
    public void setPlayerSumPassedTestCases(Integer playerSumPassedTestCases) {
        this.playerSumPassedTestCases = playerSumPassedTestCases;
    }
    public Integer getPlayerSumTotalTestCases() {
        return playerSumTotalTestCases;
    }
    public void setPlayerSumTotalTestCases(Integer playerSumTotalTestCases) {
        this.playerSumTotalTestCases = playerSumTotalTestCases;
    }
    public Double getPlayerSuccessRate() {
        return playerSuccessRate;
    }
    public void setPlayerSuccessRate(Double playerSuccessRate) {
        this.playerSuccessRate = playerSuccessRate;
    }
}
