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
    public boolean set(byte[] key, byte[] value) {
        return redisHolder.set(key,value);
    }

    @Override
    public boolean set(byte[] key, byte[] value, int expire) {
        return redisHolder.set(key,value,expire);
    }

    @Override
    public byte[] get(byte[] key) {
        return redisHolder.get(key);
    }

    @Override
    public boolean del(byte[] key) {
        return redisHolder.del(key);
    }
}
