package com.example.microlink_statistics.controller;

import com.example.microlink_statistics.entity.ContentStats;
import com.example.microlink_statistics.service.StatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;

/**
 * 提供数据统计查询的 RESTful API。
 *
 * @author Rolland1944
 */
@RestController
@RequestMapping("/api/statistics")
public class StatisticsController {
    private static final Logger logger = LoggerFactory.getLogger(StatisticsController.class);
    private final StatisticsService statisticsService;
    @Autowired
    public StatisticsController(StatisticsService statisticsService) {
        this.statisticsService = statisticsService;
    }
    /**
     * 获取指定日期的日活跃用户数 (DAU)。
     *
     * @param date 查询的日期，格式为 yyyy-MM-dd。如果未提供，则默认为当天。
     * @return 包含 DAU 数量的 JSON 对象。
     */
    @GetMapping("/dau")
    public ResponseEntity<Map<String, Object>> getDailyActiveUsers(
            @RequestParam(value = "date", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        // 如果前端没有提供日期参数，则默认查询当天的 DAU
        LocalDate queryDate = (date == null) ? LocalDate.now() : date;
        logger.info("Request received for DAU on date: {}", queryDate);
        long dau = statisticsService.getDailyActiveUsers(queryDate);
        // 将结果包装在一个 Map 中，使其返回为结构化的 JSON，例如 {"date": "2023-10-27", "dau": 1500}
        Map<String, Object> response = new HashMap<>();
        response.put("date", queryDate.toString());
        response.put("dau", dau);
        return ResponseEntity.ok(response);
    }
    /**
     * 获取指定内容的统计数据（点赞、评论、分享、浏览数）。
     *
     * @param contentId 内容的唯一标识符。
     * @return 如果找到，返回内容的统计数据和 200 OK；如果未找到，返回 404 Not Found。
     */
    @GetMapping("/content/{contentId}")
    public ResponseEntity<ContentStats> getContentStatistics(@PathVariable Long contentId) {
        logger.info("Request received for content stats with ID: {}", contentId);
        Optional<ContentStats> statsOptional = statisticsService.getContentStats(contentId);
        // 如果 statsOptional 中有值，则将其包装在 200 OK 的响应中返回
        // 如果为空，则构建一个 404 Not Found 响应返回
        return statsOptional
                .map(ResponseEntity::ok) // .map(stats -> ResponseEntity.ok(stats)) 的简写
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
