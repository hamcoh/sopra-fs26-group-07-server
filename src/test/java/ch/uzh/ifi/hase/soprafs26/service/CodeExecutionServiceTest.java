package ch.uzh.ifi.hase.soprafs26.service;

import ch.uzh.ifi.hase.soprafs26.constant.GameDifficulty;
import ch.uzh.ifi.hase.soprafs26.constant.GameLanguage;
import ch.uzh.ifi.hase.soprafs26.constant.GameMode;
import ch.uzh.ifi.hase.soprafs26.constant.GameStatus;
import ch.uzh.ifi.hase.soprafs26.constant.SubmissionStatus;
import ch.uzh.ifi.hase.soprafs26.constant.SubmissionType;
import ch.uzh.ifi.hase.soprafs26.constant.Verdict;
import ch.uzh.ifi.hase.soprafs26.entity.GameSession;
import ch.uzh.ifi.hase.soprafs26.entity.PlayerSession;
import ch.uzh.ifi.hase.soprafs26.entity.Problem;
import ch.uzh.ifi.hase.soprafs26.entity.Room;
import ch.uzh.ifi.hase.soprafs26.entity.Submission;
import ch.uzh.ifi.hase.soprafs26.entity.TestCase;
import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.repository.GameSessionRepository;
import ch.uzh.ifi.hase.soprafs26.repository.PlayerSessionRepository;
import ch.uzh.ifi.hase.soprafs26.repository.SubmissionRepository;
import ch.uzh.ifi.hase.soprafs26.rest.dto.CodeExecutionPostDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.CodeRunDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.GameRoundDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.JudgeBatchResultDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.JudgeResultDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.JudgeStatusDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.JudgeTokenDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

class CodeExecutionServiceTest {

    @Mock
    private ProblemService problemService;

    @Mock
    private SubmissionRepository submissionRepository;

    @Mock
    private JudgeService judgeService;

    @Mock
    private PlayerSessionRepository playerSessionRepository;

    @Mock
    private GameSessionRepository gameSessionRepository;

    @Mock
    private GameService gameService;

    @Mock
    private WsGameService wsGameService;

    @InjectMocks
    private CodeExecutionService codeExecutionService;

    private GameSession testGameSession;
    private PlayerSession playerSession1;
    private Room testRoom;
    private User gameHost;
    private User player2;
    private Problem p1;
    private Problem p3;
    private Problem p7;

    static final int POINTS_PER_TEST_CASE = 1;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        
        codeExecutionService = new CodeExecutionService(
            problemService,
            judgeService,
            submissionRepository,
            playerSessionRepository,
            gameService,
            wsGameService,
            gameSessionRepository
        );

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
        
        p1 = new Problem();
        p1.setProblemId(1L);
        
        p3 = new Problem();
        p3.setProblemId(3L);
        
        p7 = new Problem();
        p7.setProblemId(7L);

        testGameSession = new GameSession();
        testGameSession.setGameSessionId(1L);
        testGameSession.setRoom(testRoom);
        testGameSession.setGameStatus(GameStatus.ACTIVE);
        testGameSession.setStartedAt(LocalDateTime.of(2025, 4, 24, 23, 59, 59));

