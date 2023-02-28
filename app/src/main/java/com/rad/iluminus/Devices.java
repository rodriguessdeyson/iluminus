package com.rad.iluminus;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.rad.Adapter.DeviceListAdapter;
import com.rad.Database.DatabaseAccess;
import com.rad.Database.models.BlynkDeviceModel;
import com.rad.Interfaces.IClickListener;

import java.util.ArrayList;
import java.util.Objects;

public class Devices extends AppCompatActivity implements View.OnClickListener
{
	//region Attributes

	/**
	 * Variable to register callbacks;
	 */
	private int ADDED_NEW_DEVICE = 1000;

	/**
	 * RecyclerView with all devices configured to Blynk/Bluetooth.
	 */
	private RecyclerView RecyclerViewDevices;

	/**
	 * Floating Button reference to add new device.
	 */
	private FloatingActionButton FBAddDevice;

	/**
	 * RecyclerView adapter to create custom adapter.
	 */
	private RecyclerView.Adapter RecyclerViewAdapterDevices;

	/**
	 * All devices configured.
	 */
	private ArrayList<BlynkDeviceModel> Devices = new ArrayList<>();

	//endregion

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		// Set screens to all configuration.
		SetScreenSettings();
		super.onCreate(savedInstanceState);
		setContentView(R.layout.layout_devices);

		// Load database.
		LoadDevices();

		// Initialize all controls.
		StartViewContents();
		StartWidgetsEvents();
	}

	@Override
	protected void onResume()
	{
		super.onResume();
		UpdateUI();
	}

	@Override
	public void onBackPressed()
	{
		// Checks if the database has some device left, if not, goes to the main activity.
		this.setResult(Devices.size() == 0 ? 0 : -1);
		this.finish();
	}

	/**
	 * Loads the devices from database.
	 */
	private void LoadDevices()
	{
		// Starts the database.
		DatabaseAccess db = new DatabaseAccess(this, getString(R.string.DatabaseName));

		// Gets the devices.
		Devices.clear();
		Devices.addAll(db.ReadBlynkConfiguration());
	}

	/**
	 * Starts the views widgets.
	 */
	private void StartWidgetsEvents()
	{
		// Initialize the FB event.
		FBAddDevice.setOnClickListener(this);

		// Initialize the RecyclerView Manager.
		RecyclerViewDevices.setLayoutManager(new LinearLayoutManager(this));
		RecyclerViewDevices.addItemDecoration(CreateDividerDecorator());
		RecyclerViewDevices.setHasFixedSize(true);
		RecyclerViewAdapterDevices = new DeviceListAdapter<>(this, Devices, iClickListener);
		RecyclerViewDevices.setAdapter(RecyclerViewAdapterDevices);
	}

	/**
	 * Method to catch the delete option.
	 */
	private IClickListener iClickListener = (v, itemId) ->
	{
		switch (v.getId())
		{
			case R.id.ImageViewDeleteItem:
				DeleteDeviceSetting(itemId, v.getTag());
				break;
			case R.id.ImageViewEditItem:
				// Gets the device from item tag and start to edit.
				BlynkDeviceModel dModel = (BlynkDeviceModel)v.getTag();
				Intent editItem = new Intent(Devices.this, BlynkConnection.class);
				editItem.putExtra("EditValues", true);
				editItem.putExtra("EditSettings", dModel);
				startActivity(editItem);
				break;
		}
	};

	/**
	 * Method to delete a device from database.
	 * @param id Identification of the item to be deleted.
	 * @param device Device settings.
	 */
	private void DeleteDeviceSetting(int id, Object device)
	{
		// Asks if the user wants to delete.
		AlertDialog.Builder build = new AlertDialog.Builder(this, R.style.AlertDialog);
		build
			.setTitle(R.string.DeleteDeviceTitle)
			.setMessage(R.string.DeleteDeviceMessage)
			.setCancelable(true);
		build.setPositiveButton(R.string.ilu_yes, (dialog, which) ->
		{
			// Removes from database.
			DatabaseAccess dbAccess = new DatabaseAccess(this, getString(R.string.DatabaseName));
			dbAccess.DeleteBlynkSettingById(id);

			// Removes from the list.
			Devices.remove(device);

			// Updates the UI.
			UpdateUI();
		});
		build.setNegativeButton(R.string.ilu_no, (dialog, which) -> dialog.cancel());

		AlertDialog alertDialog = build.create();
		alertDialog.setCanceledOnTouchOutside(false);
		alertDialog.show();
	}

	/**
	 * Updates the UI based on database values.
	 */
	private void UpdateUI()
	{
		// Gets the devices.
		LoadDevices();

		// Gets the devices.
		RecyclerViewAdapterDevices.notifyDataSetChanged();
		RecyclerViewDevices.smoothScrollToPosition(Devices.size());
	}

	/**
	 * Adds a line between items on recycler view.
	 * @return A derisory line.
	 */
	private RecyclerView.ItemDecoration CreateDividerDecorator()
	{
		int orientation = ((LinearLayoutManager) Objects
			.requireNonNull(RecyclerViewDevices.getLayoutManager())).getOrientation();
		return new DividerItemDecoration(RecyclerViewDevices.getContext(), orientation);
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
		RecyclerViewDevices = findViewById(R.id.RecyclerViewDevices);
		FBAddDevice = findViewById(R.id.FloatingActionButtonAddDevice);
	}

	/**
	 * Executes the event when the widget is clicked.
	 * @param v Id of the widget.
	 */
	@Override public void onClick(View v)
	{
		if (v.getId() == R.id.FloatingActionButtonAddDevice)
			startActivityForResult(new Intent(Devices.this, BlynkConnection.class), ADDED_NEW_DEVICE);
	}

	/**
	 * Callback when the activity is finished.
	 * @param requestCode Code to receive callback.
	 * @param resultCode Result code of the activity.
	 * @param data Data received from activity.
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == ADDED_NEW_DEVICE && resultCode == Activity.RESULT_OK)
			UpdateUI();
	}
}
