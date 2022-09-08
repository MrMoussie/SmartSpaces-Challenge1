package com.example.bikepathanomalydetection;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.sql.Statement;

public class MyDatabase {
    private static String url = "jdbc:mysql://sql11.freemysqlhosting.net:3306";
    private static String driverName = "com.mysql.cj.jdbc.Driver"; // wrong driver
    private static String username = "sql11517882";
    private static String password = "eyMviDJYpD!";
    private static Connection con;
    private static String urlstring;

    public static Connection getConnection() {
        try {
            Class.forName(driverName);
            try {
                con = DriverManager.getConnection(url, username, password);
            } catch (SQLException e) {
                System.out.println("Failed to create database connection");
                e.printStackTrace();
            }
        } catch (ClassNotFoundException e) {
            System.out.println("Could not find driver for " + driverName);
            e.printStackTrace();
        }
        return con;
    }

    public static void runDBTest() {
        Connection con = null;
        Statement stmt = null;
        ResultSet rs = null;
        con = MyDatabase.getConnection();
        try {
            stmt = con.createStatement();
            rs = stmt.executeQuery("SELECT 1"); // test sql statement
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Something went wrong");
        }
        System.out.println(rs);
    }


    public static void deleteEntry() {

    }

    public static void addEntry() {

    }

    public static void updateEntry() {

    }

}
