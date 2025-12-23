package com.example.microlink_statistics.repository;

import com.example.microlink_statistics.entity.ContentStats;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * ContentStats 实体的 JPA Repository。
 * Spring Data JPA 将自动为我们实现基本的 CRUD 方法。
 *
 * @author Rolland1944
 */
@Repository
public interface ContentStatsRepository extends JpaRepository<ContentStats, Long> {
    // 可以在此定义自定义查询方法，例如：查询点赞数最多的 TOP N 内容。
}
