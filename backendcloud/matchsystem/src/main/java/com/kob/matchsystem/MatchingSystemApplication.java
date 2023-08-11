package com.kob.matchsystem;

import com.kob.matchsystem.service.impl.MatchServiceImpl;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class MatchingSystemApplication {
    public static void main(String[] args) {
        MatchServiceImpl.matchPool.start();
        SpringApplication.run(MatchingSystemApplication.class, args);
    }
}
