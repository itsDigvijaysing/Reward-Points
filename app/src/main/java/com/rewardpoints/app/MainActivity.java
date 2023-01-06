package com.rewardpoints.app;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    int counter;

    TextView RewardCount;
    Button btntodayreward;

    Dialog myDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        RewardCount = findViewById(R.id.RewardCounter);
        btntodayreward = findViewById(R.id.btndayinfo);

        myDialog = new Dialog(this);

        //Starting Counter zero at every run
        //counter=0;

        btntodayreward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                counter++;

                // Using SharedPreferences to Store Counter value in android so even if app closed it will store its value
                SharedPreferences shrd = getSharedPreferences("demo", MODE_PRIVATE);
                SharedPreferences.Editor editor = shrd.edit();
                editor.putInt("count",counter);
                editor.apply();

                RewardCount.setText(""+counter);
            }
        });

        // Code to Get the value from SharedPreferences count key
        SharedPreferences getShared = getSharedPreferences("demo", MODE_PRIVATE);
        counter = getShared.getInt("count",0);    // 0 is Default value if app not found and value then set it 0
        RewardCount.setText(""+counter);

    }
    public void ShowTodayPopup(View v) {
        Button btnclose;
        myDialog.setContentView(R.layout.todaypopup);
        btnclose = (Button) myDialog.findViewById(R.id.btnclose);
        btnclose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                myDialog.dismiss();
            }
        });
        //myDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        myDialog.show();
    }
}