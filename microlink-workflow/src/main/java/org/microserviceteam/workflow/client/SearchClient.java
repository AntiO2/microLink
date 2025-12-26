package org.microserviceteam.workflow.client;
import org.microserviceteam.common.Result;
import org.microserviceteam.common.dto.search.ContentDoc;
import org.microserviceteam.common.dto.search.SearchContentDTO;
import org.microserviceteam.common.dto.search.UserDoc;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "microlink-search", url = "${microlink.search.url:}")
public interface SearchClient {
    /**
     * 内容发布时触发索引添加
     */
    @PostMapping("/search/index")
    Result<String> indexContent(@RequestBody ContentDoc doc);

    /**
     * 用户注册时触发索引添加
     */
    @PostMapping("/search/user/index")
    Result<String> indexUser(@RequestBody UserDoc doc);

    /**
     * 带高亮的搜索接口
     */
    @GetMapping("/search/query/highlight")
    Result<List<SearchContentDTO>> searchWithHighlight(@RequestParam("q") String q);

    /**
     * 搜索接口
     */
    @GetMapping("/search/query")
    Result<List<ContentDoc>> search(@RequestParam("q") String q);
}
