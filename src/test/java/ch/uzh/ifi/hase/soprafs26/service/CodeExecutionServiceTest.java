// package ch.uzh.ifi.hase.soprafs26.service;

// import ch.uzh.ifi.hase.soprafs26.constant.GameDifficulty;
// import ch.uzh.ifi.hase.soprafs26.constant.GameEndReason;
// import ch.uzh.ifi.hase.soprafs26.constant.GameLanguage;
// import ch.uzh.ifi.hase.soprafs26.constant.PlayerSessionStatus;
// import ch.uzh.ifi.hase.soprafs26.constant.SubmissionStatus;
// import ch.uzh.ifi.hase.soprafs26.constant.SubmissionType;
// import ch.uzh.ifi.hase.soprafs26.constant.Verdict;
// import ch.uzh.ifi.hase.soprafs26.entity.GameSession;
// import ch.uzh.ifi.hase.soprafs26.entity.PlayerSession;
// import ch.uzh.ifi.hase.soprafs26.entity.Problem;
// import ch.uzh.ifi.hase.soprafs26.entity.Submission;
// import ch.uzh.ifi.hase.soprafs26.entity.TestCase;
// import ch.uzh.ifi.hase.soprafs26.entity.User;
// import ch.uzh.ifi.hase.soprafs26.repository.PlayerSessionRepository;
// import ch.uzh.ifi.hase.soprafs26.repository.UserRepository;
// import ch.uzh.ifi.hase.soprafs26.repository.SubmissionRepository;
// import ch.uzh.ifi.hase.soprafs26.rest.dto.CodeExecutionPostDTO;
// import ch.uzh.ifi.hase.soprafs26.rest.dto.CodeRunDTO;
// import ch.uzh.ifi.hase.soprafs26.rest.dto.JudgeBatchResultDTO;
// import ch.uzh.ifi.hase.soprafs26.rest.dto.JudgeResultDTO;
// import ch.uzh.ifi.hase.soprafs26.rest.dto.JudgeStatusDTO;
// import ch.uzh.ifi.hase.soprafs26.rest.dto.JudgeTokenDTO;
// import org.junit.jupiter.api.BeforeEach;
// import org.junit.jupiter.api.Test;
// import org.mockito.*;
// import org.springframework.web.server.ResponseStatusException;

// import java.util.List;

// import static org.junit.jupiter.api.Assertions.*;
// import static org.mockito.ArgumentMatchers.any;
// import static org.mockito.ArgumentMatchers.anyList;
// import static org.mockito.Mockito.*;

// class CodeExecutionServiceTest {

//     @Mock
//     private ProblemService problemService;

//     @Mock
//     private SubmissionRepository submissionRepository;

//     @Mock
//     private JudgeService judgeService;

//     @Mock
//     private UserRepository userRepository;

//     @Mock
//     private PlayerSessionRepository playerSessionRepository;

//     @Mock
//     private GameService gameService;

//     @InjectMocks
//     private CodeExecutionService codeExecutionService;

//     @BeforeEach
//     void setup() {
//         MockitoAnnotations.openMocks(this);
//         codeExecutionService = new CodeExecutionService(
//                 problemService,
//                 judgeService,
//                 submissionRepository,
//                 userRepository,
//                 playerSessionRepository,
//                 gameService
//         );
//     }

//     private PlayerSession makePlayerSession(int currentProblemIndex, int totalProblems) {
//         Problem[] problems = new Problem[totalProblems];
//         for (int i = 0; i < totalProblems; i++) {
//             problems[i] = new Problem();
//         }
//         GameSession gameSession = new GameSession();
//         gameSession.setProblems(List.of(problems));

//         User user = new User();
//         user.setTotalPoints(0L);

//         PlayerSession ps = new PlayerSession();
//         ps.setCurrentProblemIndex(currentProblemIndex);
//         ps.setCurrentScore(0);
//         ps.setPlayer(user);
//         ps.setGameSession(gameSession);
//         return ps;
//     }

//     @Test
//     void runCode_validInput_returnsFinishedStatusAndVerdict_andSavesSubmission() {
//         Long gameSessionId = 1L;
//         Long problemId = 2L;
//         Long playerSessionId = 3L;

//         CodeExecutionPostDTO request = new CodeExecutionPostDTO();
//         request.setPlayerSessionId(playerSessionId);
//         // Updated to use "def solve" and "return"
//         request.setSourceCode("def solve(input_data):\n    return input_data[::-1]");

//         TestCase tc1 = new TestCase();
//         tc1.setInput("hello");
//         tc1.setExpectedOutput("olleh");

//         TestCase tc2 = new TestCase();
//         tc2.setInput("abc");
//         tc2.setExpectedOutput("cba");

