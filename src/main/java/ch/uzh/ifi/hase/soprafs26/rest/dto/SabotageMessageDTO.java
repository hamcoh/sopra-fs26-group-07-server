package ch.uzh.ifi.hase.soprafs26.rest.dto;
import ch.uzh.ifi.hase.soprafs26.constant.SabotageType;

public class SabotageMessageDTO {
    private SabotageType item;

    public SabotageMessageDTO(SabotageType item) {
        this.item = item;
    }

    public SabotageType getItem() { 
        return item; 
    }
    
    public void setItem(SabotageType item) { 
        this.item = item; 
    }
}