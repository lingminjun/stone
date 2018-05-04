package com.lmj.stone.cache;

import com.lmj.stone.lang.LocalException;
import redis.clients.util.SafeEncoder;

import java.io.*;

/**
 * Created by lingminjun on 15/9/14.
 */
public abstract class RemoteCache {

    //返回结果，是否逻辑过期
    public final static class CacheHolder<T extends Serializable> {
        public final T obj;
        public final boolean isExpired; //是否已经过期，这里说的是逻辑过期，缓解 缓存并发 场景，主要靠加锁去DB取值（双key概念）
        public final boolean isEmpty;   //是否为空数据，解决 缓存穿透 场景，对空数据进行短暂缓存

        private CacheHolder(T obj, boolean expired, boolean empty) {
            this.obj = obj;
            this.isExpired = expired;
            this.isEmpty = empty;
        }
    }

    //简单存储
    public abstract void set(byte[] key, byte[] value);
    public abstract void set(byte[] key, byte[] value, int expire/* s */);
    public abstract byte[] get(byte[] key);
    public abstract void del(byte[] key);

    public void set(String key, String value) {
        set(SafeEncoder.encode(key),SafeEncoder.encode(value));
    }
    public void set(String key, String value, int expire/* s */) {
        set(SafeEncoder.encode(key),SafeEncoder.encode(value),expire);
    }
    public String get(String key) {
        byte[] bytes = get(SafeEncoder.encode(key));
        if (bytes != null) {
            return SafeEncoder.encode(bytes);
        }
        return null;
    }
    public void del(String key) {
        del(SafeEncoder.encode(key));
    }

    public <T extends Serializable> void set(String key, T value) {
        set(SafeEncoder.encode(key),SerializeUtil.serialize(value));
    }
    public <T extends Serializable> void set(String key, T value, int expire/* ms */) {
        set(SafeEncoder.encode(key),SerializeUtil.serialize(value),expire);
    }
    public <T extends Serializable> T get(String key, Class<T> type) {
        byte[] bytes = get(SafeEncoder.encode(key));
        if (bytes == null) {return null;}
        return (T)SerializeUtil.unserialize(bytes);
    }

    //逻辑失效(expire)和物理失效(invalid)，避免缓存击穿
    public <T extends Serializable> void set(String key, T value, int expire/* s */, int invalid /* s */) {
        if (value == null) {return;}
        CacheItem<T> item = new CacheItem<T>(value,expire + System.currentTimeMillis(),false);
        set(key,item,invalid);
    }

    public <T extends Serializable> void setEmpty(String key, int expire/* s */, int invalid /* s */) {
        CacheItem<T> item = new CacheItem<T>(null,expire + System.currentTimeMillis(),true);
        set(key,item,invalid);
    }

    public <T extends Serializable> CacheHolder<T> access(String key, Class<T> type) {
        CacheItem<T> item = get(key,CacheItem.class);
        if (item != null) {
            return new CacheHolder<T>(item.obj,item.expire <= System.currentTimeMillis(),item.isEmpty);
        } else {
            return new CacheHolder<T>(null,true,true);
        }
    }

    private final static class CacheItem<T extends Serializable> implements Serializable {
        public final T obj;
        public final long expire; //逻辑过期失效
        public final boolean isEmpty; //逻辑过期失效

        private CacheItem(T obj, long expire, boolean empty) {
            this.obj = obj;
            this.expire = expire;
            this.isEmpty = empty;
        }
    }

    private final static class SerializeUtil {

        public static byte[] serialize(Object object) {
            ObjectOutputStream oos = null;
            ByteArrayOutputStream baos = null;

            try {
                baos = new ByteArrayOutputStream();
                oos = new ObjectOutputStream(baos);

                oos.writeObject(object);

                return baos.toByteArray();
            } catch (Throwable e) {
                throw new LocalException("序列化 失败","STONE",-100,e);
            } finally {
                if (baos != null) {
                    try {
                        baos.close();
                    } catch (Throwable e) {
                        e.printStackTrace();
                    }
                }
                if (oos != null) {
                    try {
                        oos.close();
                    } catch (Throwable e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        public static Object unserialize(byte[] bytes) {

            ByteArrayInputStream bais = null;
            ObjectInputStream ois = null;
            try {
                bais = new ByteArrayInputStream(bytes);
                ois = new ObjectInputStream(bais);

                return ois.readObject();

            } catch (Throwable e) {
                throw new LocalException("反序列化 失败", "STONE", -100, e);
            } finally {
                if (ois != null) {
                    try {
                        ois.close();
                    } catch (Throwable e) {
                        e.printStackTrace();
                    }
                }
                if (bais != null) {
                    try {
                        bais.close();
                    } catch (Throwable e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}
