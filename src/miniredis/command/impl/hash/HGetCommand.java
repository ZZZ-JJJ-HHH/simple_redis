package miniredis.command.impl.hash;

import lombok.AllArgsConstructor;
import miniredis.command.Command;
import miniredis.core.Context;
import miniredis.model.DataType;
import miniredis.model.RedisObject;
import miniredis.utils.ExpireUtil;

import java.util.Map;

@AllArgsConstructor
public class HGetCommand implements Command {

    private String key;
    private String field;


    @Override
    public String execute(Context context) {

        if (ExpireUtil.isExpired(context, key)) {
            return null;
        }

        RedisObject obj = context.getStore().get(key);

        if (obj == null) {
            return "null";
        }

        if (obj.getType() != DataType.HASH) {
            throw new RuntimeException("WRONG TYPE");
        }

        Map<String, String> map = (Map<String, String>) obj.getValue();
        return map.getOrDefault(field, "null");
    }
}
