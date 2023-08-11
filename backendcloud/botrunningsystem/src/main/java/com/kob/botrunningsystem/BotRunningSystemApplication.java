package com.kob.botrunningsystem;

import com.kob.botrunningsystem.service.BotRunningService;
import com.kob.botrunningsystem.service.impl.BotRunningServiceImpl;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class BotRunningSystemApplication {
    public static void main(String[] args) {
        BotRunningServiceImpl.botPool.start();//BootStrap服务启动的时候，启动BotPool线程
        SpringApplication.run(BotRunningSystemApplication.class, args);
    }
}
