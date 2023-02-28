package com.rad.Bluetooth;

import android.os.AsyncTask;

import java.io.IOException;

public abstract class BluetoothDisconnectionHandler extends AsyncTask<Void, Void, Boolean>
{
	@Override
	protected Boolean doInBackground(Void... voids)
	{
		try
		{
			// Checks if it's necessary to close the socket.
			if (BluetoothConnectionHandler.BluetoothSocket != null &&
					BluetoothConnectionHandler.BluetoothSocket.isConnected())
			{
				// Closes the socket device.
				BluetoothConnectionHandler.BluetoothSocket.close();
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		return true;
	}
}
