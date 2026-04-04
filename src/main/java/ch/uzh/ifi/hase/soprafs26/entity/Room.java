package ch.uzh.ifi.hase.soprafs26.entity;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;


import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import ch.uzh.ifi.hase.soprafs26.constant.GameDifficulty;
import ch.uzh.ifi.hase.soprafs26.constant.GameLanguage;
import ch.uzh.ifi.hase.soprafs26.constant.GameMode;

@Entity
@Table(name = "rooms")
public class Room implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue
    private Long roomId;

    @Column(nullable = false, unique = true, updatable = false) // set 'updatable = false' to all fields that once host/system sets them should never change
    private String roomJoinCode;

    @Column(nullable = false, unique = false, updatable = false)
    private int maxNumPlayers;

    @Column(nullable = false, unique = false)
    private int currentNumPlayers;

    @Column(nullable = false, unique = false)
    private boolean isRoomOpen;

    @Column(nullable = false, unique = false, updatable = false)
    private long hostUserId;
    
    @ElementCollection
    private Set<Long> playerIds = new HashSet<>();

    @Enumerated(EnumType.STRING) //tells JPA to display it as string (less cryptic than 0/1)
    @Column(nullable = false, unique = false, updatable = false)
    private GameDifficulty gameDifficulty;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, unique = false, updatable = false)
    private GameLanguage gameLanguage;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, unique = false, updatable = false)
    private GameMode gameMode;

    /**own: fields maxSkips, timeLimitSeconds, numOfProblems are optional fields (at least atm), at the moment
     * they are not sent from frontend (since they also depend on game-mode). If, however, we do not receive any data from
     * frontend and do not set anything in backend for those fields, JPA will save those values as '0', since before they were
     * declared as primitives. We need to talk about this point in general.
    */

    @Column(nullable = true, unique = false, updatable = false) 
    private Integer maxSkips;

    @Column(nullable = true, unique = false, updatable = false) 
    private Integer timeLimitSeconds;

    @Column(nullable = true, unique = false, updatable = false) 
    private Integer numOfProblems;

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

    public GameDifficulty getGameDifficulty() {
        return gameDifficulty;
    }

    public void setGameDifficulty(GameDifficulty gameDifficulty) {
        this.gameDifficulty = gameDifficulty;
    }


    public GameLanguage getGameLanguage() {
        return gameLanguage;
    }

    public void setGameLanguage(GameLanguage gameLanguage) {
        this.gameLanguage = gameLanguage;
    }

    public GameMode getGameMode() {
        return gameMode;
    }

    public void setGameMode(GameMode gameMode) {
        this.gameMode = gameMode;
    }

    public Integer getMaxSkips() {
        return maxSkips;
    }

    public void setMaxSkips(Integer maxSkips) {
        this.maxSkips = maxSkips;
    }

    public Integer getTimeLimitSeconds() {
        return timeLimitSeconds;
    }

    public void setTimeLimitSeconds(Integer timeLimitSeconds) {
        this.timeLimitSeconds = timeLimitSeconds;
    }

    public Integer getNumOfProblems() {
        return numOfProblems;
    }

    public void setNumOfProblems(Integer numOfProblems) {
        this.numOfProblems = numOfProblems;
    }

}