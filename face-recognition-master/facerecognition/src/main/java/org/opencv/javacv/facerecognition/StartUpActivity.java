package org.opencv.javacv.facerecognition;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.objdetect.CascadeClassifier;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import testingairesponse.MainActivity;

public class StartUpActivity extends Activity {

    static PersonRecognizer fr;
    RelativeLayout linearLayout;

    static GridView gridView;
    static GridViewAdapter gridAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_up);
        SharedPreferences prefs = getSharedPreferences("first_use_flag", MODE_PRIVATE);
        boolean restoredText = prefs.getBoolean("signedup", false);


        linearLayout = (RelativeLayout)findViewById(R.id.main_layout);

        gridView = (GridView) findViewById(R.id.gridView);
        gridAdapter = new GridViewAdapter(this, R.layout.grid_item_layout, new ArrayList());
//        gridView.setAdapter(gridAdapter);

        if(restoredText){

//            new Handler().postDelayed(new Runnable() {
//                @Override
//                public void run() {
//                    System.out.println("Going to Profile Data");
//	  /* Create an Intent that will start the ProfileData-Activity. */
//                    Intent mainIntent = new Intent(StartUpActivity.this, FdActivity.class);
//                    StartUpActivity.this.startActivity(mainIntent);
//                    StartUpActivity.this.finish();
//                }
//            }, 2500);

//            Intent intent = new Intent(getApplicationContext(),org.opencv.javacv.facerecognition.FdActivity.class);
//            startActivity(intent);
//            finish();

            new LoadOpenCV().execute();

        }else{
            Intent intent = new Intent(getApplicationContext(),org.opencv.javacv.facerecognition.signup_activity.class);
            startActivity(intent);
            finish();
        }
    }

    private class LoadOpenCV extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // before making http calls

        }

        @Override
        protected Void doInBackground(Void... arg0) {
                            fr = new PersonRecognizer(getFilesDir() + "/facerecogOCV/");
                            fr.load(linearLayout,getApplicationContext());

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            Intent intent = new Intent(getApplicationContext(),org.opencv.javacv.facerecognition.FdActivity.class);
            startActivity(intent);
            finish();
        }

    }
}
