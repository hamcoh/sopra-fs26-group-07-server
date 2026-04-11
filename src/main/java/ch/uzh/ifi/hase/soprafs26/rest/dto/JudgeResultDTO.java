package ch.uzh.ifi.hase.soprafs26.rest.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class JudgeResultDTO {
    
    private String token;
    private String stdout;
    private String stderr;
    @JsonProperty("compile_output")
    private String compileOutput;
    
    private String message;
    private JudgeStatusDTO status;

    public String getToken() {
        return token;
    }
    public void setToken(String token) {
        this.token = token;
    }

    public String getStdout() {
        return stdout;
    }
    public void setStdout(String stdout) {
        this.stdout = stdout;
    }

    public String getStderr() {
        return stderr;
    }

    public void setStderr(String stderr) {
        this.stderr = stderr;
    }

    public String getCompile_output() {
        return compileOutput;
    }

    public void setCompile_output(String compile_output) {
        this.compileOutput = compile_output;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public JudgeStatusDTO getStatus() {
        return status;
    }

    public void setStatus(JudgeStatusDTO status) {
        this.status = status;
    }


}
