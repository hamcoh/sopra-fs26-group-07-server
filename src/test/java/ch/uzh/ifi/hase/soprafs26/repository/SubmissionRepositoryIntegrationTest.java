package ch.uzh.ifi.hase.soprafs26.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import ch.uzh.ifi.hase.soprafs26.constant.GameDifficulty;
import ch.uzh.ifi.hase.soprafs26.constant.GameLanguage;
import ch.uzh.ifi.hase.soprafs26.constant.SubmissionStatus;
import ch.uzh.ifi.hase.soprafs26.constant.SubmissionType;
import ch.uzh.ifi.hase.soprafs26.constant.Verdict;
import ch.uzh.ifi.hase.soprafs26.entity.Problem;
import ch.uzh.ifi.hase.soprafs26.entity.Submission;
import ch.uzh.ifi.hase.soprafs26.entity.TestCase;

@DataJpaTest
public class SubmissionRepositoryIntegrationTest {

    @Autowired
    private SubmissionRepository submissionRepository;

    @Autowired
    private ProblemRepository problemRepository;

    @Autowired
    private TestCaseRepository testCaseRepository;

    private Problem p1, p2, p3, p4;
    private Submission s1, s2, s3, s4, s5, s6, s7, s8, s9, s10, s11, s12, s13;

    @BeforeEach
    void setup() {
        testCaseRepository.deleteAll();
        problemRepository.deleteAll();
        submissionRepository.deleteAll();
        
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

        List<Problem> testProblems = new ArrayList<>(List.of(p1, p2, p3, p4));

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

        /* 
        ProblemId=1: worst-solved problem, solved 5 times
        ProblemId=2: third-best solved problem, solved 4 times
        ProblemId=3: second-best solved problem, solved 3 times
        ProblemId=4: best solved problem, solved 1 time
        */

        s1 = new Submission();
        s1.setPassedTestCases(10);
        s1.setTotalTestCases(20);
        s1.setProblemId(p1.getProblemId());
        s1.setPlayerSessionId(1L);

        s2 = new Submission();
        s2.setPassedTestCases(15);
        s2.setTotalTestCases(20);
        s2.setProblemId(p2.getProblemId());
        s2.setPlayerSessionId(1L);

        s3 = new Submission();
        s3.setPassedTestCases(18);
        s3.setTotalTestCases(20);
        s3.setProblemId(p3.getProblemId());
        s3.setPlayerSessionId(1L);

        s4 = new Submission();
        s4.setPassedTestCases(10);
        s4.setTotalTestCases(20);
        s4.setProblemId(p1.getProblemId());
        s4.setPlayerSessionId(2L);

        s5 = new Submission();
        s5.setPassedTestCases(12);
        s5.setTotalTestCases(20);
        s5.setProblemId(p2.getProblemId());
        s5.setPlayerSessionId(2L);

        s6 = new Submission();
        s6.setPassedTestCases(17);
        s6.setTotalTestCases(20);
        s6.setProblemId(p3.getProblemId());
        s6.setPlayerSessionId(3L);

        s7 = new Submission();
        s7.setPassedTestCases(15);
        s7.setTotalTestCases(20);
        s7.setProblemId(p2.getProblemId());
        s7.setPlayerSessionId(3L);

        s8 = new Submission();
        s8.setPassedTestCases(19);
        s8.setTotalTestCases(20);
        s8.setProblemId(p3.getProblemId());
        s8.setPlayerSessionId(3L);

        s9 = new Submission();
        s9.setPassedTestCases(19);
        s9.setTotalTestCases(20);
        s9.setProblemId(p4.getProblemId());
        s9.setPlayerSessionId(3L);

        s10 = new Submission();
        s10.setPassedTestCases(8);
        s10.setTotalTestCases(20);
        s10.setProblemId(p1.getProblemId());
        s10.setPlayerSessionId(3L);

        s11 = new Submission();
        s11.setPassedTestCases(8);
        s11.setTotalTestCases(20);
        s11.setProblemId(p1.getProblemId());
        s11.setPlayerSessionId(3L);

        s12 = new Submission();
        s12.setPassedTestCases(8);
        s12.setTotalTestCases(20);
        s12.setProblemId(p1.getProblemId());
        s12.setPlayerSessionId(3L);

        s13 = new Submission();
        s13.setPassedTestCases(15);
        s13.setTotalTestCases(20);
        s13.setProblemId(p2.getProblemId());
        s13.setPlayerSessionId(3L);


        List<Submission> testSubmissions = new ArrayList<>(List.of(s1, s2, s3, s4, s5, s6, s7, s8, s9, s10, s11, s12, s13));

        for (Submission s : testSubmissions) {
            s.setVerdict(Verdict.WRONG_ANSWER);
            s.setSourceCode("testCode");
            s.setType(SubmissionType.SUBMIT);
            s.setStatus(SubmissionStatus.FINISHED);
            s.setGameSessionId(1L);
            s.setJudgeTokensJson("judgeTokens");
            s.setPointsAwarded(true);
            s.setJudgeResultsJson("judgeResults");

            submissionRepository.save(s);

        }
    }


