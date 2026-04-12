package ch.uzh.ifi.hase.soprafs26.controller;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import ch.uzh.ifi.hase.soprafs26.service.WsRoomService;
import ch.uzh.ifi.hase.soprafs26.websocket.dto.RoomMessageDTO;


@ExtendWith(MockitoExtension.class)
public class WsRoomControllerTest {

    @Mock
    private WsRoomService wsRoomService;

    @InjectMocks
    private WsRoomController wsRoomController;

    @Test //tests correct delegation to wsRoomService
    void joinRoom_delegateNotificationToService() {

        RoomMessageDTO roomMessageDTO = new RoomMessageDTO();
        roomMessageDTO.setUsername("testUser");
        roomMessageDTO.setIsHost(true);
        
        wsRoomController.joinRoom(1L, roomMessageDTO);
        
        verify(wsRoomService).notifyPlayerJoinedRoom(1L, roomMessageDTO.getUsername(), roomMessageDTO.isHost());
    }

    @Test //tests throws error when roomId is null
    void joinRoom_roomIdIsNull_throwsException() {

        RoomMessageDTO roomMessageDTO = new RoomMessageDTO();
        roomMessageDTO.setUsername("testUser");
        roomMessageDTO.setIsHost(true);

        assertThrows(IllegalArgumentException.class, () -> wsRoomController.joinRoom(null, roomMessageDTO));
    }
}
