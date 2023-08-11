package com.kob.matchsystem.service.impl;

import com.kob.matchsystem.service.MatchService;
import com.kob.matchsystem.service.impl.utils.MatchPool;
import org.springframework.stereotype.Service;

@Service
public class MatchServiceImpl implements MatchService {
    public final static MatchPool matchPool = new MatchPool();

    @Override
    public String addPlayer(Integer userId, Integer rating, Integer botId) {
        System.out.println("add Player: " + userId + " " + rating);
        matchPool.addPlayer(userId, rating, botId);
        return "add Player success";
    }

    @Override
    public String removePlayer(Integer userId) {
        System.out.println("remove Player: " + userId);
        matchPool.removePlayer(userId);
        return "remove Player success";
    }
}
