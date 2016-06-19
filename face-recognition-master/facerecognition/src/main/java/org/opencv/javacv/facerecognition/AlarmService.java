package org.opencv.javacv.facerecognition;

import java.util.Calendar;
import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.IBinder;
import java.util.Timer;
import java.util.TimerTask;

public class AlarmService extends Service {

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Bundle extras = intent.getExtras();
        String time = extras.getString("time");
        String[] h_m = time.split(":");
        TimerTask timerTask = new MyTimerTask(Integer.parseInt(h_m[0]), Integer.parseInt(h_m[0]));
        //running timer task as daemon thread
        Timer timer = new Timer(true);
        timer.scheduleAtFixedRate(timerTask, 0, 1000);
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private class MyTimerTask extends TimerTask {
        boolean alarmseted = false;
        int hours, minutes;

        MyTimerTask(int h, int m) {
            hours = h;
            minutes = m;
        }

        @Override
        public void run() {
            Calendar mycal = Calendar.getInstance();
            int h = mycal.get(Calendar.HOUR_OF_DAY);
            int m = mycal.get(Calendar.MINUTE);
            if (h == hours && m == minutes && !alarmseted) {
                alarmseted = true;
                MediaPlayer mediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.tone2);
                mediaPlayer.start();
            }
        }
    }
}