package ch.uzh.ifi.hase.soprafs26.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.web.server.ResponseStatusException;

import ch.uzh.ifi.hase.soprafs26.constant.UserStatus;
import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.repository.UserRepository;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Optional;

public class UserServiceTest {

	@Mock
	private UserRepository userRepository;

	@InjectMocks
	private UserService userService;

	private User testUser;

	@BeforeEach
	public void setup() {
		MockitoAnnotations.openMocks(this);

		// given
		testUser = new User();
		testUser.setId(1L);
		testUser.setUsername("testUsername");
		testUser.setPassword("testPassword");

		// when -> any object is being save in the userRepository -> return the dummy
		// testUser
		Mockito.when(userRepository.save(Mockito.any())).thenReturn(testUser);
	}

	@Test
	public void createUser_validInputs_success() {
		// when -> any object is being save in the userRepository -> return the dummy
		// testUser
		User createdUser = userService.createUser(testUser);

		// then
		Mockito.verify(userRepository, Mockito.times(1)).save(Mockito.any());

		assertEquals(testUser.getId(), createdUser.getId());
		assertEquals(testUser.getUsername(), createdUser.getUsername());
		assertEquals(testUser.getPassword(), createdUser.getPassword());
		assertNotNull(createdUser.getToken());
		assertEquals(UserStatus.ONLINE, createdUser.getStatus());
	}

	// @Test
	// public void createUser_duplicateName_throwsException() {
	// 	// given -> a first user has already been created
	// 	userService.createUser(testUser);

	// 	// when -> setup additional mocks for UserRepository
	// 	Mockito.when(userRepository.findByUsername(Mockito.any())).thenReturn(null);

	// 	// then -> attempt to create second user with same user -> check that an error
	// 	// is thrown
	// 	assertThrows(ResponseStatusException.class, () -> userService.createUser(testUser));
	// }

	@Test
	public void createUser_duplicateUsername_throwsException() {
		// given -> a first user has already been created
		userService.createUser(testUser);

		// when -> setup additional mocks for UserRepository
		Mockito.when(userRepository.findByUsername(Mockito.any())).thenReturn(testUser);

		// then -> attempt to create second user with same user -> check that an error
		// is thrown
		assertThrows(ResponseStatusException.class, () -> userService.createUser(testUser));
	}

	@Test
	public void logoutUser_statusOffline() {
		// given
		User user = new User();
		user.setId(1L);
		user.setUsername("testUsername");
		user.setPassword("testPassword");
		user.setStatus(UserStatus.ONLINE);
		
		Mockito.when(userRepository.findById(1L)).thenReturn(Optional.of(user));

		// when
		userService.logoutUser(user.getId());

		// then

		Mockito.verify(userRepository).save(user); // Verify that the user is saved with the updated status
		Mockito.verify(userRepository).flush(); // Verify that flush is called to persist the changes
		assertEquals(UserStatus.OFFLINE, user.getStatus());
	
	}

	@Test void logoutUser_afterPasswordChange() {
		// given
		User user = new User();
		user.setId(1L);
		user.setPassword("oldPassword");
		user.setStatus(UserStatus.ONLINE);
		user.setToken("oldToken");

		User userInput = new User();
		userInput.setPassword("newPassword");
		
		Mockito.when(userRepository.findById(1L)).thenReturn(Optional.of(user));
		Mockito.when(userRepository.findByToken("oldToken")).thenReturn(user);

		// when
		userService.changePassword(userInput, user.getId(), user.getToken());
		

		// then
		assertEquals("newPassword", user.getPassword());
		assertEquals(UserStatus.OFFLINE, user.getStatus());
		assertNotEquals("oldToken", user.getToken());

		Mockito.verify(userRepository, Mockito.times(2)).save(user); // Verify that the user is saved two times (once for password change, once for status update)
		Mockito.verify(userRepository, Mockito.times(2)).flush(); // Verify that flush is called two times to persist the changes
	}
}