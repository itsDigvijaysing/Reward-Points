package com.rewardpoints.app;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    int counter;

    TextView RewardCount;
    //TextView txttotalhistory;

    Dialog myDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Calendar calendar = Calendar.getInstance();
        int currentDay = calendar.get(Calendar.DAY_OF_MONTH);

        String currentfullDate = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date());

        //Toast.makeText(MainActivity.this," Today "+currentfullDate,Toast.LENGTH_SHORT).show();


        Button btntodayP;
        Button btnmovie;
        Button btngame;
        Button btntravel;
        Button btnproduct;
        TextView congratsText;

        RewardCount = findViewById(R.id.RewardCounter);
        btnmovie = findViewById(R.id.btnmovielinear);
        btngame = findViewById(R.id.btngamelinear);
        btntravel = findViewById(R.id.btntravellinear);
        btnproduct = findViewById(R.id.btnproductlinear);
        congratsText = findViewById(R.id.txtCongrats);
        btntodayP = findViewById(R.id.btndayinfo);

        myDialog = new Dialog(this);

        //Starting Counter zero at every run
        //counter=0;

        // Using SharedPreferences to Store Counter value in android so even if app closed it will store its value
        SharedPreferences getShared = getSharedPreferences("demo", MODE_PRIVATE);
        SharedPreferences.Editor editor = getShared.edit();

        int lastDay = getShared.getInt("day",0);
        boolean tfeedbacksp = getShared.getBoolean("todayf",false);

        if(lastDay != currentDay){
            btntodayP.setVisibility(View.VISIBLE);
            btntodayP.setClickable(true);

            if(tfeedbacksp==true) {
                editor.putInt("day", currentDay);
                editor.apply();

                //Toast.makeText(MainActivity.this,"Today day set confirm",Toast.LENGTH_SHORT).show();

                // It Refresh the App
                //this.recreate();
                finish();
                overridePendingTransition(0, 0);
                startActivity(getIntent());
                overridePendingTransition(0, 0);

            }
            if(tfeedbacksp==false){
                Toast.makeText(MainActivity.this,"Give Feedback of Today",Toast.LENGTH_SHORT).show();
            }
        }else{
            btntodayP.setVisibility(View.GONE);
            btntodayP.setClickable(false);

            editor.putBoolean("todayf",false);
            editor.apply();

            Toast.makeText(MainActivity.this,"Feedback is received for Today",Toast.LENGTH_LONG).show();
        }

        // Textview is not in activity main so facing error to access it.
        //txttotalhistory = findViewById(R.id.txtforhistory);


        btnmovie.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(counter >= 200) {
                    counter = counter - 200;
                    editor.putInt("count",counter);
                    editor.apply();

                    RewardCount.setText(""+counter);

                    String historyfromsp = getShared.getString("fulldate","No Previous History");
                    editor.putString("fulldate",currentfullDate+" : 1 Full Movie Reward Claimed -200P"+"\n"+historyfromsp);
                    editor.apply();

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

                    String historyfromsp = getShared.getString("fulldate","No Previous History");
                    editor.putString("fulldate",currentfullDate+" : 2Hr Game Reward Claimed -300P"+"\n"+historyfromsp);
                    editor.apply();

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

                    String historyfromsp = getShared.getString("fulldate","No Previous History");
                    editor.putString("fulldate",currentfullDate+" : 1Hr Travel Reward Claimed -200P"+"\n"+historyfromsp);
                    editor.apply();

                    Toast.makeText(MainActivity.this,"1Hr Travel Reward Claimed...",Toast.LENGTH_SHORT).show();
                    congratsText.setVisibility(View.VISIBLE);
                }
                else {
                    Toast.makeText(MainActivity.this,"You Do Not have Sufficient Points",Toast.LENGTH_SHORT).show();
                }
            }
        });

        btnproduct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(counter >= 500) {
                    counter = counter - 500;
                    editor.putInt("count",counter);
                    editor.apply();

                    RewardCount.setText(""+counter);

                    String historyfromsp = getShared.getString("fulldate","No Previous History");
                    editor.putString("fulldate",currentfullDate+" : Product Purchase (500rs) -500P"+"\n"+historyfromsp);
                    editor.apply();

                    Toast.makeText(MainActivity.this,"Product Purchase (500rs) Claimed...",Toast.LENGTH_SHORT).show();
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



        ImageButton tosharthistory = findViewById(R.id.imgbuttonhistory);
        tosharthistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this,pointshistory.class);
                startActivity(intent);
            }
        });

    }

    public void ShowTodayPopup(View v) {

        SharedPreferences getShared = getSharedPreferences("demo", MODE_PRIVATE);
        SharedPreferences.Editor editor = getShared.edit();

        ImageButton btnclose;
        ImageButton btnvhappy;
        ImageButton btnhappy;
        ImageButton btnneutral;
        ImageButton btnsad;
        ImageButton btnvsad;

        //txttotalhistory = findViewById(R.id.txtviewofhistory);

        String currentfullDate = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date());

        myDialog.setContentView(R.layout.todaypopup);
        btnclose = (ImageButton) myDialog.findViewById(R.id.btnclose);
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
                editor.putBoolean("todayf",true);
                editor.apply();

                String historyfromsp = getShared.getString("fulldate","No Previous History");
                editor.putString("fulldate",currentfullDate+" : Very Happy Day +120P"+"\n"+historyfromsp);
                editor.apply();

                Toast.makeText(MainActivity.this,"Congratulations 120 Points Added...",Toast.LENGTH_LONG).show();
                myDialog.dismiss();
                finish();
                overridePendingTransition(0, 0);
                startActivity(getIntent());
                overridePendingTransition(0, 0);
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
                editor.putBoolean("todayf",true);
                editor.apply();

                String historyfromsp = getShared.getString("fulldate","No Previous History");
                editor.putString("fulldate",currentfullDate+" : Happy Day +60P"+"\n"+historyfromsp);
                editor.apply();

                Toast.makeText(MainActivity.this,"Congrats 60 Points Added",Toast.LENGTH_LONG).show();
                myDialog.dismiss();
                finish();
                overridePendingTransition(0, 0);
                startActivity(getIntent());
                overridePendingTransition(0, 0);
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
                editor.putBoolean("todayf",true);
                editor.apply();

                String historyfromsp = getShared.getString("fulldate","No Previous History");
                editor.putString("fulldate",currentfullDate+" : Okay Day +20P"+"\n"+historyfromsp);
                editor.apply();

                Toast.makeText(MainActivity.this,"Nice 20 Points Added",Toast.LENGTH_SHORT).show();
                myDialog.dismiss();
                finish();
                overridePendingTransition(0, 0);
                startActivity(getIntent());
                overridePendingTransition(0, 0);
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
                editor.putBoolean("todayf",true);
                editor.apply();

                String historyfromsp = getShared.getString("fulldate","No Previous History");
                editor.putString("fulldate",currentfullDate+" : Sad Day +10P"+"\n"+historyfromsp);
                editor.apply();

                Toast.makeText(MainActivity.this,"It's Okay Yaar.. 10 Points Added",Toast.LENGTH_SHORT).show();
                myDialog.dismiss();
                finish();
                overridePendingTransition(0, 0);
                startActivity(getIntent());
                overridePendingTransition(0, 0);
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
                editor.putBoolean("todayf",true);
                editor.apply();

                String historyfromsp = getShared.getString("fulldate","No Previous History");
                editor.putString("fulldate",currentfullDate+" : Very Sad Day +0"+"\n"+historyfromsp);
                editor.apply();

                Toast.makeText(MainActivity.this,"Stay +ve Better days ahead...",Toast.LENGTH_LONG).show();
                myDialog.dismiss();
                finish();
                overridePendingTransition(0, 0);
                startActivity(getIntent());
                overridePendingTransition(0, 0);

            }
        });

        //myDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        myDialog.show();
    }

    public void Showachievementpopup(View v) {

        SharedPreferences getShared = getSharedPreferences("demo", MODE_PRIVATE);
        SharedPreferences.Editor editor = getShared.edit();

        String currentfullDate = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date());

        ImageButton btncloseachievement;
        ImageButton btnvimp;
        ImageButton btnimp;
        ImageButton btnhelpful;

        myDialog.setContentView(R.layout.achievementpopup);
        btncloseachievement = (ImageButton) myDialog.findViewById(R.id.btncloseachievement);
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

                String historyfromsp = getShared.getString("fulldate","No Previous History");
                editor.putString("fulldate",currentfullDate+" : Very IMP Achievement +200"+"\n"+historyfromsp);
                editor.apply();

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

                String historyfromsp = getShared.getString("fulldate","No Previous History");
                editor.putString("fulldate",currentfullDate+" : Important Achievement +120P"+"\n"+historyfromsp);
                editor.apply();

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

                String historyfromsp = getShared.getString("fulldate","No Previous History");
                editor.putString("fulldate",currentfullDate+" : Good Achievement +60P"+"\n"+historyfromsp);
                editor.apply();

                Toast.makeText(MainActivity.this,"Nice... Keep it up +60 Points",Toast.LENGTH_LONG).show();
                myDialog.dismiss();
            }
        });

        //myDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        myDialog.show();
    }

    public void ShowmissionPopup(View v) {

        SharedPreferences getShared = getSharedPreferences("demo", MODE_PRIVATE);
        SharedPreferences.Editor editor = getShared.edit();

        ImageButton btnclose;
        ImageButton btnhealth;
        ImageButton btncwork;
        ImageButton btnselfdevelopment;

        //txttotalhistory = findViewById(R.id.txtviewofhistory);

        String currentfullDate = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date());

        myDialog.setContentView(R.layout.missionpopup);
        btnclose = (ImageButton) myDialog.findViewById(R.id.btnclosemission);
        btnclose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                myDialog.dismiss();
            }
        });

        btnhealth = (ImageButton) myDialog.findViewById(R.id.imghealthmission);
        btnhealth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                counter = counter + 25;

                editor.putInt("count",counter);
                editor.apply();

                RewardCount.setText(""+counter);
                //editor.putBoolean("todayf",true);
                //editor.apply();

                String historyfromsp = getShared.getString("fulldate","No Previous History");
                editor.putString("fulldate",currentfullDate+" : Health Mission Complete +25P"+"\n"+historyfromsp);
                editor.apply();

                Toast.makeText(MainActivity.this,"Congratulations 25 Points Added...",Toast.LENGTH_LONG).show();
                myDialog.dismiss();
