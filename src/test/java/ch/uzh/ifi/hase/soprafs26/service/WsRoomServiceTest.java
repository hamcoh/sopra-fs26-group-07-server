package ch.uzh.ifi.hase.soprafs26.service;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

@ExtendWith(MockitoExtension.class)
public class WsRoomServiceTest {
    
    @Mock
    SimpMessagingTemplate simpMessagingTemplate;

    @InjectMocks
    private WsRoomService wsRoomService;

    @Test
    void notifyPlayerJoinedRoom_host_sucess() {
        
        Long roomId = 1L;
        String username = "testUser";
        boolean isHost = true;

        wsRoomService.notifyPlayerJoinedRoom(roomId, username, isHost);

        //assert that the call was actually made
        verify(simpMessagingTemplate).convertAndSend(
            eq("/topic/room/" + roomId), //check if destination is correct
            eq((Object) Map.of( //check if payload is correct (host vs. non-host logic)
                "type", "HOST_CREATED",
                "roomId", roomId.toString(),
                "username", username,
                "message", username + " created the room"

            ))
        );
    }

    @Test
    void notifyPlayerJoinedRoom_player2_sucess() {
        
        Long roomId = 1L;
        String username = "player2";
        boolean isHost = false;

        wsRoomService.notifyPlayerJoinedRoom(roomId, username, isHost);

        verify(simpMessagingTemplate).convertAndSend(
            eq("/topic/room/" + roomId),
            eq((Object) Map.of(
                "type", "PLAYER_JOINED",
                "roomId", roomId.toString(),
                "username", username,
                "message", username + " joined the room!"

            ))
        );
    }
}

