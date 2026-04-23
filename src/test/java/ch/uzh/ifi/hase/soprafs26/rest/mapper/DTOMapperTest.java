package ch.uzh.ifi.hase.soprafs26.rest.mapper;

import org.junit.jupiter.api.Test;

import ch.uzh.ifi.hase.soprafs26.constant.UserStatus;
import ch.uzh.ifi.hase.soprafs26.entity.Room;
import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.rest.dto.ChangePassDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.RoomDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.RoomGetDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.RoomPostDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.UserDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.UserGetDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.UserPostDTO;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Date;
import java.util.Set;

import ch.uzh.ifi.hase.soprafs26.constant.GameDifficulty;
import ch.uzh.ifi.hase.soprafs26.constant.GameLanguage;
import ch.uzh.ifi.hase.soprafs26.constant.GameMode;

/**
 * DTOMapperTest
 * Tests if the mapping between the internal and the external/API representation
 * works.
 */
class DTOMapperTest {
	@Test
	void testCreateUser_fromUserPostDTO_toUser_success() {
		// create UserPostDTO
		UserPostDTO userPostDTO = new UserPostDTO();
		userPostDTO.setUsername("username");

		// MAP -> Create user
		User user = DTOMapper.INSTANCE.convertUserPostDTOtoEntity(userPostDTO);

		// check content
		assertEquals(userPostDTO.getUsername(), user.getUsername());
	}


	@Test
	void testGetUser_fromUser_toUserGetDTO_success() {
		// create User
		User user = new User();
		user.setId(1L);
		user.setUsername("firstname@lastname");
		user.setStatus(UserStatus.OFFLINE);
		user.setBio("borbone is goated");
		user.setWinCount(10);
		user.setWinRatePercentage(100.0);
		user.setTotalGamesPlayed(10);
		user.setTotalPoints(1000L);
		user.setRank(1);
		user.setAvatarId(6);

		Date date = new Date();
		user.setCreationDate(date);

		// MAP -> Create UserGetDTO
		UserGetDTO userGetDTO = DTOMapper.INSTANCE.convertEntityToUserGetDTO(user);

		// check content
		assertEquals(user.getId(), userGetDTO.getId());
		assertEquals(user.getUsername(), userGetDTO.getUsername());
		assertEquals(user.getStatus(), userGetDTO.getStatus());
		assertEquals(user.getBio(), userGetDTO.getBio());
		assertEquals(user.getWinCount(), userGetDTO.getWinCount());
		assertEquals(user.getWinRatePercentage(), userGetDTO.getWinRatePercentage());
		assertEquals(user.getTotalGamesPlayed(), userGetDTO.getTotalGamesPlayed());
		assertEquals(user.getTotalPoints(), userGetDTO.getTotalPoints());
		assertEquals(user.getRank(), userGetDTO.getRank());
		assertEquals(user.getAvatarId(), userGetDTO.getAvatarId());
		assertNotNull(userGetDTO.getCreationDate());
	}

	@Test
	void testUserDTO_fromUser_toUserDTO_success() {
		// create User
		User user = new User();
		user.setId(1L);
		user.setToken("validToken");
		user.setUsername("testUser");
		user.setBio("hey everyone!");
		user.setStatus(UserStatus.ONLINE);

		Date date = new Date();
		user.setCreationDate(date);

		// MAP -> Create UserDTO
		UserDTO userDTO = DTOMapper.INSTANCE.convertEntityToUserDTO(user);

		// check content
		assertEquals(user.getId(), userDTO.getId());
		assertEquals(user.getToken(), userDTO.getToken());
		assertEquals(user.getUsername(), userDTO.getUsername());
		assertEquals(user.getBio(), userDTO.getBio());
		assertEquals(user.getStatus(), userDTO.getStatus());
		assertEquals(user.getCreationDate(), userDTO.getCreationDate()); //own: be careful with testing date-objects
	}

	@Test
	void userInput_from_ChangePassDTO_success() {
		// create ChangePassDTO
		ChangePassDTO changePassDTO = new ChangePassDTO();
		changePassDTO.setNewPassword("newValidPassword");

		User userInput = DTOMapper.INSTANCE.convertChangePassDTOtoEntity(changePassDTO);
		assertEquals(changePassDTO.getNewPassword(), userInput.getPassword());
	}

