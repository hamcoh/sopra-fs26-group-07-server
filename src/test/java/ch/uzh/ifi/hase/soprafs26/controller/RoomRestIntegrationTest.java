package ch.uzh.ifi.hase.soprafs26.controller;

import static org.hamcrest.Matchers.matchesPattern;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;

import ch.uzh.ifi.hase.soprafs26.constant.GameDifficulty;
import ch.uzh.ifi.hase.soprafs26.constant.GameLanguage;
import ch.uzh.ifi.hase.soprafs26.constant.GameMode;
import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.repository.RoomRepository;
import ch.uzh.ifi.hase.soprafs26.repository.UserRepository;
import ch.uzh.ifi.hase.soprafs26.rest.dto.RoomPostDTO;
import ch.uzh.ifi.hase.soprafs26.service.UserService;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class RoomRestIntegrationTest {

    @Autowired private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();
    @Autowired private UserService userService;
    @Autowired private UserRepository userRepository;
    @Autowired private RoomRepository roomRepository;

    @BeforeEach
    void setup() {
        roomRepository.deleteAll();
        userRepository.deleteAll();
    }

    // helper
    private User registeredUser(String username) {
        User u = new User();
        u.setUsername(username);
        u.setPassword("test");
        return userService.createUser(u);
    }

    private String roomBody(GameDifficulty difficulty, GameLanguage language, GameMode mode) throws Exception {
        RoomPostDTO dto = new RoomPostDTO();
        dto.setGameDifficulty(difficulty);
        dto.setGameLanguage(language);
        dto.setGameMode(mode);
        return objectMapper.writeValueAsString(dto);
    }

    @Test
    void createRoom_validRequest_returns201WithJoinCode() throws Exception {

        // given
        User host = registeredUser("Leonidas");

        // when + then, full HTTP to controller to real service to real DB to JSON
        mockMvc.perform(post("/rooms")
                .header("userId", host.getId())
                .header("token", host.getToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(roomBody(GameDifficulty.EASY, GameLanguage.PYTHON, GameMode.RACE)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.roomId").exists())
            .andExpect(jsonPath("$.roomJoinCode", matchesPattern("[A-F0-9]{6}")))
            .andExpect(jsonPath("$.hostUserId").value(host.getId()))
            .andExpect(jsonPath("$.currentNumPlayers").value(1))
            .andExpect(jsonPath("$.maxNumPlayers").value(2))
            .andExpect(jsonPath("$.isRoomOpen").value(true))
            .andExpect(jsonPath("$.gameDifficulty").value("EASY"))
            .andExpect(jsonPath("$.gameLanguage").value("PYTHON"))
            .andExpect(jsonPath("$.gameMode").value("RACE"));
    }

    @Test
    void createRoom_invalidToken_returns401() throws Exception {
        // given
        User host = registeredUser("Spartacus");

        // when + then, wrong token must be rejected 
        mockMvc.perform(post("/rooms")
                .header("userId", host.getId())
                .header("token", "invalidToken")
                .contentType(MediaType.APPLICATION_JSON)
                .content(roomBody(GameDifficulty.EASY, GameLanguage.PYTHON, GameMode.RACE)))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void joinRoom_secondPlayer_returns200AndClosesRoom() throws Exception {
        // given 
        User host = registeredUser("Flamma");
        String createResponse = mockMvc.perform(post("/rooms")
                .header("userId", host.getId())
                .header("token", host.getToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(roomBody(GameDifficulty.EASY, GameLanguage.PYTHON, GameMode.RACE)))
            .andExpect(status().isCreated())
            .andReturn()
            .getResponse()
            .getContentAsString();

        String joinCode = objectMapper.readTree(createResponse).get("roomJoinCode").asText();

        // and a second user
        User guest = registeredUser("Asterix");

        // when + then, guest joins via HTTP and room closes
        mockMvc.perform(post("/rooms/players")
                .header("userId", guest.getId())
                .header("token", guest.getToken())
                .header("roomJoinCode", joinCode))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.isRoomOpen").value(false))
            .andExpect(jsonPath("$.currentNumPlayers").value(2));
    }
}
