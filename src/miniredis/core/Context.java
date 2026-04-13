package miniredis.core;

import lombok.Getter;
import miniredis.config.ConfigManager;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Getter
public class Context {

    // 多个数据库，默认 16 个
    private List<Database> databases;
    private int selectedDb = 0;  // 当前选中的数据库索引（固定为 0）
    
    public Context() {
        int dbCount = 16;  // 默认 16 个数据库
        this.databases = new ArrayList<>(dbCount);
        for (int i = 0; i < dbCount; i++) {
            this.databases.add(new Database());
        }
    }
    
    // 获取当前选中的数据库（始终返回 db0）
    public Database getCurrentDatabase() {
        return databases.get(0);  // 固定返回 db0
    }
    
    // 切换数据库（暂时不支持，始终在 db0）
    public void selectDatabase(int index) {
        // 暂不实现多数据库切换，所有操作都在 db0
        // if (index >= 0 && index < databases.size()) {
        //     this.selectedDb = index;
        // }
    }
    
    // 兼容旧代码的方法（已废弃，建议使用 getCurrentDatabase())
    @Deprecated
    public LRUCache getStore() {
        return getCurrentDatabase().getStore();
    }
    
    @Deprecated
    public ConcurrentHashMap<String, Long> getExpireMap() {
        return getCurrentDatabase().getExpireMap();
    }
    
    // 内部类：单个数据库
    @Getter
    public static class Database {
        private LRUCache store;
        private ConcurrentHashMap<String, Long> expireMap;
        
        public Database() {
            this.expireMap = new ConcurrentHashMap<>();
            int capacity = ConfigManager.getInt("lru.cache.capacity", 100);
            this.store = new LRUCache(capacity, this.expireMap);
        }
        
        // 获取所有键
        public List<String> getAllKeys() {
            return new ArrayList<>(store.getCache().keySet());
        }
    }
}
