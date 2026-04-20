package ch.uzh.ifi.hase.soprafs26.rest.dto;

import java.util.List;

import ch.uzh.ifi.hase.soprafs26.constant.GameEndReason;
import ch.uzh.ifi.hase.soprafs26.constant.GameStatus;

public class GameEndDTO {
    
    private Long gameSessionId;

    private GameStatus gameStatus;

    private GameEndReason gameEndReason;

    private Long winnerPlayerId;

    private List<PlayerScoreDTO> playerScores;

    public Long getGameSessionId() {
        return gameSessionId;
    }
    public void setGameSessionId(Long gameSessionId) {
        this.gameSessionId = gameSessionId;
    }
    public GameEndReason getGameEndReason() {
        return gameEndReason;
    }
    public void setGameEndReason(GameEndReason gameEndReason) {
        this.gameEndReason = gameEndReason;
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
    public GameStatus getGameStatus() {
        return gameStatus;
    }
    public void setGameStatus(GameStatus gameStatus) {
        this.gameStatus = gameStatus;
    }
}