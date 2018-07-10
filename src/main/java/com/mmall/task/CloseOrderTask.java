package com.mmall.task;

import com.mmall.common.Const;
import com.mmall.service.IOrderService;
import com.mmall.util.PropertiesUtil;
import com.mmall.util.RedisShardedPoolUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class CloseOrderTask {

    @Autowired
    private IOrderService orderService;

    /**
     * 定时关闭超时订单的任务，设为每一分钟执行一次，为保证在tomcat集群中，每到一个时间点，只由一个tomcat来执行，设置了redis锁机制。
     * 每个时间点，各tomcat竞争一个redis锁，竞争到锁的tomcat来执行任务。该redis锁利用了redis的setnx命令不创建重复键值的特性来实现.
     * 设置了多重防死锁机制，
     * 1.设置锁的值为当前时间点加上持有锁的最大时长，超过这个时长，其它进程就可以重置锁。这就防止了在锁未被释放和未能正确设置过期时间的情况下不会出现死锁；
     * 2.获得锁后设置锁的有效期，过期的锁自动释放；
     * 3.关单任务正确执行完成后直接释放锁，减少锁的持有时间；
     * 设置锁的有效期和最大持有时长要根据任务的执行时间来优化，在确保在持有锁的时间内关单任务能执行完成的前提下减少有效期和持有时间
     */
    @Scheduled(cron = "0 */1 * * * ?")
    public void closeOrderTask() {
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
}
