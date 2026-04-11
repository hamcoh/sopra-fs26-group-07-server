package ch.uzh.ifi.hase.soprafs26.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import ch.uzh.ifi.hase.soprafs26.constant.SubmissionType;
import ch.uzh.ifi.hase.soprafs26.entity.Submission;

@Repository("submissionRepository")
public interface SubmissionRepository extends JpaRepository<Submission, Long> {

    boolean existsByGameSessionIdAndProblemIdAndPlayerSessionIdAndType( // long ah name 💀
            Long gameSessionId,
            Long problemId,
            Long playerSessionId,
            SubmissionType type
    );

    Submission findBySubmissionId(Long submissionId);
}