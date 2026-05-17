package ch.uzh.ifi.hase.soprafs26.controller;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;


import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.web.server.ResponseStatusException;

import ch.uzh.ifi.hase.soprafs26.constant.GameLanguage;
import ch.uzh.ifi.hase.soprafs26.constant.GameDifficulty;
import ch.uzh.ifi.hase.soprafs26.constant.GameMode;
import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.rest.dto.GameStatsDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.PlayerWrappedDTO;
import ch.uzh.ifi.hase.soprafs26.service.GameStatsService;
import ch.uzh.ifi.hase.soprafs26.service.UserService;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;


@WebMvcTest(GameStatsController.class)
public class GameStatsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private GameStatsService gameStatsService;

    private User testUser;
    private GameStatsDTO gameStatsDTO1;
    private GameStatsDTO gameStatsDTO2;
    private GameStatsDTO gameStatsDTO3;
    private PlayerWrappedDTO testPlayerWrappedDTO;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);

        testUser = new User();
        testUser.setId(1L);
        testUser.setToken("validToken");

        gameStatsDTO1 = new GameStatsDTO();
        gameStatsDTO1.setProblemId(1L);
        gameStatsDTO1.setTitle("testProblem1");
        gameStatsDTO1.setDescription("testDescription1");
        gameStatsDTO1.setGameLanguage(GameLanguage.PYTHON);
        gameStatsDTO1.setSumPassedTestCases(9L);
        gameStatsDTO1.setSumTotalTestCases(18L);
        gameStatsDTO1.setTotalSubmissionCount(2L);
        gameStatsDTO1.setTotalSuccessRate((double) 9 / 18);
        gameStatsDTO1.setPlayerSumPassedTestCases(6L);
        gameStatsDTO1.setPlayerSumTotalTestCases(9L);
        gameStatsDTO1.setPlayerSuccessRate((double) 6 / 9);

        gameStatsDTO2 = new GameStatsDTO();
        gameStatsDTO2.setProblemId(2L);
        gameStatsDTO2.setTitle("testProblem2");
        gameStatsDTO2.setDescription("testDescription2");
        gameStatsDTO2.setGameLanguage(GameLanguage.PYTHON);
        gameStatsDTO2.setSumPassedTestCases(9L);
        gameStatsDTO2.setSumTotalTestCases(18L);
        gameStatsDTO2.setTotalSubmissionCount(2L);
        gameStatsDTO2.setTotalSuccessRate((double) 9 / 18);
        gameStatsDTO2.setPlayerSumPassedTestCases(6L);
        gameStatsDTO2.setPlayerSumTotalTestCases(9L);
        gameStatsDTO2.setPlayerSuccessRate((double) 6 / 9);

        gameStatsDTO3 = new GameStatsDTO();
        gameStatsDTO3.setProblemId(3L);
        gameStatsDTO3.setTitle("testProblem3");
        gameStatsDTO3.setDescription("testDescription3");
        gameStatsDTO3.setGameLanguage(GameLanguage.PYTHON);
        gameStatsDTO3.setSumPassedTestCases(1L);
        gameStatsDTO3.setSumTotalTestCases(180L);
        gameStatsDTO3.setTotalSubmissionCount(30L);
        gameStatsDTO3.setTotalSuccessRate((double) 1 / 180);

        testPlayerWrappedDTO = new PlayerWrappedDTO();
        testPlayerWrappedDTO.setUsername("testUser");
        testPlayerWrappedDTO.setTotalGamesPlayed(10);
        testPlayerWrappedDTO.setWinCount(5);
        testPlayerWrappedDTO.setPlayerSumPassedTestCases(50L);
        testPlayerWrappedDTO.setPlayerSumTotalTestCases(100L);
        testPlayerWrappedDTO.setTotalProblemsSolvedFullyCorrect(10);
        testPlayerWrappedDTO.setPercentileRank(50.0);
        testPlayerWrappedDTO.setFavGameLanguage(GameLanguage.PYTHON);
        testPlayerWrappedDTO.setFavGameDifficulty(GameDifficulty.HARD);
        testPlayerWrappedDTO.setFavGameMode(GameMode.SPRINT_ARCADE);

    }

    @Test
    void getMomentaryHardestProblems_validRequest_twoProblems_success() throws Exception {

        List<GameStatsDTO> gameStatsDTOs = List.of(gameStatsDTO1, gameStatsDTO2);

        doNothing().when(userService).verifyTokenAndUserId(testUser.getToken(), testUser.getId());
        Mockito.when(gameStatsService.getHardestProblemsAndPlayerResults(Mockito.any())).thenReturn(gameStatsDTOs);
        
        MockHttpServletRequestBuilder getRequest = get("/stats/hardest-problems")
                                                    .header("token", testUser.getToken())
                                                    .header("userId", testUser.getId())
                                                    .contentType(MediaType.APPLICATION_JSON);

        mockMvc.perform(getRequest)
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$", hasSize(2)))
                        .andExpect(jsonPath("$[0].description", is(gameStatsDTO1.getDescription())))
                        .andExpect(jsonPath("$[0].gameLanguage", is(gameStatsDTO1.getGameLanguage().toString())))
                        .andExpect(jsonPath("$[0].playerSuccessRate").exists())
                        .andExpect(jsonPath("$[0].playerSumPassedTestCases", is(gameStatsDTO1.getPlayerSumPassedTestCases().intValue())))
                        .andExpect(jsonPath("$[0].playerSumTotalTestCases", is(gameStatsDTO1.getPlayerSumTotalTestCases().intValue())))
                        .andExpect(jsonPath("$[0].problemId", is(gameStatsDTO1.getProblemId().intValue())))
                        .andExpect(jsonPath("$[0].sumPassedTestCases", is(gameStatsDTO1.getSumPassedTestCases().intValue())))
                        .andExpect(jsonPath("$[0].sumTotalTestCases", is(gameStatsDTO1.getSumTotalTestCases().intValue())))
                        .andExpect(jsonPath("$[0].title", is(gameStatsDTO1.getTitle())))
                        .andExpect(jsonPath("$[0].totalSubmissionCount", is(gameStatsDTO1.getTotalSubmissionCount().intValue())))
                        .andExpect(jsonPath("$[0].totalSuccessRate").exists())
                        .andExpect(jsonPath("$[1].description", is(gameStatsDTO2.getDescription())))
                        .andExpect(jsonPath("$[1].gameLanguage", is(gameStatsDTO2.getGameLanguage().toString())))
                        .andExpect(jsonPath("$[1].playerSuccessRate").exists())
                        .andExpect(jsonPath("$[1].playerSumPassedTestCases", is(gameStatsDTO2.getPlayerSumPassedTestCases().intValue())))
                        .andExpect(jsonPath("$[1].playerSumTotalTestCases", is(gameStatsDTO2.getPlayerSumTotalTestCases().intValue())))
                        .andExpect(jsonPath("$[1].problemId", is(gameStatsDTO2.getProblemId().intValue())))
                        .andExpect(jsonPath("$[1].sumPassedTestCases", is(gameStatsDTO2.getSumPassedTestCases().intValue())))
                        .andExpect(jsonPath("$[1].sumTotalTestCases", is(gameStatsDTO2.getSumTotalTestCases().intValue())))
                        .andExpect(jsonPath("$[1].title", is(gameStatsDTO2.getTitle())))
                        .andExpect(jsonPath("$[1].totalSubmissionCount", is(gameStatsDTO2.getTotalSubmissionCount().intValue())))
                        .andExpect(jsonPath("$[1].totalSuccessRate").exists());
                    }

    @Test
    void getMomentaryHardestProblems_playerDidNotPlay_statsAreNull_success() throws Exception {
        
        List<GameStatsDTO> gameStatsDTOs = List.of(gameStatsDTO3);

        doNothing().when(userService).verifyTokenAndUserId(testUser.getToken(), testUser.getId());
        Mockito.when(gameStatsService.getHardestProblemsAndPlayerResults(Mockito.any())).thenReturn(gameStatsDTOs);

        MockHttpServletRequestBuilder getRequest = get("/stats/hardest-problems")
                                                    .header("token", testUser.getToken())
                                                    .header("userId", testUser.getId())
                                                    .contentType(MediaType.APPLICATION_JSON);
        
        mockMvc.perform(getRequest)
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$", hasSize(1)))
                        .andExpect(jsonPath("$[0].description", is(gameStatsDTO3.getDescription())))
                        .andExpect(jsonPath("$[0].gameLanguage", is(gameStatsDTO3.getGameLanguage().toString())))
                        .andExpect(jsonPath("$[0].playerSuccessRate", is(nullValue())))
                        .andExpect(jsonPath("$[0].playerSumPassedTestCases", is(nullValue())))
                        .andExpect(jsonPath("$[0].playerSumTotalTestCases", is(nullValue())))
                        .andExpect(jsonPath("$[0].problemId", is(gameStatsDTO3.getProblemId().intValue())))
                        .andExpect(jsonPath("$[0].sumPassedTestCases", is(gameStatsDTO3.getSumPassedTestCases().intValue())))
                        .andExpect(jsonPath("$[0].sumTotalTestCases", is(gameStatsDTO3.getSumTotalTestCases().intValue())))
                        .andExpect(jsonPath("$[0].title", is(gameStatsDTO3.getTitle())))
                        .andExpect(jsonPath("$[0].totalSubmissionCount", is(gameStatsDTO3.getTotalSubmissionCount().intValue())))
                        .andExpect(jsonPath("$[0].totalSuccessRate").exists());
                    }

    @Test
    void getMomentaryHardestProblems_noProblemsToRetrieve_throwsNotFound() throws Exception {

        doNothing().when(userService).verifyTokenAndUserId(testUser.getToken(), testUser.getId());
        
        String errorReason = "Statistics unavailable: no problem has been played at least 3 times!";
		doThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, errorReason)).when(gameStatsService).getHardestProblemsAndPlayerResults(Mockito.any());

        MockHttpServletRequestBuilder getRequest = get("/stats/hardest-problems")
                                                    .header("token", testUser.getToken())
                                                    .header("userId", testUser.getId())
                                                    .contentType(MediaType.APPLICATION_JSON);
        
        mockMvc.perform(getRequest)
				        .andExpect(status().isNotFound())
				        .andExpect(jsonPath("$.detail", is(errorReason)));
                    }

    @Test
    void getMomentaryMostPopularProblems_validRequest_twoProblems_success() throws Exception {
                
        List<GameStatsDTO> gameStatsDTOs = List.of(gameStatsDTO1, gameStatsDTO2);

        doNothing().when(userService).verifyTokenAndUserId(testUser.getToken(), testUser.getId());
        Mockito.when(gameStatsService.getMostPopularProblemsAndPlayerResults(Mockito.any())).thenReturn(gameStatsDTOs);
        
        MockHttpServletRequestBuilder getRequest = get("/stats/popular-problems")
                                                    .header("token", testUser.getToken())
                                                    .header("userId", testUser.getId())
                                                    .contentType(MediaType.APPLICATION_JSON);
        
        mockMvc.perform(getRequest)
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$", hasSize(2)))
                        .andExpect(jsonPath("$[0].description", is(gameStatsDTO1.getDescription())))
                        .andExpect(jsonPath("$[0].gameLanguage", is(gameStatsDTO1.getGameLanguage().toString())))
                        .andExpect(jsonPath("$[0].playerSuccessRate").exists())
                        .andExpect(jsonPath("$[0].playerSumPassedTestCases", is(gameStatsDTO1.getPlayerSumPassedTestCases().intValue())))
                        .andExpect(jsonPath("$[0].playerSumTotalTestCases", is(gameStatsDTO1.getPlayerSumTotalTestCases().intValue())))
                        .andExpect(jsonPath("$[0].problemId", is(gameStatsDTO1.getProblemId().intValue())))
                        .andExpect(jsonPath("$[0].sumPassedTestCases", is(gameStatsDTO1.getSumPassedTestCases().intValue())))
                        .andExpect(jsonPath("$[0].sumTotalTestCases", is(gameStatsDTO1.getSumTotalTestCases().intValue())))
                        .andExpect(jsonPath("$[0].title", is(gameStatsDTO1.getTitle())))
                        .andExpect(jsonPath("$[0].totalSubmissionCount", is(gameStatsDTO1.getTotalSubmissionCount().intValue())))
                        .andExpect(jsonPath("$[0].totalSuccessRate").exists())
                        .andExpect(jsonPath("$[1].description", is(gameStatsDTO2.getDescription())))
                        .andExpect(jsonPath("$[1].gameLanguage", is(gameStatsDTO2.getGameLanguage().toString())))
                        .andExpect(jsonPath("$[1].playerSuccessRate").exists())
                        .andExpect(jsonPath("$[1].playerSumPassedTestCases", is(gameStatsDTO2.getPlayerSumPassedTestCases().intValue())))
                        .andExpect(jsonPath("$[1].playerSumTotalTestCases", is(gameStatsDTO2.getPlayerSumTotalTestCases().intValue())))
                        .andExpect(jsonPath("$[1].problemId", is(gameStatsDTO2.getProblemId().intValue())))
                        .andExpect(jsonPath("$[1].sumPassedTestCases", is(gameStatsDTO2.getSumPassedTestCases().intValue())))
                        .andExpect(jsonPath("$[1].sumTotalTestCases", is(gameStatsDTO2.getSumTotalTestCases().intValue())))
                        .andExpect(jsonPath("$[1].title", is(gameStatsDTO2.getTitle())))
                        .andExpect(jsonPath("$[1].totalSubmissionCount", is(gameStatsDTO2.getTotalSubmissionCount().intValue())))
                        .andExpect(jsonPath("$[1].totalSuccessRate").exists());
                    }
    
    @Test
    void getMomentaryMostPopularProblems_playerDidNotPlay_statsAreNull_success() throws Exception {
        
        List<GameStatsDTO> gameStatsDTOs = List.of(gameStatsDTO3);

        doNothing().when(userService).verifyTokenAndUserId(testUser.getToken(), testUser.getId());
        Mockito.when(gameStatsService.getMostPopularProblemsAndPlayerResults(Mockito.any())).thenReturn(gameStatsDTOs);

        MockHttpServletRequestBuilder getRequest = get("/stats/popular-problems")
                                                    .header("token", testUser.getToken())
                                                    .header("userId", testUser.getId())
                                                    .contentType(MediaType.APPLICATION_JSON);
        
        mockMvc.perform(getRequest)
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$", hasSize(1)))
                        .andExpect(jsonPath("$[0].description", is(gameStatsDTO3.getDescription())))
                        .andExpect(jsonPath("$[0].gameLanguage", is(gameStatsDTO3.getGameLanguage().toString())))
                        .andExpect(jsonPath("$[0].playerSuccessRate", is(nullValue())))
                        .andExpect(jsonPath("$[0].playerSumPassedTestCases", is(nullValue())))
                        .andExpect(jsonPath("$[0].playerSumTotalTestCases", is(nullValue())))
                        .andExpect(jsonPath("$[0].problemId", is(gameStatsDTO3.getProblemId().intValue())))
                        .andExpect(jsonPath("$[0].sumPassedTestCases", is(gameStatsDTO3.getSumPassedTestCases().intValue())))
                        .andExpect(jsonPath("$[0].sumTotalTestCases", is(gameStatsDTO3.getSumTotalTestCases().intValue())))
                        .andExpect(jsonPath("$[0].title", is(gameStatsDTO3.getTitle())))
                        .andExpect(jsonPath("$[0].totalSubmissionCount", is(gameStatsDTO3.getTotalSubmissionCount().intValue())))
                        .andExpect(jsonPath("$[0].totalSuccessRate").exists());
                    }
    
    @Test
    void getMomentaryMostPopularProblems_noProblemsToRetrieve_throwsNotFound() throws Exception {

        doNothing().when(userService).verifyTokenAndUserId(testUser.getToken(), testUser.getId());
        
        String errorReason = "Statistics unavailable: no problem has been played yet!";
		doThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, errorReason)).when(gameStatsService).getMostPopularProblemsAndPlayerResults(Mockito.any());

        MockHttpServletRequestBuilder getRequest = get("/stats/popular-problems")
                                                    .header("token", testUser.getToken())
                                                    .header("userId", testUser.getId())
                                                    .contentType(MediaType.APPLICATION_JSON);
        
        mockMvc.perform(getRequest)
				        .andExpect(status().isNotFound())
				        .andExpect(jsonPath("$.detail", is(errorReason)));
                    }


    @Test
    void getGameplaySummary_validRequest_success() throws Exception {

        doNothing().when(userService).verifyTokenAndUserId(testUser.getToken(), testUser.getId());
        Mockito.when(gameStatsService.getGameplaySummary(Mockito.any())).thenReturn(testPlayerWrappedDTO);
        
        MockHttpServletRequestBuilder getRequest = get("/stats/gameplay-summary/{userId}", testUser.getId())
                                                    .header("token", testUser.getToken())
                                                    .header("userId", testUser.getId())
                                                    .contentType(MediaType.APPLICATION_JSON);
        
        mockMvc.perform(getRequest)
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.username", is(testPlayerWrappedDTO.getUsername())))
                        .andExpect(jsonPath("$.totalGamesPlayed", is(testPlayerWrappedDTO.getTotalGamesPlayed())))
                        .andExpect(jsonPath("$.winCount", is(testPlayerWrappedDTO.getWinCount())))
                        .andExpect(jsonPath("$.playerSumPassedTestCases", is(testPlayerWrappedDTO.getPlayerSumPassedTestCases().intValue())))
                        .andExpect(jsonPath("$.playerSumTotalTestCases", is(testPlayerWrappedDTO.getPlayerSumTotalTestCases().intValue())))
                        .andExpect(jsonPath("$.totalProblemsSolvedFullyCorrect", is(testPlayerWrappedDTO.getTotalProblemsSolvedFullyCorrect())))
                        .andExpect(jsonPath("$.percentileRank").exists())
                        .andExpect(jsonPath("$.favGameLanguage", is(testPlayerWrappedDTO.getFavGameLanguage().toString())))
                        .andExpect(jsonPath("$.favGameDifficulty", is(testPlayerWrappedDTO.getFavGameDifficulty().toString())))
                        .andExpect(jsonPath("$.favGameMode", is(testPlayerWrappedDTO.getFavGameMode().toString())));
                    }

    @Test
    void getGameplaySummary_noSubmissionsYet_throwsNotFound() throws Exception {

        doNothing().when(userService).verifyTokenAndUserId(testUser.getToken(), testUser.getId());
        
        String errorReason = "Statistics unavailable: player has no submissions yet!";
		doThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, errorReason)).when(gameStatsService).getGameplaySummary(Mockito.any());

        MockHttpServletRequestBuilder getRequest = get("/stats/gameplay-summary/{userId}", testUser.getId())
                                                    .header("token", testUser.getToken())
                                                    .header("userId", testUser.getId())
                                                    .contentType(MediaType.APPLICATION_JSON);
        
        mockMvc.perform(getRequest)
				        .andExpect(status().isNotFound())
				        .andExpect(jsonPath("$.detail", is(errorReason)));
                    }

    }

