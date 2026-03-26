package ch.uzh.ifi.hase.soprafs26.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import ch.uzh.ifi.hase.soprafs26.entity.Problem;

/**
 * Talks to the database
 */
@Repository("problemRepository")
public interface ProblemRepository extends JpaRepository<Problem, Long> {
    
}
