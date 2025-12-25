package org.microserviceteam.social.controller;

import org.microserviceteam.common.Result;
import org.microserviceteam.common.ResultCode;
import org.microserviceteam.social.service.SocialService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/social")
public class SocialController {

    @Autowired
    private SocialService socialService;

    @PostMapping("/check/compliance")
    public Result<Boolean> checkCompliance(
            @RequestParam String userId,
            @RequestParam String action,
            @RequestParam String content) {
        return socialService.checkCompliance(userId, action, content);
    }

    @PostMapping("/record/save")
    public Result<Void> saveInteractionRecord(
            @RequestParam String userId,
            @RequestParam String action,
            @RequestParam String instanceId,
            @RequestParam String content) {
        socialService.saveRecord(userId, action, instanceId, content);
        return Result.success(null);
    }
}