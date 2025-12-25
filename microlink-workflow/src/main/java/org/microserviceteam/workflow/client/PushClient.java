package org.microserviceteam.workflow.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

@FeignClient(name = "message-push-service")
public interface PushClient {
    @PostMapping("/push/notify")
    void sendNotification(@RequestParam("to") String to, @RequestParam("msg") String msg);
}