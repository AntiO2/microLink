package com.example.microlink_push.controller;

import com.example.microlink_push.dto.ContentDTO;
import com.example.microlink_push.service.PushService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/push")
public class PushController {

    @Autowired
    private PushService pushService;

    /**
     * GET /api/v1/push/feed
     * Endpoint to get the hot content feed.
     *
     * @return A list of recommended content.
     */
    @GetMapping("/feed")
    public ResponseEntity<List<ContentDTO>> getHotFeed() {
        List<ContentDTO> feed = pushService.getHotFeed();
        return ResponseEntity.ok(feed);
    }
}

