package ch.uzh.ifi.hase.soprafs26.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import ch.uzh.ifi.hase.soprafs26.constant.GameLanguage;
import ch.uzh.ifi.hase.soprafs26.constant.GameDifficulty;
import ch.uzh.ifi.hase.soprafs26.constant.GameMode;
import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.repository.SubmissionRepository;
import ch.uzh.ifi.hase.soprafs26.rest.dto.GameStatsDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.PlayerWrappedDTO;


@ExtendWith(MockitoExtension.class)
class GameStatsServiceTest {

    @Mock
    private SubmissionRepository submissionRepository;

    @InjectMocks
    private GameStatsService gameStatsService;

    private User testUser;
    private List<Object[]> mockProblemsObjects;
    private List<Object[]> mockPlayerStatsObjects;

    @BeforeEach
	void setup() {

		testUser = new User();
		testUser.setId(1L);
		testUser.setUsername("testUsername");
		testUser.setPassword("testPassword");
        testUser.setAvatarId(5);

        mockProblemsObjects = new ArrayList<>();
        mockProblemsObjects.add(new Object[]{1L, "testProblem", "testDescription", GameLanguage.PYTHON.toString(), 10L, 20L, 2L, 50.0});

        mockPlayerStatsObjects = new ArrayList<>();
        mockPlayerStatsObjects.add(new Object[]{1L, 8L, 10L, 80.0});


	}


    @Test
    void getHardestProblemsAndPlayerResults_validInputs_success() {

        when(submissionRepository.findTopHardestProblems()).thenReturn(mockProblemsObjects);
        when(submissionRepository.findPlayerStatsForProblems(Mockito.any(), Mockito.any())).thenReturn(mockPlayerStatsObjects);

        List<GameStatsDTO> result = gameStatsService.getHardestProblemsAndPlayerResults(testUser.getId());

        Object[] mockProblem = mockProblemsObjects.get(0);
        Object[] mockPlayerStats = mockPlayerStatsObjects.get(0);

        assertEquals(1, result.size());
        assertEquals(((Number) mockProblem[0]).longValue(), result.get(0).getProblemId());
        assertEquals((String) mockProblem[1], result.get(0).getTitle());
        assertEquals((String) mockProblem[2], result.get(0).getDescription());
        assertEquals(GameLanguage.valueOf((String) mockProblem[3]), result.get(0).getGameLanguage());
        assertEquals(((Number) mockProblem[4]).longValue(), result.get(0).getSumPassedTestCases());
        assertEquals(((Number) mockProblem[5]).longValue(), result.get(0).getSumTotalTestCases());
        assertEquals(((Number) mockProblem[6]).longValue(), result.get(0).getTotalSubmissionCount());
        assertNotNull(result.get(0).getTotalSuccessRate());
        assertEquals(((Number) mockPlayerStats[1]).longValue(), result.get(0).getPlayerSumPassedTestCases());
        assertEquals(((Number) mockPlayerStats[2]).longValue(), result.get(0).getPlayerSumTotalTestCases());
        assertNotNull(result.get(0).getPlayerSuccessRate());
    }

