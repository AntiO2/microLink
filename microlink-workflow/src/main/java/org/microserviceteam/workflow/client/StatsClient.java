package org.microserviceteam.workflow.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "data-statistics-service")
public interface StatsClient {
    @PostMapping("/stats/collect")
    void collectData(@RequestParam("type") String type, @RequestParam("uid") String uid);

    @PostMapping("/stats/send")
    void sendStats(String action);
}