//         Problem problem = new Problem();
//         problem.setProblemId(problemId);
//         problem.setGameLanguage(GameLanguage.PYTHON);
//         problem.setGameDifficulty(GameDifficulty.EASY);
//         problem.setTestCases(List.of(tc1, tc2));

//         JudgeTokenDTO token1 = new JudgeTokenDTO();
//         token1.setJudgeToken("tok1");

//         JudgeTokenDTO token2 = new JudgeTokenDTO();
//         token2.setJudgeToken("tok2");

//         JudgeStatusDTO acceptedStatus = new JudgeStatusDTO();
//         acceptedStatus.setId(3);
//         acceptedStatus.setDescription("Accepted");

//         JudgeResultDTO result1 = new JudgeResultDTO();
//         result1.setToken("tok1");
//         result1.setStatus(acceptedStatus);

//         JudgeResultDTO result2 = new JudgeResultDTO();
//         result2.setToken("tok2");
//         result2.setStatus(acceptedStatus);

//         JudgeBatchResultDTO batchResult = new JudgeBatchResultDTO();
//         batchResult.setSubmissions(List.of(result1, result2));

//         when(problemService.getProblemById(problemId)).thenReturn(problem);
//         when(judgeService.submitBatch(any())).thenReturn(List.of(token1, token2));
//         when(judgeService.getBatchSubmissionResults(anyList())).thenReturn(batchResult);
//         when(submissionRepository.save(any(Submission.class))).thenAnswer(invocation -> invocation.getArgument(0));

//         CodeRunDTO result = codeExecutionService.runCode(gameSessionId, problemId, request);

//         assertNotNull(result);
//         assertEquals(gameSessionId, result.getGameSessionId());
//         assertEquals(problemId, result.getProblemId());
//         assertEquals(playerSessionId, result.getPlayerSessionId());
//         assertEquals(SubmissionStatus.FINISHED, result.getSubmissionStatus());
//         assertEquals(Verdict.CORRECT_ANSWER, result.getVerdict());

//         ArgumentCaptor<Submission> submissionCaptor = ArgumentCaptor.forClass(Submission.class);
//         verify(submissionRepository, times(2)).save(submissionCaptor.capture());

//         Submission finalSavedSubmission = submissionCaptor.getAllValues().get(1);
//         assertEquals(gameSessionId, finalSavedSubmission.getGameSessionId());
//         assertEquals(problemId, finalSavedSubmission.getProblemId());
//         assertEquals(playerSessionId, finalSavedSubmission.getPlayerSessionId());
//         assertEquals(SubmissionType.RUN, finalSavedSubmission.getType());
//         assertEquals(2, finalSavedSubmission.getPassedTestCases());
//         assertEquals(2, finalSavedSubmission.getTotalTestCases());
//         assertEquals(SubmissionStatus.FINISHED, finalSavedSubmission.getStatus());
//         assertEquals(Verdict.CORRECT_ANSWER, finalSavedSubmission.getVerdict());
//         assertNotNull(finalSavedSubmission.getJudgeTokensJson());
//     }

//     @Test
//     void submitCode_alreadySubmitted_throwsConflict() {
//         Long gameSessionId = 1L;
//         Long problemId = 2L;
//         Long playerSessionId = 3L;

//         CodeExecutionPostDTO request = new CodeExecutionPostDTO();
//         request.setPlayerSessionId(playerSessionId);
//         // Updated to use "def solve" and "return"
//         request.setSourceCode("def solve(x):\n    return x");

//         when(submissionRepository.existsByGameSessionIdAndProblemIdAndPlayerSessionIdAndType(
//                 gameSessionId, problemId, playerSessionId, SubmissionType.SUBMIT
//         )).thenReturn(true);

//         ResponseStatusException exception = assertThrows(
//                 ResponseStatusException.class,
//                 () -> codeExecutionService.submitCode(gameSessionId, problemId, request)
//         );

//         assertEquals(409, exception.getStatusCode().value());
//         verify(problemService, never()).getProblemById(any());
//         verify(judgeService, never()).submitBatch(any());
//         verify(submissionRepository, never()).save(any());
//     }

//     @Test
//     void runCode_blankSourceCode_throwsBadRequest() {
//         CodeExecutionPostDTO request = new CodeExecutionPostDTO();
//         request.setPlayerSessionId(3L);
//         request.setSourceCode("   ");

//         ResponseStatusException exception = assertThrows(
//                 ResponseStatusException.class,
//                 () -> codeExecutionService.runCode(1L, 2L, request)
//         );

