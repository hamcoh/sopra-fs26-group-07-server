package ch.uzh.ifi.hase.soprafs26.rest.dto;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PlayerGameSummaryDTO {

    private Long playerSessionId;

    private Long playerId;

    //this is simply a Map that maps keys such as "solvedCorrecty"/"notSolvedFullyCorrectly" to the problemIds
    //helps frontend to understand which problems where solved fully correctly and which not
    private Map<String, List<Long>> problemResults = new HashMap<>();

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
    
    public Map<String, List<Long>> getProblemResults() {
        return problemResults;
    }

    public void setProblemResults(Map<String, List<Long>> problemResults) {
        this.problemResults = problemResults;
    }
}
