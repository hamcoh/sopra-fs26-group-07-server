package ch.uzh.ifi.hase.soprafs26.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;

import org.springframework.http.HttpStatus;


import ch.uzh.ifi.hase.soprafs26.constant.GameLanguage;
import ch.uzh.ifi.hase.soprafs26.constant.SubmissionStatus;
import ch.uzh.ifi.hase.soprafs26.constant.SubmissionType;
import ch.uzh.ifi.hase.soprafs26.entity.Problem;
import ch.uzh.ifi.hase.soprafs26.entity.Submission;
import ch.uzh.ifi.hase.soprafs26.entity.TestCase;
import ch.uzh.ifi.hase.soprafs26.repository.SubmissionRepository;
import ch.uzh.ifi.hase.soprafs26.rest.dto.CodeExecutionPostDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.CodeRunDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.CodeSubmissionDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.JudgeBatchRequestDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.JudgeRequestDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.JudgeTokenDTO;
import com.fasterxml.jackson.databind.ObjectMapper;


@Service
@Transactional
public class CodeExecutionService {
    
    private final ProblemService problemService;
    private final JudgeService judgeService;
    private final SubmissionRepository submissionRepository;

    public CodeExecutionService(
            ProblemService problemService,
            JudgeService judgeService,
            SubmissionRepository submissionRepository
    ) {
        this.problemService = problemService;
        this.judgeService = judgeService;
        this.submissionRepository = submissionRepository;
    }   

    private final ObjectMapper objectMapper = new ObjectMapper();

    public CodeRunDTO runCode(Long gameSessionId,
                            Long problemId,
                           CodeExecutionPostDTO requestBody) {
        
        validateRequest(gameSessionId, problemId, requestBody);

        Problem problem = problemService.getProblemById(problemId);

        List<JudgeTokenDTO> tokens = sendCodeToJudge(problem, requestBody.getSourceCode());

        Submission submission = createSubmission(gameSessionId, 
                                                problemId, 
                                                requestBody.getPlayerSessionId(),
                                                requestBody.getSourceCode(),
                                                SubmissionType.RUN,
                                                problem.getTestCases().size(),
                                                tokens);

       submissionRepository.save(submission);
       submissionRepository.flush();
       
       CodeRunDTO response = new CodeRunDTO();
       response.setGameSessionId(gameSessionId);
       response.setProblemId(problemId);
       response.setPlayerSessionId(requestBody.getPlayerSessionId());
       response.setTokens(tokens.stream().map(JudgeTokenDTO::getJudgeToken).toList());

        
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

        CodeSubmissionDTO response = new CodeSubmissionDTO();
        response.setGameSessionId(gameSessionId);
        response.setProblemId(problemId);
        response.setPlayerSessionId(requestBody.getPlayerSessionId());
        response.setTokens(tokens.stream().map(JudgeTokenDTO::getJudgeToken).toList());

        return response;
    }

    public void validateRequest(Long gameSessionId, Long problemId, CodeExecutionPostDTO requestBody) {

        if (gameSessionId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Game session ID is required");
        }

        if (problemId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Problem ID is required");
        }

        if (requestBody.getPlayerSessionId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Player session ID is required");
        }

        if (requestBody.getSourceCode() == null || requestBody.getSourceCode().isBlank()) { // isBlank checks for emptiness OR only whitespaces which fits better than isEmpty()
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
        submission.setExecutionResult(null);

        try {
            submission.setJudgeTokensJson(objectMapper.writeValueAsString(judgeTokens));
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to serialize judge tokens");
        }

        return submission;

    }

    private List<JudgeTokenDTO> sendCodeToJudge(Problem problem, String userSourceCode) {
        List<TestCase> testCases = problem.getTestCases();
        if (testCases == null || testCases.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "This problem has no test cases");
        }

        Integer languageId = mapLanguagetoJudgeCode(problem.getGameLanguage());
        String wrappedSourceCode = wrapUserCode(userSourceCode, problem.getGameLanguage());

        List<JudgeRequestDTO> submissions = new ArrayList<>();

        for (TestCase testCase : testCases) {
            JudgeRequestDTO request = new JudgeRequestDTO();
            request.setSource_code(wrappedSourceCode);
            request.setLanguage_id(languageId);
            request.setStdin(testCase.getInput());
            request.setExpected_output(normalizeOutpuString(testCase.getExpectedOutput()));
            submissions.add(request);
        }

        JudgeBatchRequestDTO batchRequest = new JudgeBatchRequestDTO();
        batchRequest.setSubmissions(submissions);

        try {
            objectMapper.writeValueAsString(batchRequest); // might remove later because we serialize it again manually later
        }
        catch (Exception e) {
    throw new ResponseStatusException(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "Failed to serialize batch request."
            );
        }
        return judgeService.submitBatch(batchRequest);
    }

    private Integer mapLanguagetoJudgeCode(GameLanguage gameLanguage) {
        if (gameLanguage == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Game language is required");
        }

        switch(gameLanguage) {
            case PYTHON:
                return 71; // Judge0 Code for Python3
            case JAVA:
                return 62; // Judge0 Code for Java
            default:
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, gameLanguage + " is not a supported game language");
        }
    }

    private String wrapUserCode(String userCode, GameLanguage gameLanguage) {
        switch(gameLanguage) {
            case PYTHON:
                return wrapPythonCode(userCode);
            case JAVA:
                throw new ResponseStatusException(HttpStatus.NOT_IMPLEMENTED, "Java is currently not supported for code execution");
            default:
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, gameLanguage + " wrapping is not supported");
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

    private String normalizeOutpuString(String expectedOutput) { // this is just to make sure expectedOutput is in the right  format
        if (expectedOutput == null) {
            return "";
        }
        return expectedOutput.endsWith("\n") ? expectedOutput : expectedOutput + "\n";
    }

}