package com.kob.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.kob.backend.pojo.Talk;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface TalkMapper extends BaseMapper<Talk> {
}
