package com.lmj.stone.cache;

import com.lmj.stone.jedis.JedisPoolHolder;
import org.springframework.beans.factory.annotation.Autowired;


/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: lingminjun
 * Date: 2018-04-11
 * Time: 上午10:01
 */
//@Component
public class JedisCache extends RemoteCache {

    @Autowired(required = true) //加载默认的JedisPool
    JedisPoolHolder jedisPoolHolder;

    @Override
    public void set(byte[] key, byte[] value) {
        jedisPoolHolder.set(key,value);
    }

    @Override
    public void set(byte[] key, byte[] value, int expire) {
        jedisPoolHolder.set(key,value,expire);
    }

    @Override
    public byte[] get(byte[] key) {
        return jedisPoolHolder.get(key);
    }

    @Override
    public void del(byte[] key) {
        jedisPoolHolder.del(key);
    }
}
