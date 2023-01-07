package com.rewardpoints.app;

import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class pointshistory extends AppCompatActivity {

    TextView txtthistory;
    ImageButton toclearhistory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pointshistory);

        txtthistory = findViewById(R.id.txtviewofhistory);

        String currentfullDate = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date());

        // Using SharedPreferences to Store Counter value in android so even if app closed it will store its value
        SharedPreferences getShared = getSharedPreferences("demo", MODE_PRIVATE);
        SharedPreferences.Editor editor = getShared.edit();
        String historyfromsp = getShared.getString("fulldate","No History");

        txtthistory.setText(historyfromsp);

        toclearhistory = findViewById(R.id.imgbtnclearhistory);
        toclearhistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editor.putString("fulldate","");
                editor.apply();

                Toast.makeText(pointshistory.this,"History will be Cleared on your Next Visit",Toast.LENGTH_SHORT).show();
            }
        });

    }
}