//         assertEquals(400, exception.getStatusCode().value());
//         verify(problemService, never()).getProblemById(any());
//         verify(judgeService, never()).submitBatch(any());
//         verify(submissionRepository, never()).save(any());
//     }

//     @Test
//     void runCode_problemWithoutTestCases_throwsBadRequest() {
//         CodeExecutionPostDTO request = new CodeExecutionPostDTO();
//         request.setPlayerSessionId(3L);
//         // Updated to use "def solve" and "return"
//         request.setSourceCode("def solve(input_data):\n    return input_data");

//         Problem problem = new Problem();
//         problem.setProblemId(2L);
//         problem.setGameLanguage(GameLanguage.PYTHON);
//         problem.setTestCases(List.of());

//         when(problemService.getProblemById(2L)).thenReturn(problem);

//         ResponseStatusException exception = assertThrows(
//                 ResponseStatusException.class,
//                 () -> codeExecutionService.runCode(1L, 2L, request)
//         );

//         assertEquals(400, exception.getStatusCode().value());
//         verify(judgeService, never()).submitBatch(any());
//         verify(submissionRepository, never()).save(any());
//     }

//     @Test
//     void runCode_wrongAnswer_returnsFinishedAndWrongAnswerVerdict() {
//         Long gameSessionId = 1L;
//         Long problemId = 2L;
//         Long playerSessionId = 3L;

//         CodeExecutionPostDTO request = new CodeExecutionPostDTO();
//         request.setPlayerSessionId(playerSessionId);
//         // Updated to use "def solve" and "return"
//         request.setSourceCode("def solve(input_data):\n    return input_data");

//         TestCase tc = new TestCase();
//         tc.setInput("abc");
//         tc.setExpectedOutput("cba");

//         Problem problem = new Problem();
//         problem.setProblemId(problemId);
//         problem.setGameLanguage(GameLanguage.PYTHON);
//         problem.setGameDifficulty(GameDifficulty.EASY);
//         problem.setTestCases(List.of(tc));

//         JudgeTokenDTO token = new JudgeTokenDTO();
//         token.setJudgeToken("tok1");

//         JudgeStatusDTO wrongAnswerStatus = new JudgeStatusDTO();
//         wrongAnswerStatus.setId(4);
//         wrongAnswerStatus.setDescription("Wrong Answer");

//         JudgeResultDTO result1 = new JudgeResultDTO();
//         result1.setToken("tok1");
//         result1.setStatus(wrongAnswerStatus);

//         JudgeBatchResultDTO batchResult = new JudgeBatchResultDTO();
//         batchResult.setSubmissions(List.of(result1));

//         when(problemService.getProblemById(problemId)).thenReturn(problem);
//         when(judgeService.submitBatch(any())).thenReturn(List.of(token));
//         when(judgeService.getBatchSubmissionResults(anyList())).thenReturn(batchResult);
//         when(submissionRepository.save(any(Submission.class))).thenAnswer(invocation -> invocation.getArgument(0));

//         CodeRunDTO result = codeExecutionService.runCode(gameSessionId, problemId, request);

//         assertEquals(SubmissionStatus.FINISHED, result.getSubmissionStatus());
//         assertEquals(Verdict.WRONG_ANSWER, result.getVerdict());
//         assertEquals(0, result.getPassedTestCases());
//         assertEquals(1, result.getTotalTestCases());
//     }

//     @Test
//     void runCode_compileError_returnsFinishedAndCompileErrorVerdict() {
//         Long gameSessionId = 1L;
//         Long problemId = 2L;
//         Long playerSessionId = 3L;

//         CodeExecutionPostDTO request = new CodeExecutionPostDTO();
//         request.setPlayerSessionId(playerSessionId);
//         // Updated to use "def solve" and "return"
//         request.setSourceCode("def solve(input_data)\n    return input_data");

//         TestCase tc = new TestCase();
//         tc.setInput("abc");
//         tc.setExpectedOutput("abc");

//         Problem problem = new Problem();
//         problem.setProblemId(problemId);
//         problem.setGameLanguage(GameLanguage.PYTHON);
//         problem.setGameDifficulty(GameDifficulty.EASY);
//         problem.setTestCases(List.of(tc));

//         JudgeTokenDTO token = new JudgeTokenDTO();
//         token.setJudgeToken("tok1");

//         JudgeStatusDTO compileStatus = new JudgeStatusDTO();
//         compileStatus.setId(6);
//         compileStatus.setDescription("Compilation Error");

//         JudgeResultDTO result1 = new JudgeResultDTO();
//         result1.setToken("tok1");
//         result1.setStatus(compileStatus);

//         JudgeBatchResultDTO batchResult = new JudgeBatchResultDTO();
//         batchResult.setSubmissions(List.of(result1));

