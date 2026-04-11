package ch.uzh.ifi.hase.soprafs26.controller;

import java.util.HashSet;
import java.util.List;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.hasSize;
import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.entity.Room;
import ch.uzh.ifi.hase.soprafs26.rest.dto.RoomPostDTO;
import ch.uzh.ifi.hase.soprafs26.service.RoomService;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import ch.uzh.ifi.hase.soprafs26.constant.GameDifficulty;
import ch.uzh.ifi.hase.soprafs26.constant.GameLanguage;
import ch.uzh.ifi.hase.soprafs26.constant.GameMode;

import static org.mockito.BDDMockito.given;

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
        roomPostDTO.setGameDifficulty(GameDifficulty.EASY);
        roomPostDTO.setGameLanguage(GameLanguage.PYTHON);
        roomPostDTO.setGameMode(GameMode.RACE);
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

    // Create Room Success 201 (without optional params)
    @Test
    void createRoom_validInputWithoutOptionalParams_success() throws Exception {
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

        RoomPostDTO roomPostDTO = new RoomPostDTO();
        roomPostDTO.setGameDifficulty(GameDifficulty.EASY);
        roomPostDTO.setGameLanguage(GameLanguage.PYTHON);
        roomPostDTO.setGameMode(GameMode.RACE);
        
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
                .andExpect(jsonPath("$.maxSkips").isEmpty())
                .andExpect(jsonPath("$.timeLimitSeconds").isEmpty())
                .andExpect(jsonPath("$.numOfProblems").isEmpty());
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
     void createRoom_inavlidRequiredField_badRequest() throws Exception {
        
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

    // Join Room Success 200
    @Test
    void joinRoom_validInput_success() throws Exception {
        Room room = new Room();
        room.setRoomId(18L);
        room.setRoomJoinCode("EFG879");
        room.setMaxNumPlayers(2);
        room.setCurrentNumPlayers(2);
        room.setRoomOpen(false);
        room.setHostUserId(1L);
        room.setPlayerIds(new HashSet<>(Set.of(1L, 5L)));
        room.setGameDifficulty(GameDifficulty.EASY);
        room.setGameLanguage(GameLanguage.PYTHON);
        room.setGameMode(GameMode.RACE);

        User joiningUser = new User();
        joiningUser.setId(5L);
        joiningUser.setToken("validToken");
        
        Mockito.when(roomService.joinRoom(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(room);
        
        MockHttpServletRequestBuilder postRequest = post("/rooms/{roomId}/players", room.getRoomId())
                .header("token", joiningUser.getToken())
                .header("userId", joiningUser.getId())
                .header("roomJoinCode", room.getRoomJoinCode())
                .contentType("application/json");
        
        mockMvc.perform(postRequest)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.roomId", is(room.getRoomId().intValue())))
                .andExpect(jsonPath("$.roomJoinCode", is(room.getRoomJoinCode())))
                .andExpect(jsonPath("$.maxNumPlayers", is(room.getMaxNumPlayers())))
                .andExpect(jsonPath("$.currentNumPlayers", is(room.getCurrentNumPlayers())))
                .andExpect(jsonPath("$.isRoomOpen", is(room.isRoomOpen())))
                .andExpect(jsonPath("$.hostUserId", is(room.getHostUserId().intValue())))
                .andExpect(jsonPath("$.playerIds", hasSize(2)))
                .andExpect(jsonPath("$.gameDifficulty", is(room.getGameDifficulty().toString())))
                .andExpect(jsonPath("$.gameLanguage", is(room.getGameLanguage().toString())))
                .andExpect(jsonPath("$.gameMode", is(room.getGameMode().toString())))
                .andExpect(jsonPath("$.maxSkips").isEmpty())
                .andExpect(jsonPath("$.timeLimitSeconds").isEmpty())
                .andExpect(jsonPath("$.numOfProblems").isEmpty());
        }

        // Join Room Fail 404
        @Test
        void failedJoinRoom_roomDoesNotExist() throws Exception {
        
        Long notExistingRoomId = 7L; //arbitrary roomId simulating a non-existing one

        String errorReason = "Room was not found!";
	given(roomService.joinRoom(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any())).willThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, errorReason));
        
        MockHttpServletRequestBuilder postRequest = post("/rooms/{roomId}/players", notExistingRoomId)
                .header("token", "validToken")
                .header("userId", "1")
                .header("roomJoinCode", "ABC456")
                .contentType("application/json");

        mockMvc.perform(postRequest)
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail", is(errorReason)));
        }

        // Join Room Fail 403
        @Test
        void failedJoinRoom_invalidRoomJoinCode() throws Exception {
        
        Room room = new Room();
        room.setRoomId(2L);
        room.setRoomJoinCode("FCZ123");

        String invalidRoomJoinCode = "GCZ123"; //invalid roomJoinCode

        String errorReason = "Insufficient permission to join room!";
	given(roomService.joinRoom(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any())).willThrow(new ResponseStatusException(HttpStatus.FORBIDDEN, errorReason));
        
        MockHttpServletRequestBuilder postRequest = post("/rooms/{roomId}/players", room.getRoomId())
                .header("token", "validToken")
                .header("userId", "1")
                .header("roomJoinCode", invalidRoomJoinCode)
                .contentType("application/json");

        mockMvc.perform(postRequest)
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.detail", is(errorReason)));
        }

         // Join Room Fail 409
        @Test
        void failedJoinRoom_roomIsNotOpenToJoin() throws Exception {
        
        Room room = new Room();
        room.setRoomId(2L);
        room.setRoomJoinCode("ABC170");
        room.setRoomOpen(false); //room is closed

        String errorReason = "Cannot join room: already full!";
	given(roomService.joinRoom(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any())).willThrow(new ResponseStatusException(HttpStatus.CONFLICT, errorReason));
        
        MockHttpServletRequestBuilder postRequest = post("/rooms/{roomId}/players", room.getRoomId())
                .header("token", "validToken")
                .header("userId", "1")
                .header("roomJoinCode", "ABC170")
                .contentType("application/json");

        mockMvc.perform(postRequest)
                .andDo(print())
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail", is(errorReason)));
        }

        // Get Room Details Success 200
        @Test
        void getRoomDetails_validInput_success() throws Exception {
                long roomId = 1L;
                Long userId = 1L;
                String token = "valid_token";

                Room room = new Room();
                room.setRoomId(roomId);
                room.setRoomJoinCode("ABC123");
                room.setMaxNumPlayers(2);
                room.setCurrentNumPlayers(1);
                room.setRoomOpen(true);
                room.setHostUserId(userId);
                room.setPlayerIds(new HashSet<>(Set.of(1L)));
                room.setGameDifficulty(GameDifficulty.EASY);
                room.setGameLanguage(GameLanguage.PYTHON);
                room.setGameMode(GameMode.RACE);
                room.setMaxSkips(3);
                room.setTimeLimitSeconds(120);
                room.setNumOfProblems(5);

                Mockito.when(roomService.getRoomDetails(roomId, userId, token)).thenReturn(room);

                MockHttpServletRequestBuilder getRequest = get("/rooms/{roomId}", roomId)
                        .header("token", token)
                        .header("userId", userId);

                mockMvc.perform(getRequest)
                        .andExpect(status().isOk())
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
                        .andExpect(jsonPath("$.timeLimitSeconds", is(120)))
                        .andExpect(jsonPath("$.numOfProblems", is(5)));
        }

        // Get Room Details user not in room 403
        @Test
        void getRoomDetails_userNotInRoom_forbidden() throws Exception {
                long roomId = 1L;
                Long userId = 2L; // user not in room
                String token = "valid_token";

                Mockito.doThrow(new ResponseStatusException(HttpStatus.FORBIDDEN, "User does not have permission to access room details"))
                        .when(roomService).getRoomDetails(roomId, userId, token);

                MockHttpServletRequestBuilder getRequest = get("/rooms/{roomId}", roomId)
                        .header("token", token)
                        .header("userId", userId);

                mockMvc.perform(getRequest)
                        .andExpect(status().isForbidden());
        }

        // Get Room Details room not found 404
        @Test
        void getRoomDetails_roomNotFound_notFound() throws Exception {
                long roomId = 8953L; // non-existent room
                Long userId = 1L;
                String token = "valid_token";

                Mockito.doThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Room not found"))
                        .when(roomService).getRoomDetails(roomId, userId, token);

                MockHttpServletRequestBuilder getRequest = get("/rooms/{roomId}", roomId)
                        .header("token", token)
                        .header("userId", userId);

                mockMvc.perform(getRequest)
                        .andExpect(status().isNotFound());
        }

        // Get All Rooms Success 200
        @Test
        void getAllRooms_validInput_success() throws Exception {
                Long userId = 1L;
                String token = "valid_token";

                Room room1 = new Room();
                room1.setRoomId(1L);
                room1.setRoomJoinCode("ABC123");
                room1.setMaxNumPlayers(2);
                room1.setCurrentNumPlayers(1);
                room1.setRoomOpen(true);
                room1.setGameDifficulty(GameDifficulty.EASY);
                room1.setGameLanguage(GameLanguage.PYTHON);
                room1.setGameMode(GameMode.RACE);
                room1.setMaxSkips(3);
                room1.setTimeLimitSeconds(120);
                room1.setNumOfProblems(5);


                Room room2 = new Room();
                room2.setRoomId(2L);
                room2.setRoomJoinCode("XYZ789");
                room2.setMaxNumPlayers(6);
                room2.setCurrentNumPlayers(4);
                room2.setRoomOpen(true);
                room2.setGameDifficulty(GameDifficulty.EASY);
                room2.setGameLanguage(GameLanguage.PYTHON);
                room2.setGameMode(GameMode.RACE);
                room2.setMaxSkips(3);
                room2.setTimeLimitSeconds(180);
                room2.setNumOfProblems(8);


                List<Room> allRooms = List.of(room1, room2);
                Mockito.when(roomService.getAllRooms(userId, token)).thenReturn(allRooms);

                MockHttpServletRequestBuilder getRequest = get("/rooms")
                        .header("token", token)
                        .header("userId", userId);

                mockMvc.perform(getRequest)
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$", hasSize(2)))
                        .andExpect(jsonPath("$[0].roomId", is(1)))
                        .andExpect(jsonPath("$[0].roomJoinCode", is("ABC123")))
                        .andExpect(jsonPath("$[0].maxNumPlayers", is(2)))
                        .andExpect(jsonPath("$[0].currentNumPlayers", is(1)))
                        .andExpect(jsonPath("$[0].isRoomOpen", is(true)))
                        .andExpect(jsonPath("$[0].gameDifficulty", is(room1.getGameDifficulty().toString())))
                        .andExpect(jsonPath("$[0].gameLanguage", is(room1.getGameLanguage().toString())))
                        .andExpect(jsonPath("$[0].gameMode", is(room1.getGameMode().toString())))
                        .andExpect(jsonPath("$[0].maxSkips", is(3)))
                        .andExpect(jsonPath("$[0].timeLimitSeconds", is(120)))
                        .andExpect(jsonPath("$[0].numOfProblems", is(5)))
                        .andExpect(jsonPath("$[1].roomId", is(2)))
                        .andExpect(jsonPath("$[1].roomJoinCode", is("XYZ789")))
                        .andExpect(jsonPath("$[1].maxNumPlayers", is(6)))
                        .andExpect(jsonPath("$[1].currentNumPlayers", is(4)))
                        .andExpect(jsonPath("$[1].isRoomOpen", is(true)))
                        .andExpect(jsonPath("$[1].gameDifficulty", is(room2.getGameDifficulty().toString())))
                        .andExpect(jsonPath("$[1].gameLanguage", is(room2.getGameLanguage().toString())))
                        .andExpect(jsonPath("$[1].gameMode", is(room2.getGameMode().toString())))
                        .andExpect(jsonPath("$[1].maxSkips", is(3)))
                        .andExpect(jsonPath("$[1].timeLimitSeconds", is(180)))
                        .andExpect(jsonPath("$[1].numOfProblems", is(8)));
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
