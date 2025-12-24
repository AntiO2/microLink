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
    /**
     * 查询点赞数最多的 TOP N 内容统计信息。
     * Spring Data JPA 会自动解析方法名并生成类似 "SELECT * FROM content_stats ORDER BY like_count DESC LIMIT N" 的 SQL。
     *
     * @param pageable 包含分页和排序信息。例如 PageRequest.of(0, 10, Sort.by("likeCount").descending())
     * @return 内容统计信息列表
     */
    List<ContentStats> findAllByOrderByLikeCountDesc(Pageable pageable);
    /**
     * 查询评论数最多的 TOP N 内容统计信息。
     *
     * @param pageable 包含分页信息，例如 PageRequest.of(0, 10)
     * @return 内容统计信息列表
     */
    List<ContentStats> findAllByOrderByCommentCountDesc(Pageable pageable);
}
