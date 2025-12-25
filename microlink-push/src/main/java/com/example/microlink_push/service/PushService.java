package com.example.microlink_push.service;

import com.example.microlink_push.dto.ContentDTO;

import java.util.List;

/**
 * Interface for the main business logic of the push service.
 */
public interface PushService {

    /**
     * Starts the asynchronous process to calculate the hot feed.
     * This method is "fire-and-forget". It returns immediately after starting the process.
     * The process runs in the background.
     * @return The ID of the process instance that was started.
     */
    String startHotFeedProcess();
    /**
     * Retrieves the latest successfully calculated hot feed result.
     * This method queries the process history for the most recently finished instance
     * and returns its final 'rankedContent' variable.
     * @return A list of ContentDTO representing the top 10 hot feed, or an empty list if none is found.
     */
    List<ContentDTO> getHotFeed();
    /**
     * Handles a newly published piece of content, deciding whether to add it
     * to the cache.
     * @param contentId The ID of the newly published content.
     */
    void processNewPublishedContent(Long contentId);
}

