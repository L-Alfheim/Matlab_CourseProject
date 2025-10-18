package com.Alfheim.DatabaseInterface;
import java.sql.*;


public class SQLite implements DatabaseIO {

    private static Connection c = null;      //连接对象
    private Statement stmt = null;    //stmt为SQL语句对象

    //无参构造函数，用于matlab实例化
    public SQLite() {
    }

    /**
     * 创建或者获取数据库连接
     * 这个方法取代了原有的 connect 方法，确保每次调用都返回同一个连接对象，避免频繁创建连接带来的性能问题。
     * @version 1.0.1
     * @return Connection 数据库连接对象
     */
    public synchronized Connection getConnection() {
        if (c == null) {
            try {
                Class.forName("org.sqlite.JDBC");
                c = DriverManager.getConnection("jdbc:sqlite:database/Person.db");
                System.out.println("Database connected successfully.");
            } catch (SQLException e) {
                System.err.println(e.getMessage());
                e.printStackTrace();
            }catch (ClassNotFoundException e) {
                System.err.println(e.getClass().getName() + ": " + e.getMessage());
                e.printStackTrace();
            }
        }
        return c;
    }

    /**
     * 数据库连接
     * @deprecated 此方法已废弃，请使用 {@link #getConnection()} 代替。
     * @version 1.0.0
     * 每次调用都会新建连接，效率低
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