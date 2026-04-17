package ch.uzh.ifi.hase.soprafs26.controller;

import ch.uzh.ifi.hase.soprafs26.rest.dto.CodeRunDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.CodeSubmissionDTO;
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
}