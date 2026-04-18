package ch.uzh.ifi.hase.soprafs26.service;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.rest.dto.GameRoundDTO;

@Service
public class WsRoomService {

    private final SimpMessagingTemplate simpMessagingTemplate;
    private final UserService userService;
    private final Logger log = LoggerFactory.getLogger(UserService.class);

    public WsRoomService(SimpMessagingTemplate simpMessagingTemplate, UserService userService) {
        this.simpMessagingTemplate = simpMessagingTemplate;
        this.userService = userService;
    }

    public void notifyPlayerJoinedRoom(Long roomId, String username, boolean isHost) {

        Map<String, String> notification = Map.of(
            "type", isHost ? "HOST_CREATED" : "PLAYER_JOINED", //differentiate between host and player
            "roomId", roomId.toString(),
            "username", username,
            "message", username + (isHost ? " created the room" : " joined the room!") //differentiate again
        );

        simpMessagingTemplate.convertAndSend("/topic/room/" + roomId, notification); //message is sent back to destination '/topic/rooms/{roomId}' (all players get notifications that subscribed)
    }

    public void notifyPlayerGameStarted(GameRoundDTO gameRoundDTO) {
        User player = userService.getUserById(gameRoundDTO.getPlayerId());
        log.info("Sending DTO to: {}", player.getUsername());
        simpMessagingTemplate.convertAndSendToUser( //enables sending personalised messages (≠ room-wide broadcast)
            player.getUsername(), //username maps to a session, as configured in WsAuthChannelInterceptor.java
            "/queue/game-start", //looks like 'user/queue/game-start' and is resolved dynamically to the actual user
            gameRoundDTO
        );
        log.info("Sent to: {}", player.getUsername());
    }
}