package org.microserviceteam.microlink_content.payload.request;

import lombok.Data;
import java.util.List;

@Data
public class PublishRequest {
    private String title;
    private String content; // Maps to 'text' in entity
    private String contentType;
    private Long coverId;
    private Long mainMediaId;
    private List<Long> mediaIds;
    private String summary;

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getContentType() { return contentType; }
    public void setContentType(String contentType) { this.contentType = contentType; }
    public Long getCoverId() { return coverId; }
    public void setCoverId(Long coverId) { this.coverId = coverId; }
    public Long getMainMediaId() { return mainMediaId; }
    public void setMainMediaId(Long mainMediaId) { this.mainMediaId = mainMediaId; }
    public List<Long> getMediaIds() { return mediaIds; }
    public void setMediaIds(List<Long> mediaIds) { this.mediaIds = mediaIds; }
    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }
}
