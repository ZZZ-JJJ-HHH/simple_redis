package miniredis.core;

import miniredis.model.RedisObject;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class LRUCache {

    private final int capacity;

    private final LinkedHashMap<String, RedisObject> map;
    
    private final ConcurrentHashMap<String, Long> expireMap;

    public LRUCache(int capacity, ConcurrentHashMap<String, Long> expireMap) {
        this.capacity = capacity;
        this.expireMap = expireMap;

        // 使用LinkedHashMap实现LRU缓存
        this.map = new LinkedHashMap<>(capacity, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<String, RedisObject> eldest) {
                if (size() > capacity) {
                    String eldestKey = eldest.getKey();
                    expireMap.remove(eldestKey);  // 同步删除expireMap
                    return true;
                }
                return false;
            }
        };
    }

    public synchronized RedisObject get(String key) {
        return map.get(key);
    }

    public synchronized void put(String key, RedisObject value) {
        map.put(key, value);
    }


    public synchronized void remove(String key) {
        map.remove(key);
    }

    public synchronized boolean containsKey(String key) {
        return map.containsKey(key);
    }
}
