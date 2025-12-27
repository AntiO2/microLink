package org.microserviceteam.microlink_content.controller;

import org.microserviceteam.microlink_content.model.Content;
import org.microserviceteam.microlink_content.model.ContentMedia;
import org.microserviceteam.microlink_content.payload.request.PublishRequest;
import org.microserviceteam.microlink_content.payload.response.ContentListResponse;
import org.microserviceteam.microlink_content.service.ContentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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
    public ResponseEntity<?> publish(@RequestBody PublishRequest request) {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String authorId;
        if (principal instanceof org.springframework.security.core.userdetails.UserDetails) {
            authorId = ((org.springframework.security.core.userdetails.UserDetails) principal).getUsername();
        } else {
            authorId = principal.toString();
        }
        Content content = contentService.publishContent(request, authorId);
        return ResponseEntity.status(HttpStatus.CREATED).body(content);
    }

    @GetMapping("/list")
    public ResponseEntity<Page<ContentListResponse>> list(
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String status,
            @PageableDefault(size = 10) Pageable pageable) {
        
        String currentUserId = null;
        try {
            Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            if (principal instanceof String && !"anonymousUser".equals(principal)) {
                currentUserId = (String) principal;
            }
        } catch (Exception e) {
            // ignore
        }

        Content.ContentType contentType = null;
        if (type != null) {
            try {
                contentType = Content.ContentType.valueOf(type.toUpperCase());
            } catch (IllegalArgumentException e) {
                // ignore
            }
        }
        
        Content.ContentStatus contentStatus = null;
        if (status != null) {
            try {
                contentStatus = Content.ContentStatus.valueOf(status.toUpperCase());
            } catch (IllegalArgumentException e) {
                // ignore
            }
        }
        
        return ResponseEntity.ok(contentService.listContent(contentType, contentStatus, pageable, currentUserId));
    }
    
    @PutMapping("/update/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody PublishRequest request) {
         String authorId = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
         contentService.updateContent(id, request, authorId);
         return ResponseEntity.ok().build();
    }

    @PutMapping("/status/{id}")
    public ResponseEntity<?> updateStatus(@PathVariable Long id, @RequestParam String status) {
        try {
            Content.ContentStatus contentStatus = Content.ContentStatus.valueOf(status.toUpperCase());
            contentService.updateStatus(id, contentStatus);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Invalid status");
        }
    }

    @PostMapping("/check/{id}")
    public ResponseEntity<Boolean> checkContent(@PathVariable Long id) {
        boolean result = contentService.checkContent(id);
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String authorId;
        if (principal instanceof org.springframework.security.core.userdetails.UserDetails) {
            authorId = ((org.springframework.security.core.userdetails.UserDetails) principal).getUsername();
        } else {
            authorId = principal.toString();
        }
        contentService.deleteContent(id, authorId);
        return ResponseEntity.noContent().build();
    }
}
