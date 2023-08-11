package com.kob.matchsystem.service;

public interface MatchService {
    String addPlayer(Integer userId, Integer rating, Integer botId);

    String removePlayer(Integer userId);
}
