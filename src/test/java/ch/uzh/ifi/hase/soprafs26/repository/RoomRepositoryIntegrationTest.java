package ch.uzh.ifi.hase.soprafs26.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

import ch.uzh.ifi.hase.soprafs26.constant.GameDifficulty;
import ch.uzh.ifi.hase.soprafs26.constant.GameLanguage;
import ch.uzh.ifi.hase.soprafs26.constant.GameMode;
import ch.uzh.ifi.hase.soprafs26.entity.Room;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.HashSet;
import java.util.Set;


@DataJpaTest
class RoomRepositoryIntegrationTest {

	@Autowired
	private TestEntityManager entityManager;

	@Autowired
	private RoomRepository roomRepository;

	@Test
	void findByRoomId_success() {
		//given
		Room room = new Room();
		room.setRoomJoinCode("ABC543");
		room.setMaxNumPlayers(2);
		room.setCurrentNumPlayers(1);
		room.setRoomOpen(true);
		room.setHostUserId(1L);
		room.setPlayerIds(new HashSet<>(Set.of(1L)));
		room.setGameDifficulty(GameDifficulty.HARD);
		room.setGameLanguage(GameLanguage.JAVA);
		room.setGameMode(GameMode.RACE);
		room.setMaxSkips(2);
		room.setTimeLimitSeconds(600);
		room.setNumOfProblems(10);

		entityManager.persist(room);
		entityManager.flush();

		// when
		Room foundRoom = roomRepository.findByRoomId(room.getRoomId());

		//then
		assertNotNull(foundRoom.getRoomId());
		assertEquals(room.getRoomJoinCode(), foundRoom.getRoomJoinCode());
		assertEquals(room.getMaxNumPlayers(), foundRoom.getMaxNumPlayers());
		assertEquals(room.getCurrentNumPlayers(), foundRoom.getCurrentNumPlayers());
		assertEquals(room.isRoomOpen(), foundRoom.isRoomOpen());
		assertEquals(room.getHostUserId(), foundRoom.getHostUserId());
		assertEquals(room.getPlayerIds(), foundRoom.getPlayerIds());
		assertEquals(room.getGameDifficulty(), foundRoom.getGameDifficulty());
		assertEquals(room.getGameLanguage(), foundRoom.getGameLanguage());
		assertEquals(room.getGameMode(), foundRoom.getGameMode());
		assertEquals(room.getMaxSkips(), foundRoom.getMaxSkips());
		assertEquals(room.getTimeLimitSeconds(), foundRoom.getTimeLimitSeconds());
		assertEquals(room.getNumOfProblems(), foundRoom.getNumOfProblems());
	}
	
	@Test
	void lockedFieldsAreNotUpdatedAfterRoomCreation_success() {
		
        // given
		Room room = new Room();
		room.setRoomJoinCode("12F869");
		room.setMaxNumPlayers(2);
		room.setCurrentNumPlayers(1); //updatable
		room.setRoomOpen(true); //updatable
		room.setHostUserId(1L);
		room.setPlayerIds(new HashSet<>(Set.of(1L))); //updatable
		room.setGameDifficulty(GameDifficulty.EASY);
		room.setGameLanguage(GameLanguage.PYTHON);
		room.setGameMode(GameMode.SPRINT);
		room.setMaxSkips(2);
		room.setTimeLimitSeconds(600);
		room.setNumOfProblems(10);

		entityManager.persist(room);
		entityManager.flush();

		// when
		Room savedRoom = roomRepository.findByRoomId(room.getRoomId());

		// attempt to change not updatable fields
		savedRoom.setRoomJoinCode("C29411");
		savedRoom.setMaxNumPlayers(32);
		savedRoom.setHostUserId(5432L);
		savedRoom.setGameDifficulty(GameDifficulty.HARD);
		savedRoom.setGameLanguage(GameLanguage.JAVA);
		savedRoom.setGameMode(GameMode.RACE);
		savedRoom.setMaxSkips(250);
		savedRoom.setTimeLimitSeconds(24000);
		savedRoom.setNumOfProblems(15000);

		entityManager.persist(savedRoom);
		entityManager.flush();
		entityManager.clear(); //avoid getting cached-object and force actual db hit

		Room roomToCheck = roomRepository.findByRoomId(savedRoom.getRoomId());

		// then (check with initial settings)
		assertEquals("12F869", roomToCheck.getRoomJoinCode());
		assertEquals(2, roomToCheck.getMaxNumPlayers());
		assertEquals(1L, roomToCheck.getHostUserId());
		assertEquals(GameDifficulty.EASY, roomToCheck.getGameDifficulty());
		assertEquals(GameLanguage.PYTHON, roomToCheck.getGameLanguage());
		assertEquals(GameMode.SPRINT, roomToCheck.getGameMode());
		assertEquals(2, roomToCheck.getMaxSkips());
		assertEquals(600, roomToCheck.getTimeLimitSeconds());
		assertEquals(10, roomToCheck.getNumOfProblems());
	}
}