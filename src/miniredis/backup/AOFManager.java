package miniredis.backup;

import java.io.FileWriter;
import java.io.IOException;

public class AOFManager {

    private static final String AOF_FILE = "appendonly.aof";
    
    private static boolean enabled = true;  // AOF 开关

    public static synchronized void append(String command) {
        if (!enabled) return;  // 加载时禁用 AOF
        
        try (FileWriter writer = new FileWriter(AOF_FILE, true)) {
            writer.write(command + "\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * 临时禁用 AOF(用于加载历史数据)
     */
    public static void disable() {
        enabled = false;
    }
    
    /**
     * 启用 AOF
     */
    public static void enable() {
        enabled = true;
    }
}
