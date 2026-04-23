package ch.uzh.ifi.hase.soprafs26.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import ch.uzh.ifi.hase.soprafs26.constant.GameDifficulty;
import ch.uzh.ifi.hase.soprafs26.constant.GameLanguage;
import ch.uzh.ifi.hase.soprafs26.constant.GameMode;
import ch.uzh.ifi.hase.soprafs26.entity.Room;
import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.repository.RoomRepository;
import ch.uzh.ifi.hase.soprafs26.repository.UserRepository;
import jakarta.transaction.Transactional;

@SpringBootTest
@Transactional
class RoomServiceIntegrationTest {

    @Autowired private RoomService roomService;
    @Autowired private UserService userService;
    @Autowired private RoomRepository roomRepository;
    @Autowired private UserRepository userRepository;

    @BeforeEach
    void setup() {
        roomRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void createRoom_validHost_persistsAndAssignsJoinCode() {
        // given
        User host = new User();
        host.setUsername("ClaudeIusMaximus");
        host.setPassword("test");
        User createdHost = userService.createUser(host);

        Room roomInput = new Room();
        roomInput.setGameDifficulty(GameDifficulty.EASY);
        roomInput.setGameLanguage(GameLanguage.PYTHON);
        roomInput.setGameMode(GameMode.RACE);

        // when
        Room room = roomService.createRoom(roomInput, createdHost.getId(), createdHost.getToken());

        // then
        Room roomDB = roomRepository.findByRoomId(room.getRoomId());
        assertNotNull(roomDB.getRoomId());
        assertNotNull(roomDB.getRoomJoinCode());
        assertTrue(roomDB.getRoomJoinCode().matches("[A-F0-9]{6}"));
        assertEquals(createdHost.getId(), roomDB.getHostUserId());
        assertTrue(roomDB.isRoomOpen());
        assertEquals(1, roomDB.getCurrentNumPlayers());
        assertEquals(2, roomDB.getMaxNumPlayers());
        assertTrue(roomDB.getPlayerIds().contains(createdHost.getId()));
        assertEquals(GameDifficulty.EASY, roomDB.getGameDifficulty());
        assertEquals(GameLanguage.PYTHON, roomDB.getGameLanguage());
        assertEquals(GameMode.RACE, roomDB.getGameMode());
    }

    @Test
    void joinRoom_secondPlayer_closesRoomAndPersistsBothPlayers() {
        // given 
        User hostInput = new User();
        hostInput.setUsername("ClaudeIus");
        hostInput.setPassword("test");
        User host = userService.createUser(hostInput);

        Room roomInput = new Room();
        roomInput.setGameDifficulty(GameDifficulty.EASY);
        roomInput.setGameLanguage(GameLanguage.PYTHON);
        roomInput.setGameMode(GameMode.RACE);
        Room created = roomService.createRoom(roomInput, host.getId(), host.getToken());

        // and a second registered user
        User guestInput = new User();
        guestInput.setUsername("GeminiIus");
        guestInput.setPassword("test");
        User guest = userService.createUser(guestInput);

        // when 
        Room joined = roomService.joinRoom(created.getRoomJoinCode(), guest.getId(), guest.getToken());

        // then 
        Room roomDB = roomRepository.findByRoomId(joined.getRoomId());
        assertFalse(roomDB.isRoomOpen(), "room closes when full");
        assertEquals(2, roomDB.getCurrentNumPlayers());
        assertEquals(2, roomDB.getPlayerIds().size());
        assertTrue(roomDB.getPlayerIds().contains(host.getId()));
        assertTrue(roomDB.getPlayerIds().contains(guest.getId()));
        assertEquals(host.getId(), roomDB.getHostUserId(), "host unchanged");
    }
}
