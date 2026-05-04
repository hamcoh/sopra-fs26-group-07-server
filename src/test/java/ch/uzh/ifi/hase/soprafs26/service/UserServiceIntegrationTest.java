package ch.uzh.ifi.hase.soprafs26.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.web.server.ResponseStatusException;

import ch.uzh.ifi.hase.soprafs26.constant.UserStatus;
import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.repository.UserRepository;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for the UserResource REST resource.
 *
 * @see UserService
 */
@WebAppConfiguration
@SpringBootTest
class UserServiceIntegrationTest {

	@Qualifier("userRepository")
	@Autowired
	private UserRepository userRepository;

	@Autowired
	private UserService userService;

	@BeforeEach
	void setup() {
		userRepository.deleteAll();
	}

	@Test
	void createUser_validInputs_success() {
		// given
		assertNull(userRepository.findByUsername("testUsername"));

		User testUser = new User();
		testUser.setUsername("testUsername");
		testUser.setPassword("testPassword");

		// when
		User createdUser = userService.createUser(testUser);

		// then
		assertEquals(testUser.getId(), createdUser.getId());
		assertEquals(testUser.getUsername(), createdUser.getUsername());
		assertNotEquals("testPassword", createdUser.getPassword());
		assertNotNull(createdUser.getSalt());
		assertNotNull(createdUser.getToken());
		assertEquals(UserStatus.ONLINE, createdUser.getStatus());
		assertEquals(0, createdUser.getWinCount());
		assertEquals(0.0, createdUser.getWinRatePercentage());
		assertEquals(0, createdUser.getTotalGamesPlayed());
		assertEquals(0L, createdUser.getTotalPoints());
	}

	@Test
	void createUser_duplicateUsername_throwsException() {
		assertNull(userRepository.findByUsername("testUsername"));

		User testUser = new User();
		testUser.setUsername("testUsername");
		testUser.setPassword("testPassword");
		userService.createUser(testUser);

		// attempt to create second user with same username
		User testUser2 = new User();

		// change the name but forget about the username
		testUser2.setUsername("testUsername");
		testUser.setPassword("testPassword");

		// check that an error is thrown
		assertThrows(ResponseStatusException.class, () -> userService.createUser(testUser2));
	}

	@Test
	void changeAvatar_validInputs_persistsChange() {
		// given
		User testUser = new User();
		testUser.setUsername("testUsername");
		testUser.setPassword("testPassword");
		User createdUser = userService.createUser(testUser);
		int newAvatarId = (createdUser.getAvatarId() % 10) + 1; // guaranteed valid and different from any possible current value

		// when
		userService.changeAvatar(newAvatarId, createdUser.getId(), createdUser.getToken());

		// then
		User updatedUser = userRepository.findUserById(createdUser.getId());
		assertEquals(newAvatarId, updatedUser.getAvatarId());
	}

	@Test
	void changeAvatar_invalidAvatarId_throwsException() {
		// given
		User testUser = new User();
		testUser.setUsername("testUsername");
		testUser.setPassword("testPassword");
		User createdUser = userService.createUser(testUser);

		// then
		assertThrows(ResponseStatusException.class,
				() -> userService.changeAvatar(99, createdUser.getId(), createdUser.getToken()));
	}
}
