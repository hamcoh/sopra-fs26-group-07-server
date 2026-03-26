package ch.uzh.ifi.hase.soprafs26.service;

import ch.uzh.ifi.hase.soprafs26.service.SecretManagerService;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class SecretStartupTester {

    private static final Logger log = LoggerFactory.getLogger(SecretStartupTester.class);

    private final SecretManagerService secretManagerService;

    public SecretStartupTester(SecretManagerService secretManagerService) {
        this.secretManagerService = secretManagerService;
    }

    @PostConstruct
    public void testSecretRead() {
        try {
            String clientId = secretManagerService.getSecret("CF-Access-Client-Id");
            log.info("SECRET TEST SUCCESS: CF-Access-Client-Id loaded, length={}", clientId.length());
        } catch (Exception e) {
            log.error("SECRET TEST FAILED: could not read CF-Access-Client-Id", e);
        }
    }
}