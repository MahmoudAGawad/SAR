package org.opencv.javacv.facerecognition;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.io.File;
import java.io.IOException;

import utilities.DatabaseHelper;

public class signup_activity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup_activity);

        Button deleteDb = (Button)findViewById(R.id.deleteDb);
        Button signUp = (Button)findViewById(R.id.signup);
        Button train = (Button) findViewById(R.id.training);
        final EditText username = (EditText)findViewById(R.id.usrname);
        final EditText email= (EditText)findViewById(R.id.email);
        final EditText pass= (EditText)findViewById(R.id.password);
        final EditText passConfirm= (EditText)findViewById(R.id.cpassword);


        signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = getSharedPreferences("first_use_flag", MODE_PRIVATE).edit();
                editor.putBoolean("signedup",true);
                editor.commit();


                File SARDirectory = new File(Environment.getExternalStorageDirectory() +File.separator+"SAR");
                // have the object build the directory structure, if needed.

                boolean success = false;

                if (!SARDirectory.exists()) {
                    success = SARDirectory.mkdir();
                }


                if (success) {
                    // Do something on success
                    File outputFile = new File(SARDirectory, "tasks.txt");
                    try {
                        outputFile.createNewFile();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }

                File imageDirectory = new File(SARDirectory, "Images");
                if(!imageDirectory.exists()){
                    imageDirectory.mkdirs();
                }

                addUserToDatabase(username,email,pass);

                Intent intent = new Intent(getApplicationContext(),org.opencv.javacv.facerecognition.StartUpActivity.class);
                startActivity(intent);
                finish();
            }
        });

        train.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(username.getText().length()!=0){
                    Intent intent = new Intent(getApplicationContext(), org.opencv.javacv.facerecognition.TrainingActivity.class);
                    intent.putExtra("usrname",username.getText().toString());
                    startActivity(intent);
                }
            }
        });

    deleteDb.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            DatabaseHelper db=new DatabaseHelper(getApplicationContext());

            try {
                db.open();
            } catch (Exception e) {
                e.printStackTrace();
            }

            db.deleteEntrries();

        }

                                }
       );



    }

    private void addUserToDatabase(EditText username, EditText email, EditText pass) {

        DatabaseHelper db=new DatabaseHelper(getApplicationContext());

        try {
            db.open();
        } catch (Exception e) {
            e.printStackTrace();
        }

        db.createEntry(username.getText().toString(), email.getText().toString(), pass.getText().toString());

    }
}
