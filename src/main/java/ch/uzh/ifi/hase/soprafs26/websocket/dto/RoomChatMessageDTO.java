package ch.uzh.ifi.hase.soprafs26.websocket.dto;

import java.time.LocalDateTime;

public class RoomChatMessageDTO {
    
    private String senderUsername;
    private String content;
    private LocalDateTime timestamp;

    public String getSenderUsername() {
        return senderUsername;
    }
    public void setSenderUsername(String senderUsername) {
        this.senderUsername = senderUsername;
    }
    public String getContent() {
        return content;
    }
    public void setContent(String content) {
        this.content = content;
    }
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}
