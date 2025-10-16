/**
 * 这个类用于打包完整的人员信息，包括基本信息和面部信息
 */
package com.Alfheim.Person;
import java.io.File;
import java.io.FileOutputStream;
import com.Alfheim.DatabaseInterface.DatabaseIO;
import com.Alfheim.DatabaseInterface.SQLite;

import java.sql.*;

public class PersonPackage implements PersonPackageIO {
    private String Name; //姓名,初次解包写入
    private String phoneNumber; //电话号码，初次解包写入
    private String Birthday; //生日，初次解包写入
    private String Address;  //籍贯，初次解包写入
    private int personId; //人员ID,用于关联Person表
    private String imageURL;  //面部信息的图片路径
    private byte[] image; //面部信息的图片字节数组，初次解包写入

    DatabaseIO db = new SQLite();  //数据库连接对象

    //构造函数
    public PersonPackage() {
        try {
            // 连接数据库
            db.connect();
        } catch (Exception e) {
            System.err.println("Database connection failed: " + e.getMessage());
        }
    }

    //析构函数
    public void clear() {
        // 断开数据库连接
        Name = null;
        phoneNumber = null;
        Birthday = null;
        Address = null;
        personId = 0;
        imageURL = null;
        image = null;
    }

    //将缓存插入对象
    public void Person(String name, String phoneNumber, String birthday, String address, byte[] image) throws Exception {
        Name = name;
        this.phoneNumber = phoneNumber;
        this.Birthday = birthday;
        this.Address = address;
        this.image = new byte[image.length];
        this.image = image;
    }

