package bluethooth.remote.control;

import android.os.Bundle;
import android.app.Activity;
import android.bluetooth.BluetoothSocket;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.ToggleButton;

public class Controller {

//	private ToggleButton onOff;
//	private boolean on;
    private int horizontalAngle;
    private int verticalAngle;
    private int step = 1;
    private BluetoothConnectionManager bcm;
    private BluetoothDataManager bdm;

    public Controller() {
        horizontalAngle = 110;
        verticalAngle = 45;
        move((byte)horizontalAngle);
        move((byte) (verticalAngle +180) );
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
        Runnable run = new Runnable() {
            @Override
            public void run() {
                bdm.send(b);
            }
        };

        Thread myThread = new Thread(run);
        myThread.start();
    }

    public void goRight(int theta) {


        horizontalAngle = Math.max(horizontalAngle - theta, 0);
        move((byte) horizontalAngle);
    }

    public void goLeft(int theta) {

        horizontalAngle = Math.min(horizontalAngle + theta, 179);
        move((byte) horizontalAngle);
    }

    public void goDown() {
        verticalAngle = Math.max(verticalAngle - step, 0);
        move((byte) (verticalAngle+ 180));
    }

    public void goUp() {
        verticalAngle = Math.min(verticalAngle + step, 70);
        move((byte) (verticalAngle +180));
    }

    public void moveWithAngle(int angle){

        move((byte) angle);
    }


}