    @Test
    void getHardestProblemsAndPlayerResults_multipleProblems_success() {

        mockProblemsObjects.add(new Object[]{2L, "testProblem2", "testDescription2", GameLanguage.PYTHON.toString(), 7L, 70L, 10L, 10.0});
        mockPlayerStatsObjects.add(new Object[]{2L, 1L, 20L, 5.0});

        when(submissionRepository.findTopHardestProblems()).thenReturn(mockProblemsObjects);
        when(submissionRepository.findPlayerStatsForProblems(Mockito.any(), Mockito.any())).thenReturn(mockPlayerStatsObjects);

        List<GameStatsDTO> result = gameStatsService.getHardestProblemsAndPlayerResults(testUser.getId());

        Object[] mockProblem1 = mockProblemsObjects.get(0);
        Object[] mockProblem2 = mockProblemsObjects.get(1);
        Object[] mockPlayerStatsProblem1 = mockPlayerStatsObjects.get(0);
        Object[] mockPlayerStatsProblem2 = mockPlayerStatsObjects.get(1);

        assertEquals(2, result.size());
        assertEquals(((Number) mockProblem1[0]).longValue(), result.get(0).getProblemId());
        assertEquals((String) mockProblem1[1], result.get(0).getTitle());
        assertEquals((String) mockProblem1[2], result.get(0).getDescription());
        assertEquals(GameLanguage.valueOf((String) mockProblem1[3]), result.get(0).getGameLanguage());
        assertEquals(((Number) mockProblem1[4]).longValue(), result.get(0).getSumPassedTestCases());
        assertEquals(((Number) mockProblem1[5]).longValue(), result.get(0).getSumTotalTestCases());
        assertEquals(((Number) mockProblem1[6]).longValue(), result.get(0).getTotalSubmissionCount());
        assertNotNull(result.get(0).getTotalSuccessRate());
        assertEquals(((Number) mockPlayerStatsProblem1[1]).longValue(), result.get(0).getPlayerSumPassedTestCases());
        assertEquals(((Number) mockPlayerStatsProblem1[2]).longValue(), result.get(0).getPlayerSumTotalTestCases());
        assertNotNull(result.get(0).getPlayerSuccessRate());

        assertEquals(((Number) mockProblem2[0]).longValue(), result.get(1).getProblemId());
        assertEquals((String) mockProblem2[1], result.get(1).getTitle());
        assertEquals((String) mockProblem2[2], result.get(1).getDescription());
        assertEquals(GameLanguage.valueOf((String) mockProblem2[3]), result.get(1).getGameLanguage());
        assertEquals(((Number) mockProblem2[4]).longValue(), result.get(1).getSumPassedTestCases());
        assertEquals(((Number) mockProblem2[5]).longValue(), result.get(1).getSumTotalTestCases());
        assertEquals(((Number) mockProblem2[6]).longValue(), result.get(1).getTotalSubmissionCount());
        assertNotNull(result.get(1).getTotalSuccessRate());
        assertEquals(((Number) mockPlayerStatsProblem2[1]).longValue(), result.get(1).getPlayerSumPassedTestCases());
        assertEquals(((Number) mockPlayerStatsProblem2[2]).longValue(), result.get(1).getPlayerSumTotalTestCases());
        assertNotNull(result.get(1).getPlayerSuccessRate());
    }

    @Test
    void getHardestProblemsAndPlayerResults_noProblems_throwsNotFound() {
        when(submissionRepository.findTopHardestProblems()).thenReturn(List.of());

        assertThrows(ResponseStatusException.class,
                () -> gameStatsService.getHardestProblemsAndPlayerResults(testUser.getId()));
    }


    @Test
    void getHardestProblemsAndPlayerResults_userHasNotPlayedOneProblemYet_relevantStatsFieldsAreNull() {

        //testUser is mocked to not have played problem with problemId=2
        mockProblemsObjects.add(new Object[]{2L, "testProblem2", "testDescription2", GameLanguage.PYTHON.toString(), 7L, 70L, 10L, 10.0});

        when(submissionRepository.findTopHardestProblems()).thenReturn(mockProblemsObjects);
        when(submissionRepository.findPlayerStatsForProblems(Mockito.any(), Mockito.any())).thenReturn(mockPlayerStatsObjects);

        List<GameStatsDTO> result = gameStatsService.getHardestProblemsAndPlayerResults(testUser.getId());

        Object[] mockPlayerStatsProblem1 = mockPlayerStatsObjects.get(0);

        //testUser has stats for one problem
        assertEquals(((Number) mockPlayerStatsProblem1[1]).longValue(), result.get(0).getPlayerSumPassedTestCases());
        assertEquals(((Number) mockPlayerStatsProblem1[2]).longValue(), result.get(0).getPlayerSumTotalTestCases());
        assertNotNull(result.get(0).getPlayerSuccessRate());

        //testUser has no stats for problem below
        assertNull(result.get(1).getPlayerSumPassedTestCases());
        assertNull(result.get(1).getPlayerSumTotalTestCases());
        assertNull(result.get(1).getPlayerSuccessRate());
    }

