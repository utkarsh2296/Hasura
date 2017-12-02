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
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.LinkedList;
import java.util.Queue;

import static android.location.Location.distanceBetween;

public class User extends AppCompatActivity implements SensorEventListener {
    private TextView xAxisValue;
    private android.widget.TextView yAxisValue;
    private TextView disp;
    private TextView zAxisValue;
    private TextView latitudeValue;
    private TextView longitudeValue;
    private TextView safelabel;
    private TextView dist;
    private TextView unsafe;
    private TextView car_cond;

    private Long status;
    private String state_of_car;
    private SensorManager sensorManager;
    private Sensor accelerometerSensor;
    private boolean isSensorAvailable = false;

    private DatabaseReference accelerometerRef;
    private DatabaseReference car_locRef;

    private Queue X;
    private Queue Y;
    private Queue Z;
    private DatabaseReference phoneRef;
    private DatabaseReference carRef;
    private static final int SENSOR_DELAY = 0;
    private  int frequency = 8000;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);
        xAxisValue = findViewById(R.id.x_axis_value);
        yAxisValue = findViewById(R.id.y_axis_value);
        zAxisValue = findViewById(R.id.z_axis_value);
        latitudeValue = findViewById(R.id.latitude_value);
        longitudeValue = findViewById(R.id.longitude_value);
        disp=findViewById(R.id.disp);
        dist=findViewById(R.id.distance);
        safelabel=findViewById(R.id.stats);
        unsafe=findViewById(R.id.unsafe);
        car_cond=findViewById(R.id.condition);
        Intent old=getIntent();
        String mode=old.getStringExtra("mode");
        if(mode.equalsIgnoreCase("user"))
            disp.setText("User Application");
        else
            disp.setText("Car Application");

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        if (accelerometerSensor!=null) {
            isSensorAvailable = true;

        } else {
            Toast.makeText(this, "Accelerometer not available", Toast.LENGTH_SHORT).show();
        }
        X=new LinkedList();
        Y=new LinkedList();
        Z=new LinkedList();
        final MediaPlayer alertSound=MediaPlayer.create(getApplicationContext(),R.raw.high_alert);
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        phoneRef = database.getReference().child(mode);
        accelerometerRef = phoneRef.child("accelerometer");
        Log.d("key",accelerometerRef.toString());
        DatabaseReference stat=phoneRef.child("status");
        DatabaseReference car_state=database.getReference().child("car").child("state");
        Log.d("status path",stat.toString());
        stat.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                status=dataSnapshot.getValue(Long.class);
                Log.d("status",""+status);
                if(status!=3 && status!=5)
                {
                    unsafe.setText(null);
                    car_cond.setText("Everything is FINE");
                    car_cond.setTextColor(getResources().getColor(R.color.safe));
                    safelabel.setText("Your Vehicle is SAFE");
                }
                else if(status ==3 || status==5)
                {
                    safelabel.setText(null);
                    Car_unsafe(status);
                }

            }
            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                status=dataSnapshot.getValue(Long.class);
                Log.d("status",""+status);
                if(status!=3 && status!=5)
                {
                    dist.setText(null);
                    unsafe.setText(null);
                    car_cond.setText("Everything is FINE");
                    car_cond.setTextColor(getResources().getColor(R.color.safe));
                    safelabel.setText("Your Vehicle is SAFE :"+status);
                }
                else if(status ==3 || status==5)
                {
                    safelabel.setText(null);
                    Car_unsafe(status);

                }

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        car_state.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                state_of_car=dataSnapshot.getValue(String.class);
                Log.d("state",state_of_car);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                state_of_car=dataSnapshot.getValue(String.class);
                Log.d("state",state_of_car);
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private void openGoogleMaps() {
        final String BASE_URI = "https://www.google.com/maps";
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(BASE_URI));
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (intent.resolveActivity(getPackageManager()) != null) startActivity(intent);
    }
    private void Car_unsafe(Long s) {

        FirebaseDatabase databse = FirebaseDatabase.getInstance();
        unsafe.setText("Your Vehicle is NOT SAFE :" + s);
        carRef = databse.getReference().child("car");
        car_locRef = carRef.child("location");
        Log.d("car path", car_locRef.toString());
        if (status == 3 || status == 5) {
            if (state_of_car.equals("ON")) {
                car_cond.setText("CAR IS BEING STOLEN");
                car_cond.setTextColor(getResources().getColor(R.color.unsafe));
            } else if (state_of_car.equals("OFF")) {
                car_cond.setText(("CAR IS BEING TOWED"));
                car_cond.setTextColor(getResources().getColor(R.color.unsafe));
            }
            final MediaPlayer alertSound = MediaPlayer.create(getApplicationContext(), R.raw.high_alert);
            alertSound.start();
            alertSound.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mediaPlayer) {
                    alertSound.release();
                }
            });
        }

    }


    @Override
    protected void onResume() {
        super.onResume();
        Log.d("onResume", "onResume");
        if (isSensorAvailable) {
            sensorManager.registerListener(this, accelerometerSensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }


    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
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





    @Override
    public void onBackPressed()
    {
        super.onBackPressed();

    }
}

class Loc
{
    double latitude;
    double longitude;
    void Loc()
    {
        latitude=0;
        longitude=0;
    }
    void Loc(double latitude,double longitude)
    {
        this.latitude=latitude;
        this.longitude=longitude;

    }
}
