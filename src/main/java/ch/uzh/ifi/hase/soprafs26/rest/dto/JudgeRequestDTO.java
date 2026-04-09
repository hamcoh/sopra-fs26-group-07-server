package ch.uzh.ifi.hase.soprafs26.rest.dto;

public class JudgeRequestDTO {

    private String source_code;
    private Integer language_id;
    private String stdin;
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
