package ch.uzh.ifi.hase.soprafs26.controller;

import ch.uzh.ifi.hase.soprafs26.constant.SubmissionStatus;
import ch.uzh.ifi.hase.soprafs26.constant.Verdict;
import ch.uzh.ifi.hase.soprafs26.rest.dto.CodeRunDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.CodeSubmissionDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.GameRoundDTO;
import ch.uzh.ifi.hase.soprafs26.service.CodeExecutionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import java.util.Optional;

@WebMvcTest(CodeExecutionController.class)
class CodeExecutionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CodeExecutionService codeExecutionService;

    @Test
    void runCode_validRequest_returns200() throws Exception {
        CodeRunDTO response = new CodeRunDTO();
        response.setGameSessionId(1L);
        response.setProblemId(2L);
        response.setPlayerSessionId(3L);
        response.setTokens(List.of("tok1", "tok2"));

        when(codeExecutionService.runCode(eq(1L), eq(2L), any())).thenReturn(response);

        mockMvc.perform(post("/games/1/problems/2/runs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "playerSessionId": 3,
                                  "sourceCode": "def solution(input_data):\\n    return input_data[::-1]"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.gameSessionId").value(1))
                .andExpect(jsonPath("$.problemId").value(2))
                .andExpect(jsonPath("$.playerSessionId").value(3))
                .andExpect(jsonPath("$.tokens[0]").value("tok1"))
                .andExpect(jsonPath("$.tokens[1]").value("tok2"));
    }

    @Test
    void submitCode_validRequest_returns200() throws Exception {
        CodeSubmissionDTO response = new CodeSubmissionDTO();
        response.setGameSessionId(1L);
        response.setProblemId(2L);
        response.setPlayerSessionId(3L);
        response.setTokens(List.of("subTok1", "subTok2"));

        when(codeExecutionService.submitCode(eq(1L), eq(2L), any())).thenReturn(response);

        mockMvc.perform(post("/games/1/problems/2/submissions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "playerSessionId": 3,
                                  "sourceCode": "def solution(input_data):\\n    return input_data[::-1]"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.gameSessionId").value(1))
                .andExpect(jsonPath("$.problemId").value(2))
                .andExpect(jsonPath("$.playerSessionId").value(3))
                .andExpect(jsonPath("$.tokens[0]").value("subTok1"))
                .andExpect(jsonPath("$.tokens[1]").value("subTok2"));
    }

    @Test
    void getRunResult_validRequest_returns200() throws Exception {
        CodeRunDTO response = new CodeRunDTO();
        response.setGameSessionId(1L);
        response.setProblemId(2L);
        response.setPlayerSessionId(3L);
        response.setSubmissionStatus(SubmissionStatus.RUNNING);
        response.setVerdict(Verdict.PENDING);
        response.setPassedTestCases(0);
        response.setTotalTestCases(2);

        when(codeExecutionService.getLatestRunResult(1L, 2L, 3L)).thenReturn(response);

        mockMvc.perform(get("/games/1/problems/2/run-result")
                        .param("playerSessionId", "3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.gameSessionId").value(1))
                .andExpect(jsonPath("$.problemId").value(2))
                .andExpect(jsonPath("$.playerSessionId").value(3))
                .andExpect(jsonPath("$.submissionStatus").value("RUNNING"))
                .andExpect(jsonPath("$.verdict").value("PENDING"))
                .andExpect(jsonPath("$.passedTestCases").value(0))
                .andExpect(jsonPath("$.totalTestCases").value(2));
    }

    @Test
    void getSubmissionResult_validRequest_returns200() throws Exception {
        GameRoundDTO response = new GameRoundDTO();
        response.setGameSessionId(1L);
        response.setPlayerSessionId(3L);
        response.setPlayerId(7L);
        response.setCurrentScore(2);
        response.setNumOfSkippedProblems(0);
        response.setProblemId(4L);
        response.setTitle("Next Problem");
        response.setDescription("Solve the next problem");
        response.setInputFormat("string");
        response.setOutputFormat("string");
        response.setConstraints("1 <= n <= 100");

        when(codeExecutionService.getLatestSubmissionResult(1L, 2L, 3L))
                .thenReturn(Optional.of(response));

        mockMvc.perform(get("/games/1/problems/2/submission-result")
                        .param("playerSessionId", "3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.gameSessionId").value(1))
                .andExpect(jsonPath("$.playerSessionId").value(3))
                .andExpect(jsonPath("$.playerId").value(7))
                .andExpect(jsonPath("$.currentScore").value(2))
                .andExpect(jsonPath("$.numOfSkippedProblems").value(0))
                .andExpect(jsonPath("$.problemId").value(4))
                .andExpect(jsonPath("$.title").value("Next Problem"))
                .andExpect(jsonPath("$.description").value("Solve the next problem"))
                .andExpect(jsonPath("$.inputFormat").value("string"))
                .andExpect(jsonPath("$.outputFormat").value("string"))
                .andExpect(jsonPath("$.constraints").value("1 <= n <= 100"));
    }
}
