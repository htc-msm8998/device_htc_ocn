/*
 * Copyright (C) 2014 SlimRoms Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.slim.device;

import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.SystemClock;
import android.os.Vibrator;
import android.os.VibrationEffect;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;

import java.text.SimpleDateFormat;
import java.util.Iterator;

import slim.utils.FileUtils;
import slim.action.Action;
import slim.action.ActionConstants;

import com.slim.device.settings.Gesture;

public class SqueezeService extends Service implements SensorEventListener {

    private static final boolean DEBUG = false;

    private static final int SHORTSQUEEZE = 100;
    private static final int LONGSQUEEZE = 101;

    public static final String TAG = "SqueezeService";
    public static final String HTC_EDGESENSOR = "hTC Edge Sensor";

    private static final String EDGE_THRESHOLD_PATH=
            "/sys/class/htc_sensorhub/sensor_hub/edge_thd";

    public static final String PREF_SQUEEZE_FORCE = "squeeze_force";
    private static final int DEFAULT_SQUEEZE_FORCE = 80;

    private Context mContext;
    private SharedPreferences mPrefs;
    private SensorManager mSensorManager;
    private Sensor mEdgeSensor = null;
    private SensorEventListener mEdgeSensorEventListener;
    private long mGestureUpTime = 0;
    private int mForcePref = 0;
    private String[] mIntStrings = new String[10];

    long tStart = System.currentTimeMillis();
    long tEnd = System.currentTimeMillis();
    long tDelta =0;

    @Override
    public void onCreate() {
        super.onCreate();

        mContext = getApplicationContext();
        mPrefs = getSharedPreferences(Gesture.GESTURE_SETTINGS, Activity.MODE_PRIVATE);

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        mEdgeSensorEventListener = this;

        Iterator iterator = mSensorManager.getSensorList(-1).iterator();
        while (iterator.hasNext()) {
            Sensor sensor = (Sensor) iterator.next();
            if (sensor.getName().equals(HTC_EDGESENSOR)) {
                if (DEBUG) Log.d(TAG, "found Edge sensor");
                mEdgeSensor = sensor;
                mForcePref=mPrefs.getInt(PREF_SQUEEZE_FORCE,DEFAULT_SQUEEZE_FORCE);
                if (!FileUtils.writeLine(EDGE_THRESHOLD_PATH, Integer.toString(mForcePref))) {
                    Log.w(TAG, "Failed to write force threshold sysfs path");
                }
            }
        }
        if (mEdgeSensor != null) {
            if (mPrefs.getBoolean(Gesture.PREF_SQUEEZE_GESTURE_ENABLE, true)) {
                mSensorManager.registerListener(mEdgeSensorEventListener,
                        mEdgeSensor, SensorManager.SENSOR_DELAY_FASTEST);
                if (DEBUG) Log.d(TAG, "Registered Edge Sensor Listener");
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mSensorManager.unregisterListener(mEdgeSensorEventListener);
    }

    public IBinder onBind(Intent intent) {
        return null;
    }

    public final void onAccuracyChanged(Sensor sensor, int i) {
    }

    public void onSensorChanged(SensorEvent sensorEvent) {
        float averageValue = ((sensorEvent.values[8] + sensorEvent.values[9])/2);
           if (DEBUG) {
               Log.d(TAG, "sensorEvent.values[8]=" + sensorEvent.values[8]);
               Log.d(TAG, "sensorEvent.values[9]=" + sensorEvent.values[9]);
               Log.d(TAG, "averageForce=" + averageValue);
           }
        if (averageValue >= mForcePref) {
           tEnd = System.currentTimeMillis();
        } else {
           tDelta = tEnd - tStart;
           if (tDelta >=100 && tDelta <=700) {
               startAction(SHORTSQUEEZE);
           } else if (tDelta > 700) {
               startAction(LONGSQUEEZE);
           }
           tStart = System.currentTimeMillis();
           tEnd = System.currentTimeMillis();
           tDelta = 0;
        }
    }

    private void startAction(int gesture) {
        String action = null;
        switch (gesture) {
            case SHORTSQUEEZE:
                action = mPrefs.getString(Gesture.PREF_SHORT_SQUEEZE,
                        ActionConstants.ACTION_CAMERA);
                break;
            case LONGSQUEEZE:
                action = mPrefs.getString(Gesture.PREF_LONG_SQUEEZE,
                        ActionConstants.ACTION_SCREENSHOT);
                break;

        }
        if (action == null || action != null && action.equals(ActionConstants.ACTION_NULL)) {
            return;
        }
        if (mPrefs.getBoolean(Gesture.PREF_SQUEEZE_GESTURE_HAPTIC_ENABLE, true)) {
            Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
            v.vibrate(VibrationEffect.createOneShot(500,VibrationEffect.DEFAULT_AMPLITUDE));
        }
        if (action.equals(ActionConstants.ACTION_CAMERA)
                || !action.startsWith("**")) {
            Action.processAction(mContext, ActionConstants.ACTION_WAKE_DEVICE, false);
        }
        if (DEBUG) Log.d(TAG, action + ",gesture=" + gesture);
        mSensorManager.registerListener(mEdgeSensorEventListener,
                mEdgeSensor, SensorManager.SENSOR_DELAY_FASTEST);
        Action.processAction(mContext, action, false);
    }
}
