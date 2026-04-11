package ch.uzh.ifi.hase.soprafs26.rest.dto;
import java.util.List;

public class JudgeBatchResultDTO {
    
    private List<JudgeResultDTO> submissions;

    public List<JudgeResultDTO> getSubmissions() {
        return submissions;
    }
    
    public void setSubmissions(List<JudgeResultDTO> submissions) {
        this.submissions = submissions;
    }
}
