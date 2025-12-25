package org.microserviceteam.search;

import org.microserviceteam.common.dto.search.ContentDoc;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ContentSearchRepository extends ElasticsearchRepository<ContentDoc, Long> {

    // 自动根据方法名生成查询逻辑：搜索标题或内容中包含关键字的文档
    List<ContentDoc> findByTitleOrText(String title, String text);
}
