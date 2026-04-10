package ch.uzh.ifi.hase.soprafs26.controller;

import ch.uzh.ifi.hase.soprafs26.constant.GameDifficulty;
import ch.uzh.ifi.hase.soprafs26.constant.GameLanguage;
import ch.uzh.ifi.hase.soprafs26.entity.Problem;
import ch.uzh.ifi.hase.soprafs26.entity.TestCase;
import ch.uzh.ifi.hase.soprafs26.service.ProblemService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;


/*  !!!!!!!!!!!!!!!!!!!!!
 THIS IS A TEMPORARY ENDPOINT TO LOAD IN PROBLEMS WITH TESTCASES TO TEST FUNCTIONALITY REMOVE LATER
    !!!!!!!!!!!!!!!!!!!!!
*/  

@RestController
public class DebugProblemController {

    private final ProblemService problemService;

    public DebugProblemController(ProblemService problemService) {
        this.problemService = problemService;
    }

    @PostMapping("/debug/load-test-problem")
    public String loadTestProblem() {
        Problem problem = new Problem();
        problem.setTitle("Reverse String");
        problem.setDescription("Return the reversed input string.");
        problem.setInputFormat("One string");
        problem.setOutputFormat("Reversed string");
        problem.setConstraints("Input length >= 0");
        problem.setGameDifficulty(GameDifficulty.EASY);
        problem.setGameLanguage(GameLanguage.PYTHON);
        problem.setSampleSolution("def solution(input_data):\n    return input_data[::-1]");

        List<TestCase> testCases = new ArrayList<>();

        TestCase tc1 = new TestCase();
        tc1.setInput("hello");
        tc1.setExpectedOutput("olleh");
        tc1.setProblem(problem);
        testCases.add(tc1);

        TestCase tc2 = new TestCase();
        tc2.setInput("abc");
        tc2.setExpectedOutput("cba");
        tc2.setProblem(problem);
        testCases.add(tc2);

        TestCase tc3 = new TestCase();
        tc3.setInput("racecar");
        tc3.setExpectedOutput("racecar");
        tc3.setProblem(problem);
        testCases.add(tc3);

        problem.setTestCases(testCases);

        Problem createdProblem = problemService.createProblem(problem);

        return "Loaded test problem with id: " + createdProblem.getProblemId();
    }
}