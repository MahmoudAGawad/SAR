package org.opencv.javacv.facerecognition;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class signup_activity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup_activity);



        Button signUp = (Button)findViewById(R.id.signup);
        Button train = (Button) findViewById(R.id.training);
        final EditText username = (EditText)findViewById(R.id.usrname);
        signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = getSharedPreferences("first_use_flag", MODE_PRIVATE).edit();
                editor.putBoolean("signedup",true);
                editor.commit();
                Intent intent = new Intent(getApplicationContext(),org.opencv.javacv.facerecognition.FdActivity.class);
                startActivity(intent);
            }
        });


        train.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(username.getText().length()!=0){
                    Intent intent = new Intent(getApplicationContext(),org.opencv.javacv.facerecognition.TrainingActivity.class);
                    intent.putExtra("usrname",username.getText().toString());
                    startActivity(intent);
                }
            }
        });













    }
}
