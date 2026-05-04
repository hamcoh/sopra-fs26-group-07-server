package ch.uzh.ifi.hase.soprafs26.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import ch.uzh.ifi.hase.soprafs26.constant.PlayerSessionStatus;
import ch.uzh.ifi.hase.soprafs26.entity.PlayerSession;

@Repository("playerSessionRepository")
public interface PlayerSessionRepository extends JpaRepository<PlayerSession, Long> {

    PlayerSession findByPlayerSessionId(Long playerSessionId);
    boolean existsByPlayer_IdAndPlayerSessionStatus(Long userId, PlayerSessionStatus playerSessionStatus);
}
