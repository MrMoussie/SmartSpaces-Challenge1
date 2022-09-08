package com.example.bikepathanomalydetection;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.net.InetAddress;

public class NoSQL {
    private static FirebaseDatabase database;

    /**
     * Starts the connection to the database
     * @return void
     */
    public static void makeConnection() {
        database = FirebaseDatabase.getInstance();
    }

    /**
     * Closes the SQL Connection
     */
    public static void closeConnection() {
        if (database != null) database.goOffline();
    }

    /**
     * This function tests whether the internet works on the device
     * @return true if the internet works, false otherwise
     */
    public static boolean isInternet() {
        try {
            InetAddress ipAddr = InetAddress.getByName("google.com");
            //You can replace it with your name
            return !ipAddr.equals("");
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static void getData() {

    }

    public static void deleteData() {

    }

    /**
     * This function saves a data object into the NoSQL database
     * @param data Data object to be saved
     */
    public static void saveData(Data data) {
        if (database != null) {
            DatabaseReference ref = database.getReference().push();
            ref.setValue(data);
            data.setKey(ref.getKey());
        }
    }

    public static void updateData() {

    }
}
