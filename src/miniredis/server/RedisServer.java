package miniredis.server;

import miniredis.command.CommandParser;
import miniredis.core.Context;
import miniredis.config.ConfigManager;
import miniredis.protocol.RespParser;
import miniredis.protocol.RespEncoder;

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
            // 创建 RESP 解析器和编码器
            RespParser parser = new RespParser(in);
            RespEncoder encoder = new RespEncoder(out);
            
            System.out.println("Client connected: " + clientSocket.getRemoteSocketAddress());
            
            while (running) {
                String commandLine = null;
                try {
                    // 解析 RESP 命令
                    String[] commands = parser.parseCommand();
                    
                    if (commands == null || commands.length == 0) {
                        continue;
                    }
                    
                    String cmd = commands[0].toUpperCase();
                    
                    // 支持 PING 命令
                    if ("PING".equals(cmd)) {
                        encoder.writeSimpleString("PONG");
                        continue;
                    }
                    
                    // 支持退出命令
                    if ("EXIT".equals(cmd) || "QUIT".equals(cmd)) {
                        System.out.println("Client disconnected: " + clientSocket.getRemoteSocketAddress());
                        encoder.writeSimpleString("OK");
                        break;
                    }
                    
                    // 重新组装命令字符串（保持与原有 CommandParser 兼容）
                    StringBuilder cmdBuilder = new StringBuilder();
                    for (int i = 0; i < commands.length; i++) {
                        if (i > 0) cmdBuilder.append(" ");
                        cmdBuilder.append(commands[i]);
                    }
                    commandLine = cmdBuilder.toString();
                    
                    // 执行命令
                    String response = CommandParser.parse(commandLine).execute(context);
                    
                    // 用 RESP 格式编码响应
                    encoder.writeResponse(response);
                    
                } catch (Exception e) {
                    System.err.println("Command execution error: " + e.getMessage() + (commandLine != null ? " | Command: [" + commandLine + "]" : ""));
                    // 发送错误响应
                    encoder.writeError("ERR " + e.getMessage());
                }
            }
        } catch (IOException e) {
            if (running) {
                System.err.println("Client connection error: " + e.getMessage());
            }
        } finally {
            try {
                clientSocket.close();
                System.out.println("Client closed: " + clientSocket.getRemoteSocketAddress());
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
