package ch.uzh.ifi.hase.soprafs26.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import ch.uzh.ifi.hase.soprafs26.constant.GameDifficulty;
import ch.uzh.ifi.hase.soprafs26.constant.GameLanguage;
import ch.uzh.ifi.hase.soprafs26.constant.GameMode;
import ch.uzh.ifi.hase.soprafs26.constant.GameStatus;
import ch.uzh.ifi.hase.soprafs26.constant.PlayerSessionStatus;
import ch.uzh.ifi.hase.soprafs26.constant.UserStatus;
import ch.uzh.ifi.hase.soprafs26.entity.GameSession;
import ch.uzh.ifi.hase.soprafs26.entity.PlayerSession;
import ch.uzh.ifi.hase.soprafs26.entity.Room;
import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.service.ProblemService;
import jakarta.transaction.Transactional;

@SpringBootTest //not merely a DataJpaTest because we need the ProblemService to get a problem
@Transactional
public class PlayerSessionIntegrationTest {
    
    @Autowired
    private PlayerSessionRepository playerSessionRepository;

    //since gameSession owns the PlayerSessions, we need it here, since PlayerSessions are created when GameSession is created (cascading)
    @Autowired
    private GameSessionRepository gameSessionRepository;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProblemService problemService;

    @Test
	void findByPlayerSessionId_success() {
        GameSession gameSession = new GameSession();
        PlayerSession playerSession = new PlayerSession();

        Room room = new Room();
        room.setRoomJoinCode("ABC123");
        room.setMaxNumPlayers(2);
        room.setCurrentNumPlayers(2);
        room.setRoomOpen(false);
        room.setHostUserId(1L);
        room.setGameDifficulty(GameDifficulty.EASY);
        room.setGameLanguage(GameLanguage.JAVA);
        room.setGameMode(GameMode.RACE);

        roomRepository.save(room);
        roomRepository.flush();

        User user = new User();
        user.setUsername("testUser");
        user.setPassword("testPw");
        user.setToken("validToken");
        user.setStatus(UserStatus.ONLINE);
        user.setWinCount(10);
        user.setWinRatePercentage(75.0);
        user.setTotalGamesPlayed(20);
        user.setTotalPoints(3500L);
        
        userRepository.save(user);
        userRepository.flush();
        
        
        //set up gameSession
        gameSession.setRoom(room);
        gameSession.setProblems(List.of(problemService.getProblemById(1L)));
        gameSession.setGameStatus(GameStatus.ACTIVE);
        gameSession.setStartedAt(LocalDateTime.now());

        //set up playerSession
        playerSession.setGameSession(gameSession);
        playerSession.setPlayer(user);
        playerSession.setCurrentProblemIndex(0);
        playerSession.setCurrentScore(0);
        playerSession.setNumOfSkippedProblems(0);
        playerSession.setPlayerSessionStatus(PlayerSessionStatus.PLAYING);

        //add playerSession (it should cascade)
        gameSession.getPlayerSessions().add(playerSession);

        gameSession = gameSessionRepository.save(gameSession);
        gameSessionRepository.flush();

        PlayerSession foundPlayerSession = playerSessionRepository.findByPlayerSessionId(playerSession.getPlayerSessionId());

        assertNotNull(foundPlayerSession.getPlayerSessionId());
        assertEquals(playerSession.getGameSession().getGameSessionId(), foundPlayerSession.getGameSession().getGameSessionId());
        assertEquals(playerSession.getPlayer(), foundPlayerSession.getPlayer());
        assertEquals(playerSession.getCurrentProblemIndex(), foundPlayerSession.getCurrentProblemIndex());
        assertEquals(playerSession.getCurrentScore(), foundPlayerSession.getCurrentScore());
        assertEquals(playerSession.getPlayerSessionStatus(), foundPlayerSession.getPlayerSessionStatus());
    }
}
