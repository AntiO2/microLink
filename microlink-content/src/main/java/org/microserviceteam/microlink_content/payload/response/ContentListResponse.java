package org.microserviceteam.microlink_content.payload.response;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ContentListResponse {
    private Long id;
    private String title;
    private String summary;
    private String coverUrl;
    private String status;
    private String contentType;
    private LocalDateTime createdAt;
    private AuthorInfo author;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }
    public String getCoverUrl() { return coverUrl; }
    public void setCoverUrl(String coverUrl) { this.coverUrl = coverUrl; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getContentType() { return contentType; }
    public void setContentType(String contentType) { this.contentType = contentType; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public AuthorInfo getAuthor() { return author; }
    public void setAuthor(AuthorInfo author) { this.author = author; }

    @Data
    public static class AuthorInfo {
        private String id;
        private String nickname;
        private String avatar;
        
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getNickname() { return nickname; }
        public void setNickname(String nickname) { this.nickname = nickname; }
        public String getAvatar() { return avatar; }
        public void setAvatar(String avatar) { this.avatar = avatar; }
    }
}
