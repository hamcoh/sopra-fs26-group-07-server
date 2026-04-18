package ch.uzh.ifi.hase.soprafs26.rest.dto;

import java.util.List;

import ch.uzh.ifi.hase.soprafs26.constant.SubmissionStatus;
import ch.uzh.ifi.hase.soprafs26.constant.Verdict;

public class CodeRunDTO {

    private Long gameSessionId;
    private Long problemId;
    private Long playerSessionId;
    private List<String> tokens;
    private SubmissionStatus submissionStatus;
    private Verdict verdict;
    private String judgeResultsJson;
    private int passedTestCases;
    private int totalTestCases;
    private List<TestCaseFeedbackDTO> testCases;

    public Long getGameSessionId() {
        return gameSessionId;
    }

    public void setGameSessionId(Long gameSessionId) {
        this.gameSessionId = gameSessionId;
    }

    public Long getProblemId() {
        return problemId;
    }

    public void setProblemId(Long problemId) {
        this.problemId = problemId;
    }

    public Long getPlayerSessionId() {
        return playerSessionId;
    }

    public void setPlayerSessionId(Long playerSessionId) {
        this.playerSessionId = playerSessionId;
    }

    public List<String> getTokens() {
        return tokens;
    }

    public void setTokens(List<String> tokens) {
        this.tokens = tokens;
    }

    public SubmissionStatus getSubmissionStatus() {
        return submissionStatus;
    }

    public void setSubmissionStatus(SubmissionStatus submissionStatus) {
        this.submissionStatus = submissionStatus;
    }

    public Verdict getVerdict() {
        return verdict;
    }

    public void setVerdict(Verdict verdict) {
        this.verdict = verdict;
    }

    public String getJudgeResultsJson() {
        return judgeResultsJson;
    }

    public void setJudgeResultsJson(String judgeResultsJson) {
        this.judgeResultsJson = judgeResultsJson;
    }

    public int getPassedTestCases() {
        return passedTestCases;
    }

    public void setPassedTestCases(int passedTestCases) {
        this.passedTestCases = passedTestCases;
    }

    public int getTotalTestCases() {
        return totalTestCases;
    }

    public void setTotalTestCases(int totalTestCases) {
        this.totalTestCases = totalTestCases;
    }

    public List<TestCaseFeedbackDTO> getTestCases() {
        return testCases;
    }

    public void setTestCases(List<TestCaseFeedbackDTO> testCases) {
        this.testCases = testCases;
    }
}