package ch.uzh.ifi.hase.soprafs26.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import ch.uzh.ifi.hase.soprafs26.entity.User;

import java.util.List;

@Repository("userRepository")
public interface UserRepository extends JpaRepository<User, Long> {

	User findByUsername(String username);
	User findByToken(String token);
	User findUserById(Long userId);

	List<User> findAllByOrderByTotalPointsDesc(); //JPA transforms it into SQL query: 'SELECT * FROM users ORDER BY totalPoints DESC;'
}
