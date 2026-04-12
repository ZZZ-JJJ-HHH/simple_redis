package miniredis.core;

import lombok.Getter;
import miniredis.config.ConfigManager;

import java.util.concurrent.ConcurrentHashMap;

@Getter
public class Context {

//    private ConcurrentHashMap<String, RedisObject> store = new ConcurrentHashMap<>();

    private LRUCache store;
    // 新增：过期时间（毫秒时间戳）
    private ConcurrentHashMap<String, Long> expireMap;

    public Context() {
        this.expireMap = new ConcurrentHashMap<>();
        // 从配置文件读取LRU缓存容量
        int capacity = ConfigManager.getInt("lru.cache.capacity", 100);
        this.store = new LRUCache(capacity, this.expireMap);
    }
}
