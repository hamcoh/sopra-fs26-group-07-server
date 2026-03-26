package ch.uzh.ifi.hase.soprafs26.service;

import java.util.HashSet;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import ch.uzh.ifi.hase.soprafs26.entity.Room;
import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.repository.RoomRepository;
import ch.uzh.ifi.hase.soprafs26.repository.UserRepository;
import jakarta.transaction.Transactional;

@Service
@Transactional
public class RoomService {
    
    private final RoomRepository roomRepository;
    private final UserService userService;

    public RoomService(@Qualifier("roomRepository") RoomRepository roomRepository,
                                                    UserRepository userRepository,
                                                    UserService userService) {
        this.roomRepository = roomRepository;
        this.userService = userService;
    }

    public Room createRoom(Room roomInput, Long userId, String token) {
        userService.verifyTokenAndUserId(token, userId);
        User host = userService.getUserbyId(userId);

        roomInput.setHostUserId(host.getId());
        roomInput.setRoomJoinCode(generateRoomCode());
        roomInput.setRoomOpen(true);
        roomInput.setCurrentNumPlayers(1);

        HashSet<Long> playerIds = new HashSet<>();
        playerIds.add(host.getId());
        roomInput.setPlayerIds(playerIds);

        Room createdRoom = roomRepository.save(roomInput);
        roomRepository.flush();

        return createdRoom;
    }

    private String generateRoomCode() {
        return UUID.randomUUID().toString().substring(0, 6).toUpperCase();
    }
}