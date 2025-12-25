package org.microserviceteam.workflow.delegate.search;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.activiti.engine.delegate.DelegateExecution;
import org.microserviceteam.common.Result;
import org.microserviceteam.common.ResultCode;
import org.microserviceteam.common.dto.search.ContentDoc;
import org.microserviceteam.common.dto.search.SearchContentDTO;
import org.microserviceteam.workflow.client.SearchClient;
import org.microserviceteam.workflow.config.Constants;
import org.microserviceteam.workflow.delegate.BaseWorkflowDelegate;
import org.microserviceteam.workflow.util.ProcessVariableUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;

@Slf4j
@Component("searchDelegate")
public class SearchDelegate extends BaseWorkflowDelegate {

    @Autowired
    private SearchClient searchClient;

    @Override
    protected String run(DelegateExecution execution) throws Exception {
        String activityId = execution.getCurrentActivityId();
        String output;

        if ("task-es-index".equals(activityId)) {
            // 1. 从流程变量获取透传的 Map
            Object rawDoc = execution.getVariable("contentDoc");
            log.info(">>> [搜索服务] 正在将透传的 KV 数据转换为 ContentDoc 对象...");

            if (rawDoc instanceof Map) {
                try {
                    // 2. 使用 ObjectMapper 执行转换 (将 Map 转换为实体类 ContentDoc)
                    // 这样会自动匹配 Postman 中的 Key 与 ContentDoc 中的属性名
                    ObjectMapper mapper = new ObjectMapper();
                    ContentDoc doc = mapper.convertValue(rawDoc, ContentDoc.class);

                    // 3. 校验关键字段（可选）
                    if (doc == null || doc.getId() == null) {
                        output = "ES Indexing Failed: Converted object or ID is null";
                    } else {
                        // 4. 调用持久化接口
                        log.info(">>> [ES持久化] 准备写入对象: {}", doc);
                        Result<String> indexResult = searchClient.indexContent(doc);

                        if (indexResult != null && indexResult.getCode() == 200) {
                            output = "ES Indexing Success for ID: " + doc.getId();
                            execution.setVariable("isIndexSuccess", indexResult.getMessage());
                        } else {
                            output = "ES Indexing Failed: " + (indexResult != null ? indexResult.getMessage() : "Unknown");
                            execution.setVariable("isIndexSuccess", false);
                        }
                    }
                } catch (IllegalArgumentException e) {
                    // 转换失败，通常是字段类型不匹配
                    log.error(">>> [类型转换异常] Map 转 ContentDoc 失败: ", e);
                    output = "Type Conversion Error: " + e.getMessage();
                    execution.setVariable("isIndexSuccess", false);
                } catch (Exception e) {
                    log.error(">>> [系统异常]: ", e);
                    output = "System Error: " + e.getMessage();
                    throw e; // 抛出异常触发流程回滚
                }
            } else {
                output = "ES Indexing Failed: contentDoc variable is not a Map";
                log.error(">>> [参数错误] 流程变量 contentDoc 类型错误: {}", rawDoc != null ? rawDoc.getClass() : "null");
            }
        } else if ("task-highlight-search".equals(activityId)) {
            // 搜索逻辑：获取查询词
            String query = ProcessVariableUtil.getString(execution, "q", "");
            log.info(">>> [搜索服务] 执行高亮查询: {}", query);
            Result<List<SearchContentDTO>> listResult = searchClient.searchWithHighlight(query);

            // 3. 处理结果并存入变量
            if (listResult != null && listResult.getCode() == ResultCode.SUCCESS.getCode()) {
                List<SearchContentDTO> data = listResult.getData();

                // 将搜索结果存入流程变量，供 Controller 或后续节点使用
                execution.setVariable("searchResult", data.toString());
                output = "Search Completed. Found " + (data != null ? data.size() : 0) + " items.";
            } else {
                // 记录失败原因
                String errorMsg = listResult != null ? listResult.getMessage() : "Response is null";
                output = "Search Failed: " + errorMsg;
                // 可以根据业务需求决定是否设置标志位
                execution.setVariable("isSearchSuccess", false);
            }
        } else {
            output = "Search Service: No action defined for node " + activityId;
        }

        execution.setVariable(Constants.LAST_OUTPUT, output);
        return output;
    }
}