        playerSession1 = new PlayerSession();
        playerSession1.setPlayerSessionId(1L);
        playerSession1.setPlayer(gameHost);
    }

    private PlayerSession makePlayerSession(int currentProblemIndex, int totalProblems) {
        Problem[] problems = new Problem[totalProblems];
        for (int i = 0; i < totalProblems; i++) {
            problems[i] = new Problem();
        }
        GameSession gameSession = new GameSession();
        gameSession.setProblems(List.of(problems));

        User user = new User();
        user.setTotalPoints(0L);

        PlayerSession ps = new PlayerSession();
        ps.setCurrentProblemIndex(currentProblemIndex);
        ps.setCurrentScore(0);
        ps.setPlayer(user);
        ps.setGameSession(gameSession);
        return ps;
    }

    @Test
    void runCode_validInput_returnsFinishedStatusAndVerdict_andSavesSubmission() {
        Long gameSessionId = testGameSession.getGameSessionId();
        Long problemId = 2L;
        Long playerSessionId = playerSession1.getPlayerSessionId();

        testGameSession.getPlayerSessions().add(playerSession1);

        CodeExecutionPostDTO request = new CodeExecutionPostDTO();
        request.setPlayerSessionId(playerSessionId);
        // Updated to use "def solve" and "return"
        request.setSourceCode("def solve(input_data):\n    return input_data[::-1]");

        TestCase tc1 = new TestCase();
        tc1.setInput("hello");
        tc1.setExpectedOutput("olleh");

        TestCase tc2 = new TestCase();
        tc2.setInput("abc");
        tc2.setExpectedOutput("cba");

        Problem problem = new Problem();
        problem.setProblemId(problemId);
        problem.setGameLanguage(GameLanguage.PYTHON);
        problem.setGameDifficulty(GameDifficulty.EASY);
        problem.setTestCases(List.of(tc1, tc2));

        testGameSession.getProblems().add(problem);

        JudgeTokenDTO token1 = new JudgeTokenDTO();
        token1.setJudgeToken("tok1");

        JudgeTokenDTO token2 = new JudgeTokenDTO();
        token2.setJudgeToken("tok2");

        JudgeStatusDTO acceptedStatus = new JudgeStatusDTO();
        acceptedStatus.setId(3);
        acceptedStatus.setDescription("Accepted");

        JudgeResultDTO result1 = new JudgeResultDTO();
        result1.setToken("tok1");
        result1.setStatus(acceptedStatus);

        JudgeResultDTO result2 = new JudgeResultDTO();
        result2.setToken("tok2");
        result2.setStatus(acceptedStatus);

        JudgeBatchResultDTO batchResult = new JudgeBatchResultDTO();
        batchResult.setSubmissions(List.of(result1, result2));

        when(gameSessionRepository.findByGameSessionId(gameSessionId)).thenReturn(testGameSession);
        when(problemService.getProblemById(problemId)).thenReturn(problem);
        when(judgeService.submitBatch(any())).thenReturn(List.of(token1, token2));
        when(judgeService.getBatchSubmissionResults(anyList())).thenReturn(batchResult);
        when(submissionRepository.save(any(Submission.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CodeRunDTO result = codeExecutionService.runCode(gameSessionId, problemId, request);

        assertNotNull(result);
        assertEquals(gameSessionId, result.getGameSessionId());
        assertEquals(problemId, result.getProblemId());
        assertEquals(playerSessionId, result.getPlayerSessionId());
        assertEquals(SubmissionStatus.FINISHED, result.getSubmissionStatus());
        assertEquals(Verdict.CORRECT_ANSWER, result.getVerdict());

        ArgumentCaptor<Submission> submissionCaptor = ArgumentCaptor.forClass(Submission.class);
        verify(submissionRepository, times(2)).save(submissionCaptor.capture());

        Submission finalSavedSubmission = submissionCaptor.getAllValues().get(1);
        assertEquals(gameSessionId, finalSavedSubmission.getGameSessionId());
        assertEquals(problemId, finalSavedSubmission.getProblemId());
        assertEquals(playerSessionId, finalSavedSubmission.getPlayerSessionId());
        assertEquals(SubmissionType.RUN, finalSavedSubmission.getType());
        assertEquals(2, finalSavedSubmission.getPassedTestCases());
        assertEquals(2, finalSavedSubmission.getTotalTestCases());
        assertEquals(SubmissionStatus.FINISHED, finalSavedSubmission.getStatus());
        assertEquals(Verdict.CORRECT_ANSWER, finalSavedSubmission.getVerdict());
        assertNotNull(finalSavedSubmission.getJudgeTokensJson());
    }

    @Test
    void submitCode_alreadySubmitted_throwsConflict() {
        Long gameSessionId = testGameSession.getGameSessionId();
        Long problemId = p1.getProblemId();
        Long playerSessionId = playerSession1.getPlayerSessionId();

        testGameSession.getPlayerSessions().add(playerSession1);
        testGameSession.getProblems().add(p1);

        CodeExecutionPostDTO request = new CodeExecutionPostDTO();
        request.setPlayerSessionId(playerSessionId);

        // Updated to use "def solve" and "return"
        request.setSourceCode("def solve(x):\n    return x");

        when(gameSessionRepository.findByGameSessionId(gameSessionId)).thenReturn(testGameSession);
        when(problemService.getProblemById(problemId)).thenReturn(p1);
        when(submissionRepository.existsByGameSessionIdAndProblemIdAndPlayerSessionIdAndType(
                gameSessionId, problemId, playerSessionId, SubmissionType.SUBMIT
        )).thenReturn(true);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> codeExecutionService.submitCode(gameSessionId, problemId, request)
        );

        assertEquals(409, exception.getStatusCode().value());
        verify(judgeService, never()).submitBatch(any());
        verify(submissionRepository, never()).save(any());
    }

    @Test
    void runCode_blankSourceCode_throwsBadRequest() {
        Long gameSessionId = testGameSession.getGameSessionId();
        Long problemId = p1.getProblemId();

        testGameSession.getPlayerSessions().add(playerSession1);
        testGameSession.getProblems().add(p1);

        CodeExecutionPostDTO request = new CodeExecutionPostDTO();
        request.setPlayerSessionId(playerSession1.getPlayerSessionId());
        request.setSourceCode("   ");

        when(gameSessionRepository.findByGameSessionId(gameSessionId)).thenReturn(testGameSession);
        when(problemService.getProblemById(problemId)).thenReturn(p1);

        //it should fail only because of blankSourceCode
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> codeExecutionService.runCode(gameSessionId, problemId, request)
        );

        assertEquals(400, exception.getStatusCode().value());
        verify(judgeService, never()).submitBatch(any());
        verify(submissionRepository, never()).save(any());
    }

    @Test
    void runCode_problemWithoutTestCases_throwsBadRequest() {
        Long gameSessionId = testGameSession.getGameSessionId();
        Long problemId = p1.getProblemId();

        testGameSession.getPlayerSessions().add(playerSession1);
        testGameSession.getProblems().add(p1);

        CodeExecutionPostDTO request = new CodeExecutionPostDTO();
        request.setPlayerSessionId(playerSession1.getPlayerSessionId());

        // Updated to use "def solve" and "return"
        request.setSourceCode("def solve(input_data):\n    return input_data");

        p1.setTestCases(List.of());

        when(gameSessionRepository.findByGameSessionId(gameSessionId)).thenReturn(testGameSession);
        when(problemService.getProblemById(problemId)).thenReturn(p1);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> codeExecutionService.runCode(gameSessionId, problemId, request)
        );

        assertEquals(400, exception.getStatusCode().value());
        verify(judgeService, never()).submitBatch(any());
        verify(submissionRepository, never()).save(any());
    }

    @Test
    void runCode_wrongAnswer_returnsFinishedAndWrongAnswerVerdict() {
        Long gameSessionId = testGameSession.getGameSessionId();
        Long problemId = p1.getProblemId();

        testGameSession.getPlayerSessions().add(playerSession1);
        testGameSession.getProblems().add(p1);

        CodeExecutionPostDTO request = new CodeExecutionPostDTO();
        request.setPlayerSessionId(playerSession1.getPlayerSessionId());

        // Updated to use "def solve" and "return"
        request.setSourceCode("def solve(input_data):\n    return input_data");

        TestCase tc = new TestCase();
        tc.setInput("abc");
        tc.setExpectedOutput("cba");

        p1.setGameLanguage(GameLanguage.PYTHON);
        p1.setTestCases(List.of(tc));

        JudgeTokenDTO token = new JudgeTokenDTO();
        token.setJudgeToken("tok1");

        JudgeStatusDTO wrongAnswerStatus = new JudgeStatusDTO();
        wrongAnswerStatus.setId(4);
        wrongAnswerStatus.setDescription("Wrong Answer");

        JudgeResultDTO result1 = new JudgeResultDTO();
        result1.setToken("tok1");
        result1.setStatus(wrongAnswerStatus);

        JudgeBatchResultDTO batchResult = new JudgeBatchResultDTO();
        batchResult.setSubmissions(List.of(result1));

        when(gameSessionRepository.findByGameSessionId(gameSessionId)).thenReturn(testGameSession);
        when(problemService.getProblemById(problemId)).thenReturn(p1);
        when(judgeService.submitBatch(any())).thenReturn(List.of(token));
        when(judgeService.getBatchSubmissionResults(anyList())).thenReturn(batchResult);
        when(submissionRepository.save(any(Submission.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CodeRunDTO result = codeExecutionService.runCode(gameSessionId, problemId, request);

        assertEquals(SubmissionStatus.FINISHED, result.getSubmissionStatus());
        assertEquals(Verdict.WRONG_ANSWER, result.getVerdict());
        assertEquals(0, result.getPassedTestCases());
        assertEquals(1, result.getTotalTestCases());
    }

    @Test
    void runCode_compileError_returnsFinishedAndCompileErrorVerdict() {
        Long gameSessionId = testGameSession.getGameSessionId();
        Long problemId = p1.getProblemId();

        testGameSession.getPlayerSessions().add(playerSession1);
        testGameSession.getProblems().add(p1);

        CodeExecutionPostDTO request = new CodeExecutionPostDTO();
        request.setPlayerSessionId(playerSession1.getPlayerSessionId());

        // Updated to use "def solve" and "return"
        request.setSourceCode("def solve(input_data)\n    return input_data");

        TestCase tc = new TestCase();
        tc.setInput("abc");
        tc.setExpectedOutput("abc");

        p1.setGameLanguage(GameLanguage.PYTHON);
        p1.setGameDifficulty(GameDifficulty.EASY);
        p1.setTestCases(List.of(tc));

        JudgeTokenDTO token = new JudgeTokenDTO();
        token.setJudgeToken("tok1");

        JudgeStatusDTO compileStatus = new JudgeStatusDTO();
        compileStatus.setId(6);
        compileStatus.setDescription("Compilation Error");

        JudgeResultDTO result1 = new JudgeResultDTO();
        result1.setToken("tok1");
        result1.setStatus(compileStatus);

        JudgeBatchResultDTO batchResult = new JudgeBatchResultDTO();
        batchResult.setSubmissions(List.of(result1));

        when(gameSessionRepository.findByGameSessionId(gameSessionId)).thenReturn(testGameSession);
        when(problemService.getProblemById(problemId)).thenReturn(p1);
        when(judgeService.submitBatch(any())).thenReturn(List.of(token));
        when(judgeService.getBatchSubmissionResults(anyList())).thenReturn(batchResult);
        when(submissionRepository.save(any(Submission.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CodeRunDTO result = codeExecutionService.runCode(gameSessionId, problemId, request);

        assertEquals(SubmissionStatus.FINISHED, result.getSubmissionStatus());
        assertEquals(Verdict.COMPILE_ERROR, result.getVerdict());
    }

    @Test
    void runCode_timeLimitExceeded_returnsFinishedAndTimeLimitVerdict() {
        Long gameSessionId = testGameSession.getGameSessionId();
        Long problemId = p1.getProblemId();

        testGameSession.getPlayerSessions().add(playerSession1);
        testGameSession.getProblems().add(p1);

        CodeExecutionPostDTO request = new CodeExecutionPostDTO();
        request.setPlayerSessionId(playerSession1.getPlayerSessionId());

        // Updated to use "def solve" and "return"
        request.setSourceCode("def solve(input_data):\n    while True:\n        pass\n    return input_data");

        TestCase tc = new TestCase();
        tc.setInput("abc");
        tc.setExpectedOutput("abc");

        p1.setGameLanguage(GameLanguage.PYTHON);
        p1.setGameDifficulty(GameDifficulty.EASY);
        p1.setTestCases(List.of(tc));

        JudgeTokenDTO token = new JudgeTokenDTO();
        token.setJudgeToken("tok1");

        JudgeStatusDTO tleStatus = new JudgeStatusDTO();
        tleStatus.setId(5);
        tleStatus.setDescription("Time Limit Exceeded");

        JudgeResultDTO result1 = new JudgeResultDTO();
        result1.setToken("tok1");
        result1.setStatus(tleStatus);

        JudgeBatchResultDTO batchResult = new JudgeBatchResultDTO();
        batchResult.setSubmissions(List.of(result1));

        when(gameSessionRepository.findByGameSessionId(gameSessionId)).thenReturn(testGameSession);
        when(problemService.getProblemById(problemId)).thenReturn(p1);
        when(judgeService.submitBatch(any())).thenReturn(List.of(token));
        when(judgeService.getBatchSubmissionResults(anyList())).thenReturn(batchResult);
        when(submissionRepository.save(any(Submission.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CodeRunDTO result = codeExecutionService.runCode(gameSessionId, problemId, request);

        assertEquals(SubmissionStatus.FINISHED, result.getSubmissionStatus());
        assertEquals(Verdict.TIME_LIMIT_EXCEEDED, result.getVerdict());
    }

    /**
     * when we run the code we have a time limit for how long we wait 
     * for the judge result. The user should then get a "running" status and 
     * a "pending" verdict, which indicates that the code is still being judged.
    */
    @Test
    void runCode_processingAfterPollingWindow_returnsRunningAndPendingVerdict() {
        Long gameSessionId = testGameSession.getGameSessionId();
        Long problemId = p1.getProblemId();

        testGameSession.getPlayerSessions().add(playerSession1);
        testGameSession.getProblems().add(p1);

        CodeExecutionPostDTO request = new CodeExecutionPostDTO();
        request.setPlayerSessionId(playerSession1.getPlayerSessionId());

        // Updated to use "def solve" and "return"
        request.setSourceCode("def solve(input_data):\n    return input_data");

        TestCase tc = new TestCase();
        tc.setInput("abc");
        tc.setExpectedOutput("abc");

        p1.setGameLanguage(GameLanguage.PYTHON);
        p1.setGameDifficulty(GameDifficulty.EASY);
        p1.setTestCases(List.of(tc));

        JudgeTokenDTO token = new JudgeTokenDTO();
        token.setJudgeToken("tok1");

        JudgeStatusDTO processingStatus = new JudgeStatusDTO();
        processingStatus.setId(2);
        processingStatus.setDescription("Processing");

        JudgeResultDTO result1 = new JudgeResultDTO();
        result1.setToken("tok1");
        result1.setStatus(processingStatus);

        JudgeBatchResultDTO batchResult = new JudgeBatchResultDTO();
        batchResult.setSubmissions(List.of(result1));

        when(gameSessionRepository.findByGameSessionId(gameSessionId)).thenReturn(testGameSession);
        when(problemService.getProblemById(problemId)).thenReturn(p1);
        when(judgeService.submitBatch(any())).thenReturn(List.of(token));
        when(judgeService.getBatchSubmissionResults(anyList()))
                .thenReturn(batchResult, batchResult, batchResult);
        when(submissionRepository.save(any(Submission.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CodeRunDTO result = codeExecutionService.runCode(gameSessionId, problemId, request);

        assertEquals(SubmissionStatus.RUNNING, result.getSubmissionStatus());
        assertEquals(Verdict.PENDING, result.getVerdict());
    }

    @Test
    void submitCode_correctAnswer_twoTestCasesArePassed() {
        Long gameSessionId = testGameSession.getGameSessionId();
        Long problemId = p1.getProblemId();

        testGameSession.getPlayerSessions().add(playerSession1);
        testGameSession.getProblems().add(p1);

        CodeExecutionPostDTO request = new CodeExecutionPostDTO();
        request.setPlayerSessionId(playerSession1.getPlayerSessionId());
        request.setSourceCode("def solve(x):\n    return x[::-1]");

        TestCase tc1 = new TestCase();
        tc1.setInput("ab");
        tc1.setExpectedOutput("ba");
        TestCase tc2 = new TestCase();
        tc2.setInput("cd");
        tc2.setExpectedOutput("dc");

        p1.setGameLanguage(GameLanguage.PYTHON);
        p1.setGameDifficulty(GameDifficulty.EASY);
        p1.setTestCases(List.of(tc1, tc2));

        JudgeTokenDTO token1 = new JudgeTokenDTO();
        token1.setJudgeToken("tok1");
        JudgeTokenDTO token2 = new JudgeTokenDTO();
        token2.setJudgeToken("tok2");

        JudgeStatusDTO accepted = new JudgeStatusDTO();
        accepted.setId(3);
        accepted.setDescription("Accepted");

        JudgeResultDTO r1 = new JudgeResultDTO();
        r1.setToken("tok1");
        r1.setStatus(accepted);
        JudgeResultDTO r2 = new JudgeResultDTO();
        r2.setToken("tok2");
        r2.setStatus(accepted);

        JudgeBatchResultDTO batchResult = new JudgeBatchResultDTO();
        batchResult.setSubmissions(List.of(r1, r2));

        gameHost.setTotalPoints(0L);

        //session of gameHost
        playerSession1.setCurrentScore(0);
        playerSession1.setCurrentProblemIndex(0);

        testGameSession.setProblems(List.of(p1));
        playerSession1.setGameSession(testGameSession);

        //mock submission that is fully correct
        Submission submission = new Submission();
        submission.setGameSessionId(gameSessionId);
        submission.setProblemId(problemId);
        submission.setType(SubmissionType.SUBMIT);
        submission.setPassedTestCases(2);

        when(gameSessionRepository.findByGameSessionId(gameSessionId)).thenReturn(testGameSession);
        when(submissionRepository.existsByGameSessionIdAndProblemIdAndPlayerSessionIdAndType(
                gameSessionId, problemId, playerSession1.getPlayerSessionId(), SubmissionType.SUBMIT)).thenReturn(false);
        when(problemService.getProblemById(problemId)).thenReturn(p1);
        when(judgeService.submitBatch(any())).thenReturn(List.of(token1, token2));
        when(judgeService.getBatchSubmissionResults(anyList())).thenReturn(batchResult);
        when(submissionRepository.save(any(Submission.class))).thenAnswer(i -> i.getArgument(0));

        codeExecutionService.submitCode(gameSessionId, problemId, request);

        ArgumentCaptor<Submission> captor = ArgumentCaptor.forClass(Submission.class);
        verify(submissionRepository, times(2)).save(captor.capture());

        Submission savedSubmission = captor.getValue(); 

        assertEquals(2, savedSubmission.getPassedTestCases());
        assertEquals(gameSessionId, savedSubmission.getGameSessionId());
        assertEquals(problemId, savedSubmission.getProblemId());
        assertEquals(SubmissionType.SUBMIT, savedSubmission.getType());
    }

    @Test
    void submitCode_wrongAnswer_zeroTestCasesArePassed() {
        Long gameSessionId = testGameSession.getGameSessionId();
        Long problemId = p1.getProblemId();

        testGameSession.getPlayerSessions().add(playerSession1);
        testGameSession.getProblems().add(p1);

        CodeExecutionPostDTO request = new CodeExecutionPostDTO();
        request.setPlayerSessionId(playerSession1.getPlayerSessionId());
        request.setSourceCode("def solve(x):\n    return x");

        TestCase tc = new TestCase();
        tc.setInput("ab");
        tc.setExpectedOutput("ba");

        p1.setGameLanguage(GameLanguage.PYTHON);
        p1.setGameDifficulty(GameDifficulty.EASY);
        p1.setTestCases(List.of(tc));

        JudgeTokenDTO token = new JudgeTokenDTO();
        token.setJudgeToken("tok1");

        JudgeStatusDTO wrongAnswer = new JudgeStatusDTO();
        wrongAnswer.setId(4);
        wrongAnswer.setDescription("Wrong Answer");

        JudgeResultDTO result = new JudgeResultDTO();
        result.setToken("tok1");
        result.setStatus(wrongAnswer);

        JudgeBatchResultDTO batchResult = new JudgeBatchResultDTO();
        batchResult.setSubmissions(List.of(result));

        gameHost.setTotalPoints(0L);

        //session of gameHost
        playerSession1.setCurrentScore(0);
        playerSession1.setCurrentProblemIndex(0);

        testGameSession.setProblems(List.of(p1));
        playerSession1.setGameSession(testGameSession);

        //mock submission that is fully wrong
        Submission submission = new Submission();
        submission.setGameSessionId(gameSessionId);
        submission.setProblemId(problemId);
        submission.setType(SubmissionType.SUBMIT);
        submission.setPassedTestCases(0);

        when(gameSessionRepository.findByGameSessionId(gameSessionId)).thenReturn(testGameSession);
        when(submissionRepository.existsByGameSessionIdAndProblemIdAndPlayerSessionIdAndType(
                gameSessionId, problemId, playerSession1.getPlayerSessionId(), SubmissionType.SUBMIT)).thenReturn(false);
        when(problemService.getProblemById(problemId)).thenReturn(p1);
        when(judgeService.submitBatch(any())).thenReturn(List.of(token));
        when(judgeService.getBatchSubmissionResults(anyList())).thenReturn(batchResult);
        when(submissionRepository.save(any(Submission.class))).thenAnswer(i -> i.getArgument(0));

        codeExecutionService.submitCode(gameSessionId, problemId, request);


        ArgumentCaptor<Submission> captor = ArgumentCaptor.forClass(Submission.class);
        verify(submissionRepository, times(2)).save(captor.capture());

        Submission savedSubmission = captor.getValue(); 

        assertEquals(0, savedSubmission.getPassedTestCases());
    }

    //getLatestSubmissionResult fail: invalid PlayerSession (same checks also for getLatestRunResult, hence only tested once)
    @Test
    void getLatestSubmissionResult_invalidPlayerSession_throwsBadRequest() {

        Long playerSessionId = playerSession1.getPlayerSessionId();

        when(playerSessionRepository.findByPlayerSessionId(playerSessionId)).thenReturn(null);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                codeExecutionService.getLatestSubmissionResult(testGameSession.getGameSessionId(), p1.getProblemId(), playerSessionId)
        );

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        assertEquals("Invalid Player Session!", ex.getReason());
    }

    //getLatestSubmissionResult fail: Game's over
    @Test
    void getLatestSubmissionResult_cannotSubmitGameOver_throwsConflict() {

        testGameSession.setGameStatus(GameStatus.ENDED);
        playerSession1.setGameSession(testGameSession);
        Long playerSessionId = playerSession1.getPlayerSessionId();

        when(playerSessionRepository.findByPlayerSessionId(playerSessionId)).thenReturn(playerSession1);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                codeExecutionService.getLatestSubmissionResult(testGameSession.getGameSessionId(), p1.getProblemId(), playerSessionId)
        );

        assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
        assertEquals("Game has ended: No submission possible anymore!", ex.getReason());
    }

    //getLatestSubmissionResult fail: Mismatch PlayerSession/GameSession
    @Test
    void getLatestSubmissionResult_mismatchPlayerAndGameSession_throwsBadRequest() {

        playerSession1.setGameSession(testGameSession);
        Long playerSessionId = playerSession1.getPlayerSessionId();

        Long invalidGameSessionId = 10L;

        when(playerSessionRepository.findByPlayerSessionId(playerSessionId)).thenReturn(playerSession1);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                codeExecutionService.getLatestSubmissionResult(invalidGameSessionId, p1.getProblemId(), playerSessionId)
        );

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        assertEquals("Game Session and Player Session do not belong together!", ex.getReason());
    }

    //getLatestSubmissionResult fail: Problem is not part of Game
    @Test
    void getLatestSubmissionResult_invalidProblemNotPartOfGame_throwsBadRequest() {

        testGameSession.getProblems().add(p1);
        playerSession1.setGameSession(testGameSession);
        Long playerSessionId = playerSession1.getPlayerSessionId();

        when(playerSessionRepository.findByPlayerSessionId(playerSessionId)).thenReturn(playerSession1);
        when(problemService.getProblemById(Mockito.any())).thenReturn(p3); //p3 is not part of game session

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                codeExecutionService.getLatestSubmissionResult(testGameSession.getGameSessionId(), p1.getProblemId(), playerSessionId)
        );

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        assertEquals("Problem does not belong to current Game Session!", ex.getReason());
    }

    //getLatestSubmissionResult fail: no submission found
    @Test
    void getLatestSubmissionResult_noSubmissionFound_throwsNotFound() {

        testGameSession.getProblems().add(p1);
        playerSession1.setGameSession(testGameSession);
        Long playerSessionId = playerSession1.getPlayerSessionId();

        when(playerSessionRepository.findByPlayerSessionId(playerSessionId)).thenReturn(playerSession1);
        when(problemService.getProblemById(p1.getProblemId())).thenReturn(p1);
        when(submissionRepository.findTopByGameSessionIdAndProblemIdAndPlayerSessionIdAndTypeOrderBySubmissionIdDesc(
                testGameSession.getGameSessionId(), p1.getProblemId(), playerSession1.getPlayerSessionId(), SubmissionType.SUBMIT)).thenReturn(null);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                codeExecutionService.getLatestSubmissionResult(testGameSession.getGameSessionId(), p1.getProblemId(), playerSessionId)
        );

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
        assertEquals("No final submission found", ex.getReason());
    }


    //getLatestSubmissionResult fail: Mismatch PlayerSessions
    @Test
    void getLatestSubmissionResult_mismatchPlayerSessions_throwsBadRequest() {

        testGameSession.getProblems().add(p1);
        playerSession1.setGameSession(testGameSession);
        Long playerSessionId = playerSession1.getPlayerSessionId();

        Long otherPlayerSessionId = 4L; 

        //mock submission that is does not belong to this player session
        Submission submission = new Submission();
        submission.setGameSessionId(testGameSession.getGameSessionId());
        submission.setProblemId(p1.getProblemId());
        submission.setPlayerSessionId(otherPlayerSessionId);

        when(playerSessionRepository.findByPlayerSessionId(playerSessionId)).thenReturn(playerSession1);
        when(problemService.getProblemById(p1.getProblemId())).thenReturn(p1);
        when(submissionRepository.findTopByGameSessionIdAndProblemIdAndPlayerSessionIdAndTypeOrderBySubmissionIdDesc(
                testGameSession.getGameSessionId(), p1.getProblemId(), playerSession1.getPlayerSessionId(), SubmissionType.SUBMIT)).thenReturn(submission);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                codeExecutionService.getLatestSubmissionResult(testGameSession.getGameSessionId(), p1.getProblemId(), playerSessionId)
        );

        assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
        assertEquals("Mismatch of playerSessionIds!", ex.getReason());
    }

    //getLatestSubmissionResult: submission is saved
    @Test
    void getLatestSubmissionResult_submissionIsSaved_success() {

        testGameSession.getProblems().add(p1);
        playerSession1.setGameSession(testGameSession);
        Long playerSessionId = playerSession1.getPlayerSessionId();

        Submission submission = new Submission();
        submission.setGameSessionId(testGameSession.getGameSessionId());
        submission.setProblemId(p1.getProblemId());
        submission.setPlayerSessionId(playerSessionId);
        submission.setStatus(SubmissionStatus.FINISHED);

        when(playerSessionRepository.findByPlayerSessionId(playerSessionId)).thenReturn(playerSession1);
        when(problemService.getProblemById(p1.getProblemId())).thenReturn(p1);
        when(submissionRepository.findTopByGameSessionIdAndProblemIdAndPlayerSessionIdAndTypeOrderBySubmissionIdDesc(
                testGameSession.getGameSessionId(), p1.getProblemId(), playerSession1.getPlayerSessionId(), SubmissionType.SUBMIT)).thenReturn(submission);
        
        codeExecutionService.getLatestSubmissionResult(testGameSession.getGameSessionId(), p1.getProblemId(), playerSessionId);

        verify(playerSessionRepository).saveAndFlush(playerSession1);
    }

    //getLatestSubmissionResult: points awarded
    @Test
    void getLatestSubmissionResult_correctSubmissionAwardsPoints_success() {

        testGameSession.getProblems().add(p1);
        playerSession1.setGameSession(testGameSession);
        playerSession1.setCurrentScore(0);
        Long playerSessionId = playerSession1.getPlayerSessionId();

        Submission submission = new Submission();
        submission.setGameSessionId(testGameSession.getGameSessionId());
        submission.setProblemId(p1.getProblemId());
        submission.setPlayerSessionId(playerSessionId);
        submission.setStatus(SubmissionStatus.FINISHED);
        submission.setPassedTestCases(5);

        when(playerSessionRepository.findByPlayerSessionId(playerSessionId)).thenReturn(playerSession1);
        when(problemService.getProblemById(p1.getProblemId())).thenReturn(p1);
        when(submissionRepository.findTopByGameSessionIdAndProblemIdAndPlayerSessionIdAndTypeOrderBySubmissionIdDesc(
                testGameSession.getGameSessionId(), p1.getProblemId(), playerSession1.getPlayerSessionId(), SubmissionType.SUBMIT)).thenReturn(submission);
        
        codeExecutionService.getLatestSubmissionResult(testGameSession.getGameSessionId(), p1.getProblemId(), playerSessionId);

        ArgumentCaptor<PlayerSession> captor = ArgumentCaptor.forClass(PlayerSession.class);
        verify(playerSessionRepository).save(captor.capture());

        PlayerSession savedPlayerSession = captor.getValue();
        assertEquals(submission.getPassedTestCases() * POINTS_PER_TEST_CASE, savedPlayerSession.getCurrentScore());

    }

    //getLatestSubmissionResult: points updated correctly
    @Test
    void getLatestSubmissionResult_correctPointsUpdate_success() {

        testGameSession.getProblems().add(p1);
        playerSession1.setGameSession(testGameSession);
        playerSession1.setCurrentScore(25);
        Long playerSessionId = playerSession1.getPlayerSessionId();

        Submission submission = new Submission();
        submission.setGameSessionId(testGameSession.getGameSessionId());
        submission.setProblemId(p1.getProblemId());
        submission.setPlayerSessionId(playerSessionId);
        submission.setStatus(SubmissionStatus.FINISHED);
        submission.setPassedTestCases(25);

        when(playerSessionRepository.findByPlayerSessionId(playerSessionId)).thenReturn(playerSession1);
        when(problemService.getProblemById(p1.getProblemId())).thenReturn(p1);
        when(submissionRepository.findTopByGameSessionIdAndProblemIdAndPlayerSessionIdAndTypeOrderBySubmissionIdDesc(
                testGameSession.getGameSessionId(), p1.getProblemId(), playerSession1.getPlayerSessionId(), SubmissionType.SUBMIT)).thenReturn(submission);
        
        codeExecutionService.getLatestSubmissionResult(testGameSession.getGameSessionId(), p1.getProblemId(), playerSessionId);

        ArgumentCaptor<PlayerSession> captor = ArgumentCaptor.forClass(PlayerSession.class);
        verify(playerSessionRepository).save(captor.capture());

        PlayerSession savedPlayerSession = captor.getValue();
        assertEquals(submission.getPassedTestCases() * POINTS_PER_TEST_CASE + 25, savedPlayerSession.getCurrentScore()); //player has 50 points now
    }

    //getLatestSubmissionResult: last problem should end the game
    @Test
    void getLatestSubmissionResult_lastProblem_endsGameAndReturnsEmpty() {
        playerSession1.setCurrentProblemIndex(0);
        testGameSession.getProblems().add(p1);
        playerSession1.setGameSession(testGameSession);

        Submission submission = new Submission();
        submission.setGameSessionId(testGameSession.getGameSessionId());
        submission.setProblemId(p1.getProblemId());
        submission.setPlayerSessionId(playerSession1.getPlayerSessionId());
        submission.setStatus(SubmissionStatus.FINISHED);
        submission.setPassedTestCases(5);

        when(playerSessionRepository.findByPlayerSessionId(playerSession1.getPlayerSessionId())).thenReturn(playerSession1);
        when(problemService.getProblemById(p1.getProblemId())).thenReturn(p1);
        when(submissionRepository.findTopByGameSessionIdAndProblemIdAndPlayerSessionIdAndTypeOrderBySubmissionIdDesc(
                testGameSession.getGameSessionId(), p1.getProblemId(), playerSession1.getPlayerSessionId(), SubmissionType.SUBMIT
        )).thenReturn(submission);
        when(gameService.handlePlayerProgression(playerSession1.getPlayerSessionId())).thenReturn(Optional.empty());

        Optional<GameRoundDTO> result = codeExecutionService.getLatestSubmissionResult(
                testGameSession.getGameSessionId(), p1.getProblemId(), playerSession1.getPlayerSessionId()
        );

        verify(gameService, times(1)).handlePlayerProgression(playerSession1.getPlayerSessionId());
        assertTrue(result.isEmpty());
    }

    @Test
    void getLatestSubmissionResult_notLastProblem_advancesPlayerAndReturnsGameRoundDTO() {

        playerSession1.setCurrentProblemIndex(0);
        testGameSession.getProblems().add(p1);
        testGameSession.getProblems().add(p3);
        playerSession1.setGameSession(testGameSession);

        Submission submission = new Submission();
        submission.setGameSessionId(testGameSession.getGameSessionId());
        submission.setProblemId(p1.getProblemId());
        submission.setPlayerSessionId(playerSession1.getPlayerSessionId());
        submission.setStatus(SubmissionStatus.FINISHED);
        submission.setPassedTestCases(5);

        GameRoundDTO expectedDTO = new GameRoundDTO();
        expectedDTO.setProblemId(p3.getProblemId());

        when(playerSessionRepository.findByPlayerSessionId(playerSession1.getPlayerSessionId())).thenReturn(playerSession1);
        when(problemService.getProblemById(p1.getProblemId())).thenReturn(p1);
        when(submissionRepository.findTopByGameSessionIdAndProblemIdAndPlayerSessionIdAndTypeOrderBySubmissionIdDesc(
                testGameSession.getGameSessionId(), p1.getProblemId(), playerSession1.getPlayerSessionId(), SubmissionType.SUBMIT
        )).thenReturn(submission);
        when(gameService.handlePlayerProgression(playerSession1.getPlayerSessionId())).thenReturn(Optional.of(expectedDTO));

        Optional<GameRoundDTO> result = codeExecutionService.getLatestSubmissionResult(
                testGameSession.getGameSessionId(), p1.getProblemId(), playerSession1.getPlayerSessionId()
        );

        verify(gameService, times(1)).handlePlayerProgression(playerSession1.getPlayerSessionId());

        GameRoundDTO gameRoundDTO = result.get();
        assertNotNull(gameRoundDTO);
        assertInstanceOf(GameRoundDTO.class, gameRoundDTO);
        assertEquals(p3.getProblemId(), gameRoundDTO.getProblemId());
    }
}