package bluethooth.remote.control;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;

import android.bluetooth.BluetoothSocket;
import android.widget.TextView;

public class BluetoothDataManager {
    private BluetoothSocket socket;
    private OutputStream out;

    public BluetoothDataManager(BluetoothSocket socket) {
        this.socket = socket;

        getOutPutStream();

    }

    private void getOutPutStream() {
        try {

            out = socket.getOutputStream();
        } catch (IOException e) {

            e.printStackTrace();
        }
    }

    /**
     * send data
     *
     * @param data
     */
    public void send(byte data) {
        try {

            out.write(data);
            out.flush();
        } catch (IOException e) {

            e.printStackTrace();
        }
    }

    public void send(String data) {
        PrintWriter pw = new PrintWriter(out);
        pw.print(data);
        pw.flush();
    }

    public void closeOutputStream() {
        try {

            out.close();
        } catch (IOException e) {

            e.printStackTrace();
        }
    }

}
