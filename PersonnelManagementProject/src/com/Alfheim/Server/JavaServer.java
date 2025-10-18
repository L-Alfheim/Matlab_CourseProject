package com.Alfheim.Server;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;


import java.sql.ResultSetMetaData;

import com.Alfheim.Person.PersonPackageIO;
import com.Alfheim.DatabaseInterface.DatabaseIO;
import com.Alfheim.Person.PersonPackage;

public class JavaServer {
    /**
     * 主函数，启动服务器，并检测端口1642，创建线程处理客户端请求
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        
        
        // PersonPackageIO person = new PersonPackage(); //人员信息打包对象
        // 启动服务器
        ServerSocket server = new ServerSocket(1642);
        System.out.println("Server started on port 1642...");
        DatabaseIO create = new com.Alfheim.DatabaseInterface.SQLite(); //初始化数据库连接
        create.getConnection();
        
        // 检测客户端连接
        while (true) {
            Socket client = server.accept();
            System.out.println("Client connected: " + client.getInetAddress() + " port: " + client.getPort() + " Time:" + new java.util.Date().toString());
            
            // 新线程处理客户端通信
            new Thread(() -> handleClient(client)).start();
        }
    }
    
    // 处理客户端通信线程
    /**
     * @param client //客户端Socket
     */
    private static void handleClient(Socket client) {
        try (
            DataInputStream in = new DataInputStream(client.getInputStream()); //输入流
            DataOutputStream out = new DataOutputStream(client.getOutputStream()); //输出流
            ) {
            PersonPackageIO person = new PersonPackage();
                
            while (true) {
                // 1. ---- 读取header ----
                Map<String, String> header = readHeader(in);
                if (header == null || header.isEmpty()) {
                    System.out.println("Client closed connection or header empty.");
                    return;
                }

                // 2. ---- 提取命令 ----
                String command = header.get("command");

                // 3. ---- 解析文件大小 ----
                int fileSize;
                try {
                    fileSize = Integer.parseInt(header.get("fileSize"));
                } catch (Exception e) {
                    System.err.println("Invalid or missing fileSize");
                    return;
                }

                // 4. ---- 读取文件数据 ----
                byte[] imgData = new byte[fileSize];
                try {
                    in.readFully(imgData);
                } catch (EOFException eof) {
                    System.err.println("客户端在传输文件时断开连接。");
                    return;
                }

                // 5. ---- 写入person ----
                try {
                    person.Person(header.get("name"), 
                                header.get("phone"), 
                                header.get("birthday"), 
                                header.get("address"), 
                                imgData);
                } catch (Exception e) {
                    System.err.println("Error filling PersonPackage: " + e.getMessage());
                }

                // 处理命令并获取结果, 仅当命令为SELECT时返回数据包
                if ("SELECT".equals(command)) {
                    // 发送结果回客户端
                    try {
                        sendPersonPackage(out, person, "SingleOut");
                    } catch (IOException e) {
                        System.err.println("Client closed connection before result could be sent.");
                    }
                } else { // 其他命令返回简短结果
                    String result = processCommand(command, person);
                    // 发送结果回客户端
                    try {
                        out.write((result + "\n").getBytes(StandardCharsets.UTF_8));
                        out.flush();
                    } catch (IOException e) {
                        System.err.println("Client closed connection before result could be sent.");
                    }
                }
                person.clear();
            }

        } catch (IOException e) {
            System.err.println("Client communication error: " + e.getMessage());
        } finally {
            try { client.close(); } catch (IOException ignored) {}
        }
    }

    /**
     * 处理header部分
     * 读取并解析头数据包
     * @param in //输入流
     * @return Map<String, String> 头文件的字符串映射
     * @throws IOException
     */
    private static Map<String, String> readHeader(DataInputStream in) throws IOException {
        Map<String, String> header = new HashMap<>();

        // 1  用 ByteArrayOutputStream 收集 header 字节
        ByteArrayOutputStream headerBytes = new ByteArrayOutputStream();
        int b; //单个字节

        //使用b读取单个字节并写入headerBytes,直到结尾
        while ((b = in.read()) != -1) {
            headerBytes.write(b);
            // 临时转 UTF-8 判断是否接收到 END_HEADER
            String temp = headerBytes.toString(StandardCharsets.UTF_8.name());
            if (temp.contains("END_HEADER")) {
                break;
            }
        }

        // 没有读到 END_HEADER，但是DataInputStream已到达end，客户端提前关闭，传输中断
        if (b == -1) {
            throw new IOException("Client closed connection before sending END_HEADER.");
        }

        // header 字节转为 UTF-8 字符串
        String headerStr = headerBytes.toString(StandardCharsets.UTF_8.name());

        // 按行解析 header
        for (String line : headerStr.split("\\r?\\n")) {
            line = line.trim();
            if (line.isEmpty() || "END_HEADER".equals(line)) continue;
            if (line.contains("=")) {
                String[] parts = line.split("=", 2);
                header.put(parts[0].trim(), parts[1].trim());
            }
        }

        return header;
    }


