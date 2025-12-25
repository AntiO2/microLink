package org.microserviceteam.workflow.client;

import org.microserviceteam.common.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

@FeignClient(name = "microlink-social", path = "/social")
public interface SocialClient {
    @PostMapping("/like")
    void initLike(@RequestBody Map<String, Object> params);

    @PostMapping("/record/save")
    Result<Void> saveInteractionRecord(
            @RequestParam("userId") String userId,
            @RequestParam("action") String action,
            @RequestParam("instanceId") String instanceId,
            @RequestParam("content") String content
    );
    @PostMapping("/check/compliance")
    Result<Boolean> checkCompliance(
            @RequestParam("userId") String userId,
            @RequestParam("action") String action,
            @RequestParam("content") String content // 主要是评论内容
    );

    @PostMapping("/compute/square")
    Double square(@RequestBody Double number);

    @PostMapping("/compute/negate")
    Double negate(@RequestBody Double number);
}