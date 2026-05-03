package ch.uzh.ifi.hase.soprafs26.controller;

import ch.uzh.ifi.hase.soprafs26.rest.dto.GameRoundDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.SkipPostDTO;
import org.springframework.web.bind.annotation.RestController;

import ch.uzh.ifi.hase.soprafs26.service.GameService;
import ch.uzh.ifi.hase.soprafs26.service.UserService;

import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RequestBody;



@RestController
public class GameController {

    private final UserService userService;
    private final GameService gameService;

    GameController(UserService userService, GameService gameService) {
		this.userService = userService;
        this.gameService = gameService;
	}

    @PostMapping("/rooms/{roomId}/games")
    @ResponseStatus(HttpStatus.CREATED)
    public void startGame(@PathVariable("roomId") Long roomId,
                                @RequestHeader(value = "hostId", required = false) Long hostId,
                                @RequestHeader(value = "token", required = false) String token) {
                                    userService.verifyToken(token);
                                    gameService.createGameSession(hostId, roomId);
                                }

    @PostMapping("/games/{gameSessionId}/problems/{problemId}/skips")
    public ResponseEntity<GameRoundDTO> skipProblem(@PathVariable("gameSessionId") Long gameSessionId,
                              @PathVariable("problemId") Long problemId,
                              @RequestHeader(value = "token", required = false) String token,
                              @RequestBody SkipPostDTO skipPostDTO) {
                    
        userService.verifyToken(token);
        return gameService.skipProblem(gameSessionId, problemId, skipPostDTO.getPlayerSessionId())
                          .map(ResponseEntity::ok)
                          .orElse(ResponseEntity.noContent().build());
    }
    
}
