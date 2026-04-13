package miniredis.command.impl;

import miniredis.command.Command;
import miniredis.core.Context;

/**
 * 通用命令 - 返回固定的响应字符串
 * 用于实现 CLIENT、CONFIG、INFO 等管理命令
 */
public class GenericCommand implements Command {
    
    private final String response;
    
    public GenericCommand(String response) {
        this.response = response;
    }
    
    @Override
    public String execute(Context context) {
        return response;
    }
}
