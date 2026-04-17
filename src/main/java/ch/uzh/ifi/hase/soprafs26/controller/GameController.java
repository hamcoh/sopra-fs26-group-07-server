package ch.uzh.ifi.hase.soprafs26.controller;

import org.springframework.web.bind.annotation.RestController;

import ch.uzh.ifi.hase.soprafs26.entity.GameSession;
import ch.uzh.ifi.hase.soprafs26.rest.dto.GameRoundDTO;
import ch.uzh.ifi.hase.soprafs26.rest.mapper.DTOMapper;
import ch.uzh.ifi.hase.soprafs26.service.GameService;
import ch.uzh.ifi.hase.soprafs26.service.UserService;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.ResponseStatus;


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
                            }
