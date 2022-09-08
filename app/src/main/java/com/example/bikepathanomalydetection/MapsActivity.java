package com.example.bikepathanomalydetection;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.view.WindowManager;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.Task;
import com.google.maps.android.clustering.ClusterManager;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 * This class contains the main Google Maps activity and location tracking, as well as other visualisations.
 */
public class MapsActivity extends AppCompatActivity {

    // Managers
    private ClusterManager<Marker> clusterManager;
    private SensorManager sensorManager;

    // Maps
    private final long CITY_ZOOM = 10; // Zoom in to surrounding cities
    private final long STREET_ZOOM = 10; // Zoom in to street
    private final String ANOMALY = "Anomaly";
    private final String ANOMALY_DESCRIPTION = "Unknown!";
    private SupportMapFragment smf;
    private FusedLocationProviderClient client;

    // Location Requests
    private final long INTERVAL_TIME = 5000; // 1 second
    private final int LOCATION_COMPARE_DISTANCE = 2; // 2 meters
    private LocationRequest locationRequest;
    private Location lastLocation;

    // NoSQL
    private HashMap<Double, Set<Double>> anomalies;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        smf = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.google_map);
        client = LocationServices.getFusedLocationProviderClient(this);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        anomalies = new HashMap<>();

        Dexter.withContext(getApplicationContext())
                .withPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse permissionGrantedResponse) {
                        init();
                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse permissionDeniedResponse) {
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permissionRequest, PermissionToken permissionToken) {
                        permissionToken.continuePermissionRequest();
                    }
                }).check();
    }

    /**
     * Initialization method for this class.
     * This method sets up permissions, tasks and initializes event listeners and managers.
     */
    private void init() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            return;

        // Make Connection with database
        NoSQL.makeConnection(this);

        Task<Location> task = client.getLastLocation();
        task.addOnSuccessListener(location -> smf.getMapAsync(this::onMapReady));

        // Setup sensor activity
        SensorEventListener sensorListener = new SensorActivity(this);
        sensorManager.registerListener(sensorListener, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(sensorListener, sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE), SensorManager.SENSOR_DELAY_NORMAL);
    }

    /**
     * Initializes the cluster manager when the map is ready
     *
     * @param googleMap maps object passed when maps is ready
     */
    private void onMapReady(GoogleMap googleMap) {
        // Setup cluster functionality
        this.clusterManager = new ClusterManager<>(this, googleMap);
        googleMap.setOnMarkerClickListener(this.clusterManager);
        googleMap.setOnCameraIdleListener(this.clusterManager);

        // Setup location requests with fixed interval time
        locationRequest = new LocationRequest();
        locationRequest.setInterval(INTERVAL_TIME);
        locationRequest.setFastestInterval(INTERVAL_TIME);
        locationRequest.setSmallestDisplacement(1);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        LocationCallback mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult.getLastLocation() != null) {
//                    if (lastLocation != null && lastLocation.distanceTo(locationResult.getLastLocation()) < LOCATION_COMPARE_DISTANCE) return;

                    lastLocation = locationResult.getLastLocation();

                    //move map camera
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude()), STREET_ZOOM));
                }
            }
        };

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            //Location Permission already granted
            client.requestLocationUpdates(locationRequest, mLocationCallback, Looper.myLooper());
            googleMap.setMyLocationEnabled(true);
        } else {
            // TODO Request Location Permission
        }
    }

    /**
     * This function sets a marker on the map and adds it to the cluster manager
     *
     * @param lat latitude as a double
     * @param lng longitude as a double
     */
    public void setAnomalyMark(double lat, double lng) {
        if (this.clusterManager != null) {

            // If coordinates don't exist add to anomalies hashmap, return otherwise
            if (!anomalies.containsKey(lat)) {
                HashSet<Double> set = new HashSet<>();
                set.add(lng);
                anomalies.put(lat, set);
            } else {
                Set<Double> set = anomalies.get(lat);
                if (set.contains(lng)) return;
                set.add(lng);
            }

            Marker anomalyMark = new Marker(lat, lng, ANOMALY, ANOMALY_DESCRIPTION);
            this.clusterManager.addItem(anomalyMark);
        }
    }

    /**
     * Returns last known location of the device
     * @return Location object
     */
    public Location getLastLocation() {
        return this.lastLocation;
    }

    /**
     * This function calls the saveData function in the NoSQL class
     * @param data Data model containing the necessary data
     */
    public void saveData(Data data) {
        if (anomalyExists(data.getLatitude(), data.getLongitude())) return;
        NoSQL.saveData(data);
    }

    /**
     * Checks whether an anomaly already exists on our map
     * @param lat latitude as a double
     * @param lng longitude as a double
     * @return true if it exists, false otherwise
     */
    private boolean anomalyExists(double lat, double lng) {
        return anomalies.containsKey(lat) && anomalies.get(lat).contains(lng);
    }
}