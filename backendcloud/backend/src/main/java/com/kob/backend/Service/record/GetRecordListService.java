package com.kob.backend.Service.record;

import com.alibaba.fastjson.JSONObject;

public interface GetRecordListService {
    JSONObject getList(Integer page);
}
