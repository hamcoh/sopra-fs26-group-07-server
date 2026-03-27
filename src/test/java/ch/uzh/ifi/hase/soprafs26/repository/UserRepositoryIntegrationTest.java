package ch.uzh.ifi.hase.soprafs26.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

import ch.uzh.ifi.hase.soprafs26.constant.UserStatus;
import ch.uzh.ifi.hase.soprafs26.entity.User;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DataJpaTest
class UserRepositoryIntegrationTest {

	@Autowired
	private TestEntityManager entityManager;

	@Autowired
	private UserRepository userRepository;

	@Test
	void findByUsername_success() {
		// given
		User user = new User();
		user.setUsername("firstname@lastname");
		user.setPassword("newPassword");
		user.setBio("hello world");
		user.setStatus(UserStatus.ONLINE);
		user.setToken("valid_token");
		user.setWinCount(0);
		user.setWinRatePercentage(0.0);
		user.setTotalGamesPlayed(0);
		user.setTotalPoints(0L);

		entityManager.persist(user);
		entityManager.flush();

		// when
		User found = userRepository.findByUsername(user.getUsername());

		// then
		assertNotNull(found.getId());
		assertNotNull(found.getCreationDate());
		assertEquals(found.getUsername(), user.getUsername());
		assertEquals(found.getToken(), user.getToken());
		assertEquals(found.getStatus(), user.getStatus());
		assertEquals(found.getBio(), user.getBio());
		assertEquals(found.getWinCount(), user.getWinCount());
		assertEquals(found.getWinRatePercentage(), user.getWinRatePercentage());
		assertEquals(found.getTotalGamesPlayed(), user.getTotalGamesPlayed());
		assertEquals(found.getTotalPoints(), user.getTotalPoints());
	}

	@Test
	void findByToken_success() {
		// given
		User user = new User();
		user.setUsername("firstname@lastname");
		user.setPassword("newPassword");
		user.setBio("hello world");
		user.setStatus(UserStatus.ONLINE);
		user.setToken("valid_token");
		user.setWinCount(0);
		user.setWinRatePercentage(0.0);
		user.setTotalGamesPlayed(0);
		user.setTotalPoints(0L);

		entityManager.persist(user);
		entityManager.flush();

		// when
		User found = userRepository.findByToken(user.getToken());

		// then
		assertNotNull(found.getId());
		assertNotNull(found.getCreationDate());
		assertEquals(found.getUsername(), user.getUsername());
		assertEquals(found.getToken(), user.getToken());
		assertEquals(found.getStatus(), user.getStatus());
		assertEquals(found.getBio(), user.getBio());
		assertEquals(found.getWinCount(), user.getWinCount());
		assertEquals(found.getWinRatePercentage(), user.getWinRatePercentage());
		assertEquals(found.getTotalGamesPlayed(), user.getTotalGamesPlayed());
		assertEquals(found.getTotalPoints(), user.getTotalPoints());
	}
}
