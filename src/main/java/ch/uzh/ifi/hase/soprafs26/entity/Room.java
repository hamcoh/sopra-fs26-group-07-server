package ch.uzh.ifi.hase.soprafs26.entity;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;


import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "Room")
public class Room implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue
    private Long roomId;

    @Column(nullable = false, unique = true)
    private String roomJoinCode;

    @Column(nullable = false, unique = false)
    private int maxNumPlayers;

    @Column(nullable = false, unique = false)
    private int currentNumPlayers;

    @Column(nullable = false, unique = false)
    private boolean isRoomOpen;

    @Column(nullable = false, unique = false)
    private long hostUserId;
    
    @ElementCollection
    private Set<Long> playerIds = new HashSet<>();

    @Column(nullable = false, unique = false)
    private String gameDifficulty;

    @Column(nullable = false, unique = false)
    private String gameLanguage;

    @Column(nullable = false, unique = false)
    private String gameMode;

    @Column(nullable = false, unique = false)
    private int maxSkips;

    @Column(nullable = false, unique = false)
    private int timeLimitSeconds;

    @Column(nullable = false, unique = false)
    private int numOfProblems;

    public Long getRoomId() {
        return roomId;
    }

    public void setRoomId(Long roomId) {
        this.roomId = roomId;
    }

    public String getRoomJoinCode() {
        return roomJoinCode;
    }   

    public void setRoomJoinCode(String roomJoinCode) {
        this.roomJoinCode = roomJoinCode;
    }

    public int getMaxNumPlayers() {
        return maxNumPlayers;
    }

    public void setMaxNumPlayers(int maxNumPlayers) {
        this.maxNumPlayers = maxNumPlayers;
    }

    public int getCurrentNumPlayers() {
        return currentNumPlayers;
    }

    public void setCurrentNumPlayers(int currentNumPlayers) {
        this.currentNumPlayers = currentNumPlayers;
    }

    public boolean isRoomOpen() {
        return isRoomOpen;
    }

    public void setRoomOpen(boolean roomOpen) {
        isRoomOpen = roomOpen;
    }

    public long getHostUserId() {
        return hostUserId;
    }

    public void setHostUserId(long id) {
        this.hostUserId = id;
    }

    public Set<Long> getPlayerIds() {
        return playerIds;
    }

    public void setPlayerIds(Set<Long> playerIds) {
        this.playerIds = playerIds;
    }

    public String getGameDifficulty() {
        return gameDifficulty;
    }

    public void setGameDifficulty(String gameDifficulty) {
        this.gameDifficulty = gameDifficulty;
    }

    public String getGameLanguage() {
        return gameLanguage;
    }

    public void setGameLanguage(String gameLanguage) {
        this.gameLanguage = gameLanguage;
    }

    public String getGameMode() {
        return gameMode;
    }

    public void setGameMode(String gameMode) {
        this.gameMode = gameMode;
    }

    public int getMaxSkips() {
        return maxSkips;
    }

    public void setMaxSkips(int maxSkips) {
        this.maxSkips = maxSkips;
    }

    public int getTimeLimitSeconds() {
        return timeLimitSeconds;
    }

    public void setTimeLimitSeconds(int timeLimitSeconds) {
        this.timeLimitSeconds = timeLimitSeconds;
    }

    public int getNumOfProblems() {
        return numOfProblems;
    }

    public void setNumOfProblems(int numOfProblems) {
        this.numOfProblems = numOfProblems;
    }

}