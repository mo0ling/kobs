package com.kob.backend.controller.talk;

import com.alibaba.fastjson.JSONObject;
import com.kob.backend.Service.talk.GetListService;
import com.kob.backend.pojo.Talk;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;


@RestController
public class GetListController {
    @Autowired
    private GetListService getListService;

    @GetMapping("/api/talk/getList/")
    public JSONObject getlist(@RequestParam Map<String, String> data){
        Integer page = Integer.parseInt(data.get("page"));
        return getListService.getList(page);
    }

    @GetMapping("/api/talk/getAllList/")
    public JSONObject getAlllist(@RequestParam Map<String, String> data){
        Integer page = Integer.parseInt(data.get("page"));
        return getListService.getAllList(page);
    }
}
