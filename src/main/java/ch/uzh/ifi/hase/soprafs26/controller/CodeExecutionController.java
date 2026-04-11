package ch.uzh.ifi.hase.soprafs26.controller;

import ch.uzh.ifi.hase.soprafs26.service.CodeExecutionService;
import org.springframework.web.bind.annotation.RestController;

import ch.uzh.ifi.hase.soprafs26.rest.dto.CodeExecutionPostDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.CodeRunDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.CodeSubmissionDTO;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;


@RestController
public class CodeExecutionController {

    private final CodeExecutionService codeExecutionService;

    CodeExecutionController(CodeExecutionService codeExecutionService) {
        this.codeExecutionService = codeExecutionService;
    }

    @PostMapping("/games/{gameSessionId}/problems/{problemId}/runs")
    @ResponseStatus(HttpStatus.OK)
    public CodeRunDTO runCode(@PathVariable long gameSessionId,
                              @PathVariable  long problemId,
                              @RequestBody CodeExecutionPostDTO requestBody) {
        
        return codeExecutionService.runCode(gameSessionId, problemId, requestBody);
    }

    @PostMapping("/games/{gameSessionId}/problems/{problemId}/submissions")
    @ResponseStatus(HttpStatus.OK)
    public CodeSubmissionDTO submitCode(@PathVariable long gameSessionId,
                                        @PathVariable  long problemId,
                                        @RequestBody CodeExecutionPostDTO requestBody) {

        return codeExecutionService.submitCode(gameSessionId, problemId, requestBody);
    }
}
