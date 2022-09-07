package com.example.bikepathanomalydetection;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.Bundle;
import android.view.WindowManager;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import java.util.ArrayList;
import java.util.Arrays;

public class MapsActivity extends AppCompatActivity {

    private SensorManager sensorManager;
    private SensorEventListener sensorListener;

    private final long MIN_TIME = 1000; // 1 second
    private final long MIN_DIST = 5; // 5 meters
    private final long CITY_ZOOM = 10; // Zoom in to surrounding cities
    private final long STREET_ZOOM = 18; // Zoom in to street
    private long locationTimestamp = 0;
    //AveragingVariables
    final int arraySize = 7;
    int oldestIndexAccelerometer = 0;
    private float[] accelerometerValues = new float[3*arraySize];
    int oldestIndexGyro = 0;
    private float[] gyroscopeValues = new float[3*arraySize];
    private ArrayList averageGyro = new ArrayList();
    //FilterVariables
    private double[] filterAccelerometer = new double[]{-0.0421, 0.0606, 0.0540, 0.0494, 0.0338, 0.0018, -0.0451, -0.0994, -0.1506, -0.1870, 0.7998,-0.1870, -0.1506, -0.0994, -0.0451, 0.0018, 0.0338, 0.0494, 0.0540, 0.0606, -0.0421};
    int filterLengthAccelerometer = filterAccelerometer.length;
    int oldestIndexAccelerometerFilter = 0;
    private float[] filterBufferAccelerometer = new float[3*filterLengthAccelerometer];
    private ArrayList filterResultAccelerometer = new ArrayList();
    private double[] filterGyro = new double[]{-0.0421, 0.0606, 0.0540, 0.0494, 0.0338, 0.0018, -0.0451, -0.0994, -0.1506, -0.1870, 0.7998,-0.1870, -0.1506, -0.0994, -0.0451, 0.0018, 0.0338, 0.0494, 0.0540, 0.0606, -0.0421};
    int filterLengthGyro = filterGyro.length;
    int oldestIndexGyroFilter = 0;
    private float[] filterBufferGyro = new float[3*filterLengthGyro];
    private ArrayList filterResultGyro = new ArrayList();


    float sumX = 0;
    float sumY = 0;
    float sumZ = 0;

    float convolutionX = 0;
    float convolutionY = 0;
    float convolutionZ = 0;

