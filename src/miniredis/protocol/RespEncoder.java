package miniredis.protocol;

import java.io.PrintWriter;
import java.util.List;

/**
 * RESP 协议编码器
 * 负责将响应编码为 RESP 格式发送给客户端
 */
public class RespEncoder {
    
    private final PrintWriter writer;
    
    public RespEncoder(PrintWriter writer) {
        this.writer = writer;
    }
    
    /**
     * 发送简单字符串响应 (+OK\r\n)
     */
    public void writeSimpleString(String value) {
        writer.print("+" + value + "\r\n");
        writer.flush();
    }
    
    /**
     * 发送错误响应 (-ERR message\r\n)
     */
    public void writeError(String message) {
        writer.print("-" + message + "\r\n");
        writer.flush();
    }
    
    /**
     * 发送整数响应 (:1000\r\n)
     */
    public void writeInteger(long value) {
        writer.print(":" + value + "\r\n");
        writer.flush();
    }
    
    /**
     * 发送批量字符串响应 ($5\r\nhello\r\n)
     */
    public void writeBulkString(String value) {
        if (value == null) {
            writer.print("$-1\r\n");
        } else {
            byte[] bytes = value.getBytes();
            writer.print("$" + bytes.length + "\r\n");
            writer.print(value + "\r\n");
        }
        writer.flush();
    }
    
    /**
     * 发送数组响应 (*2\r
$3\r
foo\r
$3\r
bar\r
)
     */
    public void writeArray(List<String> values) {
        if (values == null) {
            writer.print("*-1\r\n");
            writer.flush();
            return;
        }
        
        writer.print("*" + values.size() + "\r\n");
        for (String value : values) {
            writeBulkString(value);
        }
        writer.flush();
    }
    
    /**
     * 根据响应类型自动选择编码方式
     */
    public void writeResponse(Object response) {
        if (response == null) {
            writeBulkString(null);
        } else if (response instanceof String) {
            String str = (String) response;
            
            // 检查是否是原始 RESP 格式
            if (str.startsWith("__RESP_RAW__:")) {
                String rawResp = str.substring(13); // 去掉 "__RESP_RAW__:"
                writer.print(rawResp);
                writer.flush();
                return;
            }
            
            // 判断是否是特殊格式
            if (str.startsWith("(error) ")) {
                // 错误消息
                writeError(str.substring(8));
            } else if (str.startsWith("(integer) ")) {
                // 整数
                try {
                    long num = Long.parseLong(str.substring(10));
                    writeInteger(num);
                } catch (NumberFormatException e) {
                    writeBulkString(str);
                }
            } else if (str.equals("OK")) {
                // 简单成功消息
                writeSimpleString("OK");
            } else if (str.equals("(nil)")) {
                // null 值
                writeBulkString(null);
            } else {
                // 普通字符串
                writeBulkString(str);
            }
        } else if (response instanceof Long) {
            writeInteger((Long) response);
        } else if (response instanceof Integer) {
            writeInteger((Integer) response);
        } else {
            // 默认作为字符串处理
            writeBulkString(response.toString());
        }
    }
}
