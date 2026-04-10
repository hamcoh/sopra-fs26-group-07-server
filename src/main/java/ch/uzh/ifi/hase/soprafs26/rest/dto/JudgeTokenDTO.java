package ch.uzh.ifi.hase.soprafs26.rest.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class JudgeTokenDTO {

    @JsonProperty("token")
    private String judgeToken;

    public String getJudgeToken() {
        return judgeToken; 
    }
    public void setJudgeToken(String judgeToken) {
        this.judgeToken = judgeToken;
    }
    
}
