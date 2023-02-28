package com.rad.Bluetooth;

import java.io.IOException;

/**
 * Allows to send an data through Bluetooth device.
 */
public class BluetoothWriterHandler
{
	/**
	 * Send an string command/information.
	 * @param command Command to be sent.
	 * @return True if the command has been sent. False, otherwise.
	 */
	public static boolean SendCommand(String command)
	{
		return Write(command);
	}

	/**
	 * Send an string command/information.
	 * @param command Command to be sent.
	 * @return True if the command has been sent. False, otherwise.
	 */
	public static boolean SendCommand(int command)
	{
		return Write(String.valueOf(command));
	}

	/**
	 * Send an string command/information.
	 * @param command Command to be sent.
	 * @return True if the command has been sent. False, otherwise.
	 */
	public static boolean SendCommand (float command)
	{
		return Write(String.valueOf(command));
	}

	/**
	 * Send an string command/information.
	 * @param command Command to be sent.
	 * @return True if the command has been sent. False, otherwise.
	 */
	public static boolean SendCommand (double command)
	{
		return Write(String.valueOf(command));
	}

	/**
	 * Writes the command through BluetoothDevice.
	 * @param command Command to be sent.
	 * @return Return true if the commando has been sent. False, otherwise.
	 */
	private static boolean Write(String command)
	{
		if (BluetoothConnectionHandler.BluetoothSocket != null &&
			BluetoothConnectionHandler.BluetoothSocket.isConnected())
		{
			try
			{
				BluetoothConnectionHandler.BluetoothSocket
					.getOutputStream()
					.write(command.getBytes());
				return true;
			}
			catch (IOException e)
			{
				e.printStackTrace();
				return false;
			}
		}
		return false;
	}
}
