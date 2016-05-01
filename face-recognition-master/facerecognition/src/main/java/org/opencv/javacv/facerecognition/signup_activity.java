package org.opencv.javacv.facerecognition;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import utilities.DatabaseHelper;

public class signup_activity extends Activity {

     EditText username;
     EditText email;
     EditText pass;
    static int counter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        counter=0;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup_activity);
        Button signUp = (Button)findViewById(R.id.signup);
        Button train = (Button) findViewById(R.id.training);
        username=(EditText)findViewById(R.id.usrname);
        email= (EditText)findViewById(R.id.email);
        pass= (EditText)findViewById(R.id.password);



        signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = getSharedPreferences("first_use_flag", MODE_PRIVATE).edit();
                editor.putBoolean("signedup",true);
                editor.commit();
                Intent intent = new Intent(getApplicationContext(),org.opencv.javacv.facerecognition.FdActivity.class);

                DatabaseHelper db=new DatabaseHelper(getApplicationContext());

                try {
                    db.open();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                db.createEntry(username.getText().toString(), email.getText().toString(), pass.getText().toString());
                // insert to database

              //  db.printAll();


                if(counter==1)
                startActivity(intent);


                counter++;

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
