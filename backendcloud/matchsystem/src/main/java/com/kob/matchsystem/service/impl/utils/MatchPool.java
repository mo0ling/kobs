package com.kob.matchsystem.service.impl.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 在websocket当中接收到这个请求之后，它会把我们的请求再发送给我们的匹配系统
 * 在匹配系统中看到的输出、一个添加一个取消两个操作
 * 接收到一个请求之后会将当前匹配的所有用户放到一个池子
 * 然后我们开一个额外的新线程、是每隔一秒去扫描一遍数组
 * 将能够匹配的人匹配到一起、匹配的时候、匹配两名分值比较相近的玩家
 * 随着时间的推移、两名玩家允许的分差可以越来越大可能一开始a和b分值比较大
 * 这两个人是不能匹配到一块的，但是随着时间推移我们发现没有其他玩家可以跟这两名玩家匹配
 * 我们就可以逐步放大两名玩家的一个匹配范围、直到两名玩家都可以匹配到一块为止
 * 具体就是、每位玩家有一个分值rating
 * 第0秒的时候这个玩家只能匹配分差在十以内的玩家、第二秒的时候就可以匹配分差在20以内的玩家
 */
@Component
public class MatchPool extends Thread {
    private static List<Player> players = new ArrayList<>();

    private ReentrantLock lock = new ReentrantLock();

    private static RestTemplate restTemplate;

    private final static String startGameUrl = "http://127.0.0.1:80/pk/start/game/";

    @Autowired
    public void setRestTemplate(RestTemplate restTemplate){
        MatchPool.restTemplate = restTemplate;
    }
    // 在多个线程(匹配线程遍历players时，主线程调用方法时)会操作players变量，因此加锁
    public void addPlayer(Integer userId, Integer rating, Integer botId){
        lock.lock();;
        try {
            players.add(new Player(userId, rating,botId, 0));
        }finally {
            lock.unlock();
        }
    }
    // 在多个线程(匹配线程遍历players时，主线程调用方法时)会操作players变量，因此加锁
    public void removePlayer(Integer userId){//重新建立除去id
        lock.lock();;
        try {
            List<Player> newPlayers = new ArrayList<>();
            for(int i = 0; i < players.size(); i++){
                if(!players.get(i).getUserId().equals(userId)){
                    newPlayers.add(players.get(i));
                }
            }
            players = newPlayers;
        }finally {
            lock.unlock();
        }
    }

    private void increaseWaitingTime(){//所有玩家等待时间加1
        for(Player p : players){
            p.setWaitingTime(p.getWaitingTime() + 1);
        }
    }

    private boolean checkMatched(Player a, Player b){
        //判断两名玩家是否匹配
        int ratingDelta = Math.abs(a.getRating() - b.getRating());
        int waitingTime = Math.min(a.getWaitingTime(), b.getWaitingTime());// min: 若取min则代表两者分差都小于 等待时间 * 10，实力差距最接近
        return ratingDelta <= waitingTime * 10;
    }
    private void sendResult(Player a, Player b){
        //返回a和b的匹配结果
        System.out.println("send Result: " + a + " " + b);
        MultiValueMap<String, String > data = new LinkedMultiValueMap<>();
        data.add("a_id",a.getUserId().toString());
        data.add("a_bot_id", a.getBotId().toString());
        data.add("b_id",b.getUserId().toString());
        data.add("b_bot_id", b.getBotId().toString());
        restTemplate.postForObject(startGameUrl, data, String.class);
    }
    private void matchPlayers(){
        //尝试匹配所有玩家
        boolean[] vis = new boolean[players.size()];// 标记是否被匹配
        for (int i = 0; i < players.size(); i++){// 先枚举等待最久的玩家，恰好是players前面的玩家等待的的久
            if(vis[i]){
                continue;
            }
            for(int j = i + 1; j < players.size(); j++){
                if(vis[j]){
                    continue;
                }
                Player a = players.get(i);
                Player b = players.get(j);
                if(checkMatched(a, b)){
                    vis[i] = vis[j] = true;
                    sendResult(a, b);
                    break;
                }
            }
        }
        List<Player> newPlayers = new ArrayList<>();
        for(int i = 0; i < players.size(); i++){
            if(!vis[i]){
                newPlayers.add(players.get(i));
            }
        }
        players = newPlayers;
    }

    /**
     * 线程的逻辑、周期性的执行
     * 每次执行的时候每隔一段时间判断一下当前玩家有没有匹配的
     * 这里可以写一个死循环、有匹配的返回没有就等待
     * 写一个sleep函数每次sleep一秒、每一秒钟自动执行一遍
     * 每位玩家每等待一秒就在waitingtime里加一、他未来匹配的阈值就可以变宽
     */
    @Override
    public void run() {
        while (true){
            try {
                Thread.sleep(1000);
                lock.lock();
                try {
                    increaseWaitingTime();
                    matchPlayers();
                }finally {
                    lock.unlock();
                }
            }catch (InterruptedException e){
                e.printStackTrace();
            }
        }
    }
}
