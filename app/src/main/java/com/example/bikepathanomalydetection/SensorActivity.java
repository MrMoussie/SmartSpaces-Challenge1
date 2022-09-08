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


    float sumX = 0;
    float sumY = 0;
    float sumZ = 0;

    float changeFactorAccelerometer = 0.3F;
    float thresholdAccelerometer = 0.6F;

    float changeFactorGyro = 0.3F;
    float thresholdGyro = 0.8F;

    double prevResultAccX = 0;
    double prevResultAccY = 0;
    double prevResultAccZ = 0;
    double prevResultGyrX = 0;
    double prevResultGyrY = 0;
    double prevResultGyrZ = 0;

    double resultAccX = 0;
    double resultAccY = 0;
    double resultAccZ = 0;
    double resultGyrX = 0;
    double resultGyrY = 0;
    double resultGyrZ = 0;



    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        Sensor sensor = sensorEvent.sensor;

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

                    //convert sum into average
                    sumX = sumX/arraySize;
                    sumY = sumY/arraySize;
                    sumZ = sumZ/arraySize;

                    prevResultAccX = resultAccX;
                    prevResultAccY = resultAccY;
                    prevResultAccZ = resultAccZ;

                    resultAccX = (1-changeFactorAccelerometer)*prevResultAccX+changeFactorAccelerometer*sumX;
                    resultAccY = (1-changeFactorAccelerometer)*prevResultAccY+changeFactorAccelerometer*sumY;
                    resultAccZ = (1-changeFactorAccelerometer)*prevResultAccZ+changeFactorAccelerometer*sumZ;

                    if(        Math.abs(prevResultAccX-sumX)>thresholdAccelerometer
                            || Math.abs(prevResultAccY-sumY)>thresholdAccelerometer
                            || Math.abs(prevResultAccZ-sumZ)>thresholdAccelerometer){

                        // Threshold was reached
                        // add the entries to the database
                        // put a marker on the map
//                        System.out.println("[Dif AccX] " + (prevResultAccX-sumX));
//                        System.out.println("[Sum AccX] " + sumX);
//                        System.out.println("[Dif AccY] " + (prevResultAccY-sumY));
//                        System.out.println("[Sum AccY] " + sumY);
//                        System.out.println("[Dif AccZ] " + (prevResultAccZ-sumZ));
//                        System.out.println("[Sum AccZ] " + sumZ);
                    }
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

                    //convert sum into average
                    sumX = sumX/arraySize;
                    sumY = sumY/arraySize;
                    sumZ = sumZ/arraySize;

                    prevResultGyrX = resultGyrX;
                    prevResultGyrY = resultGyrY;
                    prevResultGyrZ = resultGyrZ;

                    resultGyrX = (1-changeFactorGyro)*prevResultGyrX+changeFactorGyro*sumX;
                    resultGyrY = (1-changeFactorGyro)*prevResultGyrY+changeFactorGyro*sumY;
                    resultGyrZ = (1-changeFactorGyro)*prevResultGyrZ+changeFactorGyro*sumZ;

                    if(        Math.abs(prevResultGyrX-sumX)>thresholdGyro
                            || Math.abs(prevResultGyrY-sumY)>thresholdGyro
                            || Math.abs(prevResultGyrZ-sumZ)>thresholdGyro){

                        // Threshold was reached
                        // add the entries to the database
                        // put a marker on the map

                        System.out.println("[Dif GyrX] " + (prevResultGyrX-sumX));
                        System.out.println("[Sum GyrX] " + sumX);
                        System.out.println("[Dif GyrY] " + (prevResultGyrY-sumY));
                        System.out.println("[Sum GyrY] " + sumY);
                        System.out.println("[Dif GyrZ] " + (prevResultGyrZ-sumZ));
                        System.out.println("[Sum GyrZ] " + sumZ);
                    }
                }
                break;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {}
}
