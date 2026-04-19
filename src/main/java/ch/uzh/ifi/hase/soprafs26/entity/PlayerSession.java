package ch.uzh.ifi.hase.soprafs26.entity;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import ch.uzh.ifi.hase.soprafs26.constant.PlayerSessionStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "player_sessions")
public class PlayerSession implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue
    private Long playerSessionId;

    @ManyToOne(fetch = FetchType.EAGER) //ok for the moment
    @JoinColumn(name = "game_session_id", referencedColumnName = "gameSessionId", nullable = false) //references the PK of GameSession
    private GameSession gameSession;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "player_id", referencedColumnName = "id", nullable = false)
    private User player;

    @Column(nullable = false)
    private int currentProblemIndex;

    @Column(nullable = false)
    private int currentScore;

    @Column(nullable = true) //atm not relevant
    private int numOfSkippedProblems;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PlayerSessionStatus playerSessionStatus;

    @Column(nullable = true)
    private LocalDateTime finishedAt;

    @OneToMany //this is not the best solution i think
    @JoinTable(
    name = "player_session_submissions",
    joinColumns = @JoinColumn(name = "player_session_id"), //owns association
    inverseJoinColumns = @JoinColumn(name = "submission_id"))
    private List<Submission> submissions = new ArrayList<>();

    public Long getPlayerSessionId() {
        return playerSessionId;
    }

    public void setPlayerSessionId(Long playerSessionId) {
        this.playerSessionId = playerSessionId;
    }

    public GameSession getGameSession() {
        return gameSession;
    }

    public void setGameSession(GameSession gameSession) {
        this.gameSession = gameSession;
    }

    public User getPlayer() {
        return player;
    }

    public void setPlayer(User player) {
        this.player = player;
    }

    public int getCurrentProblemIndex() {
        return currentProblemIndex;
    }

    public void setCurrentProblemIndex(int currentProblemIndex) {
        this.currentProblemIndex = currentProblemIndex;
    }

    public int getCurrentScore() {
        return currentScore;
    }

    public void setCurrentScore(int currentScore) {
        this.currentScore = currentScore;
    }

    public int getNumOfSkippedProblems() {
        return numOfSkippedProblems;
    }

    public void setNumOfSkippedProblems(int numOfSkippedProblems) {
        this.numOfSkippedProblems = numOfSkippedProblems;
    }

    public PlayerSessionStatus getPlayerSessionStatus() {
        return playerSessionStatus;
    }

    public void setPlayerSessionStatus(PlayerSessionStatus playerSessionStatus) {
        this.playerSessionStatus = playerSessionStatus;
    }

    public LocalDateTime getFinishedAt() {
        return finishedAt;
    }

    public void setFinishedAt(LocalDateTime finishedAt) {
        this.finishedAt = finishedAt;
    }

    public List<Submission> getSubmissions() {
        return submissions;
    }

    public void setSubmissions(List<Submission> submissions) {
        this.submissions = submissions;
    }
}
