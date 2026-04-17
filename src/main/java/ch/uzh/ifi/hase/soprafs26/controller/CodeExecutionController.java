package ch.uzh.ifi.hase.soprafs26.controller;

import ch.uzh.ifi.hase.soprafs26.rest.dto.CodeExecutionPostDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.CodeRunDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.CodeSubmissionDTO;
import ch.uzh.ifi.hase.soprafs26.service.CodeExecutionService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CodeExecutionController {

    private final CodeExecutionService codeExecutionService;

    CodeExecutionController(CodeExecutionService codeExecutionService) {
        this.codeExecutionService = codeExecutionService;
    }

    @PostMapping("/games/{gameSessionId}/problems/{problemId}/runs")
    @ResponseStatus(HttpStatus.OK)
    public CodeRunDTO runCode(@PathVariable Long gameSessionId,
                              @PathVariable Long problemId,
                              @RequestBody CodeExecutionPostDTO requestBody) {

        return codeExecutionService.runCode(gameSessionId, problemId, requestBody);
    }

    @PostMapping("/games/{gameSessionId}/problems/{problemId}/submissions")
    @ResponseStatus(HttpStatus.OK)
    public CodeSubmissionDTO submitCode(@PathVariable Long gameSessionId,
                                        @PathVariable Long problemId,
                                        @RequestBody CodeExecutionPostDTO requestBody) {

        return codeExecutionService.submitCode(gameSessionId, problemId, requestBody);
    }

    @GetMapping("/games/{gameSessionId}/problems/{problemId}/run-result")
    @ResponseStatus(HttpStatus.OK)
    public CodeRunDTO getRunResult(@PathVariable Long gameSessionId,
                                   @PathVariable Long problemId,
                                   @RequestParam Long playerSessionId) {

        return codeExecutionService.getLatestRunResult(gameSessionId, problemId, playerSessionId);
    }

    @GetMapping("/games/{gameSessionId}/problems/{problemId}/submission-result")
    @ResponseStatus(HttpStatus.OK)
    public CodeSubmissionDTO getSubmissionResult(@PathVariable Long gameSessionId,
                                                 @PathVariable Long problemId,
                                                 @RequestParam Long playerSessionId) {

        return codeExecutionService.getLatestSubmissionResult(gameSessionId, problemId, playerSessionId);
    }
}