package ch.uzh.ifi.hase.soprafs26.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.*;

import ch.uzh.ifi.hase.soprafs26.entity.Room;
import ch.uzh.ifi.hase.soprafs26.rest.dto.RoomDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.RoomPostDTO;
import ch.uzh.ifi.hase.soprafs26.rest.mapper.DTOMapper;
import ch.uzh.ifi.hase.soprafs26.service.RoomService;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;


@RestController
public class RoomController {
    
    private final RoomService roomService;

    public RoomController(RoomService roomService) {
        this.roomService = roomService;
    }   

    @PostMapping("/rooms")
    @ResponseStatus(HttpStatus.CREATED)
    public RoomDTO createRoom(@RequestBody RoomPostDTO roomPostDTO, 
                                 @RequestHeader(value = "userId", required = false) Long userId, 
                                 @RequestHeader(value = "token", required = false) String token) {
        Room roomInput = DTOMapper.INSTANCE.convertRoomPostDTOtoEntity(roomPostDTO);
        Room createdRoom = roomService.createRoom(roomInput, userId, token);
        return DTOMapper.INSTANCE.convertEntityToRoomDTO(createdRoom);
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

    @PostMapping("/rooms/{roomId}/players")
    @ResponseStatus(HttpStatus.OK)
    public RoomDTO joinRoom(@PathVariable("roomId") Long roomId, 
            @RequestHeader(value = "userId", required = false) Long userId, 
            @RequestHeader(value = "token", required = false) String token,
            @RequestHeader(value = "roomJoinCode", required = false) String roomJoinCode) {
                
                Room joinedRoom = roomService.joinRoom(roomId, roomJoinCode, userId, token);
                return DTOMapper.INSTANCE.convertEntityToRoomDTO(joinedRoom);
            }


    @GetMapping("/rooms/{roomId}")
    @ResponseStatus(HttpStatus.OK)
    public RoomDTO getRoomDetails(@PathVariable("roomId") Long roomId,
                                  @RequestHeader(value = "userId", required = false) Long userId, 
                                  @RequestHeader(value = "token", required = false) String token) {

        Room room = roomService.getRoomDetails(roomId, userId, token);
        return DTOMapper.INSTANCE.convertEntityToRoomDTO(room);      
    }
    
}
