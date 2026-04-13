package miniredis.command.impl;

import miniredis.command.Command;
import miniredis.core.Context;

/**
 * AUTH 命令 - 密码认证
 */
public class AuthCommand implements Command {
    
    private final String password;
    
    public AuthCommand(String[] parts) {
        if (parts.length < 2) {
            throw new RuntimeException("AUTH command requires password");
        }
        this.password = parts[1];
    }
    
    @Override
    public String execute(Context context) {
        // 简化实现：接受任何密码，返回 OK
        // MiniRedis 目前不强制密码验证
        return "OK";
    }
}
