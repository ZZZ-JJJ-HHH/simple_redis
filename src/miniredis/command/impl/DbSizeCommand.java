package miniredis.command.impl;

import miniredis.command.Command;
import miniredis.core.Context;

/**
 * DBSIZE 命令 - 返回当前数据库的键数量
 */
public class DbSizeCommand implements Command {
    
    @Override
    public String execute(Context context) {
        int size = context.getCurrentDatabase().getAllKeys().size();
        // 返回特殊标记，表示这是原始 RESP 整数格式
        return "__RESP_RAW__::" + size;
    }
}
