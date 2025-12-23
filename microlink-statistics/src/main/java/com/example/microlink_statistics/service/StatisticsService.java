package com.example.microlink_statistics.service;

import com.example.microlink_statistics.dto.ContentInteractionEvent;
import com.example.microlink_statistics.dto.UserActivityEvent;
import com.example.microlink_statistics.entity.ContentStats;
import java.time.LocalDate;
import java.util.Optional;

/**
 * 数据统计服务核心业务逻辑接口。
 * 定义了所有统计相关的功能，供 Consumer 和 Controller 调用。
 *
 * @author Rolland1944
 */
public interface StatisticsService {

    /**
     * 记录一次用户活动，主要用于统计 DAU。
     *
     * @param event 用户活动事件
     */
    void recordUserActivity(UserActivityEvent event);

    /**
     * 更新内容互动数据，例如点赞、评论数。
     *
     * @param event 内容互动事件
     */
    void updateContentInteraction(ContentInteractionEvent event);

    /**
     * 获取指定日期的日活跃用户数 (DAU)。
     *
     * @param date 查询日期
     * @return 当天的 DAU
     */
    long getDailyActiveUsers(LocalDate date);

    /**
     * 获取指定内容的热度统计信息。
     *
     * @param contentId 内容 ID
     * @return 内容统计信息 Optional
     */
    Optional<ContentStats> getContentStats(Long contentId);

    // 未来可以扩展更多接口，例如获取热门内容列表等。
}
