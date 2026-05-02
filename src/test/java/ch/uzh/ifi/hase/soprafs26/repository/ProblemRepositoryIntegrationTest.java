package ch.uzh.ifi.hase.soprafs26.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import ch.uzh.ifi.hase.soprafs26.constant.GameDifficulty;
import ch.uzh.ifi.hase.soprafs26.constant.GameLanguage;
import ch.uzh.ifi.hase.soprafs26.entity.Problem;
import ch.uzh.ifi.hase.soprafs26.entity.TestCase;

@DataJpaTest
public class ProblemRepositoryIntegrationTest {
    
    @Autowired
    private ProblemRepository problemRepository;

    @Autowired
    private TestCaseRepository testCaseRepository;

    private Problem p1, p2, p3, p4, p5;

    @BeforeEach
    void setup() {
        testCaseRepository.deleteAll();
        problemRepository.deleteAll();
        
        p1 = new Problem();
        p1.setGameDifficulty(GameDifficulty.HARD);
        p1.setGameLanguage(GameLanguage.PYTHON);

        p2 = new Problem();
        p2.setGameDifficulty(GameDifficulty.HARD);
        p2.setGameLanguage(GameLanguage.PYTHON);

        p3 = new Problem();
        p3.setGameDifficulty(GameDifficulty.EASY);
        p3.setGameLanguage(GameLanguage.PYTHON);

        p4 = new Problem();
        p4.setGameDifficulty(GameDifficulty.EASY);
        p4.setGameLanguage(GameLanguage.PYTHON);

        p5 = new Problem();
        p5.setGameDifficulty(GameDifficulty.HARD);
        p5.setGameLanguage(GameLanguage.JAVA);

        List<Problem> testProblems = new ArrayList<>(List.of(p1, p2, p3, p4, p5));

        for (Problem p : testProblems) {
            p.setTitle("testProblem");
            p.setDescription("testProblem");
            p.setInputFormat("testProblem");
            p.setOutputFormat("testProblem");
            p.setConstraints("testProblem");
            p.setSampleSolution("testProblem");

            problemRepository.save(p);

            TestCase testTestCase = new TestCase();
            testTestCase.setInput("test");
            testTestCase.setExpectedOutput("test");
            testTestCase.setProblem(p);
            testCaseRepository.save(testTestCase);

            p.setTestCases(new ArrayList<>(List.of(testTestCase)));
            problemRepository.save(p);
        }
    }

    @Test
	void findAllByGameLanguageAndGameDifficulty_Success1() {
        List<Problem> foundProblems = problemRepository.findAllByGameLanguageAndGameDifficulty(GameLanguage.PYTHON, GameDifficulty.HARD);

        assertEquals(2, foundProblems.size());
        assertTrue(foundProblems.contains(p1));
        assertTrue(foundProblems.contains(p2));
    }
    
    @Test
	void findAllByGameLanguageAndGameDifficulty_Success2() {
        List<Problem> foundProblems = problemRepository.findAllByGameLanguageAndGameDifficulty(GameLanguage.PYTHON, GameDifficulty.EASY);

        assertEquals(2, foundProblems.size());
        assertTrue(foundProblems.contains(p3));
        assertTrue(foundProblems.contains(p4));
    }

    @Test
	void findAllByGameLanguageAndGameDifficulty_Success3() {
        List<Problem> foundProblems = problemRepository.findAllByGameLanguageAndGameDifficulty(GameLanguage.JAVA, GameDifficulty.EASY);

        assertEquals(0, foundProblems.size());
    }


    @Test
	void findAllByGameLanguageAndGameDifficulty_Success4() {
        List<Problem> foundProblems = problemRepository.findAllByGameLanguageAndGameDifficulty(GameLanguage.JAVA, GameDifficulty.HARD);

        assertEquals(1, foundProblems.size());
        assertTrue(foundProblems.contains(p5));
    }
}
