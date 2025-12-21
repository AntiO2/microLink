package com.example.microlink_content.repository;

import com.example.microlink_content.model.Content;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ContentRepository extends JpaRepository<Content, Long> {
    List<Content> findByStatus(Content.ContentStatus status);
    List<Content> findByStatusOrAuthorId(Content.ContentStatus status, String authorId);
    
    Page<Content> findByStatus(Content.ContentStatus status, Pageable pageable);
    Page<Content> findByContentTypeAndStatus(Content.ContentType contentType, Content.ContentStatus status, Pageable pageable);

    Page<Content> findByStatusAndAuthorId(Content.ContentStatus status, String authorId, Pageable pageable);
    Page<Content> findByContentTypeAndStatusAndAuthorId(Content.ContentType contentType, Content.ContentStatus status, String authorId, Pageable pageable);

    @Query("SELECT c FROM Content c WHERE (c.status = 'PUBLISHED' OR c.authorId = :authorId) AND (:type IS NULL OR c.contentType = :type)")
    Page<Content> findAllForUser(@Param("authorId") String authorId, @Param("type") Content.ContentType type, Pageable pageable);
}