//         when(problemService.getProblemById(problemId)).thenReturn(problem);
//         when(judgeService.submitBatch(any())).thenReturn(List.of(token));
//         when(judgeService.getBatchSubmissionResults(anyList())).thenReturn(batchResult);
//         when(submissionRepository.save(any(Submission.class))).thenAnswer(invocation -> invocation.getArgument(0));

//         CodeRunDTO result = codeExecutionService.runCode(gameSessionId, problemId, request);

//         assertEquals(SubmissionStatus.FINISHED, result.getSubmissionStatus());
//         assertEquals(Verdict.COMPILE_ERROR, result.getVerdict());
//     }

//     @Test
//     void runCode_timeLimitExceeded_returnsFinishedAndTimeLimitVerdict() {
//         Long gameSessionId = 1L;
//         Long problemId = 2L;
//         Long playerSessionId = 3L;

//         CodeExecutionPostDTO request = new CodeExecutionPostDTO();
//         request.setPlayerSessionId(playerSessionId);
//         // Updated to use "def solve" and "return"
//         request.setSourceCode("def solve(input_data):\n    while True:\n        pass\n    return input_data");

//         TestCase tc = new TestCase();
//         tc.setInput("abc");
//         tc.setExpectedOutput("abc");

//         Problem problem = new Problem();
//         problem.setProblemId(problemId);
//         problem.setGameLanguage(GameLanguage.PYTHON);
//         problem.setGameDifficulty(GameDifficulty.EASY);
//         problem.setTestCases(List.of(tc));

//         JudgeTokenDTO token = new JudgeTokenDTO();
//         token.setJudgeToken("tok1");

//         JudgeStatusDTO tleStatus = new JudgeStatusDTO();
//         tleStatus.setId(5);
//         tleStatus.setDescription("Time Limit Exceeded");

//         JudgeResultDTO result1 = new JudgeResultDTO();
//         result1.setToken("tok1");
//         result1.setStatus(tleStatus);

//         JudgeBatchResultDTO batchResult = new JudgeBatchResultDTO();
//         batchResult.setSubmissions(List.of(result1));

//         when(problemService.getProblemById(problemId)).thenReturn(problem);
//         when(judgeService.submitBatch(any())).thenReturn(List.of(token));
//         when(judgeService.getBatchSubmissionResults(anyList())).thenReturn(batchResult);
//         when(submissionRepository.save(any(Submission.class))).thenAnswer(invocation -> invocation.getArgument(0));

//         CodeRunDTO result = codeExecutionService.runCode(gameSessionId, problemId, request);

//         assertEquals(SubmissionStatus.FINISHED, result.getSubmissionStatus());
//         assertEquals(Verdict.TIME_LIMIT_EXCEEDED, result.getVerdict());
//     }

//     /**
//      * when we run the code we have a time limit for how long we wait 
//      * for the judge result. The user should then get a "running" status and 
//      * a "pending" verdict, which indicates that the code is still being judged.
//     */
//     @Test
//     void runCode_processingAfterPollingWindow_returnsRunningAndPendingVerdict() {
//         Long gameSessionId = 1L;
//         Long problemId = 2L;
//         Long playerSessionId = 3L;

//         CodeExecutionPostDTO request = new CodeExecutionPostDTO();
//         request.setPlayerSessionId(playerSessionId);
//         // Updated to use "def solve" and "return"
//         request.setSourceCode("def solve(input_data):\n    return input_data");

//         TestCase tc = new TestCase();
//         tc.setInput("abc");
//         tc.setExpectedOutput("abc");

//         Problem problem = new Problem();
//         problem.setProblemId(problemId);
//         problem.setGameLanguage(GameLanguage.PYTHON);
//         problem.setGameDifficulty(GameDifficulty.EASY);
//         problem.setTestCases(List.of(tc));

//         JudgeTokenDTO token = new JudgeTokenDTO();
//         token.setJudgeToken("tok1");

//         JudgeStatusDTO processingStatus = new JudgeStatusDTO();
//         processingStatus.setId(2);
//         processingStatus.setDescription("Processing");

//         JudgeResultDTO result1 = new JudgeResultDTO();
//         result1.setToken("tok1");
//         result1.setStatus(processingStatus);

//         JudgeBatchResultDTO batchResult = new JudgeBatchResultDTO();
//         batchResult.setSubmissions(List.of(result1));

//         when(problemService.getProblemById(problemId)).thenReturn(problem);
//         when(judgeService.submitBatch(any())).thenReturn(List.of(token));
//         when(judgeService.getBatchSubmissionResults(anyList()))
//                 .thenReturn(batchResult, batchResult, batchResult);
//         when(submissionRepository.save(any(Submission.class))).thenAnswer(invocation -> invocation.getArgument(0));

