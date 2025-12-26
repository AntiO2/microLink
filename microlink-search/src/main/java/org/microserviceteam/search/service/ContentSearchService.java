package org.microserviceteam.search.service;

import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.microserviceteam.common.dto.search.ContentDoc;
import org.microserviceteam.common.dto.search.SearchContentDTO;
import org.microserviceteam.common.dto.search.UserDoc;
import org.microserviceteam.search.ContentSearchRepository;
import org.microserviceteam.search.UserSearchRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ContentSearchService {

    // 手动定义日志对象，替代 @Slf4j
    private static final Logger log = LoggerFactory.getLogger(ContentSearchService.class);

    @Autowired
    private ContentSearchRepository repository;

    @Autowired
    private UserSearchRepository userRepository;

    // 使用新版推荐的 ElasticsearchOperations
    @Autowired
    private ElasticsearchOperations elasticsearchOperations;

    /**
     * 添加或更新索引文档
     */
    public void save(ContentDoc doc) {
        repository.save(doc);
        log.info(">>> [ES搜索] 已成功索引内容文档, ID: {}", doc.getId());
    }

    /**
     * 添加或更新用户索引文档
     */
    public void saveUser(UserDoc doc) {
        userRepository.save(doc);
        log.info(">>> [ES搜索] 已成功索引用户文档, ID: {}", doc.getId());
    }

    /**
     * 高级搜索：多字段权重匹配
     */
    public List<ContentDoc> searchContent(String keyword) {
        log.info(">>> [ES搜索] 开始全文检索, 关键字: {}", keyword);

        // 构建原生查询：匹配标题、内容和摘要
        NativeSearchQuery searchQuery = new NativeSearchQueryBuilder()
                .withQuery(QueryBuilders.multiMatchQuery(keyword, "title", "text", "summary")
                        .field("title", 2.0f) // 标题权重翻倍
                        .field("text", 1.0f))
                .withFilter(QueryBuilders.termQuery("status", "PUBLISHED")) // 仅限已发布的
                .withPageable(PageRequest.of(0, 20)) // 分页
                .build();

        // 执行查询
        SearchHits<ContentDoc> searchHits = elasticsearchOperations.search(searchQuery, ContentDoc.class);

        // 提取结果
        return searchHits.getSearchHits().stream()
                .map(SearchHit::getContent)
                .collect(Collectors.toList());
    }

    public List<SearchContentDTO> searchWithHighlight(String keyword) {
        // 定义高亮规则
        HighlightBuilder.Field highlightTitle = new HighlightBuilder.Field("title")
                .preTags("<span style='color:red'>")
                .postTags("</span>");

        NativeSearchQuery searchQuery = new NativeSearchQueryBuilder()
                .withQuery(QueryBuilders.multiMatchQuery(keyword, "title", "text"))
                .withHighlightFields(highlightTitle)
                .build();

        SearchHits<ContentDoc> searchHits = elasticsearchOperations.search(searchQuery, ContentDoc.class);

        return searchHits.getSearchHits().stream().map(hit -> {
            // 1. 获取原始文档
            ContentDoc source = hit.getContent();

            // 2. 提取所有高亮字段放入 Map
            Map<String, String> highlightMap = new HashMap<>();
            hit.getHighlightFields().forEach((field, fragments) -> {
                // fragments 是一个数组，通常取第一个匹配片段
                highlightMap.put(field, fragments.get(0).toString());
            });

            SearchContentDTO dto = new SearchContentDTO();
            dto.setSource(source);
            dto.setHighlights(highlightMap);
            return dto;
        }).collect(Collectors.toList());
    }
}
