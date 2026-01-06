package org.microserviceteam.search.service;

import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.common.lucene.search.function.FunctionScoreQuery;
import org.elasticsearch.common.unit.Fuzziness;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.functionscore.FunctionScoreQueryBuilder;
import org.elasticsearch.index.query.functionscore.ScoreFunctionBuilders;
import org.microserviceteam.common.dto.search.UserIndex;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class UserSearchServiceImpl implements UserSearchService {

    @Autowired
    private ElasticsearchRestTemplate elasticsearchRestTemplate;

    @Override
    public Page<UserIndex> searchUsers(String keyword, Pageable pageable) {
        // 1. 构造多字段搜索请求
        // 提高昵称 (nickname) 的权重，个人简介 (bio) 权重次之
        QueryBuilder queryBuilder = QueryBuilders.multiMatchQuery(keyword, "nickname", "username", "bio")
                .field("nickname", 2.0f)
                .field("username", 1.5f)
                .fuzziness(Fuzziness.AUTO); // 允许一定的模糊匹配

        // 2. 结合业务加权（如认证用户排名靠前）
        FunctionScoreQueryBuilder functionScoreQueryBuilder = QueryBuilders.functionScoreQuery(
                queryBuilder,
                new FunctionScoreQueryBuilder.FilterFunctionBuilder[]{
                        new FunctionScoreQueryBuilder.FilterFunctionBuilder(
                                QueryBuilders.termQuery("isVerified", true),
                                ScoreFunctionBuilders.weightFactorFunction(1.2f)
                        )
                }
        ).scoreMode(FunctionScoreQuery.ScoreMode.MULTIPLY);

        NativeSearchQuery searchQuery = new NativeSearchQueryBuilder()
                .withQuery(functionScoreQueryBuilder)
                .withPageable(pageable)
                .build();

        // 3. 执行查询并转换结果
        SearchHits<UserIndex> searchHits = elasticsearchRestTemplate.search(searchQuery, UserIndex.class);

        List<UserIndex> users = searchHits.getSearchHits().stream()
                .map(SearchHit::getContent)
                .collect(Collectors.toList());

        return new PageImpl<>(users, pageable, searchHits.getTotalHits());
    }

    @Override
    public void saveOrUpdateUser(UserIndex userIndex) {
        elasticsearchRestTemplate.save(userIndex);
        log.info(">>> [搜索服务] 用户索引已更新: {}", userIndex.getNickname());
    }

    @Override
    public void deleteUser(String userId) {
        elasticsearchRestTemplate.delete(userId, UserIndex.class);
        log.info(">>> [搜索服务] 用户索引已删除: ID={}", userId);
    }
}
