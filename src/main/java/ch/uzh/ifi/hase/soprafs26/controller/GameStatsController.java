package ch.uzh.ifi.hase.soprafs26.controller;

import org.springframework.web.bind.annotation.*;

import ch.uzh.ifi.hase.soprafs26.rest.dto.GameStatsDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.PlayerWrappedDTO;
import ch.uzh.ifi.hase.soprafs26.service.GameStatsService;
import ch.uzh.ifi.hase.soprafs26.service.UserService;

import java.util.List;

import org.springframework.http.HttpStatus;


@RestController
public class GameStatsController {
    
    private final UserService userService;
    private final GameStatsService gameStatsService;

    GameStatsController(UserService userService, GameStatsService gameStatsService) {
        this.userService = userService;
        this.gameStatsService = gameStatsService;
    }


    @GetMapping("/stats/hardest-problems")
    @ResponseStatus(HttpStatus.OK)
    public List<GameStatsDTO> getMomentaryHardestProblems(
        @RequestHeader(value = "token", required = false) String token,
        @RequestHeader(value = "userId", required = false) Long userId) {

        userService.verifyTokenAndUserId(token, userId);

        List<GameStatsDTO> gameStatsDTO = gameStatsService.getHardestProblemsAndPlayerResults(userId);

        return gameStatsDTO;
    }

    @GetMapping("/stats/popular-problems")
    @ResponseStatus(HttpStatus.OK)
    public List<GameStatsDTO> getMomentaryMostPopularProblems(
        @RequestHeader(value = "token", required = false) String token,
        @RequestHeader(value = "userId", required = false) Long userId) {

        userService.verifyTokenAndUserId(token, userId);

        List<GameStatsDTO> gameStatsDTO = gameStatsService.getMostPopularProblemsAndPlayerResults(userId);

        return gameStatsDTO;
    }

    @GetMapping("/stats/gameplay-summary/{userId}")
    @ResponseStatus(HttpStatus.OK)
    public PlayerWrappedDTO getGameplaySummary(
        @PathVariable("userId") Long userId, 
        @RequestHeader(value = "token", required = false) String token) {

        userService.verifyTokenAndUserId(token, userId);

        PlayerWrappedDTO playerWrappedDTO = gameStatsService.getGameplaySummary(userId);

        return playerWrappedDTO;
    }
    
}
