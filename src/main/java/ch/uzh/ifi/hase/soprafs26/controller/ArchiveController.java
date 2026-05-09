package ch.uzh.ifi.hase.soprafs26.controller;

import org.springframework.web.bind.annotation.RestController;

import ch.uzh.ifi.hase.soprafs26.rest.dto.ArchiveProblemDTO;
import ch.uzh.ifi.hase.soprafs26.service.ArchiveService;
import ch.uzh.ifi.hase.soprafs26.service.UserService;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;


@RestController
public class ArchiveController {
    
    private final UserService userService;
    private final ArchiveService archiveService;

    ArchiveController(UserService userService, ArchiveService archiveService) {
        this.userService = userService;
        this.archiveService = archiveService;
    }


    @GetMapping("archive/problems")
    public List<ArchiveProblemDTO> getDifficultProblems(
        @RequestHeader(value = "token", required = false) String token,
        @RequestHeader(value = "userId", required = false) Long userId) {

        userService.verifyTokenAndUserId(token, userId);

        List<ArchiveProblemDTO> archiveProblemDTOs = archiveService.getArchiveProblemsAndPlayerResults(userId);

        return archiveProblemDTOs;
    }
    
}
