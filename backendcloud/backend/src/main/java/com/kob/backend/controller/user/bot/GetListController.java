package com.kob.backend.controller.user.bot;

import com.kob.backend.Service.user.bot.GetListService;
import com.kob.backend.pojo.Bot;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;

import org.springframework.web.bind.annotation.RestController;

import java.util.List;


@RestController
public class GetListController {
    @Autowired
    private GetListService getListService;

    @GetMapping("/api/user/bot/getlist/")
    public List<Bot> getlist(){
        return getListService.getList();
    }
}
