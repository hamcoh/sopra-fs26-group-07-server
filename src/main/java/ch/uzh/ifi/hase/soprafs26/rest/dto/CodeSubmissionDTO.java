package ch.uzh.ifi.hase.soprafs26.rest.dto;

import java.util.List;

public class CodeSubmissionDTO {

    private Long gameSessionId;
    private Long problemId;
    private Long playerSessionId;
    private List<String> tokens;

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
}