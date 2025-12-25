package org.microserviceteam.search.config;

import org.microserviceteam.common.dto.search.ContentDoc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.IndexOperations;
import org.springframework.stereotype.Component;

@Component
public class IndexInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(IndexInitializer.class);

    @Autowired
    private ElasticsearchOperations elasticsearchOperations;

    @Override
    public void run(ApplicationArguments args) {
        log.info(">>> [ES初始化] 正在检查 Elasticsearch 索引状态...");

        // 获取针对 ContentDoc 实体的索引操作对象
        IndexOperations indexOps = elasticsearchOperations.indexOps(ContentDoc.class);

        // 1. 检查索引是否存在
        if (!indexOps.exists()) {
            log.warn(">>> [ES初始化] 索引 'content_index' 不存在，准备创建...");

            // 2. 创建索引（根据 @Document 注解中的设置）
            indexOps.create();

            // 3. 创建映射（根据 @Field 注解定义分词器、类型等）
            // 这是最关键的一步，否则 IK 分词器可能不会生效
            indexOps.putMapping(indexOps.createMapping());

            log.info(">>> [ES初始化] 索引及 Mapping 映射创建成功！");
        } else {
            log.info(">>> [ES初始化] 索引 'content_index' 已存在，跳过初始化。");
        }
    }
}