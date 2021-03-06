package bluethooth.remote.control;

import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.util.concurrent.atomic.AtomicBoolean;

public class Controller {

    //	private ToggleButton onOff;
//	private boolean on;
    private int horizontalAngle;
    private int verticalAngle;
    private int step = 1;
    private BluetoothConnectionManager bcm;
    private BluetoothDataManager bdm;

    private AtomicBoolean acceptHorizontalData;
    private AtomicBoolean acceptVerticalData;

    public Controller() {
        horizontalAngle = 110;
        verticalAngle = 45;
        acceptHorizontalData = new AtomicBoolean(true);
        acceptVerticalData = new AtomicBoolean(true);
        move((byte) horizontalAngle, acceptHorizontalData);
        move((byte) (verticalAngle + 180), acceptVerticalData);
        acceptVerticalData =  new AtomicBoolean(true);
        move((byte)horizontalAngle , acceptHorizontalData);
        move((byte) (verticalAngle + 180) , acceptVerticalData);
    }

    public void connectToSAR(int width, int height) {

        final String dimension = "W" + width + "H" + height + "Z";

        if(bcm == null) {
            Runnable run = new Runnable() {
                @Override
                public void run() {
                    bcm = new BluetoothConnectionManager();
                    BluetoothSocket socket = bcm.connect();
                    bdm = new BluetoothDataManager(socket);


                    Log.e("SAR.init", dimension);
                    bdm.send(dimension);
                }
            };


            Thread myThread = new Thread(run);
            myThread.start();
        }
    }

    public void disconnectToSAR() {

        if (bdm != null)
            bdm.closeOutputStream();
        if (bcm != null)
            bcm.closeSocket();
    }

    public void move(int x, int y) {
        Log.e("SAR", "enter");
        if(bdm != null) {
            String position = "X" + x + "Y" + y + "Z";
            Log.e("SAR", position);
            bdm.send(position);
        }
    }



    private void move(final byte b, final AtomicBoolean acceptData) {
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
        Log.e("right boleaaaaaaaaaan", acceptHorizontalData.toString());
        if (acceptHorizontalData.get()) {
            horizontalAngle = Math.max(horizontalAngle - theta, 0);
            Log.e("Hori. SAAAAAAAAAAAAR", horizontalAngle + "");
            move((byte) horizontalAngle, acceptHorizontalData);
            move((byte) horizontalAngle ,acceptHorizontalData );
        }
    }

    public void goLeft(int theta) {
        Log.e("left boleaaaaaaaaaan", acceptHorizontalData.toString());
        if (acceptHorizontalData.get()) {
            if (acceptHorizontalData.get()) {
                horizontalAngle = Math.min(horizontalAngle + theta, 179);
                Log.e("Hori. SAAAAAAAAAAAR", horizontalAngle + "");
                move((byte) horizontalAngle, acceptHorizontalData);
            }
        }
    }

    public void goDown(int theta) {
        Log.e("down boleaaaaaaaaaan", acceptVerticalData.toString());
        if (acceptVerticalData.get()) {
            if (acceptVerticalData.get()) {
                verticalAngle = Math.max(verticalAngle - theta, 0);
                Log.e("Vert. SAAAAAAAAAAAR", (verticalAngle + 180) + "");
                move((byte) (verticalAngle + 180), acceptVerticalData);
            }
        }
    }

    public void goUp(int theta) {
        Log.e("up boleaaaaaaaaaan", acceptVerticalData.toString());
        if (acceptVerticalData.get()) {
            if (acceptVerticalData.get()) {
                verticalAngle = Math.min(verticalAngle + theta, 70);
                Log.e("Vert. SAAAAAAAAAAAR", (verticalAngle + 180) + "");
                move((byte) (verticalAngle + 180), acceptVerticalData);
            }
        }
    }
//
//    public void moveWithAngle(int angle){
//
//        move((byte) angle);
//    }


}
//
//    public void moveWithAngle(int angle){
//
//        move((byte) angle);
//    }



