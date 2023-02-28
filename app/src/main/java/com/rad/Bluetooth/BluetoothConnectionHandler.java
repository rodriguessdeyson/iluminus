package com.rad.Bluetooth;

import android.bluetooth.BluetoothSocket;
import android.os.AsyncTask;

import com.rad.Database.models.BluetoothDevicesModel;

public abstract class BluetoothConnectionHandler extends AsyncTask<Void, Void, Boolean>
{
	/**
	 * Flag indicating that the selected device has been connected successfully.
	 */
	protected boolean IsSelectedDeviceConnected = false;

	/**
	 * Allows to manipulate the BluetoothSocket.
	 */
	public static BluetoothSocket BluetoothSocket = null;

	/**
	 * Device selected to connect.
	 */
	private BluetoothDevicesModel DeviceToConnect;

	/**
	 * Method to manipulate Bluetooth.
	 */
	private BluetoothHandler Bluetooth =  new BluetoothHandler();

	/**
	 * Constructor for this abstract class.
	 * @param deviceToConnect Device selected to connect.
	 */
	public BluetoothConnectionHandler(BluetoothDevicesModel deviceToConnect)
	{
		DeviceToConnect = deviceToConnect;
	}

	@Override
	protected Boolean doInBackground(Void... voids)
	{
		try
		{
			if (BluetoothSocket == null || !BluetoothSocket.isConnected())
			{
				BluetoothSocket = Bluetooth
					.GetDeviceByAddress(DeviceToConnect.getDeviceAddress())
					.createInsecureRfcommSocketToServiceRecord(DeviceToConnect.getServiceUUID());
				Bluetooth.StopSearchingDevices();
				BluetoothSocket.connect();
				IsSelectedDeviceConnected = true;
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return IsSelectedDeviceConnected = false;
		}
		return IsSelectedDeviceConnected;
	}
}