    /**
     * 处理数据库命令的静态方法，包括SELECTall, INSERT, DELETE
     * @param command 指令
     * @param person 操作的 PersonPackageIO 对象
     * @return 处理结果字符串
     */
    private static String processCommand(String command, PersonPackageIO person) {
        try {

            switch (command) {
                case "SELECT":
                    // 执行查询
                    
                    return "";

                case "SELECTall":
                    // 执行查询所有
                    ResultSet rsAll = person.queryAllPersonInfo();
                    if (rsAll == null) {
                        return "Query returned no result.";
                    }
                    StringBuilder resultAll = new StringBuilder();
                    // 遍历结果集，将所有列数据拼接到字符串中
                    ResultSetMetaData metaDataAll = rsAll.getMetaData();
                    int columnCountAll = metaDataAll.getColumnCount();
                    while (rsAll.next()) {
                        for (int i = 1; i <= columnCountAll; i++) {
                            resultAll.append(rsAll.getString(i));
                            if (i < columnCountAll) {
                                resultAll.append(", "); // 列之间用逗号分隔
                            }
                        }
                        resultAll.append("\n"); // 每行数据换行
                    }
                    return resultAll.toString().trim();

                case "INSERT":
                    // 执行更新或插入
                    return person.InsertOrUpdate();

                case "DELETE":
                    // 执行删除
                    return person.deletePersonInfo();
                default:
                    return "Unknown command: " + command;
            }

        } catch (SQLException e) {
            // SQL 异常
            return "SQL Error: " + e.getMessage();
        } catch (Exception e) {
            // 其他异常
            return "Error processing command: " + e.getMessage();
        }
    }

    /**
     * 打包并发送 PersonPackage 对象
     * @param out 输出流
     * @param person PersonPackageIO 对象，包含人员信息和图片路径
     * @param command 要发送的命令
     * @throws IOException
     */
    public static void sendPersonPackage(OutputStream out, PersonPackageIO person, String command) throws IOException {
        // 构造HEADER
        Map<String, String> header = new LinkedHashMap<>();
        String result = person.getPersonFromDatabase();
        if ("Query returned no result.".equals(result)) {
            //查无此人
            header.put("command", "NoResult");
            header.put("END_HEADER", "");

            StringBuilder sb = new StringBuilder();
            for (Map.Entry<String, String> entry : header.entrySet()) {
                if ("END_HEADER".equals(entry.getKey())) {
                    sb.append("END_HEADER\n");
                } else {
                    sb.append(entry.getKey()).append("=").append(entry.getValue()).append("\n");
                }
            }
            byte[] dataToSend = sb.toString().getBytes("UTF-8");

            //写入并发送DataOutputStream
            out.write(dataToSend);
            out.flush();
            
            System.out.println("未查询到: " + person.getName() + ", 已返回: null");

        } else {
            header.put("command", command);
            header.put("name", person.getName() != null ? person.getName() : "");
            header.put("phone", person.getPhoneNumber() != null ? person.getPhoneNumber() : "");
            header.put("birthday", person.getBirthday() != null ? person.getBirthday() : "");
            header.put("address", person.getAddress() != null ? person.getAddress() : "");

            // 写入图片的二进制数据
            byte[] imageBytes = new byte[0];
            String fileName = "";
            if (person.getImageURL() != null) {
                //如果图片存在，读取图片
                File imgFile = new File(person.getImageURL());
                if (imgFile.exists() && imgFile.isFile()) {
                    imageBytes = Files.readAllBytes(imgFile.toPath());
                    fileName = imgFile.getName();
                }
            }
            header.put("fileName", fileName);
            header.put("fileSize", String.valueOf(imageBytes.length));
            header.put("END_HEADER", "");

            // 拼接HEADER + 图片
            StringBuilder sb = new StringBuilder();
            for (Map.Entry<String, String> entry : header.entrySet()) {
                if ("END_HEADER".equals(entry.getKey())) {
                    sb.append("END_HEADER\n");
                } else {
                    sb.append(entry.getKey()).append("=").append(entry.getValue()).append("\n");
                }
            }
            byte[] headerBytes = sb.toString().getBytes("UTF-8");

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            baos.write(headerBytes);
            baos.write(imageBytes);
            byte[] dataToSend = baos.toByteArray();
            
            // 写入DataOutStream 并发送
            out.write(dataToSend);
            out.flush();
            
            System.out.println("已发送 PersonPackage: " + person.getName() + ", 图片大小: " + imageBytes.length + " 字节");
        }
    }
}



