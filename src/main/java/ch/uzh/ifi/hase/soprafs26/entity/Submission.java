package ch.uzh.ifi.hase.soprafs26.entity;

import jakarta.persistence.*;

import java.io.Serializable;
import java.time.LocalDateTime;

import ch.uzh.ifi.hase.soprafs26.constant.SubmissionStatus;
import ch.uzh.ifi.hase.soprafs26.constant.SubmissionType;

/**
 * Defines how submission is to be stored in the DB
 * Every variable will be mapped into a database field with the @Column
 * annotation
 * - nullable = false -> this cannot be left empty
 * - unique = true -> this value must be unqiue across the database
 */
@Entity
@Table(name = "submission")
public class Submission implements Serializable { // Careful submission doesn't necessarily imply 'SUBMIT' (it can also be 'RUN'), it's just the submission to Judge0
    
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue
    private Long submissionId; // Primary key

    @Column(nullable = false, columnDefinition="TEXT") // This lets us store superlong strings
    private String sourceCode;

    @Column(nullable = false)
    private LocalDateTime submittedAt;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING) // without this the enum will be stored as int in the DB, which will be unreadable
    private SubmissionType type;

    @Column(nullable = false)
    private int passedTestCases;

    @Column(nullable = false)
    private int totalTestCases;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private SubmissionStatus status;

    /**
     * 1:1 Relationship
     * CascadeType.ALL means if you save/delete the Submission, it will cascade 
     * that action to the ExecutionsResult, this is because Submission
     * is the boss of ExecutionResult and "owns it". Think about it an ExecutionResult can't exist
     * without a submission.
     * Refer to UML class diagram 
     */  
    @OneToOne(cascade = CascadeType.ALL) 
    @JoinColumn(name = "execution_result_id", referencedColumnName = "resultId")
    private ExecutionResult executionResult;

    public ExecutionResult getExecutionResult() {
        return executionResult;
    }

    public void setExecutionResult(ExecutionResult executionResult) {
        this.executionResult = executionResult;
    }

    public Long getSubmissionId() {
        return submissionId;
    }

    public void setSubmissionId(Long submissionId) {
        this.submissionId = submissionId;
    }

    public String getSourceCode() {
        return sourceCode;
    }

    public void setSourceCode(String sourceCode) {
        this.sourceCode = sourceCode;
    }

    public LocalDateTime getSubmittedAt() {
        return submittedAt;
    }

    // Set date automatically on creation
    @PrePersist
    protected void onCreate() {
    if (this.submittedAt == null) {
        this.submittedAt = LocalDateTime.now();
        }
    }

    

    public SubmissionType getType() {
        return type;
    }

    public void setType(SubmissionType type) {
        this.type = type;
    }

    public int getPassedTestCases() {
        return passedTestCases;
    }

    public void setPassedTestCases(int passedTestCases) {
        this.passedTestCases = passedTestCases;
    }

    public int getTotalTestCases() {
        return totalTestCases;
    }

    public void setTotalTestCases(int totalTestCases) {
        this.totalTestCases = totalTestCases;
    }

    public SubmissionStatus getStatus() {
        return status;
    }

    public void setStatus(SubmissionStatus status) {
        this.status = status;
    }
    

}
