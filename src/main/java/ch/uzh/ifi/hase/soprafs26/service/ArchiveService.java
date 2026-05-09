package ch.uzh.ifi.hase.soprafs26.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import ch.uzh.ifi.hase.soprafs26.repository.SubmissionRepository;
import ch.uzh.ifi.hase.soprafs26.rest.dto.ArchiveProblemDTO;
import jakarta.transaction.Transactional;

@Service
@Transactional
public class ArchiveService {

    private final Logger log = LoggerFactory.getLogger(ArchiveService.class);
    private final SubmissionRepository submissionRepository;

    public ArchiveService(SubmissionRepository submissionRepository) {
        this.submissionRepository = submissionRepository;
    }

    public List<ArchiveProblemDTO> getArchiveProblemsAndPlayerResults(Long userId) {

        List<ArchiveProblemDTO> results = getListOfHardestProblems();
        List<ArchiveProblemDTO> archivedProblems = getPlayerStatistics(userId, results);

        return archivedProblems;
    }

    private List<ArchiveProblemDTO> getListOfHardestProblems() {

        List<Object[]> hardestProblems = submissionRepository.findTopHardestProblems();

        if (hardestProblems.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No hardest Problems yet to display!");
        }

        List<ArchiveProblemDTO> archiveProblems = new ArrayList<>();

        for (Object[] hp : hardestProblems) {

            ArchiveProblemDTO archiveProblemDTO = new ArchiveProblemDTO();
            archiveProblemDTO.setProblemId(((Number) hp[0]).longValue());
            archiveProblemDTO.setTitle((String) hp[1]);
            archiveProblemDTO.setDescription((String) hp[2]);
            archiveProblemDTO.setSumPassedTestCases(((Number) hp[3]).longValue());
            archiveProblemDTO.setSumTotalTestCases(((Number) hp[4]).longValue());
            archiveProblemDTO.setTotalSubmissionCount(((Number) hp[5]).longValue());
            archiveProblemDTO.setTotalSuccessRate(hp[6] != null ? ((Number) hp[6]).doubleValue() : 0.0);
            archiveProblems.add(archiveProblemDTO);
        }

        return archiveProblems;
    }

    private List<ArchiveProblemDTO> getPlayerStatistics(Long userId, List<ArchiveProblemDTO> archiveProblems) {
        
        List<Long> problemIds = archiveProblems.stream().map(ArchiveProblemDTO::getProblemId).toList();
        
        List<Object[]> playerStats = submissionRepository.findPlayerStatsForProblems(userId, problemIds);

        Map<Long, Object[]> playerStatsMap = playerStats.stream().collect(Collectors.toMap(row -> ((Number) row[0]).longValue(), row -> row));

        for (ArchiveProblemDTO ap : archiveProblems) {
            Object[] problemStats = playerStatsMap.get(ap.getProblemId());
            if (problemStats != null) {
                ap.setPlayerSumPassedTestCases(((Number) problemStats[1]).intValue());
                ap.setPlayerSumTotalTestCases(((Number) problemStats[2]).intValue());
                ap.setPlayerSuccessRate(problemStats[3] != null ? ((Number) problemStats[3]).doubleValue() : 0.0);
            }
        }

        return archiveProblems;
    }
} 

