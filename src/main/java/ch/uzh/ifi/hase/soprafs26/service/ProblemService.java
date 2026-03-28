package ch.uzh.ifi.hase.soprafs26.service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;  
import ch.uzh.ifi.hase.soprafs26.entity.Problem;
import ch.uzh.ifi.hase.soprafs26.repository.ProblemRepository;
import ch.uzh.ifi.hase.soprafs26.entity.TestCase;
import java.util.List;


@Service
@Transactional
public class ProblemService {
    
    private final Logger log = LoggerFactory.getLogger(ProblemService.class);

    private final ProblemRepository problemRepository;

    public ProblemService(@Qualifier("problemRepository") ProblemRepository problemRepository) {
        this.problemRepository = problemRepository;
    }


    // [Actual methods]
    public Problem createProblem(Problem newProblem) {
        
        if (newProblem.getTestCases() == null || newProblem.getTestCases().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A problem must have at least one test case.");
        } else if (newProblem.getTitle() == null || newProblem.getTitle().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A problem must have a title.");
        } else if (newProblem.getDescription() == null || newProblem.getDescription().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A problem must have a description.");
        } else if (newProblem.getInputFormat() == null || newProblem.getInputFormat().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A problem must have an input format.");
        } else if (newProblem.getOutputFormat() == null || newProblem.getOutputFormat().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A problem must have an output format.");
        } else if (newProblem.getConstraints() == null || newProblem.getConstraints().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A problem must have constraints.");
        } else { // no edge cases then we can save the problem and save it testcasees.
            for (int i = 0; i < newProblem.getTestCases().size(); i++) {
                TestCase testCase = newProblem.getTestCases().get(i);
                if (testCase.getInput() == null || testCase.getInput().isEmpty()) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Each test case must have an input.");
                } else if (testCase.getExpectedOutput() == null || testCase.getExpectedOutput().isEmpty()) {
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
