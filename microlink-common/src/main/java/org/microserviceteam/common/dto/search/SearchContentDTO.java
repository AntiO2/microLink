package org.microserviceteam.common.dto.search;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SearchContentDTO {
    // 原始文档数据（包含完整字段，无 HTML 标签）
    private ContentDoc source;

    // 高亮片段映射（Key 为字段名，Value 为带 HTML 的高亮字符串）
    // 例如：{"title": "如何学习<em>Java</em>"}
    private Map<String, String> highlights;

    public ContentDoc getSource() {
        return source;
    }

    public void setSource(ContentDoc source) {
        this.source = source;
    }

    public Map<String, String> getHighlights() {
        return highlights;
    }

    public void setHighlights(Map<String, String> highlights) {
        this.highlights = highlights;
    }
}
