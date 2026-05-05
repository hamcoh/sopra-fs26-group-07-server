package ch.uzh.ifi.hase.soprafs26.rest.dto;
import ch.uzh.ifi.hase.soprafs26.constant.SabotageType;

// Frontend will send this to the backend when a player uses a sabotage item, so that the backend can process the sabotage and broadcast the effects to all players in the game session.
public class SabotagePostDTO {
    
    private Long playerSessionId;
    private SabotageType item; // e.g., "SQUID_INK_SABOTAGE", "JITTER_SABOTAGE", "ROTATE_SABOTAGE"

    public Long getPlayerSessionId() { 
        return playerSessionId; 
    }
    
    public void setPlayerSessionId(Long playerSessionId) { 
        this.playerSessionId = playerSessionId;
 }
    
    public SabotageType getItem() { 
        return item; 
    }

    public void setItem(SabotageType item) { 
        this.item = item; 
    }
}
