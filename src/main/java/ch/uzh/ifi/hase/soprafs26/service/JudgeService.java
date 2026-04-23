package ch.uzh.ifi.hase.soprafs26.service;

import ch.uzh.ifi.hase.soprafs26.rest.dto.JudgeBatchRequestDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.JudgeBatchResultDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.JudgeTokenDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@Transactional
public class JudgeService {

    private final Logger log = LoggerFactory.getLogger(JudgeService.class);

    private final SecretManagerService secretManagerService;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public JudgeService(SecretManagerService secretManagerService, RestTemplate restTemplate) {
        this.secretManagerService = secretManagerService;
        this.restTemplate = restTemplate;
        this.objectMapper = new ObjectMapper();
    }

    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("CF-Access-Client-Id", secretManagerService.getSecret("CF-Access-Client-Id"));
        headers.set("CF-Access-Client-Secret", secretManagerService.getSecret("CF-Access-Client-Secret"));
        return headers;
    }

    /**
     * POST: Submits a batch of code to the Judge0 API protected by Cloudflare.
     */
    public List<JudgeTokenDTO> submitBatch(JudgeBatchRequestDTO requestPayload) {
        String judge0Url = "https://judge.hamcoh.com/submissions/batch?base64_encoded=false";

        try {
            // Force the Java object into a perfect JSON String
            String jsonBody = objectMapper.writeValueAsString(requestPayload);
            
            log.info("Sending JSON Payload to Judge0: {}", jsonBody);

            // Attach the raw JSON string to our Request Entity
            HttpEntity<String> entity = new HttpEntity<>(jsonBody, createHeaders());

            log.info("Sending batch submission to Judge0...");
            
            // Using ParameterizedTypeReference because Judge0 returns a JSON Array of tokens
            ResponseEntity<List<JudgeTokenDTO>> response = restTemplate.exchange(
                    judge0Url,
                    HttpMethod.POST,
                    entity,
                    new ParameterizedTypeReference<List<JudgeTokenDTO>>() {}
            );
            
            log.info("Successfully submitted batch. Status: {}", response.getStatusCode().value());
            return response.getBody();

        } catch (RestClientResponseException e) {
            int statusCode = e.getStatusCode().value();
            log.error("Failed to submit batch to Judge API. Status: {} | Response: {}", 
                      statusCode, e.getResponseBodyAsString());
            
            throw new ResponseStatusException(
                    HttpStatus.valueOf(statusCode), 
                    "Error communicating with Judge0 API: " + e.getResponseBodyAsString() 
            );
        } catch (Exception e) {
            log.error("Unexpected error during Judge0 API call", e);
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR, 
                    "Internal error while contacting the Judge API."
            );
        }
    }

    /**
     * GET: Fetches the results for a list of submission tokens.
     */
    public JudgeBatchResultDTO getBatchSubmissionResults(List<String> tokens) {
        // Judge0 expects tokens comma-separated in the URL
        String joinedTokens = String.join(",", tokens);
        String judge0Url = "https://judge.hamcoh.com/submissions/batch?tokens=" + joinedTokens + "&base64_encoded=false";

        // GET requests don't have a body, just headers
        HttpEntity<Void> entity = new HttpEntity<>(createHeaders());

        try {
            log.info("Polling Judge0 for batch results...");
            ResponseEntity<JudgeBatchResultDTO> response = restTemplate.exchange(
                    judge0Url,
                    HttpMethod.GET,
                    entity,
                    JudgeBatchResultDTO.class
            );

            return response.getBody();

        } catch (RestClientResponseException e) {
            int statusCode = e.getStatusCode().value();
            log.error("Failed to fetch batch results. Status: {} | Response: {}", 
                      statusCode, e.getResponseBodyAsString());
            
            throw new ResponseStatusException(
                    HttpStatus.valueOf(statusCode), 
                    "Error fetching results from Judge0 API: " + e.getMessage()
            );
        } catch (Exception e) {
            log.error("Unexpected error fetching Judge0 results", e);
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR, 
                    "Internal error while fetching results."
            );
        }
    }
}