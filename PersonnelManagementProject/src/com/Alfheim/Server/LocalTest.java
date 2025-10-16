package com.Alfheim.Server;

import com.Alfheim.DatabaseInterface.SQLite;

public class LocalTest {
    public static void main(String[] args) {
        SQLite db = new SQLite();
        db.connect();
        String query = "SELECT * FROM BaseInfo LIMIT 20;";
        try {
            var rs = db.executeQuery(query);
            while (rs != null && rs.next()) {
                System.out.println("ID: " + rs.getInt("id") + ", Name: " + rs.getString("name"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
