package com.example.bikepathanomalydetection;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

public class DatabaseEventListener implements ValueEventListener {

    private MapsActivity mapsActivity;

    public DatabaseEventListener(MapsActivity mapsActivity) {
        this.mapsActivity = mapsActivity;
    }

    @Override
    public void onDataChange(@NonNull DataSnapshot snapshot) {
        for (DataSnapshot data : snapshot.getChildren()) {
            if (data == null) return;

            mapsActivity.setAnomalyMark((double) data.child("latitude").getValue(), (double) data.child("longitude").getValue());
        }

    }

    @Override
    public void onCancelled(@NonNull DatabaseError error) {

    }
}
