package com.rad.Bluetooth;

import android.bluetooth.BluetoothA2dp;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothHeadset;
import android.bluetooth.BluetoothHealth;
import android.content.Intent;
import android.os.ParcelUuid;
import android.os.Parcelable;

import com.rad.Database.models.BluetoothDevicesModel;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

import static android.bluetooth.BluetoothAdapter.ACTION_REQUEST_ENABLE;
import static android.bluetooth.BluetoothDevice.EXTRA_DEVICE;

/**
 * Allows to create an object of BluetoothHandler to control the Bluetooth device.
 */
public class BluetoothHandler
{
	//region Attributes

	/**
	 * Allows to manipulate the Bluetooth adapter.
	 */
	private BluetoothAdapter BluetoothAdapter;

	//endregion

	//region Constructor

	/**
	 * Initializes an object of type BluetoothHandler.
	 */
	public BluetoothHandler()
	{
		BluetoothAdapter = android.bluetooth.BluetoothAdapter.getDefaultAdapter();
	}

	/**
	 * Default UUID returned if the desired one is not found.
	 */
	public UUID DefaultUUID = UUID.fromString("00000000-0000-0000-0000-00800000000");

	//endregion

	// region Methods

	/**
	 * Checks if the Bluetooth device is available.
	 * @return True if the device has Bluetooth.
	 */
	public Boolean IsAvailable()
	{
		return BluetoothAdapter != null;
	}

	/**
	 * Check if the Bluetooth device is enabled.
	 * @return True for enabled, false otherwise.
	 */
	public Boolean IsEnabled()
	{
		return BluetoothAdapter.isEnabled();
	}

	/**
	 * Returns whether the Bluetooth device is turned on, turned off or turning off.
	 * @return The state of the Bluetooth device.
	 */
	public int GetBluetoothState()
	{
		return BluetoothAdapter.getState();
	}

	/**
	 * Method to turn user's device Bluetooth on.
	 * @return An intent command to turn bluetooth on.
	 */
	public Intent TurnOn()
	{
		return new Intent(ACTION_REQUEST_ENABLE);
	}

	/**
	 * Turns off the Bluetooth.
	 */
	public void TurnOff()
	{
		if (IsEnabled() && !BluetoothDevicesConnected())
		{
			try
			{
				BluetoothAdapter.disable();
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
	}

	/**
	 * Gets all paired devices that matches to the current Bluetooth service used.
	 * @return A list with BluetoothDevices information.
	 */
	@NotNull
	private ArrayList<BluetoothDevicesModel> GetBoundedDevices()
	{
		Set<BluetoothDevice> devicesArray = BluetoothAdapter.getBondedDevices();
		ArrayList<BluetoothDevicesModel> pairedDevices = new ArrayList<>();
		if (devicesArray.size() > 0)
		{
			for (BluetoothDevice device: devicesArray)
			{
				for (ParcelUuid uuid : device.getUuids())
				{
					String uuidService = uuid.getUuid().toString().split("-")[0];
					if (uuidService.equals("00001101"))
					{
						BluetoothDevicesModel btModel = new BluetoothDevicesModel();
						btModel.setDevice(device);
						btModel.setDeviceName(device.getName());
						btModel.setDeviceAddress(device.getAddress());
						btModel.setServiceUUID(uuid.getUuid());
						pairedDevices.add(btModel);
					}
				}
			}
		}
		return pairedDevices;
	}

	/**
	 * Starts discovering all bluetooth devices around.
	 */
	public void SearchDevices()
	{
		if (!BluetoothAdapter.isDiscovering())
			BluetoothAdapter.startDiscovery();
	}

	/**
	 * Gets the device from the address, if it is visible.
	 * @param deviceAddress The address of the device to be found.
	 * @return BluetoothDevice structure.
	 */
	BluetoothDevice GetDeviceByAddress(String deviceAddress)
	{
		return BluetoothAdapter.getRemoteDevice(deviceAddress);
	}

	/**
	 * Stops the searching if it's running.
	 */
	void StopSearchingDevices()
	{
		if (BluetoothAdapter.isDiscovering())
			BluetoothAdapter.cancelDiscovery();
	}

	/**
	 * Checks if the Bluetooth device has active connections.
	 * @return Returns true if there's an active connection.
	 */
	private Boolean BluetoothDevicesConnected()
	{

		return
				BluetoothAdapter.getProfileConnectionState(BluetoothHeadset.HEADSET) ==
						BluetoothHeadset.STATE_CONNECTED ||
				BluetoothAdapter.getProfileConnectionState(BluetoothHealth.HEALTH) ==
						BluetoothHealth.STATE_CONNECTED ||
				BluetoothAdapter.getProfileConnectionState(BluetoothA2dp.A2DP) ==
						BluetoothA2dp.STATE_CONNECTED;
	}

	/**
	 * Allows to retrieve the Bluetooth device data from intent action.
	 * @param intent Intent action the happened.
	 * @return Return the structured device detected.
	 */
	public BluetoothDevicesModel GetBluetoothDevice(@NotNull Intent intent)
	{
		BluetoothDevice deviceFound = intent.getParcelableExtra(EXTRA_DEVICE);
		BluetoothDevicesModel bt = null;
		String s;
		ArrayList<BluetoothDevicesModel> boundedDevices = GetBoundedDevices();
		if (boundedDevices.size() > 0)
		{
			for (int i = 0; i < boundedDevices.size(); i++)
			{
				bt = boundedDevices.get(i);
				if (deviceFound.getName() != null && deviceFound.getName().equals(bt.getDeviceName()))
				{
					s = "*";
					bt.setDeviceName(bt.getDeviceName() + " " + s);
					break;
				}
				else
				{
					bt = new BluetoothDevicesModel();
					bt.setDeviceName(deviceFound.getName());
					bt.setDeviceAddress(deviceFound.getAddress());
					bt.setDevice(deviceFound);
				}
			}
		}
		else
		{
			bt = new BluetoothDevicesModel();
			bt.setDeviceName(deviceFound.getName());
			bt.setDeviceAddress(deviceFound.getAddress());
			bt.setDevice(deviceFound);
		}
		return bt;
	}

	/**
	 * Gets the devices UUID discovered.
	 * @param intent The intent of the request.
	 * @return The UUID required.
	 */
	public UUID GetUUID(Intent intent)
	{
		Parcelable[] uuidExtra = intent.getParcelableArrayExtra(BluetoothDevice.EXTRA_UUID);
		UUID desiredUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

		if (uuidExtra == null)
			return DefaultUUID;

		String uuidService = DefaultUUID.toString();
		for (Parcelable uuid : uuidExtra)
		{
			// Checks if the service is available in device.
			if (!uuid.toString().equals(desiredUUID.toString()))
				return DefaultUUID;

				// Saves the uuid id.
				uuidService = uuid.toString();
				break;
		}
		return UUID.fromString(uuidService);
	}

	//endregion
}