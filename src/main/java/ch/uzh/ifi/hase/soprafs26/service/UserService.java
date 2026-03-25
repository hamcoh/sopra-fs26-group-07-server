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
		newUser.setStatus(UserStatus.ONLINE);
		checkIfUserExists(newUser);
		checkIfUsernameIsValid(newUser.getUsername());
		checkIfPasswordIsValid(newUser.getPassword());
		checkIfBioIsValid(newUser.getBio());

		initialiseGameStats(newUser);

		// saves the given entity but data is only persisted in the database once
		// flush() is called
		newUser = userRepository.save(newUser);
		userRepository.flush();

		log.debug("Created Information for User: {}", newUser);
		return newUser;
	}

	public User loginUser(User loginUser) {

		User user = userRepository.findByUsername(loginUser.getUsername());

		if (user == null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Login failed: No user found with given username!");
		}
		else if (!user.getPassword().equals(loginUser.getPassword())){
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Login failed: Invalid credentials!");
		}
		else if (user.getStatus().equals(UserStatus.ONLINE)){
			//user is already logged-in
			return user;
		}

		user.setStatus(UserStatus.ONLINE);
		user.setToken(UUID.randomUUID().toString());

		user = userRepository.save(user);
		userRepository.flush();

		log.debug("Successfully logged in User: {}", user);
		return user;
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

		logoutUser(userId);
	}

	public void logoutUser(Long userId) {
		User user = getUserbyId(userId);
		user.setStatus(UserStatus.OFFLINE);
		user.setToken(UUID.randomUUID().toString()); // invalidate the token by assigning a new random token
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

	private void checkIfUserExists(User userToBeCreated) {
		User userByUsername = userRepository.findByUsername(userToBeCreated.getUsername()); //own: case-sensitive atm
		if (userByUsername != null) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "Provided username is not unique. Therefore, the user could not be created!");
		}
	}

	/**
	 * OWN Helper Methods, such as checking for valid user-inputs.
	 */

	private void checkIfUsernameIsValid(String username) {
		if(username == null || username.isBlank()){
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Username is invalid: username cannot be empty or contain only spaces!");
		}
		else if (username.length() > 255) { //own: H2-db stores a string by default as VARCHAR(255), hence, check for safety
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Username is invalid: username cannot exceed 255 characters!");
		}
	}

	private void checkIfPasswordIsValid(String password) {
		if(password == null || password.isBlank()){
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password is invalid: password cannot be empty or contain only spaces!");
		}
		else if (password.length() > 255) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password is invalid: password cannot exceed 255 characters!");
		}
	} 

	private void checkIfBioIsValid(String bio) {
		if(bio != null && bio.length() > 255){
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Bio is invalid: bio cannot exceed 255 characters!");
		}
	}

	private void initialiseGameStats(User user){
		user.setWinCount(0);
		user.setWinRatePercentage(0.0);
		user.setTotalGamesPlayed(0);
		user.setTotalPoints(0L);
	}
}
