package org.microserviceteam.common.dto.search;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

@Data
@Document(indexName = "users")
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserIndex {

    @Id
    private String id; // 对应用户微服务的 userId

    @Field(type = FieldType.Text, analyzer = "ik_max_word")
    private String nickname; // 用户昵称，支持中文分词

    @Field(type = FieldType.Keyword)
    private String username; // 用户名，精确匹配

    @Field(type = FieldType.Text)
    private String bio; // 个人简介

    @Field(type = FieldType.Integer)
    private Integer userLevel; // 用户等级（可用于排序加权）

    @Field(type = FieldType.Boolean)
    private Boolean isVerified; // 是否认证账号
}