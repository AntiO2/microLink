package org.microserviceteam.common.dto.search;

import lombok.Data;
import java.util.List;

@Data
public class ContentIndexDTO
{
    private Long id;
    private Long userId;
    private String content;
    private List<String> tags;
    private String location;
    private Long createTime;
}