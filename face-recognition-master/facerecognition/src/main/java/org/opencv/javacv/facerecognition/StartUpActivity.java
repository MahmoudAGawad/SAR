package org.opencv.javacv.facerecognition;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

public class StartUpActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_up);
        SharedPreferences prefs = getSharedPreferences("first_use_flag", MODE_PRIVATE);
        boolean restoredText = prefs.getBoolean("signedup", false);
        if(restoredText){
            Intent intent = new Intent(getApplicationContext(),org.opencv.javacv.facerecognition.FdActivity.class);
            startActivity(intent);
            finish();
        }else{
            Intent intent = new Intent(getApplicationContext(),org.opencv.javacv.facerecognition.signup_activity.class);
            startActivity(intent);
            finish();
        }
    }
}
