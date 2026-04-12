# MiniRedis 项目复盘总结

## 一、项目本质

实现了一个**简化版 Redis 内核系统**（Mini In-Memory Data Store），覆盖了真实 Redis 最核心的四个子系统。

---

## 二、核心子系统

### 1️⃣ 数据模型层（Data Model）

**实现内容：**
- `key → RedisObject(type + value)`
- 支持数据类型：String、Hash

**本质能力：**
- 统一抽象（type + value）
- 动态类型系统（运行时判断）

**关键代码：**
```java
public class RedisObject {
    private Object value;
    private DataType type;  // STRING, HASH
}
```

---

### 2️⃣ 命令执行层（Command Engine）

**实现流程：**
```
输入字符串 → 解析 → Command → execute(context)
```

**核心设计：**
- **Command 模式**：每个命令独立类
- **Parser + Registry**：命令注册与分发
- **Context**：执行上下文（store + expireMap）

**本质能力：**
- 把"用户请求"转化为"系统操作"
- 这是所有中间件/框架的核心模型

**关键代码：**
```java
// 命令注册
REGISTRY.put("set", parts -> new SetCommand(parts[1], parts[2]));
REGISTRY.put("hset", parts -> new HSetCommand(parts));

// 命令执行
Command cmd = CommandParser.parse(input);
cmd.execute(context);
```

---

### 3️⃣ 内存管理层（Memory Management）

#### TTL（过期机制）
- **惰性删除**：访问时检查是否过期
- **定期删除**：定时任务扫描清理

**关键代码：**
```java
// 定期清理任务
scheduler.scheduleAtFixedRate(() -> {
    cleanExpiredKeys(context);
}, 5, 5, TimeUnit.SECONDS);
```

#### LRU 淘汰
- 基于 `LinkedHashMap(accessOrder=true)`
- 最近最少使用策略
- 容量满时自动淘汰最久未使用的 key

**关键代码：**
```java
new LinkedHashMap<>(capacity, 0.75f, true) {
    @Override
    protected boolean removeEldestEntry(Map.Entry eldest) {
        if (size() > capacity) {
            expireMap.remove(eldest.getKey());  // 同步清理
            return true;
        }
        return false;
    }
};
```

**本质能力：**
- 控制数据生命周期（TTL）
- 控制内存上限（LRU）

---

### 4️⃣ 持久化层（Persistence）

**AOF（Append Only File）实现：**
- 写操作追加日志
- 启动时重放日志恢复数据
- 防止重复写入（加载时禁用 AOF）

**关键代码：**
```java
// 写入 AOF
AOFManager.append(command);

// 加载时禁用 AOF，避免重复写入
AOFManager.disable();
try {
    // 重放命令
    cmd.execute(context);
} finally {
    AOFManager.enable();
}
```

**本质能力：**
- 内存数据 → 可恢复状态
- 解决重启数据丢失问题

---

## 三、系统架构图

```
              Client
                |
                v
         Command Parser
                |
                v
         Command Engine
                |
                v
            Context
         /           \
   store(LRU)    expireMap
                     |
                     |
          -------------------------
          |           |           |
      TTL检查     LRU淘汰     AOF日志
```

**面试价值：** 能画出这个结构已超过 80% Java 候选人

---

## 四、掌握的核心系统能力

### 1️⃣ 抽象能力（最重要）
- `RedisObject = value + type`
- `Context = 数据容器`
- `Command = 操作`

**这是系统设计能力，不是编码能力**

### 2️⃣ 状态管理能力
同时维护：
- `store`（数据存储）
- `expireMap`（过期时间）
- `AOF 文件`（持久化日志）

并保证**状态一致性**

### 3️⃣ 时间维度控制
- TTL 过期
- 定时任务调度

**本质：数据不是静态的，而是"随时间变化"**

### 4️⃣ 空间维度控制
- LRU 淘汰策略

**本质：资源有限，必须做决策**

### 5️⃣ 副作用控制（高级）
- 解决 AOF 重放导致日志膨胀
- 通过 `enable` 标志隔离副作用

**本质：幂等性 + 副作用隔离**

---

## 五、认知层级跨越

```
初级（CRUD）
  ↓ 写接口、调数据库
  
中级（框架使用者）
  ↓ 用 Redis、用 Spring
  
现在（系统实现者）
  ↓ 我知道 Redis 是怎么工作的
  ↓ 我能自己实现一套
```

