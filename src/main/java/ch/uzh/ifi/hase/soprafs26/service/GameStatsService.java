package ch.uzh.ifi.hase.soprafs26.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import ch.uzh.ifi.hase.soprafs26.repository.SubmissionRepository;
import ch.uzh.ifi.hase.soprafs26.rest.dto.GameStatsDTO;
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
    

    private List<GameStatsDTO> getHardestProblemsList() {

        List<Object[]> hardestProblems = submissionRepository.findTopHardestProblems();

        if (hardestProblems.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Statistics unavailable: no problem has been played at least 3 times!");
        }

        log.info("Retrieved problems: {}", hardestProblems.size());

        List<GameStatsDTO> gameStatsDTOList = new ArrayList<>();

        for (Object[] hp : hardestProblems) {

            GameStatsDTO gameStatsDTO = new GameStatsDTO();
            gameStatsDTO.setProblemId(((Number) hp[0]).longValue());
            gameStatsDTO.setTitle((String) hp[1]);
            gameStatsDTO.setDescription((String) hp[2]);
            gameStatsDTO.setSumPassedTestCases(((Number) hp[3]).longValue());
            gameStatsDTO.setSumTotalTestCases(((Number) hp[4]).longValue());
            gameStatsDTO.setTotalSubmissionCount(((Number) hp[5]).longValue());
            gameStatsDTO.setTotalSuccessRate(hp[6] != null ? ((Number) hp[6]).doubleValue() : 0.0);
            gameStatsDTOList.add(gameStatsDTO);
        }

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
} 

