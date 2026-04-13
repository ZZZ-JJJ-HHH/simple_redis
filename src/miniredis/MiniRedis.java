package miniredis;

import miniredis.backup.AOFLoader;
import miniredis.core.Context;
import miniredis.core.ExpireTask;
import miniredis.server.RedisServer;

public class MiniRedis {

    public static void main(String[] args) {
        Context context = new Context();
        
        // 加载历史数据 AOF
        AOFLoader.load(context);
        
        // 定时检查过期数据
        ExpireTask.start(context);

        // 创建并启动 Redis 服务器
        RedisServer server = new RedisServer(context);
        
        // 添加优雅退出钩子
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("\nShutting down MiniRedis...");
            server.stop();
            System.out.println("MiniRedis shutdown complete");
        }));

        // 启动服务器（阻塞运行）
        server.start();
    }
}
