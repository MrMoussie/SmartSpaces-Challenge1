package com.example.bikepathanomalydetection;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import java.util.ArrayList;
import uk.me.berndporr.iirj.*;

/**
 * This class implements the SensorEventListener class and listens for sensor events.
 */
public class SensorActivity implements SensorEventListener {

    //AveragingVariables:
    final int arraySize = 7;
    int oldestIndexAccelerometer = 0;
    private final float[] accelerometerValues = new float[3*arraySize];
    int oldestIndexGyro = 0;
    int arrayLength = 300;
    private final float[] gyroscopeValues = new float[3*arraySize];
    private final ArrayList<Double> filterResultAccelerometer = new ArrayList<>();
    private final ArrayList<Double> filterResultGyro = new ArrayList<>();

    Butterworth accX = null;
    Butterworth accY = null;
    Butterworth accZ = null;
    Butterworth gyrX = null;
    Butterworth gyrY = null;
    Butterworth gyrZ = null;

    float sumX = 0;
    float sumY = 0;
    float sumZ = 0;

    public void filterSetup(){
        accX = new Butterworth();
        accY = new Butterworth();
        accZ = new Butterworth();
        gyrX = new Butterworth();
        gyrY = new Butterworth();
        gyrZ = new Butterworth();
        gyrX.highPass(20,10,3);
        gyrY.highPass(20,10,3);
        gyrZ.highPass(20,10,3);
        accX.highPass(20,10,3);
        accY.highPass(20,10,3);
        accZ.highPass(20,10,3);
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        Sensor sensor = sensorEvent.sensor;
        if(accX == null || accY == null || accZ == null ||
            gyrX == null || gyrY == null || gyrZ == null){
            filterSetup();
        }

        switch(sensor.getType()) {
            case Sensor.TYPE_ACCELEROMETER:
                for (int i = 0; i < 3; i++) {
                    accelerometerValues[oldestIndexAccelerometer*3+i] = (sensorEvent.values[i]);
                }
                oldestIndexAccelerometer = (oldestIndexAccelerometer+1) % arraySize;
                if (accelerometerValues[3*arraySize-1] != 0) {
                    sumX = 0;
                    sumY = 0;
                    sumZ = 0;
                    for (int i = 0; i < 3*arraySize; i++) {
                        if (i % 3 == 0) {
                            sumX += accelerometerValues[i];
                        } else if (i % 3 == 1) {
                            sumY += accelerometerValues[i];
                        } else {
                            sumZ += accelerometerValues[i];
                        }
                    }



                    if(filterResultGyro.size()<arrayLength) {
                        filterResultAccelerometer.add(accX.filter(sumX/arraySize));
                        filterResultAccelerometer.add(accY.filter(sumY/arraySize));
                        filterResultAccelerometer.add(accZ.filter(sumZ/arraySize));
                    }
                    System.out.println(accX.filter(sumX) + "   " + sumX + " [accelerometer X]");
                    System.out.println(accY.filter(sumY) + "   " + sumY + " [accelerometer Y]");
                    System.out.println(accZ.filter(sumZ) + "   " + sumZ + " [accelerometer Z]");

//                    try {
//                        Thread.sleep(2000);
//                    } catch (InterruptedException e) {
//
//                    }
                }
                break;
            case Sensor.TYPE_GYROSCOPE:
                for(int i = 0; i < 3; i++) {
                    gyroscopeValues[oldestIndexGyro*3+i] = sensorEvent.values[i];
                }
                oldestIndexGyro = (oldestIndexGyro+1)%arraySize;
                if (gyroscopeValues[3*arraySize-1] != 0) {
                    sumX = 0;
                    sumY = 0;
                    sumZ = 0;
                    for (int i = 0; i < 3*arraySize; i++) {
                        if (i % 3 == 0) {
                            sumX += gyroscopeValues[i];
                        } else if (i % 3 == 1) {
                            sumY += gyroscopeValues[i];
                        } else {
                            sumZ += gyroscopeValues[i];
                        }
                    }

                    if(filterResultGyro.size()<arrayLength) {
                        filterResultGyro.add(gyrX.filter(sumX/arraySize));
                        filterResultGyro.add(gyrY.filter(sumY/arraySize));
                        filterResultGyro.add(gyrZ.filter(sumZ/arraySize));
                    }

                    System.out.println(accX.filter(sumX) + "   " + sumX);
                    System.out.println(accY.filter(sumY) + "   " + sumY);
                    System.out.println(accZ.filter(sumZ) + "   " + sumZ);
                }
                break;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {}
}
