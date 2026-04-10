package ch.uzh.ifi.hase.soprafs26.service;

import java.util.List;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.client.RestTemplate;

import ch.uzh.ifi.hase.soprafs26.rest.dto.JudgeBatchRequestDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.JudgeTokenDTO;
import com.fasterxml.jackson.databind.ObjectMapper;



@Service
public class JudgeService {

    private final RestTemplate restTemplate;
    private final SecretManagerService secretManagerService;

    @Value("${judge0.api.url}")
    private String judgeApiUrl;

    public JudgeService(RestTemplate restTemplate, SecretManagerService secretManagerService) {
        this.restTemplate = restTemplate;
        this.secretManagerService = secretManagerService;
    }

    public List<JudgeTokenDTO> submitBatch(JudgeBatchRequestDTO batchRequest) {
    try {
        String clientId = secretManagerService.getSecret("CF-Access-Client-Id");
        String clientSecret = secretManagerService.getSecret("CF-Access-Client-Secret");

        String url = judgeApiUrl + "/submissions/batch?base64_encoded=false&wait=false";

        // Serialize manually because it did not work before and I could not figure out why
        ObjectMapper mapper = new ObjectMapper();
        String jsonBody = mapper.writeValueAsString(batchRequest);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("CF-Access-Client-Id", clientId);
        headers.set("CF-Access-Client-Secret", clientSecret);

        // Send as String instead of DTO again I dont know why but this is the only solution I found
        HttpEntity<String> requestEntity = new HttpEntity<>(jsonBody, headers);

        ResponseEntity<List<JudgeTokenDTO>> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                requestEntity,
                new ParameterizedTypeReference<List<JudgeTokenDTO>>() {}
        );

        return response.getBody();
    }
    catch (Exception e) {
        throw new IllegalStateException("Failed to submit batch to Judge API: " + e.getMessage(), e); // because globalerror handler gives unuseful info
    }
}

}