package com.example.microlink_push.flowable;

import com.example.microlink_push.dto.ContentDTO;
import com.example.microlink_push.dto.StatisticsDTO;
import com.example.microlink_push.service.StatisticsServiceClient;
import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.JavaDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Component("fetchContentStatisticsDelegate")
public class FetchContentStatisticsDelegate implements JavaDelegate {
    private static final Logger logger = LoggerFactory.getLogger(FetchContentStatisticsDelegate.class);

    @Autowired
    private StatisticsServiceClient statisticsServiceClient;

    @Override
    public void execute(DelegateExecution execution) {
        logger.info("Flowable Task: Fetching statistics for candidate content...");
        List<ContentDTO> candidates = (List<ContentDTO>) execution.getVariable("candidateContent");

        // Fetch statistics for each content item and store them in a Map<ContentID, Stats>
        Map<Long, StatisticsDTO> statsMap = candidates.parallelStream()
                .map(content -> {
                    try {
                        return statisticsServiceClient.getStatisticsForContent(content.getId());
                    } catch (Exception e) {
                        logger.warn("Could not fetch stats for contentId {}: {}", content.getId(), e.getMessage());
                        return null; // Handle cases where stats service fails for one item
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(StatisticsDTO::getContentId, stats -> stats));

        execution.setVariable("statisticsMap", statsMap);
        logger.info("Successfully fetched statistics for {} content items.", statsMap.size());
    }
}

