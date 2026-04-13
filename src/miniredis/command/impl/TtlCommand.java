package miniredis.command.impl;

import miniredis.command.Command;
import miniredis.core.Context;

/**
 * TTL 命令 - 返回键的剩余生存时间（秒）
 */
public class TtlCommand implements Command {
    
    private final String key;
    
    public TtlCommand(String[] parts) {
        if (parts.length < 2) {
            throw new RuntimeException("TTL command requires key");
        }
        this.key = parts[1];
    }
    
    @Override
    public String execute(Context context) {
        Long expireTime = context.getExpireMap().get(key);
        
        if (expireTime == null) {
            // 键存在但没有过期时间，返回 -1
            if (context.getStore().containsKey(key)) {
                return "(integer) -1";
            }
            // 键不存在，返回 -2
            return "(integer) -2";
        }
        
        // 计算剩余秒数
        long remaining = expireTime - System.currentTimeMillis();
        if (remaining <= 0) {
            return "(integer) -2";  // 已过期
        }
        
        return "(integer) " + (remaining / 1000);  // 转换为秒
    }
}
