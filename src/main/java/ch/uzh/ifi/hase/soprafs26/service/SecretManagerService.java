package ch.uzh.ifi.hase.soprafs26.service;

import com.google.cloud.secretmanager.v1.AccessSecretVersionResponse;
import com.google.cloud.secretmanager.v1.SecretManagerServiceClient;
import com.google.cloud.secretmanager.v1.SecretVersionName;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class SecretManagerService {

    @Value("${gcp.project-id:}")
    private String projectId;

    public String getSecret(String secretId) {
        // 1) Local development fallback: environment variables only
        String envValue = System.getenv(secretId);

        // Support shell-friendly names like CF_Access_Client_Id
        if (envValue == null || envValue.isBlank()) {
            String normalizedEnvName = secretId.replace('-', '_');
            envValue = System.getenv(normalizedEnvName);
        }

        if (envValue != null && !envValue.isBlank()) {
            return envValue;
        }

        // 2) Deployment fallback: Google Secret Manager
        if (projectId == null || projectId.isBlank()) {
            throw new IllegalStateException(
                    "Missing GCP project id and no local environment variable found for secret: " + secretId
            );
        }

        SecretVersionName secretVersionName =
                SecretVersionName.of(projectId, secretId, "latest");

        try (SecretManagerServiceClient client = SecretManagerServiceClient.create()) {
            AccessSecretVersionResponse response = client.accessSecretVersion(secretVersionName);
            return response.getPayload().getData().toStringUtf8();
        } catch (Exception e) {
            throw new IllegalStateException("Failed to read secret: " + secretId, e);
        }
    }
}