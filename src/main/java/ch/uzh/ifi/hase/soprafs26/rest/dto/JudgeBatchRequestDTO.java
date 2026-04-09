package ch.uzh.ifi.hase.soprafs26.rest.dto;

import java.util.List;

public class JudgeBatchRequestDTO {

    private List<JudgeRequestDTO> submissions;

    public List<JudgeRequestDTO> getSubmissions() {
        return submissions;
    }

    public void setSubmissions(List<JudgeRequestDTO> submissions) {
        this.submissions = submissions;
    }
}