package com.kob.backend.consumer;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.kob.backend.consumer.utils.Game;
import com.kob.backend.consumer.utils.JwtAuthentication;
import com.kob.backend.mapper.BotMapper;
import com.kob.backend.mapper.RecordMapper;
import com.kob.backend.mapper.UserMapper;
import com.kob.backend.pojo.Bot;
import com.kob.backend.pojo.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

@Component
@ServerEndpoint("/websocket/{token}")  //接口路径 注意不要以'/'结尾
public class WebSocketServer {
    // 目前正在链接的所有用户链接
    // ConcurrentHashMap: 线程安全的哈希表
    public final static ConcurrentHashMap<Integer, WebSocketServer> users = new ConcurrentHashMap<>();// 用来存在线连接用户信息，concurrent包的线程安全Set，用来存放每个客户端对应的MyWebSocket对象。
    private Session session = null;//与某个客户端的连接会话，需要通过它来给客户端发送数据// 每个连接用Session来维护

    private User user;
    public static UserMapper userMapper;
    public Game game = null;
    public static RecordMapper recordMapper;

    public static RestTemplate restTemplate;
    private static BotMapper botMapper;
    private final static String addPlayerUrl = "http://127.0.0.1:3001/player/add/";
    private final static String removePlayerUrl = "http://127.0.0.1:3001/player/remove/";
    //不是单例// 因为WebSocketServer不是单例的，因此需要用此方式注入UserMapper
    //    //每建立一次连接，就一个实例所以不是单例模式
    @Autowired
    public void setUserMapper(UserMapper userMapper) {
        WebSocketServer.userMapper = userMapper;
    }
    @Autowired
    public void setRecordMapper(RecordMapper recordMapper) {
        WebSocketServer.recordMapper = recordMapper;
    }
    @Autowired
    public void setBotMapper(BotMapper botMapper) {
        WebSocketServer.botMapper = botMapper;
    }
    @Autowired
    public void setRestTemplate(RestTemplate restTemplate){
        WebSocketServer.restTemplate = restTemplate;
    }
    /**
     * 连接建立成功调用的方法
     */
    @OnOpen
    public void onOpen(Session session, @PathParam("token") String token) throws IOException {
        // 建立连接
        this.session = session;
        System.out.println("connected!");
        Integer userId = JwtAuthentication.getUserId(token);
        this.user = userMapper.selectById(userId);
        if(this.user != null){
            users.put(userId, this);//加入set中

        }else{
            this.session.close();
        }
        System.out.println(users);
    }
    /**
     * 连接关闭调用的方法
     */
    @OnClose
    public void onClose() {
        // 关闭链接
        System.out.println("disconnected!");
        if(this.user != null){
            users.remove(this.user.getId());//从set中删除
        }

    }

    public static void startGame(Integer aId,Integer aBotId, Integer bId, Integer bBotId){
        User a = userMapper.selectById(aId);
        User b = userMapper.selectById(bId);
        Bot botA = botMapper.selectById(aBotId), botB = botMapper.selectById(bBotId);


        Game game = new Game(
                13,
                14,
                20,
                a.getId(),
                botA,
                b.getId(),
                botB);
        game.createMap();//画图
        if(users.get(a.getId()) != null)
            users.get(a.getId()).game = game;
        if(users.get(b.getId()) != null)
            users.get(b.getId()).game = game;

        game.start();// 一局游戏一个线程，会执行game类的run方法
        JSONObject respGame = new JSONObject();
        respGame.put("a_id", game.getPlayerA().getId());// 玩家的id以及横纵信息
        respGame.put("a_sx", game.getPlayerA().getSx());
        respGame.put("a_sy", game.getPlayerA().getSy());

        respGame.put("b_id", game.getPlayerB().getId());
        respGame.put("b_sx", game.getPlayerB().getSx());
        respGame.put("b_sy", game.getPlayerB().getSy());
        respGame.put("map",game.getG());

        JSONObject respA = new JSONObject();
        respA.put("event","start-matching");
        respA.put("opponent_username", b.getUsername());
        respA.put("opponent_photo", b.getPhoto());
        respA.put("game",respGame);
        if(users.get(a.getId()) != null)
            users.get(a.getId()).sendMessage(respA.toJSONString());// 通过userId取出a的连接，给A发送respA

        JSONObject respB = new JSONObject();
        respB.put("event","start-matching");
        respB.put("opponent_username", a.getUsername());
        respB.put("opponent_photo", a.getPhoto());
        respB.put("game",respGame);
        if(users.get(b.getId()) != null)
            users.get(b.getId()).sendMessage(respB.toJSONString());
    }
    private void startMatching(Integer botId){
        System.out.println("开始匹配");
        MultiValueMap<String, String> data = new LinkedMultiValueMap<>();
        data.add("user_id",this.user.getId().toString());
        data.add("rating", this.user.getRating().toString());
        data.add("bot_id", botId.toString());
        restTemplate.postForObject(addPlayerUrl, data, String.class);
    }
    private void stopMatching(){
        System.out.println("停止匹配");
        MultiValueMap<String, String> data = new LinkedMultiValueMap<>();
        data.add("user_id",this.user.getId().toString());
        restTemplate.postForObject(removePlayerUrl, data, String.class);

    }
    /**
     * 收到客户端消息后调用的方法
     * @param message 客户端发送过来的消息
     */
    @OnMessage
    public void onMessage(String message, Session session) {
        // 从Client接收消息
        System.out.println("receive message");
        JSONObject json = JSON.parseObject(message);
        String event = json.getString("event");
        if("start-matching".equals(event)){
            startMatching(json.getInteger("bot_id"));
        }else if("stop-matching".equals(event)){
            stopMatching();
        }else if("move".equals(event)){
            move(json.getInteger("direction"));
        }
    }
    private void move(int direction){
        if(game.getPlayerA().getId().equals(user.getId())){
            if(game.getPlayerA().getBotId().equals(-1)) { //人工操作
                game.setNextStepA(direction);
            }
        }else if(game.getPlayerB().getId().equals(user.getId())){
            if(game.getPlayerB().getBotId().equals(-1)){
                game.setNextStepB(direction);
            }
        }
    }

    @OnError
    public void onError(Session session, Throwable error) {
        error.printStackTrace();
    }
    // 后端向前端发送信息
    public void sendMessage(String message){
        synchronized (this.session){ //每个session维护连接
            try{
                this.session.getBasicRemote().sendText(message);//发送文本消息
            }catch (IOException e){
                e.printStackTrace();
            }
        }
    }
}

