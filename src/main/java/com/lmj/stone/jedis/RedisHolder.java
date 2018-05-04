package com.lmj.stone.jedis;

import redis.clients.jedis.Jedis;
import redis.clients.util.SafeEncoder;

import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: lingminjun
 * Date: 2018-05-03
 * Time: 下午10:37
 *
 * 操作redis : https://www.jianshu.com/p/f957d8bcbdc5
 * docker redis : https://yeasy.gitbooks.io/docker_practice/content/container/attach_exec.html
 *
 *
 * 查看redis进程 ps aux|grep redis
 *
 * docker中操作指南
 * 先查看image
 * $docker image ls （是否存在redis的image，可省略）
 * $docker container ls -a
 * $docker run --publish 6379:6379 --name default-redis -d redis
 * $docker container ls
 *
 * 探测端口
 * $telnet 127.0.0.1 6379
 *
 *
 * 想进入查看可用
 * $docker exec -it 32d58c91abdc bash
 * $redis-cli -h 127.0.0.1 -p 6379
 * //做一些redis的操作
 * $shutdown
 */
public abstract class RedisHolder {

    public boolean set(String key, String value) {
        return set(SafeEncoder.encode(key),SafeEncoder.encode(value));
    }

    public boolean setnx(String key, String value) {
        return setnx(SafeEncoder.encode(key),SafeEncoder.encode(value));
    }

    public boolean set(String key,String value, int seconds){
        return set(SafeEncoder.encode(key),SafeEncoder.encode(value),seconds);
    }

    public String get(String key) {
        byte[] bytes = get(SafeEncoder.encode(key));
        if (bytes != null) {
            return SafeEncoder.encode(bytes);
        }
        return null;
    }

    public boolean del(String key) {
        return del(SafeEncoder.encode(key));
    }

    public long ttl(String key) {
        return ttl(SafeEncoder.encode(key));
    }

    public void hset(String key, String field, String value) {
        hset(SafeEncoder.encode(key), SafeEncoder.encode(field), SafeEncoder.encode(value));
    }

    public String hget(String key, String field) {
        byte[] bytes = hget(SafeEncoder.encode(key), field == null ? new byte[0] : SafeEncoder.encode(field));
        if (bytes != null) {
            return SafeEncoder.encode(bytes);
        }
        return null;
    }

    public boolean hsetnx(String key, String field, String value) {
        return this.hsetnx(SafeEncoder.encode(key), SafeEncoder.encode(field), SafeEncoder.encode(value));
    }

    public boolean hdel(String hkey, String key) {
        return hdel(SafeEncoder.encode(hkey), key == null ? new byte[0] : SafeEncoder.encode(key));
    }

    public Set<String> hkeys(String hkey) {
        Set<byte[]> keys = hkeys(SafeEncoder.encode(hkey));
        Set<String> ks = new HashSet<String>();
        for (byte[] bytes : keys) {
            ks.add(SafeEncoder.encode(bytes));
        }
        return ks;
    }

    public List<String> hvals(String hkey) {
        List<byte[]> values = hvals(SafeEncoder.encode(hkey));
        List<String> vs = new ArrayList<String>();
        for (byte[] bytes : values) {
            vs.add(SafeEncoder.encode(bytes));
        }
        return vs;
    }

    public Map<String, String> hgetAll(String hkey) {
        Map<byte[],byte[]> all = hgetAll(SafeEncoder.encode(hkey));
        Map<String, String> map = new HashMap<String, String>();
        for (byte[] bytes : all.keySet()) {
            byte[] value = all.get(bytes);
            map.put(SafeEncoder.encode(bytes),SafeEncoder.encode(value));
        }
        return map;
    }

    public long decrBy(String key, long integer) {
        return decrBy(SafeEncoder.encode(key),integer);
    }
    public long decr(String key) {
        return decr(SafeEncoder.encode(key));
    }
    public long incrBy(String key, long integer) {
        return incrBy(SafeEncoder.encode(key),integer);
    }
    public long incr(String key) {
        return incr(SafeEncoder.encode(key));
    }


    public abstract boolean set(byte[] key, byte[] value);
    public abstract boolean setnx(byte[] key, byte[] value);
    public abstract boolean set(byte[] key, byte[] value, int seconds);
    public abstract byte[] get(byte[] key);
    public abstract boolean del(byte[] key);

    public abstract byte[] hget(byte[] hkey, byte[] key);
    public abstract boolean hset(byte[] hkey, byte[] key, byte[] value);
    public abstract boolean hdel(byte[] hkey, byte[] key);
    public abstract Set<byte[]> hkeys(byte[] hkey);
    public abstract List<byte[]> hvals(byte[] hkey);
    public abstract Map<byte[], byte[]> hgetAll(byte[] hkey);
    public abstract long ttl(byte[] key);
    public abstract boolean hsetnx(byte[] key, byte[] field, byte[] value);

    public abstract long decrBy(byte[] key, long integer);
    public abstract long decr(byte[] key);
    public abstract long incrBy(byte[] key, long integer);
    public abstract long incr(byte[] key);


    private static final String OK_CODE = "OK";
    private static final String OK_MULTI_CODE = "+OK";

    protected static boolean isOk(String status) {
        return (status != null) && (OK_CODE.equals(status) || OK_MULTI_CODE.equals(status));
    }

    protected static boolean isOk(Long status) {
        return status != null && status >= 1;
    }

}
