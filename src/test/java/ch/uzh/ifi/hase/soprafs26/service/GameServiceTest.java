package ch.uzh.ifi.hase.soprafs26.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import ch.uzh.ifi.hase.soprafs26.constant.GameDifficulty;
import ch.uzh.ifi.hase.soprafs26.constant.GameEndReason;
import ch.uzh.ifi.hase.soprafs26.constant.GameLanguage;
import ch.uzh.ifi.hase.soprafs26.constant.GameMode;
import ch.uzh.ifi.hase.soprafs26.constant.GameStatus;
import ch.uzh.ifi.hase.soprafs26.constant.PlayerSessionStatus;
import ch.uzh.ifi.hase.soprafs26.constant.Verdict;
import ch.uzh.ifi.hase.soprafs26.entity.GameSession;
import ch.uzh.ifi.hase.soprafs26.entity.PlayerSession;
import ch.uzh.ifi.hase.soprafs26.entity.Problem;
import ch.uzh.ifi.hase.soprafs26.entity.Room;
import ch.uzh.ifi.hase.soprafs26.entity.Submission;
import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.repository.GameSessionRepository;
import ch.uzh.ifi.hase.soprafs26.repository.RoomRepository;
import ch.uzh.ifi.hase.soprafs26.repository.UserRepository;
import ch.uzh.ifi.hase.soprafs26.rest.dto.GameEndDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.GameSessionSampleSolutionsDTO;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ScheduledFuture;
import ch.uzh.ifi.hase.soprafs26.rest.dto.GameTimeWarningDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.PlayerGameSummaryDTO;

class GameServiceTest {

    @Mock
    private RoomRepository roomRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserService userService;

    @Mock
    private ProblemService problemService;

    @Mock
    private GameSessionRepository gameSessionRepository;

    @Mock
    private SimpMessagingTemplate messagingTemplate;
  
    @Mock
    private WsRoomService wsRoomService;

    @Mock
    private WsGameService wsGameService;

    @Mock
    private TaskScheduler taskScheduler;

    @Mock
    private ScheduledFuture<Object> warningFuture;

    @Mock
    private ScheduledFuture<Object> endFuture;

    @InjectMocks
    private GameService gameService;

