package ch.uzh.ifi.hase.soprafs26.rest.dto;

import ch.uzh.ifi.hase.soprafs26.constant.GameDifficulty;
import ch.uzh.ifi.hase.soprafs26.constant.GameLanguage;
import ch.uzh.ifi.hase.soprafs26.constant.GameMode;

public class PlayerWrappedDTO {

    private String username;
    private int totalGamesPlayed;
    private int winCount;

    private Long playerSumPassedTestCases;
    private Long playerSumTotalTestCases;
    private Integer totalProblemsSolvedFullyCorrect;
    private Double percentileRank;

    private GameLanguage favGameLanguage;
    private GameDifficulty favGameDifficulty;
    private GameMode favGameMode;

    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }
    public int getTotalGamesPlayed() {
        return totalGamesPlayed;
    }
    public void setTotalGamesPlayed(int totalGamesPlayed) {
        this.totalGamesPlayed = totalGamesPlayed;
    }
    public int getWinCount() {
        return winCount;
    }
    public void setWinCount(int winCount) {
        this.winCount = winCount;
    }
    public Long getPlayerSumPassedTestCases() {
        return playerSumPassedTestCases;
    }
    public void setPlayerSumPassedTestCases(Long playerSumPassedTestCases) {
        this.playerSumPassedTestCases = playerSumPassedTestCases;
    }
    public Long getPlayerSumTotalTestCases() {
        return playerSumTotalTestCases;
    }
    public void setPlayerSumTotalTestCases(Long playerSumTotalTestCases) {
        this.playerSumTotalTestCases = playerSumTotalTestCases;
    }
    public Integer getTotalProblemsSolvedFullyCorrect() {
        return totalProblemsSolvedFullyCorrect;
    }
    public void setTotalProblemsSolvedFullyCorrect(Integer totalProblemsSolvedFullyCorrect) {
        this.totalProblemsSolvedFullyCorrect = totalProblemsSolvedFullyCorrect;
    }
    public Double getPercentileRank() {
        return percentileRank;
    }
    public void setPercentileRank(Double percentileRank) {
        this.percentileRank = percentileRank;
    }
    public GameLanguage getFavGameLanguage() {
        return favGameLanguage;
    }
    public void setFavGameLanguage(GameLanguage favGameLanguage) {
        this.favGameLanguage = favGameLanguage;
    }
    public GameDifficulty getFavGameDifficulty() {
        return favGameDifficulty;
    }
    public void setFavGameDifficulty(GameDifficulty favGameDifficulty) {
        this.favGameDifficulty = favGameDifficulty;
    }
    public GameMode getFavGameMode() {
        return favGameMode;
    }
    public void setFavGameMode(GameMode favGameMode) {
        this.favGameMode = favGameMode;
    }
}
