package com.rad.Database.models;

import android.os.Parcel;
import android.os.Parcelable;

import org.jetbrains.annotations.NotNull;

/**
 * Blynk setting properties.
 */
public class BlynkDeviceModel implements Parcelable
{
	/**
	 * Id of the device.
	 */
	private int Id;

	/**
	 * Name of the device.
	 */
	private String Name;

	/**
	 * API Auth Token.
	 */
	private String Auth;

	/**
	 * Flag to indicate if the json is merged or not.
	 */
	private boolean IsMerged;

	/**
	 * Virtual R pin indicator.
	 */
	private String PinR;

	/**
	 * Virtual G pin indicator.
	 */
	private String PinG;

	/**
	 * Virtual B pin indicator.
	 */
	private String PinB;

	/**
	 * Virtual pin indicator.
	 */
	private String Pin;

	public BlynkDeviceModel() {}

	protected BlynkDeviceModel(Parcel in)
	{
		Id       = in.readInt();
		Name     = in.readString();
		Auth     = in.readString();
		IsMerged = in.readByte() != 0;
		PinR     = in.readString();
		PinG     = in.readString();
		PinB     = in.readString();
		Pin      = in.readString();
	}

	public static final Creator<BlynkDeviceModel> CREATOR = new Creator<BlynkDeviceModel>() {
		@Override
		public BlynkDeviceModel createFromParcel(Parcel in) {
			return new BlynkDeviceModel(in);
		}

		@Override
		public BlynkDeviceModel[] newArray(int size) {
			return new BlynkDeviceModel[size];
		}
	};

	public void setId(int id) {
		Id = id;
	}

	public void setName(String name) {
		Name = name;
	}

	public void setAuth(String auth) {
		Auth = auth;
	}

	public void setMerged(int merged)
	{
		IsMerged = merged == 1;
	}

	public void setPin(String pin)
	{
		Pin = pin;
	}

	public void setPinB(String pinB) {
		PinB = pinB;
	}

	public void setPinG(String pinG) {
		PinG = pinG;
	}

	public void setPinR(String pinR) {
		PinR = pinR;
	}

	public int getId() {
		return Id;
	}

	public String getName() { return Name; }

	public String getPin() { return Pin; }

	public String getPinB() { return PinB; }

	public String getPinG() { return PinG; }

	public String getPinR() { return PinR;	}

	public int isMerged() {	return IsMerged ? 1 : 0; }

	public String getAuth()	{ return Auth;	}

	@NotNull
	@Override
	public String toString()
	{
		return Name.isEmpty() ? "" : Name;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(Id);
		dest.writeString(Name);
		dest.writeString(Auth);
		dest.writeByte((byte) (IsMerged ? 1 : 0));
		dest.writeString(PinR);
		dest.writeString(PinG);
		dest.writeString(PinB);
		dest.writeString(Pin);
	}
}