//         CodeRunDTO result = codeExecutionService.runCode(gameSessionId, problemId, request);

//         assertEquals(SubmissionStatus.RUNNING, result.getSubmissionStatus());
//         assertEquals(Verdict.PENDING, result.getVerdict());
//     }

//     @Test
//     void submitCode_correctAnswer_awardsOnePointPerPassedTestCase() {
//         Long gameSessionId = 1L;
//         Long problemId = 2L;
//         Long playerSessionId = 3L;

//         CodeExecutionPostDTO request = new CodeExecutionPostDTO();
//         request.setPlayerSessionId(playerSessionId);
//         request.setSourceCode("def solve(x):\n    return x[::-1]");

//         TestCase tc1 = new TestCase();
//         tc1.setInput("ab");
//         tc1.setExpectedOutput("ba");
//         TestCase tc2 = new TestCase();
//         tc2.setInput("cd");
//         tc2.setExpectedOutput("dc");

//         Problem problem = new Problem();
//         problem.setProblemId(problemId);
//         problem.setGameLanguage(GameLanguage.PYTHON);
//         problem.setGameDifficulty(GameDifficulty.EASY);
//         problem.setTestCases(List.of(tc1, tc2));

//         JudgeTokenDTO token1 = new JudgeTokenDTO();
//         token1.setJudgeToken("tok1");
//         JudgeTokenDTO token2 = new JudgeTokenDTO();
//         token2.setJudgeToken("tok2");

//         JudgeStatusDTO accepted = new JudgeStatusDTO();
//         accepted.setId(3);
//         accepted.setDescription("Accepted");

//         JudgeResultDTO r1 = new JudgeResultDTO();
//         r1.setToken("tok1");
//         r1.setStatus(accepted);
//         JudgeResultDTO r2 = new JudgeResultDTO();
//         r2.setToken("tok2");
//         r2.setStatus(accepted);

//         JudgeBatchResultDTO batchResult = new JudgeBatchResultDTO();
//         batchResult.setSubmissions(List.of(r1, r2));

//         User user = new User();
//         user.setTotalPoints(0L);

//         PlayerSession playerSession = new PlayerSession();
//         playerSession.setCurrentScore(0);
//         playerSession.setPlayer(user);
//         playerSession.setCurrentProblemIndex(0);
//         GameSession gameSession = new GameSession();
//         gameSession.setProblems(List.of(new Problem(), new Problem(), new Problem()));
//         playerSession.setGameSession(gameSession);

//         when(submissionRepository.existsByGameSessionIdAndProblemIdAndPlayerSessionIdAndType(
//                 gameSessionId, problemId, playerSessionId, SubmissionType.SUBMIT)).thenReturn(false);
//         when(problemService.getProblemById(problemId)).thenReturn(problem);
//         when(judgeService.submitBatch(any())).thenReturn(List.of(token1, token2));
//         when(judgeService.getBatchSubmissionResults(anyList())).thenReturn(batchResult);
//         when(submissionRepository.save(any(Submission.class))).thenAnswer(i -> i.getArgument(0));
//         when(playerSessionRepository.findByPlayerSessionId(playerSessionId)).thenReturn(playerSession);

//         codeExecutionService.submitCode(gameSessionId, problemId, request);

//         assertEquals(2, playerSession.getCurrentScore());
//         assertEquals(2L, user.getTotalPoints());
//         verify(playerSessionRepository, atLeastOnce()).save(playerSession);
//         verify(userRepository).save(user);
//     }

//     @Test
//     void submitCode_wrongAnswer_doesNotAwardPoints() {
//         Long gameSessionId = 1L;
//         Long problemId = 2L;
//         Long playerSessionId = 3L;

//         CodeExecutionPostDTO request = new CodeExecutionPostDTO();
//         request.setPlayerSessionId(playerSessionId);
//         request.setSourceCode("def solve(x):\n    return x");

//         TestCase tc = new TestCase();
//         tc.setInput("ab");
//         tc.setExpectedOutput("ba");

//         Problem problem = new Problem();
//         problem.setProblemId(problemId);
//         problem.setGameLanguage(GameLanguage.PYTHON);
//         problem.setGameDifficulty(GameDifficulty.EASY);
//         problem.setTestCases(List.of(tc));

//         JudgeTokenDTO token = new JudgeTokenDTO();
//         token.setJudgeToken("tok1");

//         JudgeStatusDTO wrongAnswer = new JudgeStatusDTO();
//         wrongAnswer.setId(4);
//         wrongAnswer.setDescription("Wrong Answer");

