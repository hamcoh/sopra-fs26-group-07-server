package ch.uzh.ifi.hase.soprafs26.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.nio.charset.StandardCharsets;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import ch.uzh.ifi.hase.soprafs26.rest.dto.JudgeBatchRequestDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.JudgeBatchResultDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.JudgeResultDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.JudgeTokenDTO;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked") 
class JudgeServiceTest {

    @Mock private SecretManagerService secretManagerService;
    @Mock private RestTemplate restTemplate;

    private JudgeService judgeService;

    @BeforeEach
    void setup() {
        when(secretManagerService.getSecret("CF-Access-Client-Id")).thenReturn("test-cf-id");
        when(secretManagerService.getSecret("CF-Access-Client-Secret")).thenReturn("test-cf-secret");
        judgeService = new JudgeService(secretManagerService, restTemplate);
    }


    @Test
    void submitBatch_success_returnsTokenList() {
        JudgeTokenDTO token1 = new JudgeTokenDTO();
        token1.setJudgeToken("abc-111");
        JudgeTokenDTO token2 = new JudgeTokenDTO();
        token2.setJudgeToken("abc-222");

        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST),
                any(HttpEntity.class), any(ParameterizedTypeReference.class)))
            .thenReturn(ResponseEntity.ok(List.of(token1, token2)));

        List<JudgeTokenDTO> result = judgeService.submitBatch(new JudgeBatchRequestDTO());

        assertEquals(2, result.size());
        assertEquals("abc-111", result.get(0).getJudgeToken());
        assertEquals("abc-222", result.get(1).getJudgeToken());
    }

    @Test
    void submitBatch_verifiesCloudflareHeadersAreSent() {
        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST),
                any(HttpEntity.class), any(ParameterizedTypeReference.class)))
            .thenReturn(ResponseEntity.ok(List.of()));

        judgeService.submitBatch(new JudgeBatchRequestDTO());

        // capture the real HttpEntity that was passed to RestTemplate and check both headers
        ArgumentCaptor<HttpEntity<String>> captor = ArgumentCaptor.forClass(HttpEntity.class);
        verify(restTemplate).exchange(anyString(), eq(HttpMethod.POST),
                captor.capture(), any(ParameterizedTypeReference.class));

        assertEquals("test-cf-id",     captor.getValue().getHeaders().getFirst("CF-Access-Client-Id"));
        assertEquals("test-cf-secret", captor.getValue().getHeaders().getFirst("CF-Access-Client-Secret"));
    }

    @Test
    void submitBatch_judge0InvalidSubmission_throws422() {
        // Judge0 returns 422 when the submission is faulty (wrong language ID, bad JSON)
        RestClientResponseException ex = new RestClientResponseException(
                "Unprocessable Entity", 422, "Unprocessable",
                null, "invalid source code".getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8);

        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST),
                any(HttpEntity.class), any(ParameterizedTypeReference.class)))
            .thenThrow(ex);

        ResponseStatusException thrown = assertThrows(ResponseStatusException.class,
                () -> judgeService.submitBatch(new JudgeBatchRequestDTO()));

        assertEquals(422, thrown.getStatusCode().value());
        assertTrue(thrown.getReason().contains("invalid source code"));
    }

    @Test
    void submitBatch_judge0RateLimited_throws429() {
        // Judge0 returns 429 when too many submissions are sent in a short window
        RestClientResponseException ex = new RestClientResponseException(
                "Too Many Requests", 429, "Too Many Requests",
                null, "rate limit exceeded".getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8);

        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST),
                any(HttpEntity.class), any(ParameterizedTypeReference.class)))
            .thenThrow(ex);

        ResponseStatusException thrown = assertThrows(ResponseStatusException.class,
                () -> judgeService.submitBatch(new JudgeBatchRequestDTO()));

        assertEquals(429, thrown.getStatusCode().value());
    }

    @Test
    void submitBatch_judge0Overloaded_throws503() {
        // Judge0 returns 503 when the judge server itself is down or overloaded
        RestClientResponseException ex = new RestClientResponseException(
                "Service Unavailable", 503, "Service Unavailable",
                null, "judge0 overloaded".getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8);

        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST),
                any(HttpEntity.class), any(ParameterizedTypeReference.class)))
            .thenThrow(ex);

        ResponseStatusException thrown = assertThrows(ResponseStatusException.class,
                () -> judgeService.submitBatch(new JudgeBatchRequestDTO()));

        assertEquals(503, thrown.getStatusCode().value());
    }

    @Test
    void submitBatch_networkCrash_throws500() {
        // any unexpected failure (timeout, DNS, connection refused) must be 500
        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST),
                any(HttpEntity.class), any(ParameterizedTypeReference.class)))
            .thenThrow(new RuntimeException("connection refused"));

        ResponseStatusException thrown = assertThrows(ResponseStatusException.class,
                () -> judgeService.submitBatch(new JudgeBatchRequestDTO()));

        assertEquals(500, thrown.getStatusCode().value());
    }

    @Test
    void getBatchResults_multipleTokens_joinsThemWithCommaInUrl() {
        JudgeBatchResultDTO expected = new JudgeBatchResultDTO();
        expected.setSubmissions(List.of(new JudgeResultDTO(), new JudgeResultDTO()));

        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET),
                any(HttpEntity.class), eq(JudgeBatchResultDTO.class)))
            .thenReturn(ResponseEntity.ok(expected));

        JudgeBatchResultDTO result =
                judgeService.getBatchSubmissionResults(List.of("tok-1", "tok-2", "tok-3"));

        assertEquals(2, result.getSubmissions().size());

        ArgumentCaptor<String> urlCaptor = ArgumentCaptor.forClass(String.class);
        verify(restTemplate).exchange(urlCaptor.capture(), eq(HttpMethod.GET),
                any(HttpEntity.class), eq(JudgeBatchResultDTO.class));

        assertTrue(urlCaptor.getValue().contains("tokens=tok-1,tok-2,tok-3"),
                "Expected comma-joined tokens in URL but got: " + urlCaptor.getValue());
    }

    @Test
    void getBatchResults_networkCrash_throws500() {
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET),
                any(HttpEntity.class), eq(JudgeBatchResultDTO.class)))
            .thenThrow(new RuntimeException("timeout"));

        ResponseStatusException thrown = assertThrows(ResponseStatusException.class,
                () -> judgeService.getBatchSubmissionResults(List.of("tok-1")));

        assertEquals(500, thrown.getStatusCode().value());
    }
}
