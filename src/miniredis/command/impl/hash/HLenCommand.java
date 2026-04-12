package miniredis.command.impl.hash;

import lombok.AllArgsConstructor;
import miniredis.command.Command;
import miniredis.core.Context;
import miniredis.model.DataType;
import miniredis.model.RedisObject;

import java.util.Map;

@AllArgsConstructor
public class HLenCommand implements Command {

    private String key;

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
        return String.valueOf(map.size());
    }
}
