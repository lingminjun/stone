package com.lmj.stone.cache;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializeFilter;
import com.lmj.stone.lang.LocalException;
import redis.clients.util.SafeEncoder;

import java.io.*;

/**
 * Created by lingminjun on 15/9/14.
 */
public abstract class RemoteCache {

    //返回结果，是否逻辑过期
    public final static class CacheHolder<T> {
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
    public abstract boolean set(byte[] key, byte[] value);
    public abstract boolean set(byte[] key, byte[] value, int expire/* s */);
    public abstract byte[] get(byte[] key);
    public abstract boolean del(byte[] key);

    public boolean set(String key, String value) {
        return set(SafeEncoder.encode(key),SafeEncoder.encode(value));
    }
    public boolean set(String key, String value, int expire/* s */) {
        return set(SafeEncoder.encode(key),SafeEncoder.encode(value),expire);
    }
    public final String get(String key) {
        byte[] bytes = get(SafeEncoder.encode(key));
        if (bytes != null) {
            return SafeEncoder.encode(bytes);
        }
        return null;
    }
    public boolean del(String key) {
        return del(SafeEncoder.encode(key));
    }

    public <T extends Serializable> boolean set(String key, T value) {
        return set(SafeEncoder.encode(key),SerializeUtil.serialize(value));
    }
    public <T extends Serializable> boolean set(String key, T value, int expire/* s */) {
        if (expire <= 0) {
            return set(SafeEncoder.encode(key),SerializeUtil.serialize(value));
        } else {
            return set(SafeEncoder.encode(key),SerializeUtil.serialize(value),expire);
        }
    }
    public <T extends Serializable> T get(String key, Class<T> type) {
        byte[] bytes = get(SafeEncoder.encode(key));
        if (bytes == null) {return null;}
        Object obj = SerializeUtil.unserialize(bytes);
        if (obj.getClass() == type) {
            return (T)obj;
        } else if (obj instanceof CacheItem) {
            CacheItem item = (CacheItem)obj;
            if (item.isEmpty) {
                return null;
            } else if (item.expire > 0 && item.expire <= ((int)(System.currentTimeMillis()/1000))) {//过期
                return null;
            } else {
                return (T)item.obj;
            }
        } else {
            return (T)obj;
        }
    }

    //逻辑失效(expire)和物理失效(invalid)，避免缓存击穿
    public <T extends Serializable> boolean set(String key, T value, int expire/* s */, int invalid /* s */) {
        if (value == null) {return false;}
        CacheItem<T> item = new CacheItem<T>(value,expire <= 0 ? 0 : (expire + ((int)(System.currentTimeMillis()/1000))),false);
        return set(key,item,invalid);
    }

    public <T extends Serializable> boolean setEmpty(String key, int expire/* s */, int invalid /* s */) {
        CacheItem<T> item = new CacheItem<T>(null,expire <= 0 ? 30 : (expire + ((int)(System.currentTimeMillis()/1000))),true);
        return set(key,item,invalid);
    }

    public <T extends Serializable> CacheHolder<T> access(String key, Class<T> type) {
        CacheItem<T> item = get(key,CacheItem.class);
        if (item != null) {
            return new CacheHolder<T>(item.obj,item.expire > 0 && item.expire <= ((int)(System.currentTimeMillis()/1000)),item.isEmpty);
        } else {
            return new CacheHolder<T>(null,true,true);
        }
    }

    // json数据支持，采用fastjson编码和解码
    public <T extends Object> boolean setJSON(String key, T value) {
        return set(SafeEncoder.encode(key), JSON.toJSONBytes(value,emptyFilters));
    }
    public <T extends Object> boolean setJSON(String key, T value, int expire/* s */) {
        if (expire <= 0) {
            return set(SafeEncoder.encode(key), JSON.toJSONBytes(value,emptyFilters));
        } else {
            return set(SafeEncoder.encode(key), JSON.toJSONBytes(value, emptyFilters), expire);
        }
    }
    public <T extends Object> T getJSON(String key, Class<T> type) {
        byte[] bytes = get(SafeEncoder.encode(key));
        if (bytes == null) {return null;}
        Object obj = JSON.parseObject(bytes,type);
        if (obj.getClass() == type) {
            return (T)obj;
        } else if (obj instanceof JsonItem) {
            JsonItem item = (JsonItem)obj;
            if (item.isEmpty) {
                return null;
            } else if (item.expire > 0 && item.expire <= ((int)(System.currentTimeMillis()/1000))) {//过期
                return null;
            } else if (item.json != null) {
                return JSON.parseObject(item.json,type);
            } else {
                return (T)obj;
            }
        } else {
            return (T)obj;
        }
    }

    //逻辑失效(expire)和物理失效(invalid)，避免缓存击穿
    public <T extends Object> boolean setJSON(String key, T value, int expire/* s */, int invalid /* s */) {
        if (value == null) {return false;}
        JsonItem item = new JsonItem(value,expire <= 0 ? 0 : (expire + ((int)(System.currentTimeMillis()/1000))),false);
        return setJSON(key,item,invalid);
    }

    public <T extends Object> boolean setJSONEmpty(String key, int expire/* s */, int invalid /* s */) {
        JsonItem item = new JsonItem(null,expire <= 0 ? 30 : (expire + ((int)(System.currentTimeMillis()/1000))),true);
        return setJSON(key,item,invalid);
    }

    public <T extends Object> CacheHolder<T> accessJSON(String key, Class<T> type) {
        JsonItem item = getJSON(key,JsonItem.class);
        if (item != null) {
            return new CacheHolder<T>(item.json != null ? JSON.parseObject(item.json,type) : null,item.expire > 0 && item.expire <= ((int)(System.currentTimeMillis()/1000)),item.isEmpty);
        } else {
            return new CacheHolder<T>(null,true,true);
        }
    }

    private static final SerializeFilter[] emptyFilters = new SerializeFilter[0];

    private final static class CacheItem<T> implements Serializable {
        private static final long serialVersionUID = 1L;
        public final T obj;
        public final int expire; //逻辑过期失效
        public final boolean isEmpty; //逻辑过期失效

        private CacheItem(T obj, int expire, boolean empty) {
            this.obj = obj;
            this.expire = expire;
            this.isEmpty = empty;
        }
    }

    public final static class JsonItem implements Serializable {
        private static final long serialVersionUID = 1L;
        public String json;
        public int expire; //逻辑过期失效
        public boolean isEmpty; //逻辑过期失效

        public JsonItem() { }

        private JsonItem(Object obj, int expire, boolean empty) {
            this.json = obj == null ? null : JSON.toJSONString(obj);
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
