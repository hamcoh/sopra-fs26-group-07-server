package ch.uzh.ifi.hase.soprafs26.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import ch.uzh.ifi.hase.soprafs26.entity.Room;
import ch.uzh.ifi.hase.soprafs26.rest.dto.RoomGetDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.RoomPostDTO;
import ch.uzh.ifi.hase.soprafs26.rest.mapper.DTOMapper;
import ch.uzh.ifi.hase.soprafs26.service.RoomService;
import java.util.Map;

@RestController
public class RoomController {
    
    private final RoomService roomService;

    public RoomController(RoomService roomService) {
        this.roomService = roomService;
    }   

    @PostMapping("/rooms")
    @ResponseStatus(HttpStatus.CREATED)
    public RoomGetDTO createRoom(@RequestBody RoomPostDTO roomPostDTO, 
                                 @RequestHeader(value = "userId", required = false) Long userId, 
                                 @RequestHeader(value = "token", required = false) String token) {
        Room roomInput = DTOMapper.INSTANCE.convertRoomPostDTOtoEntity(roomPostDTO);
        Room createdRoom = roomService.createRoom(roomInput, userId, token);
        return DTOMapper.INSTANCE.convertEntityToRoomGetDTO(createdRoom);
    }

    //return more descriptive error message when invalid game settings attributes are received
    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleInvalidRoomSettingsValue(HttpMessageNotReadableException ex) {
        return Map.of(
            "reason", "Room creation failed: Invalid value provided",
            "message", "Check that all room settings fields have valid values!"
        );
    }
}
