package com.kob.matchsystem.controller;

import com.kob.matchsystem.service.MatchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;
import java.util.Objects;

@RestController
public class MatchingController {
    @Autowired
    private MatchService matchService;

    // 参数不能使用普通map，MultiValueMap和普通map的区别，这个是一个键对应多个值
    @PostMapping("/player/add/")
    public String addPlayer(@RequestParam MultiValueMap<String, String> data){
        Integer userId = Integer.parseInt(Objects.requireNonNull(data.getFirst("user_id")));
        Integer rating = Integer.parseInt(Objects.requireNonNull(data.getFirst("rating")));
        Integer botId = Integer.parseInt(Objects.requireNonNull(data.getFirst("bot_id")));
        return matchService.addPlayer(userId, rating, botId);
    }

    @PostMapping("/player/remove/")
    public String removePlayer(@RequestParam MultiValueMap<String, String> data){
        Integer userId = Integer.parseInt(Objects.requireNonNull(data.getFirst("user_id")));
        return matchService.removePlayer(userId);
    }
}