//         JudgeResultDTO result = new JudgeResultDTO();
//         result.setToken("tok1");
//         result.setStatus(wrongAnswer);

//         JudgeBatchResultDTO batchResult = new JudgeBatchResultDTO();
//         batchResult.setSubmissions(List.of(result));

//         when(submissionRepository.existsByGameSessionIdAndProblemIdAndPlayerSessionIdAndType(
//                 gameSessionId, problemId, playerSessionId, SubmissionType.SUBMIT)).thenReturn(false);
//         when(problemService.getProblemById(problemId)).thenReturn(problem);
//         when(judgeService.submitBatch(any())).thenReturn(List.of(token));
//         when(judgeService.getBatchSubmissionResults(anyList())).thenReturn(batchResult);
//         when(submissionRepository.save(any(Submission.class))).thenAnswer(i -> i.getArgument(0));

//         codeExecutionService.submitCode(gameSessionId, problemId, request);

//         verify(playerSessionRepository, never()).save(any());
//         verify(userRepository, never()).save(any());
//     }

//     @Test
//     void submitCode_correctAnswer_playerSessionNotFound_doesNotThrow() {
//         Long gameSessionId = 1L;
//         Long problemId = 2L;
//         Long playerSessionId = 3L;

//         CodeExecutionPostDTO request = new CodeExecutionPostDTO();
//         request.setPlayerSessionId(playerSessionId);
//         request.setSourceCode("def solve(x):\n    return x[::-1]");

//         TestCase tc = new TestCase();
//         tc.setInput("ab");
//         tc.setExpectedOutput("ba");

//         Problem problem = new Problem();
//         problem.setProblemId(problemId);
//         problem.setGameLanguage(GameLanguage.PYTHON);
//         problem.setGameDifficulty(GameDifficulty.EASY);
//         problem.setTestCases(List.of(tc));

//         JudgeTokenDTO token = new JudgeTokenDTO();
//         token.setJudgeToken("tok1");

//         JudgeStatusDTO accepted = new JudgeStatusDTO();
//         accepted.setId(3);
//         accepted.setDescription("Accepted");

//         JudgeResultDTO result = new JudgeResultDTO();
//         result.setToken("tok1");
//         result.setStatus(accepted);

//         JudgeBatchResultDTO batchResult = new JudgeBatchResultDTO();
//         batchResult.setSubmissions(List.of(result));

//         when(submissionRepository.existsByGameSessionIdAndProblemIdAndPlayerSessionIdAndType(
//                 gameSessionId, problemId, playerSessionId, SubmissionType.SUBMIT)).thenReturn(false);
//         when(problemService.getProblemById(problemId)).thenReturn(problem);
//         when(judgeService.submitBatch(any())).thenReturn(List.of(token));
//         when(judgeService.getBatchSubmissionResults(anyList())).thenReturn(batchResult);
//         when(submissionRepository.save(any(Submission.class))).thenAnswer(i -> i.getArgument(0));
//         when(playerSessionRepository.findByPlayerSessionId(playerSessionId)).thenReturn(null);

//         assertDoesNotThrow(() -> codeExecutionService.submitCode(gameSessionId, problemId, request));
//         verify(playerSessionRepository, never()).save(any());
//         verify(userRepository, never()).save(any());
//     }

//     @Test
//     void submitCode_correctAnswer_accumulatesWithExistingPoints() {
//         Long gameSessionId = 1L;
//         Long problemId = 2L;
//         Long playerSessionId = 3L;

//         CodeExecutionPostDTO request = new CodeExecutionPostDTO();
//         request.setPlayerSessionId(playerSessionId);
//         request.setSourceCode("def solve(x):\n    return x[::-1]");

//         TestCase tc1 = new TestCase();
//         tc1.setInput("ab");
//         tc1.setExpectedOutput("ba");
//         TestCase tc2 = new TestCase();
//         tc2.setInput("cd");
//         tc2.setExpectedOutput("dc");
//         TestCase tc3 = new TestCase();
//         tc3.setInput("ef");
//         tc3.setExpectedOutput("fe");

//         Problem problem = new Problem();
//         problem.setProblemId(problemId);
//         problem.setGameLanguage(GameLanguage.PYTHON);
//         problem.setGameDifficulty(GameDifficulty.EASY);
//         problem.setTestCases(List.of(tc1, tc2, tc3));

//         JudgeTokenDTO token1 = new JudgeTokenDTO();
//         token1.setJudgeToken("tok1");
//         JudgeTokenDTO token2 = new JudgeTokenDTO();
//         token2.setJudgeToken("tok2");
//         JudgeTokenDTO token3 = new JudgeTokenDTO();
//         token3.setJudgeToken("tok3");

