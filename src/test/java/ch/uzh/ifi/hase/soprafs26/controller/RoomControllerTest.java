package ch.uzh.ifi.hase.soprafs26.controller;

import java.util.HashSet;
import java.util.Set;

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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.hasSize;
import ch.uzh.ifi.hase.soprafs26.entity.Room;
import ch.uzh.ifi.hase.soprafs26.rest.dto.RoomPostDTO;
import ch.uzh.ifi.hase.soprafs26.service.RoomService;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import ch.uzh.ifi.hase.soprafs26.constant.GameDifficulty;
import ch.uzh.ifi.hase.soprafs26.constant.GameLanguage;
import ch.uzh.ifi.hase.soprafs26.constant.GameMode;

@WebMvcTest(RoomController.class)
class RoomControllerTest {

    @Autowired MockMvc mockMvc;

    @MockitoBean
    private RoomService roomService;

    // Create Room Success 201
    @Test
    void createRoom_validInput_success() throws Exception {
    Room room = new Room();
    room.setRoomId(1L);
    room.setRoomJoinCode("ABC123");
    room.setMaxNumPlayers(2);
    room.setCurrentNumPlayers(1);
    room.setRoomOpen(true);
    room.setHostUserId(1L);
    room.setPlayerIds(new HashSet<>(Set.of(1L)));
    room.setGameDifficulty(GameDifficulty.EASY);
    room.setGameLanguage(GameLanguage.PYTHON);
    room.setGameMode(GameMode.RACE);
    room.setMaxSkips(3);
    room.setTimeLimitSeconds(60);
    room.setNumOfProblems(10); 

    RoomPostDTO roomPostDTO = new RoomPostDTO();
    room.setGameDifficulty(GameDifficulty.EASY);
    room.setGameLanguage(GameLanguage.PYTHON);
    room.setGameMode(GameMode.RACE);
    roomPostDTO.setMaxSkips(3);
    roomPostDTO.setTimeLimitSeconds(60);
    roomPostDTO.setNumOfProblems(10);

    Mockito.when(roomService.createRoom(Mockito.any(), Mockito.anyLong(), Mockito.anyString())).thenReturn(room);

    MockHttpServletRequestBuilder postRequest = post("/rooms")
        .header("token", "valid_token")
        .header("userId", "1")
        .contentType("application/json")
        .content(asJsonString(roomPostDTO));

     mockMvc.perform(postRequest)
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.roomId", is(1)))
        .andExpect(jsonPath("$.roomJoinCode", is("ABC123")))
        .andExpect(jsonPath("$.maxNumPlayers", is(2)))
        .andExpect(jsonPath("$.currentNumPlayers", is(1)))
        .andExpect(jsonPath("$.isRoomOpen", is(true)))
        .andExpect(jsonPath("$.hostUserId", is(1)))
        .andExpect(jsonPath("$.playerIds", hasSize(1)))
        .andExpect(jsonPath("$.playerIds[0]", is(1)))
        .andExpect(jsonPath("$.gameDifficulty", is(room.getGameDifficulty().toString())))
        .andExpect(jsonPath("$.gameLanguage", is(room.getGameLanguage().toString())))
        .andExpect(jsonPath("$.gameMode", is(room.getGameMode().toString())))
        .andExpect(jsonPath("$.maxSkips", is(3)))
        .andExpect(jsonPath("$.timeLimitSeconds", is(60)))
        .andExpect(jsonPath("$.numOfProblems", is(10)));
    }

    // Create Room UnAuthorized 401 - Invalid Token
    @Test
    void createRoom_invalidToken_unauthorized() throws Exception {
        RoomPostDTO roomPostDTO = new RoomPostDTO();
        roomPostDTO.setGameDifficulty(GameDifficulty.HARD);
        roomPostDTO.setGameLanguage(GameLanguage.JAVA);
        roomPostDTO.setGameMode(GameMode.SPRINT);
        roomPostDTO.setMaxSkips(3);
        roomPostDTO.setTimeLimitSeconds(60);
        roomPostDTO.setNumOfProblems(10);

        Mockito.doThrow(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token is invalid"))
                .when(roomService).createRoom(Mockito.any(Room.class), Mockito.anyLong(), Mockito.eq("badToken"));

        MockHttpServletRequestBuilder postRequest = post("/rooms")
                .header("token", "badToken")
                .header("userId", "1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(roomPostDTO));

        mockMvc.perform(postRequest)
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.detail", is("Token is invalid")));
}

// Create Room Bad Request 400 - given invalid parameter
@Test
void createRoom_missingRequiredField_badRequest() throws Exception {
        
        String invalidGameSettings = """
        {
        "gameDifficulty": "INVALID",
        "gameLanguage": "PYTHON",
        "gameMode": "SPRINT"
        }
        """;

        String errorReason = "Room creation failed: Invalid value provided";
        String errorHandling = "Check that all room settings fields have valid values!";


        Mockito.doThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST, errorReason))
                .when(roomService).createRoom(Mockito.any(Room.class), Mockito.anyLong(), Mockito.anyString());

        MockHttpServletRequestBuilder postRequest = post("/rooms")
                .header("token", "valid_token")
                .header("userId", "1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidGameSettings);

        mockMvc.perform(postRequest)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.reason", is(errorReason)))
                .andExpect(jsonPath("$.message", is(errorHandling)));
        }

    /**
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
