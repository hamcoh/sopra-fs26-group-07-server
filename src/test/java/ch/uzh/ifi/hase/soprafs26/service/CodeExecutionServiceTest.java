package ch.uzh.ifi.hase.soprafs26.service;

import ch.uzh.ifi.hase.soprafs26.constant.GameDifficulty;
import ch.uzh.ifi.hase.soprafs26.constant.GameLanguage;
import ch.uzh.ifi.hase.soprafs26.constant.SubmissionStatus;
import ch.uzh.ifi.hase.soprafs26.constant.SubmissionType;
import ch.uzh.ifi.hase.soprafs26.entity.Problem;
import ch.uzh.ifi.hase.soprafs26.entity.Submission;
import ch.uzh.ifi.hase.soprafs26.entity.TestCase;
import ch.uzh.ifi.hase.soprafs26.repository.SubmissionRepository;
import ch.uzh.ifi.hase.soprafs26.rest.dto.CodeExecutionPostDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.CodeRunDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.JudgeTokenDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class CodeExecutionServiceTest {

    @Mock
    private ProblemService problemService;

    @Mock
    private SubmissionRepository submissionRepository;

    @Mock
    private JudgeService judgeService;

    @InjectMocks
    private CodeExecutionService codeExecutionService;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        codeExecutionService = new CodeExecutionService(
                problemService,
                judgeService,
                submissionRepository
        );
    }

    @Test
    void runCode_validInput_returnsTokensAndSavesSubmission() {
        Long gameSessionId = 1L;
        Long problemId = 2L;
        Long playerSessionId = 3L;

        CodeExecutionPostDTO request = new CodeExecutionPostDTO();
        request.setPlayerSessionId(playerSessionId);
        request.setSourceCode("def solution(input_data):\n    return input_data[::-1]");

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

        JudgeTokenDTO token1 = new JudgeTokenDTO();
        token1.setJudgeToken("tok1");

        JudgeTokenDTO token2 = new JudgeTokenDTO();
        token2.setJudgeToken("tok2");

        when(problemService.getProblemById(problemId)).thenReturn(problem);
        when(judgeService.submitBatch(any())).thenReturn(List.of(token1, token2));
        when(submissionRepository.save(any(Submission.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CodeRunDTO result = codeExecutionService.runCode(gameSessionId, problemId, request);

        assertNotNull(result);
        assertEquals(gameSessionId, result.getGameSessionId());
        assertEquals(problemId, result.getProblemId());
        assertEquals(playerSessionId, result.getPlayerSessionId());
        assertEquals(2, result.getTokens().size());
        assertEquals("tok1", result.getTokens().get(0));
        assertEquals("tok2", result.getTokens().get(1));

        ArgumentCaptor<Submission> submissionCaptor = ArgumentCaptor.forClass(Submission.class);
        verify(submissionRepository, times(1)).save(submissionCaptor.capture());

        Submission savedSubmission = submissionCaptor.getValue();
        assertEquals(gameSessionId, savedSubmission.getGameSessionId());
        assertEquals(problemId, savedSubmission.getProblemId());
        assertEquals(playerSessionId, savedSubmission.getPlayerSessionId());
        assertEquals(SubmissionType.RUN, savedSubmission.getType());
        assertEquals(0, savedSubmission.getPassedTestCases());
        assertEquals(2, savedSubmission.getTotalTestCases());
        assertEquals(SubmissionStatus.PENDING, savedSubmission.getStatus());
        assertNotNull(savedSubmission.getJudgeTokensJson());
    }

    @Test
    void submitCode_alreadySubmitted_throwsConflict() {
        Long gameSessionId = 1L;
        Long problemId = 2L;
        Long playerSessionId = 3L;

        CodeExecutionPostDTO request = new CodeExecutionPostDTO();
        request.setPlayerSessionId(playerSessionId);
        request.setSourceCode("print('hello')");

        when(submissionRepository.existsByGameSessionIdAndProblemIdAndPlayerSessionIdAndType(
                gameSessionId, problemId, playerSessionId, SubmissionType.SUBMIT
        )).thenReturn(true);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> codeExecutionService.submitCode(gameSessionId, problemId, request)
        );

        assertEquals(409, exception.getStatusCode().value());
        verify(problemService, never()).getProblemById(any());
        verify(judgeService, never()).submitBatch(any());
        verify(submissionRepository, never()).save(any());
    }

    @Test
    void runCode_blankSourceCode_throwsBadRequest() {
        CodeExecutionPostDTO request = new CodeExecutionPostDTO();
        request.setPlayerSessionId(3L);
        request.setSourceCode("   ");

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> codeExecutionService.runCode(1L, 2L, request)
        );

        assertEquals(400, exception.getStatusCode().value());
        verify(problemService, never()).getProblemById(any());
        verify(judgeService, never()).submitBatch(any());
        verify(submissionRepository, never()).save(any());
    }

    @Test
    void runCode_problemWithoutTestCases_throwsBadRequest() {
        CodeExecutionPostDTO request = new CodeExecutionPostDTO();
        request.setPlayerSessionId(3L);
        request.setSourceCode("def solution(input_data):\n    return input_data");

        Problem problem = new Problem();
        problem.setProblemId(2L);
        problem.setGameLanguage(GameLanguage.PYTHON);
        problem.setTestCases(List.of());

        when(problemService.getProblemById(2L)).thenReturn(problem);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> codeExecutionService.runCode(1L, 2L, request)
        );

        assertEquals(400, exception.getStatusCode().value());
        verify(judgeService, never()).submitBatch(any());
        verify(submissionRepository, never()).save(any());
    }
}