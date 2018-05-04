package com.lmj.stone.cache;

import com.lmj.stone.jedis.RedisHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;


/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: lingminjun
 * Date: 2018-04-11
 * Time: 上午10:01
 */
//@Component(value = "xxxxx")
public abstract class JedisCache extends RemoteCache {

    @Autowired(required = true) //加载默认的JedisPool
    @Qualifier("cacheRedis")
    RedisHolder redisHolder;

    @Override
    public void set(byte[] key, byte[] value) {
        redisHolder.set(key,value);
    }

    @Override
    public void set(byte[] key, byte[] value, int expire) {
        redisHolder.set(key,value,expire);
    }

    @Override
    public byte[] get(byte[] key) {
        return redisHolder.get(key);
    }

    @Override
    public void del(byte[] key) {
        redisHolder.del(key);
    }
}
