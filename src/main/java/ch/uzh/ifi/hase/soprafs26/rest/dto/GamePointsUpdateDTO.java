package ch.uzh.ifi.hase.soprafs26.rest.dto;

public class GamePointsUpdateDTO {

    private Long gameSessionId;

    private Long playerSessionId;

    private int currentScore;

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

    public int getCurrentScore() {
        return currentScore;
    }

    public void setCurrentScore(int currentScore) {
        this.currentScore = currentScore;
    }
}
