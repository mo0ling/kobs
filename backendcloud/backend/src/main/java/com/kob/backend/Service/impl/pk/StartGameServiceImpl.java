package com.kob.backend.Service.impl.pk;

import com.kob.backend.Service.pk.StartGameService;
import com.kob.backend.consumer.WebSocketServer;
import org.springframework.stereotype.Service;

@Service
public class StartGameServiceImpl implements StartGameService {
    @Override
    public String startGame(Integer aId,Integer aBotId, Integer bId, Integer bBotId) {
        System.out.println("start Game: " + aId + " " + bId);
        WebSocketServer.startGame(aId, aBotId, bId, bBotId);
        return "success start Game";
    }
}