//                finish();
//                overridePendingTransition(0, 0);
//                startActivity(getIntent());
//                overridePendingTransition(0, 0);
            }
        });

        btncwork = (ImageButton) myDialog.findViewById(R.id.imgcworkmission);
        btncwork.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                counter = counter + 25;

                editor.putInt("count",counter);
                editor.apply();

                RewardCount.setText(""+counter);
                //editor.putBoolean("todayf",true);
                //editor.apply();

                String historyfromsp = getShared.getString("fulldate","No Previous History");
                editor.putString("fulldate",currentfullDate+" : Completed Computer Dev Mission +25P"+"\n"+historyfromsp);
                editor.apply();

                Toast.makeText(MainActivity.this,"Congrats 25 Points Added",Toast.LENGTH_LONG).show();
                myDialog.dismiss();
//                finish();
//                overridePendingTransition(0, 0);
//                startActivity(getIntent());
//                overridePendingTransition(0, 0);
            }
        });

        btnselfdevelopment = (ImageButton) myDialog.findViewById(R.id.imgsdmission);
        btnselfdevelopment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                counter = counter + 25;

                editor.putInt("count",counter);
                editor.apply();

                RewardCount.setText(""+counter);
                //editor.putBoolean("todayf",true);
                //editor.apply();

                String historyfromsp = getShared.getString("fulldate","No Previous History");
                editor.putString("fulldate",currentfullDate+" : Completed Self Development Mission +25P"+"\n"+historyfromsp);
                editor.apply();

                Toast.makeText(MainActivity.this,"Nice 25 Points Added",Toast.LENGTH_SHORT).show();
                myDialog.dismiss();
//                finish();
//                overridePendingTransition(0, 0);
//                startActivity(getIntent());
//                overridePendingTransition(0, 0);
            }

        });

        //myDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        myDialog.show();
    }

    /*
    public void showPointHistory(View v) {
        // To go to next activity add in listner for best case
        Intent intent = new Intent(MainActivity.this,pointshistory.class);
        startActivity(intent);
    }*/
}