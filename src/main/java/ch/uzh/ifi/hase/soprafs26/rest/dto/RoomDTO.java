package ch.uzh.ifi.hase.soprafs26.rest.dto;

import ch.uzh.ifi.hase.soprafs26.constant.GameDifficulty;
import ch.uzh.ifi.hase.soprafs26.constant.GameLanguage;
import ch.uzh.ifi.hase.soprafs26.constant.GameMode;

import java.util.Set;

public class RoomDTO {

    private Long roomId;
    private String roomJoinCode;
    private int maxNumPlayers;
    private int currentNumPlayers;
    private boolean isRoomOpen;
    private Long hostUserId;
    private Set<Long> playerIds;
    private GameDifficulty gameDifficulty;
    private GameLanguage gameLanguage;
    private GameMode gameMode;
    private Integer maxSkips;
    private Integer timeLimitSeconds;
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

    public boolean getIsRoomOpen() {
        return isRoomOpen;
    }

    public void setIsRoomOpen(boolean isRoomOpen) {
        this.isRoomOpen = isRoomOpen;
    }

    public Long getHostUserId() {
        return hostUserId;
    }

    public void setHostUserId(Long hostUserId) {
        this.hostUserId = hostUserId;
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