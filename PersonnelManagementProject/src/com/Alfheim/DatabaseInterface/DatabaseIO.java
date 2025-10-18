package com.Alfheim.DatabaseInterface;
import java.sql.*;

public interface DatabaseIO {
    public void connect();  //连接数据库
    public void disconnect(); //断开连接
    public ResultSet executeQuery(String query);  //执行查询语句
    public int executeUpdate(String command); //执行更新语句
    public void insertQuery(String command); //执行插入语句
    public void deleteQuery(String command); //执行删除语句
    public void ImageQuery(String command); //执行图片插入语句
    Connection getConnection(); //创建或者获取数据库连接
}
