package ch.uzh.ifi.hase.soprafs26.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import org.springframework.http.HttpStatus;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import ch.uzh.ifi.hase.soprafs26.constant.GameEndReason;
import ch.uzh.ifi.hase.soprafs26.constant.GameStatus;
import ch.uzh.ifi.hase.soprafs26.constant.PlayerSessionStatus;
import ch.uzh.ifi.hase.soprafs26.entity.GameSession;
import ch.uzh.ifi.hase.soprafs26.entity.PlayerSession;
import ch.uzh.ifi.hase.soprafs26.entity.Problem;
import ch.uzh.ifi.hase.soprafs26.entity.Room;
import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.repository.GameSessionRepository;
import ch.uzh.ifi.hase.soprafs26.repository.RoomRepository;
import ch.uzh.ifi.hase.soprafs26.repository.UserRepository;
import ch.uzh.ifi.hase.soprafs26.rest.dto.GameEndDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.GameRoundDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.PlayerScoreDTO;
import jakarta.transaction.Transactional;

@Service
@Transactional
public class GameService {
    
    private final RoomRepository roomRepository;
    private final UserService userService;
    private final UserRepository userRepository;
    private final ProblemService problemService;
    private final GameSessionRepository gameSessionRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final WsRoomService wsRoomService;
    private final WsGameService wsGameService;

    public GameService(RoomRepository roomRepository, UserService userService,ProblemService problemService, GameSessionRepository gameSessionRepository, UserRepository userRepository, SimpMessagingTemplate messagingTemplate, WsRoomService wsRoomService, WsGameService wsGameService) {
        this.roomRepository = roomRepository;
        this.userService = userService;
        this.problemService = problemService;
        this.gameSessionRepository = gameSessionRepository;
        this.userRepository = userRepository;
        this.messagingTemplate = messagingTemplate;
        this.wsRoomService = wsRoomService;
        this.wsGameService = wsGameService;
    }

    public void createGameSession(Long hostId, Long roomId){

        // 1. validate the request
        Room room = roomRepository.findByRoomId(roomId);
        User user = userRepository.findUserById(hostId);

        if (room == null){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Room was not found!");
        }
        else if (user == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User was not found!"); //maybe integrate it to the one above and return "Resource not found"
        }
        else if (!hostId.equals(room.getHostUserId())){
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User not allowed to start the game!");
        }
        else if (room.getCurrentNumPlayers() < 2){
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Not enough players to start the game!");
        }
        
        // 2. create game session + player sessions + extract problems
        Integer requestedNumOfProblems = room.getNumOfProblems();
        if (requestedNumOfProblems > problemService.getAllProblems().size()) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "More problems requested than available!"); //Internal server error because this check already happens when room created
        }

        //select randomly problems based on requestedNumOfProblems
        //as soon as we have different categories of problem EASY/HARD, PYTHON/JAVA we need to implement a more sophisticated function here
        List<Problem> allProblems = new ArrayList<>(problemService.getAllProblems());
        Collections.shuffle(allProblems);
        List<Problem> selectedRandomProblems = allProblems.stream().limit(requestedNumOfProblems).toList();

        //create GameSession
        GameSession gameSession = new GameSession();
        gameSession.setRoom(room);
        gameSession.setProblems(selectedRandomProblems);
        gameSession.setGameStatus(GameStatus.ACTIVE);
        gameSession.setStartedAt(LocalDateTime.now());

        //create PlayerSessions
        Set<Long> participatingPlayerIds = room.getPlayerIds();

        for(Long participatingPlayerId : participatingPlayerIds){
            PlayerSession playerSession = new PlayerSession();
            User player = userService.getUserById(participatingPlayerId);

            playerSession.setGameSession(gameSession);
            playerSession.setPlayer(player);
            playerSession.setCurrentProblemIndex(0);
            playerSession.setCurrentScore(0);
            playerSession.setNumOfSkippedProblems(0);
            playerSession.setPlayerSessionStatus(PlayerSessionStatus.PLAYING);

            gameSession.getPlayerSessions().add(playerSession);
        }

        gameSession = gameSessionRepository.save(gameSession);
        gameSessionRepository.flush();

        // prepare and send personalised GameRoundDTO via WS
        for(PlayerSession playerSession : gameSession.getPlayerSessions()) {
            
            GameRoundDTO gameRoundDTO = new GameRoundDTO();
            gameRoundDTO.setGameSessionId(playerSession.getGameSession().getGameSessionId());
            gameRoundDTO.setGameStatus(GameStatus.ACTIVE); //set GameStatus.Ended when firing back GameEndDTO via WebSocket
            gameRoundDTO.setPlayerSessionId(playerSession.getPlayerSessionId());
            gameRoundDTO.setPlayerId(playerSession.getPlayer().getId());
            gameRoundDTO.setCurrentScore(0);
            gameRoundDTO.setNumOfSkippedProblems(0);

            Problem firstProblem = gameSession.getProblems().get(0); // get first problem
            gameRoundDTO.setProblemId(firstProblem.getProblemId());
            gameRoundDTO.setTitle(firstProblem.getTitle());
            gameRoundDTO.setDescription(firstProblem.getDescription());
            gameRoundDTO.setInputFormat(firstProblem.getInputFormat());
            gameRoundDTO.setOutputFormat(firstProblem.getOutputFormat());
            gameRoundDTO.setConstraints(firstProblem.getConstraints());

            //send personalised message to each player
            wsRoomService.notifyPlayerGameStarted(gameRoundDTO);
        }
    }

    public void endGameSession(GameSession gameSession, GameEndReason gameEndReason) {
        gameSession.setGameStatus(GameStatus.ENDED);
        gameSession.setEndedAt(LocalDateTime.now());
        gameSession.setGameEndReason(gameEndReason);
        gameSessionRepository.save(gameSession);
        gameSessionRepository.flush();

        List<PlayerSession> sessions = gameSession.getPlayerSessions();

        List<PlayerScoreDTO> scores = sessions.stream()
            .map(ps -> {
                PlayerScoreDTO dto = new PlayerScoreDTO();
                dto.setPlayerSessionId(ps.getPlayerSessionId());
                dto.setUserId(ps.getPlayer().getId());
                dto.setUsername(ps.getPlayer().getUsername());
                dto.setScore(ps.getCurrentScore());
                dto.setProblemsSolved(ps.getCurrentProblemIndex());
                return dto;
            })
            .sorted(Comparator.comparingInt(PlayerScoreDTO::getScore).reversed())
            .toList();

        PlayerSession winner = sessions.stream()
            .max(Comparator.comparingInt(PlayerSession::getCurrentScore))
            .orElse(null);

        // null if tie 
        
        long topScore = winner != null ? winner.getCurrentScore() : 0;
        boolean tie = sessions.stream().filter(ps -> ps.getCurrentScore() == topScore).count() > 1;

        GameEndDTO gameEndDTO = new GameEndDTO();
        gameEndDTO.setGameSessionId(gameSession.getGameSessionId());
        gameEndDTO.setGameStatus(GameStatus.ENDED);
        gameEndDTO.setGameEndReason(gameEndReason);
        gameEndDTO.setWinnerPlayerId((!tie && winner != null) ? winner.getPlayer().getId() : null);
        gameEndDTO.setPlayerScores(scores);

        //fire room-wide game-end msg
        wsGameService.notifyPlayerGameEnded(gameEndDTO);
    }
}
