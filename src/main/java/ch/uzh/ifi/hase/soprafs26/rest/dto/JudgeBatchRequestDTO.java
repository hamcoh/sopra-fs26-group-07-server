package ch.uzh.ifi.hase.soprafs26.rest.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class JudgeBatchRequestDTO {

    // Forces this list to be named "submissions" in the JSON
    @JsonProperty("submissions")
    private List<JudgeRequestDTO> submissions;

    public List<JudgeRequestDTO> getSubmissions() {
        return submissions;
    }

    public void setSubmissions(List<JudgeRequestDTO> submissions) {
        this.submissions = submissions;
    }
}