//         JudgeStatusDTO accepted = new JudgeStatusDTO();
//         accepted.setId(3);
//         accepted.setDescription("Accepted");

//         JudgeResultDTO r1 = new JudgeResultDTO();
//         r1.setToken("tok1");
//         r1.setStatus(accepted);
//         JudgeResultDTO r2 = new JudgeResultDTO();
//         r2.setToken("tok2");
//         r2.setStatus(accepted);
//         JudgeResultDTO r3 = new JudgeResultDTO();
//         r3.setToken("tok3");
//         r3.setStatus(accepted);

//         JudgeBatchResultDTO batchResult = new JudgeBatchResultDTO();
//         batchResult.setSubmissions(List.of(r1, r2, r3));

//         User user = new User();
//         user.setTotalPoints(10L);

//         PlayerSession playerSession = new PlayerSession();
//         playerSession.setCurrentScore(5);
//         playerSession.setPlayer(user);
//         playerSession.setCurrentProblemIndex(0);
//         GameSession gameSession = new GameSession();
//         gameSession.setProblems(List.of(new Problem(), new Problem(), new Problem()));
//         playerSession.setGameSession(gameSession);

//         when(submissionRepository.existsByGameSessionIdAndProblemIdAndPlayerSessionIdAndType(
//                 gameSessionId, problemId, playerSessionId, SubmissionType.SUBMIT)).thenReturn(false);
//         when(problemService.getProblemById(problemId)).thenReturn(problem);
//         when(judgeService.submitBatch(any())).thenReturn(List.of(token1, token2, token3));
//         when(judgeService.getBatchSubmissionResults(anyList())).thenReturn(batchResult);
//         when(submissionRepository.save(any(Submission.class))).thenAnswer(i -> i.getArgument(0));
//         when(playerSessionRepository.findByPlayerSessionId(playerSessionId)).thenReturn(playerSession);

//         codeExecutionService.submitCode(gameSessionId, problemId, request);

//         assertEquals(8, playerSession.getCurrentScore()); // 5 existing + 3 passed test cases
//         assertEquals(13L, user.getTotalPoints()); // 10 existing + 3 passed test cases
//         verify(playerSessionRepository, atLeastOnce()).save(playerSession);
//         verify(userRepository).save(user);
//     }


//     @Test
//     void submitCode_correctAnswer_lastProblem_triggersGameEnd() {
//         Long gameSessionId = 1L;
//         Long problemId = 2L;
//         Long playerSessionId = 3L;

//         CodeExecutionPostDTO request = new CodeExecutionPostDTO();
//         request.setPlayerSessionId(playerSessionId);
//         request.setSourceCode("def solve(x):\n    return x[::-1]");

//         TestCase tc = new TestCase();
//         tc.setInput("ab");
//         tc.setExpectedOutput("ba");

//         Problem problem = new Problem();
//         problem.setProblemId(problemId);
//         problem.setGameLanguage(GameLanguage.PYTHON);
//         problem.setGameDifficulty(GameDifficulty.EASY);
//         problem.setTestCases(List.of(tc));

//         JudgeTokenDTO token = new JudgeTokenDTO();
//         token.setJudgeToken("tok1");

//         JudgeStatusDTO accepted = new JudgeStatusDTO();
//         accepted.setId(3);
//         accepted.setDescription("Accepted");

//         JudgeResultDTO r = new JudgeResultDTO();
//         r.setToken("tok1");
//         r.setStatus(accepted);

//         JudgeBatchResultDTO batchResult = new JudgeBatchResultDTO();
//         batchResult.setSubmissions(List.of(r));

//         // player is on index 1 (last) of a 2 problem game so it should end
//         PlayerSession playerSession = makePlayerSession(1, 2);

//         when(submissionRepository.existsByGameSessionIdAndProblemIdAndPlayerSessionIdAndType(
//                 gameSessionId, problemId, playerSessionId, SubmissionType.SUBMIT)).thenReturn(false);
//         when(problemService.getProblemById(problemId)).thenReturn(problem);
//         when(judgeService.submitBatch(any())).thenReturn(List.of(token));
//         when(judgeService.getBatchSubmissionResults(anyList())).thenReturn(batchResult);
//         when(submissionRepository.save(any(Submission.class))).thenAnswer(i -> i.getArgument(0));
//         when(playerSessionRepository.findByPlayerSessionId(playerSessionId)).thenReturn(playerSession);

//         codeExecutionService.submitCode(gameSessionId, problemId, request);

