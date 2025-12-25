package com.example.microlink_push.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * A generic DTO for handling paginated API responses from other services,
 * like microlink-content's /list endpoint.
 * @param <T> The type of the content in the list.
 */
@Data
public class PaginatedResponse<T> implements Serializable {
    private List<T> content;
    private int pageNumber;
    private int pageSize;
    private long totalElements;
    private int totalPages;
}

