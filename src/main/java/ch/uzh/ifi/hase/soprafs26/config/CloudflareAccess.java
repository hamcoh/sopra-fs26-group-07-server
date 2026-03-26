package ch.uzh.ifi.hase.soprafs26.config;

import ch.uzh.ifi.hase.soprafs26.service.SecretManagerService;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

@Component
public class CloudflareAccess {

    private final SecretManagerService secretManagerService;

    private String clientId;
    private String clientSecret;

    public CloudflareAccess(SecretManagerService secretManagerService) {
        this.secretManagerService = secretManagerService;
    }

    @PostConstruct
    public void init() {
        this.clientId = secretManagerService.getSecret("CF-Access-Client-Id");
        this.clientSecret = secretManagerService.getSecret("CF-Access-Client-Secret");
    }

    public String getClientId() {
        return clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }
}