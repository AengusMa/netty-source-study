package com.mwl.ch11;

import io.netty.util.concurrent.DefaultEventExecutorChooserFactory;
import io.netty.util.concurrent.EventExecutor;

/**
 * @author mawenlong
 * @date 2019/1/31
 * @see DefaultEventExecutorChooserFactory#newChooser(EventExecutor[])
 */
public class Strategy {
    private Cache cacheMemory = new CacheMemoryImpl();
    private Cache cacheRedis = new CacheRedisImpl();

    public interface Cache {
        boolean add(String key, Object object);
    }

    public class CacheMemoryImpl implements Cache {
        @Override
        public boolean add(String key, Object object) {
            // 保存到map
            return false;
        }
    }

    public class CacheRedisImpl implements Cache {
        @Override
        public boolean add(String key, Object object) {
            // 保存到redis
            return false;
        }
    }

    public Cache getCache(String key) {
        //key的长度小于10使用redis
        if (key.length() < 10) {
            return cacheRedis;
        }
        //否则使用内存
        return cacheMemory;
    }
}
