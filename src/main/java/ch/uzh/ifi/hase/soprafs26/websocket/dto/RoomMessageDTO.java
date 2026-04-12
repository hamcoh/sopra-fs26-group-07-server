package ch.uzh.ifi.hase.soprafs26.websocket.dto;

public class RoomMessageDTO {

    private String username;
    private boolean host;

    public RoomMessageDTO() {}

    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }
    public boolean isHost() {
        return host;
    }
    public void setIsHost(boolean host) {
        this.host = host;
    }
}
