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
@RequestMapping("/api/stats")
public class StatisticsController {

    private final StatisticsService statisticsService;

    @Autowired
    public StatisticsController(StatisticsService statisticsService) {
        this.statisticsService = statisticsService;
    }

    /**
     * 获取指定日期的日活跃用户数 (DAU)。
     *
     * @param date 查询日期, 格式为 yyyy-MM-dd。如果未提供，则默认为当天。
     * @return DAU 数量
     */
    @GetMapping("/dau")
    public ResponseEntity<Long> getDau(
            @RequestParam(value = "date", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        
        LocalDate queryDate = (date == null) ? LocalDate.now() : date;
        long dauCount = statisticsService.getDailyActiveUsers(queryDate);
        return ResponseEntity.ok(dauCount);
    }

    /**
     * 获取指定内容的热度统计信息。
     *
     * @param contentId 内容 ID
     * @return 内容统计信息
     */
    @GetMapping("/content/{contentId}")
    public ResponseEntity<ContentStats> getContentStats(@PathVariable Long contentId) {
        return statisticsService.getContentStats(contentId)
                .map(ResponseEntity::ok) // 如果找到，返回 200 OK 和数据
                .orElse(ResponseEntity.notFound().build()); // 如果未找到，返回 404 Not Found
    }
}
