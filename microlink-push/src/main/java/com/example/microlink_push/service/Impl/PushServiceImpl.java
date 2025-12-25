package com.example.microlink_push.service.Impl;

import com.example.microlink_push.config.CacheConfig;
import com.example.microlink_push.dto.ContentDTO;
import com.example.microlink_push.service.ContentServiceClient;
import com.example.microlink_push.dto.PaginatedResponse;
import com.example.microlink_push.service.PushService;
import org.flowable.engine.HistoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.engine.history.HistoricProcessInstance;
import org.flowable.variable.api.history.HistoricVariableInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
public class PushServiceImpl implements PushService {

    private static final Logger logger = LoggerFactory.getLogger(PushServiceImpl.class);

    public static final String PROCESS_KEY = "hotContentPushProcess";
    @Autowired
    private RuntimeService runtimeService;
    @Autowired
    private HistoryService historyService;

    @Autowired
    private ContentServiceClient contentServiceClient;

    @Autowired
    private CacheManager cacheManager;


    @Override
    @Transactional
    public String startHotFeedProcess() {
        logger.info("Starting a new '{}' process instance.", PROCESS_KEY);
        // We are not passing any initial variables, the delegates will fetch data.
        Map<String, Object> initialVariables = Collections.emptyMap();
        var processInstance = runtimeService.startProcessInstanceByKey(PROCESS_KEY, initialVariables);
        logger.info("Successfully started process instance with ID: {}", processInstance.getId());
        return processInstance.getId();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ContentDTO> getHotFeed() {
        logger.info("Querying for the latest finished '{}' process result.", PROCESS_KEY);
        // Find the most recently completed process instance for our process definition.
        HistoricProcessInstance latestFinishedInstance = historyService.createHistoricProcessInstanceQuery()
                .processDefinitionKey(PROCESS_KEY)
                .finished() // We only care about completed processes
                .orderByProcessInstanceEndTime().desc() // Order by end time to get the latest
                .listPage(0, 1) // Get only the top one
                .stream()
                .findFirst()
                .orElse(null);
        if (latestFinishedInstance == null) {
            logger.warn("No finished process instance found for key '{}'. Returning empty list.", PROCESS_KEY);
            return Collections.emptyList();
        }
        logger.info("Found latest finished instance with ID: {}. Retrieving result variable 'rankedContent'.", latestFinishedInstance.getId());
        // Now, retrieve the variable from that historical instance.
        HistoricVariableInstance variableInstance = historyService.createHistoricVariableInstanceQuery()
                .processInstanceId(latestFinishedInstance.getId())
                .variableName("rankedContent")
                .singleResult();
        if (variableInstance == null) {
            logger.error("Process {} finished, but the 'rankedContent' variable was not found in its history.", latestFinishedInstance.getId());
            return Collections.emptyList();
        }
        Object rawValue = variableInstance.getValue();
        if (rawValue instanceof List) {
            @SuppressWarnings("unchecked")
            List<ContentDTO> result = (List<ContentDTO>) rawValue;
            return result;
        }
        logger.error("Variable 'rankedContent' was not of the expected type List. Found: {}", rawValue.getClass().getName());
        return Collections.emptyList();
    }

    @Override
    public void processNewPublishedContent(Long contentId) {
        logger.info("Processing newly published content with ID: {} for caching.", contentId);
        try {
            // In a real application, you might fetch a simpler, smaller version of the content
            // for caching. Here, we'll just use the list endpoint as a placeholder.
            PaginatedResponse<ContentDTO> response = contentServiceClient.listPublishedContent(0, 1, "PUBLISHED"); // This is a simplification
            ContentDTO contentToCache = response.getContent().stream()
                    .filter(c -> c.getId().equals(contentId))
                    .findFirst()
                    .orElse(null);

            if (contentToCache != null) {
                Objects.requireNonNull(cacheManager.getCache(CacheConfig.CONTENT_CACHE))
                        .put(contentId, contentToCache);
                logger.info("Successfully cached content with ID: {}", contentId);
            } else {
                logger.warn("Could not find content with ID {} to cache.", contentId);
            }
        } catch (Exception e) {
            logger.error("Failed to cache content with ID: {}", contentId, e);
        }
    }
}


