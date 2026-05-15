package ch.uzh.ifi.hase.soprafs26.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import ch.uzh.ifi.hase.soprafs26.constant.SubmissionType;
import ch.uzh.ifi.hase.soprafs26.entity.Submission;
import java.time.LocalDateTime;
import java.util.List;

@Repository("submissionRepository")
public interface SubmissionRepository extends JpaRepository<Submission, Long> {

    boolean existsByGameSessionIdAndProblemIdAndPlayerSessionIdAndType( // long ah name 💀
            Long gameSessionId,
            Long problemId,
            Long playerSessionId,
            SubmissionType type
    );

    Submission findBySubmissionId(Long submissionId);

    Submission findTopByGameSessionIdAndProblemIdAndPlayerSessionIdAndTypeOrderBySubmissionIdDesc( // long long ah name part 2 💀💀
        Long gameSessionId,
        Long problemId,
        Long playerSessionId,
        SubmissionType type
    );

    long countByGameSessionIdAndProblemIdAndPlayerSessionIdAndTypeAndSubmittedAtAfter(
        Long gameSessionId,
        Long problemId,
        Long playerSessionId,
        SubmissionType type,
        LocalDateTime submittedAt
    );

    /**
     Query retrieves the currently 5 'hardest' problems. 
     'Hardest' is defined based on how well a problem was solved in general.
     In order to retrieve the 5 'hardest' problems, the query:
        1) Filters for problems that have been played at least 3 times
        2) Computes a naive difficulty indicator: 
              - The formula is: 1 - (passed_test_cases/total_test_cases) * log(1 + problemSolveCount)
             - the log-part dampens the effect of problems that have been played way more often than others
        3) Returns the top 5 of the query (arbitrary amount which can be changed)
     */ 
    @Query(value = """
            SELECT 
                s.problem_id,
                p.title,
                p.description,
                p.game_language,
                SUM(s.passed_test_cases),
                SUM(s.total_test_cases),
                COUNT(s.submission_id),
                (SUM(s.passed_test_cases) * 1.0 / SUM(s.total_test_cases)) * 100 AS success_rate
            FROM submissions s
            JOIN problems p ON s.problem_id = p.problem_id
            GROUP BY s.problem_id, p.title, p.description
            HAVING COUNT(s.submission_id) > 2
            ORDER BY (1.0 - (SUM(s.passed_test_cases) * 1.0 / SUM(s.total_test_cases)))
                    * LOG(1 + COUNT(s.submission_id)) DESC
            LIMIT 5;
            """, nativeQuery = true)
    List<Object[]> findTopHardestProblems();

    @Query(value = """
            SELECT 
                s.problem_id,
                p.title,
                p.description,
                p.game_language,
                SUM(s.passed_test_cases),
                SUM(s.total_test_cases),
                COUNT(s.submission_id),
                (SUM(s.passed_test_cases) * 1.0 / SUM(s.total_test_cases)) * 100 AS success_rate
            FROM submissions s
            JOIN problems p ON s.problem_id = p.problem_id
            GROUP BY s.problem_id, p.title, p.description
            ORDER BY COUNT(s.submission_id) DESC
            LIMIT 5;
            """, nativeQuery = true)
    List<Object[]> findMostPopularProblems();
    
    /*
    Query retrieves the player (via param userId) stats for the currently hardest problems
    retrieved in the query above (via param problemIds).
    In short: it returns how well a player scored so far on the received problems. 
    Beware: It may be that the player did not solve some (or any) of the given problems.
     */
    @Query(value = """
            SELECT s.problem_id,
                SUM(s.passed_test_cases),
                SUM(s.total_test_cases),
                (SUM(s.passed_test_cases) * 1.0 / SUM(s.total_test_cases)) * 100 AS player_success_rate
            FROM player_sessions ps
            JOIN submissions s ON s.player_session_id = ps.player_session_id
            WHERE ps.player_id = :userId
            AND s.problem_id IN :problemIds
            GROUP BY s.problem_id
            """, nativeQuery = true)
    List<Object[]> findPlayerStatsForProblems(@Param("userId") Long userId, @Param("problemIds") List<Long> problemIds);

}