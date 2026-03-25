package ch.uzh.ifi.hase.soprafs26.rest.dto;

public class RoomPostDTO {

    private int maxNumPlayers;
    private String gameDifficulty;
    private String gameLanguage;
    private String gameMode;
    private Integer maxSkips;
    private Integer timeLimitSeconds;
    private Integer numOfProblems;

    public int getMaxNumPlayers() {
        return maxNumPlayers;
    }

    public void setMaxNumPlayers(int maxNumPlayers) {
        this.maxNumPlayers = maxNumPlayers;
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