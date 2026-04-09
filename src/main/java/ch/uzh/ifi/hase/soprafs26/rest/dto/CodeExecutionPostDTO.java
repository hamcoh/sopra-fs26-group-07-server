package ch.uzh.ifi.hase.soprafs26.rest.dto;

public class CodeExecutionPostDTO {

    private Long playerSessionId;
    private String sourceCode;

    public Long getPlayerSessionId() {
        return playerSessionId;
    }

    public void setPlayerSessionId(Long playerSessionId) {
        this.playerSessionId = playerSessionId;
    }

    public String getSourceCode() {
        return sourceCode;
    }

    public void setSourceCode(String sourceCode) {
        this.sourceCode = sourceCode;
    }
}