package org.microserviceteam.microlink_content.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.microserviceteam.microlink_content.model.Content;
import org.microserviceteam.microlink_content.payload.request.PublishRequest;
import org.microserviceteam.microlink_content.service.ContentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class ContentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ContentService contentService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(username = "testUser")
    public void testPublishContentApi() throws Exception {
        PublishRequest request = new PublishRequest();
        request.setTitle("API Test Title");
        request.setContent("API Test Content");

        Content mockContent = new Content();
        mockContent.setId(123L);
        mockContent.setTitle("API Test Title");
        
        when(contentService.publishContent(any(PublishRequest.class), eq("testUser"))).thenReturn(mockContent);

        mockMvc.perform(post("/api/content/publish")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(123L))
                .andExpect(jsonPath("$.title").value("API Test Title"));
    }
}
