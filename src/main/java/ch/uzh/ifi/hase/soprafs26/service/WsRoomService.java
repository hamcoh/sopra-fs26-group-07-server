package ch.uzh.ifi.hase.soprafs26.service;

import java.util.Map;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class WsRoomService {

    private final SimpMessagingTemplate simpMessagingTemplate;

    public WsRoomService(SimpMessagingTemplate simpMessagingTemplate) {
        this.simpMessagingTemplate = simpMessagingTemplate;
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
}