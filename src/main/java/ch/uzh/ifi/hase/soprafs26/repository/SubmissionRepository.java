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
            WHERE s.type = 'SUBMIT'
            AND s.status = 'FINISHED'
            GROUP BY s.problem_id, p.title, p.description
            HAVING COUNT(s.submission_id) > 2
            ORDER BY (1.0 - (SUM(s.passed_test_cases) * 1.0 / SUM(s.total_test_cases)))
                    * LOG(1 + COUNT(s.submission_id)) DESC
            LIMIT 5;
            """, nativeQuery = true)
    List<Object[]> findTopHardestProblems();
    
    /**
     Query retrieves the 5 most popular/played problems. 
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
            WHERE s.type = 'SUBMIT'
            AND s.status = 'FINISHED'
            GROUP BY s.problem_id, p.title, p.description
            ORDER BY COUNT(s.submission_id) DESC
            LIMIT 5;
            """, nativeQuery = true)
    List<Object[]> findMostPopularProblems();
    
    /*
    Query retrieves the player (via param userId) stats for provided problem Ids (via param problemIds).
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
            AND s.type = 'SUBMIT'
            AND s.status = 'FINISHED'
            GROUP BY s.problem_id
            """, nativeQuery = true)
    List<Object[]> findPlayerStatsForProblems(@Param("userId") Long userId, @Param("problemIds") List<Long> problemIds);

    /*
    Query retrieves some player stats.
    Optional because it could be fully empty (if no submissions so far).
    'percentile_rank' is calculated s.t. it returns percentage of players 
    whose total points are less than or equal for the user making the
    query/request.
     */
    @Query(value = """
            SELECT 
                u.username, 
                u.total_games_played, 
                u.win_count, 
                SUM(s.passed_test_cases) AS total_passed_test_cases, 
                SUM(s.total_test_cases) AS total_test_cases, 
                SUM(CASE WHEN s.VERDICT  = 'CORRECT_ANSWER' THEN 1 ELSE 0 END) AS total_problems_fully_correct,
                (SELECT ROUND(100.0 * SUM(CASE WHEN u2.total_points <= u.total_points THEN 1 ELSE 0 END) / COUNT(*), 1)
                FROM users u2) AS percentile_rank
            FROM submissions s
            JOIN problems p ON s.problem_id = p.problem_id
            JOIN player_sessions ps ON ps.player_session_id = s.player_session_id
            JOIN users u ON u.id = ps.player_id
            WHERE u.id = :userId
            AND s.type = 'SUBMIT'
            AND s.status = 'FINISHED'
            GROUP BY u.id, u.username, u.total_games_played, u.win_count, u.total_points
            """, nativeQuery = true)
    List<Object[]> findPlayerWrappedStats(@Param("userId") Long userId);

    /*
    Query retrieves for all submission of a player the three enums, i.e., GameLanguage, GameDifficulty and GameMode.
    In the service, for each of those, the most frequent is computed.
     */
    @Query(value = """
            SELECT p.game_language, p.game_difficulty, r.game_mode
            FROM submissions s
            JOIN problems p ON s.problem_id = p.problem_id
            JOIN player_sessions ps ON ps.player_session_id = s.player_session_id
            JOIN game_sessions gs ON gs.game_session_id = ps.game_session_id
            JOIN rooms r ON r.room_id = gs.room_id
            WHERE ps.player_id = :userId
            AND s.type = 'SUBMIT'
            AND s.status = 'FINISHED'
            """, nativeQuery = true)
    List<Object[]> findPlayerEnumStats(@Param("userId") Long userId);

}