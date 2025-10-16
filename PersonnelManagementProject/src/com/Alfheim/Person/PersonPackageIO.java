package com.Alfheim.Person;
import java.sql.ResultSet;

public interface PersonPackageIO {
    public void Person(String name, String phoneNumber, String birthday, String address, byte[] image) throws Exception; //将缓存插入对象
    public void clear();
    public String InsertOrUpdate() throws Exception; //插入或更新基本信息和图片URL
    public String deletePersonInfo() throws Exception; //删除人员信息
    public ResultSet queryPersonInfo(); //以人名查询人员信息
    public ResultSet queryAllPersonInfo(); //查询所有人员信息
    public String getPersonFromDatabase();
    public String getName();
    public String getPhoneNumber();
    public String getBirthday();
    public String getAddress();
    public String getImageURL();
}
