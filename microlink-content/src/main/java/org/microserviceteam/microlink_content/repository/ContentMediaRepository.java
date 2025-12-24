package org.microserviceteam.microlink_content.repository;

import org.microserviceteam.microlink_content.model.ContentMedia;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ContentMediaRepository extends JpaRepository<ContentMedia, Long> {
    List<ContentMedia> findByContentId(Long contentId);
}
