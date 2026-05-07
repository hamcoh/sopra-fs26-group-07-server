package ch.uzh.ifi.hase.soprafs26.controller;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.security.Principal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import ch.uzh.ifi.hase.soprafs26.entity.Room;
import ch.uzh.ifi.hase.soprafs26.service.WsRoomService;
import ch.uzh.ifi.hase.soprafs26.websocket.dto.RoomChatMessageDTO;
import ch.uzh.ifi.hase.soprafs26.websocket.dto.RoomMessageDTO;


@ExtendWith(MockitoExtension.class)
public class WsRoomControllerTest {

    @Mock
    private WsRoomService wsRoomService;

    @Mock
    private SimpMessagingTemplate simpMessagingTemplate;

    @InjectMocks
    private WsRoomController wsRoomController;

    private RoomMessageDTO roomMessageDTO;
    private Room testRoom;
    private RoomChatMessageDTO roomChatMessageDTO;

    @BeforeEach
    void setup() {
        roomMessageDTO = new RoomMessageDTO();
        roomMessageDTO.setUsername("testUser");
        roomMessageDTO.setIsHost(true);

        testRoom = new Room();
        testRoom.setRoomId(1L);

        roomChatMessageDTO = new RoomChatMessageDTO();
        roomChatMessageDTO.setSenderUsername("testUser");
        roomChatMessageDTO.setContent("hello world");
    }

    @Test //tests correct delegation to wsRoomService
    void joinRoom_delegateNotificationToService() {

        wsRoomController.joinRoom(1L, roomMessageDTO);
        
        verify(wsRoomService, times(1)).notifyPlayerJoinedRoom(1L, roomMessageDTO.getUsername(), roomMessageDTO.isHost());
    }

    @Test //tests throws error when roomId is null
    void joinRoom_roomIdIsNull_throwsException() {

        assertThrows(IllegalArgumentException.class, () -> wsRoomController.joinRoom(null, roomMessageDTO));
    }

    @Test
    void sendMessageToRoom_delegateNotificationToService() {

        wsRoomController.sendMessageToRoom(testRoom.getRoomId(), roomChatMessageDTO);
        
        verify(wsRoomService, times(1)).sendRoomChatMessage(testRoom.getRoomId(), roomChatMessageDTO);
    }

    @Test
    void handleException_sendsErrorToUser() {
        String username = "testUser";
        Principal mockPrincipal = () -> username;
        
        String errorReason = "Invalid roomId!";
        wsRoomController.handleException(new IllegalArgumentException(errorReason), mockPrincipal);

        verify(simpMessagingTemplate, times(1)).convertAndSendToUser(
            eq(username),
            eq("/queue/errors"),
            eq(errorReason)
        );
    }
}
