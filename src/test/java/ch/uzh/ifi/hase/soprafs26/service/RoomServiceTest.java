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

import ch.uzh.ifi.hase.soprafs26.entity.Problem;
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

    @Mock
    private ProblemService problemService;

    @Mock
    private WsRoomService wsRoomService;

    @InjectMocks
    private RoomService roomService;

    private Room testRoom;
    private User testUser;
    private User player2;
    private User testHost;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);

        testUser = new User();
        testUser.setId(2L);

        player2 = new User();
        player2.setId(8L);
        player2.setToken("validToken8");

        testHost = new User();
        testHost.setId(1L); 
        testHost.setToken("validTokenHost");

        testRoom = new Room();
        testRoom.setRoomId(9L);
        testRoom.setRoomJoinCode("ABC123");
        testRoom.setRoomOpen(true);
        testRoom.setCurrentNumPlayers(1);
        Set<Long> playerIds = new HashSet<>();
        playerIds.add(testHost.getId()); //host with hostId=1L
        testRoom.setPlayerIds(playerIds);
        testRoom.setHostUserId(testHost.getId());
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
        roomPostDTO.setNumOfProblems(null); //CHANGE THIS
        
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

    // Create Room Fails: more requested Problems than available
    @Test
    void createRoom_moreProblemsRequestedThenAvailable_throwsBadRequest() {
        User host = new User();
        host.setId(1L);
        host.setUsername("hostUser");
        host.setToken("validToken");

        testRoom.setNumOfProblems(5432);
        
        given(userService.getUserbyId(host.getId())).willReturn(host);
        given(problemService.getAllProblems()).willReturn(List.of(new Problem()));

        assertThrows(ResponseStatusException.class, () ->
                roomService.createRoom(testRoom, host.getId(), host.getToken()));
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
        given(roomRepository.findByRoomJoinCode("ABC123")).willReturn(testRoom);

        Room resultRoom = roomService.joinRoom("ABC123", 2L, "validToken");

        assertTrue(resultRoom.getPlayerIds().contains(2L));
        assertEquals(2, resultRoom.getPlayerIds().size());
        assertEquals(2, resultRoom.getCurrentNumPlayers());
        assertFalse(resultRoom.isRoomOpen());
        verify(roomRepository).save(testRoom);
    }

    //Join room fail (400) 1
    @Test
    void failedjoinRoom_badFormattedRoomJoinCode_throwsNotFound1() {
        given(userService.getUserbyId(2L)).willReturn(testUser);

        assertThrows(ResponseStatusException.class, () ->
                roomService.joinRoom("ABc123", 2L, "validToken"));
    }

    //Join room fail (400) 2
    @Test
    void failedjoinRoom_badFormattedRoomJoinCode_throwsNotFound2() {
        given(userService.getUserbyId(2L)).willReturn(testUser);

        assertThrows(ResponseStatusException.class, () ->
                roomService.joinRoom("ABC1234", 2L, "validToken"));
    }

    //Join room fail (400) 3
    @Test
    void failedjoinRoom_badFormattedRoomJoinCode_throwsNotFound3() {
        given(userService.getUserbyId(2L)).willReturn(testUser);

        assertThrows(ResponseStatusException.class, () ->
                roomService.joinRoom("ABC12", 2L, "validToken"));
    }

    //Join room fail (400) 4
    @Test
    void failedjoinRoom_badFormattedRoomJoinCode_throwsNotFound4() {
        given(userService.getUserbyId(2L)).willReturn(testUser);

        assertThrows(ResponseStatusException.class, () ->
                roomService.joinRoom("ABG123", 2L, "validToken"));
    }

    //Join room fail (404)
    @Test
    void failedjoinRoom_roomNotFound_throwsNotFound() {
        given(roomRepository.findByRoomJoinCode(any())).willReturn(null);

        assertThrows(ResponseStatusException.class, () ->
                roomService.joinRoom("ABC123", 2L, "validToken"));
    }

    //Join room fail (409)
    @Test
    void failedjoinRoom_roomIsNotOpen_throwsConflict() {
        testRoom.setRoomOpen(false); //room already closed, no entry possible anymore
        given(userService.getUserbyId(2L)).willReturn(testUser);
        given(roomRepository.findByRoomJoinCode("ABC123")).willReturn(testRoom);

        assertThrows(ResponseStatusException.class, () ->
                roomService.joinRoom("ABC123", 2L, "validToken"));
    }

    //Join room fail (500; very defensive coding, this should never happen!)
    @Test
    void failedjoinRoom_roomIsInInvalidState_throwsInteralServerError() {
        testRoom.setCurrentNumPlayers(2); //should be only 1, since only host in room when another player joins
        given(userService.getUserbyId(2L)).willReturn(testUser);
        given(roomRepository.findByRoomJoinCode("ABC123")).willReturn(testRoom);

        assertThrows(ResponseStatusException.class, () ->
                roomService.joinRoom("ABC123", 2L, "validToken"));
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

    //non-host leaves room sucess
    @Test
    void leaveRoom_nonHostLeaves_success() {
        testRoom.getPlayerIds().add(player2.getId());
        testRoom.setCurrentNumPlayers(testRoom.getCurrentNumPlayers()+1);
        testRoom.setRoomOpen(false);

        doNothing().when(userService).verifyTokenAndUserId(player2.getToken(), player2.getId());
        given(roomRepository.findByRoomId(testRoom.getRoomId())).willReturn(testRoom);
        given(userService.getUserById(player2.getId())).willReturn(player2);
        doNothing().when(wsRoomService).notifyRoomPlayerLeft(player2, testRoom.getRoomId(), false);

        roomService.leaveRoom(testRoom.getRoomId(), player2.getId(), player2.getToken());

        assertEquals(1, testRoom.getCurrentNumPlayers());
        assertTrue(testRoom.isRoomOpen());
        assertEquals(1, testRoom.getPlayerIds().size());
        assertFalse(testRoom.getPlayerIds().contains(player2.getId()));

        verify(roomRepository, times(1)).saveAndFlush(testRoom);
        verify(wsRoomService, times(1)).notifyRoomPlayerLeft(player2, testRoom.getRoomId(), false);

    }

    //host leaves room sucess
    @Test
    void leaveRoom_hostLeaves_success() {
        testRoom.getPlayerIds().add(player2.getId());
        testRoom.setCurrentNumPlayers(testRoom.getCurrentNumPlayers()+1);
        testRoom.setRoomOpen(false);

        doNothing().when(userService).verifyTokenAndUserId(testHost.getToken(), testHost.getId());
        given(roomRepository.findByRoomId(testRoom.getRoomId())).willReturn(testRoom);
        given(userService.getUserById(testHost.getId())).willReturn(testHost);
        doNothing().when(wsRoomService).notifyRoomPlayerLeft(testHost, testRoom.getRoomId(), true);

        roomService.leaveRoom(testRoom.getRoomId(), testHost.getId(), testHost.getToken());

        verify(roomRepository, times(1)).delete(testRoom);
        verify(roomRepository, times(1)).flush();
        verify(wsRoomService, times(1)).notifyRoomPlayerLeft(testHost, testRoom.getRoomId(), true);

    }

    //leave room, room not found
    @Test
    void leaveRoom_invalidRoomId_throwsNotFound() {
        
        Long invalidRoomId = 4L; 

        doNothing().when(userService).verifyTokenAndUserId(testHost.getToken(), testHost.getId());
        given(roomRepository.findByRoomId(invalidRoomId)).willReturn(null);


        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            roomService.leaveRoom(invalidRoomId, testHost.getId(), testHost.getToken());
        });

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    }

    //leave room, user not even in room to leave
    @Test
    void leaveRoom_playerNotInRoom_throwsForbidden() {
        
        doNothing().when(userService).verifyTokenAndUserId(testUser.getToken(), testUser.getId());
        given(roomRepository.findByRoomId(testRoom.getRoomId())).willReturn(testRoom);


        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            roomService.leaveRoom(testRoom.getRoomId(), testUser.getId(), testUser.getToken());
        });

        assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());
    }

}