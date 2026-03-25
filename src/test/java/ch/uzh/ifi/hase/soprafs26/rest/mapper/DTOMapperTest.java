package ch.uzh.ifi.hase.soprafs26.rest.mapper;

import org.junit.jupiter.api.Test;

import ch.uzh.ifi.hase.soprafs26.constant.UserStatus;
import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.rest.dto.ChangePassDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.UserDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.UserGetDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.UserPostDTO;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Date;

/**
 * DTOMapperTest
 * Tests if the mapping between the internal and the external/API representation
 * works.
 */
public class DTOMapperTest {
	@Test
	public void testCreateUser_fromUserPostDTO_toUser_success() {
		// create UserPostDTO
		UserPostDTO userPostDTO = new UserPostDTO();
		userPostDTO.setUsername("username");

		// MAP -> Create user
		User user = DTOMapper.INSTANCE.convertUserPostDTOtoEntity(userPostDTO);

		// check content
		assertEquals(userPostDTO.getUsername(), user.getUsername());
	}

	@Test
	public void testGetUser_fromUser_toUserGetDTO_success() {
		// create User
		User user = new User();
		user.setUsername("firstname@lastname");
		user.setStatus(UserStatus.OFFLINE);
		user.setToken("1");

		// MAP -> Create UserGetDTO
		UserGetDTO userGetDTO = DTOMapper.INSTANCE.convertEntityToUserGetDTO(user);

		// check content
		assertEquals(user.getId(), userGetDTO.getId());
		assertEquals(user.getUsername(), userGetDTO.getUsername());
		assertEquals(user.getStatus(), userGetDTO.getStatus());
	}

	@Test
	public void testUserDTO_fromUser_toUserDTO_success() {
		// create User
		User user = new User();
		user.setId(1L);
		user.setToken("validToken");
		user.setUsername("testUser");
		user.setBio("hey everyone!");
		user.setStatus(UserStatus.ONLINE);

		Date date = new Date();
		user.setCreationDate(date);

		// MAP -> Create UserDTO
		UserDTO userDTO = DTOMapper.INSTANCE.convertEntityToUserDTO(user);

		// check content
		assertEquals(user.getId(), userDTO.getId());
		assertEquals(user.getToken(), userDTO.getToken());
		assertEquals(user.getUsername(), userDTO.getUsername());
		assertEquals(user.getBio(), userDTO.getBio());
		assertEquals(user.getStatus(), userDTO.getStatus());
		assertEquals(user.getCreationDate(), userDTO.getCreationDate()); //own: be careful with testing date-objects
	}

	@Test
	public void userInput_from_ChangePassDTO_success() {
		// create ChangePassDTO
		ChangePassDTO changePassDTO = new ChangePassDTO();
		changePassDTO.setNewPassword("newValidPassword");

		User userInput = DTOMapper.INSTANCE.convertChangePassDTOtoEntity(changePassDTO);
		assertEquals(changePassDTO.getNewPassword(), userInput.getPassword());
	}
}
