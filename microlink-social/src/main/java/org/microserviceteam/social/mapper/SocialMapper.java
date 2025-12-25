package org.microserviceteam.social.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.microserviceteam.social.entity.SocialRecord;

@Mapper
public interface SocialMapper extends BaseMapper<SocialRecord> {
}