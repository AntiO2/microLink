package com.example.microlink_content.repository;

import com.example.microlink_content.model.Content;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ContentRepository extends JpaRepository<Content, Long> {
    List<Content> findByStatus(Content.ContentStatus status);
    List<Content> findByStatusOrAuthorId(Content.ContentStatus status, String authorId);
}
