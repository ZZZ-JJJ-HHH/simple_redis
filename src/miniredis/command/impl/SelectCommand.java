package miniredis.command.impl;

import miniredis.command.Command;
import miniredis.core.Context;
import miniredis.model.RedisObject;

import java.util.List;

/**
 * SELECT 命令 - 切换数据库
 */
public class SelectCommand implements Command {
    
    private final int dbIndex;
    
    public SelectCommand(String[] parts) {
        if (parts.length < 2) {
            throw new RuntimeException("SELECT command requires database index");
        }
        this.dbIndex = Integer.parseInt(parts[1]);
    }
    
    @Override
    public String execute(Context context) {
        context.selectDatabase(dbIndex);
        return "OK";
    }
}