    SupportMapFragment smf;
    FusedLocationProviderClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        smf = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.google_map);
        client = LocationServices.getFusedLocationProviderClient(this);

        Arrays.fill(accelerometerValues, 0);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

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


    public void init() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        Task<Location> task = client.getLastLocation();
        task.addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(final Location location) {
                smf.getMapAsync(new OnMapReadyCallback() {
                    @Override
                    public void onMapReady(GoogleMap googleMap) {
                        LatLng latLng=new LatLng(location.getLatitude(),location.getLongitude());
                        MarkerOptions markerOptions=new MarkerOptions().position(latLng).title("You are here...!!");

                        googleMap.addMarker(markerOptions);
                        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng,17));
                    }
                });
            }
        });

        sensorListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent sensorEvent) {
                Sensor sensor = sensorEvent.sensor;

                switch(sensor.getType()) {
                    case Sensor.TYPE_ACCELEROMETER:
                        for (int i = 0; i < 3; i++) {
                            accelerometerValues[oldestIndexAccelerometer*3+i] = (sensorEvent.values[i]);
                        }
                        oldestIndexAccelerometer = (oldestIndexAccelerometer+1)%arraySize;
                        if (accelerometerValues[3*arraySize-1] != 0) {
                            sumX = 0;
                            sumY = 0;
                            sumZ = 0;
                            for (int i = 0; i < 3*arraySize; i++) {
                                if (i % 3 == 0) {
                                    sumX += (float) accelerometerValues[i];
                                } else if (i % 3 == 1) {
                                    sumY += (float) accelerometerValues[i];
                                } else {
                                    sumZ += (float) accelerometerValues[i];
                                }
                            }
                            //FilterBuffer:
                            filterBufferAccelerometer[3*oldestIndexAccelerometerFilter] = sumX;
                            filterBufferAccelerometer[3*oldestIndexAccelerometerFilter+1] = sumY;
                            filterBufferAccelerometer[3*oldestIndexAccelerometerFilter+2] = sumZ;
                            oldestIndexAccelerometerFilter = (oldestIndexAccelerometerFilter+1)%filterLengthAccelerometer;
                            //ConvolutionSum:
                            convolutionX = 0;
                            convolutionY = 0;
                            convolutionZ = 0;
                            int arrayIndex = 3*oldestIndexAccelerometerFilter;
                            for(int i = 0; i < 3*filterLengthAccelerometer; i ++){
                                if (arrayIndex % 3 == 0) {
                                    convolutionX+=filterAccelerometer[(filterLengthAccelerometer-1)-(i-0)/3]*filterBufferAccelerometer[arrayIndex];
                                } else if (arrayIndex % 3 == 1) {
                                    convolutionY+=filterAccelerometer[(filterLengthAccelerometer-1)-(i-1)/3]*filterBufferAccelerometer[arrayIndex];
                                } else {
                                    convolutionZ+=filterAccelerometer[(filterLengthAccelerometer-1)-(i-2)/3]*filterBufferAccelerometer[arrayIndex];
                                }

                                if(arrayIndex>=3*filterLengthAccelerometer-1){
                                    arrayIndex = 0;
                                }
                                else{
                                    arrayIndex++;
                                }
                            }
                            if(filterResultAccelerometer.size()<30) {
                                filterResultAccelerometer.add(convolutionX);
                                filterResultAccelerometer.add(convolutionY);
                                filterResultAccelerometer.add(convolutionZ);
                            }

                            System.out.println(Arrays.toString(sensorEvent.values));

                        }
                        break;

                    case Sensor.TYPE_GYROSCOPE:
                        for(int i = 0; i < 3; i++) {
                            gyroscopeValues[oldestIndexGyro*3+i] = (sensorEvent.values[i]);
                        }
                        oldestIndexGyro = (oldestIndexGyro+1)%arraySize;
                        if (gyroscopeValues[3*arraySize-1] != 0) {
                            sumX = 0;
                            sumY = 0;
                            sumZ = 0;
                            for (int i = 0; i < 3*arraySize; i++) {
                                if (i % 3 == 0) {
                                    sumX += (float) gyroscopeValues[i];
                                } else if (i % 3 == 1) {
                                    sumY += (float) gyroscopeValues[i];
                                } else {
                                    sumZ += (float) gyroscopeValues[i];
                                }
                            }
                            //FilterBuffer:
                            filterBufferGyro[3*oldestIndexGyroFilter] = sumX;
                            filterBufferGyro[3*oldestIndexGyroFilter+1] = sumY;
                            filterBufferGyro[3*oldestIndexGyroFilter+2] = sumZ;
                            oldestIndexGyroFilter = (oldestIndexGyroFilter+1)%filterLengthGyro;
                            //ConvolutionSum:
                            convolutionX = 0;
                            convolutionY = 0;
                            convolutionZ = 0;
                            int arrayIndex = 3*oldestIndexGyroFilter;
                            for(int i = 0; i < 3*filterLengthGyro; i ++){
                                if (arrayIndex % 3 == 0) {
                                    convolutionX+=filterGyro[(filterLengthGyro-1)-(i-0)/3]*filterBufferGyro[arrayIndex];
                                } else if (arrayIndex % 3 == 1) {
                                    convolutionY+=filterGyro[(filterLengthGyro-1)-(i-1)/3]*filterBufferGyro[arrayIndex];
                                } else {
                                    convolutionZ+=filterGyro[(filterLengthGyro-1)-(i-2)/3]*filterBufferGyro[arrayIndex];
                                }

                                if(arrayIndex>=3*filterLengthGyro-1){
                                    arrayIndex = 0;
                                }
                                else{
                                    arrayIndex++;
                                }
                            }
                            if(filterResultGyro.size()<30) {
                                filterResultGyro.add(convolutionX);
                                filterResultGyro.add(convolutionY);
                                filterResultGyro.add(convolutionZ);
                            }
                        }
                        break;
                    default:
                        System.out.println("[SYSTEM] ENTERED DEFAULT WITH: " + sensor.getType());
                        break;
                }
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int i) {
            }
        };

        try {
            sensorManager.registerListener(sensorListener, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);
            sensorManager.registerListener(sensorListener, sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE), SensorManager.SENSOR_DELAY_NORMAL);
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }
}