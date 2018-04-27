package com.lmj.stone.lock;

/**
 * Created with IntelliJ IDEA.
 * Description: 分布式锁实现
 * User: lingminjun
 * Date: 2018-04-09
 * Time: 上午10:56
 */
public interface Lock {
    void lock(String key); //阻塞
    boolean trylock(String key, long timeOut); //返回true表示获取锁，阻塞到超时
    void unlock(String key);
}