    //保存图片到指定地址
    private void saveImage() {
        // 生成文件名和路径（保存为jpg图片）
        String fileName = "img_" + System.currentTimeMillis() + ".png"; // 使用时间戳生成唯一文件名
        File dir = new File("database/images"); // 确保目录存在

        // 如果目录不存在则创建
        if (!dir.exists()) {
            dir.mkdirs();
        }

        // 写入图片文件
        File file = new File(dir, fileName);
        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(image);
            fos.flush();
        } catch (Exception e) {
            System.err.println("保存图片失败: " + e.getMessage());
            return;
        }
        System.out.println("图像已保存至：" + file.getAbsolutePath());
        // 更新imageURL
        imageURL = file.getAbsolutePath();
    }

    //保存图片URL到数据库
    private void saveImageURL() {
        // 连接数据库
        try {
            // 插入图片URL到Person表
            String sql = "INSERT INTO FaceRecord (personid, imageURL) VALUES ('" + personId + "', '" + imageURL.replace("\\", "/") + "');\n";
            db.executeQuery(sql);
            System.out.println("图片URL插入成功");
        } catch (Exception e) {
            System.err.println("保存图片URL到数据库失败: " + e.getMessage());
        }
    }

    //更新图片URL到数据库
    private void updateImageURL() {
        try {
            String sql = "UPDATE FaceRecord SET imageURL = '" + imageURL.replace("\\", "/") + "' WHERE personid = " + personId + ";\n"; 
            db.executeUpdate(sql);
            System.out.println("图片URL更新成功");
        } catch (Exception e) {
            System.err.println("更新图片URL到数据库失败: " + e.getMessage());
        }
    }

    //插入人员基本信息到数据库
    private void insertPersonInfo() {
        String sql = "INSERT INTO Person(Name, phoneNumber, Birthday, Address) VALUES('" + this.Name + "', '" + this.phoneNumber + "', '" + this.Birthday + "', '" + this.Address + "')\n";
        try {
            // 插入基本信息到Person表
            db.executeUpdate(sql);
            System.out.println("数据插入成功");
        } catch (Exception e) {
            System.err.println("插入失败: " + e.getMessage());
        }
    }

    //更新人员基本信息到数据库
    private void updatePersonInfo() {
        String sql = "UPDATE Person SET phoneNumber = '" + this.phoneNumber + "', Birthday = '" + this.Birthday + "', Address = '" + this.Address + "' WHERE Name = '" + this.Name + "'\n";
        try {
            db.executeUpdate(sql);
            System.out.println("数据更新成功");
        } catch (Exception e) {
            System.err.println("更新失败: " + e.getMessage());
        }
    }

    //插入或者更新人员的全部信息
    public String InsertOrUpdate() {
        try {
            // 先查询是否存在该人员
            String sql = "SELECT id FROM Person WHERE Name = '" + Name + "'\n";
            ResultSet rs = db.executeQuery(sql);
            if (rs.next()) {
                personId = rs.getInt("id"); //获取关联id
                // 如果存在则更新
                updatePersonInfo();
                saveImage();    //保存图片到本地
                updateImageURL();
                return "数据更新成功";
            } else {
                // 如果不存在则插入
                insertPersonInfo(); //保存基本信息
                sql = "SELECT id FROM Person WHERE Name = '" + Name + "'\n";    //查询
                rs = db.executeQuery(sql);  //获取关联id
                personId = rs.getInt("id");
                saveImage();      //保存图片到本地
                saveImageURL();   //保存图片URL到数据库
                return "数据插入成功";
            }
        } catch (Exception e) {
            System.err.println("插入或更新失败: " + e.getMessage());
            return "插入或更新失败: " + e.getMessage();
        }
    }

    //删除信息
    public String deletePersonInfo() {
        try {
            String sql = "DELETE FROM Person WHERE Name = '" + this.Name + "'\n";
            int rowsEffect = db.executeUpdate(sql);
            if (rowsEffect > 0) {
                return "删除成功";
            } else {
                return "删除失败，数据库不存在" + this.Name + "的信息";
            }
        } catch (Exception e) {
            System.err.println("删除失败: " + e.getMessage());
            return "删除失败: " + e.getMessage();
        }
    }

    //查询人员信息
    public ResultSet queryPersonInfo() {
        String sql = "SELECT * FROM Person WHERE Name = '" + Name + "'\n";
        try {
            return db.executeQuery(sql);
        } catch (Exception e) {
            System.err.println("查询失败: " + e.getMessage());
            return null;
        }
    }

    //查询所有人员信息
    public ResultSet queryAllPersonInfo() {
        String sql = "SELECT * FROM Person\n";
        try {
            return db.executeQuery(sql);
        } catch (Exception e) {
            System.err.println("查询失败: " + e.getMessage());
            return null;
        }
    }
    
    //查询图像信息
    private String queryImageInfo() {
        try {
            String sql = "SELECT * FROM Person WHERE Name = '" + Name +"'\n";
            ResultSet result = db.executeQuery(sql);
            personId = result.getInt("id");
            sql = "SELECT * FROM FaceRecord WHERE personId = '" + personId +"'\n";
            result = db.executeQuery(sql);
            if (result.next()) {
                return result.getString("imageURL");
            }else{
                return "NULL";
            }
        } catch (Exception e) {
            System.err.println("查询失败: " + e.getMessage());
            return null;
        }
    }

    //获取单个人员详细信息
    public String getPersonFromDatabase(){
        ResultSet rs = queryPersonInfo();
        try {
            if (!rs.next()) {
                return "Query returned no result.";
            } else {
                phoneNumber = rs.getString("phoneNumber");
                Address = rs.getString("Address");
                Birthday = rs.getString("Birthday");
                imageURL = queryImageInfo();
            }
        } catch (SQLException e) {
            System.err.println("获取人员信息失败: " + e.getMessage());
            return "获取人员信息失败: " + e.getMessage();
        }
        
        return "查询到一条信息";
    }

    public String getName() {
        return this.Name;
    }

    public String getPhoneNumber() {
        return this.phoneNumber;
    }

    public String getBirthday() {
        return this.Birthday;
    }

    public String getAddress() {
        return this.Address;
    }

    public String getImageURL() {
        return this.imageURL;
    }
}
