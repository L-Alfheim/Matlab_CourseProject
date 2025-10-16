package com.Alfheim.DatabaseInterface;
import java.sql.*;


public class SQLite implements DatabaseIO {

    private Connection c = null;      //连接对象
    private Statement stmt = null;    //stmt为SQL语句对象

    //无参构造函数，用于matlab实例化
    public SQLite() {
    }

    /**
     * 数据库连接
     */
    @Override
    public void connect() {
      try {
          Class.forName("org.sqlite.JDBC");
          c = DriverManager.getConnection("jdbc:sqlite:database/Person.db");
          System.out.println("Database connected successfully.");
      } catch (Exception e) {
          System.err.println(e.getClass().getName() + ": " + e.getMessage());
      }
    }

    /**
     * 断开数据库连接
     */
    @Override
    public void disconnect() {
      try {
          if (stmt != null) stmt.close();
          if (c != null) c.close();
          System.out.println("Database disconnected successfully.");
      } catch (SQLException e) {
          System.err.println(e.getClass().getName() + ": " + e.getMessage());
      }
    }

    /**
     * 执行语句
     * @param query
     * @return ResultSet
     */
    @Override
    public ResultSet executeQuery(String query) {
      try {
          stmt = c.createStatement();
          return stmt.executeQuery(query);
      } catch (SQLException e) {
          System.err.println(e.getClass().getName() + ": " + e.getMessage());
          return null;
      }
    }

    /**
     * 执行更新语句
     * @param command
     * @return
     */
    @Override
    public int executeUpdate(String command) {
      try {
          stmt = c.createStatement();
          return stmt.executeUpdate(command);
      } catch (SQLException e) {
          System.err.println(e.getClass().getName() + ": " + e.getMessage());
          return -1;
      }
    }

    /**
     * 执行插入语句
     * @param command
     */
    @Override
    public void insertQuery(String command) {
      try {
          stmt = c.createStatement();
          stmt.executeUpdate(command);
      } catch (SQLException e) {
          System.err.println(e.getClass().getName() + ": " + e.getMessage());
      }
    }

    /**
     * 执行删除语句
     * @param command
     */
    @Override
    public void deleteQuery(String command) {
      try {
          stmt = c.createStatement();
          stmt.executeUpdate(command);
      } catch (SQLException e) {
          System.err.println(e.getClass().getName() + ": " + e.getMessage());
      }
    }

    /**
     * 执行图片插入语句
     * @param command
     */
    @Override
    public void ImageQuery(String command) {
      try {
          stmt = c.createStatement();
          stmt.executeUpdate(command);
      } catch (SQLException e) {
          System.err.println(e.getClass().getName() + ": " + e.getMessage());
      }
    }
}

