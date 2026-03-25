package ch.uzh.ifi.hase.soprafs26.controller;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;


import ch.uzh.ifi.hase.soprafs26.constant.UserStatus;
import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.rest.dto.ChangePassDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.UserPostDTO;
import ch.uzh.ifi.hase.soprafs26.service.UserService;

import org.h2.mvstore.tx.TransactionStore.Change;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collections;
import java.util.List;
import java.util.Date;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * UserControllerTest
 * This is a WebMvcTest which allows to test the UserController i.e. GET/POST
 * request without actually sending them over the network.
 * This tests if the UserController works.
 */
@WebMvcTest(UserController.class)
public class UserControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private UserService userService;

	@Test
	public void givenUsers_whenGetUsers_thenReturnJsonArray() throws Exception {
		// given
		User user = new User();
		user.setUsername("firstname@lastname");
		user.setStatus(UserStatus.OFFLINE);

		List<User> allUsers = Collections.singletonList(user);

		// this mocks the UserService -> we define above what the userService should
		// return when getUsers() is called
		given(userService.getUsers()).willReturn(allUsers);

		// when
		MockHttpServletRequestBuilder getRequest = get("/users").contentType(MediaType.APPLICATION_JSON);

		// then
		mockMvc.perform(getRequest).andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(1)))
				.andExpect(jsonPath("$[0].username", is(user.getUsername())))
				.andExpect(jsonPath("$[0].status", is(user.getStatus().toString())));
	}

	//own: Happy Test: /users/register; expect: 201
	@Test
	public void registerUser_validInput_userCreated() throws Exception {
		// given
		User user = new User();
		user.setId(1L);
		user.setUsername("testUsername");
		user.setPassword("testPassword");
		user.setBio("testBio");
		user.setToken("1");
		user.setStatus(UserStatus.ONLINE);

		Date date = new Date();
		user.setCreationDate(date);

		UserPostDTO userPostDTO = new UserPostDTO();
		userPostDTO.setUsername("testUsername");
		userPostDTO.setPassword("testPassword");
		userPostDTO.setBio("testBio");

		given(userService.createUser(Mockito.any())).willReturn(user);

		// when/then -> do the request + validate the result
		MockHttpServletRequestBuilder postRequest = post("/users/register")
				.contentType(MediaType.APPLICATION_JSON)
				.content(asJsonString(userPostDTO));

		// then
		mockMvc.perform(postRequest)
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id", is(user.getId().intValue())))
				.andExpect(jsonPath("$.username", is(user.getUsername())))
				.andExpect(jsonPath("$.status", is(user.getStatus().toString())))
				.andExpect(jsonPath("$.creationDate").isNotEmpty())
				.andExpect(jsonPath("$.bio", is(user.getBio())))
				.andExpect(jsonPath("$.token", is(user.getToken())));		
	}

	//Negative Test: /users/register; expect: 409 (username already exists)
	@Test
	public void failedRegisterUser_usernameAlreadyExists() throws Exception {

		UserPostDTO userPostDTO = new UserPostDTO();
		userPostDTO.setUsername("user1");
		userPostDTO.setPassword("newPassword");
		userPostDTO.setBio("hello world!");

		String errorReason = "Provided username is not unique. Therefore, the user could not be created!";
		given(userService.createUser(Mockito.any())).willThrow(new ResponseStatusException(HttpStatus.CONFLICT, errorReason));

		MockHttpServletRequestBuilder postRequest = post("/users/register")
				.contentType(MediaType.APPLICATION_JSON)
				.content(asJsonString(userPostDTO));
		
		mockMvc.perform(postRequest)
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.detail", is(errorReason)));
	}

	//Negative Test: /users/register; expect: 400 (username cannot be empty/blank)
	@Test
	public void failedRegisterUser_invalidUserInput() throws Exception {

		UserPostDTO userPostDTO = new UserPostDTO();
		userPostDTO.setUsername("");

		String errorReason = "Username is invalid: username cannot be empty or contain only spaces!";
		given(userService.createUser(Mockito.any())).willThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST, errorReason));

		MockHttpServletRequestBuilder postRequest = post("/users/register")
				.contentType(MediaType.APPLICATION_JSON)
				.content(asJsonString(userPostDTO));
		
		mockMvc.perform(postRequest)
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.detail", is(errorReason)));
	}

	// /users/logout/{userId}; expect: 204 (logout successful)
	@Test
	public void logoutUser_validInput_logoutSuccessful() throws Exception {
		Long userId = 1L;
		String token = "validToken";

		MockHttpServletRequestBuilder postRequest = post("/users/logout/{userId}", userId)
				.header("token", token)
				.contentType(MediaType.APPLICATION_JSON);

		mockMvc.perform(postRequest)
				.andExpect(status().isNoContent());

		Mockito.verify(userService).verifyTokenAndUserId(token, userId); // verify that verifyTokenAndUserId was called
		Mockito.verify(userService).logoutUser(userId); // verify that logoutUser was called
	}

	// /users/logout/{userId}; expect: 403 (token/user mismatch)
	@Test
	public void logoutUser_invalidToken_userMismatch() throws Exception {
		Long userId = 1L;
		String token = "invalidToken";

		Mockito.doThrow(new ResponseStatusException(HttpStatus.FORBIDDEN, "Not allowed"))
				.when(userService).verifyTokenAndUserId(Mockito.eq(token), Mockito.eq(userId));

		MockHttpServletRequestBuilder postRequest = post("/users/logout/{userId}", userId)
				.header("token", token)
				.contentType(MediaType.APPLICATION_JSON);

		mockMvc.perform(postRequest)
				.andExpect(status().isForbidden());

		Mockito.verify(userService).verifyTokenAndUserId(token, userId); // verify that verifyTokenAndUserId was called
		Mockito.verify(userService, Mockito.never()).logoutUser(userId); // verify that logoutUser was NOT called
	}

	// /users/logout/{userId}; expect: 401 (token is missing)
	@Test
	public void logoutUser_missingToken_unauthorized() throws Exception {
		Long userId = 1L;
		String token = null;

		Mockito.doThrow(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized"))
				.when(userService).verifyTokenAndUserId(Mockito.eq(token), Mockito.eq(userId));

		MockHttpServletRequestBuilder postRequest = post("/users/logout/{userId}", userId)
				.contentType(MediaType.APPLICATION_JSON);
		
		mockMvc.perform(postRequest)
				.andExpect(status().isUnauthorized());
		
		Mockito.verify(userService).verifyTokenAndUserId(null, userId); // verify that verifyTokenAndUserId was called with null token
		Mockito.verify(userService, Mockito.never()).logoutUser(userId); // verify that logoutUser was NOT called
	}

	// /users/logout/{userId}; expect: 404 (user not found)
	@Test
	public void logoutUser_userNotFound() throws Exception {
		Long userId = 1L;
		String token = "validToken";

		Mockito.doThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"))
				.when(userService).verifyTokenAndUserId(token, userId);


		MockHttpServletRequestBuilder postRequest = post("/users/logout/{userId}", userId)
				.header("token", token)
				.contentType(MediaType.APPLICATION_JSON);

		mockMvc.perform(postRequest)
				.andExpect(status().isNotFound());

		Mockito.verify(userService).verifyTokenAndUserId(token, userId);
		Mockito.verify(userService, Mockito.never()).logoutUser(userId);
	}

	// /users/{userId}/password; expect: 204 (password change successful)
	@Test
	public void changePassword_validInput_passwordChangeSuccessful() throws Exception {
		Long userId = 1L;
		String token = "validToken";	

		ChangePassDTO changePassDTO = new ChangePassDTO();
		changePassDTO.setNewPassword("newValidPassword");

		MockHttpServletRequestBuilder putRequest = put("/users/{userId}/password", userId)
				.header("token", token)
				.contentType(MediaType.APPLICATION_JSON)
				.content(asJsonString(changePassDTO));

		mockMvc.perform(putRequest)
				.andExpect(status().isNoContent());

		Mockito.verify(userService).verifyTokenAndUserId(token, userId);
		Mockito.verify(userService).changePassword(Mockito.any(User.class), Mockito.eq(userId), Mockito.eq(token));
	}

	// /users/{userId}/password; expect: 400 (new password is empty)
	@Test
	public void changePassword_emptyNewPassword_badRequest() throws Exception {
		Long userId = 1L;
		String token = "validToken";

		ChangePassDTO changePassDTO = new ChangePassDTO();
		changePassDTO.setNewPassword("");

		Mockito.doThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST, "New password is empty"))
				.when(userService).changePassword(Mockito.any(User.class), Mockito.eq(userId), Mockito.eq(token));

		MockHttpServletRequestBuilder putRequest = put("/users/{userId}/password", userId)
				.header("token", token)
				.contentType(MediaType.APPLICATION_JSON)
				.content(asJsonString(changePassDTO));

		mockMvc.perform(putRequest)
				.andExpect(status().isBadRequest());

		Mockito.verify(userService).verifyTokenAndUserId(token, userId);
		Mockito.verify(userService).changePassword(Mockito.any(User.class), Mockito.eq(userId), Mockito.eq(token));
	}

	// /users/{userId}/password; expect: 401 (token is missing)
	@Test
	public void changePassword_missingToken_unauthorized() throws Exception {
		Long userId = 1L;
		String token = null;

		ChangePassDTO changePassDTO = new ChangePassDTO();
		changePassDTO.setNewPassword("newValidPassword");

		Mockito.doThrow(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized"))
				.when(userService).verifyTokenAndUserId(token, userId);

		MockHttpServletRequestBuilder putRequest = put("/users/{userId}/password", userId)
				.contentType(MediaType.APPLICATION_JSON)
				.content(asJsonString(changePassDTO));

		mockMvc.perform(putRequest)
				.andExpect(status().isUnauthorized());

		Mockito.verify(userService).verifyTokenAndUserId(token, userId);
		Mockito.verify(userService, Mockito.never()).changePassword(Mockito.any(User.class), Mockito.eq(userId), Mockito.eq(token));
	}

	// /users/{userId}/password; expect: 403 (token/user mismatch)
	@Test
	public void changePassword_invalidToken_userMismatch() throws Exception {
		Long userId = 1L;
		String token = "invalidToken";

		ChangePassDTO changePassDTO = new ChangePassDTO();
		changePassDTO.setNewPassword("newValidPassword");

		Mockito.doThrow(new ResponseStatusException(HttpStatus.FORBIDDEN, "Not allowed"))
				.when(userService).verifyTokenAndUserId(Mockito.eq(token), Mockito.eq(userId));

		MockHttpServletRequestBuilder putRequest = put("/users/{userId}/password", userId)
				.header("token", token)
				.contentType(MediaType.APPLICATION_JSON)
				.content(asJsonString(changePassDTO));
		
		mockMvc.perform(putRequest)
				.andExpect(status().isForbidden());

		Mockito.verify(userService).verifyTokenAndUserId(token, userId);
		Mockito.verify(userService, Mockito.never()).changePassword(Mockito.any(User.class), Mockito.eq(userId), Mockito.eq(token));
	}

	// /users/{userId}/password; expect: 404 (user not found)
	@Test
	public void changePassword_userNotFound() throws Exception {
		Long userId = 1L;
		String token = "validToken";

		ChangePassDTO changePassDTO = new ChangePassDTO();
		changePassDTO.setNewPassword("newValidPassword");

		Mockito.doThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"))
				.when(userService).verifyTokenAndUserId(token, userId);

		MockHttpServletRequestBuilder putRequest = put("/users/{userId}/password", userId)
				.header("token", token)
				.contentType(MediaType.APPLICATION_JSON)
				.content(asJsonString(changePassDTO));

		mockMvc.perform(putRequest)
				.andExpect(status().isNotFound());

		Mockito.verify(userService).verifyTokenAndUserId(token, userId);
		Mockito.verify(userService, Mockito.never()).changePassword(Mockito.any(User.class), Mockito.eq(userId), Mockito.eq(token));
	}

	/**
	 * Helper Method to convert userPostDTO into a JSON string such that the input
	 * can be processed
	 * Input will look like this: {"name": "Test User", "username": "testUsername"}
	 * 
	 * @param object
	 * @return string
	 */
	private String asJsonString(final Object object) {
		try {
			return new ObjectMapper().writeValueAsString(object);
		} catch (JacksonException e) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
					String.format("The request body could not be created.%s", e.toString()));
		}
	}
}