    private Room testRoom;
    private User gameHost;
    private User player2;
    private User player3;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);

        gameHost = new User();
        gameHost.setId(2L);
        gameHost.setUsername("gameHost");
        gameHost.setWinCount(0);
		gameHost.setWinRatePercentage(0.0);
		gameHost.setTotalGamesPlayed(0);
		gameHost.setTotalPoints(0L);

        player2 = new User();
        player2.setId(3L);
        player2.setUsername("player2");
        player2.setWinCount(0);
		player2.setWinRatePercentage(0.0);
		player2.setTotalGamesPlayed(0);
		player2.setTotalPoints(0L);

        player3 = new User();
        player3.setId(4L);
        player3.setUsername("player3");
        player3.setWinCount(0);
		player3.setWinRatePercentage(0.0);
		player3.setTotalGamesPlayed(0);
		player3.setTotalPoints(0L);

        testRoom = new Room();
        testRoom.setRoomId(8L);
        testRoom.setRoomJoinCode("ABC123");
        testRoom.setMaxNumPlayers(2);
        testRoom.setRoomOpen(false);
        testRoom.setCurrentNumPlayers(2);
        testRoom.setHostUserId(gameHost.getId());
        Set<Long> playerIds = new HashSet<>();
        playerIds.add(gameHost.getId()); 
        playerIds.add(player2.getId());
        testRoom.setPlayerIds(playerIds);
        testRoom.setGameDifficulty(GameDifficulty.EASY);
        testRoom.setGameLanguage(GameLanguage.PYTHON);
        testRoom.setGameMode(GameMode.RACE);
    }

    //createGameSession success; IMPORTANT: since this method fires back a personalised WS message the WS-part is tested separately
    //this mainly tests whether a GameSession object is created succesfully
    @Test
    void createGameSession_validInputs_success() {
        Problem p1 = new Problem();
        p1.setProblemId(1L);
        Problem p3 = new Problem();
        p3.setProblemId(3L);
        Problem p7 = new Problem();
        p7.setProblemId(7L);

        testRoom.setNumOfProblems(3);
        
        given(roomRepository.findByRoomId(testRoom.getRoomId())).willReturn(testRoom);
        given(userRepository.findUserById(gameHost.getId())).willReturn(gameHost);
        given(problemService.getAllProblems()).willReturn(List.of(p1, p3, p7));
        given(userService.getUserById(gameHost.getId())).willReturn(gameHost);
        given(userService.getUserById(player2.getId())).willReturn(player2);
        given(gameSessionRepository.save(any(GameSession.class)))
            .willAnswer(invocation -> {
                GameSession s = invocation.getArgument(0);
                s.setGameSessionId(99L);
                return s;
            });
        given(taskScheduler.schedule(any(Runnable.class), any(Instant.class)))
            .willReturn((ScheduledFuture) warningFuture, (ScheduledFuture) endFuture);

        doNothing().when(wsRoomService).notifyPlayerGameStarted(Mockito.any());

        gameService.createGameSession(gameHost.getId(), testRoom.getRoomId());

        // let's us set an object that is able to capture the created GameSession in createGameSession
        ArgumentCaptor<GameSession> captor = ArgumentCaptor.forClass(GameSession.class);
        verify(gameSessionRepository).save(captor.capture()); //capture the GameSession object
        verify(gameSessionRepository).flush();

        // inspect the captured value
        GameSession savedGameSession = captor.getValue();
        assertEquals(GameStatus.ACTIVE, savedGameSession.getGameStatus());
        assertEquals(testRoom, savedGameSession.getRoom());
        assertEquals(3, savedGameSession.getProblems().size());
        assertEquals(2, savedGameSession.getPlayerSessions().size());
        assertNotNull(savedGameSession.getStartedAt());
    }

    

    //createGameSession fail: room not found
    @Test
    void createGameSession_roomNotFound_throwsNotFound() {
        given(roomRepository.findByRoomId(testRoom.getRoomId())).willReturn(null);

        assertThrows(ResponseStatusException.class, () ->
                gameService.createGameSession(gameHost.getId(), testRoom.getRoomId()));
    }

    //createGameSession fail: user not found
    @Test
    void createGameSession_userNotFound_throwsNotFound() {
        given(roomRepository.findByRoomId(testRoom.getRoomId())).willReturn(testRoom);
        given(userRepository.findUserById(gameHost.getId())).willReturn(null);

        assertThrows(ResponseStatusException.class, () ->
                gameService.createGameSession(gameHost.getId(), testRoom.getRoomId()));
    }

    //createGameSession fail: non-host tries to start/create game
    @Test
    void createGameSession_invalidHostId_throwsForbidden() {
        given(roomRepository.findByRoomId(testRoom.getRoomId())).willReturn(testRoom);
        given(userRepository.findUserById(gameHost.getId())).willReturn(gameHost);

        assertThrows(ResponseStatusException.class, () ->
                gameService.createGameSession(player2.getId(), testRoom.getRoomId()));
    }

    //createGameSession fail: not enough players to start the game
    @Test
    void createGameSession_notEnoughPlayers_throwsConflict() {
        testRoom.setCurrentNumPlayers(1);
        testRoom.setRoomOpen(true);
        testRoom.setPlayerIds(Set.of(gameHost.getId()));

        given(roomRepository.findByRoomId(testRoom.getRoomId())).willReturn(testRoom);
        given(userRepository.findUserById(gameHost.getId())).willReturn(gameHost);

        assertThrows(ResponseStatusException.class, () ->
                gameService.createGameSession(gameHost.getId(), testRoom.getRoomId()));
    }


    //createGameSession fail: requested more problems than available (throws InternalServerError because this is the second-check of the same thing, i.e., very defensive coding)
    @Test
    void createGameSession_moreProblemsRequestedThenAvailable_throwsInternalServerErrror() {
        testRoom.setNumOfProblems(5432);

        given(roomRepository.findByRoomId(testRoom.getRoomId())).willReturn(testRoom);
        given(userRepository.findUserById(gameHost.getId())).willReturn(gameHost);
        given(problemService.getAllProblems()).willReturn(List.of(new Problem()));

        assertThrows(ResponseStatusException.class, () ->
                gameService.createGameSession(gameHost.getId(), testRoom.getRoomId()));
    }

    @Test
    void createGameSessionSchedulesWarningAndEndTasks() {
        Problem p1 = new Problem();
        p1.setProblemId(1L);
        testRoom.setNumOfProblems(1);

        given(roomRepository.findByRoomId(testRoom.getRoomId())).willReturn(testRoom);
        given(userRepository.findUserById(gameHost.getId())).willReturn(gameHost);
        given(problemService.getAllProblems()).willReturn(List.of(p1));
        given(userService.getUserById(gameHost.getId())).willReturn(gameHost);
        given(userService.getUserById(player2.getId())).willReturn(player2);
        given(gameSessionRepository.save(any(GameSession.class)))
            .willAnswer(invocation -> {
                GameSession s = invocation.getArgument(0);
                s.setGameSessionId(11L);
                return s;
            });
        given(taskScheduler.schedule(any(Runnable.class), any(Instant.class)))
            .willReturn((ScheduledFuture) warningFuture, (ScheduledFuture) endFuture);

        gameService.createGameSession(gameHost.getId(), testRoom.getRoomId());

        ArgumentCaptor<Instant> whenCaptor = ArgumentCaptor.forClass(Instant.class);
        verify(taskScheduler, Mockito.times(2))
            .schedule(any(Runnable.class), whenCaptor.capture());

        List<Instant> scheduledAt = whenCaptor.getAllValues();
        long firstOffsetSec  = Duration.between(Instant.now(), scheduledAt.get(0)).getSeconds();
        long secondOffsetSec = Duration.between(Instant.now(), scheduledAt.get(1)).getSeconds();
        // allow a few seconds of test slack
        assertEquals(14 * 60, firstOffsetSec, 5);
        assertEquals(15 * 60, secondOffsetSec, 5);
    }

    @Test
    void warningTask_whenFired_broadcastsTimeWarning() {
        Problem p1 = new Problem();
        p1.setProblemId(1L);
        testRoom.setNumOfProblems(1);

        given(roomRepository.findByRoomId(testRoom.getRoomId())).willReturn(testRoom);
        given(userRepository.findUserById(gameHost.getId())).willReturn(gameHost);
        given(problemService.getAllProblems()).willReturn(List.of(p1));
        given(userService.getUserById(Mockito.anyLong())).willReturn(gameHost);
        given(gameSessionRepository.save(any(GameSession.class)))
            .willAnswer(inv -> {
                GameSession s = inv.getArgument(0);
                s.setGameSessionId(42L);
                return s;
            });
        given(taskScheduler.schedule(any(Runnable.class), any(Instant.class)))
            .willReturn((ScheduledFuture) warningFuture, (ScheduledFuture) endFuture);

        gameService.createGameSession(gameHost.getId(), testRoom.getRoomId());

        ArgumentCaptor<Runnable> runnableCaptor = ArgumentCaptor.forClass(Runnable.class);
        verify(taskScheduler, Mockito.times(2))
        .schedule(runnableCaptor.capture(), any(Instant.class));

        // stub the repo re-fetch that the runnable performs
        GameSession active = new GameSession();
        active.setGameSessionId(42L);
        active.setGameStatus(GameStatus.ACTIVE);
        given(gameSessionRepository.findByGameSessionId(42L)).willReturn(active);

        // fire the warning runnable (first one scheduled)
        runnableCaptor.getAllValues().get(0).run();

        ArgumentCaptor<GameTimeWarningDTO> warnCaptor = ArgumentCaptor.forClass(GameTimeWarningDTO.class);
        verify(wsGameService).notifyPlayerGameTimeWarning(warnCaptor.capture());
        assertEquals(60L, warnCaptor.getValue().getRemainingTimeSeconds());
        assertEquals(42L, warnCaptor.getValue().getGameSessionId());
    }

    @Test
    void endTask_whenFired_endsGameWithTimeUp() {
        Problem p1 = new Problem();
        p1.setProblemId(1L);
        testRoom.setNumOfProblems(1);

        given(roomRepository.findByRoomId(testRoom.getRoomId())).willReturn(testRoom);
        given(userRepository.findUserById(gameHost.getId())).willReturn(gameHost);
        given(problemService.getAllProblems()).willReturn(List.of(p1));
        given(userService.getUserById(Mockito.anyLong())).willReturn(gameHost);
        given(gameSessionRepository.save(any(GameSession.class)))
            .willAnswer(inv -> {
                GameSession s = inv.getArgument(0);
                s.setGameSessionId(77L);
                return s;
            });
        given(taskScheduler.schedule(any(Runnable.class), any(Instant.class)))
            .willReturn((ScheduledFuture) warningFuture, (ScheduledFuture) endFuture);

                ReflectionTestUtils.setField(gameService, "self", gameService);
                gameService.createGameSession(gameHost.getId(), testRoom.getRoomId());

        ArgumentCaptor<Runnable> runnableCaptor = ArgumentCaptor.forClass(Runnable.class);
        verify(taskScheduler, Mockito.times(2))
            .schedule(runnableCaptor.capture(), any(Instant.class));

        // build a minimal ACTIVE session 
        GameSession active = new GameSession();
        active.setGameSessionId(77L);
        active.setGameStatus(GameStatus.ACTIVE);
        PlayerSession ps = new PlayerSession();
        ps.setPlayerSessionId(1L);
        ps.setPlayer(gameHost);
        ps.setCurrentScore(0);
        ps.setCurrentProblemIndex(0);
        ps.setPlayerSessionStatus(PlayerSessionStatus.PLAYING);
        active.setPlayerSessions(new ArrayList<>(List.of(ps)));
        given(gameSessionRepository.findByGameSessionId(77L)).willReturn(active);

        // fire the end run
        runnableCaptor.getAllValues().get(1).run();

        ArgumentCaptor<GameEndDTO> endCaptor = ArgumentCaptor.forClass(GameEndDTO.class);
        verify(wsGameService).notifyPlayerGameEnded(endCaptor.capture());
        assertEquals(GameEndReason.TIME_UP, endCaptor.getValue().getGameEndReason());
        assertEquals(GameStatus.ENDED, active.getGameStatus());
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void endGameSession_earlyFinish_cancelsScheduledTasks() {
        Problem p1 = new Problem();
        p1.setProblemId(1L);
        testRoom.setNumOfProblems(1);

        given(roomRepository.findByRoomId(testRoom.getRoomId())).willReturn(testRoom);
        given(userRepository.findUserById(gameHost.getId())).willReturn(gameHost);
        given(problemService.getAllProblems()).willReturn(List.of(p1));
        given(userService.getUserById(Mockito.anyLong())).willReturn(gameHost);
        given(gameSessionRepository.save(any(GameSession.class)))
            .willAnswer(inv -> {
                GameSession s = inv.getArgument(0);
                s.setGameSessionId(55L);
                return s;
            });
        given(taskScheduler.schedule(any(Runnable.class), any(Instant.class)))
            .willReturn((ScheduledFuture) warningFuture, (ScheduledFuture) endFuture);

        gameService.createGameSession(gameHost.getId(), testRoom.getRoomId());


        ArgumentCaptor<GameSession> savedCaptor = ArgumentCaptor.forClass(GameSession.class);
        verify(gameSessionRepository).save(savedCaptor.capture());
        GameSession saved = savedCaptor.getValue();

        gameService.endGameSession(saved, GameEndReason.PLAYER_FINISHED);

        verify(warningFuture).cancel(false);
        verify(endFuture).cancel(false);
    }

    //--- endGameSession tests ---

    private GameSession buildGameSession(long sessionId, PlayerSession... sessions) {
        GameSession gs = new GameSession();
        gs.setGameSessionId(sessionId);
        List<PlayerSession> psList = new ArrayList<>();
        for (PlayerSession ps : sessions) {
            ps.setGameSession(gs);
            psList.add(ps);
        }
        gs.setPlayerSessions(psList);
        given(gameSessionRepository.save(any(GameSession.class))).willAnswer(i -> i.getArgument(0));
        return gs;
    }

    private PlayerSession buildPlayerSession(long psId, User player, int score, int problemsSolved) {

        PlayerSession ps = new PlayerSession();
        ps.setPlayerSessionId(psId);
        ps.setPlayer(player);
        ps.setCurrentScore(score);
        ps.setCurrentProblemIndex(problemsSolved);
        ps.setPlayerSessionStatus(PlayerSessionStatus.PLAYING);

        return ps;
    }

    @Test
    void endGameSession_singleWinner_setsStatusAndBroadcastsWinner() {
        PlayerSession ps1 = buildPlayerSession(1L, gameHost, 5, 2);
        PlayerSession ps2 = buildPlayerSession(2L, player2, 2, 1);
        GameSession gs = buildGameSession(99L, ps1, ps2);

        gameService.endGameSession(gs, GameEndReason.PLAYER_FINISHED);

        assertEquals(GameStatus.ENDED, gs.getGameStatus());
        assertEquals(GameEndReason.PLAYER_FINISHED, gs.getGameEndReason());
        assertNotNull(gs.getEndedAt());

        ArgumentCaptor<GameEndDTO> captor = ArgumentCaptor.forClass(GameEndDTO.class);
        verify(wsGameService).notifyPlayerGameEnded(captor.capture());

        GameEndDTO gameEndDTO = captor.getValue();
        assertEquals(99L, gameEndDTO.getGameSessionId());
        assertEquals(gameHost.getId(), gameEndDTO.getWinnerPlayerId());
        assertEquals(GameEndReason.PLAYER_FINISHED, gameEndDTO.getGameEndReason());
        assertEquals(gameHost.getId(), gameEndDTO.getWinnerPlayerId()); // host has higher score
        assertEquals(2, gameEndDTO.getPlayerScores().size());
        assertEquals(gameHost.getUsername(), gameEndDTO.getPlayerScores().get(0).getUsername()); // sorted by score desc
    }

    @Test
    void endGameSession_tie_broadcastsNullWinner() {
        PlayerSession ps1 = buildPlayerSession(1L, gameHost, 3, 1);
        PlayerSession ps2 = buildPlayerSession(2L, player2, 3, 1);
        GameSession gs = buildGameSession(99L, ps1, ps2);

        gameService.endGameSession(gs, GameEndReason.TIME_UP);

        ArgumentCaptor<GameEndDTO> captor = ArgumentCaptor.forClass(GameEndDTO.class);
        verify(wsGameService).notifyPlayerGameEnded(captor.capture());

        GameEndDTO gameEndDTO = captor.getValue();
        assertNull(gameEndDTO.getWinnerPlayerId());
        assertEquals(GameEndReason.TIME_UP, gameEndDTO.getGameEndReason());
    }

    @Test
    void endGameSession_playerScoresSortedDescending() {
        PlayerSession ps1 = buildPlayerSession(1L, gameHost,  1, 0);
        PlayerSession ps2 = buildPlayerSession(3L, player2, 4, 1);
        PlayerSession ps3 = buildPlayerSession(3L, player3, 10, 2);
        GameSession gs = buildGameSession(99L, ps1, ps2, ps3);

        gameService.endGameSession(gs, GameEndReason.TIME_UP);

        ArgumentCaptor<GameEndDTO> captor = ArgumentCaptor.forClass(GameEndDTO.class);
        verify(wsGameService).notifyPlayerGameEnded(captor.capture());

        GameEndDTO gameEndDTO = captor.getValue();
        assertEquals(player3.getUsername(), gameEndDTO.getPlayerScores().get(0).getUsername());
        assertEquals(player2.getUsername(), gameEndDTO.getPlayerScores().get(1).getUsername());
        assertEquals(gameHost.getUsername(), gameEndDTO.getPlayerScores().get(2).getUsername());
        assertEquals(player3.getId(), gameEndDTO.getWinnerPlayerId());
    }

    //player stats are updated in user profile
    @Test 
    void endGameSession_updatesEachPlayersGameStats() {

        //players already have points
        gameHost.setTotalGamesPlayed(20);
        gameHost.setTotalPoints(100L);
        gameHost.setWinCount(10);
        gameHost.setWinRatePercentage((double) 10 / 20);

        player2.setTotalGamesPlayed(40);
        player2.setTotalPoints(100L);
        player2.setWinCount(10);
        player2.setWinRatePercentage((double) 10 / 40);

        PlayerSession ps1 = buildPlayerSession(1L, gameHost, 1, 0);
        PlayerSession ps2 = buildPlayerSession(2L, player2, 8, 3);
        GameSession gs = buildGameSession(99L, ps1, ps2);

        gameService.endGameSession(gs, GameEndReason.PLAYER_FINISHED);

        assertEquals(21, gameHost.getTotalGamesPlayed());
        assertEquals(101, gameHost.getTotalPoints().intValue());
        assertEquals(10, gameHost.getWinCount());
        assertTrue((gameHost.getWinRatePercentage() < ((double) 10 / 20 * 100))); //since host did not win

        assertEquals(41, player2.getTotalGamesPlayed());
        assertEquals(108, player2.getTotalPoints().intValue());
        assertEquals(11, player2.getWinCount());
        assertTrue(player2.getWinRatePercentage() > ((double) 10 / 40 * 100)); //since player 2 did win
    }

    //game stats update is actually saved and flushed
    @Test
    void endGameSession_savesAndFlushesEachPlayer() {

        PlayerSession ps1 = buildPlayerSession(1L, gameHost, 10, 4);
        PlayerSession ps2 = buildPlayerSession(2L, player2, 20, 7);
        GameSession gs = buildGameSession(99L, ps1, ps2);

        gameService.endGameSession(gs, GameEndReason.PLAYER_FINISHED);

        verify(userRepository, times(2)).saveAndFlush(any(User.class));
    }

    //gameSessionSampleSolution are included
    @Test 
    void endGameSession_prepareGameSessionSampleSolutions_success() {

        Problem p1 = new Problem();
        p1.setProblemId(1L);
        p1.setSampleSolution("sampleSolution1");

        Problem p2 = new Problem();
        p2.setProblemId(2L);
        p2.setSampleSolution("sampleSolution2");

        PlayerSession ps1 = buildPlayerSession(1L, gameHost, 10, 4);
        PlayerSession ps2 = buildPlayerSession(2L, player2, 20, 7);
        
        GameSession gameSession = new GameSession();
        gameSession.setGameSessionId(1L);
        gameSession.setPlayerSessions(List.of(ps1, ps2));
        gameSession.setProblems(List.of(p1, p2));

        gameService.endGameSession(gameSession, GameEndReason.TIME_UP);

        ArgumentCaptor<GameEndDTO> captor = ArgumentCaptor.forClass(GameEndDTO.class);
        verify(wsGameService).notifyPlayerGameEnded(captor.capture());
    
        //check that there are actually the sample solutions and that their order is correct
        GameEndDTO gameEndDTO = captor.getValue();

        Map<Long, GameSessionSampleSolutionsDTO> solutions = gameEndDTO.getGameSessionSampleSolutions();


        List<Long> expectedOrder = List.of(p1.getProblemId(), p2.getProblemId());
        List<Long> actualOrder = new ArrayList<>(solutions.keySet());

        assertEquals(expectedOrder, actualOrder);
        assertNotNull(gameEndDTO);
        assertEquals(2, gameEndDTO.getGameSessionSampleSolutions().size());
        assertNotNull(gameEndDTO.getGameSessionSampleSolutions());
        gameEndDTO.getGameSessionSampleSolutions().forEach((key, value) -> {
            assertNotNull(key);
            assertNotNull(value);
        });
    }

    //prepare correct playerGameSummaryDTO
    @Test 
    void endGameSession_prepareCorrectPlayerGameSummaryDTO_success() {

        //prepare problems and associated submissions
        //3 submissions fully correct, 2 not fully correct
        Problem p1 = new Problem();
        p1.setProblemId(1L);

        Submission s1 = new Submission();
        s1.setProblemId(p1.getProblemId());
        s1.setVerdict(Verdict.CORRECT_ANSWER);

        Problem p2 = new Problem();
        p2.setProblemId(2L);

        Submission s2 = new Submission();
        s2.setProblemId(p2.getProblemId());
        s2.setVerdict(Verdict.CORRECT_ANSWER);

        Problem p3 = new Problem();
        p3.setProblemId(3L);

        Submission s3 = new Submission();
        s3.setProblemId(p3.getProblemId());
        s3.setVerdict(Verdict.WRONG_ANSWER);

        Problem p4 = new Problem();
        p4.setProblemId(4L);

        Submission s4 = new Submission();
        s4.setProblemId(p4.getProblemId());
        s4.setVerdict(Verdict.CORRECT_ANSWER);

        Problem p5 = new Problem();
        p5.setProblemId(5L);

        Submission s5 = new Submission();
        s5.setProblemId(p5.getProblemId());
        s5.setVerdict(Verdict.WRONG_ANSWER);

        PlayerSession ps1 = buildPlayerSession(1L, gameHost, 15, 5);
        ps1.setSubmissions(List.of(s1, s2, s3, s4, s5));

        GameSession gameSession = new GameSession();
        gameSession.setGameSessionId(1L);
        gameSession.setPlayerSessions(List.of(ps1));
        gameSession.setProblems(List.of(p1, p2, p3, p4, p5));

        gameService.endGameSession(gameSession, GameEndReason.TIME_UP);

        ArgumentCaptor<PlayerGameSummaryDTO> captor = ArgumentCaptor.forClass(PlayerGameSummaryDTO.class);
        verify(wsGameService).sendPlayerGameSummary(captor.capture());

        PlayerGameSummaryDTO playerGameSummaryDTO = captor.getValue();
        assertNotNull(playerGameSummaryDTO);
        assertEquals(playerGameSummaryDTO.getPlayerSessionId(), ps1.getPlayerSessionId());
        assertEquals(playerGameSummaryDTO.getPlayerId(), ps1.getPlayer().getId());

        //check that solvedCorrectly contains three problems
        List<Long> solvedCorrectly = playerGameSummaryDTO.getProblemResults().get("solvedCorrectly");
        assertNotNull(solvedCorrectly);
        assertEquals(3, solvedCorrectly.size());
        assertTrue(solvedCorrectly.contains(p1.getProblemId()));
        assertTrue(solvedCorrectly.contains(p2.getProblemId()));
        assertTrue(solvedCorrectly.contains(p4.getProblemId()));

        //check that solvedCorrectly contains three problems
        List<Long> notSolvedFullyCorrectly = playerGameSummaryDTO.getProblemResults().get("notSolvedFullyCorrectly");
        assertNotNull(notSolvedFullyCorrectly);
        assertEquals(2, notSolvedFullyCorrectly.size());
        assertTrue(notSolvedFullyCorrectly.contains(p3.getProblemId()));
        assertTrue(notSolvedFullyCorrectly.contains(p5.getProblemId()));
    }

    //prepare correct playerGameSummaryDTO => all correct
    @Test 
    void endGameSession_prepareCorrectPlayerGameSummaryDTOOnlyCorrectSubmissions_success() {

        Problem p1 = new Problem();
        p1.setProblemId(1L);

        Submission s1 = new Submission();
        s1.setProblemId(p1.getProblemId());
        s1.setVerdict(Verdict.CORRECT_ANSWER);

        Problem p2 = new Problem();
        p2.setProblemId(2L);

        Submission s2 = new Submission();
        s2.setProblemId(p2.getProblemId());
        s2.setVerdict(Verdict.CORRECT_ANSWER);

        PlayerSession ps1 = buildPlayerSession(1L, gameHost, 15, 5);
        ps1.setSubmissions(List.of(s1, s2));

        GameSession gameSession = new GameSession();
        gameSession.setGameSessionId(1L);
        gameSession.setPlayerSessions(List.of(ps1));
        gameSession.setProblems(List.of(p1, p2));

        gameService.endGameSession(gameSession, GameEndReason.TIME_UP);

        ArgumentCaptor<PlayerGameSummaryDTO> captor = ArgumentCaptor.forClass(PlayerGameSummaryDTO.class);
        verify(wsGameService).sendPlayerGameSummary(captor.capture());

        PlayerGameSummaryDTO playerGameSummaryDTO = captor.getValue();
        assertNotNull(playerGameSummaryDTO);
        assertEquals(playerGameSummaryDTO.getPlayerSessionId(), ps1.getPlayerSessionId());
        assertEquals(playerGameSummaryDTO.getPlayerId(), ps1.getPlayer().getId());

        List<Long> solvedCorrectly = playerGameSummaryDTO.getProblemResults().get("solvedCorrectly");
        assertNotNull(solvedCorrectly);
        assertEquals(2, solvedCorrectly.size());
        assertTrue(solvedCorrectly.contains(p1.getProblemId()));
        assertTrue(solvedCorrectly.contains(p2.getProblemId()));

        List<Long> notSolvedFullyCorrectly = playerGameSummaryDTO.getProblemResults().get("notSolvedFullyCorrectly");
        assertNotNull(notSolvedFullyCorrectly);
        assertTrue(notSolvedFullyCorrectly.isEmpty());
    }

    //prepare correct playerGameSummaryDTO => all wrong
    @Test 
    void endGameSession_prepareCorrectPlayerGameSummaryDTOOnlyWrongSubmissions_success() {

        Problem p1 = new Problem();
        p1.setProblemId(1L);

        Submission s1 = new Submission();
        s1.setProblemId(p1.getProblemId());
        s1.setVerdict(Verdict.WRONG_ANSWER);

        PlayerSession ps1 = buildPlayerSession(1L, gameHost, 15, 5);
        ps1.setSubmissions(List.of(s1));

        GameSession gameSession = new GameSession();
        gameSession.setGameSessionId(1L);
        gameSession.setPlayerSessions(List.of(ps1));
        gameSession.setProblems(List.of(p1));

        gameService.endGameSession(gameSession, GameEndReason.TIME_UP);

        ArgumentCaptor<PlayerGameSummaryDTO> captor = ArgumentCaptor.forClass(PlayerGameSummaryDTO.class);
        verify(wsGameService).sendPlayerGameSummary(captor.capture());

        PlayerGameSummaryDTO playerGameSummaryDTO = captor.getValue();
        assertNotNull(playerGameSummaryDTO);
        assertEquals(playerGameSummaryDTO.getPlayerSessionId(), ps1.getPlayerSessionId());
        assertEquals(playerGameSummaryDTO.getPlayerId(), ps1.getPlayer().getId());

        List<Long> solvedCorrectly = playerGameSummaryDTO.getProblemResults().get("solvedCorrectly");
        assertNotNull(solvedCorrectly);
        assertTrue(solvedCorrectly.isEmpty());

        List<Long> notSolvedFullyCorrectly = playerGameSummaryDTO.getProblemResults().get("notSolvedFullyCorrectly");
        assertNotNull(notSolvedFullyCorrectly);
        assertEquals(1, notSolvedFullyCorrectly.size());
        assertTrue(notSolvedFullyCorrectly.contains(p1.getProblemId()));
    }
}
