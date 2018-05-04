package com.lmj.stone.jedis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * Description: 持有jedis pool
 * User: lingminjun
 * Date: 2018-04-10
 * Time: 下午11:10
 *
 <bean id="youJedisPoolHolder" class="com.lmj.stone.jedis.JedisPoolHolder" destroy-method="beanDestroy">
    <constructor-arg name="host" value="${default.redis.host}"/>
    <constructor-arg name="port" value="${default.redis.port}"/>
    <constructor-arg name="passWord" value="${default.redis.password}"/>
    <constructor-arg name="maxTotal" value="${default.redis.maxTotal}"/>
    <constructor-arg name="maxWait" value="${default.redis.maxWait}"/>
    <constructor-arg name="minIdle" value="${default.redis.minIdle}"/>
    <constructor-arg name="maxIdle" value="${default.redis.maxIdle}"/>
    <constructor-arg name="testOnBorrow" value="${default.redis.testOnBorrow}"/>
    <constructor-arg name="timeout" value="${default.redis.timeout}"/>
 </bean>
 */
//@Component
public abstract class JedisPoolHolder extends RedisHolder {

    private static final Logger logger = LoggerFactory.getLogger(JedisPoolHolder.class);

    private JedisPool jedisPool;

    public JedisPoolHolder(@Value("${default.redis.host}") String host,
                           @Value("${default.redis.port}") int port,
                           @Value("${default.redis.passWord}") String passWord,
                           @Value("${default.redis.maxTotal}") int maxTotal,
                           @Value("${default.redis.maxWait}") long maxWait,
                           @Value("${default.redis.minIdle}") int minIdle,
                           @Value("${default.redis.maxIdle}") int maxIdle,
                           @Value("${default.redis.testOnBorrow}") boolean testOnBorrow,
                           @Value("${default.redis.timeout}") int timeout) {

        JedisPoolConfig poolConfig = new JedisPoolConfig();
        //最大连接数, 默认20个
        poolConfig.setMaxTotal(maxTotal);
        //最小空闲连接数, 默认0
        poolConfig.setMinIdle(minIdle);
        //获取连接时的最大等待毫秒数(如果设置为阻塞时BlockWhenExhausted), 如果超时就抛异常, 小于零:阻塞不确定的时间, 默认 - 1
        poolConfig.setMaxWaitMillis(maxWait);
        //最大空闲连接数, 默认20个
        poolConfig.setMaxIdle(maxIdle);
        //在获取连接的时候检查有效性, 默认false
        poolConfig.setTestOnBorrow(testOnBorrow);
        poolConfig.setTestOnReturn(Boolean.FALSE);
        //在空闲时检查有效性, 默认false
        poolConfig.setTestWhileIdle(Boolean.TRUE);
        //逐出连接的最小空闲时间 默认1800000毫秒(30分钟)
        poolConfig.setMinEvictableIdleTimeMillis(1800000);
        //每次逐出检查时 逐出的最大数目 如果为负数就是: idleObjects.size / abs(n), 默认3
        poolConfig.setNumTestsPerEvictionRun(3);
        //对象空闲多久后逐出, 当空闲时间 > 该值 且 空闲连接>最大空闲数 时直接逐出, 不再根据MinEvictableIdleTimeMillis判断 (默认逐出策略)
        poolConfig.setSoftMinEvictableIdleTimeMillis(1800000);
        //逐出扫描的时间间隔(毫秒) 如果为负数, 则不运行逐出线程, 默认 - 1
        poolConfig.setTimeBetweenEvictionRunsMillis(60000);

        if (passWord != null && passWord.length() > 0) {
            this.jedisPool = new JedisPool(poolConfig, host, port, timeout, passWord);
        } else {
            this.jedisPool = new JedisPool(poolConfig, host, port, timeout);
        }
    }

    protected Jedis getJedis() {
        return jedisPool.getResource();
    }

    protected void releaseJedis(Jedis jedis) {
        if (null != jedis) {
            jedis.close();
        }
    }

    public boolean set(byte[] key, byte[] value) {
        Jedis jedis = null;
        try {
            jedis = getJedis();
            return isOk(jedis.set(key, value));
        } catch (Throwable e) {
            logger.warn("jedis set exception!", e);
        } finally {
            releaseJedis(jedis);
        }
        return false;
    }

    @Override
    public boolean setnx(byte[] key, byte[] value) {
        Jedis jedis = null;
        try {
            jedis = getJedis();
            return isOk(jedis.setnx(key, value));
        } catch (Throwable e) {
            logger.warn("jedis setnx exception!", e);
        } finally {
            releaseJedis(jedis);
        }
        return false;
    }

    public boolean set(byte[] key, byte[] value, int seconds){
        Jedis jedis = null;
        try {
            jedis = getJedis();
            return isOk(jedis.setex(key, seconds, value));
        } catch (Throwable e) {
            logger.warn("jedis set exception!", e);
        } finally {
            releaseJedis(jedis);
        }
        return false;
    }


    public byte[] get(byte[] key) {
        byte[] result = null;
        Jedis jedis = null;
        try {
            jedis = getJedis();
            result = jedis.get(key);
        } catch (Throwable e) {
            logger.warn("jedis get exception!", e);
        } finally {
            releaseJedis(jedis);
        }
        return result;
    }

