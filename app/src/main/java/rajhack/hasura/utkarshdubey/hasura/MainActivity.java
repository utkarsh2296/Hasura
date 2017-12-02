package rajhack.hasura.utkarshdubey.hasura;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private TextView user;
    private TextView car;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final SharedPreferences sp= getSharedPreferences("mode", Context.MODE_PRIVATE);
        final SharedPreferences sp1= getSharedPreferences("login", Context.MODE_PRIVATE);
        final SharedPreferences.Editor edit =sp.edit();
        String mode=sp.getString("mode","");
        String logdin=sp.getString("login","");
        Toast.makeText(this,mode,Toast.LENGTH_LONG).show();
        if(mode.equalsIgnoreCase("user"))
        {
            Intent i=new Intent(this,User.class);
            i.putExtra("mode","user");
            this.startActivity(i);
        }
        else if (mode.equalsIgnoreCase("car"))
        {
            Intent i=new Intent(this,Car.class);
            i.putExtra("mode","car");
            this.startActivity(i);
        }
        else
        {
            user=(TextView) findViewById(R.id.type_user);
            car=(TextView) findViewById(R.id.type_car);
            user.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    edit.putString("mode","user");
                    edit.apply();
                    Intent i=new Intent(MainActivity.this,User.class);
                    i.putExtra("mode","user");
                    startActivity(i);
                }
            });
            car.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view)
                {
                    edit.putString("mode","car");
                    edit.apply();
                    Intent i=new Intent(MainActivity.this,Car.class);
                    i.putExtra("mode","car");
                    startActivity(i);
                }
            });

        }

    }
    }

