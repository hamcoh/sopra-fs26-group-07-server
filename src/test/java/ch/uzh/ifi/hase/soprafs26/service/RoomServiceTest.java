package ch.uzh.ifi.hase.soprafs26.service;


import ch.uzh.ifi.hase.soprafs26.entity.User; // <- ensure this is the correct User type

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.web.server.ResponseStatusException;

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

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
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

}