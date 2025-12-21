package com.example.microlink_content.controller;

import com.example.microlink_content.model.Content;
import com.example.microlink_content.model.ContentMedia;
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

    @PostMapping("/upload")
    public ResponseEntity<?> upload(@RequestParam("file") MultipartFile file) {
        String uploaderId = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        ContentMedia media = contentService.uploadMedia(file, uploaderId);
        return ResponseEntity.ok(media);
    }

    @PostMapping("/publish")
    public ResponseEntity<?> publish(@RequestParam(value = "title", required = false) String title,
                                     @RequestParam("text") String text,
                                     @RequestParam(value = "contentType", defaultValue = "POST") String contentTypeStr,
                                     @RequestParam(value = "cover", required = false) MultipartFile cover,
                                     @RequestParam(value = "media", required = false) MultipartFile media,
                                     @RequestParam(value = "mediaIds", required = false) List<Long> mediaIds) {
        String authorId = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        Content.ContentType contentType;
        try {
            contentType = Content.ContentType.valueOf(contentTypeStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Invalid content type");
        }

        Content content = contentService.publishContent(title, text, contentType, cover, media, mediaIds, authorId);
        return ResponseEntity.ok(content);
    }

    @GetMapping("/list")
    public ResponseEntity<List<Content>> list() {
        String authorId = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return ResponseEntity.ok(contentService.getContentForUser(authorId));
    }
}
