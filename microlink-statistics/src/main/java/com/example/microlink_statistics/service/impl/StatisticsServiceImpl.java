package com.example.microlink_statistics.service.impl;

import com.example.microlink_statistics.dto.ContentInteractionEvent;
import com.example.microlink_statistics.dto.UserActivityEvent;
import com.example.microlink_statistics.entity.ContentStats;
import com.example.microlink_statistics.repository.ContentStatsRepository;
import com.example.microlink_statistics.repository.DailyUserActivityRepository;
import com.example.microlink_statistics.service.StatisticsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

/**
 * 数据统计服务核心业务逻辑实现。
 *
 * @author Rolland1944
 */
@Service
public class StatisticsServiceImpl implements StatisticsService {

    private static final Logger logger = LoggerFactory.getLogger(StatisticsServiceImpl.class);
    // --- 常量定义 ---
    private static final String REDIS_KEY_DAU_PREFIX = "dau:";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE; // yyyy-MM-dd
    // 定义互动类型的常量，避免在代码中使用魔法字符串
    private static final String INTERACTION_TYPE_LIKE = "LIKE";
    private static final String INTERACTION_TYPE_COMMENT = "COMMENT";
    private static final String INTERACTION_TYPE_SHARE = "SHARE";
    private static final String INTERACTION_TYPE_VIEW = "VIEW";
    private final RedisTemplate<String, String> redisTemplate;
    private final ContentStatsRepository contentStatsRepository;
    private final DailyUserActivityRepository dailyUserActivityRepository;

    @Autowired
    public StatisticsServiceImpl(RedisTemplate<String, String> redisTemplate,
                                 ContentStatsRepository contentStatsRepository,
                                 DailyUserActivityRepository dailyUserActivityRepository) {
        this.redisTemplate = redisTemplate;
        this.contentStatsRepository = contentStatsRepository;
        this.dailyUserActivityRepository = dailyUserActivityRepository;
    }
    /**
     * {@inheritDoc}
     * 使用 Redis 的 HyperLogLog 数据结构记录日活用户。
     * HyperLogLog 非常适合用于统计海量数据的基数（不重复元素数量），它占用空间极小且性能极高。
     */
    @Override
    public void recordUserActivity(UserActivityEvent event) {
        if (event == null || event.getUserId() == null) {
            logger.warn("Received a user activity event with null data.");
            return;
        }
        // 1. 定义 Redis Key，格式为 "dau:yyyy-MM-dd"
        String key = REDIS_KEY_DAU_PREFIX + DATE_FORMATTER.format(LocalDate.now());
        // 2. 将用户 ID 添加到当天的 HyperLogLog 结构中
        // RedisTemplate 会自动处理 PFADD 命令
        redisTemplate.opsForHyperLogLog().add(key, String.valueOf(event.getUserId()));
        logger.debug("Recorded user activity for user ID {} in key {}", event.getUserId(), key);
    }
    /**
     * {@inheritDoc}
     * 这是一个事务性操作，确保数据更新的一致性。
     * 它会先查找内容统计实体，如果不存在则创建新的，然后根据事件类型更新计数值，最后保存回数据库。
     */
    @Override
    @Transactional
    public void updateContentInteraction(ContentInteractionEvent event) {
        if (event == null || event.getContentId() == null || event.getEventType() == null) {
            logger.warn("Received a content interaction event with null data.");
            return;
        }
        Long contentId = event.getContentId();
        // 1. 根据 contentId 查找实体，如果不存在，则创建一个新的实例
        ContentStats stats = contentStatsRepository.findById(contentId)
                .orElseGet(() -> {
                    logger.info("No stats found for content ID {}, creating a new entry.", contentId);
                    ContentStats newStats = new ContentStats();
                    newStats.setContentId(contentId);
                    return newStats;
                });
        // 2. 根据事件类型更新计数值
        switch (event.getEventType().toUpperCase()) {
            case INTERACTION_TYPE_LIKE:
                stats.setLikeCount(stats.getLikeCount() + 1);
                break;
            case INTERACTION_TYPE_COMMENT:
                stats.setCommentCount(stats.getCommentCount() + 1);
                break;
            case INTERACTION_TYPE_SHARE:
                stats.setShareCount(stats.getShareCount() + 1);
                break;
            case INTERACTION_TYPE_VIEW:
                stats.setViewCount(stats.getViewCount() + 1);
                break;
            default:
                logger.warn("Received unknown interaction event type: {}", event.getEventType());
                return; // 未知类型，直接返回
        }
        // 3. 将更新后的实体保存回数据库 (无论是新建还是更新)
        contentStatsRepository.save(stats);
        logger.debug("Updated content interaction stats for content ID {}", contentId);
    }
    /**
     * {@inheritDoc}
     * 从 Redis 的 HyperLogLog 中直接获取指定日期的 DAU 估算值。
     * 这种方式响应速度极快，远胜于从数据库进行 COUNT(DISTINCT user_id)。
     */
    @Override
    public long getDailyActiveUsers(LocalDate date) {
        if (date == null) {
            return 0;
        }
        // 1. 定义 Redis Key
        String key = REDIS_KEY_DAU_PREFIX + DATE_FORMATTER.format(date);
        
        // 2. 获取 HyperLogLog 的计数值
        // RedisTemplate 会自动处理 PFCOUNT 命令
        Long size = redisTemplate.opsForHyperLogLog().size(key);
        
        return size != null ? size : 0L;
    }
    /**
     * {@inheritDoc}
     * 直接从数据库中获取内容的热度统计信息。
     * Spring Data JPA 的 findById 方法已经为我们实现了高效的查询。
     * 进阶优化：可以为此方法添加缓存（如 @Cacheable），先查 Redis，未命中再查数据库。
     */
    @Override
    public Optional<ContentStats> getContentStats(Long contentId) {
        if (contentId == null) {
            return Optional.empty();
        }
        logger.debug("Fetching stats for content ID {}", contentId);
        return contentStatsRepository.findById(contentId);
    }
}
