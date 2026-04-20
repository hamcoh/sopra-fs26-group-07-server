package ch.uzh.ifi.hase.soprafs26.rest.dto;

public class GameTimeWarningDTO {

    private Long gameSessionId;
    private Long remainingTimeSeconds;

    public Long getGameSessionId() {
        return gameSessionId;
    }

    public void setGameSessionId(Long gameSessionId) {
        this.gameSessionId = gameSessionId;
    }

    public Long getRemainingTimeSeconds() {
        return remainingTimeSeconds;
    }

    public void setRemainingTimeSeconds(Long remainingTimeSeconds) {
        this.remainingTimeSeconds = remainingTimeSeconds;
    }
    
}
