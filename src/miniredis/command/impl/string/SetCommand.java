package miniredis.command.impl.string;

import lombok.AllArgsConstructor;
import miniredis.backup.AOFManager;
import miniredis.command.Command;
import miniredis.core.Context;
import miniredis.model.DataType;
import miniredis.model.RedisObject;

@AllArgsConstructor
public class SetCommand implements Command {

    private final String key;
    private final String value;
    private Integer expireSeconds;

    public SetCommand(String key, String value) {
        this.key = key;
        this.value = value;
    }


    @Override
    public String execute(Context context) {
        context.getStore().put(key, new RedisObject(value, DataType.STRING));
        
        // 如果指定了过期时间，使用指定的；否则设置默认 TTL（1小时 = 3600秒）
        long expireSeconds;
        if (this.expireSeconds != null) {
            expireSeconds = this.expireSeconds;
        } else {
            expireSeconds = 3600;  // 默认 1 小时
        }
        
        long expireTime = System.currentTimeMillis() + expireSeconds * 1000L;
        context.getExpireMap().put(key, expireTime);
        
        AOFManager.append("set " + key + " " + value);
        return "OK";
    }
}