    @Test
	void findBySubmissionId_success() {

        Submission foundSubmission = submissionRepository.findBySubmissionId(s1.getSubmissionId());

        assertNotNull(foundSubmission.getSubmissionId());
        assertNotNull(foundSubmission.getSubmittedAt());
        assertEquals(SubmissionType.SUBMIT, foundSubmission.getType());
        assertEquals(SubmissionStatus.FINISHED, foundSubmission.getStatus());
        assertEquals(Verdict.WRONG_ANSWER, foundSubmission.getVerdict());
        assertEquals("testCode", foundSubmission.getSourceCode());
        assertEquals(1L, foundSubmission.getGameSessionId());
        assertEquals("judgeTokens", foundSubmission.getJudgeTokensJson());
        assertTrue(foundSubmission.isPointsAwarded());
        assertEquals("judgeResults", foundSubmission.getJudgeResultsJson());
        assertEquals(10, foundSubmission.getPassedTestCases());
        assertEquals(20, foundSubmission.getTotalTestCases());
        assertEquals(p1.getProblemId(), foundSubmission.getProblemId());
        assertEquals(1L, foundSubmission.getPlayerSessionId());
    }

    //regarding the object indeces: [0]=problemId, [1]=title, [2]=description, [3]=GameLanguage, [4]=passedTestCases, [5]=totalTestCases, [6]=submissionCount, [7]=successRate
    @Test
    void findTopHardestProblems_success() {

        List<Object[]> hardestProblems = submissionRepository.findTopHardestProblems();

        Object[] hardest = hardestProblems.get(0);
        Object[] secondHardest = hardestProblems.get(1);
        Object[] thirdHardest = hardestProblems.get(2);

        double hardestRate = ((Number) hardest[7]).doubleValue();
        double secondHardestRate = ((Number) secondHardest[7]).doubleValue();
        double thirdHardestRate = ((Number) thirdHardest[7]).doubleValue();

        //ProblemId=4 is excluded, since it has not been played at least 3 times
        assertEquals(3, hardestProblems.size());

        assertNotNull(hardest);
        assertEquals(p1.getProblemId(), ((Number) hardest[0]).longValue());
        assertEquals(p1.getTitle(), (String) hardest[1]);
        assertEquals(p1.getDescription(), (String) hardest[2]);
        assertEquals(p1.getGameLanguage(), GameLanguage.valueOf(hardest[3].toString()));
        assertEquals((s1.getPassedTestCases()+s4.getPassedTestCases()+s10.getPassedTestCases()+s11.getPassedTestCases()+s12.getPassedTestCases()), ((Number) hardest[4]).longValue());
        assertEquals((s1.getTotalTestCases()+s4.getTotalTestCases()+s10.getTotalTestCases()+s11.getTotalTestCases()+s12.getTotalTestCases()), ((Number) hardest[5]).longValue());
        assertEquals(5, ((Number) hardest[6]).longValue());
        assertTrue(((Number) hardest[7]).doubleValue() > 0);

        assertNotNull(secondHardest);
        assertEquals(p2.getProblemId(), ((Number) secondHardest[0]).longValue());
        assertEquals(p2.getTitle(), (String) secondHardest[1]);
        assertEquals(p2.getDescription(), (String) secondHardest[2]);
        assertEquals(p2.getGameLanguage(), GameLanguage.valueOf(secondHardest[3].toString()));
        assertEquals((s2.getPassedTestCases()+s5.getPassedTestCases()+s7.getPassedTestCases()+s13.getPassedTestCases()), ((Number) secondHardest[4]).longValue());
        assertEquals((s2.getTotalTestCases()+s5.getTotalTestCases()+s7.getTotalTestCases()+s13.getTotalTestCases()), ((Number) secondHardest[5]).longValue());
        assertEquals(4, ((Number) secondHardest[6]).longValue());
        assertTrue(((Number) secondHardest[7]).doubleValue() > 0);

        assertNotNull(thirdHardest);
        assertEquals(p3.getProblemId(), ((Number) thirdHardest[0]).longValue());
        assertEquals(p3.getProblemId(), ((Number) thirdHardest[0]).longValue());
        assertEquals(p3.getTitle(), (String) thirdHardest[1]);
        assertEquals(p3.getDescription(), (String) thirdHardest[2]);
        assertEquals(p3.getGameLanguage(), GameLanguage.valueOf(thirdHardest[3].toString()));
        assertEquals((s3.getPassedTestCases()+s6.getPassedTestCases()+s8.getPassedTestCases()), ((Number) thirdHardest[4]).longValue());
        assertEquals((s3.getTotalTestCases()+s6.getTotalTestCases()+s8.getTotalTestCases()), ((Number) thirdHardest[5]).longValue());
        assertEquals(3, ((Number) thirdHardest[6]).longValue());
        assertTrue(((Number) thirdHardest[7]).doubleValue() > 0);

        assertTrue(hardestRate < secondHardestRate);
        assertTrue(secondHardestRate < thirdHardestRate);

    }

