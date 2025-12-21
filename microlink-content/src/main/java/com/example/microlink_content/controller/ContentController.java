package com.example.microlink_content.controller;

import com.example.microlink_content.model.Content;
import com.example.microlink_content.service.ContentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/content")
public class ContentController {
    @Autowired
    private ContentService contentService;

    @PostMapping("/publish")
    public ResponseEntity<?> publish(@RequestParam("text") String text,
                                     @RequestParam(value = "file", required = false) MultipartFile file) {
        String authorId = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Content content = contentService.publishContent(text, file, authorId);
        return ResponseEntity.ok(content);
    }

    @GetMapping("/list")
    public ResponseEntity<List<Content>> list() {
        String authorId = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return ResponseEntity.ok(contentService.getContentForUser(authorId));
    }
}
