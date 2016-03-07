package bluethooth.remote.control;

import java.io.IOException;
import java.util.UUID;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.widget.TextView;

/**
 * 
 * @author M.Houssainy
 * 
 *         BluetoothConnectionManager is one of the Remote Control for RC_Car
 *         controlled by using this app this class is responsible of creating
 *         the connection.
 */
public class BluetoothConnectionManager {

	private BluetoothAdapter btAdapter;
	private BluetoothSocket btSocket = null;
	private BluetoothDevice device;
	private final String macAddress = "00:12:11:23:01:06";//module
	// private final String macAddress = "08:D4:2B:E6:51:30";khaled
//	private final String macAddress = "58:C3:8B:CD:9E:98";//abdelmajeed
//	private final String macAddress = "58:C3:8B:E4:6C:F7";//Zeyad
	private final String uuid = "00001101-0000-1000-8000-00805F9B34FB"; /*
																		 * default
																		 * uuid
																		 */

	/**
	 * Constructor
	 */
	public BluetoothConnectionManager() {
		btAdapter = BluetoothAdapter.getDefaultAdapter();
	}

	/**
	 * create a connection with the given MacAddress and UUID
	 * 
	 * @return Bluetooth Socket
	 */
	public BluetoothSocket connect() {

		device = btAdapter.getRemoteDevice(macAddress);

		try {
			btSocket = device.createRfcommSocketToServiceRecord(UUID.fromString(uuid));

			btSocket.connect();

		} catch (IOException e) {
			e.printStackTrace();
		}

//		closeSocket();

		return btSocket;
	}

	public void closeSocket() {
		try {
			btSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
