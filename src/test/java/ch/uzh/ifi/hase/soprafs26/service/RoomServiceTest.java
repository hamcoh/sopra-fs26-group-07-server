package ch.uzh.ifi.hase.soprafs26.service;


import ch.uzh.ifi.hase.soprafs26.entity.User; // <- ensure this is the correct User type

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import ch.uzh.ifi.hase.soprafs26.entity.Room;
import ch.uzh.ifi.hase.soprafs26.repository.RoomRepository;
import ch.uzh.ifi.hase.soprafs26.rest.dto.RoomPostDTO;
import ch.uzh.ifi.hase.soprafs26.rest.mapper.DTOMapper;
import ch.uzh.ifi.hase.soprafs26.constant.GameDifficulty;
import ch.uzh.ifi.hase.soprafs26.constant.GameLanguage;
import ch.uzh.ifi.hase.soprafs26.constant.GameMode;

class RoomServiceTest {

    @Mock
    private RoomRepository roomRepository;

    @Mock
    private UserService userService;

    @InjectMocks
    private RoomService roomService;

    private Room testRoom;
    private User testUser;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);

        testUser = new User();
        testUser.setId(2L);

        testRoom = new Room();
        testRoom.setRoomId(9L);
        testRoom.setRoomJoinCode("ALE123");
        testRoom.setRoomOpen(true);
        testRoom.setCurrentNumPlayers(1);
        Set<Long> playerIds = new HashSet<>();
        playerIds.add(1L); //host with hostId=1L
        testRoom.setPlayerIds(playerIds);
    }

    // Create Room Success 201
    @Test
    void createRoom_validInput_success() {
        // given
        User host = new User();
        host.setId(1L);
        host.setUsername("hostUser");

        RoomPostDTO roomPostDTO = new RoomPostDTO();
        roomPostDTO.setGameDifficulty(GameDifficulty.EASY);
        roomPostDTO.setGameLanguage(GameLanguage.PYTHON);
        roomPostDTO.setGameMode(GameMode.RACE);
        roomPostDTO.setMaxSkips(3);
        roomPostDTO.setTimeLimitSeconds(60);
        roomPostDTO.setNumOfProblems(10);  
        
        Mockito.when(userService.getUserbyToken("validToken")).thenReturn(host);
        Mockito.when(userService.getUserbyId(host.getId())).thenReturn(host);
        Mockito.when(roomRepository.save(Mockito.any(Room.class))).thenAnswer(invocation -> {
            Room room = invocation.getArgument(0);
            room.setRoomId(1L);
            return room;
        }); //returns same object that was passed in to avoid nullpointer exception

        Room createdRoom = roomService.createRoom(DTOMapper.INSTANCE.convertRoomPostDTOtoEntity(roomPostDTO), host.getId(), "validToken");
        
        assertEquals(host.getId(), createdRoom.getHostUserId());
        assertEquals(2, createdRoom.getMaxNumPlayers());
        assertEquals(roomPostDTO.getGameDifficulty(), createdRoom.getGameDifficulty());
        assertEquals(roomPostDTO.getGameLanguage(), createdRoom.getGameLanguage());
        assertEquals(roomPostDTO.getGameMode(), createdRoom.getGameMode());
        assertEquals(roomPostDTO.getMaxSkips(), createdRoom.getMaxSkips());
        assertEquals(roomPostDTO.getTimeLimitSeconds(), createdRoom.getTimeLimitSeconds());
        assertEquals(roomPostDTO.getNumOfProblems(), createdRoom.getNumOfProblems());
        assertNotNull(createdRoom.getRoomJoinCode());
        assertNotEquals("", createdRoom.getRoomJoinCode());
        assertTrue(createdRoom.isRoomOpen());
        assertEquals(1, createdRoom.getCurrentNumPlayers());
        assertNotNull(createdRoom.getRoomId());
        assertTrue(createdRoom.getPlayerIds().contains(host.getId()));
    }

    // getRoomDetails Player in room success 200
    @Test
    void getRoomDetails_playerInRoom_success() {
        // given
        Long roomId = 1L;
        Long userId = 1L;
        String token = "validToken";

        Room room = new Room();
        room.setRoomId(roomId);
        room.setHostUserId(userId);
        Set<Long> playerIds = new HashSet<>();
        playerIds.add(userId);
        room.setPlayerIds(playerIds);
        room.setCurrentNumPlayers(playerIds.size());

        doNothing().when(userService).verifyTokenAndUserId(token, userId);
        Mockito.when(roomRepository.findByRoomId(roomId)).thenReturn(room); 

        Room retrievedRoom = roomService.getRoomDetails(roomId, userId, token);

        assertEquals(roomId, retrievedRoom.getRoomId());
        assertEquals(userId, retrievedRoom.getHostUserId());
        assertTrue(retrievedRoom.getPlayerIds().contains(userId));
        assertEquals(1, retrievedRoom.getCurrentNumPlayers());
        assertNotNull(retrievedRoom);

        verify(userService).verifyTokenAndUserId(token, userId);
        verify(roomRepository).findByRoomId(roomId);
    }

    // getRoomDetails Player not in room failure 403
    @Test
    void getRoomDetails_playerNotInRoom_failure() {
        // given
        Long roomId = 1L;
        Long userId = 69L; // different user
        String token = "validToken";

        Room room = new Room();
        room.setRoomId(roomId);
        room.setHostUserId(1L); // different host
        Set<Long> playerIds = new HashSet<>();
        playerIds.add(1L); // different player
        room.setPlayerIds(playerIds);
        room.setCurrentNumPlayers(playerIds.size());

        doNothing().when(userService).verifyTokenAndUserId(token, userId);
        Mockito.when(roomRepository.findByRoomId(roomId)).thenReturn(room);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            roomService.getRoomDetails(roomId, userId, token);
        });

        assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());

        verify(userService).verifyTokenAndUserId(token, userId);
        verify(roomRepository).findByRoomId(roomId);
    }

    // getRoomDetails Room not found failure 404
    @Test
    void getRoomDetails_roomNotFound_failure() {
        // given
        Long roomId = 999L;
        Long userId = 1L;
        String token = "validToken";

        doNothing().when(userService).verifyTokenAndUserId(token, userId);
        Mockito.when(roomRepository.findByRoomId(roomId)).thenReturn(null);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            roomService.getRoomDetails(roomId, userId, token);
        });

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());

        verify(userService).verifyTokenAndUserId(token, userId);
        verify(roomRepository).findByRoomId(roomId);
    }

    //Join room success (200)
    @Test
    void joinRoom_validInputs_success() {

        given(userService.getUserbyId(2L)).willReturn(testUser);
        given(roomRepository.findByRoomId(9L)).willReturn(testRoom);

        Room resultRoom = roomService.joinRoom(9L, "ALE123", 2L, "validToken");

        assertTrue(resultRoom.getPlayerIds().contains(2L));
        assertEquals(2, resultRoom.getPlayerIds().size());
        assertEquals(2, resultRoom.getCurrentNumPlayers());
        assertFalse(resultRoom.isRoomOpen());
        verify(roomRepository).save(testRoom);
    }

    //Join room fail (404)
    @Test
    void failedjoinRoom_roomNotFound_throwsNotFound() {
        given(roomRepository.findByRoomId(any())).willReturn(null);

        assertThrows(ResponseStatusException.class, () ->
                roomService.joinRoom(19L, "ALE123", 2L, "validToken"));
    }

    //Join room fail (403)
    @Test
    void failedjoinRoom_invalidRoomJoinCode_throwsForbidden() {
        given(userService.getUserbyId(2L)).willReturn(testUser);
        given(roomRepository.findByRoomId(9L)).willReturn(testRoom);

        assertThrows(ResponseStatusException.class, () ->
                roomService.joinRoom(9L, "wrongRoomJoinCode", 2L, "validToken"));
    }

    //Join room fail (409)
    @Test
    void failedjoinRoom_roomIsNotOpen_throwsConflict() {
        testRoom.setRoomOpen(false); //room already closed, no entry possible anymore
        given(userService.getUserbyId(2L)).willReturn(testUser);
        given(roomRepository.findByRoomId(9L)).willReturn(testRoom);

        assertThrows(ResponseStatusException.class, () ->
                roomService.joinRoom(9L, "ALE123", 2L, "validToken"));
    }

    //Join room fail (500; very defensive coding, this should never happen!)
    @Test
    void failedjoinRoom_roomIsInInvalidState_throwsInteralServerError() {
        testRoom.setCurrentNumPlayers(2); //should be only 1, since only host in room when another player joins
        given(userService.getUserbyId(2L)).willReturn(testUser);
        given(roomRepository.findByRoomId(9L)).willReturn(testRoom);

        assertThrows(ResponseStatusException.class, () ->
                roomService.joinRoom(9L, "ALE123", 2L, "validToken"));
    }

    // getAllRooms valid user, only open rooms
    @Test
    void getAllRooms_validUser_returnsOnlyOpenRooms() {
        long userId = 1L;
        String token = "validToken";

        Room openRoom1 = new Room();
        openRoom1.setRoomId(1L);
        openRoom1.setRoomOpen(true);

        Room openRoom2 = new Room();
        openRoom2.setRoomId(2L);
        openRoom2.setRoomOpen(true);

        Room closedRoom = new Room();
        closedRoom.setRoomId(3L);
        closedRoom.setRoomOpen(false);

        List<Room> allRooms = List.of(openRoom1, openRoom2, closedRoom);

        doNothing().when(userService).verifyTokenAndUserId(token, userId);
        Mockito.when(roomRepository.findAll()).thenReturn(allRooms);

        List<Room> result = roomService.getAllRooms(userId, token);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.contains(openRoom1));
        assertTrue(result.contains(openRoom2));
        assertFalse(result.contains(closedRoom));

        verify(userService).verifyTokenAndUserId(token, userId);
        verify(roomRepository).findAll();
    }

    // getAllRooms valid user, no open rooms is a empty list not NULL
    @Test
    public void getAllActiveRooms_noOpenRooms_returnsEmptyList() {
        Long userId = 1L;
        String token = "validToken";

        Room closedRoom1 = new Room();
        closedRoom1.setRoomId(1L);
        closedRoom1.setRoomOpen(false);

        Room closedRoom2 = new Room();
        closedRoom2.setRoomId(2L);
        closedRoom2.setRoomOpen(false);

        when(roomRepository.findAll()).thenReturn(List.of(closedRoom1, closedRoom2));
        doNothing().when(userService).verifyTokenAndUserId(token, userId);

        List<Room> result = roomService.getAllRooms(userId, token);

        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(userService, times(1)).verifyTokenAndUserId(token, userId);
        verify(roomRepository, times(1)).findAll();
    }

    // getAllRooms unauthenticated user failure 401
    @Test
    void getAllRooms_unauthenticatedUser_throwsUnauthorized() {
        Long userId = 1L;
        String token = "invalidToken";

        Mockito.doThrow(new ResponseStatusException(HttpStatus.UNAUTHORIZED)).when(userService)
                                                                     .verifyTokenAndUserId(token, userId);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            roomService.getAllRooms(userId, token);
        });

        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusCode());

        verify(userService).verifyTokenAndUserId(token, userId);
        verify(roomRepository, times(0)).findAll();
    }
}