package miniredis.command.impl.string;

import lombok.AllArgsConstructor;
import miniredis.command.Command;
import miniredis.core.Context;
import miniredis.model.DataType;
import miniredis.model.RedisObject;
import miniredis.utils.ExpireUtil;

@AllArgsConstructor
public class GetCommand implements Command {

    private final String key;

    @Override
    public String execute(Context context) {

        if (ExpireUtil.isExpired(context, key)) {
            return null;
        }

        RedisObject obj = context.getStore().get(key);

        if (obj == null) {
            return null;
        }

        if (obj.getType() != DataType.STRING) {
            throw new RuntimeException("WRONG TYPE");
        }
        
        // 每次 GET 时刷新 TTL（重置为默认 1 小时）
        long expireTime = System.currentTimeMillis() + 3600 * 1000L;  // 1 小时
        context.getExpireMap().put(key, expireTime);

        return (String) obj.getValue();
    }
}
