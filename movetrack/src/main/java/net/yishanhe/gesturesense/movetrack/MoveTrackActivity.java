package net.yishanhe.gesturesense.movetrack;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Environment;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;


public class MoveTrackActivity extends ActionBarActivity implements SensorEventListener {

    private Button controlBTN;
    private boolean controlFlag = true;
    private TextView sensorStatus;
    private SensorManager mSensorManager = null;
    Sensor mAccel = null;
    Sensor mGyro = null;
    MoveTrackActivity activity;


    public static String SAVE_DIR = "cs664";
    public static final String EOL = System.getProperty("line.separator");


    final int SENSOR_MAX_LEN = 10000;

    long [] ts_acc = new long[SENSOR_MAX_LEN];
    float[][] acc = new float[SENSOR_MAX_LEN][3];
    int accIndex = 0; //how many acc samples now
    long [] ts_gyro = new long[SENSOR_MAX_LEN];
    float[][] gyro = new float[SENSOR_MAX_LEN][3];
    int gyroIndex = 0; //how many gyro samples now


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_move_track);

        // acceleration = gravity + linear-acceleration
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        mAccel = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
//        mAccel = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        mGyro = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);

        activity = this;
        // initialize text view
        sensorStatus = (TextView) findViewById(R.id.sensor_status);
        sensorStatus.setText("Ready to work."+EOL);

        // @TODO add round counter
        // @TODO add revoke button

        // control button
        controlBTN = (Button) findViewById(R.id.control_btn);
        controlBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (controlFlag == true){
                    PostMessage("Start collecting ...");
                    controlFlag = false;
                    controlBTN.setText("Stop");
                    // Start action here

                    mSensorManager.registerListener(activity, mAccel, SensorManager.SENSOR_DELAY_FASTEST);
                    mSensorManager.registerListener(activity, mGyro, SensorManager.SENSOR_DELAY_FASTEST);

                }else{
                    PostMessage("Start saving ...");
                    controlFlag = true;
                    controlBTN.setText("Start");
                    // Stop action here
                    mSensorManager.unregisterListener(activity);
                    SaveSamples();
                }
            }
        });



    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.move_track, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    public void onSensorChanged(SensorEvent event){
        if ((accIndex>=SENSOR_MAX_LEN)||(gyroIndex>=SENSOR_MAX_LEN)){
            return;
        }

        if ( event.sensor == mAccel ) {
            ts_acc[accIndex] = event.timestamp;
            acc[accIndex][0] = event.values[0];
            acc[accIndex][1] = event.values[1];
            acc[accIndex][2] = event.values[2];
            accIndex++;
        } else if (event.sensor == mGyro ) {
            ts_gyro[gyroIndex] = event.timestamp;
            gyro[gyroIndex][0] = event.values[0];
            gyro[gyroIndex][1] = event.values[1];
            gyro[gyroIndex][2] = event.values[2];
            gyroIndex++;
        }
    }

    public void SaveSamples() {
        BufferedWriter bw = null;
        // Save acc's samples
        if(accIndex > 0){
            DecimalFormat df = new DecimalFormat("0.0000");
            try {
                bw = new BufferedWriter(new FileWriter(
                        createNewFile(SAVE_DIR, "cs644_acc")));
                for(int i=0; i<accIndex; i++){
                    bw.write(String.valueOf(ts_acc[i])+" "+
                            df.format(acc[i][0])+" "+
                            df.format(acc[i][1])+" "+
                            df.format(acc[i][2])+" "+EOL);
                }
                if(bw!=null) bw.close();
                sensorStatus.append(String.valueOf(accIndex)+
                        " accs have been saved!"+EOL);
                accIndex = 0;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // Save gyro's samples
        if(gyroIndex > 0){
            DecimalFormat df = new DecimalFormat("0.0000");
            try {
                bw = new BufferedWriter(new FileWriter(
                        createNewFile(SAVE_DIR, "cs644_gyro")));
                for(int i=0; i<gyroIndex; i++){
                    bw.write(String.valueOf(ts_gyro[i])+" "+
                            df.format(gyro[i][0])+" "+
                            df.format(gyro[i][1])+" "+
                            df.format(gyro[i][2])+" "+EOL);
                }
                if(bw!=null) bw.close();
                sensorStatus.append(String.valueOf(gyroIndex)+
                        " gyros have been saved!"+EOL);
                gyroIndex = 0;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public File createNewFile(String dir, String name){
        int num = 0;

        File folder = new File(Environment.getExternalStorageDirectory().getAbsoluteFile()
                + File.separator + dir);
        folder.mkdir();

        File file = new File(folder.toString(),name + String.valueOf(num++));

        while(file.exists()){
            file = new File(folder.toString(),name + String.valueOf(num++));
        }

//        if (revokeFlag==true && num>=2){
//            num--;
//            file = new File(folder.toString(),name + String.valueOf(--num));
//        }

        return file;
    }


    public void PostMessage(final String msg){
        sensorStatus.append(msg+EOL);
    }
}
