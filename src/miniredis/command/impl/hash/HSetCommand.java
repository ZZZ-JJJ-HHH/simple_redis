package miniredis.command.impl.hash;

import miniredis.backup.AOFManager;
import miniredis.command.Command;
import miniredis.core.Context;
import miniredis.model.DataType;
import miniredis.model.RedisObject;

import java.util.HashMap;
import java.util.Map;

public class HSetCommand implements Command {

    private String key;
    private String[] args;

    public HSetCommand(String[] parts) {
        this.key = parts[1];
        this.args = parts;
    }

    @Override
    public String execute(Context context) {

        if ((args.length - 2) % 2 != 0) {
            throw new RuntimeException("HSET requires field value pairs");
        }

        RedisObject obj = context.getStore().get(key);
        Map<String, String> map;

        if (obj == null) {
            map = new HashMap<>();
            context.getStore().put(key, new RedisObject(map, DataType.HASH));
        } else {
            if (obj.getType() != DataType.HASH) {
                throw new RuntimeException("WRONG TYPE");
            }
            map = (Map<String, String>) obj.getValue();
        }

        int newCount = 0;

        for (int i = 2; i < args.length; i += 2) {
            String field = args[i];
            String value = args[i + 1];

            if (!map.containsKey(field)) {
                newCount++;
            }

            map.put(field, value);
        }

        // 拼接命令字符串，去掉中括号
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < args.length; i++) {
            if (i > 0) sb.append(" ");
            sb.append(args[i]);
        }
        AOFManager.append(sb.toString());

        return String.valueOf(newCount);
    }
}
