package ch.uzh.ifi.hase.soprafs26.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import ch.uzh.ifi.hase.soprafs26.entity.GameSession;

@Repository("gameSessionRepository")
public interface GameSessionRepository extends JpaRepository<GameSession, Long> {
    
    GameSession findByGameSessionId(Long gameSessionId);
    
}