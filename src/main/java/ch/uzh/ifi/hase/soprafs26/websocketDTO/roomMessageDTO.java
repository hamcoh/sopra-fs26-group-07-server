package ch.uzh.ifi.hase.soprafs26.websocketDTO;

public class roomMessageDTO {

    private String username;
    private Boolean host;

    public roomMessageDTO() {}

    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }
    public Boolean isHost() {
        return host;
    }
    public void setIsHost(Boolean host) {
        this.host = host;
    }
}
