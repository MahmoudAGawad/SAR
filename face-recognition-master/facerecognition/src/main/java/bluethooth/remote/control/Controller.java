package bluethooth.remote.control;

import android.os.Bundle;
import android.app.Activity;
import android.bluetooth.BluetoothSocket;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.ToggleButton;

import java.util.concurrent.atomic.AtomicBoolean;

public class Controller {

//	private ToggleButton onOff;
//	private boolean on;
    private int horizontalAngle;
    private int verticalAngle;
    private int step = 1;
    private BluetoothConnectionManager bcm;
    private BluetoothDataManager bdm;

    private AtomicBoolean acceptData;

    public Controller() {
        horizontalAngle = 110;
        verticalAngle = 45;
        acceptData = new AtomicBoolean(true);
        move((byte)horizontalAngle);
        move((byte) (verticalAngle + 180));
    }

    public void connectToSAR() {
        Runnable run = new Runnable() {
            @Override
            public void run() {
                bcm = new BluetoothConnectionManager();
                BluetoothSocket socket = bcm.connect();
                bdm = new BluetoothDataManager(socket);
            }
        };

        Thread myThread = new Thread(run);
        myThread.start();
    }

    public void disconnectToSAR() {

        Runnable run = new Runnable() {
            @Override
            public void run() {
                if (bdm != null)
                    bdm.closeOutputStream();
                if (bcm != null)
                    bcm.closeSocket();
            }
        };

        Thread myThread = new Thread(run);
        myThread.start();
    }

    private void move(final byte b) {
        if(bdm == null) return;
        acceptData.set(false);
        synchronized (acceptData) {
            Runnable run = new Runnable() {
                @Override
                public void run() {
                    bdm.send(b);
                    acceptData.set(true);
                }
            };

            Thread myThread = new Thread(run);
            myThread.start();
        }
    }

    public void goRight(int theta) {
        Log.e("boleaaaaaaaaaaaaan", acceptData.toString());
        if (acceptData.get()) {
            horizontalAngle = Math.max(horizontalAngle - theta, 0);
            Log.e("Hori. SAAAAAAAAAAAAR", horizontalAngle + "");
            move((byte) horizontalAngle);
        }
    }

    public void goLeft(int theta) {
        Log.e("boleaaaaaaaaaaaaan", acceptData.toString());
        if(acceptData.get()) {
            horizontalAngle = Math.min(horizontalAngle + theta, 179);
            Log.e("Hori. SAAAAAAAAAAAR", horizontalAngle + "");
            move((byte) horizontalAngle);
        }
    }

    public void goDown(int theta) {
        if(acceptData.get()) {
            verticalAngle = Math.max(verticalAngle - theta, 0);
            move((byte) (verticalAngle + 180));
        }
    }

    public void goUp(int theta) {
        if(acceptData.get()) {
            verticalAngle = Math.min(verticalAngle + theta, 70);
            move((byte) (verticalAngle + 180));
        }
    }

    public void moveWithAngle(int angle){

        move((byte) angle);
    }


}
