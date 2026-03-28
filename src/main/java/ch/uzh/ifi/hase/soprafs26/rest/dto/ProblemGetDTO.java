package ch.uzh.ifi.hase.soprafs26.rest.dto;

import java.util.List;

public class ProblemGetDTO {
    
    private Long problemId;
    private String title;
    private String description;
    private String inputFormat;
    private String outputFormat;
    private String constraints;
    private List<TestCaseGetDTO> testCases;

    public List<TestCaseGetDTO> getTestCases() {
        return testCases;
    }
    
    public void setTestCases(List<TestCaseGetDTO> testCases) {
        this.testCases = testCases;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getInputFormat() {
        return inputFormat;
    }

    public void setInputFormat(String inputFormat) {
        this.inputFormat = inputFormat;
    }

    public String getOutputFormat() {
        return outputFormat;
    }

    public void setOutputFormat(String outputFormat) {
        this.outputFormat = outputFormat;
    }

    public String getConstraints() {
        return constraints;
    }

    public void setConstraints(String constraints) {
        this.constraints = constraints;
    }

    public Long getProblemId() {
        return problemId;
    }

    public void setProblemId(Long problemId) {
        this.problemId = problemId;
    }


}
