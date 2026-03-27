package ch.uzh.ifi.hase.soprafs26.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

import ch.uzh.ifi.hase.soprafs26.constant.UserStatus;
import ch.uzh.ifi.hase.soprafs26.entity.User;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.List;

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

	@Test
	void findAllByOrderByTotalPointsDesc_success() {
		
		// given: 3 users with some points
		User user1 = new User();
		user1.setTotalPoints(100L);
		user1.setUsername("user1");
		user1.setPassword("newPassword1");
		user1.setStatus(UserStatus.ONLINE);
		user1.setToken("validToken1");
		user1.setWinCount(5);
		user1.setWinRatePercentage(50.0);
		user1.setTotalGamesPlayed(10);

		User user2 = new User();
		user2.setTotalPoints(5432L);
		user2.setUsername("user2");
		user2.setPassword("newPassword2");
		user2.setStatus(UserStatus.ONLINE);
		user2.setToken("validToken2");
		user2.setWinCount(123);
		user2.setWinRatePercentage(100.0);
		user2.setTotalGamesPlayed(123);

		User user3 = new User();
		user3.setTotalPoints(50L);
		user3.setUsername("user3");
		user3.setPassword("newPassword3");
		user3.setStatus(UserStatus.ONLINE);
		user3.setToken("validToken3");
		user3.setWinCount(2);
		user3.setWinRatePercentage(50.0);
		user3.setTotalGamesPlayed(4);

		entityManager.persist(user1);
		entityManager.persist(user2);
		entityManager.persist(user3);
		entityManager.flush();

		List<User> users = userRepository.findAllByOrderByTotalPointsDesc();

		assertEquals(5432L, users.get(0).getTotalPoints());
    	assertEquals(100L, users.get(1).getTotalPoints());
   		assertEquals(50L, users.get(2).getTotalPoints());
	}
}
