package org.microserviceteam.workflow.client;

import org.microserviceteam.common.Result;
import org.microserviceteam.common.dto.search.ContentDoc;
import org.microserviceteam.common.dto.search.SearchContentDTO;
import org.microserviceteam.common.dto.search.UserIndex;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * 搜索微服务远程调用客户端
 */
@FeignClient(name = "microlink-search")
public interface SearchClient {

    // ================= 内容搜索相关 =================

    /**
     * 内容发布时触发索引添加
     */
    @PostMapping("/search/index")
    Result<String> indexContent(@RequestBody ContentDoc doc);

    /**
     * 带高亮的搜索接口
     */
    @GetMapping("/search/query/highlight")
    Result<List<SearchContentDTO>> searchWithHighlight(@RequestParam("q") String q);

    /**
     * 基础内容搜索接口
     */
    @GetMapping("/search/query")
    Result<List<ContentDoc>> search(@RequestParam("q") String q);

    // ================= 用户搜索相关 (新增) =================

    /**
     * 远程检索用户信息
     * 对应 SearchController 中的 /search/users 接口
     */
    @GetMapping("/search/users")
    Result<Page<UserIndex>> searchUsers(
            @RequestParam("keyword") String keyword,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size
    );

    /**
     * 用户信息变更时触发索引同步
     * 对应 SearchController 中的 /search/user/index 接口
     */
    @PostMapping("/search/user/index")
    Result<String> indexUser(@RequestBody UserIndex userIndex);
}