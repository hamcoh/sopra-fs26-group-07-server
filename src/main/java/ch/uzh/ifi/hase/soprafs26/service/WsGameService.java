package ch.uzh.ifi.hase.soprafs26.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.rest.dto.GameEndDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.GamePointsUpdateDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.GameTimeWarningDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.PlayerGameSummaryDTO;

@Service
public class WsGameService {

    private final SimpMessagingTemplate simpMessagingTemplate;
    private final Logger log = LoggerFactory.getLogger(WsGameService.class);
    private final UserService userService;

    public WsGameService(SimpMessagingTemplate simpMessagingTemplate, UserService userService) {
        this.simpMessagingTemplate = simpMessagingTemplate;
        this.userService = userService;
    }

    //ATM PLAYERS HAVE TO MAKE FOUR SUBSCRIPTIONS:
    // - 1.: /topic/game/{gameSessionId}/end
    // - 2.: /topic/game/{gameSessionId}/points-update
    // - 3.: /topic/game/{gameSessionId}/time-warning
    // - 4.: /users/queue/game-summary


    public void notifyPlayerGameEnded(GameEndDTO gameEndDTO) {
        Long gameSessionId = gameEndDTO.getGameSessionId();
        log.info("Game with gameSessionId=" +  gameSessionId + " is over!");
        log.info("Sending gameEndDTO to all Player in gameSessionID={}", gameSessionId);
        simpMessagingTemplate.convertAndSend( 
            "/topic/game/" + gameSessionId + "/end", 
            gameEndDTO
        );
        log.info("gameEndDTO was sent");
    }

    public void broadcastPointsUpdate(GamePointsUpdateDTO gamePointsUpdateDTO){
        Long gameSessionId = gamePointsUpdateDTO.getGameSessionId();
        Long playerSessionId = gamePointsUpdateDTO.getPlayerSessionId();
        simpMessagingTemplate.convertAndSend( 
            "/topic/game/" + gameSessionId + "/points-update", 
            gamePointsUpdateDTO
        );
        log.info("Sent points update to gameSession with gameSessionId=" + gameSessionId + " regarding playerSessionId=" + playerSessionId);
        log.info("New current points: " + gamePointsUpdateDTO.getCurrentScore() + " of playerSessionId=" + playerSessionId);
    }

    public void notifyPlayerGameTimeWarning(GameTimeWarningDTO gameTimeWarningDTO) {
        Long gameSessionid = gameTimeWarningDTO.getGameSessionId();
        simpMessagingTemplate.convertAndSend(
            "/topic/game/" + gameSessionid + "/time-warning",gameTimeWarningDTO
        );
        log.info("Sent time warning ({}s remaining) to gameSessionId={}",
            gameTimeWarningDTO.getRemainingTimeSeconds(), gameSessionid
        );
    }

    public void sendPlayerGameSummary(PlayerGameSummaryDTO playerGameSummaryDTO) {
        
        User player = userService.getUserById(playerGameSummaryDTO.getPlayerId());
        log.info("Sending personalised playerGameSummaryDTO to: {}", player.getUsername());
        simpMessagingTemplate.convertAndSendToUser( 
            player.getUsername(), 
            "/queue/game-summary", 
            playerGameSummaryDTO
        );
        log.info("playerGameSummaryDTO sent to: {}", player.getUsername());
    }
}