    //object indeces: [0]=problemId, [1]=title, [2]=description, [3]=GameLanguage, [4]=passedTestCases, [5]=totalTestCases, [6]=submissionCount, [7]=successRate
    @Test
    void findMostPopularProblems_success() {

        List<Object[]> mostPopularProblems = submissionRepository.findMostPopularProblems();

        Object[] mostPlayed = mostPopularProblems.get(0);
        Object[] secondMostPlayed = mostPopularProblems.get(1);
        Object[] thirdMostPlayed = mostPopularProblems.get(2);
        Object[] leastPlayed = mostPopularProblems.get(3);

        long mostPlayedSubmissionCount = ((Number) mostPlayed[6]).longValue();
        long secondMostPlayedSubmissionCount = ((Number) secondMostPlayed[6]).longValue();
        long thirdMostPlayedSubmissionCount = ((Number) thirdMostPlayed[6]).longValue();
        long leastPlayedSubmissionCount = ((Number) leastPlayed[6]).longValue();

        assertEquals(4, mostPopularProblems.size());

        assertNotNull(mostPlayed);
        assertEquals(p1.getProblemId(), ((Number) mostPlayed[0]).longValue());
        assertEquals(p1.getTitle(), (String) mostPlayed[1]);
        assertEquals(p1.getDescription(), (String) mostPlayed[2]);
        assertEquals(p1.getGameLanguage(), GameLanguage.valueOf(mostPlayed[3].toString()));
        assertEquals((s1.getPassedTestCases()+s4.getPassedTestCases()+s10.getPassedTestCases()+s11.getPassedTestCases()+s12.getPassedTestCases()), ((Number) mostPlayed[4]).longValue());
        assertEquals((s1.getTotalTestCases()+s4.getTotalTestCases()+s10.getTotalTestCases()+s11.getTotalTestCases()+s12.getTotalTestCases()), ((Number) mostPlayed[5]).longValue());
        assertEquals(5, ((Number) mostPlayed[6]).longValue());
        assertTrue(((Number) mostPlayed[7]).doubleValue() > 0);

        assertNotNull(secondMostPlayed);
        assertEquals(p2.getProblemId(), ((Number) secondMostPlayed[0]).longValue());
        assertEquals(p2.getTitle(), (String) secondMostPlayed[1]);
        assertEquals(p2.getDescription(), (String) secondMostPlayed[2]);
        assertEquals(p2.getGameLanguage(), GameLanguage.valueOf(secondMostPlayed[3].toString()));
        assertEquals((s2.getPassedTestCases()+s5.getPassedTestCases()+s7.getPassedTestCases()+s13.getPassedTestCases()), ((Number) secondMostPlayed[4]).longValue());
        assertEquals((s2.getTotalTestCases()+s5.getTotalTestCases()+s7.getTotalTestCases()+s13.getTotalTestCases()), ((Number) secondMostPlayed[5]).longValue());
        assertEquals(4, ((Number) secondMostPlayed[6]).longValue());
        assertTrue(((Number) secondMostPlayed[7]).doubleValue() > 0);

        assertNotNull(thirdMostPlayed);
        assertEquals(p3.getProblemId(), ((Number) thirdMostPlayed[0]).longValue());
        assertEquals(p3.getTitle(), (String) thirdMostPlayed[1]);
        assertEquals(p3.getDescription(), (String) thirdMostPlayed[2]);
        assertEquals(p3.getGameLanguage(), GameLanguage.valueOf(thirdMostPlayed[3].toString()));
        assertEquals((s3.getPassedTestCases()+s6.getPassedTestCases()+s8.getPassedTestCases()), ((Number) thirdMostPlayed[4]).longValue());
        assertEquals((s3.getTotalTestCases()+s6.getTotalTestCases()+s8.getTotalTestCases()), ((Number) thirdMostPlayed[5]).longValue());
        assertEquals(3, ((Number) thirdMostPlayed[6]).longValue());
        assertTrue(((Number) thirdMostPlayed[7]).doubleValue() > 0);

        assertNotNull(leastPlayed);
        assertEquals(p4.getProblemId(), ((Number) leastPlayed[0]).longValue());
        assertEquals(p4.getTitle(), (String) leastPlayed[1]);
        assertEquals(p4.getDescription(), (String) leastPlayed[2]);
        assertEquals(p4.getGameLanguage(), GameLanguage.valueOf(leastPlayed[3].toString()));
        assertEquals((s9.getPassedTestCases()), ((Number) leastPlayed[4]).longValue());
        assertEquals((s9.getTotalTestCases()), ((Number) leastPlayed[5]).longValue());
        assertEquals(1, ((Number) leastPlayed[6]).longValue());
        assertTrue(((Number) leastPlayed[7]).doubleValue() > 0);

        assertEquals(5L, mostPlayedSubmissionCount);   
        assertEquals(4L, secondMostPlayedSubmissionCount); 
        assertEquals(3L, thirdMostPlayedSubmissionCount);
        assertEquals(1L, leastPlayedSubmissionCount);

        assertTrue(secondMostPlayedSubmissionCount < mostPlayedSubmissionCount);
        assertTrue(thirdMostPlayedSubmissionCount < secondMostPlayedSubmissionCount);
        assertTrue(leastPlayedSubmissionCount < thirdMostPlayedSubmissionCount);
    }
}
