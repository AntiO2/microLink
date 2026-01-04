package org.microserviceteam.microlink_content.service;

import org.microserviceteam.microlink_content.client.UserDTO;
import org.microserviceteam.microlink_content.client.UserServiceClient;
import org.microserviceteam.microlink_content.client.WorkflowClient;
import org.microserviceteam.microlink_content.model.Content;
import org.microserviceteam.microlink_content.model.ContentMedia;
import org.microserviceteam.microlink_content.payload.request.PublishRequest;
import org.microserviceteam.microlink_content.payload.response.ContentListResponse;
import org.microserviceteam.microlink_content.repository.ContentMediaRepository;
import org.microserviceteam.microlink_content.repository.ContentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

    @Autowired
    private WorkflowClient workflowClient;
    
    @Autowired
    private UserServiceClient userServiceClient;

    public ContentMedia uploadMedia(MultipartFile file, String uploaderId) {
        String url = fileStorageService.storeFile(file);

        ContentMedia media = new ContentMedia();
        media.setUrl(url);
        media.setUploaderId(uploaderId);
        media.setSize(file.getSize());

        String contentType = file.getContentType();
        if (contentType != null && contentType.startsWith("video")) {
            media.setFileType(ContentMedia.MediaType.VIDEO);
            // TODO: Extract video duration using metadata-extractor or ffmpeg
            // For now, we only set size
        } else {
            media.setFileType(ContentMedia.MediaType.IMAGE);
            try {
                BufferedImage bimg = ImageIO.read(file.getInputStream());
                if (bimg != null) {
                    media.setWidth(bimg.getWidth());
                    media.setHeight(bimg.getHeight());
                }
            } catch (IOException e) {
                // Ignore if not an image or failed to read
            }
        }

        return contentMediaRepository.save(media);
    }

    public Content publishContent(PublishRequest request, String authorId) {
        Content content = new Content();
        content.setTitle(request.getTitle());
        content.setText(request.getContent());
        content.setSummary(request.getSummary());
        content.setAuthorId(authorId);
        content.setStatus(Content.ContentStatus.PENDING);
        
        try {
            if (request.getContentType() != null) {
                content.setContentType(Content.ContentType.valueOf(request.getContentType().toUpperCase()));
            } else {
                content.setContentType(Content.ContentType.POST);
            }
        } catch (IllegalArgumentException e) {
            content.setContentType(Content.ContentType.POST);
        }

        // Handle Cover
        if (request.getCoverId() != null) {
            contentMediaRepository.findById(request.getCoverId()).ifPresent(media -> {
                 content.setCoverUrl(media.getUrl());
            });
        }

        // Handle Main Media
        if (request.getMainMediaId() != null) {
             contentMediaRepository.findById(request.getMainMediaId()).ifPresent(media -> {
                 content.setMediaUrl(media.getUrl());
            });
        }

        Content savedContent = contentRepository.save(content);

        // Handle Media IDs
        if (request.getMediaIds() != null && !request.getMediaIds().isEmpty()) {
            List<ContentMedia> mediaList = contentMediaRepository.findAllById(request.getMediaIds());
            for (ContentMedia m : mediaList) {
                if (!m.getUploaderId().equals(authorId)) {
                     // In real scenario, we should throw exception or ignore
                     // throw new RuntimeException("Unauthorized access to media: " + m.getId());
                }
                m.setContentId(savedContent.getId());
                contentMediaRepository.save(m);
            }
        }

        // Start Workflow
        Map<String, Object> variables = new HashMap<>();
        variables.put("contentId", savedContent.getId());
        variables.put("authorId", authorId);
        processService.startProcess("content-publish-process-v2", variables);
        // workflowClient.startProcess("content-publish-process", variables);

        return savedContent;
    }

    public Page<ContentListResponse> listContent(Content.ContentType type, Content.ContentStatus status, Pageable pageable, String currentUserId) {
        Page<Content> contentPage;
        
        if (currentUserId != null && status == null) {
             // Return Published content + User's own content (e.g. Pending)
             contentPage = contentRepository.findAllForUser(currentUserId, type, pageable);
        } else {
             // Strict filtering or Anonymous
             Content.ContentStatus queryStatus = status != null ? status : Content.ContentStatus.PUBLISHED;
             
             if (queryStatus == Content.ContentStatus.PUBLISHED) {
                 // Publicly accessible
                 if (type != null) {
                     contentPage = contentRepository.findByContentTypeAndStatus(type, queryStatus, pageable);
                 } else {
                     contentPage = contentRepository.findByStatus(queryStatus, pageable);
                 }
             } else {
                 // Restricted: only author can view their own non-published content
                 if (currentUserId != null) {
                     if (type != null) {
                         contentPage = contentRepository.findByContentTypeAndStatusAndAuthorId(type, queryStatus, currentUserId, pageable);
                     } else {
                         contentPage = contentRepository.findByStatusAndAuthorId(queryStatus, currentUserId, pageable);
                     }
                 } else {
                     // Anonymous users cannot see non-published content
                     contentPage = Page.empty(pageable);
                 }
             }
        }

        return contentPage.map(this::mapToResponse);
    }
    
    private ContentListResponse mapToResponse(Content content) {
        ContentListResponse response = new ContentListResponse();
        response.setId(content.getId());
        response.setTitle(content.getTitle());
        response.setContent(content.getText());
        response.setSummary(content.getSummary());
        response.setCoverUrl(content.getCoverUrl());
        response.setStatus(content.getStatus().name());
        response.setContentType(content.getContentType().name());
        response.setCreatedAt(content.getCreatedAt());
        
        // Fetch Author Info
        UserDTO user = userServiceClient.getUser(content.getAuthorId());
        ContentListResponse.AuthorInfo authorInfo = new ContentListResponse.AuthorInfo();
        authorInfo.setId(String.valueOf(user.getId()));
        authorInfo.setNickname(user.getUsername());
        // authorInfo.setAvatar(user.getAvatar()); // Not available yet
        response.setAuthor(authorInfo);
        
        return response;
    }

    public void updateContent(Long id, PublishRequest request, String authorId) {
        Content content = contentRepository.findById(id).orElseThrow(() -> new RuntimeException("Content not found"));
        if (!content.getAuthorId().equals(authorId)) {
            throw new RuntimeException("Unauthorized");
        }
        
        content.setTitle(request.getTitle());
        content.setText(request.getContent());
        if (request.getSummary() != null) content.setSummary(request.getSummary());
        
        // Update cover/media if provided (logic can be more complex, e.g. removing old ones)
        if (request.getCoverId() != null) {
            contentMediaRepository.findById(request.getCoverId()).ifPresent(media -> content.setCoverUrl(media.getUrl()));
        }
        if (request.getMainMediaId() != null) {
            contentMediaRepository.findById(request.getMainMediaId()).ifPresent(media -> content.setMediaUrl(media.getUrl()));
        }
        
        contentRepository.save(content);
    }

    public void deleteContent(Long id, String authorId) {
        Content content = contentRepository.findById(id).orElseThrow(() -> new RuntimeException("Content not found"));
        // Allow admin delete? For now just author.
        if (!content.getAuthorId().equals(authorId)) {
            throw new RuntimeException("Unauthorized");
        }
        contentRepository.delete(content);
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

    public boolean checkContent(Long id) {
        Content content = getContent(id);
        if (content == null) {
            return false;
        }
        // Simple mock logic for auto-review
        // Reject if title or content contains "Bad words"
        String text = (content.getTitle() + " " + content.getText()).toLowerCase();
        if (text.contains("bad words")) {
            return false;
        }
        return true;
    }
}
