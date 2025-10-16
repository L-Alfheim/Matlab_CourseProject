以下是为您的人员通讯录系统后端编写的详细文档（DOC格式），包含架构说明、接口文档、类说明和使用示例：

# 人员通讯录系统 - 后端文档

## 1. 系统概述

本系统是一个基于Java的人员通讯录管理系统，采用客户端-服务器架构，支持人员基本信息和面部图像的存储、查询、更新和删除操作。

### 技术栈
- **数据库**: SQLite
- **网络通信**: Socket + 自定义协议
- **数据处理**: JDBC + 自定义数据封装
- **架构模式**: 分层架构（接口隔离）

## 2. 系统架构

```
Client → JavaServer → PersonPackage → DatabaseIO → SQLite Database
```

## 3. 核心组件说明

### 3.1 数据库接口层

#### 接口：DatabaseIO
**包名**: `com.Alfheim.DatabaseInterface`
**职责**: 定义数据库操作的标准接口

| 方法 | 参数 | 返回值 | 说明 |
|------|------|--------|------|
| `connect()` | 无 | void | 连接数据库 |
| `disconnect()` | 无 | void | 断开数据库连接 |
| `executeQuery(String query)` | SQL查询语句 | ResultSet | 执行查询操作 |
| `executeUpdate(String command)` | SQL更新语句 | int | 执行更新操作 |
| `insertQuery(String command)` | SQL插入语句 | void | 执行插入操作 |
| `deleteQuery(String command)` | SQL删除语句 | void | 执行删除操作 |
| `ImageQuery(String command)` | 图片相关SQL | void | 执行图片操作 |

#### 实现类：SQLite
**包名**: `com.Alfheim.DatabaseInterface`
**职责**: SQLite数据库的具体实现

**特性**:
- 自动连接本地SQLite数据库(`database/Person.db`)
- 完整的异常处理机制
- 线程安全的连接管理

### 3.2 业务逻辑层

#### 接口：PersonPackageIO
**包名**: `com.Alfheim.Person`
**职责**: 定义人员信息操作接口

| 方法 | 参数 | 返回值 | 说明 |
|------|------|--------|------|
| `Person()` | 人员信息+图片 | void | 初始化人员对象 |
| `clear()` | 无 | void | 清空对象数据 |
| `InsertOrUpdate()` | 无 | String | 插入或更新人员信息 |
| `deletePersonInfo()` | 无 | String | 删除人员信息 |
| `queryPersonInfo()` | 无 | ResultSet | 查询单个人员 |
| `queryAllPersonInfo()` | 无 | ResultSet | 查询所有人员 |
| `getPersonFromDatabase()` | 无 | String | 从数据库获取完整信息 |
| 各getter方法 | 无 | String | 获取各属性值 |

#### 实现类：PersonPackage
**包名**: `com.Alfheim.Person`
**职责**: 人员信息的完整封装和处理

**核心功能**:
- **数据持久化**: 自动保存图片到文件系统(`database/images/`)
- **数据库关联**: 维护Person表与FaceRecord表的关联
- **智能更新**: 根据姓名自动判断插入或更新操作
- **资源管理**: 自动管理数据库连接和文件资源

**主要属性**:
- `Name`: 姓名（主键）
- `phoneNumber`: 电话号码
- `Birthday`: 生日
- `Address`: 地址
- `personId`: 人员ID（数据库自增）
- `imageURL`: 图片存储路径
- `image`: 图片字节数据

### 3.3 网络服务层

#### 类：JavaServer
**包名**: `com.Alfheim.Server`
**职责**: 多客户端Socket服务器

**核心特性**:
- **多线程支持**: 每个客户端连接独立线程处理
- **自定义协议**: 基于Header+Data的通信协议
- **命令分发**: 支持SELECT/INSERT/DELETE等操作
- **二进制传输**: 支持图片等二进制数据传输

**服务器配置**:
- 端口: 1642
- 协议: TCP Socket
- 编码: UTF-8

## 4. 通信协议规范

### 4.1 客户端请求格式

```
command=INSERT
name=张三
phone=13800138000
birthday=1990-01-01
address=北京市
fileSize=1024
END_HEADER
[二进制图片数据]
```

### 4.2 服务器响应格式

**文本响应**:
```
数据插入成功
```

**数据包响应**:
```
command=SingleOut
name=张三
phone=13800138000
birthday=1990-01-01
address=北京市
fileName=img_1234567890.png
fileSize=1024
END_HEADER
[二进制图片数据]
```

### 4.3 支持的命令

| 命令 | 功能 | 响应类型 |
|------|------|----------|
| `INSERT` | 插入/更新人员信息 | 文本 |
| `SELECT` | 查询单个人员 | 数据包 |
| `SELECTall` | 查询所有人员 | 文本(CSV格式) |
| `DELETE` | 删除人员信息 | 文本 |

## 5. 数据库设计

### Person表
| 字段 | 类型 | 说明 |
|------|------|------|
| id | INTEGER PRIMARY KEY | 自增主键 |
| Name | TEXT | 姓名（唯一） |
| phoneNumber | TEXT | 电话号码 |
| Birthday | TEXT | 生日 |
| Address | TEXT | 地址 |

### FaceRecord表
| 字段 | 类型 | 说明 |
|------|------|------|
| id | INTEGER PRIMARY KEY | 自增主键 |
| personid | INTEGER | 关联Person.id |
| imageURL | TEXT | 图片存储路径 |

## 6. 使用示例

### 6.1 启动服务器
```java
// 直接运行JavaServer.main()方法
java com.Alfheim.Server.JavaServer
```

### 6.2 客户端集成示例
```java
// 连接服务器
Socket socket = new Socket("localhost", 1642);
DataOutputStream out = new DataOutputStream(socket.getOutputStream());

// 构造请求
String header = "command=INSERT\n" +
               "name=李四\n" +
               "phone=13900139000\n" +
               "birthday=1985-05-15\n" +
               "address=上海市\n" +
               "fileSize=" + imageBytes.length + "\n" +
               "END_HEADER\n";

// 发送请求
out.write(header.getBytes("UTF-8"));
out.write(imageBytes);
out.flush();

// 接收响应
DataInputStream in = new DataInputStream(socket.getInputStream());
// 处理响应...
```

## 7. 异常处理

系统采用分层异常处理机制：
- **数据库层**: SQLException捕获并返回错误码
- **业务层**: Exception统一捕获并记录日志
- **网络层**: IOException处理连接异常

## 8. 部署说明

### 环境要求
- Java 8+
- SQLite JDBC驱动
- 磁盘写入权限（图片存储）

### 目录结构
```
project/
├── database/
│   ├── Person.db          # SQLite数据库
│   └── images/            # 图片存储目录
├── src/
│   └── com/Alfheim/       # 源代码
└── lib/
    └── sqlite-jdbc.jar    # SQLite驱动
```

## 9. 注意事项

1. **线程安全**: 每个客户端连接创建独立的PersonPackage实例
2. **资源释放**: 使用后调用clear()方法释放资源
3. **图片存储**: 图片自动保存为PNG格式，使用时间戳确保文件名唯一
4. **字符编码**: 统一使用UTF-8编码避免乱码问题
5. **路径分隔符**: 自动处理Windows/Linux路径分隔符差异

