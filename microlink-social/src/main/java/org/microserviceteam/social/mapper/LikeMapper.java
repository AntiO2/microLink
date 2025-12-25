package org.microserviceteam.social.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.microserviceteam.social.dto.LikeDO;

@Mapper
public interface LikeMapper extends BaseMapper<LikeDO> {
    // 继承后自动拥有 insert, update, selectCount 等方法
}