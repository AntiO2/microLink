package com.example.microlink_push.service;

import com.example.microlink_push.config.FeignClientConfig;
import com.example.microlink_push.dto.StatisticsDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * Feign client to communicate with the microlink-statistics microservice.
 * The endpoint is an assumption and can be adjusted.
 */
@FeignClient(name = "statistics-service", url = "${clients.statistics-service.url}", configuration = FeignClientConfig.class)
public interface StatisticsServiceClient {

    @GetMapping("/api/statistics/content/{contentId}")
    StatisticsDTO getStatisticsForContent(@PathVariable("contentId") Long contentId);
}

