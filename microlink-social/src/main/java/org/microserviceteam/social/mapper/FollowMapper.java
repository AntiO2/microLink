package org.microserviceteam.social.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.microserviceteam.social.dto.FollowDO;

@Mapper
public interface FollowMapper extends BaseMapper<FollowDO> {
}
