package ch.uzh.ifi.hase.soprafs26.service;

import com.google.cloud.secretmanager.v1.AccessSecretVersionResponse;
import com.google.cloud.secretmanager.v1.SecretManagerServiceClient;
import com.google.cloud.secretmanager.v1.SecretVersionName;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class SecretManagerService {

    @Value("${gcp.project-id}")
    private String projectId;

    public String getSecret(String secretId) {
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