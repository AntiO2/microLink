package org.microserviceteam.workflow.delegate.search;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.activiti.engine.delegate.DelegateExecution;
import org.microserviceteam.common.Result;
import org.microserviceteam.common.ResultCode;
import org.microserviceteam.common.dto.search.ContentDoc;
import org.microserviceteam.common.dto.search.SearchContentDTO;
import org.microserviceteam.common.dto.search.UserIndex;
import org.microserviceteam.workflow.client.SearchClient;
import org.microserviceteam.workflow.config.Constants;
import org.microserviceteam.workflow.delegate.BaseWorkflowDelegate;
import org.microserviceteam.workflow.util.ProcessVariableUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Slf4j
@Component("searchDelegate")
public class SearchDelegate extends BaseWorkflowDelegate {

    @Autowired
    private SearchClient searchClient;

    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    protected String run(DelegateExecution execution) throws Exception {
        String activityId = execution.getCurrentActivityId();
        String output;

        switch (activityId) {
            case "task-es-index":
                output = handleContentIndexing(execution);
                break;

            case "task-highlight-search":
                output = handleHighlightSearch(execution);
                break;

            case "task-user-index":
                // 新增：处理用户索引同步逻辑
                output = handleUserIndexing(execution);
                break;

            case "task-user-search":
                // 新增：处理用户检索逻辑
                output = handleUserSearch(execution);
                break;

            default:
                output = "Search Service: No action defined for node " + activityId;
        }

        execution.setVariable(Constants.LAST_OUTPUT, output);
        return output;
    }

    /**
     * 处理内容索引 (task-es-index)
     */
    private String handleContentIndexing(DelegateExecution execution) {
        Object rawDoc = execution.getVariable("contentDoc");
        if (!(rawDoc instanceof Map)) {
            return "ES Content Indexing Failed: contentDoc is not a Map";
        }

        try {
            ContentDoc doc = mapper.convertValue(rawDoc, ContentDoc.class);
            Result<String> result = searchClient.indexContent(doc);
            boolean success = result != null && result.getCode() == 200;
            execution.setVariable("isIndexSuccess", success);
            return success ? "Content Indexing Success ID: " + doc.getId() : "Failed: " + result.getMessage();
        } catch (Exception e) {
            log.error(">>> [Content Indexing Error]: ", e);
            return "Error: " + e.getMessage();
        }
    }

    /**
     * 处理高亮搜索 (task-highlight-search)
     */
    private String handleHighlightSearch(DelegateExecution execution) {
        String query = ProcessVariableUtil.getString(execution, "q", "");
        Result<List<SearchContentDTO>> result = searchClient.searchWithHighlight(query);

        if (result != null && result.getCode() == ResultCode.SUCCESS.getCode()) {
            execution.setVariable("searchResult", result.getData());
            return "Search Success. Found " + (result.getData() != null ? result.getData().size() : 0);
        }
        return "Search Failed";
    }

    /**
     * 新增：处理用户同步逻辑 (task-user-index)
     */
    private String handleUserIndexing(DelegateExecution execution) {
        Object rawUser = execution.getVariable("userIndexDoc");
        log.info(">>> [搜索服务] 正在同步用户索引数据...");

        if (rawUser instanceof Map) {
            try {
                UserIndex userIndex = mapper.convertValue(rawUser, UserIndex.class);
                Result<String> result = searchClient.indexUser(userIndex);

                boolean success = result != null && result.getCode() == 200;
                execution.setVariable("isUserIndexSuccess", success);
                return success ? "User Indexing Success ID: " + userIndex.getId() : "User Indexing Failed";
            } catch (Exception e) {
                log.error(">>> [User Indexing Error]: ", e);
                return "User conversion error: " + e.getMessage();
            }
        }
        return "User Indexing Failed: Invalid Input Type";
    }

    /**
     * 新增：处理用户分页搜索逻辑 (task-user-search)
     */
    private String handleUserSearch(DelegateExecution execution) {
        String keyword = ProcessVariableUtil.getString(execution, "keyword", "");
        int page = ProcessVariableUtil.getInt(execution, "page", 0);
        int size = ProcessVariableUtil.getInt(execution, "size", 10);

        log.info(">>> [搜索服务] 正在检索用户, 关键字: {}, 页码: {}", keyword, page);
        Result<Page<UserIndex>> result = searchClient.searchUsers(keyword, page, size);

        if (result != null && result.getCode() == ResultCode.SUCCESS.getCode()) {
            Page<UserIndex> userPage = result.getData();
            execution.setVariable("userSearchResult", userPage.getContent());
            execution.setVariable("userTotalCount", userPage.getTotalElements());
            return "User Search Success. Count: " + userPage.getNumberOfElements();
        }
        return "User Search Failed";
    }
}