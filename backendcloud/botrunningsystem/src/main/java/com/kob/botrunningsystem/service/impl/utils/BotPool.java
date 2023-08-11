package com.kob.botrunningsystem.service.impl.utils;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class BotPool extends Thread{

    private final ReentrantLock lock = new ReentrantLock();

    private Condition condition = lock.newCondition();

    private Queue<Bot> bots = new LinkedList<>();

    public void addBot(Integer userId, String botCode, String input){
        lock.lock();
        try {
            bots.add(new Bot(userId, botCode, input));
            condition.signalAll();
        }finally {
            lock.unlock();
        }
    }

    private void consume(Bot bot){
        Consumer consumer = new Consumer();
        consumer.startTimeout(2000, bot);

    }

    @Override
    public void run() {
        while(true){
            lock.lock();
            if(bots.isEmpty()){//如果队列为空，线程将会被阻塞。当addBot()被调用，队列中添加新的任务时，线程将会被唤醒
                try {
                    condition.await();// 若执行了await会自动释放锁//如果任务队列为空，就要将其阻塞，当有任务出现时，就要发生信号量，将其唤醒。因此需要用到条件变量。
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    lock.unlock();
                    break;
                }
            }else{
                Bot bot = bots.remove();//返回并删除对头
                lock.unlock();
                consume(bot);//耗时,执行几s，执行代码
            }
        }
    }
}
