package com.camergo.notification.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.twilio.http.TwilioRestClient;

@Configuration
@EnableConfigurationProperties(TwilioProperties.class)
public class TwilioConfig {
    @Bean
    public TwilioRestClient twilioInitializer(TwilioProperties config) {
        return new TwilioRestClient.Builder(config.getAccountSid(), config.getAuthToken()).build();
    }
}