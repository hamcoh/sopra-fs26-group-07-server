package ch.uzh.ifi.hase.soprafs26.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.rest.dto.ChangePassDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.UserDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.UserGetDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.UserPostDTO;
import ch.uzh.ifi.hase.soprafs26.rest.mapper.DTOMapper;
import ch.uzh.ifi.hase.soprafs26.service.UserService;

import java.util.ArrayList;
import java.util.List;


/**
 * User Controller
 * This class is responsible for handling all REST request that are related to
 * the user.
 * The controller will receive the request and delegate the execution to the
 * UserService and finally return the result.
 */
@RestController
public class UserController {

	private final UserService userService;

	UserController(UserService userService) {
		this.userService = userService;
	}

	@GetMapping("/users/leaderboard")
	@ResponseStatus(HttpStatus.OK)
	public List<UserGetDTO> getGlobalLeaderboard(@RequestHeader(value = "token", required = false)  String token) {
		
		//check for permission
		userService.verifyToken(token);

		// fetch all users in the internal representation and sort them by totalPoints
		List<User> users = userService.getGlobalUsersLeaderboard();
		List<UserGetDTO> userGetDTOs = new ArrayList<>();

		// convert each user to the API representation
		for (User user : users) {
			userGetDTOs.add(DTOMapper.INSTANCE.convertEntityToUserGetDTO(user));
		}
		return userGetDTOs;
	}

	@GetMapping("/users/{userId}")
	@ResponseStatus(HttpStatus.OK) 
	@ResponseBody
	public UserGetDTO getUserInfo(@PathVariable("userId") Long userId, @RequestHeader(value = "token", required = false) String token) {
		
		userService.verifyToken(token);

		User user = userService.getUserById(userId);

		return DTOMapper.INSTANCE.convertEntityToUserGetDTO(user);
	}

	@PostMapping("/users/register")
	@ResponseStatus(HttpStatus.CREATED)
	public UserDTO createUser(@RequestBody UserPostDTO userPostDTO) {
		// convert API user to internal representation
		User userInput = DTOMapper.INSTANCE.convertUserPostDTOtoEntity(userPostDTO);

		// create user
		User createdUser = userService.createUser(userInput);
		// convert internal representation of user back to API
		return DTOMapper.INSTANCE.convertEntityToUserDTO(createdUser);
	}

	@PostMapping("/users/login")
	@ResponseStatus(HttpStatus.OK)
	public UserDTO loginUser(@RequestBody UserPostDTO userPostDTO) { 
		
		User userCredentials = DTOMapper.INSTANCE.convertUserPostDTOtoEntity(userPostDTO);

		User user = userService.loginUser(userCredentials);

		return DTOMapper.INSTANCE.convertEntityToUserDTO(user);
	}

	@PutMapping("users/{userId}/password")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void changePassword(@PathVariable Long userId, @RequestBody ChangePassDTO changePassDTO,
		 						@RequestHeader(value = "token", required = false)  String token) {
		userService.verifyTokenAndUserId(token, userId);
		User userInput = DTOMapper.INSTANCE.convertChangePassDTOtoEntity(changePassDTO);
		userService.changePassword(userInput, userId, token);	

	}

	@PostMapping("/users/logout/{userId}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void logoutUser(@PathVariable Long userId, @RequestHeader(value = "token", required = false)  String token) {
		userService.verifyTokenAndUserId(token, userId);
		userService.logoutUser(userId);
	}

	@PostMapping("/auth")
	@ResponseStatus(HttpStatus.OK)
	public void checkToken(@RequestHeader(value = "token", required = false)  String token) {
		userService.verifyToken(token);
	}

	@PutMapping("/users/{userId}/avatar")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void changeAvatar(@PathVariable Long userId,
							@RequestBody int avatarId,
							@RequestHeader(value = "token", required = false)  String token) {
		userService.changeAvatar(avatarId, userId, token);
	}
}

// trigger docker build 
// trigger sonar build
// just for commit