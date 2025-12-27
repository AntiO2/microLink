package org.microserviceteam.microlink_content.service;

import org.microserviceteam.microlink_content.model.Content;
import org.microserviceteam.microlink_content.payload.request.PublishRequest;
import org.microserviceteam.microlink_content.repository.ContentMediaRepository;
import org.microserviceteam.microlink_content.repository.ContentRepository;
import org.microserviceteam.microlink_content.client.UserServiceClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ContentServiceTest {

    @Mock
    private ContentRepository contentRepository;

    @Mock
    private ContentMediaRepository contentMediaRepository;

    @Mock
    private FileStorageService fileStorageService;

    @Mock
    private ProcessService processService;

    @Mock
    private UserServiceClient userServiceClient;

    @InjectMocks
    private ContentService contentService;

    @Test
    void testPublishContent_Article() {
        // Arrange
        String title = "My Article";
        String contentText = "This is a long article";
        String type = "ARTICLE";
        String authorId = "user123";
        
        PublishRequest request = new PublishRequest();
        request.setTitle(title);
        request.setContent(contentText);
        request.setContentType(type);
        
        Content savedContent = new Content();
        savedContent.setId(1L);
        savedContent.setTitle(title);
        savedContent.setText(contentText);
        savedContent.setContentType(Content.ContentType.ARTICLE);
        savedContent.setStatus(Content.ContentStatus.PENDING);

        when(contentRepository.save(any(Content.class))).thenReturn(savedContent);

        // Act
        Content result = contentService.publishContent(request, authorId);

        // Assert
        assertNotNull(result);
        assertEquals(title, result.getTitle());
        assertEquals(Content.ContentType.ARTICLE, result.getContentType());
        verify(processService, times(1)).startProcess(eq("content-publish-process-v2"), any(Map.class));
    }

    @Test
    void testCheckContent_Safe() {
        Content content = new Content();
        content.setTitle("Good Title");
        content.setText("Nice content");
        when(contentRepository.findById(1L)).thenReturn(java.util.Optional.of(content));

        assertTrue(contentService.checkContent(1L));
    }

    @Test
    void testCheckContent_BadWords() {
        Content content = new Content();
        content.setTitle("Bad words Title");
        content.setText("Something else");
        when(contentRepository.findById(2L)).thenReturn(java.util.Optional.of(content));

        assertFalse(contentService.checkContent(2L));
    }

    @Test
    void testUpdateStatus() {
        Content content = new Content();
        content.setId(1L);
        content.setStatus(Content.ContentStatus.PENDING);
        when(contentRepository.findById(1L)).thenReturn(java.util.Optional.of(content));

        contentService.updateStatus(1L, Content.ContentStatus.PUBLISHED);

        assertEquals(Content.ContentStatus.PUBLISHED, content.getStatus());
        verify(contentRepository, times(1)).save(content);
    }
}