**这一步非常关键：从"用工具的人"变成"造工具的人"**

---

## 六、面试讲解指南

### 一句话项目描述

> 我实现了一个简化版 Redis，支持多数据结构、TTL 过期、LRU 淘汰以及 AOF 持久化，并重点解决了数据一致性和内存管理问题。

### 面试讲解结构（标准答案）

#### 1️⃣ 数据结构设计
- RedisObject（type + value）
- 为什么不用泛型（运行时类型系统）

#### 2️⃣ 命令执行模型
- Command 模式
- Parser 解耦

#### 3️⃣ 过期策略
- 惰性删除 vs 定期删除
- 为什么不能全量扫描

#### 4️⃣ 内存淘汰
- LRU 原理
- 为什么用 LinkedHashMap

#### 5️⃣ 持久化
- AOF 原理
- 为什么会日志膨胀
- 如何避免（disable/enable 开关）

**讲完这 5 点，面试官基本会默认你是"中高级工程师水平"**

---

## 七、技术细节笔记

### LinkedHashMap 实现 LRU

```java
// 第三个参数 true = 按访问顺序排序
new LinkedHashMap<>(capacity, 0.75f, true) {
    @Override
    protected boolean removeEldestEntry(Map.Entry eldest) {
        return size() > capacity;  // 超过容量删除最老的
    }
};
```

**关键点：**
- `true` 启用访问顺序，`get()` 也会更新顺序
- `removeEldestEntry()` 是钩子方法，返回 true 时自动删除

### 匿名内部类

```java
new LinkedHashMap(...) {
    @Override
    protected boolean removeEldestEntry(...) { ... }
};
```

**等价于：**
```java
class MyMap extends LinkedHashMap {
    @Override
    protected boolean removeEldestEntry(...) { ... }
}
new MyMap(...);
```

### Java 值传递

- **基本类型**：传递值的副本
- **引用类型**：传递引用的副本（地址值）
- 可以修改对象内容，但不能改变原引用指向

### Maven 资源加载

配置文件必须放在 `src/resources/` 目录，并在 `pom.xml` 中声明：

```xml
<resources>
    <resource>
        <directory>src/resources</directory>
    </resource>
</resources>
```

加载方式：
```java
getClassLoader().getResourceAsStream("config.properties")
```

---

## 八、后续优化方向（按价值排序）

### 1. 网络化（最推荐）
- 使用 Socket / NIO
- 实现 `telnet localhost 6379`
- 变成真正的 Redis Server

### 2. AOF Rewrite
- 压缩日志文件
- 合并重复命令

### 3. 并发优化
- 读写锁
- 分段锁

### 4. 数据结构扩展
- 支持 List / Set / ZSet

---

## 九、核心价值总结

完成了一个很多人"看过但没做过"的东西：

> **把一个复杂系统拆开 → 一块一块实现 → 再组合起来**

这件事的价值在于：

以后再看任何系统（Redis、MySQL、MQ、Spring），脑子里都会自动问：

- ✅ 它的数据结构是什么？
- ✅ 它的状态怎么管理？
- ✅ 它的边界条件是什么？
- ✅ 它的副作用在哪里？

**这才是真正获得的能力。**

---

## 十、项目文件清单

```
src/
├── miniredis/
│   ├── MiniRedis.java              # 主入口
│   ├── config/
│   │   ├── ConfigManager.java      # 配置管理
│   │   └── config.properties       # 配置文件
│   ├── core/
│   │   ├── Context.java            # 执行上下文
│   │   ├── LRUCache.java           # LRU 缓存
│   │   └── ExpireTask.java         # 过期清理任务
│   ├── model/
│   │   ├── RedisObject.java        # 数据对象
│   │   └── DataType.java           # 数据类型枚举
│   ├── command/
│   │   ├── Command.java            # 命令接口
│   │   ├── CommandParser.java      # 命令解析器
│   │   └── impl/
│   │       ├── string/             # String 命令
│   │       ├── hash/               # Hash 命令
│   │       └── expire/             # 过期命令
│   └── backup/
│       ├── AOFManager.java         # AOF 管理器
│       └── AOFLoader.java          # AOF 加载器
└── resources/
    └── config.properties           # 配置文件
```

---

**最后记住：**

你不再是 CRUD Boy，你是**系统实现者**。
