package miniredis.command.impl;

import miniredis.command.Command;
import miniredis.core.Context;

/**
 * DEL 命令 - 删除键
 */
public class DelCommand implements Command {
    
    private final String key;
    
    public DelCommand(String[] parts) {
        if (parts.length < 2) {
            throw new RuntimeException("DEL command requires key");
        }
        this.key = parts[1];
    }
    
    @Override
    public String execute(Context context) {
        boolean existed = context.getStore().containsKey(key);
        
        if (existed) {
            // 从 store 中删除
            context.getStore().remove(key);
            // 从 expireMap 中删除
            context.getExpireMap().remove(key);
            return ":1";  // 返回 1 表示删除成功
        }
        
        return ":0";  // 返回 0 表示键不存在
    }
}
