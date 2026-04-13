package miniredis.protocol;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * RESP 协议解析器
 * 负责解析客户端发送的 RESP 格式数据
 */
public class RespParser {
    
    private final BufferedReader reader;
    
    public RespParser(BufferedReader reader) {
        this.reader = reader;
    }


    // 以客户端发送的数据为例子 *3\r\n$3\r\nSET\r\n$5\r\nmykey\r\n$5\r\nhello\r\n
    /**
     * 解析完整的 RESP 命令，返回命令数组
     * 以客户端发送的数据为例子 *3\r\n$3\r\nSET\r\n$5\r\nmykey\r\n$5\r\nhello\r\n
     **完全正确！您理解得非常准确！** 🎯

     ## 核心本质：

     `RespParser` 的所有方法都操作**同一个 `BufferedReader` 对象**，通过不断调用 `read()` 和 `readLine()`，让**读取指针（位置）持续向后移动**，逐步"消费"掉 RESP 协议的格式标记，最终提取出真正的命令内容。

     ---

     ## 以 `*3\r\n$3\r\nSET\r\n$5\r\nmykey\r\n$5\r\nhello\r\n` 为例：

     ### 初始状态：
     ```
     数据流：* 3 \r \n $ 3 \r \n S E T \r \n $ 5 \r \n m y k e y \r \n $ 5 \r \n h e l l o \r \n
     ↑
     指针位置
     ```


     ### 第 1 步：`parseValue()` → `reader.read()`
     ```java
     int type = reader.read();  // 读到 '*'
     ```
     ```
     数据流：* 3 \r \n $ 3 \r \n S E T \r \n ...
     ↑ ↑
     已读 指针移到这里
     ```


     ### 第 2 步：`parseArray()` → `readLine()`
     ```java
     String countStr = readLine();  // 读到 "3"
     ```
     ```
     数据流：* 3 \r \n $ 3 \r \n S E T \r \n ...
     ^^^^^^
     已读（丢弃）
     ↑
     指针移到这里
     ```


     ### 第 3 步：循环 3 次，每次调用 `parseValue()` → `parseBulkString()`

     **第 1 个元素：**
     ```java
     // parseBulkString() 内部
     readLine();  // 读到 "3"（长度）
     // 然后读 3 个字符 "SET"
     ```
     ```
     数据流：... $ 3 \r \n S E T \r \n $ 5 \r \n ...
     ^^^^^^^^^^^^^^^
     已读（丢弃格式，保留 "SET"）
     ↑
     指针移到这里
     ```


     **第 2 个元素：**
     ```java
     readLine();  // 读到 "5"
     // 然后读 5 个字符 "mykey"
     ```


     **第 3 个元素：**
     ```java
     readLine();  // 读到 "5"
     // 然后读 5 个字符 "hello"
     ```


     ### 最终结果：
     ```
     解析完成！
     返回：["SET", "mykey", "hello"]

     数据流全部被"消费"完毕
     ```


     ---

     ## 关键点：

     ### 1. **共享同一个 Reader**
     ```java
     public class RespParser {
     private final BufferedReader reader;  // ← 所有方法共用这一个

     public String[] parseCommand() {
     parseValue();  // 用 reader
     }

     private List<Object> parseArray() {
     readLine();    // 用同一个 reader
     }

     private String parseBulkString() {
     readLine();    // 还是同一个 reader
     }
     }
     ```


     ### 2. **指针单向移动**
     - `BufferedReader` 内部维护一个**位置指针**
     - 每次 `read()` 或 `readLine()` 都会**自动后移指针**
     - **不能回退**（除非用 `mark/reset`，但这里没用）

     ### 3. **"消费"协议格式**
     ```
     原始数据：*3\r\n$3\r\nSET\r\n$5\r\nmykey\r\n$5\r\nhello\r\n
     ^^   ^^   ^^^   ^^   ^^^^^   ^^   ^^^^^
     丢弃 丢弃 保留  丢弃 保留   丢弃 保留

     最终提取：["SET", "mykey", "hello"]
     ```


     ---

     ## 类比理解：

     想象您在**吃一串糖葫芦**：
     - **糖葫芦** = RESP 数据流
     - **您的嘴** = `BufferedReader`
     - **竹签上的山楂** = 有用数据（命令内容）
     - **糖浆和间隔** = RESP 格式标记（`*`、`$`、`\r\n` 等）

     您从前往后吃：
     1. 舔掉糖浆（读取并丢弃格式标记）
     2. 吃掉山楂（提取有用数据）
     3. 继续往后吃（指针后移）
     4. 吃完一整串（解析完成）

     **不能回头吃已经吃过的部分**（指针不能回退）。

     ---

     ## 总结：

     ✅ **是的，`RespParser` 的本质就是：**
     1. 操作同一个 `reader`
     2. 指针单向向后移动
     3. 逐步"消费"掉 RESP 协议的格式包装
     4. 提取出纯净的命令字符串数组

     **这是一种典型的"流式解析"（Streaming Parser）模式，高效且内存友好！** 👍
     */
    public String[] parseCommand() throws IOException {
        Object result = parseValue(); // result = ["SET", "mykey", "hello"]
        
        if (result instanceof List) {
            List<?> list = (List<?>) result;
            String[] commands = new String[list.size()];
            for (int i = 0; i < list.size(); i++) {
                commands[i] = (String) list.get(i);
            }
            return commands; // commands = ["SET", "mykey", "hello"]
        }
        
        throw new IOException("Expected array but got: " + result.getClass().getSimpleName());
    }
    
