package miniredis.command.impl;

import miniredis.command.Command;
import miniredis.core.Context;

/**
 * EXISTS 命令 - 检查键是否存在
 */
public class ExistsCommand implements Command {
    
    private final String key;
    
    public ExistsCommand(String[] parts) {
        if (parts.length < 2) {
            throw new RuntimeException("EXISTS command requires key");
        }
        this.key = parts[1];
    }
    
    @Override
    public String execute(Context context) {
        boolean exists = context.getStore().containsKey(key);
        return exists ? ":1" : ":0";
    }
}
