package miniredis.command.impl;

import miniredis.command.Command;
import miniredis.core.Context;
import miniredis.model.RedisObject;

/**
 * TYPE 命令 - 返回键的值类型
 */
public class TypeCommand implements Command {
    
    private final String key;
    
    public TypeCommand(String[] parts) {
        if (parts.length < 2) {
            throw new RuntimeException("TYPE command requires key");
        }
        this.key = parts[1];
    }
    
    @Override
    public String execute(Context context) {
        RedisObject obj = context.getStore().get(key);
        
        if (obj == null) {
            return "none";
        }
        
        // 返回小写的类型名称
        return obj.getType().name().toLowerCase();
    }
}
