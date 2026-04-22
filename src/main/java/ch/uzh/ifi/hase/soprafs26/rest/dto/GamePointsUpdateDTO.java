package ch.uzh.ifi.hase.soprafs26.rest.dto;

import java.util.HashMap;
import java.util.Map;

public class GamePointsUpdateDTO {

    private Long gameSessionId;

    Map<Long, Integer> scores = new HashMap<>();

    public Long getGameSessionId() {
        return gameSessionId;
    }

    public void setGameSessionId(Long gameSessionId) {
        this.gameSessionId = gameSessionId;
    }

    public Map<Long, Integer> getScores() {
        return scores;
    }

    public void setScores(Map<Long, Integer> scores) {
        this.scores = scores;
    }
}
