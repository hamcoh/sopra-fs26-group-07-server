package ch.uzh.ifi.hase.soprafs26.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.server.ResponseStatusException;

import ch.uzh.ifi.hase.soprafs26.entity.Room;
import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.repository.RoomRepository;
import ch.uzh.ifi.hase.soprafs26.repository.UserRepository;
import ch.uzh.ifi.hase.soprafs26.rest.dto.GameRoundDTO;
import ch.uzh.ifi.hase.soprafs26.websocket.dto.RoomChatMessageDTO;

@ExtendWith(MockitoExtension.class)
public class WsRoomServiceTest {
    
    @Mock
    SimpMessagingTemplate simpMessagingTemplate;

    @Mock
    UserService userService;

    @Mock
    UserRepository userRepository;

    @Mock
    RoomRepository roomRepository;

    @InjectMocks
    private WsRoomService wsRoomService;

    private User testUser;
    private User testUser7;
    private Room testRoom;
    private RoomChatMessageDTO roomChatMessageDTO;

    @BeforeEach
    void setup() {

        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testUser");
        testUser.setToken("validToken");

        testUser7 = new User();
        testUser7.setId(7L);
        testUser7.setUsername("testUser7");
        testUser7.setToken("validToken7");

        testRoom = new Room();
        testRoom.setRoomId(1L);

        roomChatMessageDTO = new RoomChatMessageDTO();
        roomChatMessageDTO.setSenderUsername("testUser");
        roomChatMessageDTO.setContent("hello world");
    }

    @Test
    void notifyPlayerJoinedRoom_host_success() {
        
        Long roomId = testRoom.getRoomId();
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
        
        Long roomId = testRoom.getRoomId();
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
        ArgumentCaptor<GameRoundDTO> payloadCaptorGameRoundDTO = ArgumentCaptor.forClass(GameRoundDTO.class);

        verify(simpMessagingTemplate).convertAndSendToUser(
            usernameCaptor.capture(),
            destinationCaptor.capture(),
            payloadCaptorGameRoundDTO.capture()
        );

        assertEquals(testUser.getUsername(), usernameCaptor.getValue());
        assertEquals("/queue/game-start", destinationCaptor.getValue());

        GameRoundDTO captured = payloadCaptorGameRoundDTO.getValue();
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

    @Test
    void notifyPlayerGameStarted_userNotFound_throwsException() {
        GameRoundDTO gameRoundDTO = new GameRoundDTO();
        gameRoundDTO.setPlayerId(2121L);

        String errorReason = "Resource was not found!";
        Mockito.when(userService.getUserById(Mockito.any())).thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, errorReason));

