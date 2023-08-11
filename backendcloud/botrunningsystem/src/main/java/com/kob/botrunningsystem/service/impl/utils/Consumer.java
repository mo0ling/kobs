package com.kob.botrunningsystem.service.impl.utils;

import org.joor.Reflect;//Joor，可以动态编译和执行Java代码。
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.*;
import java.util.UUID;
import java.util.function.Supplier;

//为了让整个执行过程时间可控，每执行一段代码，就需要将其放在一个线程中，线程可以支持如果超时就会断掉的操作。新建一个Consumer类用于表示这种线程。
@Component
public class Consumer extends Thread{
    private Bot bot;


    private static RestTemplate restTemplate;

    private final static String recevieBotMoveUrl = "http://127.0.0.1:80/pk/receive/bot/move/";
    @Autowired
    public void setRestTemplate(RestTemplate restTemplate){
        Consumer.restTemplate = restTemplate;
    }
    public void startTimeout(long timeout, Bot bot){
        this.bot = bot;
        this.start();//开辟线程run

        try {// 在 程序运行结束后 或 程序在指定timeout时间后还未执行完毕 直接中断代码执
            this.join(timeout); //最多等待timeout秒
        } catch (InterruptedException e) {
            e.printStackTrace();
        }finally {
            this.interrupt(); //终端当前线程
        }
    }

    private String addUid(String code, String uid){
        //在code中Bot类名后添加uid
        int k = code.indexOf(" implements java.util.function.Supplier<Integer>");
        return code.substring(0, k) + uid + code.substring(k);
    }


    @Override
    public void run() {
        //动态编译一个代码  如果类重名的话，只会编译一次 ，为了能够让类名不一样 + uuid
        UUID uuid = UUID.randomUUID();
        String uid = uuid.toString().substring(0, 8);//前8位

        Supplier<Integer> botInterface = Reflect.compile(
                "com.kob.botrunningsystem.utils.Bot" + uid,
                addUid(bot.getBotCode(), uid)//在BotCode的Bot类名之后添加uid
        ).create().get();

        File file = new File("input.txt");
        try (PrintWriter fout = new PrintWriter(file)) {
            fout.println(bot.getInput());
            fout.flush();
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        Integer direction = botInterface.get();
        System.out.println("move-direction: " + bot.getUserId() + " " + direction);
        MultiValueMap<String, String> data = new LinkedMultiValueMap<>();
        data.add("user_id", bot.getUserId().toString());
        data.add("direction", direction.toString());

        restTemplate.postForObject(recevieBotMoveUrl, data, String.class);
    }
}
