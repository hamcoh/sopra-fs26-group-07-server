package ch.uzh.ifi.hase.soprafs26.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import ch.uzh.ifi.hase.soprafs26.constant.GameDifficulty;
import ch.uzh.ifi.hase.soprafs26.constant.GameLanguage;
import ch.uzh.ifi.hase.soprafs26.entity.Problem;

/**
 * Talks to the database
 */
@Repository("problemRepository")
public interface ProblemRepository extends JpaRepository<Problem, Long> {

    Problem findByProblemId(Long problemId);

    List<Problem> findAllByGameLanguageAndGameDifficulty(GameLanguage gameLanguage, GameDifficulty gameDifficulty);
}
