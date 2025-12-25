package org.microserviceteam.social.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.microserviceteam.social.dto.CommentDO;

@Mapper
public interface CommentMapper extends BaseMapper<CommentDO> {
}
