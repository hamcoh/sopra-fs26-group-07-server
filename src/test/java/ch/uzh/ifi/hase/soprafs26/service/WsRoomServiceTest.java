package ch.uzh.ifi.hase.soprafs26.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.rest.dto.GameRoundDTO;

@ExtendWith(MockitoExtension.class)
public class WsRoomServiceTest {
    
    @Mock
    SimpMessagingTemplate simpMessagingTemplate;

    @Mock
    UserService userService;

    @InjectMocks
    private WsRoomService wsRoomService;

    @Test
    void notifyPlayerJoinedRoom_host_success() {
        
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
    void notifyPlayerJoinedRoom_player2_success() {
        
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

    @Test
    void notifyPlayerGameStarted_success() {
        
        User testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testUser");

        GameRoundDTO gameRoundDTO = new GameRoundDTO();

        Mockito.when(userService.getUserById(Mockito.any())).thenReturn(testUser);

        wsRoomService.notifyPlayerGameStarted(gameRoundDTO);

        verify(simpMessagingTemplate, times(1)).convertAndSendToUser(
            testUser.getUsername(),
            "/queue/game-start",
            gameRoundDTO
            );
        }

    @Test
    void notifyPlayerGameStarted_sendsCorrectGameRoundDTO_success() {

        User testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testUser");

        GameRoundDTO gameRoundDTO = new GameRoundDTO();
        gameRoundDTO.setGameSessionId(9L);
        gameRoundDTO.setPlayerSessionId(2L);
        gameRoundDTO.setPlayerId(17L);
        gameRoundDTO.setCurrentScore(5432);
        gameRoundDTO.setNumOfSkippedProblems(1);
        gameRoundDTO.setProblemId(8L);
        gameRoundDTO.setTitle("Valid Palindrome");
        gameRoundDTO.setDescription("Solve the plaindrome problem!");
        gameRoundDTO.setInputFormat("A single lowercase string containing only alphabetic characters");
        gameRoundDTO.setOutputFormat("Boolean: True if s is palindrome, otherwise False");
        gameRoundDTO.setConstraints("0 < len(s) < 50");

        Mockito.when(userService.getUserById(Mockito.any())).thenReturn(testUser);

        wsRoomService.notifyPlayerGameStarted(gameRoundDTO);

        ArgumentCaptor<String> usernameCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> destinationCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<GameRoundDTO> payloadCaptor = ArgumentCaptor.forClass(GameRoundDTO.class);

        verify(simpMessagingTemplate).convertAndSendToUser(
            usernameCaptor.capture(),
            destinationCaptor.capture(),
            payloadCaptor.capture()
        );

        assertEquals(testUser.getUsername(), usernameCaptor.getValue());
        assertEquals("/queue/game-start", destinationCaptor.getValue());

        GameRoundDTO captured = payloadCaptor.getValue();
        assertEquals(9, captured.getGameSessionId().intValue());
        assertEquals(2, captured.getPlayerSessionId().intValue());
        assertEquals(17, captured.getPlayerId().intValue());
        assertEquals(5432, captured.getCurrentScore());
        assertEquals(1, captured.getNumOfSkippedProblems());
        assertEquals(8, captured.getProblemId().intValue());
        assertEquals("Valid Palindrome", captured.getTitle());
        assertEquals("Solve the plaindrome problem!", captured.getDescription());
        assertEquals("A single lowercase string containing only alphabetic characters", captured.getInputFormat());
        assertEquals("Boolean: True if s is palindrome, otherwise False", captured.getOutputFormat());
        assertEquals("0 < len(s) < 50", captured.getConstraints());
    }
}

