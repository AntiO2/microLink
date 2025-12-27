package org.microserviceteam.search.controller;

import lombok.extern.slf4j.Slf4j;
import org.microserviceteam.common.Result;
import org.microserviceteam.common.ResultCode;
import org.microserviceteam.common.dto.search.ContentDoc;
import org.microserviceteam.common.dto.search.SearchContentDTO;
import org.microserviceteam.search.service.ContentSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/search")
@Slf4j
public class SearchController {

    @Autowired
    private ContentSearchService searchService;

    @GetMapping("/query")
    public Result<List<ContentDoc>> search(@RequestParam String q) {
        List<ContentDoc> list = searchService.searchContent(q);
        return Result.success(list);
    }

    @GetMapping("/query/highlight")
    public Result<List<SearchContentDTO>> searchWithHighlight(@RequestParam String q) {
        List<SearchContentDTO> results = searchService.searchWithHighlight(q);
        return Result.success(results);
    }

    @PostMapping("/index")
    public Result<String> indexContent(@RequestBody ContentDoc doc) {
        // 1. 基本参数校验（防止空对象进入 Service 层）
        if (doc == null || doc.getId() == null) {
            return Result.error(ResultCode.PARAM_ERROR, "文档对象或ID不能为空");
        }

        try {
            // 2. 调用服务层进行持久化/索引
            searchService.save(doc);

            // 3. 返回标准的成功响应
            log.info(">>> 搜索服务：成功索引文档 [ID: {}]", doc.getId());
            return Result.success("文档索引成功");

        } catch (Exception e) {
            // 4. 捕获 ES 连接或操作异常
            log.error(">>> 搜索服务：索引文档失败 [ID: {}], 原因: {}", doc.getId(), e.getMessage());
            return Result.error(ResultCode.SYSTEM_ERROR, "Elasticsearch 索引失败: " + e.getMessage());
        }
    }
}