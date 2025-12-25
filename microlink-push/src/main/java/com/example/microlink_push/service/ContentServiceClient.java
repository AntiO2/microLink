package com.example.microlink_push.service;

import com.example.microlink_push.config.FeignClientConfig;
import com.example.microlink_push.dto.ContentDTO;
import com.example.microlink_push.dto.PaginatedResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Feign client to communicate with the microlink-content microservice.
 */
@FeignClient(name = "content-service", url = "${clients.content-service.url}", configuration = FeignClientConfig.class)
public interface ContentServiceClient {

    @GetMapping("/api/content/list")
    PaginatedResponse<ContentDTO> listPublishedContent(
            @RequestParam("page") int page,
            @RequestParam("size") int size,
            @RequestParam("status") String status);
}

