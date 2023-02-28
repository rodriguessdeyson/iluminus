package com.rad.Database.models;

import android.bluetooth.BluetoothDevice;

import androidx.annotation.NonNull;

import java.util.UUID;

/**
 * Class representation of database entity Configuration.
 */
public class BluetoothDevicesModel
{
	/**
	 * Id of the register.
	 */
	private int Id;

	/**
	 * Name of Bluetooth device.
	 */
	private String DeviceName;

	/**
	 * Address of the device.
	 */
	private String DeviceAddress;

	/**
	 * Supported service uuid of the device.
	 */
	private UUID ServiceUUID;

	/**
	 * Bluetooth device reference.
	 */
	private BluetoothDevice Device;

	/**
	 * Set and Id.
	 * @param id Id of row.
	 */
	public void setId(int id)
	{
		Id = id;
	}

	/**
	 * Set the device address.
	 * @param deviceAddress Address of the device.
	 */
	public void setDeviceAddress(String deviceAddress)
	{
		this.DeviceAddress = deviceAddress;
	}

	/**
	 * Set the device name.
	 * @param deviceName Name of the device.
	 */
	public void setDeviceName(String deviceName)
	{
		this.DeviceName = deviceName;
	}

	/**
	 * Set the identifier of the device service.
	 * @param serviceUUID UUID of the used service.
	 */
	public void setServiceUUID(UUID serviceUUID)
	{
		this.ServiceUUID = serviceUUID;
	}

	/**
	 * Set the reference of the bluetooth device.
	 * @param device Bluetooth device of the configured model.
	 */
	public void setDevice(BluetoothDevice device)
	{
		this.Device = device;
	}

	/**
	 * Get the device Id.
	 * @return Id of the row.
	 */
	public int getId()
	{
		return this.Id;
	}

	/**
	 * Get the device UUID.
	 * @return UUID of the device service.
	 */
	public UUID getServiceUUID()
	{
		return this.ServiceUUID;
	}

	/**
	 * Get the Bluetooth device name.
	 * @return Bluetooth device name.
	 */
	public String getDeviceName()
	{
		return this.DeviceName;
	}

	/**
	 * Get the Bluetooth device address.
	 * @return The Address of the Bluetooth.
	 */
	public String getDeviceAddress()
	{
		return this.DeviceAddress;
	}

	/**
	 * Get the Bluetooth device configured reference.
	 * @return Bluetooth device reference.
	 */
	public BluetoothDevice getDevice()
	{
		return this.Device;
	}

	@NonNull
	@Override
	public String toString()
	{
		return getDeviceName();
	}

}
