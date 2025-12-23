package org.microserviceteam.workflow.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

@FeignClient(name = "microlink-social")
public interface SocialClient {
    @PostMapping("/social/like")
    void initLike(@RequestBody Map<String, Object> params);

    @PostMapping("/social/compute/square")
    Double square(@RequestBody Double number);

    @PostMapping("/social/compute/negate")
    Double negate(@RequestBody Double number);
}