package ch.uzh.ifi.hase.soprafs26.rest.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class JudgeRequestDTO {

    @JsonProperty("source_code")
    private String source_code;
    
    @JsonProperty("language_id")
    private Integer language_id;
    
    @JsonProperty("stdin")
    private String stdin;
    
    @JsonProperty("expected_output")
    private String expected_output;

    public String getSource_code() {
        return source_code;
    }
    public void setSource_code(String source_code) {
        this.source_code = source_code;
    }

    public Integer getLanguage_id() {
        return language_id;
    }
    public void setLanguage_id(Integer language_id) {
        this.language_id = language_id;
    }

    public String getStdin() {
        return stdin;
    }
    public void setStdin(String stdin) {
        this.stdin = stdin;
    }

    public String getExpected_output() {
        return expected_output;
    }
    public void setExpected_output(String expected_output) {
        this.expected_output = expected_output;
    }
}