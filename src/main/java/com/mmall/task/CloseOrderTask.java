package com.mmall.task;

import com.mmall.common.Const;
import com.mmall.common.RedissonManager;
import com.mmall.dao.OrderMapper;
import com.mmall.service.IOrderService;
import com.mmall.util.PropertiesUtil;
import com.mmall.util.RedisShardedPoolUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class CloseOrderTask {

    @Autowired
    private IOrderService orderService;

    @Autowired
    private RedissonManager manager;

    /**
     * 原生的实现分布式redis锁
     * 定时关闭超时订单的任务，设为每一分钟执行一次，为保证在tomcat集群中，每到一个时间点，只由一个tomcat来执行，设置了redis锁机制。
     * 每个时间点，各tomcat竞争一个redis锁，竞争到锁的tomcat来执行任务。该redis锁利用了redis的setnx命令不创建重复键值的特性来实现.
     * 设置了多重防死锁机制，
     * 1.设置锁的值为当前时间点加上持有锁的最大时长，超过这个时长，其它进程就可以重置锁。这就防止了在锁未被释放和未能正确设置过期时间的情况下不会出现死锁；
     * 2.获得锁后设置锁的有效期，过期的锁自动释放；
     * 3.关单任务正确执行完成后直接释放锁，减少锁的持有时间；
     * 设置锁的有效期和最大持有时长要根据任务的执行时间来优化，在确保在持有锁的时间内关单任务能执行完成的前提下减少有效期和持有时间
     */
    @Scheduled(cron = "0 */10 * * * ?")
    public void closeOrderTaskV1() {
        log.info("关闭订单定时任务启动");
        long lockTimeout = Long.parseLong(PropertiesUtil.getProperty("redis_lock_timeout", "50000"));
        String lockKey = Const.RedisLock.CLOSE_ORDER_TASK_LOCK;
        Long lock = RedisShardedPoolUtil.setnx(lockKey, String.valueOf(System.currentTimeMillis() + lockTimeout));
        if (lock != null && lock.intValue() == 1) {
            log.info("获取redis锁成功:{}，ThreadName:{}", lockKey, Thread.currentThread().getName());
//            执行关单
            closeOrder(lockKey);
            log.info("任务结束，释放redis锁:{}，ThreadName:{}", lockKey, Thread.currentThread().getName());
        } else {
//            在获取锁失败后进一步检查是否是因为出现死锁，如果当前时间已经大于了锁的值，表示锁未被正确设置过期时间，这种情况在一台tomcat执行到已为锁赋值，
//              但在为锁设置过期时间之前被强制关闭时就会出现，这时锁就不会被释放，形成死锁，这种情况下，就可以重新为锁赋新值和过期时间，获得该锁
            String lockValueStr = RedisShardedPoolUtil.get(lockKey);
            if (lockValueStr != null && System.currentTimeMillis() > Long.parseLong(lockValueStr)) {
//                getset:赋新值，取旧值，重置未正确设置过期时间的锁
                String getsetValue = RedisShardedPoolUtil.getset(lockKey, String.valueOf(System.currentTimeMillis() + lockTimeout));
//                getsetValue == null表示在执行get之后，getset之前，锁被其它进程释放了，此时可以获得锁；
//                后一种情况表示get和getset之间，锁的状态没变，同时其它进程也没有为锁赋新值，此时也可以重新获得锁
                if (getsetValue == null || (getsetValue != null && StringUtils.equals(lockValueStr, getsetValue))) {
                    log.info("获取redis锁成功:{}，ThreadName:{}", lockKey, Thread.currentThread().getName());
                    closeOrder(lockKey);
                    log.info("任务结束，释放redis锁:{}，ThreadName:{}", lockKey, Thread.currentThread().getName());
                } else {
                    log.info("获取redis锁失败:{}，ThreadName:{}", lockKey, Thread.currentThread().getName());
                }
            } else {
                log.info("获取redis锁失败:{}，ThreadName:{}", lockKey, Thread.currentThread().getName());
            }
        }
    }

    /**
     * 为锁设置过期时间，执行关闭订单任务
     *
     * @param lockKey
     */
    private void closeOrder(String lockKey) {
//        设置锁的超时时间，防止死锁
        RedisShardedPoolUtil.expire(lockKey,
                Integer.parseInt(PropertiesUtil.getProperty("redis_lock_expire_time", "50")));
//        执行关单
        String payTimeDurationHour = PropertiesUtil.getProperty("pay_time_duration_hour", "2");
        orderService.closeOrder(Integer.parseInt(payTimeDurationHour));
//        释放锁
        RedisShardedPoolUtil.delete(lockKey);
    }

    /**
     * 使用Redisson框架来实现锁
     */
    @Scheduled(cron = "0 */10 * * * ?")
    public void closeOrderTaskV2() {
        String lockKey = Const.RedisLock.CLOSE_ORDER_TASK_LOCK;
        RLock lock = manager.getRedisson().getLock(lockKey);
        boolean getLock = false;
        try {
//            waitTime通常设为0，即tomcat未获得锁后不进行等待，直接退出方法，因为获得锁的tomcat执行关单的速度很快，可能在等待时间内就
//            执行完毕并释放锁了，这时等待的tomcat有一台就会重新获得锁并执行，这样同一时间点就出现了多个tomcat执行定时任务的情况，
//            而关单操作执行的时间是不好确定的，所以就直接把等待时间设为0
            if (getLock = lock.tryLock(0, 10, TimeUnit.SECONDS)) {
                log.info("Redisson获取到分布式锁：{},ThreadName:{}", lockKey, Thread.currentThread().getName());
                int payTimedurationHour = Integer.parseInt(PropertiesUtil.getProperty("pay_time_duration_hour","2"));
                orderService.closeOrder(payTimedurationHour);
            } else {
                log.info("Redisson未获取到分布式锁：{},ThreadName:{} ", lockKey, Thread.currentThread().getName());
            }

        } catch (InterruptedException e) {
            log.error("Redisson获取分布式锁异常", e);
        } finally {
//            未获得锁则退出
            if (!getLock) {
                return;
            }
//            获得锁最后要释放锁
            lock.unlock();
            log.info("Redisson已释放分布式锁：{}，ThreadName:{} ", lockKey, Thread.currentThread().getName());
        }
    }


//    private static final Logger LOGGER = LoggerFactory.getLogger();
//
//    private static void myTest1(){
//        String[] str = new String[]{"1","2","3"};
//        List list = Arrays.asList(str);
//        list.add("4");
//        for (int i = 0; i < str.length; i++) {
//            System.out.println(list.get(i));
//        }
//    }
//
//    private static void myTest2() {
//        List<String> list = new ArrayList<>();
//        list.add("1");
//        list.add("2");
//        list.add("3");
//        for (String str : list) {
//            if ("2".equals(str)) {
//                list.remove(str);
//            }
//        }
//        for (int i = 0; i < list.size(); i++) {
//            System.out.println(list.get(i));
//        }
//    }
//
//    public static void main(String[] args) {
//        myTest2();
//    }
}
