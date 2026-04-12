package miniredis.backup;

import miniredis.command.Command;
import miniredis.command.CommandParser;
import miniredis.core.Context;

import java.io.BufferedReader;
import java.io.FileReader;

public class AOFLoader {

    public static void load(Context context) {
        AOFManager.disable();  // 加载时禁用 AOF,避免重复写入
        
        try (BufferedReader reader = new BufferedReader(new FileReader("appendonly.aof"))) {

            String line;
            while ((line = reader.readLine()) != null) {

                try {
                    Command cmd = CommandParser.parse(line);
                    cmd.execute(context);
                } catch (Exception e) {
                    System.out.println("AOF load error: " + line);
                }
            }

        } catch (Exception e) {
            // 文件不存在也正常（第一次启动）
        } finally {
            AOFManager.enable();  // 加载完成后重新启用 AOF
        }
    }
}
