package ch.uzh.ifi.hase.soprafs26.entity;

import java.io.Serializable;

import jakarta.persistence.*;

@Entity
@Table(name = "test_cases")
public class TestCase implements Serializable {
    
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue
    private Long testCaseId; 

    @Column(nullable = false, columnDefinition = "TEXT")
    private String input;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String expectedOutput;

    /**
     * Owning side in the relationship to Problem
     * since one Problem will have many testCases
     * 
     * this column is stored on the test_case table
     * it should point to the parent Problem
     */
    @ManyToOne
    @JoinColumn(name = "problem_id", nullable = false)
    private Problem problem;

    public Problem getProblem() {
        return problem;
    }

    public void setProblem(Problem problem) {
        this.problem = problem;
    }

    public Long getTestCaseId() {
        return testCaseId;
    }

    public void setTestCaseId(Long testCaseId) {
        this.testCaseId = testCaseId;
    }

    public String getInput() {
        return input;
    }

    public void setInput(String input) {
        this.input = input;
    }

    public String getExpectedOutput() {
        return expectedOutput;
    }

    public void setExpectedOutput(String expectedOutput) {
        this.expectedOutput = expectedOutput;
    }
    
}
