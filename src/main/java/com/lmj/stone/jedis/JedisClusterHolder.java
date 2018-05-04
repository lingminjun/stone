package com.lmj.stone.jedis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import redis.clients.jedis.*;

import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * Description: 持有jedis pool
 * User: lingminjun
 * Date: 2018-04-10
 * Time: 下午11:10
 *
 <bean id="youJedisClusterHolder" class="com.lmj.stone.jedis.JedisClusterHolder" destroy-method="beanDestroy">
    <constructor-arg name="hosts" value="${default.redis.cluster.hosts}"/>
    <constructor-arg name="passWord" value="${default.redis.cluster.password}"/>
    <constructor-arg name="maxTotal" value="${default.redis.cluster.maxTotal}"/>
    <constructor-arg name="maxWait" value="${default.redis.cluster.maxWait}"/>
    <constructor-arg name="minIdle" value="${default.redis.cluster.minIdle}"/>
    <constructor-arg name="maxIdle" value="${default.redis.cluster.maxIdle}"/>
    <constructor-arg name="testOnBorrow" value="${default.redis.cluster.testOnBorrow}"/>
    <constructor-arg name="timeout" value="${default.redis.cluster.timeout}"/>
    <constructor-arg name="maxRedirection" value="${default.redis.cluster.maxRedirection}"/>
 </bean>
 */
//@Component
public abstract class JedisClusterHolder extends RedisHolder {

    private static final Logger logger = LoggerFactory.getLogger(JedisClusterHolder.class);

    private JedisCluster jedisCluster;

    public JedisClusterHolder(@Value("${default.redis.cluster.hosts}") String hosts,
                              @Value("${default.redis.cluster.passWord}") String passWord,
                              @Value("${default.redis.cluster.maxTotal}") int maxTotal,
                              @Value("${default.redis.cluster.maxWait}") long maxWait,
                              @Value("${default.redis.cluster.minIdle}") int minIdle,
                              @Value("${default.redis.cluster.maxIdle}") int maxIdle,
                              @Value("${default.redis.cluster.testOnBorrow}") boolean testOnBorrow,
                              @Value("${default.redis.cluster.timeout}") int timeout,
                              @Value("${default.redis.cluster.maxRedirection}") int maxRedirection) {

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

        Set<HostAndPort> nodes = getJedisClusterNode(hosts);
        if (passWord != null && passWord.length() > 0) {
            this.jedisCluster = new JedisCluster(nodes, timeout, timeout, maxRedirection, passWord, poolConfig);
        } else {
            this.jedisCluster = new JedisCluster(nodes, timeout, maxRedirection, poolConfig);
        }
//        jedisCluster.hgetAll()
//        jedisCluster.hgetAll((byte[])null)
    }

    private Set<HostAndPort> getJedisClusterNode(String redisServerHosts) {
        Set<HostAndPort> nodes = new HashSet<HostAndPort>();
        String[] hostAndIps = redisServerHosts.split(",|;");
        for (String hostAndIp : hostAndIps) {
            String[] strs = hostAndIp.split(":");
            nodes.add(new HostAndPort(strs[0], Integer.parseInt(strs[1])));
        }
        return nodes;
    }

    protected JedisCluster getJedisCluster() {
        return jedisCluster;
    }


    public boolean set(byte[] key, byte[] value) {
        try {
            return isOk(jedisCluster.set(key, value));
        } catch (Throwable e) {
            logger.warn("jedis set exception!", e);
        }
        return false;
    }

    @Override
    public boolean setnx(byte[] key, byte[] value) {
        try {
            return isOk(jedisCluster.setnx(key, value));
        } catch (Throwable e) {
            logger.warn("jedis set exception!", e);
        }
        return false;
    }

    public boolean set(byte[] key, byte[] value, int seconds){
        try {
            return isOk(jedisCluster.setex(key, seconds, value));
        } catch (Throwable e) {
            logger.warn("jedis set exception!", e);
        }
        return false;
    }


