package miniredis.command.impl.hash;

import lombok.AllArgsConstructor;
import miniredis.command.Command;
import miniredis.core.Context;
import miniredis.model.DataType;
import miniredis.model.RedisObject;

import java.util.Map;

@AllArgsConstructor
public class HExistsCommand implements Command {

    private String key;
    private String field;

    @Override
    public String execute(Context context) {
        RedisObject obj = context.getStore().get(key);

        if (obj == null) {
            return "0";
        }

        if (obj.getType() != DataType.HASH) {
            throw new RuntimeException("WRONG TYPE");
        }

        Map<String, String> map = (Map<String, String>) obj.getValue();
        return map.containsKey(field) ? "1" : "0";
    }
}