//         assertEquals(PlayerSessionStatus.FINISHED, playerSession.getPlayerSessionStatus());
//         verify(gameService).endGameSession(playerSession.getGameSession(), GameEndReason.PLAYER_FINISHED);
//     }

//     @Test
//     void submitCode_correctAnswer_notLastProblem_advancesProblemIndex() {
//         Long gameSessionId = 1L;
//         Long problemId = 2L;
//         Long playerSessionId = 3L;

//         CodeExecutionPostDTO request = new CodeExecutionPostDTO();
//         request.setPlayerSessionId(playerSessionId);
//         request.setSourceCode("def solve(x):\n    return x[::-1]");

//         TestCase tc = new TestCase();
//         tc.setInput("ab");
//         tc.setExpectedOutput("ba");

//         Problem problem = new Problem();
//         problem.setProblemId(problemId);
//         problem.setGameLanguage(GameLanguage.PYTHON);
//         problem.setGameDifficulty(GameDifficulty.EASY);
//         problem.setTestCases(List.of(tc));

//         JudgeTokenDTO token = new JudgeTokenDTO();
//         token.setJudgeToken("tok1");

//         JudgeStatusDTO accepted = new JudgeStatusDTO();
//         accepted.setId(3);
//         accepted.setDescription("Accepted");

//         JudgeResultDTO r = new JudgeResultDTO();
//         r.setToken("tok1");
//         r.setStatus(accepted);

//         JudgeBatchResultDTO batchResult = new JudgeBatchResultDTO();
//         batchResult.setSubmissions(List.of(r));

//         // player is on index 0 of a 3-problem game so it should advance 
//         PlayerSession playerSession = makePlayerSession(0, 3);

//         when(submissionRepository.existsByGameSessionIdAndProblemIdAndPlayerSessionIdAndType(
//                 gameSessionId, problemId, playerSessionId, SubmissionType.SUBMIT)).thenReturn(false);
//         when(problemService.getProblemById(problemId)).thenReturn(problem);
//         when(judgeService.submitBatch(any())).thenReturn(List.of(token));
//         when(judgeService.getBatchSubmissionResults(anyList())).thenReturn(batchResult);
//         when(submissionRepository.save(any(Submission.class))).thenAnswer(i -> i.getArgument(0));
//         when(playerSessionRepository.findByPlayerSessionId(playerSessionId)).thenReturn(playerSession);

//         codeExecutionService.submitCode(gameSessionId, problemId, request);

//         assertEquals(1, playerSession.getCurrentProblemIndex());
//         verify(gameService, never()).endGameSession(any(), any());
//     }

//     @Test
//     void submitCode_playerSessionNull_doesNotTriggerGameEnd() {
//         Long gameSessionId = 1L;
//         Long problemId = 2L;
//         Long playerSessionId = 3L;

//         CodeExecutionPostDTO request = new CodeExecutionPostDTO();
//         request.setPlayerSessionId(playerSessionId);
//         request.setSourceCode("def solve(x):\n    return x[::-1]");

//         TestCase tc = new TestCase();
//         tc.setInput("ab");
//         tc.setExpectedOutput("ba");

//         Problem problem = new Problem();
//         problem.setProblemId(problemId);
//         problem.setGameLanguage(GameLanguage.PYTHON);
//         problem.setGameDifficulty(GameDifficulty.EASY);
//         problem.setTestCases(List.of(tc));

//         JudgeTokenDTO token = new JudgeTokenDTO();
//         token.setJudgeToken("tok1");

//         JudgeStatusDTO accepted = new JudgeStatusDTO();
//         accepted.setId(3);
//         accepted.setDescription("Accepted");

//         JudgeResultDTO r = new JudgeResultDTO();
//         r.setToken("tok1");
//         r.setStatus(accepted);

//         JudgeBatchResultDTO batchResult = new JudgeBatchResultDTO();
//         batchResult.setSubmissions(List.of(r));

//         when(submissionRepository.existsByGameSessionIdAndProblemIdAndPlayerSessionIdAndType(
//                 gameSessionId, problemId, playerSessionId, SubmissionType.SUBMIT)).thenReturn(false);
//         when(problemService.getProblemById(problemId)).thenReturn(problem);
//         when(judgeService.submitBatch(any())).thenReturn(List.of(token));
//         when(judgeService.getBatchSubmissionResults(anyList())).thenReturn(batchResult);
//         when(submissionRepository.save(any(Submission.class))).thenAnswer(i -> i.getArgument(0));
//         when(playerSessionRepository.findByPlayerSessionId(playerSessionId)).thenReturn(null);

//         assertDoesNotThrow(() -> codeExecutionService.submitCode(gameSessionId, problemId, request));
//         verify(gameService, never()).endGameSession(any(), any());
//     }

// }