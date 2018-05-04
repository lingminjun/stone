package com.lmj.stone.lock;

import com.lmj.stone.jedis.RedisHolder;
import com.lmj.stone.lang.LocalException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: lingminjun
 * Date: 2018-04-09
 * Time: 上午11:07
 */
//@Component
public abstract class DistributedLock implements Lock {

    @Autowired(required = true) //加载默认的JedisPool
    @Qualifier("lockRedis")
    RedisHolder redisHolder;

    @Override
    public void lock(String key) {
        while(true){
            if (redisHolder.setnx(key,"1")){
                System.out.println(Thread.currentThread().getName() + " get lock....");
                return;
            }
            System.out.println(Thread.currentThread().getName() + " is trying lock....");
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                throw new LocalException("加锁失败","STONE",-100,e);
            }
        }
    }

    @Override
    public boolean trylock(String key, long timeOut) {
        long expired = System.currentTimeMillis() + timeOut;
        while(true){
            if (redisHolder.setnx(key,"1")){
                System.out.println(Thread.currentThread().getName() + " get lock....");
                return true;
            }
            System.out.println(Thread.currentThread().getName() + " is trying lock....");
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                throw new LocalException("加锁失败","STONE",-100,e);
            }

            if (System.currentTimeMillis() > expired) {
                System.out.println(Thread.currentThread().getName() + " trying lock timeout....");
                return false;
            }
        }
    }

    @Override
    public void unlock(String key) {
        redisHolder.del(key);
//        if (flag > 0){
            System.out.println(Thread.currentThread().getName() + " release lock....");
//        }else {
//            System.out.println(Thread.currentThread().getName() + " release lock too....");
//        }
    }

}
