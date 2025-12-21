package com.example.microlink_content.service;

import com.example.microlink_content.model.Content;
import com.example.microlink_content.model.ContentMedia;
import com.example.microlink_content.repository.ContentMediaRepository;
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
    private ContentMediaRepository contentMediaRepository;

    @Autowired
    private FileStorageService fileStorageService;

    @Autowired
    private ProcessService processService;

    public ContentMedia uploadMedia(MultipartFile file, String uploaderId) {
        String url = fileStorageService.storeFile(file);

        ContentMedia media = new ContentMedia();
        media.setUrl(url);
        media.setUploaderId(uploaderId);

        String contentType = file.getContentType();
        if (contentType != null && contentType.startsWith("video")) {
            media.setFileType(ContentMedia.MediaType.VIDEO);
        } else {
            media.setFileType(ContentMedia.MediaType.IMAGE);
        }

        return contentMediaRepository.save(media);
    }

    public Content publishContent(String title, String text, Content.ContentType contentType, 
                                  MultipartFile cover, MultipartFile media, List<Long> mediaIds, 
                                  String authorId) {
        String coverUrl = null;
        if (cover != null && !cover.isEmpty()) {
            coverUrl = fileStorageService.storeFile(cover);
        }

        String mediaUrl = null;
        if (media != null && !media.isEmpty()) {
            mediaUrl = fileStorageService.storeFile(media);
        }

        Content content = new Content();
        content.setTitle(title);
        content.setText(text);
        content.setContentType(contentType);
        content.setCoverUrl(coverUrl);
        content.setMediaUrl(mediaUrl);
        content.setAuthorId(authorId);
        content.setStatus(Content.ContentStatus.PENDING);

        Content savedContent = contentRepository.save(content);

        if (mediaIds != null && !mediaIds.isEmpty()) {
            List<ContentMedia> mediaList = contentMediaRepository.findAllById(mediaIds);
            for (ContentMedia m : mediaList) {
                if (!m.getUploaderId().equals(authorId)) {
                    throw new RuntimeException("Unauthorized access to media: " + m.getId());
                }
                m.setContentId(savedContent.getId());
                contentMediaRepository.save(m);
            }
        }

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
