package ch.uzh.ifi.hase.soprafs26.service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import ch.uzh.ifi.hase.soprafs26.constant.GameDifficulty;
import ch.uzh.ifi.hase.soprafs26.constant.GameLanguage;
import ch.uzh.ifi.hase.soprafs26.constant.GameMode;
import ch.uzh.ifi.hase.soprafs26.constant.GameStatus;
import ch.uzh.ifi.hase.soprafs26.entity.GameSession;
import ch.uzh.ifi.hase.soprafs26.entity.Problem;
import ch.uzh.ifi.hase.soprafs26.entity.Room;
import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.repository.GameSessionRepository;
import ch.uzh.ifi.hase.soprafs26.repository.RoomRepository;
import ch.uzh.ifi.hase.soprafs26.repository.UserRepository;

public class GameServiceTest {

    @Mock
    private RoomRepository roomRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserService userService;

    @Mock
    private ProblemService problemService;

    @Mock
    private GameSessionRepository gameSessionRepository;

    @InjectMocks
    private GameService gameService;

    private Room testRoom;
    private User gameHost;
    private User player2;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);

        gameHost = new User();
        gameHost.setId(2L);

        player2 = new User();
        player2.setId(3L);

        testRoom = new Room();
        testRoom.setRoomId(8L);
        testRoom.setRoomJoinCode("ABC123");
        testRoom.setMaxNumPlayers(2);
        testRoom.setRoomOpen(false);
        testRoom.setCurrentNumPlayers(2);
        testRoom.setHostUserId(gameHost.getId());
        Set<Long> playerIds = new HashSet<>();
        playerIds.add(gameHost.getId()); 
        playerIds.add(player2.getId());
        testRoom.setPlayerIds(playerIds);
        testRoom.setGameDifficulty(GameDifficulty.EASY);
        testRoom.setGameLanguage(GameLanguage.PYTHON);
        testRoom.setGameMode(GameMode.RACE);
    }

    //createGameSession success; IMPORTANT: since this method fires back a personalised WS message the WS-part is tested separately
    //this mainly tests whether a GameSession object is created succesfully
    @Test
    void createGameSession_validInputs_success() {
        Problem p1 = new Problem();
        p1.setProblemId(1L);
        Problem p3 = new Problem();
        p3.setProblemId(3L);
        Problem p7 = new Problem();
        p7.setProblemId(7L);

        testRoom.setNumOfProblems(3);
        
        given(roomRepository.findByRoomId(testRoom.getRoomId())).willReturn(testRoom);
        given(userRepository.findUserById(gameHost.getId())).willReturn(gameHost);
        given(problemService.getAllProblems()).willReturn(List.of(p1, p3, p7));
        given(userService.getUserById(gameHost.getId())).willReturn(gameHost);
        given(userService.getUserById(player2.getId())).willReturn(player2);
        given(gameSessionRepository.save(any(GameSession.class)))
            .willAnswer(invocation -> invocation.getArgument(0));

        gameService.createGameSession(gameHost.getId(), testRoom.getRoomId());

        // let's us set an object that is able to capture the created GameSession in createGameSession
        ArgumentCaptor<GameSession> captor = ArgumentCaptor.forClass(GameSession.class);
        verify(gameSessionRepository).save(captor.capture()); //capture the GameSession object
        verify(gameSessionRepository).flush();

        // inspect the captured value
        GameSession savedGameSession = captor.getValue();
        assertEquals(GameStatus.ACTIVE, savedGameSession.getGameStatus());
        assertEquals(testRoom, savedGameSession.getRoom());
        assertEquals(3, savedGameSession.getProblems().size());
        assertEquals(2, savedGameSession.getPlayerSessions().size());
        assertNotNull(savedGameSession.getStartedAt());
    }

    //createGameSession fail: room not found
    @Test
    void createGameSession_roomNotFound_throwsNotFound() {
        given(roomRepository.findByRoomId(testRoom.getRoomId())).willReturn(null);

        assertThrows(ResponseStatusException.class, () ->
                gameService.createGameSession(gameHost.getId(), testRoom.getRoomId()));
    }

    //createGameSession fail: user not found
    @Test
    void createGameSession_userNotFound_throwsNotFound() {
        given(roomRepository.findByRoomId(testRoom.getRoomId())).willReturn(testRoom);
        given(userRepository.findUserById(gameHost.getId())).willReturn(null);

        assertThrows(ResponseStatusException.class, () ->
                gameService.createGameSession(gameHost.getId(), testRoom.getRoomId()));
    }

    //createGameSession fail: non-host tries to start/create game
    @Test
    void createGameSession_invalidHostId_throwsForbidden() {
        given(roomRepository.findByRoomId(testRoom.getRoomId())).willReturn(testRoom);
        given(userRepository.findUserById(gameHost.getId())).willReturn(gameHost);

        assertThrows(ResponseStatusException.class, () ->
                gameService.createGameSession(player2.getId(), testRoom.getRoomId()));
    }

    //createGameSession fail: not enough players to start the game
    @Test
    void createGameSession_notEnoughPlayers_throwsConflict() {
        testRoom.setCurrentNumPlayers(1);
        testRoom.setRoomOpen(true);
        testRoom.setPlayerIds(Set.of(gameHost.getId()));

        given(roomRepository.findByRoomId(testRoom.getRoomId())).willReturn(testRoom);
        given(userRepository.findUserById(gameHost.getId())).willReturn(gameHost);

        assertThrows(ResponseStatusException.class, () ->
                gameService.createGameSession(gameHost.getId(), testRoom.getRoomId()));
    }


    //createGameSession fail: requested more problems than available (throws InternalServerError because this is the second-check of the same thing, i.e., very defensive coding)
    @Test
    void createGameSession_moreProblemsRequestedThenAvailable_throwsInternalServerErrror() {
        testRoom.setNumOfProblems(5432);

        given(roomRepository.findByRoomId(testRoom.getRoomId())).willReturn(testRoom);
        given(userRepository.findUserById(gameHost.getId())).willReturn(gameHost);
        given(problemService.getAllProblems()).willReturn(List.of(new Problem()));

        assertThrows(ResponseStatusException.class, () ->
                gameService.createGameSession(gameHost.getId(), testRoom.getRoomId()));
    }
}
