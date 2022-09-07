package com.example.bikepathanomalydetection;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import java.util.ArrayList;

/**
 * This class implements the SensorEventListener class and listens for sensor events.
 */
public class SensorActivity implements SensorEventListener {

    //AveragingVariables
    final int arraySize = 7;
    int oldestIndexAccelerometer = 0;
    private final float[] accelerometerValues = new float[3*arraySize];
    int oldestIndexGyro = 0;
    private final float[] gyroscopeValues = new float[3*arraySize];
    private final ArrayList<Double> averageGyro = new ArrayList<>();

    //FilterVariables
    private final double[] filterAccelerometer = new double[]{-0.0421, 0.0606, 0.0540, 0.0494, 0.0338, 0.0018, -0.0451, -0.0994, -0.1506, -0.1870, 0.7998,-0.1870, -0.1506, -0.0994, -0.0451, 0.0018, 0.0338, 0.0494, 0.0540, 0.0606, -0.0421};
    int filterLengthAccelerometer = filterAccelerometer.length;
    int oldestIndexAccelerometerFilter = 0;
    private final float[] filterBufferAccelerometer = new float[3*filterLengthAccelerometer];
    private final ArrayList<Float> filterResultAccelerometer = new ArrayList<>();
    private final double[] filterGyro = new double[]{-0.0421, 0.0606, 0.0540, 0.0494, 0.0338, 0.0018, -0.0451, -0.0994, -0.1506, -0.1870, 0.7998,-0.1870, -0.1506, -0.0994, -0.0451, 0.0018, 0.0338, 0.0494, 0.0540, 0.0606, -0.0421};
    int filterLengthGyro = filterGyro.length;
    int oldestIndexGyroFilter = 0;
    private final float[] filterBufferGyro = new float[3*filterLengthGyro];
    private final ArrayList<Float> filterResultGyro = new ArrayList<>();

    float sumX = 0;
    float sumY = 0;
    float sumZ = 0;

    float convolutionX = 0;
    float convolutionY = 0;
    float convolutionZ = 0;

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
                            convolutionX+=filterAccelerometer[(filterLengthAccelerometer-1)-(i)/3]*filterBufferAccelerometer[arrayIndex];
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

                    // FilterBuffer
                    filterBufferGyro[3*oldestIndexGyroFilter] = sumX;
                    filterBufferGyro[3*oldestIndexGyroFilter+1] = sumY;
                    filterBufferGyro[3*oldestIndexGyroFilter+2] = sumZ;
                    oldestIndexGyroFilter = (oldestIndexGyroFilter+1)%filterLengthGyro;

                    //ConvolutionSum
                    convolutionX = 0;
                    convolutionY = 0;
                    convolutionZ = 0;
                    int arrayIndex = 3*oldestIndexGyroFilter;
                    for(int i = 0; i < 3*filterLengthGyro; i ++){
                        if (arrayIndex % 3 == 0) {
                            convolutionX+=filterGyro[(filterLengthGyro-1)-(i)/3]*filterBufferGyro[arrayIndex];
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
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {}
}
