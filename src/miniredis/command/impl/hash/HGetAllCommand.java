package miniredis.command.impl.hash;

import lombok.AllArgsConstructor;
import miniredis.command.Command;
import miniredis.core.Context;
import miniredis.model.DataType;
import miniredis.model.RedisObject;
import miniredis.utils.ExpireUtil;

import java.util.Map;

@AllArgsConstructor
public class HGetAllCommand implements Command {

    private String key;

    @Override
    public String execute(Context context) {
        if (ExpireUtil.isExpired(context, key)) {
            return null;
        }
        RedisObject obj = context.getStore().get(key);

        if (obj == null) {
            return "(empty list or set)";
        }

        if (obj.getType() != DataType.HASH) {
            throw new RuntimeException("WRONG TYPE");
        }

        Map<String, String> map = (Map<String, String>) obj.getValue();
        StringBuilder result = new StringBuilder();
        
        int i = 0;
        for (Map.Entry<String, String> entry : map.entrySet()) {
            if (i > 0) result.append(", ");
            result.append(entry.getKey()).append(": ").append(entry.getValue());
            i++;
        }

        return result.toString();
    }
}
