package com.example.bikepathanomalydetection;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class NoSQL {
    private static FirebaseDatabase database;

    /**
     * Starts the connection to the database
     * @return void
     */
    public static void makeConnection(MapsActivity mapsActivity) {
        database = FirebaseDatabase.getInstance();
        database.getReference().addValueEventListener(new DatabaseEventListener(mapsActivity));
    }

    /**
     * Closes the SQL Connection
     */
    public static void closeConnection() {
        if (database != null) database.goOffline();
    }

    /**
     * This function saves a data object into the NoSQL database
     * @param data Data object to be saved
     */
    public static void saveData(Data data) {
        System.out.println(database);
        if (database != null) {
            DatabaseReference ref = database.getReference().push();
            ref.setValue(data);
        }
    }
}
