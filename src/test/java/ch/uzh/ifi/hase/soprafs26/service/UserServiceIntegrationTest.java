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
public class UserServiceIntegrationTest {

	@Qualifier("userRepository")
	@Autowired
	private UserRepository userRepository;

	@Autowired
	private UserService userService;

	@BeforeEach
	public void setup() {
		userRepository.deleteAll();
	}

	@Test
	public void createUser_validInputs_success() {
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
		assertEquals(testUser.getPassword(), createdUser.getPassword());
		assertNotNull(createdUser.getToken());
		assertEquals(UserStatus.ONLINE, createdUser.getStatus());
	}

	@Test
	public void createUser_duplicateUsername_throwsException() {
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
}
