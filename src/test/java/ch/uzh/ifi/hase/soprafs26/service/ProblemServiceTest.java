package ch.uzh.ifi.hase.soprafs26.service;

import ch.uzh.ifi.hase.soprafs26.constant.GameDifficulty;
import ch.uzh.ifi.hase.soprafs26.constant.GameLanguage;
import ch.uzh.ifi.hase.soprafs26.entity.Problem;
import ch.uzh.ifi.hase.soprafs26.entity.TestCase;
import ch.uzh.ifi.hase.soprafs26.repository.ProblemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProblemServiceTest {

    @Mock
    private ProblemRepository problemRepository;

    @InjectMocks
    private ProblemService problemService;

    private Problem validProblem;

    @BeforeEach
    void setup() {
        validProblem = createValidProblem();
    }

    @Test
    void createProblem_validProblem_savesProblemAndTestCases() {
        when(problemRepository.save(any(Problem.class))).thenAnswer(invocation -> {
            Problem p = invocation.getArgument(0);
            p.setProblemId(1L);
            return p;
        });

        Problem createdProblem = problemService.createProblem(validProblem);

        assertNotNull(createdProblem);
        assertEquals(1L, createdProblem.getProblemId());
        assertEquals("mergeSort", createdProblem.getTitle());
        assertEquals(2, createdProblem.getTestCases().size());

        for (TestCase testCase : createdProblem.getTestCases()) {
            assertSame(createdProblem, testCase.getProblem());
        }

        ArgumentCaptor<Problem> captor = ArgumentCaptor.forClass(Problem.class);
        verify(problemRepository, times(1)).save(captor.capture());
        verify(problemRepository, times(1)).flush();

        Problem savedProblem = captor.getValue();
        assertEquals("mergeSort", savedProblem.getTitle());
        assertEquals(2, savedProblem.getTestCases().size());
        assertSame(savedProblem, savedProblem.getTestCases().get(0).getProblem());
        assertSame(savedProblem, savedProblem.getTestCases().get(1).getProblem());
    }

    @Test
    void createProblem_withoutTestCases_throwsBadRequest() {
        validProblem.setTestCases(new ArrayList<>());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> problemService.createProblem(validProblem)
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        verify(problemRepository, never()).save(any());
    }

    @Test
    void createProblem_withoutTitle_throwsBadRequest() {
        validProblem.setTitle("");

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> problemService.createProblem(validProblem)
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        verify(problemRepository, never()).save(any());
    }

    @Test
    void createProblem_withoutDescription_throwsBadRequest() {
        validProblem.setDescription("");

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> problemService.createProblem(validProblem)
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        verify(problemRepository, never()).save(any());
    }

    @Test
    void createProblem_withoutInputFormat_throwsBadRequest() {
        validProblem.setInputFormat("");

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> problemService.createProblem(validProblem)
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        verify(problemRepository, never()).save(any());
    }

    @Test
    void createProblem_withoutOutputFormat_throwsBadRequest() {
        validProblem.setOutputFormat("");

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> problemService.createProblem(validProblem)
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        verify(problemRepository, never()).save(any());
    }

    @Test
    void createProblem_withoutConstraints_throwsBadRequest() {
        validProblem.setConstraints("");

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> problemService.createProblem(validProblem)
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        verify(problemRepository, never()).save(any());
    }

    @Test
    void createProblem_withoutGameDifficulty_throwsBadRequest() {
        validProblem.setGameDifficulty(null);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> problemService.createProblem(validProblem)
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        verify(problemRepository, never()).save(any());
    }

    @Test
    void createProblem_withoutGameLanguage_throwsBadRequest() {
        validProblem.setGameLanguage(null);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> problemService.createProblem(validProblem)
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        verify(problemRepository, never()).save(any());
    }

    @Test
    void createProblem_withoutSampleSolution_throwsBadRequest() {
        validProblem.setSampleSolution("");

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> problemService.createProblem(validProblem)
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        verify(problemRepository, never()).save(any());
    }

    @Test
    void createProblem_testCaseWithoutInput_throwsBadRequest() {
        validProblem.getTestCases().get(0).setInput("");

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> problemService.createProblem(validProblem)
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        verify(problemRepository, never()).save(any());
    }

    @Test
    void createProblem_testCaseWithoutExpectedOutput_throwsBadRequest() {
        validProblem.getTestCases().get(0).setExpectedOutput("");

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> problemService.createProblem(validProblem)
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        verify(problemRepository, never()).save(any());
    }

    @Test
    void getAllProblems_returnsAllProblems() {
        List<Problem> problems = List.of(createValidProblem(), createAnotherProblem());
        when(problemRepository.findAll()).thenReturn(problems);

        List<Problem> result = problemService.getAllProblems();

        assertEquals(2, result.size());
        verify(problemRepository, times(1)).findAll();
    }

    @Test
    void getProblemById_existingId_returnsProblem() {
        Problem problem = createValidProblem();
        problem.setProblemId(1L);

        when(problemRepository.findById(1L)).thenReturn(Optional.of(problem));

        Problem result = problemService.getProblemById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getProblemId());
        assertEquals("mergeSort", result.getTitle());
    }

    @Test
    void getProblemById_nullId_throwsBadRequest() {
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> problemService.getProblemById(null)
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
    }

    @Test
    void getProblemById_missingId_throwsNotFound() {
        when(problemRepository.findById(999L)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> problemService.getProblemById(999L)
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    }

    private Problem createValidProblem() {
        Problem problem = new Problem();
        problem.setTitle("mergeSort");
        problem.setDescription("Sort an array using merge sort.");
        problem.setInputFormat("An array of integers.");
        problem.setOutputFormat("The sorted array.");
        problem.setConstraints("1 <= n <= 100000");
        problem.setGameDifficulty(GameDifficulty.EASY);
        problem.setGameLanguage(GameLanguage.PYTHON);
        problem.setSampleSolution("def solve(x):\n    return x");

        TestCase tc1 = new TestCase();
        tc1.setInput("5\n3 1 4 2 5");
        tc1.setExpectedOutput("1 2 3 4 5");

        TestCase tc2 = new TestCase();
        tc2.setInput("3\n9 7 8");
        tc2.setExpectedOutput("7 8 9");

        List<TestCase> testCases = new ArrayList<>();
        testCases.add(tc1);
        testCases.add(tc2);

        problem.setTestCases(testCases);
        return problem;
    }

    private Problem createAnotherProblem() {
        Problem problem = new Problem();
        problem.setTitle("twoSum");
        problem.setDescription("Find two indices whose values sum to target.");
        problem.setInputFormat("Array and target.");
        problem.setOutputFormat("Two indices.");
        problem.setConstraints("Exactly one valid answer exists.");

        TestCase tc = new TestCase();
        tc.setInput("4\n2 7 11 15\n9");
        tc.setExpectedOutput("0 1");

        problem.setTestCases(List.of(tc));
        return problem;
    }
}