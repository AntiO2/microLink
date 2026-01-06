package org.microserviceteam.search.service;

import org.microserviceteam.common.dto.search.UserIndex;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public interface UserSearchService {
    /**
     * 根据关键字搜索用户
     */
    Page<UserIndex> searchUsers(String keyword, Pageable pageable);

    /**
     * 同步/更新用户信息到索引库
     */
    void saveOrUpdateUser(UserIndex userIndex);

    /**
     * 从索引库删除用户
     */
    void deleteUser(String userId);
}
