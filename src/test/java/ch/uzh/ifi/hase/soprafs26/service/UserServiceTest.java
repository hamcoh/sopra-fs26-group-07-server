package ch.uzh.ifi.hase.soprafs26.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.web.server.ResponseStatusException;

import ch.uzh.ifi.hase.soprafs26.constant.PlayerSessionStatus;
import ch.uzh.ifi.hase.soprafs26.constant.UserStatus;
import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.repository.PlayerSessionRepository;
import ch.uzh.ifi.hase.soprafs26.repository.UserRepository;
import ch.uzh.ifi.hase.soprafs26.util.PasswordHashUtil;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Optional;
import java.util.List;

class UserServiceTest {

	@Mock
	private UserRepository userRepository;

	@Mock
	private PlayerSessionRepository playerSessionRepository;

	@InjectMocks
	private UserService userService;

	private User testUser;

	@BeforeEach
	void setup() {
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
	void createUser_validInputs_success() {
		// when -> any object is being save in the userRepository -> return the dummy
		// testUser
		User createdUser = userService.createUser(testUser);

		// then
		Mockito.verify(userRepository, Mockito.times(1)).save(Mockito.any());

		assertEquals(testUser.getId(), createdUser.getId());
		assertEquals(testUser.getUsername(), createdUser.getUsername());
		assertEquals(testUser.getPassword(), createdUser.getPassword());
		assertNotNull(createdUser.getToken());
		assertTrue(createdUser.getAvatarId() >= 1 && createdUser.getAvatarId() <= 10);
		assertEquals(UserStatus.ONLINE, createdUser.getStatus());
		assertEquals(0, createdUser.getWinCount());
		assertEquals(0.0, createdUser.getWinRatePercentage());
		assertEquals(0, createdUser.getTotalGamesPlayed());
		assertEquals(0L, createdUser.getTotalPoints());
	}

	@Test
	void createUser_duplicateUsername_throwsException() {
		// given -> a first user has already been created
		userService.createUser(testUser);

		// when -> setup additional mocks for UserRepository
		Mockito.when(userRepository.findByUsername(Mockito.any())).thenReturn(testUser);

		// then -> attempt to create second user with same user -> check that an error
		// is thrown
		assertThrows(ResponseStatusException.class, () -> userService.createUser(testUser));
	}

	@Test
	void loginUser_validInputs_success() {
		// given: stored user has hashed password
		String salt = PasswordHashUtil.generateSalt();
		testUser.setPassword(PasswordHashUtil.hashPassword("testPassword", salt));
		testUser.setSalt(salt);
		testUser.setStatus(UserStatus.OFFLINE);

		User loginAttempt = new User();
		loginAttempt.setUsername(testUser.getUsername());
		loginAttempt.setPassword("testPassword");

		// when
		Mockito.when(userRepository.findByUsername(testUser.getUsername())).thenReturn(testUser);
		User loggedInUser = userService.loginUser(loginAttempt);

		// then
		assertEquals(UserStatus.ONLINE, loggedInUser.getStatus());
		assertNotNull(loggedInUser.getToken());
	}

	@Test
	void loginUser_userAlreadyLoggedIn_success() {
		// given: stored user has hashed password
		String salt = PasswordHashUtil.generateSalt();
		testUser.setPassword(PasswordHashUtil.hashPassword("testPassword", salt));
		testUser.setSalt(salt);
		testUser.setStatus(UserStatus.ONLINE);

		User loginAttempt = new User();
		loginAttempt.setUsername(testUser.getUsername());
		loginAttempt.setPassword("testPassword");

		// when
		Mockito.when(userRepository.findByUsername(testUser.getUsername())).thenReturn(testUser);
		User loggedInUser = userService.loginUser(loginAttempt);

		// then
		assertEquals(UserStatus.ONLINE, loggedInUser.getStatus());
		assertEquals(testUser.getToken(), loggedInUser.getToken()); //same session-token
	}

	@Test
	void loginUser_passwordMismatch_throwsException() {
		// given: stored user has hashed password
		String salt = PasswordHashUtil.generateSalt();
		testUser.setPassword(PasswordHashUtil.hashPassword("testPassword", salt));
		testUser.setSalt(salt);

		User loginAttempt = new User();
		loginAttempt.setUsername("testUsername");
		loginAttempt.setPassword("wrongPassword");

		//when
		Mockito.when(userRepository.findByUsername(testUser.getUsername())).thenReturn(testUser);

		//then
		assertThrows(ResponseStatusException.class, () -> userService.loginUser(loginAttempt));
	}

	@Test
	void loginUser_usernameNotFound_throwsException() {

		//given: unknown username
		User loginAttempt = new User();
		loginAttempt.setUsername("wrongUsername");
		loginAttempt.setPassword("testPassword");

		//when
		Mockito.when(userRepository.findByUsername(testUser.getUsername())).thenReturn(testUser);
		
		//then
		assertThrows(ResponseStatusException.class, () -> userService.loginUser(loginAttempt));
	}

	@Test
	void logoutUser_statusOffline() {
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

	@Test
	void logoutUser_afterPasswordChange() {
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
		assertEquals(PasswordHashUtil.hashPassword("newPassword", user.getSalt()), user.getPassword());
		assertEquals(UserStatus.OFFLINE, user.getStatus());
		assertNotEquals("oldToken", user.getToken());

		Mockito.verify(userRepository, Mockito.times(2)).save(user); // Verify that the user is saved two times (once for password change, once for status update)
		Mockito.verify(userRepository, Mockito.times(2)).flush(); // Verify that flush is called two times to persist the changes
	}

	@Test
	void getGlobalUsersLeaderboard_assignsRanksCorrectly() {
		
		// given
		User user1 = new User();
		user1.setTotalPoints(29L);

		User user2 = new User();
		user2.setTotalPoints(30L);

		User user3 = new User();
		user3.setTotalPoints(90L);

		List<User> users = List.of(user1, user2, user3);
		Mockito.when(userRepository.findAllByOrderByTotalPointsDesc()).thenReturn(users);

		// when
		List<User> result = userService.getGlobalUsersLeaderboard();

		// then
		assertEquals(1, result.get(0).getRank());
		assertEquals(2, result.get(1).getRank());
		assertEquals(3, result.get(2).getRank());
	}

	@Test
	void getGlobalUsersLeaderboard_noUsers_returnsEmptyList() { // function does not break when no users stored

		Mockito.when(userRepository.findAllByOrderByTotalPointsDesc()).thenReturn(List.of()); //return empty list

		List<User> users = userService.getGlobalUsersLeaderboard();

		assertTrue(users.isEmpty());
	}

	@Test
	void changeAvatar_validAvatarId_success() {
		// given
		testUser.setToken("validToken");
		testUser.setAvatarId(1);
		Mockito.when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
		Mockito.when(userRepository.findByToken("validToken")).thenReturn(testUser);
		Mockito.when(userRepository.findUserById(1L)).thenReturn(testUser);
		Mockito.when(playerSessionRepository.existsByPlayer_IdAndPlayerSessionStatus(1L, PlayerSessionStatus.PLAYING)).thenReturn(false);

		// when
		userService.changeAvatar(5, 1L, "validToken");

		// then
		assertEquals(5, testUser.getAvatarId());
		Mockito.verify(userRepository).save(testUser);
		Mockito.verify(userRepository).flush();
	}

	@Test
	void changeAvatar_avatarIdTooLow_throwsException() {
		// given
		testUser.setToken("validToken");
		Mockito.when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
		Mockito.when(userRepository.findByToken("validToken")).thenReturn(testUser);
		Mockito.when(playerSessionRepository.existsByPlayer_IdAndPlayerSessionStatus(1L, PlayerSessionStatus.PLAYING)).thenReturn(false);

		// then
		assertThrows(ResponseStatusException.class, () -> userService.changeAvatar(0, 1L, "validToken"));
	}

	@Test
	void changeAvatar_avatarIdTooHigh_throwsException() {
		// given
		testUser.setToken("validToken");
		Mockito.when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
		Mockito.when(userRepository.findByToken("validToken")).thenReturn(testUser);
		Mockito.when(playerSessionRepository.existsByPlayer_IdAndPlayerSessionStatus(1L, PlayerSessionStatus.PLAYING)).thenReturn(false);

		// then
		assertThrows(ResponseStatusException.class, () -> userService.changeAvatar(11, 1L, "validToken"));
	}

	@Test
	void changeAvatar_activePlayerSession_throwsException() {
		// given
		testUser.setToken("validToken");
		Mockito.when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
		Mockito.when(userRepository.findByToken("validToken")).thenReturn(testUser);
		Mockito.when(playerSessionRepository.existsByPlayer_IdAndPlayerSessionStatus(1L, PlayerSessionStatus.PLAYING)).thenReturn(true);

		// then
		assertThrows(ResponseStatusException.class, () -> userService.changeAvatar(3, 1L, "validToken"));
	}

	@Test
	void changeAvatar_invalidToken_throwsException() {
		// given
		Mockito.when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
		Mockito.when(userRepository.findByToken("badToken")).thenReturn(null);

		// then
		assertThrows(ResponseStatusException.class, () -> userService.changeAvatar(3, 1L, "badToken"));
	}
}