    @Test
    void getMostPopularProblemsAndPlayerResults_validInputs_success() {

        when(submissionRepository.findMostPopularProblems()).thenReturn(mockProblemsObjects);
        when(submissionRepository.findPlayerStatsForProblems(Mockito.any(), Mockito.any())).thenReturn(mockPlayerStatsObjects);

        List<GameStatsDTO> result = gameStatsService.getMostPopularProblemsAndPlayerResults(testUser.getId());

        Object[] mockProblem = mockProblemsObjects.get(0);
        Object[] mockPlayerStats = mockPlayerStatsObjects.get(0);

        assertEquals(1, result.size());
        assertEquals(((Number) mockProblem[0]).longValue(), result.get(0).getProblemId());
        assertEquals((String) mockProblem[1], result.get(0).getTitle());
        assertEquals((String) mockProblem[2], result.get(0).getDescription());
        assertEquals(GameLanguage.valueOf((String) mockProblem[3]), result.get(0).getGameLanguage());
        assertEquals(((Number) mockProblem[4]).longValue(), result.get(0).getSumPassedTestCases());
        assertEquals(((Number) mockProblem[5]).longValue(), result.get(0).getSumTotalTestCases());
        assertEquals(((Number) mockProblem[6]).longValue(), result.get(0).getTotalSubmissionCount());
        assertNotNull(result.get(0).getTotalSuccessRate());
        assertEquals(((Number) mockPlayerStats[1]).longValue(), result.get(0).getPlayerSumPassedTestCases());
        assertEquals(((Number) mockPlayerStats[2]).longValue(), result.get(0).getPlayerSumTotalTestCases());
        assertNotNull(result.get(0).getPlayerSuccessRate());
    }

    @Test
    void getMostPopularProblemsAndPlayerResults_multipleProblems_success() {

        mockProblemsObjects.add(new Object[]{2L, "testProblem2", "testDescription2", GameLanguage.PYTHON.toString(), 7L, 70L, 10L, 10.0});
        mockPlayerStatsObjects.add(new Object[]{2L, 1L, 20L, 5.0});

        when(submissionRepository.findMostPopularProblems()).thenReturn(mockProblemsObjects);
        when(submissionRepository.findPlayerStatsForProblems(Mockito.any(), Mockito.any())).thenReturn(mockPlayerStatsObjects);

        List<GameStatsDTO> result = gameStatsService.getMostPopularProblemsAndPlayerResults(testUser.getId());

        Object[] mockProblem1 = mockProblemsObjects.get(0);
        Object[] mockProblem2 = mockProblemsObjects.get(1);
        Object[] mockPlayerStatsProblem1 = mockPlayerStatsObjects.get(0);
        Object[] mockPlayerStatsProblem2 = mockPlayerStatsObjects.get(1);

        assertEquals(2, result.size());
        assertEquals(((Number) mockProblem1[0]).longValue(), result.get(0).getProblemId());
        assertEquals((String) mockProblem1[1], result.get(0).getTitle());
        assertEquals((String) mockProblem1[2], result.get(0).getDescription());
        assertEquals(GameLanguage.valueOf((String) mockProblem1[3]), result.get(0).getGameLanguage());
        assertEquals(((Number) mockProblem1[4]).longValue(), result.get(0).getSumPassedTestCases());
        assertEquals(((Number) mockProblem1[5]).longValue(), result.get(0).getSumTotalTestCases());
        assertEquals(((Number) mockProblem1[6]).longValue(), result.get(0).getTotalSubmissionCount());
        assertNotNull(result.get(0).getTotalSuccessRate());
        assertEquals(((Number) mockPlayerStatsProblem1[1]).longValue(), result.get(0).getPlayerSumPassedTestCases());
        assertEquals(((Number) mockPlayerStatsProblem1[2]).longValue(), result.get(0).getPlayerSumTotalTestCases());
        assertNotNull(result.get(0).getPlayerSuccessRate());

        assertEquals(((Number) mockProblem2[0]).longValue(), result.get(1).getProblemId());
        assertEquals((String) mockProblem2[1], result.get(1).getTitle());
        assertEquals((String) mockProblem2[2], result.get(1).getDescription());
        assertEquals(GameLanguage.valueOf((String) mockProblem2[3]), result.get(1).getGameLanguage());
        assertEquals(((Number) mockProblem2[4]).longValue(), result.get(1).getSumPassedTestCases());
        assertEquals(((Number) mockProblem2[5]).longValue(), result.get(1).getSumTotalTestCases());
        assertEquals(((Number) mockProblem2[6]).longValue(), result.get(1).getTotalSubmissionCount());
        assertNotNull(result.get(1).getTotalSuccessRate());
        assertEquals(((Number) mockPlayerStatsProblem2[1]).longValue(), result.get(1).getPlayerSumPassedTestCases());
        assertEquals(((Number) mockPlayerStatsProblem2[2]).longValue(), result.get(1).getPlayerSumTotalTestCases());
        assertNotNull(result.get(1).getPlayerSuccessRate());
    }

