package com.example.microlink_push.flowable;

import com.example.microlink_push.dto.ContentDTO;
import com.example.microlink_push.dto.StatisticsDTO;
import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.JavaDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component("rankAndSelectTopContentDelegate")
public class RankAndSelectTopContentDelegate implements JavaDelegate {
    private static final Logger logger = LoggerFactory.getLogger(RankAndSelectTopContentDelegate.class);

    @Override
    public void execute(DelegateExecution execution) {
        logger.info("Flowable Task: Ranking content and selecting top 10...");
        List<ContentDTO> candidates = (List<ContentDTO>) execution.getVariable("candidateContent");
        Map<Long, StatisticsDTO> statsMap = (Map<Long, StatisticsDTO>) execution.getVariable("statisticsMap");

        List<ContentDTO> rankedList = candidates.stream()
                // Calculate a score for each content item
                .map(content -> new ScoredContent(content, calculateScore(statsMap.get(content.getId()))))
                // Sort in descending order of score
                .sorted(Comparator.comparingDouble(ScoredContent::getScore).reversed())
                // Limit to the top 10
                .limit(10)
                // Extract the original ContentDTO
                .map(ScoredContent::getContent)
                .collect(Collectors.toList());

        // Set the final result as a process variable
        execution.setVariable("rankedContent", rankedList);
        logger.info("Successfully ranked and selected top {} content items.", rankedList.size());
    }

    private double calculateScore(StatisticsDTO stats) {
        if (stats == null) {
            return 0.0;
        }
        // Simple weighted scoring algorithm
        // Weights: share=5, comment=3, like=1, view=0.1
        return (stats.getShares() * 5.0) +
                (stats.getComments() * 3.0) +
                (stats.getLikes() * 1.0) +
                (stats.getViews() * 0.1);
    }

    // Helper inner class to hold content and its calculated score
    private static class ScoredContent {
        private final ContentDTO content;
        private final double score;

        public ScoredContent(ContentDTO content, double score) {
            this.content = content;
            this.score = score;
        }

        public ContentDTO getContent() { return content; }
        public double getScore() { return score; }
    }
}

