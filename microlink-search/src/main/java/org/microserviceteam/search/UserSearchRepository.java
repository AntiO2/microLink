package org.microserviceteam.search;

import org.microserviceteam.common.dto.search.UserDoc;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserSearchRepository extends ElasticsearchRepository<UserDoc, Long> {
}
