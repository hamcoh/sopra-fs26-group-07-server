package ch.uzh.ifi.hase.soprafs26.service;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;  
import ch.uzh.ifi.hase.soprafs26.entity.Problem;
import ch.uzh.ifi.hase.soprafs26.repository.ProblemRepository;
import ch.uzh.ifi.hase.soprafs26.entity.TestCase;
import java.util.List;

/**
 * If we use seeding, we can use this service to create the problems and test cases 
 * that we want to have in our database when the application starts.
 * So this is only for internal seeding use and has nothing to do with the frontend / REST Api controller etc.
 * 
 * !! Also please not that we don't need a TestService because test cases are always created 
 * together with a problem and are then stored in the database via the cascade = CascadeType.ALL option
 * in the Problem entity. So we can just use the ProblemService to create problems 
 * and test cases at the same time. !!
 */

@Service
@Transactional
public class ProblemService {
    

    private final ProblemRepository problemRepository;

    public ProblemService(@Qualifier("problemRepository") ProblemRepository problemRepository) {
        this.problemRepository = problemRepository;
    }


    // [Actual methods]
    public Problem createProblem(Problem newProblem) {
        
        if (newProblem.getTestCases() == null || newProblem.getTestCases().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A problem must have at least one test case.");
        } else if (newProblem.getTitle() == null || newProblem.getTitle().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A problem must have a title.");
        } else if (newProblem.getDescription() == null || newProblem.getDescription().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A problem must have a description.");
        } else if (newProblem.getInputFormat() == null || newProblem.getInputFormat().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A problem must have an input format.");
        } else if (newProblem.getOutputFormat() == null || newProblem.getOutputFormat().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A problem must have an output format.");
        } else if (newProblem.getConstraints() == null || newProblem.getConstraints().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A problem must have constraints.");
        } else if (newProblem.getGameDifficulty() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A problem must have a game difficulty.");
        } else if (newProblem.getGameLanguage() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A problem must have a game language.");
        } else if (newProblem.getSampleSolution() == null || newProblem.getSampleSolution().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A problem must have a sample solution.");
        } else { // no edge cases then we can save the problem and save its testcasees.
            for (int i = 0; i < newProblem.getTestCases().size(); i++) {
                TestCase testCase = newProblem.getTestCases().get(i);
                if (testCase.getInput() == null || testCase.getInput().isBlank()) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Each test case must have an input.");
                } else if (testCase.getExpectedOutput() == null || testCase.getExpectedOutput().isBlank()) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Each test case must have an expected output.");
                }
                
                testCase.setProblem(newProblem);
            }
        }
        Problem createdProblem = problemRepository.save(newProblem);
        problemRepository.flush();

        return createdProblem;
    }

    public List<Problem> getAllProblems() {
        return problemRepository.findAll();
    }

    public Problem getProblemById(Long problemId) {
        if (problemId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Problem ID must not be null.");
        }
        return problemRepository.findById(problemId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Problem with ID " + problemId + " not found."));
    }

    // [/Actual methods]
}
