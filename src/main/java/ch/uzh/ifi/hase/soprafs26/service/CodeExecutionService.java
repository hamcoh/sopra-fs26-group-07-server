package ch.uzh.ifi.hase.soprafs26.service;

import ch.uzh.ifi.hase.soprafs26.constant.GameLanguage;
import ch.uzh.ifi.hase.soprafs26.constant.SubmissionStatus;
import ch.uzh.ifi.hase.soprafs26.constant.SubmissionType;
import ch.uzh.ifi.hase.soprafs26.constant.Verdict;
import ch.uzh.ifi.hase.soprafs26.entity.Problem;
import ch.uzh.ifi.hase.soprafs26.entity.Submission;
import ch.uzh.ifi.hase.soprafs26.entity.TestCase;
import ch.uzh.ifi.hase.soprafs26.repository.SubmissionRepository;
import ch.uzh.ifi.hase.soprafs26.rest.dto.CodeExecutionPostDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.CodeRunDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.CodeSubmissionDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.JudgeBatchRequestDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.JudgeBatchResultDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.JudgeRequestDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.JudgeTokenDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class CodeExecutionService {

    private final ProblemService problemService;
    private final JudgeService judgeService;
    private final SubmissionRepository submissionRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final int MAX_RESULT_CHECKS = 3;
    private static final long RESULT_CHECK_DELAY_MS = 1000;

    public CodeExecutionService(
            ProblemService problemService,
            JudgeService judgeService,
            SubmissionRepository submissionRepository
    ) {
        this.problemService = problemService;
        this.judgeService = judgeService;
        this.submissionRepository = submissionRepository;
    }

    public CodeRunDTO runCode(Long gameSessionId,
                              Long problemId,
                              CodeExecutionPostDTO requestBody) {

        validateRequest(gameSessionId, problemId, requestBody);

        Problem problem = problemService.getProblemById(problemId);
        List<JudgeTokenDTO> tokens = sendCodeToJudge(problem, requestBody.getSourceCode());

        Submission submission = createSubmission(
                gameSessionId,
                problemId,
                requestBody.getPlayerSessionId(),
                requestBody.getSourceCode(),
                SubmissionType.RUN,
                problem.getTestCases().size(),
                tokens
        );

        submissionRepository.save(submission);
        submissionRepository.flush();

        List<String> judgeTokens = tokens.stream()
                .map(JudgeTokenDTO::getJudgeToken)
                .toList();

        JudgeBatchResultDTO batchResult = pollJudgeResults(judgeTokens);

        if (areAllJudgeResultsFinal(batchResult)) {
            submission.setStatus(SubmissionStatus.FINISHED);
            submission.setVerdict(aggregateVerdict(batchResult));
        } else {
            submission.setStatus(SubmissionStatus.RUNNING);
            submission.setVerdict(Verdict.PENDING);
        }

        submissionRepository.save(submission);
        submissionRepository.flush();

        CodeRunDTO response = new CodeRunDTO();
        response.setGameSessionId(gameSessionId);
        response.setProblemId(problemId);
        response.setPlayerSessionId(requestBody.getPlayerSessionId());
        response.setSubmissionStatus(submission.getStatus());
        response.setVerdict(submission.getVerdict());

        return response;
    }

    public CodeSubmissionDTO submitCode(Long gameSessionId,
                                        Long problemId,
                                        CodeExecutionPostDTO requestBody) {

        validateRequest(gameSessionId, problemId, requestBody);

        boolean alreadySubmitted = submissionRepository
                .existsByGameSessionIdAndProblemIdAndPlayerSessionIdAndType(
                        gameSessionId,
                        problemId,
                        requestBody.getPlayerSessionId(),
                        SubmissionType.SUBMIT
                );

        if (alreadySubmitted) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Player has already submitted his solution."
            );
        }

        Problem problem = problemService.getProblemById(problemId);
        List<JudgeTokenDTO> tokens = sendCodeToJudge(problem, requestBody.getSourceCode());

        Submission submission = createSubmission(
                gameSessionId,
                problemId,
                requestBody.getPlayerSessionId(),
                requestBody.getSourceCode(),
                SubmissionType.SUBMIT,
                problem.getTestCases().size(),
                tokens
        );

        submissionRepository.save(submission);
        submissionRepository.flush();

        List<String> judgeTokens = tokens.stream()
                .map(JudgeTokenDTO::getJudgeToken)
                .toList();

        JudgeBatchResultDTO batchResult = pollJudgeResults(judgeTokens);

        if (areAllJudgeResultsFinal(batchResult)) {
            submission.setStatus(SubmissionStatus.FINISHED);
            submission.setVerdict(aggregateVerdict(batchResult));
        } else {
            submission.setStatus(SubmissionStatus.RUNNING);
            submission.setVerdict(Verdict.PENDING);
        }

        submissionRepository.save(submission);
        submissionRepository.flush();

        CodeSubmissionDTO response = new CodeSubmissionDTO();
        response.setGameSessionId(gameSessionId);
        response.setProblemId(problemId);
        response.setPlayerSessionId(requestBody.getPlayerSessionId());
        response.setSubmissionStatus(submission.getStatus());
        response.setVerdict(submission.getVerdict());

        return response;
    }

    private void validateRequest(Long gameSessionId,
                                 Long problemId,
                                 CodeExecutionPostDTO requestBody) {

        if (gameSessionId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Game session ID is required");
        }

        if (problemId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Problem ID is required");
        }

        if (requestBody == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Request body is required");
        }

        if (requestBody.getPlayerSessionId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Player session ID is required");
        }

        if (requestBody.getSourceCode() == null || requestBody.getSourceCode().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Source code is required");
        }
    }

    private Submission createSubmission(long gameSessionId,
                                        long problemId,
                                        long playerSessionId,
                                        String sourceCode,
                                        SubmissionType submissionType,
                                        int totalTestCases,
                                        List<JudgeTokenDTO> judgeTokens) {

        Submission submission = new Submission();
        submission.setGameSessionId(gameSessionId);
        submission.setProblemId(problemId);
        submission.setPlayerSessionId(playerSessionId);
        submission.setSourceCode(sourceCode);
        submission.setType(submissionType);
        submission.setTotalTestCases(totalTestCases);
        submission.setStatus(SubmissionStatus.PENDING);
        submission.setVerdict(Verdict.PENDING);
        submission.setExecutionResult(null);

        try {
            submission.setJudgeTokensJson(objectMapper.writeValueAsString(judgeTokens));
        } catch (Exception e) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Failed to serialize Judge0 tokens"
            );
        }

        return submission;
    }

    private List<JudgeTokenDTO> sendCodeToJudge(Problem problem, String userSourceCode) {
        List<TestCase> testCases = problem.getTestCases();
        if (testCases == null || testCases.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "This problem has no test cases");
        }

        Integer languageId = mapLanguageToJudgeCode(problem.getGameLanguage());
        String wrappedSourceCode = wrapUserCode(userSourceCode, problem.getGameLanguage());

        List<JudgeRequestDTO> submissions = new ArrayList<>();

        for (TestCase testCase : testCases) {
            JudgeRequestDTO request = new JudgeRequestDTO();
            request.setSource_code(wrappedSourceCode);
            request.setLanguage_id(languageId);
            request.setStdin(testCase.getInput());
            request.setExpected_output(normalizeOutputString(testCase.getExpectedOutput()));
            submissions.add(request);
        }

        JudgeBatchRequestDTO batchRequest = new JudgeBatchRequestDTO();
        batchRequest.setSubmissions(submissions);

        return judgeService.submitBatch(batchRequest);
    }

    private Integer mapLanguageToJudgeCode(GameLanguage gameLanguage) {
        if (gameLanguage == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Game language is required");
        }

        switch (gameLanguage) {
            case PYTHON:
                return 71;
            case JAVA:
                return 62;
            default:
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        gameLanguage + " is not a supported game language"
                );
        }
    }

    private String wrapUserCode(String userCode, GameLanguage gameLanguage) {
        switch (gameLanguage) {
            case PYTHON:
                return wrapPythonCode(userCode);
            case JAVA:
                throw new ResponseStatusException(
                        HttpStatus.NOT_IMPLEMENTED,
                        "Java is currently not supported for code execution"
                );
            default:
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        gameLanguage + " wrapping is not supported"
                );
        }
    }

    private String wrapPythonCode(String userCode) {
        return userCode + "\n\n"
                + "if __name__ == '__main__':\n"
                + "    import sys\n"
                + "    input_data = sys.stdin.read().strip()\n"
                + "    result = solution(input_data)\n"
                + "    print(result)\n";
    }

    private String normalizeOutputString(String expectedOutput) {
        if (expectedOutput == null) {
            return "";
        }
        return expectedOutput.endsWith("\n") ? expectedOutput : expectedOutput + "\n";
    }

    private boolean isFinalJudgeStatus(Integer statusId) {
        if (statusId == null) {
            return false;
        }
        return statusId != 1 && statusId != 2;
    }

    private boolean areAllJudgeResultsFinal(JudgeBatchResultDTO batchResult) {
        if (batchResult == null || batchResult.getSubmissions() == null || batchResult.getSubmissions().isEmpty()) {
            return false;
        }

        return batchResult.getSubmissions().stream()
                .allMatch(result -> result.getStatus() != null
                        && isFinalJudgeStatus(result.getStatus().getId()));
    }

    private JudgeBatchResultDTO pollJudgeResults(List<String> tokens) {
        JudgeBatchResultDTO latestResult = null;

        for (int attempt = 1; attempt <= MAX_RESULT_CHECKS; attempt++) {
            latestResult = judgeService.getBatchSubmissionResults(tokens);

            if (areAllJudgeResultsFinal(latestResult)) {
                return latestResult;
            }

            if (attempt < MAX_RESULT_CHECKS) {
                try {
                    Thread.sleep(RESULT_CHECK_DELAY_MS);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new IllegalStateException("Judge polling was interrupted", e);
                }
            }
        }

        return latestResult;
    }

    private Verdict mapJudgeStatusToVerdict(Integer statusId) {
        if (statusId == null) {
            return Verdict.INTERNAL_ERROR;
        }

        if (statusId == 1 || statusId == 2) {
            return Verdict.PENDING;
        } else if (statusId == 3) {
            return Verdict.CORRECT_ANSWER;
        } else if (statusId == 4) {
            return Verdict.WRONG_ANSWER;
        } else if (statusId == 5) {
            return Verdict.TIME_LIMIT_EXCEEDED;
        } else if (statusId == 6) {
            return Verdict.COMPILE_ERROR;
        } else {
            return Verdict.INTERNAL_ERROR;
        }
    }

    private Verdict aggregateVerdict(JudgeBatchResultDTO batchResult) {
        if (batchResult == null || batchResult.getSubmissions() == null || batchResult.getSubmissions().isEmpty()) {
            return Verdict.INTERNAL_ERROR;
        }

        List<Verdict> verdicts = batchResult.getSubmissions().stream()
                .map(result -> {
                    if (result.getStatus() == null) {
                        return Verdict.INTERNAL_ERROR;
                    }
                    return mapJudgeStatusToVerdict(result.getStatus().getId());
                })
                .toList();

        if (verdicts.contains(Verdict.COMPILE_ERROR)) {
            return Verdict.COMPILE_ERROR;
        }

        if (verdicts.contains(Verdict.TIME_LIMIT_EXCEEDED)) {
            return Verdict.TIME_LIMIT_EXCEEDED;
        }

        if (verdicts.contains(Verdict.INTERNAL_ERROR)) {
            return Verdict.INTERNAL_ERROR;
        }

        if (verdicts.contains(Verdict.WRONG_ANSWER)) {
            return Verdict.WRONG_ANSWER;
        }

        if (verdicts.stream().allMatch(verdict -> verdict == Verdict.CORRECT_ANSWER)) {
            return Verdict.CORRECT_ANSWER;
        }

        return Verdict.PENDING;
    }

    private List<String> extractJudgeTokens(Submission submission) {
        if (submission == null || submission.getJudgeTokensJson() == null || submission.getJudgeTokensJson().isBlank()) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Submission has no stored Judge0 tokens");
        }

        try {
            List<JudgeTokenDTO> judgeTokens = objectMapper.readValue(
                    submission.getJudgeTokensJson(),
                    objectMapper.getTypeFactory().constructCollectionType(List.class, JudgeTokenDTO.class)
            );

            return judgeTokens.stream()
                    .map(JudgeTokenDTO::getJudgeToken)
                    .toList();
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to deserialize stored Judge0 tokens");
        }
    }

    private Submission refreshSubmissionIfNeeded(Submission submission) {
        if (submission == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Submission not found");
        }

        if (submission.getStatus() == SubmissionStatus.FINISHED || submission.getStatus() == SubmissionStatus.FAILED) {
            return submission;
        }

        List<String> judgeTokens = extractJudgeTokens(submission);
        JudgeBatchResultDTO batchResult = judgeService.getBatchSubmissionResults(judgeTokens);

        if (areAllJudgeResultsFinal(batchResult)) {
            submission.setStatus(SubmissionStatus.FINISHED);
            submission.setVerdict(aggregateVerdict(batchResult));
        } else {
            submission.setStatus(SubmissionStatus.RUNNING);
            submission.setVerdict(Verdict.PENDING);
        }

        submissionRepository.save(submission);
        submissionRepository.flush();

        return submission;
    }

    public CodeRunDTO getLatestRunResult(Long gameSessionId, Long problemId, Long playerSessionId) {
        if (gameSessionId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Game session ID is required");
        }
        if (problemId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Problem ID is required");
        }
        if (playerSessionId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Player session ID is required");
        }

        Submission submission = submissionRepository
                .findTopByGameSessionIdAndProblemIdAndPlayerSessionIdAndTypeOrderBySubmissionIdDesc(
                        gameSessionId,
                        problemId,
                        playerSessionId,
                        SubmissionType.RUN
                );
        if (submission == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No run submission found");
        }

        submission = refreshSubmissionIfNeeded(submission);

        CodeRunDTO response = new CodeRunDTO();
        response.setGameSessionId(gameSessionId);
        response.setProblemId(problemId);
        response.setPlayerSessionId(playerSessionId);
        response.setSubmissionStatus(submission.getStatus());
        response.setVerdict(submission.getVerdict());

        return response;
    }

    public CodeSubmissionDTO getLatestSubmissionResult(Long gameSessionId, Long problemId, Long playerSessionId) {
        if (gameSessionId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Game session ID is required");
        }
        if (problemId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Problem ID is required");
        }
        if (playerSessionId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Player session ID is required");
        }

        Submission submission = submissionRepository
                .findTopByGameSessionIdAndProblemIdAndPlayerSessionIdAndTypeOrderBySubmissionIdDesc(
                        gameSessionId,
                        problemId,
                        playerSessionId,
                        SubmissionType.SUBMIT
                );
        if (submission == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No final submission found");
        }

        submission = refreshSubmissionIfNeeded(submission);

        CodeSubmissionDTO response = new CodeSubmissionDTO();
        response.setGameSessionId(gameSessionId);
        response.setProblemId(problemId);
        response.setPlayerSessionId(playerSessionId);
        response.setSubmissionStatus(submission.getStatus());
        response.setVerdict(submission.getVerdict());

        return response;
    }
}