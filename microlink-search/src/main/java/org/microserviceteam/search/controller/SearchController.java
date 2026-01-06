package org.microserviceteam.search.controller;

import lombok.extern.slf4j.Slf4j;
import org.microserviceteam.common.Result;
import org.microserviceteam.common.ResultCode;
import org.microserviceteam.common.dto.search.ContentDoc;
import org.microserviceteam.common.dto.search.SearchContentDTO;
import org.microserviceteam.common.dto.search.UserIndex;
import org.microserviceteam.search.service.ContentSearchService;
import org.microserviceteam.search.service.UserSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/search")
@Slf4j
public class SearchController {

    @Autowired
    private ContentSearchService searchService;

    @Autowired
    private UserSearchService userSearchService;

    // ================= 内容搜索接口 =================

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

    // ================= 用户搜索接口 (新增) =================

    /**
     * 根据关键字搜索用户
     * 支持分页查询
     */
    @GetMapping("/users")
    public Result<Page<UserIndex>> searchUsers(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        log.info(">>> 搜索服务：执行用户检索 [关键字: {}, 分页: {}]", keyword, page);
        Page<UserIndex> userPage = userSearchService.searchUsers(keyword, PageRequest.of(page, size));
        return Result.success(userPage);
    }

    // ================= 索引维护接口 =================

    @PostMapping("/index")
    public Result<String> indexContent(@RequestBody ContentDoc doc) {
        if (doc == null || doc.getId() == null) {
            return Result.error(ResultCode.PARAM_ERROR, "文档对象或ID不能为空");
        }
        try {
            searchService.save(doc);
            log.info(">>> 搜索服务：成功索引内容文档 [ID: {}]", doc.getId());
            return Result.success("内容索引成功");
        } catch (Exception e) {
            log.error(">>> 搜索服务：索引内容失败 [ID: {}], 原因: {}", doc.getId(), e.getMessage());
            return Result.error(ResultCode.SYSTEM_ERROR, "ES 索引失败: " + e.getMessage());
        }
    }

    /**
     * 手动更新/创建用户索引
     */
    @PostMapping("/user/index")
    public Result<String> indexUser(@RequestBody UserIndex userIndex) {
        if (userIndex == null || userIndex.getId() == null) {
            return Result.error(ResultCode.PARAM_ERROR, "用户对象或ID不能为空");
        }
        try {
            userSearchService.saveOrUpdateUser(userIndex);
            log.info(">>> 搜索服务：成功索引用户 [ID: {}, 昵称: {}]", userIndex.getId(), userIndex.getNickname());
            return Result.success("用户索引成功");
        } catch (Exception e) {
            log.error(">>> 搜索服务：索引用户失败 [ID: {}], 原因: {}", userIndex.getId(), e.getMessage());
            return Result.error(ResultCode.SYSTEM_ERROR, "ES 用户索引失败: " + e.getMessage());
        }
    }
}