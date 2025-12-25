package org.microserviceteam.common.dto.search;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.time.LocalDateTime;

@Data
@Document(indexName = "content_index") // 索引名称
public class ContentDoc {

    @Id
    private Long id;

    // 使用 ik_max_word 分词器进行深度搜索优化
    @Field(type = FieldType.Text, analyzer = "ik_max_word", searchAnalyzer = "ik_smart")
    private String title;

    @Field(type = FieldType.Text, analyzer = "ik_max_word")
    private String text;

    private String summary;

    @Field(type = FieldType.Keyword) // 不分词，精确匹配
    private String authorId;

    @Field(type = FieldType.Keyword)
    private String contentType;

    @Field(type = FieldType.Keyword)
    private String status;

    @Field(type = FieldType.Date, format = {}, pattern = "uuuu-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
}