    @Test
    void getMostPopularProblemsAndPlayerResults_noProblems_throwsNotFound() {
        when(submissionRepository.findMostPopularProblems()).thenReturn(List.of());

        assertThrows(ResponseStatusException.class,
                () -> gameStatsService.getMostPopularProblemsAndPlayerResults(testUser.getId()));
    }

    @Test
    void getGameplaySummary_validInputs_success() {
        
        Object[] mockWrappedStats = new Object[]{"username", 10, 5, 100L, 200L, 3, 75.0};
        List<Object[]> mockResult = new ArrayList<>();
        mockResult.add(mockWrappedStats);
        when(submissionRepository.findPlayerWrappedStats(1L)).thenReturn(mockResult);

        Object[] mockEnumRow1 = new Object[]{"JAVA", "EASY", "SPRINT_ARCADE"};
        Object[] mockEnumRow2 = new Object[]{"PYTHON", "HARD", "SPRINT_CLASSIC"};
        Object[] mockEnumRow3 = new Object[]{"PYTHON", "HARD", "SPRINT_CLASSIC"};

        List<Object[]> mockEnumResult = new ArrayList<>();
        mockEnumResult.add(mockEnumRow1);
        mockEnumResult.add(mockEnumRow2);
        mockEnumResult.add(mockEnumRow3);
        when(submissionRepository.findPlayerEnumStats(1L)).thenReturn(mockEnumResult);

        PlayerWrappedDTO result = gameStatsService.getGameplaySummary(1L);

        assertEquals("username", result.getUsername());
        assertEquals(10, result.getTotalGamesPlayed());
        assertEquals(5, result.getWinCount());
        assertEquals(100L, result.getPlayerSumPassedTestCases());
        assertEquals(200L, result.getPlayerSumTotalTestCases());
        assertEquals(3, result.getTotalProblemsSolvedFullyCorrect());
        assertEquals(75.0, result.getPercentileRank());
        assertEquals(GameLanguage.PYTHON, result.getFavGameLanguage());
        assertEquals(GameDifficulty.HARD, result.getFavGameDifficulty());
        assertEquals(GameMode.SPRINT_CLASSIC, result.getFavGameMode());
    }

    @Test
    void getGameplaySummary_noSubmissions_throwsNotFound() {
        when(submissionRepository.findPlayerWrappedStats(1L)).thenReturn(List.of());

        assertThrows(ResponseStatusException.class, () -> 
            gameStatsService.getGameplaySummary(1L));
    }
}
