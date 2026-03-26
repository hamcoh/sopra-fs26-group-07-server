package ch.uzh.ifi.hase.soprafs26.config;

import ch.uzh.ifi.hase.soprafs26.service.SecretManagerService;
import org.springframework.stereotype.Component;

@Component
public class CloudflareAccess {

    private final SecretManagerService secretManagerService;

    private String clientId;
    private String clientSecret;

    public CloudflareAccess(SecretManagerService secretManagerService) {
        this.secretManagerService = secretManagerService;
    }

    public String getClientId() {
        if (clientId == null) {
            clientId = secretManagerService.getSecret("CF-Access-Client-Id");
        }
        return clientId;
    }

    public String getClientSecret() {
        if (clientSecret == null) {
            clientSecret = secretManagerService.getSecret("CF-Access-Client-Secret");
        }
        return clientSecret;
    }
}