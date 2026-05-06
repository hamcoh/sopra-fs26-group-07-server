package ch.uzh.ifi.hase.soprafs26.controller;

import java.security.Principal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import ch.uzh.ifi.hase.soprafs26.service.WsRoomService;
import ch.uzh.ifi.hase.soprafs26.websocket.dto.RoomMessageDTO;
import ch.uzh.ifi.hase.soprafs26.websocket.dto.RoomChatMessageDTO;

@Controller //handles+processes STOMP messages and delegates to wsRoomService that forwards to correct destination
public class WsRoomController {

    private final WsRoomService wsRoomService;
    private final SimpMessagingTemplate simpMessagingTemplate;
    private final Logger log = LoggerFactory.getLogger(WsRoomController.class);

    public WsRoomController(WsRoomService wsRoomService, SimpMessagingTemplate simpMessagingTemplate) {
        this.wsRoomService = wsRoomService;
        this.simpMessagingTemplate = simpMessagingTemplate;
    }
    
    @MessageMapping("/room/{roomId}/join") //listens to any STOMP-message sent to '/room/{roomId}/join' and invokes method 'joinRoom'
    //the message is sent back via 'notifyPlayerJoinedRoom' to destination '/topic/rooms/{roomId}' (~specific room subscription) 
    public void joinRoom(@DestinationVariable Long roomId, @Payload RoomMessageDTO roomMessageDTO) { //extract roomId + deserialises payload of message

        if (roomId == null) {
            throw new IllegalArgumentException("Received invalid destination variable!");
        }
        
        wsRoomService.notifyPlayerJoinedRoom(
            roomId,
            roomMessageDTO.getUsername(),
            roomMessageDTO.isHost()
        );
    }
    //IMPORTANT: Method 'joinRoom' expects payload of type {username: "test1", host: true}

    @MessageMapping("/room/{roomId}/send")
    public void sendMessageToRoom(@DestinationVariable Long roomId, @Payload RoomChatMessageDTO roomChatMessageDTO){

        wsRoomService.sendRoomChatMessage(roomId, roomChatMessageDTO);

    }

    //Needed for sending informative error messages (regarding chat messages) back to the client!
    @MessageExceptionHandler
    public void handleException(Exception ex, Principal principal) {
        log.info("Principal=" + principal.getName() + " gets the following error: " + ex.getMessage());
        simpMessagingTemplate.convertAndSendToUser(
            principal.getName(),
            "/queue/errors",
            ex.getMessage()
        );
        log.info("Error message was sent!");
    }
}