    public boolean del(byte[] key) {
        long result = 0;
        Jedis jedis = null;
        try {
            jedis = getJedis();
            result = jedis.del(key); //特例，返回0表示也ok
        } catch (Throwable e) {
            logger.warn("jedis del exception!", e);
        } finally {
            releaseJedis(jedis);
        }
        return true;
    }

    @Override
    public byte[] hget(byte[] hkey, byte[] key) {
        byte[] result = null;
        Jedis jedis = null;
        try {
            jedis = getJedis();
            result = jedis.hget(hkey, key);
        } catch (Throwable e) {
            logger.warn("jedis hget exception!", e);
        } finally {
            releaseJedis(jedis);
        }
        return result;
    }

    @Override
    public boolean hset(byte[] hkey, byte[] key, byte[] value) {
        Jedis jedis = null;
        try {
            jedis = getJedis();
            return isOk(jedis.hset(hkey,key,value));
        } catch (Throwable e) {
            logger.warn("jedis hset exception!", e);
        } finally {
            releaseJedis(jedis);
        }
        return false;
    }

    @Override
    public boolean hdel(byte[] hkey, byte[] key) {
        long result = 0;
        Jedis jedis = null;
        try {
            jedis = getJedis();
            result = jedis.hdel(hkey,key); //特例，返回0表示也ok
        } catch (Throwable e) {
            logger.warn("jedis hdel exception!", e);
        } finally {
            releaseJedis(jedis);
        }
        return true;
    }

    @Override
    public Set<byte[]> hkeys(byte[] hkey) {
        Set<byte[]> result = null;
        Jedis jedis = null;
        try {
            jedis = getJedis();
            result = jedis.hkeys(hkey);
        } catch (Throwable e) {
            logger.warn("jedis hkeys exception!", e);
        } finally {
            releaseJedis(jedis);
        }
        return result;
    }

    @Override
    public List<byte[]> hvals(byte[] hkey) {
        List<byte[]> result = null;
        Jedis jedis = null;
        try {
            jedis = getJedis();
            result = jedis.hvals(hkey);
        } catch (Throwable e) {
            logger.warn("jedis hvals exception!", e);
        } finally {
            releaseJedis(jedis);
        }
        return result;
    }

    @Override
    public Map<byte[], byte[]> hgetAll(byte[] hkey) {
        Map<byte[], byte[]> result = null;
        Jedis jedis = null;
        try {
            jedis = getJedis();
            result = jedis.hgetAll(hkey);
        } catch (Throwable e) {
            logger.warn("jedis hgetAll exception!", e);
        } finally {
            releaseJedis(jedis);
        }
        return result;
    }

    @Override
    public long ttl(byte[] key) {
        Long result = -1l;
        Jedis jedis = null;
        try {
            jedis = getJedis();
            result = jedis.ttl(key);
            if (result == null) {
                result = -1l;
            }
        } catch (Throwable e) {
            logger.warn("jedis ttl exception!", e);
        } finally {
            releaseJedis(jedis);
        }
        return result;
    }

    @Override
    public boolean hsetnx(byte[] key, byte[] field, byte[] value) {
        Jedis jedis = null;
        try {
            jedis = getJedis();
            return isOk(jedis.hsetnx(key,field,value));
        } catch (Throwable e) {
            logger.warn("jedis hsetnx exception!", e);
        } finally {
            releaseJedis(jedis);
        }
        return false;
    }

    @Override
    public long decrBy(byte[] key, long integer) {
        Long result = 0l;
        Jedis jedis = null;
        try {
            jedis = getJedis();
            result = jedis.decrBy(key,integer);
            if (result == null) {
                result = 0l;
            }
        } catch (Throwable e) {
            logger.warn("jedis decrBy exception!", e);
        } finally {
            releaseJedis(jedis);
        }
        return result;
    }

    @Override
    public long decr(byte[] key) {
        Long result = 0l;
        Jedis jedis = null;
        try {
            jedis = getJedis();
            result = jedis.decr(key);
            if (result == null) {
                result = 0l;
            }
        } catch (Throwable e) {
            logger.warn("jedis decr exception!", e);
        } finally {
            releaseJedis(jedis);
        }
        return result;
    }

    @Override
    public long incrBy(byte[] key, long integer) {
        Long result = 0l;
        Jedis jedis = null;
        try {
            jedis = getJedis();
            result = jedis.incrBy(key,integer);
            if (result == null) {
                result = 0l;
            }
        } catch (Throwable e) {
            logger.warn("jedis incrBy exception!", e);
        } finally {
            releaseJedis(jedis);
        }
        return result;
    }

    @Override
    public long incr(byte[] key) {
        Long result = 0l;
        Jedis jedis = null;
        try {
            jedis = getJedis();
            result = jedis.incr(key);
            if (result == null) {
                result = 0l;
            }
        } catch (Throwable e) {
            logger.warn("jedis incr exception!", e);
        } finally {
            releaseJedis(jedis);
        }
        return result;
    }

    public void beanDestroy() {
        try {
            if (jedisPool != null) {
                jedisPool.close();
            }
        } catch (Throwable e) {
            logger.error("jedis pool release exception", e);
        }
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        beanDestroy();
    }
}
