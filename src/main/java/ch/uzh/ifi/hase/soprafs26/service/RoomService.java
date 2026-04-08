package ch.uzh.ifi.hase.soprafs26.service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

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
        roomInput.setMaxNumPlayers(2); //In the beginning, only allow 1v1 (later stage: no hard-coding, send field from frontend)
        // roomInput.setNumOfProblems(10); //all problems so far
        // roomInput.setMaxSkips(2); // arbitrary
        // roomInput.setTimeLimitSeconds(600); //10 mins

        HashSet<Long> playerIds = new HashSet<>();
        playerIds.add(host.getId());
        roomInput.setPlayerIds(playerIds);

        Room createdRoom = roomRepository.save(roomInput);
        roomRepository.flush();

        return createdRoom;
    }

    public Room joinRoom(Long roomId, String roomJoinCode, Long userId, String token) {
        userService.verifyTokenAndUserId(token, userId);
        User newPlayer = userService.getUserbyId(userId);

        Room targetRoom = roomRepository.findByRoomId(roomId);

        if (targetRoom == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Room was not found!");
        }
        else if (!targetRoom.getRoomJoinCode().equals(roomJoinCode)){
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Insufficient permission to join room!");
        }
        else if(!targetRoom.isRoomOpen()){
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Cannot join room: already full!");
        }
        else if(targetRoom.getCurrentNumPlayers() != 1 || targetRoom.getPlayerIds().size() != 1) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Room is in an invalid state: expected exactly 1 player (=host) before joining.");
        }

        targetRoom.getPlayerIds().add(newPlayer.getId());
        targetRoom.setCurrentNumPlayers(targetRoom.getPlayerIds().size());
        targetRoom.setRoomOpen(false);

        roomRepository.save(targetRoom);
        roomRepository.flush();

        return targetRoom;
    }

    public Room getRoomDetails(Long roomId, Long userId, String token) {
        userService.verifyTokenAndUserId(token, userId);
        Room room = roomRepository.findByRoomId(roomId);

        if (room == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Room was not found!");
        }
        else if (!room.getPlayerIds().contains(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Player must be in Lobby to have access to room details!");
        }

        return room;
    }

    public List<Room> getAllRooms(Long userId, String token) {
        userService.verifyTokenAndUserId(token, userId);

        List<Room> allRooms = roomRepository.findAll();

        return allRooms.stream()
                       .filter(Room::isRoomOpen)
                       .toList();   
    }

    private String generateRoomCode() {
        return UUID.randomUUID().toString().substring(0, 6).toUpperCase();
    }
}