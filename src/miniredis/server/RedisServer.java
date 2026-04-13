package miniredis.server;

import miniredis.command.CommandParser;
import miniredis.core.Context;
import miniredis.config.ConfigManager;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


/*
## RedisServer 核心功能总结

**1. 启动 TCP 服务器**
- `ServerSocket` 监听 6379 端口
- `accept()` 阻塞等待客户端连接

**2. 多线程处理并发**
- `ExecutorService` 线程池管理
- 每个客户端连接分配独立线程处理

**3. 命令交互**
- 读取客户端发送的命令（按行）
- 调用 `CommandParser` 执行
- 返回结果给客户端

**4. 优雅关闭**
- `stop()` 方法关闭 ServerSocket 和线程池
- 支持 EXIT/QUIT 命令断开单个客户端

---

**简单说：把原来的命令行交互改成了网络服务，支持多客户端同时连接使用。**
* */

public class RedisServer {

    private final int port;
    private final Context context;
    private ServerSocket serverSocket;
    private ExecutorService threadPool;
    private volatile boolean running = false;

    public RedisServer(Context context) {
        this.context = context;
        this.port = ConfigManager.getInt("server.port", 6379);
        // 创建线程池处理客户端连接
        this.threadPool = Executors.newCachedThreadPool();
    }

    public void start() {
        try {
            serverSocket = new ServerSocket(port);
            running = true;
            System.out.println("MiniRedis server started on port " + port);

            while (running) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    threadPool.submit(() -> handleClient(clientSocket));
                } catch (IOException e) {
                    if (running) {
                        System.err.println("Accept connection failed: " + e.getMessage());
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Failed to start server: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void handleClient(Socket clientSocket) {
        try (
            BufferedReader in = new BufferedReader(
                new InputStreamReader(clientSocket.getInputStream())
            );
            PrintWriter out = new PrintWriter(
                clientSocket.getOutputStream(), true
            )
        ) {
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                if (inputLine.trim().isEmpty()) {
                    continue;
                }

                // 支持退出命令
                if ("EXIT".equalsIgnoreCase(inputLine.trim()) || 
                    "QUIT".equalsIgnoreCase(inputLine.trim())) {
                    out.println("OK");
                    break;
                }

                try {
                    String response = CommandParser.parse(inputLine).execute(context);
                    out.println(response);
                } catch (Exception e) {
                    out.println("ERROR: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            System.err.println("Client connection error: " + e.getMessage());
        } finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
                // ignore
            }
        }
    }

    public void stop() {
        running = false;
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
            threadPool.shutdown();
            System.out.println("MiniRedis server stopped");
        } catch (IOException e) {
            System.err.println("Error stopping server: " + e.getMessage());
        }
    }
}
