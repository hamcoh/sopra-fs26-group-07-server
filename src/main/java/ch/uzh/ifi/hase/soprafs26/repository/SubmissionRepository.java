package ch.uzh.ifi.hase.soprafs26.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import ch.uzh.ifi.hase.soprafs26.entity.Submission;

/**
 * Talks to the database
 * The repository gives methods like:
 * save a submission !
 * find a submission by id
 * get all submissions for a user
 * check whether a final submission already exists
 */
@Repository("submissionRepository")
public interface SubmissionRepository extends JpaRepository<Submission, Long> {
    
}
