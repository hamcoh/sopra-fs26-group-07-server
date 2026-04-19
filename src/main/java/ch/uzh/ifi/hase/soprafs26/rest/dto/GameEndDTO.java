package ch.uzh.ifi.hase.soprafs26.rest.dto;

import java.util.List;
import java.util.Map;

import ch.uzh.ifi.hase.soprafs26.constant.GameEndReason;

public class GameEndDTO {
    
    private Long gameSessionId;

    private GameEndReason reason;

    private Long winnerPlayerId;

    private List<PlayerScoreDTO> playerScores;

    public Long getGameSessionId() {
        return gameSessionId;
    }
    public void setGameSessionId(Long gameSessionId) {
        this.gameSessionId = gameSessionId;
    }
    public GameEndReason getReason() {
        return reason;
    }
    public void setReason(GameEndReason reason) {
        this.reason = reason;
    }
    public Long getWinnerPlayerId() {
        return winnerPlayerId;
    }
    public void setWinnerPlayerId(Long winnerPlayerId) {
        this.winnerPlayerId = winnerPlayerId;
    }
    public List<PlayerScoreDTO> getPlayerScores() {
        return playerScores;
    }
    public void setPlayerScores(List<PlayerScoreDTO> playerScores) {
        this.playerScores = playerScores;
    }
}