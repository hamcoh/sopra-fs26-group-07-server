package ch.uzh.ifi.hase.soprafs26.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import ch.uzh.ifi.hase.soprafs26.rest.dto.JudgeBatchRequestDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.JudgeBatchResultDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.JudgeRequestDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.JudgeTokenDTO;

@SpringBootTest
class JudgeServiceIntegrationTest {

    @Autowired private JudgeService judgeService;
    @Autowired private RestTemplate restTemplate;

    @MockitoBean private SecretManagerService secretManagerService;

    private MockRestServiceServer mockServer;

    @BeforeEach
    void setup() {
        when(secretManagerService.getSecret("CF-Access-Client-Id")).thenReturn("test-cf-id");
        when(secretManagerService.getSecret("CF-Access-Client-Secret")).thenReturn("test-cf-secret");
        mockServer = MockRestServiceServer.createServer(restTemplate);
    }


    @Test
    void submitBatch_usesCorrectUrlAndMethod() {
        mockServer.expect(requestTo(org.hamcrest.Matchers.containsString("/submissions/batch")))
                  .andExpect(method(HttpMethod.POST))
                  .andExpect(requestTo(org.hamcrest.Matchers.containsString("base64_encoded=false")))
                  .andRespond(withSuccess("[]", MediaType.APPLICATION_JSON));

        judgeService.submitBatch(new JudgeBatchRequestDTO());

        mockServer.verify();
    }

    @Test
    void submitBatch_attachesCloudflareAccessHeaders() {
        mockServer.expect(requestTo(org.hamcrest.Matchers.containsString("/submissions/batch")))
                  .andExpect(header("CF-Access-Client-Id", "test-cf-id"))
                  .andExpect(header("CF-Access-Client-Secret", "test-cf-secret"))
                  .andExpect(header("Content-Type", org.hamcrest.Matchers.containsString("application/json")))
                  .andRespond(withSuccess("[]", MediaType.APPLICATION_JSON));

        judgeService.submitBatch(new JudgeBatchRequestDTO());

        mockServer.verify();
    }

    @Test
    void submitBatch_withRealSubmission_sendsCorrectJsonBody() {
        // build a real submission with all fields
        JudgeRequestDTO submission = new JudgeRequestDTO();
        submission.setSource_code("print('hello')");
        submission.setLanguage_id(71); // Python 
        submission.setStdin("");
        submission.setExpected_output("hello");

        JudgeBatchRequestDTO request = new JudgeBatchRequestDTO();
        request.setSubmissions(List.of(submission));

        mockServer.expect(requestTo(org.hamcrest.Matchers.containsString("/submissions/batch")))
                  .andExpect(method(HttpMethod.POST))
                  .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                  .andExpect(content().string(org.hamcrest.Matchers.containsString("source_code")))
                  .andExpect(content().string(org.hamcrest.Matchers.containsString("print('hello')")))
                  .andExpect(content().string(org.hamcrest.Matchers.containsString("language_id")))
                  .andExpect(content().string(org.hamcrest.Matchers.containsString("71")))
                  .andRespond(withSuccess("[{\"token\":\"tok-xyz\"}]", MediaType.APPLICATION_JSON));

        List<JudgeTokenDTO> result = judgeService.submitBatch(request);

        mockServer.verify();
        assertEquals(1, result.size());
        assertEquals("tok-xyz", result.get(0).getJudgeToken());
    }

    @Test
    void submitBatch_parsesMultipleTokensFromResponse() {
        String responseBody = "[{\"token\":\"tok-1\"},{\"token\":\"tok-2\"},{\"token\":\"tok-3\"}]";

        mockServer.expect(requestTo(org.hamcrest.Matchers.containsString("/submissions/batch")))
                  .andRespond(withSuccess(responseBody, MediaType.APPLICATION_JSON));

        List<JudgeTokenDTO> result = judgeService.submitBatch(new JudgeBatchRequestDTO());

        assertEquals(3, result.size());
        assertEquals("tok-1", result.get(0).getJudgeToken());
        assertEquals("tok-2", result.get(1).getJudgeToken());
        assertEquals("tok-3", result.get(2).getJudgeToken());
    }

    @Test
    void getBatchResults_multipleTokens_joinsWithCommaInUrl() {
        mockServer.expect(requestTo(org.hamcrest.Matchers.containsString("tokens=tok-A,tok-B,tok-C")))
                  .andExpect(method(HttpMethod.GET))
                  .andRespond(withSuccess("{\"submissions\":[]}", MediaType.APPLICATION_JSON));

        judgeService.getBatchSubmissionResults(List.of("tok-A", "tok-B", "tok-C"));

        mockServer.verify();
    }

    @Test
    void getBatchResults_sendsGetWithCloudflareHeaders() {
        mockServer.expect(requestTo(org.hamcrest.Matchers.containsString("tokens=")))
                  .andExpect(method(HttpMethod.GET))
                  .andExpect(header("CF-Access-Client-Id", "test-cf-id"))
                  .andExpect(header("CF-Access-Client-Secret", "test-cf-secret"))
                  .andRespond(withSuccess("{\"submissions\":[]}", MediaType.APPLICATION_JSON));

        judgeService.getBatchSubmissionResults(List.of("tok-1"));

        mockServer.verify();
    }

    @Test
    void getBatchResults_parsesSubmissionsFromResponse() {
        String responseBody = "{\"submissions\":[" +
                "{\"stdout\":\"3\\n\",\"status\":{\"id\":3,\"description\":\"Accepted\"}}," +
                "{\"stdout\":null,\"status\":{\"id\":4,\"description\":\"Wrong Answer\"}}" +
                "]}";

        mockServer.expect(requestTo(org.hamcrest.Matchers.containsString("tokens=")))
                  .andRespond(withSuccess(responseBody, MediaType.APPLICATION_JSON));

        JudgeBatchResultDTO result =
                judgeService.getBatchSubmissionResults(List.of("tok-1", "tok-2"));

        assertNotNull(result.getSubmissions());
        assertEquals(2, result.getSubmissions().size());
    }

}
