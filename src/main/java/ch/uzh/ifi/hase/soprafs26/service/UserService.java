package ch.uzh.ifi.hase.soprafs26.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import ch.uzh.ifi.hase.soprafs26.constant.UserStatus;
import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.repository.UserRepository;

import java.util.List;
import java.util.UUID;

/**
 * User Service
 * This class is the "worker" and responsible for all functionality related to
 * the user
 * (e.g., it creates, modifies, deletes, finds). The result will be passed back
 * to the caller.
 */
@Service
@Transactional
public class UserService {

	private final Logger log = LoggerFactory.getLogger(UserService.class);

	private final UserRepository userRepository;

	public UserService(@Qualifier("userRepository") UserRepository userRepository) {
		this.userRepository = userRepository;
	}

	public List<User> getUsers() {
		return this.userRepository.findAll();
	}

	public User createUser(User newUser) {
		newUser.setToken(UUID.randomUUID().toString());
		newUser.setStatus(UserStatus.OFFLINE);
		// checkIfUserExists(newUser);
		// saves the given entity but data is only persisted in the database once
		// flush() is called
		newUser = userRepository.save(newUser);
		userRepository.flush();

		log.debug("Created Information for User: {}", newUser);
		return newUser;
	}

	public User getUserbyId(Long userId) {
		
		return userRepository.findById(userId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
	}

	public User getUserbyToken(String token) {
	
		User user = userRepository.findByToken(token);

    	if (user == null) {
        	throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
    	}

    	return user;
	}

	public void verifyToken(String token) {
		if (token == null || token.trim().isEmpty()) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token is missing");
		}
		User user = userRepository.findByToken(token);
		if (user == null) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token is invalid");
		}
	}


	public void verifyTokenAndUserId(String token, Long userId) {
		getUserbyId(userId);
		verifyToken(token);
		User user = userRepository.findByToken(token);
		if (!user.getId().equals(userId)) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Token/User mismatch"); 
		}	
	}

	public void changePassword(User userInput, Long userId, String token) {

		verifyTokenAndUserId(token, userId);

		if (userInput.getPassword() == null || userInput.getPassword().trim().isEmpty()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "New password cant be empty");
		}
		User user = getUserbyId(userId);
		user.setPassword(userInput.getPassword());

		userRepository.save(user);
		userRepository.flush();
	}

	/**
	 * This is a helper method that will check the uniqueness criteria of the
	 * username and the name
	 * defined in the User entity. The method will do nothing if the input is unique
	 * and throw an error otherwise.
	 *
	 * @param userToBeCreated
	 * @throws org.springframework.web.server.ResponseStatusException
	 * @see User
	 */
	// private void checkIfUserExists(User userToBeCreated) {
	// 	User userByUsername = userRepository.findByUsername(userToBeCreated.getUsername());

	// 	String baseErrorMessage = "The %s provided %s not unique. Therefore, the user could not be created!";
	// 	if (userByUsername != null && userByName != null) {
	// 		throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
	// 				String.format(baseErrorMessage, "username and the name", "are"));
	// 	} else if (userByUsername != null) {
	// 		throw new ResponseStatusException(HttpStatus.BAD_REQUEST, String.format(baseErrorMessage, "username", "is"));
	// 	} else if (userByName != null) {
	// 		throw new ResponseStatusException(HttpStatus.BAD_REQUEST, String.format(baseErrorMessage, "name", "is"));
	// 	}
	// }
}
