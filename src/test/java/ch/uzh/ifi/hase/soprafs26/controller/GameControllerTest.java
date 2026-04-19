package ch.uzh.ifi.hase.soprafs26.controller;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import org.springframework.web.server.ResponseStatusException;

import ch.uzh.ifi.hase.soprafs26.entity.Room;
import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.service.GameService;
import ch.uzh.ifi.hase.soprafs26.service.UserService;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.hamcrest.Matchers.is;


@WebMvcTest(GameController.class)
public class GameControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GameService gameService;

    @MockitoBean
    private UserService userService;

    private User testUser;
    private Room testRoom;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);

        testUser = new User();
        testUser.setId(1L);
        testUser.setToken("validToken");

        testRoom = new Room();
        testRoom.setRoomId(3L);
        testRoom.setHostUserId(testUser.getId());
    }

    // /rooms/{roomId}/games; expect 201 (game created)
    @Test
    void startGame_validRequest_success() throws Exception {
        
        Long roomId = testRoom.getRoomId();
        Long hostId = testRoom.getHostUserId();

        doNothing().when(userService).verifyToken(testUser.getToken());
        doNothing().when(gameService).createGameSession(hostId, roomId);

        MockHttpServletRequestBuilder postRequest = post("/rooms/{roomId}/games", roomId)
                                                    .header("token", testUser.getToken())
                                                    .header("hostId", hostId)
                                                    .contentType(MediaType.APPLICATION_JSON);

        mockMvc.perform(postRequest)
                .andExpect(status().isCreated());
        
        verify(gameService, times(1)).createGameSession(hostId, roomId);

    }

    // /rooms/{roomId}/games; expect 404 (room not found)
    @Test
    void startGame_invalidRoom_notFound() throws Exception {

        Long invalidRoomId = 832L;

        String errorReason = "Room was not found!";
        Mockito.doThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, errorReason)).when(gameService).createGameSession(Mockito.any(), Mockito.any());

        MockHttpServletRequestBuilder postRequest = post("/rooms/{roomId}/games", invalidRoomId)
                                                    .header("token", testUser.getToken())
                                                    .header("hostId", testUser.getId())
                                                    .contentType(MediaType.APPLICATION_JSON);
        mockMvc.perform(postRequest)
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail", is(errorReason)));
            }
    
    // /rooms/{roomId}/games; expect 404 (user not found)
    @Test
    void startGame_invalidUser_notFound() throws Exception {

        Long invalidHostId = 123L;
        Long roomId = testRoom.getRoomId();

        String errorReason = "User was not found!";
        Mockito.doThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, errorReason)).when(gameService).createGameSession(Mockito.any(), Mockito.any());

        MockHttpServletRequestBuilder postRequest = post("/rooms/{roomId}/games", roomId)
                                                    .header("token", testUser.getToken())
                                                    .header("hostId", invalidHostId)
                                                    .contentType(MediaType.APPLICATION_JSON);
        mockMvc.perform(postRequest)
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail", is(errorReason)));
            }

    // /rooms/{roomId}/games; expect 403 (non-host tries to create room)
    @Test
    void startGame_invalidHostId_Forbidden() throws Exception {

        Long userId = 4L; //not host tries to start the game
        Long roomId = testRoom.getRoomId();

        String errorReason = "User not allowed to start the game!";
        Mockito.doThrow(new ResponseStatusException(HttpStatus.FORBIDDEN, errorReason)).when(gameService).createGameSession(Mockito.any(), Mockito.any());

        MockHttpServletRequestBuilder postRequest = post("/rooms/{roomId}/games", roomId)
                                                    .header("token", testUser.getToken())
                                                    .header("hostId", userId)
                                                    .contentType(MediaType.APPLICATION_JSON);
        mockMvc.perform(postRequest)
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.detail", is(errorReason)));
            }
    
    // /rooms/{roomId}/games; expect 409 (not enough players to play)
    @Test
    void startGame_notEnoughPlayersToStart_Conflict() throws Exception {

        Long hostId = testRoom.getHostUserId();
        Long roomId = testRoom.getRoomId();

        String errorReason = "Not enough players to start the game!";
        Mockito.doThrow(new ResponseStatusException(HttpStatus.CONFLICT, errorReason)).when(gameService).createGameSession(Mockito.any(), Mockito.any());

        MockHttpServletRequestBuilder postRequest = post("/rooms/{roomId}/games", roomId)
                                                    .header("token", testUser.getToken())
                                                    .header("hostId", hostId)
                                                    .contentType(MediaType.APPLICATION_JSON);
        mockMvc.perform(postRequest)
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail", is(errorReason)));
            }
}
