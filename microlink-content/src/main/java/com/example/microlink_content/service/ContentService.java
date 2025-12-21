package com.example.microlink_content.service;

import com.example.microlink_content.model.Content;
import com.example.microlink_content.repository.ContentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ContentService {
    @Autowired
    private ContentRepository contentRepository;

    @Autowired
    private FileStorageService fileStorageService;

    @Autowired
    private ProcessService processService;

    public Content publishContent(String title, String text, Content.ContentType contentType, MultipartFile file, String authorId) {
        String mediaUrl = null;
        if (file != null && !file.isEmpty()) {
            mediaUrl = fileStorageService.storeFile(file);
        }

        Content content = new Content();
        content.setTitle(title);
        content.setText(text);
        content.setContentType(contentType);
        content.setMediaUrl(mediaUrl);
        content.setAuthorId(authorId);
        content.setStatus(Content.ContentStatus.PENDING);

        Content savedContent = contentRepository.save(content);

        // Start Workflow
        Map<String, Object> variables = new HashMap<>();
        variables.put("contentId", savedContent.getId());
        variables.put("authorId", authorId);
        processService.startProcess("content-review", variables);

        return savedContent;
    }

    public List<Content> getPublishedContent() {
        return contentRepository.findByStatus(Content.ContentStatus.PUBLISHED);
    }

    public List<Content> getContentForUser(String userId) {
        return contentRepository.findByStatusOrAuthorId(Content.ContentStatus.PUBLISHED, userId);
    }
    
    public Content getContent(Long id) {
        return contentRepository.findById(id).orElse(null);
    }
    
    public void updateStatus(Long id, Content.ContentStatus status) {
        Content content = getContent(id);
        if (content != null) {
            content.setStatus(status);
            contentRepository.save(content);
        }
    }
}
