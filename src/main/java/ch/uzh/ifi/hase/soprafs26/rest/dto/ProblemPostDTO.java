package ch.uzh.ifi.hase.soprafs26.rest.dto;

import java.util.List;

public class ProblemPostDTO {

    private String title;
    private String description;
    private String inputFormat;
    private String outputFormat;
    private String constraints;
    private List<TestCasePostDTO> testCases;


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

    public List<TestCasePostDTO> getTestCases() {
        return testCases;
    }

    public void setTestCases(List<TestCasePostDTO> testCases) {
        this.testCases = testCases;
    }
 
}
