package ch.uzh.ifi.hase.soprafs26.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import com.google.cloud.secretmanager.v1.AccessSecretVersionResponse;
import com.google.cloud.secretmanager.v1.SecretManagerServiceClient;
import com.google.cloud.secretmanager.v1.SecretPayload;
import com.google.cloud.secretmanager.v1.SecretVersionName;
import com.google.protobuf.ByteString;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.test.util.ReflectionTestUtils;

class SecretManagerServiceTest {

    private SecretManagerService secretManagerService;
    // Note: The tests assume that the projectId is set to "test-project-id" for testing purposes.
    @BeforeEach
    void setUp() {
        secretManagerService = new SecretManagerService();
        ReflectionTestUtils.setField(secretManagerService, "projectId", "test-project-id");
    }
    // The following tests cover the getSecret method of SecretManagerService, ensuring it correctly retrieves secrets and handles exceptions.
    @Test
    void getSecret_validSecretId_returnsSecretPayload() throws Exception {
        // setup
        String secretId = "CF-Access-Client-Id";
        String expectedSecret = "test-secret-value";

        SecretManagerServiceClient client = mock(SecretManagerServiceClient.class);
        // Mock the response from the Secret Manager API to return the expected secret value when accessed with the correct secret ID.
        SecretPayload payload =
                SecretPayload.newBuilder()
                        .setData(ByteString.copyFromUtf8(expectedSecret))
                        .build();
        // The response is constructed to mimic what the Secret Manager API would return when the secret is accessed successfully.
        AccessSecretVersionResponse response =
                AccessSecretVersionResponse.newBuilder()
                        .setPayload(payload)
                        .build();
        // The expected secret version name is constructed based on the project ID and secret ID, following the format used by the Secret Manager API.
        SecretVersionName expectedSecretVersionName =
                SecretVersionName.of("test-project-id", secretId, "latest");

        when(client.accessSecretVersion(expectedSecretVersionName))
                .thenReturn(response);
        
        try (MockedStatic<SecretManagerServiceClient> mockedClient =
                     mockStatic(SecretManagerServiceClient.class)) {

            mockedClient.when(SecretManagerServiceClient::create)
                    .thenReturn(client);

            // Act
            String result = secretManagerService.getSecret(secretId);

            // Assert
            assertEquals(expectedSecret, result);
        }
    }
    // This test verifies that if the SecretManagerServiceClient throws an exception (e.g., due to missing credentials), the getSecret method correctly wraps it in an IllegalStateException with a descriptive message.
    @Test
    void getSecret_clientThrowsException_wrapsInIllegalStateException() throws Exception {
        // Arrange
        String secretId = "missing-secret";

        try (MockedStatic<SecretManagerServiceClient> mockedClient =
                     mockStatic(SecretManagerServiceClient.class)) {

            mockedClient.when(SecretManagerServiceClient::create)
                    .thenThrow(new RuntimeException("Google credentials missing"));

            // Act
            IllegalStateException thrown = assertThrows(
                    IllegalStateException.class,
                    () -> secretManagerService.getSecret(secretId)
            );

            // Assert
            assertEquals("Failed to read secret: " + secretId, thrown.getMessage());
        }
    }
}