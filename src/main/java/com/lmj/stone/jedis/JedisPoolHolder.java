package com.lmj.stone.jedis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.util.SafeEncoder;

/**
 * Created with IntelliJ IDEA.
 * Description: 持有jedis pool
 * User: lingminjun
 * Date: 2018-04-10
 * Time: 下午11:10
 *
 <bean id="youJedisPoolHolder" class="com.lmj.stone.jedis.JedisPoolHolder" destroy-method="beanDestroy">
    <constructor-arg name="hosts" value="${default.jedis.hosts}"/>
    <constructor-arg name="port" value="${default.jedis.port}"/>
    <constructor-arg name="passWord" value="${default.jedis.password}"/>
    <constructor-arg name="maxTotal" value="${default.jedis.maxTotal}"/>
    <constructor-arg name="maxWait" value="${default.jedis.maxWait}"/>
    <constructor-arg name="minIdle" value="${default.jedis.minIdle}"/>
    <constructor-arg name="maxIdle" value="${default.jedis.maxIdle}"/>
    <constructor-arg name="testOnBorrow" value="${default.jedis.testOnBorrow}"/>
    <constructor-arg name="timeout" value="${default.jedis.timeout}"/>
 </bean>
 */
//@Component
public abstract class JedisPoolHolder {

    private static final Logger logger = LoggerFactory.getLogger(JedisPoolHolder.class);

    private JedisPool jedisPool;

    public JedisPoolHolder(@Value("default.jedis.hosts") String hosts,
                           @Value("default.jedis.port") int port,
                           @Value("default.jedis.passWord") String passWord,
                           @Value("default.jedis.maxTotal") int maxTotal,
                           @Value("default.jedis.maxWait") long maxWait,
                           @Value("default.jedis.minIdle") int minIdle,
                           @Value("default.jedis.maxIdle") int maxIdle,
                           @Value("default.jedis.testOnBorrow") boolean testOnBorrow,
                           @Value("default.jedis.timeout") int timeout) {

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
            this.jedisPool = new JedisPool(poolConfig, hosts, port, timeout, passWord);
        } else {
            this.jedisPool = new JedisPool(poolConfig, hosts, port, timeout);
        }
    }

    public Jedis getJedis() {
        return jedisPool.getResource();
    }

    public void releaseJedis(Jedis jedis) {
        if (null != jedis) {
            jedis.close();
        }
    }

    public boolean set(String key, String value) {
        return set(SafeEncoder.encode(key),SafeEncoder.encode(value));
    }

    public boolean set(String key,String value, int seconds){
        return set(SafeEncoder.encode(key),SafeEncoder.encode(value),seconds);
    }

    public String get(String key) {
        return SafeEncoder.encode(get(SafeEncoder.encode(key)));
    }

    public boolean del(String key) {
        return del(SafeEncoder.encode(key));
    }

    public boolean set(byte[] key, byte[] value) {
        Jedis jedis = null;
        try {
            jedis = getJedis();
            jedis.set(key, value);
        } catch (Throwable e) {
            logger.warn("jedis set exception!", e);
        } finally {
            releaseJedis(jedis);
        }
        return true;
    }

    public boolean set(byte[] key, byte[] value, int seconds){
        Jedis jedis = null;
        try {
            jedis = getJedis();
            jedis.setex(key, seconds, value);
        } catch (Throwable e) {
            logger.warn("jedis set exception!", e);
        } finally {
            releaseJedis(jedis);
        }
        return true;
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
            result = jedis.del(key);
        } catch (Throwable e) {
            logger.warn("jedis del exception!", e);
        } finally {
            releaseJedis(jedis);
        }
        return true;
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

}
