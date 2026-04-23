package ch.uzh.ifi.hase.soprafs26.rest.dto;

import java.util.Date;

import ch.uzh.ifi.hase.soprafs26.constant.UserStatus;

public class UserDTO {
    
    private Long id;
    private String token;
    private String username;
    private String bio;
    private UserStatus status;
    private Date creationDate;
    private int winCount;
    private double winRatePercentage;
    private int totalGamesPlayed;
    private long totalPoints;
    private int avatarId;

    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public String getToken() {
        return token;
    }
    public void setToken(String token) {
        this.token = token;
    }
    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }
    public String getBio() {
        return bio;
    }
    public void setBio(String bio) {
        this.bio = bio;
    }
    public UserStatus getStatus() {
        return status;
    }
    public void setStatus(UserStatus status) {
        this.status = status;
    }
    public Date getCreationDate() {
        return creationDate;
    }
    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }
    public int getWinCount() {
        return winCount;
    }
    public void setWinCount(int winCount) {
        this.winCount = winCount;
    }
    public double getWinRatePercentage() {
        return winRatePercentage;
    }
    public void setWinRatePercentage(double winRatePercentage) {
        this.winRatePercentage = winRatePercentage;
    }
    public int getTotalGamesPlayed() {
        return totalGamesPlayed;
    }
    public void setTotalGamesPlayed(int totalGamesPlayed) {
        this.totalGamesPlayed = totalGamesPlayed;
    }
    public long getTotalPoints() {
        return totalPoints;
    }
    public void setTotalPoints(long totalPoints) {
        this.totalPoints = totalPoints;
    }
    public int getAvatarId() {
        return avatarId;
    }
    public void setAvatarId(int avatarId) {
        this.avatarId = avatarId;
    }
}