    @Test
    void convertRoomPostDTOtoEntity_validInput_success() {
        RoomPostDTO roomPostDTO = new RoomPostDTO();
        roomPostDTO.setGameDifficulty(GameDifficulty.EASY);
        roomPostDTO.setGameLanguage(GameLanguage.JAVA);
        roomPostDTO.setGameMode(GameMode.SPRINT);
        roomPostDTO.setMaxSkips(2);
        roomPostDTO.setTimeLimitSeconds(600);
        roomPostDTO.setNumOfProblems(3);

        Room room = DTOMapper.INSTANCE.convertRoomPostDTOtoEntity(roomPostDTO);

        assertEquals("EASY", room.getGameDifficulty().toString());
        assertEquals("JAVA", room.getGameLanguage().toString());
        assertEquals("SPRINT", room.getGameMode().toString());
        assertEquals(2, room.getMaxSkips());
        assertEquals(600, room.getTimeLimitSeconds());
        assertEquals(3, room.getNumOfProblems());
    }

    @Test
    void convertEntityToRoomGetDTO_validInput_success() {
        Room room = new Room();
        room.setRoomId(1L);
        room.setRoomJoinCode("ABC123");
        room.setMaxNumPlayers(2);
        room.setCurrentNumPlayers(1);
        room.setRoomOpen(true);
        room.setHostUserId(10L);
        room.setPlayerIds(Set.of(10L));
        room.setGameDifficulty(GameDifficulty.EASY);
        room.setGameLanguage(GameLanguage.JAVA);
        room.setGameMode(GameMode.RACE);
        room.setMaxSkips(2);
        room.setTimeLimitSeconds(600);
        room.setNumOfProblems(3);

        RoomGetDTO roomGetDTO = DTOMapper.INSTANCE.convertEntityToRoomGetDTO(room);

        assertEquals(1L, roomGetDTO.getRoomId());
        assertEquals("ABC123", roomGetDTO.getRoomJoinCode());
        assertEquals(2, roomGetDTO.getMaxNumPlayers());
        assertEquals(1, roomGetDTO.getCurrentNumPlayers());
        assertTrue(roomGetDTO.getIsRoomOpen());
        assertEquals("EASY", roomGetDTO.getGameDifficulty().toString());
		assertEquals("JAVA", roomGetDTO.getGameLanguage().toString());
		assertEquals("RACE", roomGetDTO.getGameMode().toString());
		assertEquals(2, roomGetDTO.getMaxSkips());
		assertEquals(600, roomGetDTO.getTimeLimitSeconds());
		assertEquals(3, roomGetDTO.getNumOfProblems());
    }

	@Test
    void convertEntityToRoomDTO_validInput_success() {
        Room room = new Room();
        room.setRoomId(1L);
        room.setRoomJoinCode("ABC123");
        room.setMaxNumPlayers(2);
        room.setCurrentNumPlayers(1);
        room.setRoomOpen(true);
        room.setHostUserId(10L);
        room.setPlayerIds(Set.of(10L));
        room.setGameDifficulty(GameDifficulty.HARD);
        room.setGameLanguage(GameLanguage.JAVA);
        room.setGameMode(GameMode.SPRINT);
        room.setMaxSkips(2);
        room.setTimeLimitSeconds(600);
        room.setNumOfProblems(3);

        RoomDTO roomDTO = DTOMapper.INSTANCE.convertEntityToRoomDTO(room);

        assertEquals(1L, roomDTO.getRoomId());
        assertEquals("ABC123", roomDTO.getRoomJoinCode());
        assertEquals(2, roomDTO.getMaxNumPlayers());
        assertEquals(1, roomDTO.getCurrentNumPlayers());
        assertTrue(roomDTO.getIsRoomOpen());
		assertEquals(10L, roomDTO.getHostUserId());
        assertEquals(Set.of(10L), roomDTO.getPlayerIds());
        assertEquals("HARD", roomDTO.getGameDifficulty().toString());
		assertEquals("JAVA", roomDTO.getGameLanguage().toString());
		assertEquals("SPRINT", roomDTO.getGameMode().toString());
		assertEquals(2, roomDTO.getMaxSkips());
		assertEquals(600, roomDTO.getTimeLimitSeconds());
		assertEquals(3, roomDTO.getNumOfProblems());
    }
}


