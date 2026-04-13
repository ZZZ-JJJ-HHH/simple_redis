package miniredis.command.impl;

import miniredis.command.Command;
import miniredis.core.Context;

import java.util.List;

/**
 * SCAN 命令 - 扫描当前数据库的所有键
 * 简化实现：一次性返回所有键
 */
public class ScanCommand implements Command {
    
    public ScanCommand(String[] parts) {
        // 暂不处理参数
    }
    
    @Override
    public String execute(Context context) {
        // 返回特殊对象，RespEncoder 会识别并直接写入
        return "__RESP_RAW__:*2\r\n$1\r\n0\r\n" + buildKeysArray(context);
    }
    
    private String buildKeysArray(Context context) {
        List<String> keys = context.getCurrentDatabase().getAllKeys();
        StringBuilder sb = new StringBuilder();
        sb.append("*").append(keys.size()).append("\r\n");
        
        for (String key : keys) {
            sb.append("$").append(key.length()).append("\r\n");
            sb.append(key).append("\r\n");
        }
        
        return sb.toString();
    }
}