    public byte[] get(byte[] key) {
        byte[] result = null;
        try {
            result = jedisCluster.get(key);
        } catch (Throwable e) {
            logger.warn("jedis get exception!", e);
        }
        return result;
    }

    public boolean del(byte[] key) {
        long result = 0;
        try {
            result = jedisCluster.del(key);
        } catch (Throwable e) {
            logger.warn("jedis del exception!", e);
        }
        return true;
    }

    @Override
    public byte[] hget(byte[] hkey, byte[] key) {
        byte[] result = null;
        try {
            result = jedisCluster.hget(hkey,key);
        } catch (Throwable e) {
            logger.warn("jedis hget exception!", e);
        }
        return result;
    }

    @Override
    public boolean hset(byte[] hkey, byte[] key, byte[] value) {
        try {
            return isOk(jedisCluster.hset(hkey, key, value));
        } catch (Throwable e) {
            logger.warn("jedis hset exception!", e);
        }
        return false;
    }

    @Override
    public boolean hdel(byte[] hkey, byte[] key) {
        long result = 0;
        try {
            result = jedisCluster.hdel(hkey,key);
        } catch (Throwable e) {
            logger.warn("jedis hdel exception!", e);
        }
        return true;
    }

    @Override
    public Set<byte[]> hkeys(byte[] hkey) {
        Set<byte[]> result = null;
        try {
            result = jedisCluster.hkeys(hkey);
        } catch (Throwable e) {
            logger.warn("jedis hkeys exception!", e);
        }
        return result;
    }

    @Override
    public List<byte[]> hvals(byte[] hkey) {
        List<byte[]> result = null;
        try {
            Collection<byte[]> collection = jedisCluster.hvals(hkey);
            if (collection != null) {
                result = new ArrayList<byte[]>(collection);
            }
        } catch (Throwable e) {
            logger.warn("jedis hvals exception!", e);
        }
        return result;
    }

    @Override
    public Map<byte[], byte[]> hgetAll(byte[] hkey) {
        Map<byte[], byte[]> result = null;
        try {
            result = jedisCluster.hgetAll(hkey);
        } catch (Throwable e) {
            logger.warn("jedis hgetAll exception!", e);
        }
        return result;
    }

    @Override
    public long ttl(byte[] key) {
        Long result = -1l;
        try {
            result = jedisCluster.ttl(key);
            if (result == null) {
                result = -1l;
            }
        } catch (Throwable e) {
            logger.warn("jedis ttl exception!", e);
        }
        return result;
    }

    @Override
    public boolean hsetnx(byte[] key, byte[] field, byte[] value) {
        try {
            return isOk(jedisCluster.hsetnx(key,field,value));
        } catch (Throwable e) {
            logger.warn("jedis hsetnx exception!", e);
        }
        return false;
    }

    @Override
    public long decrBy(byte[] key, long integer) {
        Long result = 0l;
        try {
            result = jedisCluster.decrBy(key,integer);
            if (result == null) {
                result = 0l;
            }
        } catch (Throwable e) {
            logger.warn("jedis decrBy exception!", e);
        }
        return result;
    }

    @Override
    public long decr(byte[] key) {
        Long result = 0l;
        try {
            result = jedisCluster.decr(key);
            if (result == null) {
                result = 0l;
            }
        } catch (Throwable e) {
            logger.warn("jedis decr exception!", e);
        }
        return result;
    }

    @Override
    public long incrBy(byte[] key, long integer) {
        Long result = 0l;
        try {
            result = jedisCluster.incrBy(key,integer);
            if (result == null) {
                result = 0l;
            }
        } catch (Throwable e) {
            logger.warn("jedis incrBy exception!", e);
        }
        return result;
    }

    @Override
    public long incr(byte[] key) {
        Long result = 0l;
        try {
            result = jedisCluster.incr(key);
            if (result == null) {
                result = 0l;
            }
        } catch (Throwable e) {
            logger.warn("jedis incr exception!", e);
        }
        return result;
    }

    public void beanDestroy() {
        try {
            if (jedisCluster != null) {
                jedisCluster.close();
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
