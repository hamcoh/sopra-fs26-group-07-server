package ch.uzh.ifi.hase.soprafs26.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import ch.uzh.ifi.hase.soprafs26.entity.TestCase;

/**
 * Recall this is how Spring talks to the database.
 */
@Repository("testCaseRepository")
public interface TestCaseRepository extends JpaRepository<TestCase, Long> {
    
    TestCase findByTestCaseId(Long testCaseId);

}
