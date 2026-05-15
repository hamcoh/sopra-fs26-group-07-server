package ch.uzh.ifi.hase.soprafs26.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import ch.uzh.ifi.hase.soprafs26.constant.GameDifficulty;
import ch.uzh.ifi.hase.soprafs26.constant.GameLanguage;
import ch.uzh.ifi.hase.soprafs26.constant.GameMode;
import ch.uzh.ifi.hase.soprafs26.repository.SubmissionRepository;
import ch.uzh.ifi.hase.soprafs26.rest.dto.GameStatsDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.PlayerWrappedDTO;
import jakarta.transaction.Transactional;

@Service
@Transactional
public class GameStatsService {

    private final Logger log = LoggerFactory.getLogger(GameStatsService.class);
    private final SubmissionRepository submissionRepository;

    public GameStatsService(SubmissionRepository submissionRepository) {
        this.submissionRepository = submissionRepository;
    }


    public List<GameStatsDTO> getHardestProblemsAndPlayerResults(Long userId) {

        List<GameStatsDTO> hardestProblems = getHardestProblemsList();
        List<GameStatsDTO> hardestProblemsAndPlayerResults = getPlayerStats(userId, hardestProblems);

        return hardestProblemsAndPlayerResults;
    }

    public List<GameStatsDTO> getMostPopularProblemsAndPlayerResults(Long userId) {

        List<GameStatsDTO> mostPopularProblems = getMostPopularProblemsList();
        List<GameStatsDTO> mostPopularProblemsAndPlayerResults = getPlayerStats(userId, mostPopularProblems);

        return mostPopularProblemsAndPlayerResults;
    }

    private List<GameStatsDTO> getHardestProblemsList() {

        List<Object[]> hardestProblems = submissionRepository.findTopHardestProblems();

        if (hardestProblems.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Statistics unavailable: no problem has been played at least 3 times!");
        }

        log.info("Retrieved problems: {}", hardestProblems.size());

        List<GameStatsDTO> gameStatsDTOList = prepareGameStatsDTOList(hardestProblems);

        return gameStatsDTOList;
    }

    public PlayerWrappedDTO getGameplaySummary(Long userId) {

        Object[] playerWrappedStats = getPlayerWrappedStats(userId);
        List<String> playerEnumStats = getPlayerEnumStats(userId);

        PlayerWrappedDTO playerWrappedDTO = new PlayerWrappedDTO();
        playerWrappedDTO.setUsername((String) playerWrappedStats[0]);
        playerWrappedDTO.setTotalGamesPlayed(((Number) playerWrappedStats[1]).intValue());
        playerWrappedDTO.setWinCount(((Number) playerWrappedStats[2]).intValue());
        playerWrappedDTO.setPlayerSumPassedTestCases(((Number) playerWrappedStats[3]).longValue());
        playerWrappedDTO.setPlayerSumTotalTestCases(((Number) playerWrappedStats[4]).longValue());
        playerWrappedDTO.setTotalProblemsSolvedFullyCorrect(((Number) playerWrappedStats[5]).intValue());
        playerWrappedDTO.setPercentileRank(((Number) playerWrappedStats[6]).doubleValue());

        playerWrappedDTO.setFavGameLanguage(playerEnumStats.get(0) != null ? GameLanguage.valueOf(playerEnumStats.get(0)) : null);
        playerWrappedDTO.setFavGameDifficulty(playerEnumStats.get(1) != null ? GameDifficulty.valueOf(playerEnumStats.get(1)) : null);
        playerWrappedDTO.setFavGameMode(playerEnumStats.get(2) != null ? GameMode.valueOf(playerEnumStats.get(2)) : null);

        return playerWrappedDTO;
    }

    public Object[] getPlayerWrappedStats(Long userId) {

        List<Object[]> playerWrappedStats = submissionRepository.findPlayerWrappedStats(userId);

        if (playerWrappedStats == null || playerWrappedStats.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Statistics unavailable: player has no submissions yet!");
        }

        return playerWrappedStats.get(0);
    }

    public List<String> getPlayerEnumStats(Long userId) {

        List<Object[]> playerEnumStats = submissionRepository.findPlayerEnumStats(userId);

        String favouriteGameLanguage = getMostFrequent(playerEnumStats, 0);
        String favouriteGameDifficulty = getMostFrequent(playerEnumStats, 1);
        String favouriteGameMode = getMostFrequent(playerEnumStats, 2);

        return Arrays.asList(favouriteGameLanguage, favouriteGameDifficulty, favouriteGameMode);
    }

    private List<GameStatsDTO> getMostPopularProblemsList() {

        List<Object[]> mostPopularProblems = submissionRepository.findMostPopularProblems();

        if (mostPopularProblems.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Statistics unavailable: no problem has been played yet!");
        }

        log.info("Retrieved problems: {}", mostPopularProblems.size());

        List<GameStatsDTO> gameStatsDTOList = prepareGameStatsDTOList(mostPopularProblems);

        return gameStatsDTOList;
    }

    private List<GameStatsDTO> getPlayerStats(Long userId, List<GameStatsDTO> gameStatsDTOList) {
        
        List<Long> problemIds = gameStatsDTOList.stream().map(GameStatsDTO::getProblemId).toList();
        
        List<Object[]> playerStats = submissionRepository.findPlayerStatsForProblems(userId, problemIds);

        Map<Long, Object[]> playerStatsMap = playerStats.stream().collect(Collectors.toMap(row -> ((Number) row[0]).longValue(), row -> row));

        for (GameStatsDTO gsDTO : gameStatsDTOList) {
            Object[] problemStats = playerStatsMap.get(gsDTO.getProblemId());
            if (problemStats != null) {
                log.info("Player={} has stats for problemId={}", userId, gsDTO.getProblemId());
                gsDTO.setPlayerSumPassedTestCases(((Number) problemStats[1]).longValue());
                gsDTO.setPlayerSumTotalTestCases(((Number) problemStats[2]).longValue());
                gsDTO.setPlayerSuccessRate(problemStats[3] != null ? ((Number) problemStats[3]).doubleValue() : 0.0);
            }
        }

        return gameStatsDTOList;
    }

    private List<GameStatsDTO> prepareGameStatsDTOList(List<Object[]> objectList) {

        List<GameStatsDTO> gameStatsDTOList = new ArrayList<>();

        for (Object[] obj : objectList) {

            GameStatsDTO gameStatsDTO = new GameStatsDTO();
            gameStatsDTO.setProblemId(((Number) obj[0]).longValue());
            gameStatsDTO.setTitle((String) obj[1]);
            gameStatsDTO.setDescription((String) obj[2]);
            gameStatsDTO.setGameLanguage(GameLanguage.valueOf(obj[3].toString()));
            gameStatsDTO.setSumPassedTestCases(((Number) obj[4]).longValue());
            gameStatsDTO.setSumTotalTestCases(((Number) obj[5]).longValue());
            gameStatsDTO.setTotalSubmissionCount(((Number) obj[6]).longValue());
            gameStatsDTO.setTotalSuccessRate(obj[7] != null ? ((Number) obj[7]).doubleValue() : 0.0);
            gameStatsDTOList.add(gameStatsDTO);
        }

        return gameStatsDTOList;
    }
    /*
    Helper method to extract the most frequent element within a column
     */
    private String getMostFrequent(List<Object[]> rows, int index) {
        return rows.stream()
        .map(row -> row[index].toString())
        .collect(Collectors.groupingBy(v -> v, Collectors.counting()))
        .entrySet().stream()
        .max(Map.Entry.comparingByValue())
        .map(Map.Entry::getKey)
        .orElse(null);
    }
} 

