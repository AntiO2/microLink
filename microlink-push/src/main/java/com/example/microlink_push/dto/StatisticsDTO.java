package com.example.microlink_push.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * DTO to hold the interaction statistics for a piece of content.
 */
@Data
public class StatisticsDTO implements Serializable {
    private Long contentId;
    private long likes;
    private long comments;
    private long shares;
    private long views;
}
