package com.example.microlink_content.service;

import com.example.microlink_content.model.Content;
import com.example.microlink_content.repository.ContentRepository;
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
    private FileStorageService fileStorageService;

    @Mock
    private ProcessService processService;

    @InjectMocks
    private ContentService contentService;

    @Test
    void testPublishContent_Article() {
        // Arrange
        String title = "My Article";
        String text = "This is a long article";
        Content.ContentType type = Content.ContentType.ARTICLE;
        String authorId = "user123";
        MultipartFile file = null;

        Content savedContent = new Content();
        savedContent.setId(1L);
        savedContent.setTitle(title);
        savedContent.setText(text);
        savedContent.setContentType(type);
        savedContent.setStatus(Content.ContentStatus.PENDING);

        when(contentRepository.save(any(Content.class))).thenReturn(savedContent);

        // Act
        Content result = contentService.publishContent(title, text, type, file, authorId);

        // Assert
        assertNotNull(result);
        assertEquals(title, result.getTitle());
        assertEquals(Content.ContentType.ARTICLE, result.getContentType());
        verify(processService, times(1)).startProcess(eq("content-review"), any(Map.class));
    }
}
