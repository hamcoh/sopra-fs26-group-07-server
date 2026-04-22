package ch.uzh.ifi.hase.soprafs26.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.rest.dto.GameEndDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.GamePointsUpdateDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.PlayerGameSummaryDTO;

@ExtendWith(MockitoExtension.class)
public class WsGameServiceTest {

    @Mock
    SimpMessagingTemplate simpMessagingTemplate;

    @Mock
    UserService userService;

    @InjectMocks
    private WsGameService wsGameService;

    @Test
    void notifyPlayerGameEnded_sendsToCorrectTopic() {
        GameEndDTO gameEndDTO = new GameEndDTO();
        gameEndDTO.setGameSessionId(54L);

        wsGameService.notifyPlayerGameEnded(gameEndDTO);

        verify(simpMessagingTemplate, times(1)).convertAndSend(
            "/topic/game/54/end",
            gameEndDTO
        );
    }

    @Test
    void broadcastPointsUpdate_sendsToCorrectTopic() {
        GamePointsUpdateDTO gamePointsUpdateDTO = new GamePointsUpdateDTO();
        gamePointsUpdateDTO.setGameSessionId(10L);
        gamePointsUpdateDTO.setPlayerSessionId(30L);
        gamePointsUpdateDTO.setCurrentScore(20);

        wsGameService.broadcastPointsUpdate(gamePointsUpdateDTO);

        verify(simpMessagingTemplate, times(1)).convertAndSend(
            "/topic/game/10/points-update",
            gamePointsUpdateDTO
        );

    }

    @Test 
    void broadcastPointsUpdate_sendsCorrectScoreAndCorrectPlayerSessionId () {
        GamePointsUpdateDTO gamePointsUpdateDTO = new GamePointsUpdateDTO();
        gamePointsUpdateDTO.setGameSessionId(10L);
        gamePointsUpdateDTO.setPlayerSessionId(30L);
        gamePointsUpdateDTO.setCurrentScore(20);

        wsGameService.broadcastPointsUpdate(gamePointsUpdateDTO);

        ArgumentCaptor<GamePointsUpdateDTO> payloadCaptorGamePointsUpdateDTO = ArgumentCaptor.forClass(GamePointsUpdateDTO.class);
        verify(simpMessagingTemplate).convertAndSend(
            eq("/topic/game/10/points-update"),
            payloadCaptorGamePointsUpdateDTO.capture()
        );

        GamePointsUpdateDTO captured = payloadCaptorGamePointsUpdateDTO.getValue();
        assertEquals(10L, captured.getGameSessionId());
        assertEquals(30L, captured.getPlayerSessionId());
        assertEquals(20, captured.getCurrentScore());
    }

    @Test
    void sendPlayerGameSummary_sucess() {

        User testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testUser");

        PlayerGameSummaryDTO playerGameSummaryDTO = new PlayerGameSummaryDTO();

        Mockito.when(userService.getUserById(Mockito.any())).thenReturn(testUser);

        wsGameService.sendPlayerGameSummary(playerGameSummaryDTO);

        verify(simpMessagingTemplate, times(1)).convertAndSendToUser(
            testUser.getUsername(),
            "/queue/game-summary",
            playerGameSummaryDTO
            );
    }
    
}
