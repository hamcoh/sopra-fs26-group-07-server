package ch.uzh.ifi.hase.soprafs26.entity;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import ch.uzh.ifi.hase.soprafs26.constant.GameEndReason;
import ch.uzh.ifi.hase.soprafs26.constant.GameStatus;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;


@Entity
@Table(name = "game_sessions")
public class GameSession implements Serializable {
    
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue
    private Long gameSessionId;

    @OneToOne //1-1 relationship, maybe add CascadeType.ALL if room should be deleted when GameSession is deleted
    @JoinColumn(name = "room_Id", referencedColumnName = "roomId", nullable = false) //room_Id is the FK created in 'game_session' and references the roomId column of rooms (referencedColumnName has to be identical to field-name in entity or JPA complains)
    private Room room;

    //creates a join table, since a GameSession can have many problems and a problem can be in many GameSessions
    @ManyToMany
    @JoinTable(
    name = "game_session_problems",
    joinColumns = @JoinColumn(name = "game_session_id"), //owns association
    inverseJoinColumns = @JoinColumn(name = "problem_id"))
    private List<Problem> problems = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private GameStatus gameStatus;

    @Column(nullable = false, updatable = false)
    private LocalDateTime startedAt; //CAREFUL WITH DATES!

    @Column(nullable = true)
    private LocalDateTime endedAt; //CAREFUL WITH DATES!

    @Enumerated(EnumType.STRING)
    @Column(nullable = true)
    private GameEndReason gameEndReason;

    // One gameSession is related to Many playerSessions (i.e., 2 atm)
    // Since PlayerSession do not have any meaning without a GameSession implement CascadeType.ALL
    @OneToMany(mappedBy = "gameSession", fetch = FetchType.EAGER, cascade = CascadeType.ALL) //mappedBy is FK in the PlayerSession class and is identical to field name; FetchType.Eager, as there is not much playerSession-data to load (atm only two PlayerSession per game), hence, shouldn't cause affect performance
    private List<PlayerSession> playerSessions = new ArrayList<>();

    public Long getGameSessionId() {
        return gameSessionId;
    }

    public void setGameSessionId(Long gameSessionId) {
        this.gameSessionId = gameSessionId;
    }

    public Room getRoom() {
        return room;
    }

    public void setRoom(Room room) {
        this.room = room;
    }

    public GameStatus getGameStatus() {
        return gameStatus;
    }

    public List<Problem> getProblems() {
        return problems;
    }

    public void setProblems(List<Problem> problems) {
        this.problems = problems;
    }

    public void setGameStatus(GameStatus gameStatus) {
        this.gameStatus = gameStatus;
    }

    public LocalDateTime getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(LocalDateTime startedAt) {
        this.startedAt = startedAt;
    }

    public LocalDateTime getEndedAt() {
        return endedAt;
    }

    public void setEndedAt(LocalDateTime endedAt) {
        this.endedAt = endedAt;
    }

    public GameEndReason getGameEndReason() {
        return gameEndReason;
    }

    public void setGameEndReason(GameEndReason gameEndReason) {
        this.gameEndReason = gameEndReason;
    }

    public List<PlayerSession> getPlayerSessions() {
        return playerSessions;
    }

    public void setPlayerSessions(List<PlayerSession> playerSessions) {
        this.playerSessions = playerSessions;
    }
}
