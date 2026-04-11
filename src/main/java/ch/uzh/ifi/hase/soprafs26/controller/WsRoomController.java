package ch.uzh.ifi.hase.soprafs26.controller;

import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;

import ch.uzh.ifi.hase.soprafs26.service.WsRoomService;
import ch.uzh.ifi.hase.soprafs26.websocketDTO.roomMessageDTO;

@Controller //handles+processes STOMP messages and delegates to wsRoomService that forwards to correct destination
public class WsRoomController {

    private final WsRoomService wsRoomService;

    public WsRoomController(WsRoomService wsRoomService) {
        this.wsRoomService = wsRoomService;
    }
    
    @MessageMapping("/room/{roomId}/join") //listens to any STOMP-message sent to '/room/{roomId}/join' and invokes method 'joinRoom'
    //the message is sent back via 'notifyPlayerJoinedRoom' to destination '/topic/rooms/{roomId}' (~specific room subscription)
    public void joinRoom(@DestinationVariable Long roomId, @Payload roomMessageDTO roomMessageDTO) { //extract roomId + deserialises payload of message
        wsRoomService.notifyPlayerJoinedRoom(
            roomId,
            roomMessageDTO.getUsername(),
            roomMessageDTO.isHost()
        ); //no error-checking mechanisms yet
    }
    //IMPORTANT: Method 'joinRoom' expects payload of type {username: "test1", host: true}
}