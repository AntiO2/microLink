package com.example.microlink_push.config;

import feign.Logger;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for Feign clients.
 * This class can be used to define shared configurations like logging,
 * request interceptors, or error decoders for all Feign clients.
 */
@Configuration
public class FeignClientConfig {

    /**
     * Sets the logging level for Feign clients to FULL.
     * This is very useful for debugging requests and responses to other microservices.
     * @return Logger.Level
     */
    @Bean
    Logger.Level feignLoggerLevel() {
        return Logger.Level.FULL;
    }
}

