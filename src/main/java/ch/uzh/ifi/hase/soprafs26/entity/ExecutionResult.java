package ch.uzh.ifi.hase.soprafs26.entity;

import jakarta.persistence.*;

import java.io.Serializable;

import ch.uzh.ifi.hase.soprafs26.constant.Verdict;

/**
 * The result that came back from executing the code
 */
@Entity
@Table(name = "execution_results")
public class ExecutionResult implements Serializable {
    
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue
    private Long resultId;

    // Makes it owned by e
    @OneToOne(mappedBy = "executionResult") 
    private Submission submission;

    public Submission getSubmission() {
        return submission;
    }

    public void setSubmission(Submission submission) {
        this.submission = submission;
    }

    @Column(nullable = true, columnDefinition="TEXT") // unsure: if it's an error there may not be an output?
    private String output;

    @Column(nullable = true, columnDefinition="TEXT") // unsure: logically there doesnt need to be an error if it works
    private String errorMessage;

    @Column(nullable = true, columnDefinition="TEXT")
    private String compileOutput;

    @Column(nullable = true)
    private Double runTime;

    @Column(nullable = true)
    private Double memory;

    @Column(nullable = true)
    @Enumerated(EnumType.STRING)
    private Verdict verdict;

    public Verdict getVerdict() {
        return verdict;
    }

    public void setVerdict(Verdict verdict) {
        this.verdict = verdict;
    }

    public String getOutput() {
        return output;
    }

    public void setOutput(String output) {
        this.output = output;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getCompileOutput() {
        return compileOutput;
    }

    public void setCompileOutput(String compileOutput) {
        this.compileOutput = compileOutput;
    }

    public Double getRunTime() {
        return runTime;
    }

    public void setRunTime(Double runTime) {
        this.runTime = runTime;
    }

    public Double getMemory() {
        return memory;
    }

    public void setMemory(Double memory) {
        this.memory = memory;
    }

}
