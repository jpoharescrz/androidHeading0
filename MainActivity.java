package com.example.heading0;

import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.os.Handler;
import android.widget.TextView;
import android.widget.Button;
import android.util.Log;
import java.util.List;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;


public class MainActivity extends AppCompatActivity implements SensorEventListener {

    TextView counter;
    Button btnStartStop;
    Integer count;
    //float heading;
    TextView tvHeading, tvDeviceList;
    //boolean hdgUpdated = false;
    long time;

    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private Sensor mMagnetometer;

    private float[] mLastAccelerometer = new float[3];
    private float[] mLastMagnetometer = new float[3];
    private boolean mLastAccelerometerSet = false;
    private boolean mLastMagnetometerSet = false;
    private float[] mR = new float[9];
    private float[] mOrientation = new float[3];
    private float mCurrentDegree = 0f;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        counter = findViewById(R.id.tviewCounter) ;
        tvHeading = findViewById(R.id.tviewHeading) ;
        btnStartStop = findViewById(R.id.btnStartStop);
        tvDeviceList = findViewById(R.id.tvDevices);
        count = 0;

        mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        //@Nullable
        List<Sensor> mList= mSensorManager.getSensorList(Sensor.TYPE_ALL);
        for (int i = 1; i < mList.size(); i++) {
            tvDeviceList.setVisibility(View.VISIBLE);
            //tvDeviceList.append("\n" + mList.get(i).getName() + "\n" + mList.get(i).getVendor() + "\n" + mList.get(i).getVersion());
            tvDeviceList.append("\n" + mList.get(i).getName());
        }
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mMagnetometer = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // for the system's orientation sensor registered listeners
        mSensorManager.registerListener(this, mAccelerometer, 200000, SensorManager.SENSOR_DELAY_GAME);
        mSensorManager.registerListener(this, mMagnetometer, 200000,
                SensorManager.SENSOR_DELAY_GAME);
    }
    @Override
    protected void onPause() {
        super.onPause();

        // to stop the listener and save battery
        mSensorManager.unregisterListener(this, mMagnetometer);
        mSensorManager.unregisterListener(this, mAccelerometer);
    }

    // Create the Handler object (on the main thread by default)
    Handler handler = new Handler();
    // Define the code block to be executed
    private Runnable runnableCode = new Runnable() {
        @Override
        public void run() {

            long new_time, delay_ms;

            // increment the counter
            count += 1;
            // Do something here on the main thread
            //Log.d("Handlers", "Called on main thread:".concat(String.valueOf(count)));
            counter.setText(String.valueOf(count));
            tvHeading.setText(String.valueOf(mCurrentDegree));
            // Repeat this the same runnable code block again another 1 seconds
            // 'this' is referencing the Runnable object
            new_time = System.currentTimeMillis(); // get the current milliseconds
            delay_ms = (time + 1000) - new_time;
            //Log.d("Handlers", "Delay of:".concat(String.valueOf(delay_ms)));
            //Log.d("Handlers", "Heading update:".concat(String.format("%.0f",hdgUpdated)));
            if (delay_ms < 0) {
                delay_ms = 0;
            }
            time += 1000;
            handler.postDelayed(this, delay_ms);
        }
    };

    // SensorEventListener override
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // not in use
    }

    // SensorEventListener override
    public void onSensorChanged(SensorEvent event) {
         if (event.sensor == mMagnetometer) {

             System.arraycopy(event.values, 0, mLastMagnetometer, 0, event.values.length);
             mLastMagnetometerSet = true;
             //Log.d("Mag Sensor Event", "Heading Sensor event update:".concat(String.valueOf(event.values[0])));
        }
         else if (event.sensor == mAccelerometer) {
             System.arraycopy(event.values, 0, mLastAccelerometer, 0, event.values.length);
             mLastAccelerometerSet = true;
             //Log.d("Acc Sensor Event", "Accelerator Sensor event update:".concat(String.valueOf(event.values[0])));
         }
        if (mLastAccelerometerSet && mLastMagnetometerSet) {
            SensorManager.getRotationMatrix(mR, null, mLastAccelerometer, mLastMagnetometer);
            SensorManager.getOrientation(mR, mOrientation);
            float azimuthInRadians = mOrientation[0];
            //Log.d("Sensor","Radians:".concat(String.valueOf(azimuthInRadians)));
            mCurrentDegree = (float)(Math.toDegrees(azimuthInRadians)+360)%360;
            /*RotateAnimation ra = new RotateAnimation(
                    mCurrentDegree,
                    -azimuthInDegress,
                    Animation.RELATIVE_TO_SELF, 0.5f,
                    Animation.RELATIVE_TO_SELF,
                    0.5f);

            ra.setDuration(250);

            ra.setFillAfter(true);

            mPointer.startAnimation(ra);*/
            //mCurrentDegree = azimuthInDegrees;
            mLastAccelerometerSet = false;
            mLastMagnetometerSet = false;
        }
    }

    // Button onClick code for Start/Stop button
    public void startStopCounter(View view){

        if (btnStartStop.getText().equals("Start")){
            // Start the initial runnable task by posting through the handler
            count = 0;
            time= System.currentTimeMillis(); // get the current milliseconds
            handler.post(runnableCode);
            btnStartStop.setText(R.string.stop);
        }
        else { // Stopping counter
            // Removes pending code execution
            handler.removeCallbacks(runnableCode);
            btnStartStop.setText(R.string.start);
        }

    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
