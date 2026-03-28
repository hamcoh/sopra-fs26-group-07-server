package ch.uzh.ifi.hase.soprafs26.controller;

import ch.uzh.ifi.hase.soprafs26.entity.Problem;
import ch.uzh.ifi.hase.soprafs26.rest.dto.ProblemGetDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.ProblemPostDTO;
import ch.uzh.ifi.hase.soprafs26.rest.mapper.DTOMapper;
import ch.uzh.ifi.hase.soprafs26.service.ProblemService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
public class ProblemController {

    private final ProblemService problemService;

    ProblemController(ProblemService problemService) {
        this.problemService = problemService;
    }

    @PostMapping("/problems")
    @ResponseStatus(HttpStatus.CREATED)
    public ProblemGetDTO createProblem(@RequestBody ProblemPostDTO problemPostDTO) {
        Problem problemInput = DTOMapper.INSTANCE.convertProblemPostDTOtoEntity(problemPostDTO);
        Problem createdProblem = problemService.createProblem(problemInput);
        return DTOMapper.INSTANCE.convertEntityToProblemGetDTO(createdProblem);
    }

    @GetMapping("/problems")
    @ResponseStatus(HttpStatus.OK)
    public List<ProblemGetDTO> getAllProblems() {
        List<Problem> problems = problemService.getAllProblems();
        List<ProblemGetDTO> problemGetDTOs = new ArrayList<>();

        for (Problem problem : problems) {
            problemGetDTOs.add(DTOMapper.INSTANCE.convertEntityToProblemGetDTO(problem));
        }

        return problemGetDTOs;
    }

    @GetMapping("/problems/{problemId}")
    @ResponseStatus(HttpStatus.OK)
    public ProblemGetDTO getProblemById(@PathVariable("problemId") Long problemId) {
        Problem problem = problemService.getProblemById(problemId);
        return DTOMapper.INSTANCE.convertEntityToProblemGetDTO(problem);
    }
}