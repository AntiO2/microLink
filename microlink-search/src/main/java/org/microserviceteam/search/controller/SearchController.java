package org.microserviceteam.search.controller;

import org.microserviceteam.common.Result;
import org.microserviceteam.common.ResultCode;
import org.microserviceteam.common.dto.search.ContentDoc;
import org.microserviceteam.common.dto.search.SearchContentDTO;
import org.microserviceteam.common.dto.search.UserDoc;
import org.microserviceteam.search.service.ContentSearchService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class SearchController {

    private static final Logger log = LoggerFactory.getLogger(SearchController.class);

    @Autowired
    private ContentSearchService searchService;

    @GetMapping("/search/query")
    public Result<List<ContentDoc>> search(@RequestParam String q) {
        List<ContentDoc> list = searchService.searchContent(q);
        return Result.success(list);
    }

    @GetMapping("/search/query/highlight")
    public Result<List<SearchContentDTO>> searchWithHighlight(@RequestParam String q) {
        List<SearchContentDTO> results = searchService.searchWithHighlight(q);
        return Result.success(results);
    }

    @PostMapping("/search/index")
    public Result<String> indexContent(@RequestBody ContentDoc doc) {
        if (doc == null || doc.getId() == null) {
            return Result.error(ResultCode.PARAM_ERROR, "文档对象或ID不能为空");
        }

        try {
            searchService.save(doc);
            log.info(">>> 搜索服务：成功索引内容文档 [ID: {}]", doc.getId());
            return Result.success("内容文档索引成功");
        } catch (Exception e) {
            log.error(">>> 搜索服务：索引内容文档失败 [ID: {}], 原因: {}", doc.getId(), e.getMessage());
            return Result.error(ResultCode.SYSTEM_ERROR, "Elasticsearch 索引失败: " + e.getMessage());
        }
    }

    @PostMapping("/search/user/index")
    public Result<String> indexUser(@RequestBody UserDoc doc) {
        if (doc == null || doc.getId() == null) {
            return Result.error(ResultCode.PARAM_ERROR, "用户对象或ID不能为空");
        }

        try {
            searchService.saveUser(doc);
            log.info(">>> 搜索服务：成功索引用户文档 [ID: {}]", doc.getId());
            return Result.success("用户文档索引成功");
        } catch (Exception e) {
            log.error(">>> 搜索服务：索引用户文档失败 [ID: {}], 原因: {}", doc.getId(), e.getMessage());
            return Result.error(ResultCode.SYSTEM_ERROR, "Elasticsearch 索引失败: " + e.getMessage());
        }
    }
}
