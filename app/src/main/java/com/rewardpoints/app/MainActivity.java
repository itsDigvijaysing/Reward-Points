package com.rewardpoints.app;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    int counter;

    TextView RewardCount;

    Dialog myDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btnmovie;
        Button btngame;
        Button btntravel;
        TextView congratsText;

        RewardCount = findViewById(R.id.RewardCounter);
        btnmovie = findViewById(R.id.btnmovielinear);
        btngame = findViewById(R.id.btngamelinear);
        btntravel = findViewById(R.id.btntravellinear);
        congratsText = findViewById(R.id.txtCongrats);

        myDialog = new Dialog(this);

        //Starting Counter zero at every run
        //counter=0;

        // Using SharedPreferences to Store Counter value in android so even if app closed it will store its value
        SharedPreferences getShared = getSharedPreferences("demo", MODE_PRIVATE);
        SharedPreferences.Editor editor = getShared.edit();

        btnmovie.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(counter >= 200) {
                    counter = counter - 200;
                    editor.putInt("count",counter);
                    editor.apply();

                    RewardCount.setText(""+counter);
                    Toast.makeText(MainActivity.this,"1 Full Movie Reward Claimed...",Toast.LENGTH_SHORT).show();
                    congratsText.setVisibility(View.VISIBLE);
                }
                else {
                    Toast.makeText(MainActivity.this,"You Do Not have Sufficient Points",Toast.LENGTH_SHORT).show();
                }
            }
        });

        btngame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(counter >= 300) {
                    counter = counter - 300;
                    editor.putInt("count",counter);
                    editor.apply();

                    RewardCount.setText(""+counter);
                    Toast.makeText(MainActivity.this,"2Hr Game Reward Claimed...",Toast.LENGTH_SHORT).show();
                    congratsText.setVisibility(View.VISIBLE);
                }
                else {
                    Toast.makeText(MainActivity.this,"You Do Not have Sufficient Points",Toast.LENGTH_SHORT).show();
                }
            }
        });

        btntravel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(counter >= 200) {
                    counter = counter - 200;
                    editor.putInt("count",counter);
                    editor.apply();

                    RewardCount.setText(""+counter);
                    Toast.makeText(MainActivity.this,"1Hr Travel Reward Claimed...",Toast.LENGTH_SHORT).show();
                    congratsText.setVisibility(View.VISIBLE);
                }
                else {
                    Toast.makeText(MainActivity.this,"You Do Not have Sufficient Points",Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Code to Get the value from SharedPreferences count key
        counter = getShared.getInt("count",0);    // 0 is Default value if app not found and value then set it 0
        RewardCount.setText(""+counter);

    }

    public void ShowTodayPopup(View v) {

        SharedPreferences getShared = getSharedPreferences("demo", MODE_PRIVATE);
        SharedPreferences.Editor editor = getShared.edit();

        Button btnclose;
        ImageButton btnvhappy;
        ImageButton btnhappy;
        ImageButton btnneutral;
        ImageButton btnsad;
        ImageButton btnvsad;

        myDialog.setContentView(R.layout.todaypopup);
        btnclose = (Button) myDialog.findViewById(R.id.btnclose);
        btnclose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                myDialog.dismiss();
            }
        });

        btnvhappy = (ImageButton) myDialog.findViewById(R.id.imggreat);
        btnvhappy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                counter = counter + 120;

                editor.putInt("count",counter);
                editor.apply();

                RewardCount.setText(""+counter);
                Toast.makeText(MainActivity.this,"Congratulations 120 Points Added...",Toast.LENGTH_LONG).show();
                myDialog.dismiss();
            }
        });

        btnhappy = (ImageButton) myDialog.findViewById(R.id.imggood);
        btnhappy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                counter = counter + 60;

                editor.putInt("count",counter);
                editor.apply();

                RewardCount.setText(""+counter);
                Toast.makeText(MainActivity.this,"Congrats 60 Points Added",Toast.LENGTH_LONG).show();
                myDialog.dismiss();
            }
        });

        btnneutral = (ImageButton) myDialog.findViewById(R.id.imgneutral);
        btnneutral.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                counter = counter + 20;

                editor.putInt("count",counter);
                editor.apply();

                RewardCount.setText(""+counter);
                Toast.makeText(MainActivity.this,"Nice 20 Points Added",Toast.LENGTH_SHORT).show();
                myDialog.dismiss();
            }
        });

        btnsad = (ImageButton) myDialog.findViewById(R.id.imgsad);
        btnsad.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                counter = counter + 10;

                editor.putInt("count",counter);
                editor.apply();

                RewardCount.setText(""+counter);
                Toast.makeText(MainActivity.this,"It's Okay Yaar.. 10 Points Added",Toast.LENGTH_SHORT).show();
                myDialog.dismiss();
            }
        });

        btnvsad = (ImageButton) myDialog.findViewById(R.id.imgverysad);
        btnvsad.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                counter = counter + 0;

                editor.putInt("count",counter);
                editor.apply();

                RewardCount.setText(""+counter);
                Toast.makeText(MainActivity.this,"Stay +ve Better days ahead...",Toast.LENGTH_LONG).show();
                myDialog.dismiss();
            }
        });

        //myDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        myDialog.show();
    }

    public void Showachievementpopup(View v) {

        SharedPreferences getShared = getSharedPreferences("demo", MODE_PRIVATE);
        SharedPreferences.Editor editor = getShared.edit();

        Button btncloseachievement;
        ImageButton btnvimp;
        ImageButton btnimp;
        ImageButton btnhelpful;

        myDialog.setContentView(R.layout.achievementpopup);
        btncloseachievement = (Button) myDialog.findViewById(R.id.btncloseachievement);
        btncloseachievement.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                myDialog.dismiss();
            }
        });

        btnvimp = (ImageButton) myDialog.findViewById(R.id.imgvimpachievement);
        btnvimp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                counter = counter + 200;

                editor.putInt("count",counter);
                editor.apply();

                RewardCount.setText(""+counter);
                Toast.makeText(MainActivity.this,"Congrats Yaar.. Kadak.... +200 Points",Toast.LENGTH_LONG).show();
                myDialog.dismiss();
            }
        });

        btnimp = (ImageButton) myDialog.findViewById(R.id.imgimpachievement);
        btnimp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                counter = counter + 120;

                editor.putInt("count",counter);
                editor.apply();

                RewardCount.setText(""+counter);
                Toast.makeText(MainActivity.this,"You are doing great +120 Points",Toast.LENGTH_LONG).show();
                myDialog.dismiss();
            }
        });

        btnhelpful = (ImageButton) myDialog.findViewById(R.id.imgniceachievement);
        btnhelpful.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                counter = counter + 60;

                editor.putInt("count",counter);
                editor.apply();

                RewardCount.setText(""+counter);
                Toast.makeText(MainActivity.this,"Nice... Keep it up +60 Points",Toast.LENGTH_LONG).show();
                myDialog.dismiss();
            }
        });

        //myDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        myDialog.show();
    }
}