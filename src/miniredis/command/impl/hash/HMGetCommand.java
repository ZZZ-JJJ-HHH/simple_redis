package miniredis.command.impl.hash;

import lombok.AllArgsConstructor;
import miniredis.command.Command;
import miniredis.core.Context;
import miniredis.model.DataType;
import miniredis.model.RedisObject;
import miniredis.utils.ExpireUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@AllArgsConstructor
public class HMGetCommand implements Command {

    private String key;
    private String[] fields;

    public HMGetCommand(String[] parts) {
        this.key = parts[1];
        this.fields = new String[parts.length - 2];
        System.arraycopy(parts, 2, this.fields, 0, fields.length);
    }

    @Override
    public String execute(Context context) {
        if (ExpireUtil.isExpired(context, key)) {
            return null;
        }
        RedisObject obj = context.getStore().get(key);

        if (obj == null) {
            StringBuilder result = new StringBuilder();
            for (int i = 0; i < fields.length; i++) {
                if (i > 0) result.append(", ");
                result.append("null");
            }
            return result.toString();
        }

        if (obj.getType() != DataType.HASH) {
            throw new RuntimeException("WRONG TYPE");
        }

        Map<String, String> map = (Map<String, String>) obj.getValue();
        List<String> values = new ArrayList<>();
        
        for (String field : fields) {
            values.add(map.getOrDefault(field, "null"));
        }

        return String.join(", ", values);
    }
}
