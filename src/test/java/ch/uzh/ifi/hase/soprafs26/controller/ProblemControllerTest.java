package ch.uzh.ifi.hase.soprafs26.controller;

import ch.uzh.ifi.hase.soprafs26.entity.Problem;
import ch.uzh.ifi.hase.soprafs26.entity.TestCase;
import ch.uzh.ifi.hase.soprafs26.rest.dto.ProblemPostDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.TestCasePostDTO;
import ch.uzh.ifi.hase.soprafs26.service.ProblemService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.web.server.ResponseStatusException;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProblemController.class)
class ProblemControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    private ProblemService problemService;

    @Test
    void createProblem_validInput_success() throws Exception {
        Problem problem = createProblemEntity();
        problem.setProblemId(1L);

        ProblemPostDTO problemPostDTO = createProblemPostDTO();

        Mockito.when(problemService.createProblem(Mockito.any(Problem.class))).thenReturn(problem);

        MockHttpServletRequestBuilder postRequest = post("/problems")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(problemPostDTO));

        mockMvc.perform(postRequest)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.problemId", is(1)))
                .andExpect(jsonPath("$.title", is("mergeSort")))
                .andExpect(jsonPath("$.description", is("Sort an array using merge sort.")))
                .andExpect(jsonPath("$.inputFormat", is("An array of integers.")))
                .andExpect(jsonPath("$.outputFormat", is("The sorted array.")))
                .andExpect(jsonPath("$.constraints", is("1 <= n <= 100000")))
                .andExpect(jsonPath("$.testCases", hasSize(2)))
                .andExpect(jsonPath("$.testCases[0].testCaseId", is(10)))
                .andExpect(jsonPath("$.testCases[0].input", is("5\n3 1 4 2 5")))
                .andExpect(jsonPath("$.testCases[0].expectedOutput", is("1 2 3 4 5")));
    }

    @Test
    void createProblem_invalidInput_badRequest() throws Exception {
        ProblemPostDTO invalidProblemPostDTO = new ProblemPostDTO();
        invalidProblemPostDTO.setTitle("");
        invalidProblemPostDTO.setDescription("Sort an array using merge sort.");
        invalidProblemPostDTO.setInputFormat("An array of integers.");
        invalidProblemPostDTO.setOutputFormat("The sorted array.");
        invalidProblemPostDTO.setConstraints("1 <= n <= 100000");
        invalidProblemPostDTO.setTestCases(createTestCasePostDTOs());

        Mockito.doThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST, "A problem must have a title."))
                .when(problemService).createProblem(Mockito.any(Problem.class));

        MockHttpServletRequestBuilder postRequest = post("/problems")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(invalidProblemPostDTO));

        mockMvc.perform(postRequest)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail", is("A problem must have a title.")));
    }

    @Test
    void getAllProblems_success() throws Exception {
        Problem firstProblem = createProblemEntity();
        firstProblem.setProblemId(1L);

        Problem secondProblem = createSecondProblemEntity();
        secondProblem.setProblemId(2L);

        Mockito.when(problemService.getAllProblems()).thenReturn(List.of(firstProblem, secondProblem));

        mockMvc.perform(get("/problems"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].problemId", is(1)))
                .andExpect(jsonPath("$[0].title", is("mergeSort")))
                .andExpect(jsonPath("$[1].problemId", is(2)))
                .andExpect(jsonPath("$[1].title", is("twoSum")));
    }

    @Test
    void getProblemById_success() throws Exception {
        Problem problem = createProblemEntity();
        problem.setProblemId(1L);

        Mockito.when(problemService.getProblemById(1L)).thenReturn(problem);

        mockMvc.perform(get("/problems/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.problemId", is(1)))
                .andExpect(jsonPath("$.title", is("mergeSort")))
                .andExpect(jsonPath("$.testCases", hasSize(2)));
    }

    @Test
    void getProblemById_notFound() throws Exception {
        Mockito.when(problemService.getProblemById(999L))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Problem not found."));

        mockMvc.perform(get("/problems/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail", is("Problem not found.")));
    }

    private ProblemPostDTO createProblemPostDTO() {
        ProblemPostDTO dto = new ProblemPostDTO();
        dto.setTitle("mergeSort");
        dto.setDescription("Sort an array using merge sort.");
        dto.setInputFormat("An array of integers.");
        dto.setOutputFormat("The sorted array.");
        dto.setConstraints("1 <= n <= 100000");
        dto.setTestCases(createTestCasePostDTOs());
        return dto;
    }

    private List<TestCasePostDTO> createTestCasePostDTOs() {
        TestCasePostDTO tc1 = new TestCasePostDTO();
        tc1.setInput("5\n3 1 4 2 5");
        tc1.setExpectedOutput("1 2 3 4 5");

        TestCasePostDTO tc2 = new TestCasePostDTO();
        tc2.setInput("3\n9 7 8");
        tc2.setExpectedOutput("7 8 9");

        List<TestCasePostDTO> testCases = new ArrayList<>();
        testCases.add(tc1);
        testCases.add(tc2);
        return testCases;
    }

    private Problem createProblemEntity() {
        Problem problem = new Problem();
        problem.setTitle("mergeSort");
        problem.setDescription("Sort an array using merge sort.");
        problem.setInputFormat("An array of integers.");
        problem.setOutputFormat("The sorted array.");
        problem.setConstraints("1 <= n <= 100000");

        TestCase tc1 = new TestCase();
        tc1.setTestCaseId(10L);
        tc1.setInput("5\n3 1 4 2 5");
        tc1.setExpectedOutput("1 2 3 4 5");
        tc1.setProblem(problem);

        TestCase tc2 = new TestCase();
        tc2.setTestCaseId(11L);
        tc2.setInput("3\n9 7 8");
        tc2.setExpectedOutput("7 8 9");
        tc2.setProblem(problem);

        List<TestCase> testCases = new ArrayList<>();
        testCases.add(tc1);
        testCases.add(tc2);

        problem.setTestCases(testCases);
        return problem;
    }

    private Problem createSecondProblemEntity() {
        Problem problem = new Problem();
        problem.setTitle("twoSum");
        problem.setDescription("Find two indices whose values sum to target.");
        problem.setInputFormat("Array and target.");
        problem.setOutputFormat("Two indices.");
        problem.setConstraints("Exactly one valid answer exists.");

        TestCase tc = new TestCase();
        tc.setTestCaseId(20L);
        tc.setInput("4\n2 7 11 15\n9");
        tc.setExpectedOutput("0 1");
        tc.setProblem(problem);

        problem.setTestCases(List.of(tc));
        return problem;
    }

    private String asJsonString(final Object object) {
        try {
            return new ObjectMapper().writeValueAsString(object);
        } catch (JacksonException e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    String.format("The request body could not be created.%s", e)
            );
        }
    }
}