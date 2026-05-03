package ch.uzh.ifi.hase.soprafs26.service;

import java.util.HashSet;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import ch.uzh.ifi.hase.soprafs26.entity.Room;
import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.repository.ProblemRepository;
import ch.uzh.ifi.hase.soprafs26.repository.RoomRepository;
import ch.uzh.ifi.hase.soprafs26.repository.UserRepository;
import jakarta.transaction.Transactional;

@Service
@Transactional
public class RoomService {
    
    private final RoomRepository roomRepository;
    private final UserService userService;
    private final ProblemService problemService;
    private final ProblemRepository problemRepository;

    public RoomService(@Qualifier("roomRepository") RoomRepository roomRepository,
                                                    UserRepository userRepository,
                                                    UserService userService,
                                                    ProblemService problemService,
                                                    ProblemRepository problemRepository) {
        this.roomRepository = roomRepository;
        this.userService = userService;
        this.problemService = problemService;
        this.problemRepository = problemRepository;
    }

    public Room createRoom(Room roomInput, Long userId, String token) {
        userService.verifyTokenAndUserId(token, userId);
        User host = userService.getUserbyId(userId);

        roomInput.setHostUserId(host.getId());
        roomInput.setRoomJoinCode(generateRoomCode());
        roomInput.setRoomOpen(true);
        roomInput.setCurrentNumPlayers(1);
        roomInput.setMaxNumPlayers(2); //In the beginning, only allow 1v1 (later stage: no hard-coding, send field from frontend)
        
        Integer requestNumOfProblems = roomInput.getNumOfProblems();
        if (requestNumOfProblems != null){
            Integer storedNumOfProblems = problemRepository.findAllByGameLanguageAndGameDifficulty(roomInput.getGameLanguage(), roomInput.getGameDifficulty()).size();
            if(requestNumOfProblems > storedNumOfProblems ) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot create room: number of problems requested (" + requestNumOfProblems + ") for specific language/difficulty combination exceeds amount of problems available (" + storedNumOfProblems + ") !");
            }
            roomInput.setNumOfProblems(requestNumOfProblems);
        }
       
        // roomInput.setMaxSkips(2); // arbitrary
        // roomInput.setTimeLimitSeconds(600); //10 mins

        HashSet<Long> playerIds = new HashSet<>();
        playerIds.add(host.getId());
        roomInput.setPlayerIds(playerIds);

        Room createdRoom = roomRepository.save(roomInput);
        roomRepository.flush();

        return createdRoom;
    }

    public Room joinRoom(String roomJoinCode, Long userId, String token) {
        userService.verifyTokenAndUserId(token, userId);
        User newPlayer = userService.getUserbyId(userId);

        if (roomJoinCode != null) {
            if (!roomJoinCode.matches("[A-F0-9]{6}")) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid code!");
            }
        }

        Room targetRoom = roomRepository.findByRoomJoinCode(roomJoinCode);

        if (targetRoom == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Room was not found!");
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