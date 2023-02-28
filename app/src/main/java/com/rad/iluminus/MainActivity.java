package com.rad.iluminus;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.rad.Bluetooth.BluetoothHandler;
import com.rad.Database.DatabaseAccess;
import com.rad.Utils.Lumos;

/**
 * Main class of the application.
 */
public class MainActivity extends AppCompatActivity
{
	//region Views

	/**
	 * Button Setup bluetooth reference.
	 */
	private Button ButtonBluetooth;

	/**
	 * Button Setup blynk reference.
	 */
	private Button ButtonBlynk;

	//endregion

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		// Set screens to all configuration.
		SetScreenSettings();
		super.onCreate(savedInstanceState);
		setContentView(R.layout.layout_main);

		// Initialize all controls.
		StartViewContents();

		// Method to treat all events.
		ControlsEvent();
	}

	/**
	 * Method to configure users app interface.
	 */
	@SuppressLint("SourceLockedOrientationActivity")
	private void SetScreenSettings()
	{
		this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
	}

	/**
	 * Method to initiate all view controllers.
	 */
	private void StartViewContents()
	{
		ButtonBluetooth = findViewById(R.id.ButtonBluetooth);
		ButtonBlynk = findViewById(R.id.ButtonBlynk);
	}

	/**
	 * Manipulate all events.
	 */
	private void ControlsEvent()
	{
		/*
		  Event triggered when the Blynk button is pressed.
		*/
		ButtonBlynk.setOnClickListener(v -> RunBlynkConfiguration());

		/*
		  Event triggered when the Bluetooth button is pressed.
		*/
		ButtonBluetooth.setOnClickListener(v -> RunBluetoothConfiguration());
	}

	/**
	 * Run the configuration.
	 */
	private void RunBlynkConfiguration()
	{
		// Checks if blynk devices are configured.
		DatabaseAccess dbAccess = new DatabaseAccess(this, getString(R.string.DatabaseName));
		Intent startActivity;
		if (dbAccess.ReadBlynkConfiguration().size() == 0)
			// Intent to initialize other activities.
			startActivity = new Intent(MainActivity.this, BlynkConnection.class);
		else
			// Intent to initialize other activities.
			startActivity = new Intent(MainActivity.this, IluminusBlynkActivity.class);
		startActivity(startActivity);
	}

	/**
	 * Run the configuration.
	 */
	private void RunBluetoothConfiguration()
	{
		// Checks if the current device has Bluetooth.
		BluetoothHandler bHandler = new BluetoothHandler();
		if (!bHandler.IsAvailable())
		{
			Lumos.Show(this, R.string.WelcomeSetup_BluetoothNotAvailable);
			return;
		}

		// Checks if there's Bluetooth configured.
		DatabaseAccess dbAccess = new DatabaseAccess(this, getString(R.string.DatabaseName));
		Intent startActivity;
		if (dbAccess.ReadBluetoothConfiguration().size() == 0)
			startActivity = new Intent(MainActivity.this, BluetoothConnection.class);
		else
			startActivity =
				new Intent(MainActivity.this, IluminusBluetoothActivity.class);
		startActivity(startActivity);
	}
}