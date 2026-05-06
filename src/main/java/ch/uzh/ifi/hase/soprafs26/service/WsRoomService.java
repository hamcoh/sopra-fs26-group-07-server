package ch.uzh.ifi.hase.soprafs26.service;

import java.time.LocalDateTime;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import ch.uzh.ifi.hase.soprafs26.entity.Room;
import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.repository.RoomRepository;
import ch.uzh.ifi.hase.soprafs26.repository.UserRepository;
import ch.uzh.ifi.hase.soprafs26.rest.dto.GameRoundDTO;
import ch.uzh.ifi.hase.soprafs26.websocket.dto.RoomChatMessageDTO;
import jakarta.transaction.Transactional;

@Service
public class WsRoomService {

    private final SimpMessagingTemplate simpMessagingTemplate;
    private final UserService userService;
    private final UserRepository userRepository;
    private final RoomRepository roomRepository;
    private final Logger log = LoggerFactory.getLogger(WsRoomService.class);

    public WsRoomService(SimpMessagingTemplate simpMessagingTemplate, UserService userService, UserRepository userRepository, RoomRepository roomRepository) {
        this.simpMessagingTemplate = simpMessagingTemplate;
        this.userService = userService;
        this.userRepository = userRepository;
        this.roomRepository = roomRepository;
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
        
        log.info("Sending personalised GameRoundDTO to: {}", player.getUsername());
        simpMessagingTemplate.convertAndSendToUser( //enables sending personalised messages (≠ room-wide broadcast)
            player.getUsername(), //username maps to a session, as configured in WsAuthChannelInterceptor.java
            "/queue/game-start", //looks like 'user/queue/game-start' and is resolved dynamically to the actual user
            gameRoundDTO
        );
        log.info("GameRoundDTO sent to: {}", player.getUsername());
    }

    public void notifyRoomPlayerLeft(User user, Long roomId, Boolean isHost){
        String username = user.getUsername();

        log.info("User=" + username + " is leaving room=" + roomId);
        log.info("User=" + username + " isHost=" + isHost);

        Map<String, String> notification = Map.of(
            "type", isHost ? "ROOM_CLOSED" : "PLAYER_LEFT",
            "roomId", roomId.toString(),
            "username", username,
            "message", username + (isHost ? " closed the room. Room was deleted." : " left the room. Room persists.")
        );
        
        log.info("Sending room-wide info to roomId=" + roomId);
        simpMessagingTemplate.convertAndSend("/topic/room/" + roomId, notification);
    }

    @Transactional
    public void sendRoomChatMessage(Long roomId, RoomChatMessageDTO roomChatMessageDTO) {
        log.info("Received message with content: " + roomChatMessageDTO.getContent() + " from: " + roomChatMessageDTO.getSenderUsername());
        validateRoomChatMessage(roomId, roomChatMessageDTO);

        roomChatMessageDTO.setTimestamp(LocalDateTime.now());

        simpMessagingTemplate.convertAndSend("/topic/chat/room/" + roomId, roomChatMessageDTO);
        log.info("Received message was sent back to roomId=" + roomId);

    }

    private void validateRoomChatMessage(Long roomId, RoomChatMessageDTO roomChatMessageDTO){

        Room room = roomRepository.findByRoomId(roomId);
        if (room == null) {
            throw new IllegalArgumentException("Invalid roomId!");
        }

        User sender = userRepository.findByUsername(roomChatMessageDTO.getSenderUsername());
        if (sender == null) {
            throw new IllegalArgumentException("Sender with given username not found!");
        }
        else if (!room.getPlayerIds().contains(sender.getId())) {
            throw new IllegalArgumentException("Sender not allowed to send messages in this room!");
        }

        String message = roomChatMessageDTO.getContent();
        if(message == null || message.isBlank()){
			throw new IllegalArgumentException("Message is invalid: Messages cannot be empty or contain only spaces!");
		}
		else if (message.length() > 255) {
			throw new IllegalArgumentException("Message is invalid: Messages cannot exceed 255 characters!");
		}
    }
}