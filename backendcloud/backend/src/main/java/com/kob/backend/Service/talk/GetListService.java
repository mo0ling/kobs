package com.kob.backend.Service.talk;

import com.alibaba.fastjson.JSONObject;
import com.kob.backend.pojo.Talk;

import java.util.List;

public interface GetListService {
    JSONObject getList(Integer page);
    JSONObject getAllList(Integer page);
}
