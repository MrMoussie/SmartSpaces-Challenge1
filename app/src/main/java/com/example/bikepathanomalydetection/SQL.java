package com.example.bikepathanomalydetection;

import java.net.InetAddress;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class SQL {
    private final static String url = "jdbc:mysql://www.sql11.freemysqlhosting.net:3306/sql11517882";
    private final static String driverName = "com.mysql.cj.jdbc.Driver";
    private final static String username = "sql11517882";
    private final static String password = "eyMviDJYpD";

    // DELETE AFTER DEBUG
    private final static String url2 = "jdbc:mysql://localhost:3306/studyplanner_teaching_aids";
    private final static String username2 = "studyspace";
    private final static String password2 = "studyspace";

    private static Connection con;

    /**
     * Starts the connection to the database
     * @return Connection object representing the SQL connection
     */
    public static Connection getConnection() {
        try {
            Class.forName(driverName);
            con = DriverManager.getConnection(url, username, password);

            // TODO DEBUG

        } catch (ClassNotFoundException e) {
            System.out.println("Could not find driver for " + driverName);
            e.printStackTrace();
        } catch (SQLException e) {
            System.out.println("Failed to create database connection");
            e.printStackTrace();
        }

        return con;
    }

    /**
     * Closes the SQL Connection
     */
    public static void closeConnection() {
        if (con != null) {
            try {
                con.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * This function tests whether the internet works on the device
     * @return true if the internet works, false otherwise
     */
    public static boolean isInternet() {
        try {
            InetAddress ipAddr = InetAddress.getByName("sql11.freemysqlhosting.net");
            //You can replace it with your name
            return !ipAddr.equals("");
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static void getEntry() {

    }


    public static void deleteEntry() {

    }

    public static void addEntry() {

    }

    public static void updateEntry() {

    }
}