        assertThrows(ResponseStatusException.class, () ->
                wsRoomService.notifyPlayerGameStarted(gameRoundDTO));
    }

    @Test
    void notifyRoomPlayerLeft_hostIsLeaving_success() {
        
        Long roomId = testRoom.getRoomId();
        boolean isHost = true;

        wsRoomService.notifyRoomPlayerLeft(testUser, roomId, isHost);

        verify(simpMessagingTemplate).convertAndSend(
            eq("/topic/room/" + roomId), 
            eq((Object) Map.of( 
                "type", "ROOM_CLOSED",
                "roomId", roomId.toString(),
                "username", testUser.getUsername(),
                "message", testUser.getUsername() + " closed the room. Room was deleted."
            ))
        );
    }

    @Test
    void notifyRoomPlayerLeft_nonHostIsLeaving_success() {
        
        Long roomId = testRoom.getRoomId();
        boolean isHost = false;

        wsRoomService.notifyRoomPlayerLeft(testUser, roomId, isHost);

        verify(simpMessagingTemplate).convertAndSend(
            eq("/topic/room/" + roomId), 
            eq((Object) Map.of( 
                "type", "PLAYER_LEFT",
                "roomId", roomId.toString(),
                "username", testUser.getUsername(),
                "message", testUser.getUsername() + " left the room. Room persists."
            ))
        );
    }

    @Test
    void sendRoomChatMessage_success() {

        testRoom.setPlayerIds(Set.of(testUser.getId()));

        Mockito.when(roomRepository.findByRoomId(Mockito.any())).thenReturn(testRoom);
        Mockito.when(userRepository.findByUsername(Mockito.any())).thenReturn(testUser);

        wsRoomService.sendRoomChatMessage(testRoom.getRoomId(), roomChatMessageDTO);

        ArgumentCaptor<String> destinationCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<RoomChatMessageDTO> payloadCaptorRoomChatMessageDTO= ArgumentCaptor.forClass(RoomChatMessageDTO.class);

        verify(simpMessagingTemplate, times(1)).convertAndSend(
            destinationCaptor.capture(),
            payloadCaptorRoomChatMessageDTO.capture()
        );

        assertEquals("/topic/chat/room/" + testRoom.getRoomId(), destinationCaptor.getValue());

        RoomChatMessageDTO captured = payloadCaptorRoomChatMessageDTO.getValue();
        assertEquals(roomChatMessageDTO.getContent(), captured.getContent());
        assertEquals(roomChatMessageDTO.getSenderUsername(), captured.getSenderUsername());
        assertNotNull(captured.getTimestamp());
    }

    @Test
    void sendRoomChatMessage_invalidRoomId_throwsException() {

        Mockito.when(roomRepository.findByRoomId(Mockito.any())).thenReturn(null);

        assertThrows(IllegalArgumentException.class, () ->
            wsRoomService.sendRoomChatMessage(testRoom.getRoomId(), roomChatMessageDTO));
    }

    @Test
    void sendRoomChatMessage_invalidSender_throwsException() {

        Mockito.when(roomRepository.findByRoomId(Mockito.any())).thenReturn(testRoom);
        Mockito.when(userRepository.findByUsername(Mockito.any())).thenReturn(null);

        assertThrows(IllegalArgumentException.class, () ->
            wsRoomService.sendRoomChatMessage(testRoom.getRoomId(), roomChatMessageDTO));
    }

    @Test
    void sendRoomChatMessage_senderNotInRoom_throwsException() {
        
        testRoom.setPlayerIds(Set.of(testUser7.getId()));

        Mockito.when(roomRepository.findByRoomId(Mockito.any())).thenReturn(testRoom);
        Mockito.when(userRepository.findByUsername(Mockito.any())).thenReturn(testUser);

        assertThrows(IllegalArgumentException.class, () ->
            wsRoomService.sendRoomChatMessage(testRoom.getRoomId(), roomChatMessageDTO));
    }

    @Test
    void sendRoomChatMessage_blankMessage_throwsException() {

        testRoom.setPlayerIds(Set.of(testUser.getId()));
        roomChatMessageDTO.setContent("  ");

        Mockito.when(roomRepository.findByRoomId(Mockito.any())).thenReturn(testRoom);
        Mockito.when(userRepository.findByUsername(Mockito.any())).thenReturn(testUser);

        assertThrows(IllegalArgumentException.class, () ->
            wsRoomService.sendRoomChatMessage(testRoom.getRoomId(), roomChatMessageDTO));
    }

    @Test
    void sendRoomChatMessage_emptyMessage_throwsException() {

        testRoom.setPlayerIds(Set.of(testUser.getId()));
        roomChatMessageDTO.setContent("");

        Mockito.when(roomRepository.findByRoomId(Mockito.any())).thenReturn(testRoom);
        Mockito.when(userRepository.findByUsername(Mockito.any())).thenReturn(testUser);

        assertThrows(IllegalArgumentException.class, () ->
            wsRoomService.sendRoomChatMessage(testRoom.getRoomId(), roomChatMessageDTO));
    }

    @Test
    void sendRoomChatMessage_messageTooLong_throwsException() {

        int charLimit = 255;

        testRoom.setPlayerIds(Set.of(testUser.getId()));
        roomChatMessageDTO.setContent("a".repeat(charLimit+1));

        Mockito.when(roomRepository.findByRoomId(Mockito.any())).thenReturn(testRoom);
        Mockito.when(userRepository.findByUsername(Mockito.any())).thenReturn(testUser);

        assertThrows(IllegalArgumentException.class, () ->
            wsRoomService.sendRoomChatMessage(testRoom.getRoomId(), roomChatMessageDTO));
    }

    // notifyRoomExpired sends ROOM_EXPIRED event to the room topic
    @Test
    void notifyRoomExpired_success() {
        Long roomId = testRoom.getRoomId();

        wsRoomService.notifyRoomExpired(roomId);

        verify(simpMessagingTemplate).convertAndSend(
            eq("/topic/room/" + roomId),
            eq((Object) Map.of(
                "type", "ROOM_EXPIRED",
                "roomId", roomId.toString(),
                "message", "Lobby closed due to inactivity."
            ))
        );
    }
}

