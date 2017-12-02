package rajhack.hasura.utkarshdubey.hasura;

import android.*;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.net.Uri;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.LinkedList;
import java.util.Queue;

public class Car extends AppCompatActivity implements SensorEventListener {

    private TextView xAxisValue;
    private TextView yAxisValue;
    private TextView disp;
    private TextView zAxisValue;
    private TextView latitudeValue;
    private TextView longitudeValue;
    private ToggleButton togle;
    private SensorManager sensorManager;
    private Sensor accelerometerSensor;
    private boolean isSensorAvailable = false;

    private DatabaseReference accelerometerRef;
    private DatabaseReference locationRef;
    private DatabaseReference car_state;

    //private static int points=5000;
    private Queue X;
    private Queue Y;
    private Queue Z;
    private DatabaseReference phoneRef;
    private DatabaseReference carRef;
    private static final int SENSOR_DELAY = 0;
    private int frequency = 8000;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_car);
        xAxisValue = findViewById(R.id.x_axis_value_car);
        yAxisValue = findViewById(R.id.y_axis_value_car);
        zAxisValue = findViewById(R.id.z_axis_value_car);
        latitudeValue = findViewById(R.id.latitude_value_car);
        longitudeValue = findViewById(R.id.longitude_value_car);
        disp = findViewById(R.id.disp_car);
        togle = findViewById(R.id.toggleButton);
        Intent old = getIntent();
        String mode = old.getStringExtra("mode");
        if (mode.equalsIgnoreCase("user"))
            disp.setText("User Application");
        else
            disp.setText("Car Application");

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        if (accelerometerSensor != null) {
            isSensorAvailable = true;

        } else {
            Toast.makeText(this, "Accelerometer not available", Toast.LENGTH_SHORT).show();
        }
        X = new LinkedList();
        Y = new LinkedList();
        Z = new LinkedList();
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        phoneRef = database.getReference().child(mode);
        accelerometerRef = phoneRef.child("accelerometer");
        locationRef = phoneRef.child("location");
//        car_state=phoneRef.child("state");
        togle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {

                    Log.d("state ", "ON");
                    phoneRef.child("state").setValue(null);
                    car_state = phoneRef.child("state");
                    car_state.child("state").setValue("ON");
                } else {
                    Log.d("state ", "OFF");
                    phoneRef.child("state").setValue(null);
                    car_state = phoneRef.child("state");
                    car_state.child("state").setValue("OFF");
                }
            }
        });
    }
    private void openGoogleMaps() {
        final String BASE_URI = "https://www.google.com/maps";
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(BASE_URI));
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (intent.resolveActivity(getPackageManager()) != null) startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d("onResume", "onResume");
        if (isSensorAvailable) {
            sensorManager.registerListener(this, accelerometerSensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }



    long lastTimeSensorValueReceived = System.currentTimeMillis();

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        long currentTimeInMillis = System.currentTimeMillis();
        if (currentTimeInMillis - lastTimeSensorValueReceived < SENSOR_DELAY) {
            return;
        }
        lastTimeSensorValueReceived = currentTimeInMillis;
        xAxisValue.setText(String.valueOf(sensorEvent.values[0]));
        yAxisValue.setText(String.valueOf(sensorEvent.values[1]));
        zAxisValue.setText(String.valueOf(sensorEvent.values[2]));

        if(X.size()>=500 && Y.size()>=500 && Z.size()>=500)
        {
            phoneRef.child("accelerometer").setValue(null);
            upload();
//                while(!X.isEmpty() || !Y.isEmpty() || !Z.isEmpty())
//                {
//                    if(X.size()==100)
//                        phoneRef.child("accelerometer").setValue(null);
//                    newRef.child("x").setValue(X.remove());
//                    newRef.child("y").setValue(Y.remove());
//                    newRef.child("z").setValue(Z.remove());
//                }
        }
        else
        {
            Log.d("size of key"," "+X.size());
            X.add(sensorEvent.values[0]);
            Y.add(sensorEvent.values[1]);
            Z.add(sensorEvent.values[2]);
        }
    }

    public  void upload()
    {
        //Log.d("key",newrf.toString());
        while(X.size()>0 && Y.size()>0 && Z.size()>0)
        {
            DatabaseReference newrf = accelerometerRef.child(accelerometerRef.push().getKey());
            Log.d("path",newrf.toString());
            newrf.child("x").setValue(X.remove());
            newrf.child("y").setValue(Y.remove());
            newrf.child("z").setValue(Z.remove());
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }



    private boolean arePermissionsGranted() {
        return ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }


    @Override
    public void onBackPressed()
    {
        super.onBackPressed();

    }
}

