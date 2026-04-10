package ch.uzh.ifi.hase.soprafs26.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestTemplateConfig {

    @Bean
    public RestTemplate restTemplate() {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.getMessageConverters().add(
            new org.springframework.http.converter.json.JacksonJsonHttpMessageConverter() 
        );
        return restTemplate;
    }
    // modified because i thought 
    // this was the reason for malfunctioned http calls, 
    // but it was not, but i will keep it just in case
}