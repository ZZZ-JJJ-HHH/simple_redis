package miniredis.command;

import miniredis.command.impl.GenericCommand;
import miniredis.command.impl.expire.ExpireCommand;
import miniredis.command.impl.hash.*;
import miniredis.command.impl.string.GetCommand;
import miniredis.command.impl.string.SetCommand;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class CommandParser {

    private static final Map<String, Function<String[], Command>> REGISTRY = new HashMap<>();

    static {
        REGISTRY.put("set", parts -> {
            if (parts.length < 3) {
                throw new RuntimeException("SET command requires key and value");
            }
            if (parts.length >= 5 && parts[3].equalsIgnoreCase("ex")) {
                int seconds = Integer.parseInt(parts[4]);
                return new SetCommand(parts[1], parts[2],seconds);
            }
            return new SetCommand(parts[1], parts[2]);
        });

        REGISTRY.put("get", parts -> {
            if (parts.length < 2) {
                throw new RuntimeException("GET command requires key");
            }
            return new GetCommand(parts[1]);
        });


        REGISTRY.put("hset", parts -> {
            if (parts.length < 4) {
                throw new RuntimeException("HSET requires at least one field value pair");
            }
            return new HSetCommand(parts);
        });

        REGISTRY.put("hget", parts -> {
            if (parts.length < 3) {
                throw new RuntimeException("HGET command requires key and field");
            }
            return new HGetCommand(parts[1], parts[2]);
        });

        REGISTRY.put("hmget", parts -> {
            if (parts.length < 3) {
                throw new RuntimeException("HMGET command requires key and at least one field");
            }
            return new HMGetCommand(parts);
        });

        REGISTRY.put("hgetall", parts -> {
            if (parts.length < 2) {
                throw new RuntimeException("HGETALL command requires key");
            }
            return new HGetAllCommand(parts[1]);
        });

        REGISTRY.put("hkeys", parts -> {
            if (parts.length < 2) {
                throw new RuntimeException("HKEYS command requires key");
            }
            return new HKeysCommand(parts[1]);
        });

        REGISTRY.put("hvals", parts -> {
            if (parts.length < 2) {
                throw new RuntimeException("HVALS command requires key");
            }
            return new HValsCommand(parts[1]);
        });

        REGISTRY.put("hlen", parts -> {
            if (parts.length < 2) {
                throw new RuntimeException("HLEN command requires key");
            }
            return new HLenCommand(parts[1]);
        });

        REGISTRY.put("hexists", parts -> {
            if (parts.length < 3) {
                throw new RuntimeException("HEXISTS command requires key and field");
            }
            return new HExistsCommand(parts[1], parts[2]);
        });


        REGISTRY.put("expire", parts -> {
            if (parts.length < 3) {
                throw new RuntimeException("EXPIRE command requires key and seconds");
            }
            int seconds = Integer.parseInt(parts[2]);
            return new ExpireCommand(parts[1], seconds);
        });

        // 管理命令支持（返回空响应或默认值）
        REGISTRY.put("client", parts -> new GenericCommand("OK"));
        REGISTRY.put("config", parts -> {
            // CONFIG GET databases 返回数组格式
            if (parts.length >= 3 && "get".equalsIgnoreCase(parts[1]) && "databases".equalsIgnoreCase(parts[2])) {
                return new GenericCommand("*2\r\n$9\r\ndatabases\r\n:16\r\n");
            }
            return new GenericCommand("OK");
        });
        REGISTRY.put("scan", parts -> {
            // SCAN 返回空列表
            return new GenericCommand("*2\r\n$1\r\n0\r\n*0\r\n");
        });
        REGISTRY.put("info", parts -> new GenericCommand("# Server\r\nredis_version:1.0.0\r\n"));
        REGISTRY.put("dbsize", parts -> new GenericCommand(":0"));
        REGISTRY.put("select", parts -> new GenericCommand("OK"));
        REGISTRY.put("command", parts -> new GenericCommand("*0\r\n"));

    }

    public static Command parse(String input) {
        String[] parts = input.trim().split("\\s+");
        String cmd = parts[0].toLowerCase();

        Function<String[], Command> creator = REGISTRY.get(cmd);
        if (creator == null) {
            throw new RuntimeException("Unknown command");
        }

        return creator.apply(parts);
    }
}
