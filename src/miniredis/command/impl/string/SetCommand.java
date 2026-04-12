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
        if (expireSeconds != null) {
            long expireTime = System.currentTimeMillis() + expireSeconds * 1000L;
            context.getExpireMap().put(key, expireTime);
        }
        AOFManager.append("set " + key + " " + value);
        return "OK";
    }
}