    /**
     * 解析 RESP 值
     */
    private Object parseValue() throws IOException {
        int type = reader.read();
        if (type == -1) {
            throw new IOException("Connection closed");
        }
        
        switch (type) {
            case '*': // 数组
                return parseArray();
            case '$': // 批量字符串
                return parseBulkString();
            case '+': // 简单字符串
                return parseSimpleString();
            case ':': // 整数
                return parseInteger();
            case '-': // 错误
                return parseError();
            default:
                throw new IOException("Unknown RESP type: " + (char) type);
        }
    }
    
    /**
     * 解析数组 (*<count>\r\n...)
     */
    private List<Object> parseArray() throws IOException {
        String countStr = readLine();  // 第一次 lengthStr = 3
        int count = Integer.parseInt(countStr);
        if (count == -1) {
            return null; // null 数组
        }
        
        List<Object> elements = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            elements.add(parseValue());
            // 第1次: parseValue() → parseBulkString() → 返回 "SET"
            // 第2次: parseValue() → parseBulkString() → 返回 "mykey"
            // 第3次: parseValue() → parseBulkString() → 返回 "h
        }
        
        return elements; // 返回 ["SET", "mykey", "hello"]
    }
    
    /**
     * 解析批量字符串 ($<length>\r\n<data>\r\n)
     */
    private String parseBulkString() throws IOException {
        String lengthStr = readLine();  // 第一次 lengthStr = 3 ，来自$3，并且之后已经去掉了 \r\n, 此时reader移到 SET 的S
        int length = Integer.parseInt(lengthStr);
        
        if (length == -1) {
            return null; // null 字符串
        }
        
        char[] buffer = new char[length];
        int totalRead = 0;
        while (totalRead < length) {
            /*这行代码是从 reader 中读取指定长度的字符到缓冲区。
                buffer，目标字符数组，用来存储读取的数据
                totalRead，从 buffer 的哪个位置开始存放（偏移量）
                length - totalRead，最多读取多少个字符
            */
            int read = reader.read(buffer, totalRead, length - totalRead);
            if (read == -1) {
                throw new IOException("Unexpected end of stream");
            }
            totalRead += read;
        }
        // buffer = [s,e,t]
        
        // 读取 \r\n
        reader.read(); // \r
        reader.read(); // \n
        
        return new String(buffer); // set
    }
    
    /**
     * 解析简单字符串 (+<string>\r\n)
     */
    private String parseSimpleString() throws IOException {
        return readLine();
    }
    
    /**
     * 解析整数 (:<number>\r\n)
     */
    private Long parseInteger() throws IOException {
        String numStr = readLine();
        return Long.parseLong(numStr);
    }
    
    /**
     * 解析错误 (-<error>\r\n)
     */
    private String parseError() throws IOException {
        return readLine();
    }
    
    /**
     * 读取一行（直到 \r\n）
     */
    private String readLine() throws IOException {
        StringBuilder sb = new StringBuilder();
        int c;
        
        while ((c = reader.read()) != -1) {
            if (c == '\r') {
                // 读取下一个字符，应该是 \n
                int next = reader.read();
                if (next == '\n') {
                    break;
                } else {
                    throw new IOException("Expected \\n after \\r");
                }
            }
            sb.append((char) c);
        }
        
        if (c == -1) {
            throw new IOException("Unexpected end of stream");
        }
        
        return sb.toString();
    }
}
