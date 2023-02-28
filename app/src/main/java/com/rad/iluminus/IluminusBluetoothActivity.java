package com.rad.iluminus;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.res.ResourcesCompat;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.rad.Bluetooth.BluetoothConnectionHandler;
import com.rad.Bluetooth.BluetoothDisconnectionHandler;
import com.rad.Bluetooth.BluetoothWriterHandler;
import com.rad.Database.DatabaseAccess;
import com.rad.Database.models.BluetoothDevicesModel;
import com.rad.Database.models.ColorHistory;
import com.rad.Utils.Lumos;
import com.rad.Utils.Alert;
import com.rad.Utils.ViewUtils;
import com.skydoves.colorpickerview.ColorPickerView;
import com.skydoves.colorpickerview.listeners.ColorListener;
import com.skydoves.colorpickerview.sliders.BrightnessSlideBar;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;

import static android.bluetooth.BluetoothAdapter.ACTION_STATE_CHANGED;

/**
 * Class that manipulates the Activity Iluminus.
 */
public class IluminusBluetoothActivity extends Activity implements View.OnClickListener, View.OnLongClickListener
{
	//region Attributes

	/**
	 * Color information.
	 */
	public static String ColorHistoryDate;

	/**
	 * Flag to indicate that the device saved, selected, etc. is connected.
	 */
	private static boolean IsDeviceConnected = false;

	/**
	 * Intent filter to manipulate actions.
	 */
	private IntentFilter FilterAction;

	/**
	 * Bluetooth device model to be connected.
	 */
	private BluetoothDevicesModel BluetoothToConnect;

	/**
	 * Object reference to ReadInput thread.
	 */
	private static ReadInput ReadInputThread = null;

	/**
	 * Object to connect to the database.
	 */
	private DatabaseAccess Database;

	/**
	 * ArrayAdapter to show color in ListView.
	 */
	private ArrayAdapter<ColorHistory> ColorHistoryAdapter;

	//endregion

	//region Views

	/**
	 * List of objects references of fast coloring.
	 */
	private ArrayList<FrameLayout> FrameLayoutsFastColors;

	/**
	 * FB reference to open color split.
	 */
	private FloatingActionButton FBColorSplit;

	/**
	 * FB reference to close color history layout.
	 */
	private FloatingActionButton FBCloseColorHistory;

	/**
	 * FB reference to close split color.
	 */
	private FloatingActionButton FBClose;

	/**
	 * Button reference to send color.
	 */
	private Button ButtonSendColor;

	/**
	 * Object reference to ColorPicker.
	 */
	private ColorPickerView ColorPickerSelected;

	/**
	 * Object reference to manipulate SplitColorView.
	 */
	private View SplitColorView;

	/**
	 * Object reference to manipulate ColorHistory.
	 */
	private View ColorHistoryView;

	/**
	 * Object reference to manipulate ColorHistoryListView.
	 */
	private ListView ColorHistoryListView;

	/**
	 * Object reference to clean database.
	 */
	private TextView TextViewCleanColorHistory;

	/**
	 * Spinner with all devices configured.
	 */
	private Spinner SpinnerDevices;

	//endregion

	//region Activity Override

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		// Set screens to all configuration.
		SetScreenSettings();
		super.onCreate(savedInstanceState);
		setContentView(R.layout.layout_iluminus);

		// Initialize all controls.
		StartViewContents();

		// Initialize filters.
		FilteringActions();

		// Method to treat all events.
		ControlsEvent();
	}

	@Override
	protected void onResume()
	{
		super.onResume();

		// Retrieve the Bluetooth device selected.
		RetrieveBluetoothDevices();
		if (!IsDeviceConnected)
		{
			try
			{
				// Starts the connection task.
				new StartConnection(this, BluetoothToConnect).execute();
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
	}

	@Override
	protected void onPause()
	{
		if (IsDeviceConnected)
		{
			// Stop the tread.
			if (ReadInputThread.IsRunning())
				ReadInputThread.Stop();

			new StopConnection().execute();
		}
		super.onPause();
	}

	@Override
	public void onBackPressed()
	{
		AlertDialog.Builder build = new AlertDialog.Builder(this);
		build.setTitle(R.string.ilu_Close_Title);
		build.setMessage(R.string.ilu_Close_Message).setCancelable(true);
		build.setPositiveButton(R.string.ilu_yes, (dialog, which) ->
		{
			new StopConnection().execute();
			IluminusBluetoothActivity.super.onBackPressed();
		});
		build.setNegativeButton(R.string.ilu_no, (dialog, which) ->
		{
			// TODO Auto-generated method stub
			dialog.cancel();
		});

		AlertDialog alertDialog = build.create();
		alertDialog.setCanceledOnTouchOutside(false);
		alertDialog.show();
	}

	//endregion

	//region Methods

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
		// Initialize objects.
		FilterAction                  = new IntentFilter();
		Database                      = new DatabaseAccess(this, getString(R.string.DatabaseName));
		FrameLayoutsFastColors        = new ArrayList<>();

		// Reference the layouts views.
		FrameLayoutsFastColors.addAll(Arrays.asList(
			findViewById(R.id.ViewLayoutFastColorSelected1),
			findViewById(R.id.ViewLayoutFastColorSelected2),
			findViewById(R.id.ViewLayoutFastColorSelected3),
			findViewById(R.id.ViewLayoutFastColorSelected4),
			findViewById(R.id.ViewLayoutFastColorSelected5)));
		ColorHistoryDate              = getResources().getString(R.string.Iluminus_Saved_Color);
		TextViewCleanColorHistory     = findViewById(R.id.TextViewCleanColorHistory);
		FBColorSplit                  = findViewById(R.id.FloatingActionButtonSettings);
		FBClose                       = findViewById(R.id.FloatingActionButtonClose);
		FBCloseColorHistory           = findViewById(R.id.FloatingActionButtonCloseColorHistory);
		ButtonSendColor               = findViewById(R.id.ButtonSendColor);
		ColorPickerSelected           = findViewById(R.id.ColorPickerView);
		SplitColorView                = findViewById(R.id.SplitLedStrip);
		SpinnerDevices                = findViewById(R.id.SpinnerDevices);
		ColorHistoryView              = findViewById(R.id.ColorHistory);
		ColorHistoryListView          = findViewById(R.id.ListViewColorHistory);
		ColorHistoryAdapter           = GetColorHistoryAdapter();
		ColorHistoryListView.setAdapter(ColorHistoryAdapter);

		// Set the brightness.
		BrightnessSlideBar brightnessSlideBar = findViewById(R.id.BrightnessSlide);
		ColorPickerSelected.attachBrightnessSlider(brightnessSlideBar);
	}

	/**
	 * Creates a custom adapter.
	 * @return New custom adapter.
	 */
	@NotNull
	private ArrayAdapter<ColorHistory> GetColorHistoryAdapter()
	{
		return new ArrayAdapter<ColorHistory>(this, android.R.layout.simple_list_item_1, 0)
		{
			@NonNull @Override
			public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent)
			{
				View row;
				if (convertView == null)
				{
					LayoutInflater inflater = (LayoutInflater)getContext()
						.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
					row = inflater.inflate(R.layout.list_color_history, parent, false);
				}
				else
					row = convertView;
				TextView textView = row.findViewById(R.id.TextViewInfo);
				ImageView imageView = row.findViewById(R.id.ImageViewColorHistory);

				ColorHistory colorHistory = ColorHistoryAdapter.getItem(position);
				assert colorHistory != null;
				textView.setText(colorHistory.toString());
				GradientDrawable gDraw = (GradientDrawable)ResourcesCompat
					.getDrawable(getResources(), R.drawable.led_frame, null);
				assert gDraw != null;
				gDraw.setColor(colorHistory.GetColor());
				imageView.setBackground(gDraw);
				return row;
			}
		};
	}

	/**
	 * Gets the selected Bluetooth device configuration to connect.
	 */
	private void RetrieveBluetoothDevices()
	{
		ArrayList<BluetoothDevicesModel> bluetoothDevices = Database.ReadBluetoothConfiguration();
		ArrayAdapter<BluetoothDevicesModel> bDevices =
				new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, bluetoothDevices);
		SpinnerDevices.setAdapter(bDevices);

		// Gets the first device.
		BluetoothToConnect = (BluetoothDevicesModel) SpinnerDevices.getSelectedItem();
	}

	/**
	 * 	Method to filter Bluetooth actions.
	 */
	private void FilteringActions()
	{
		FilterAction.addAction(ACTION_STATE_CHANGED);
	}

	/**
	 * 	Register the controls events to be triggered.
	 */
	private void ControlsEvent()
	{
		// Creates the event of FloatingButton.
		FBColorSplit.setOnClickListener(this);
		FBClose.setOnClickListener(this);
		FBCloseColorHistory.setOnClickListener(this);
		TextViewCleanColorHistory.setOnClickListener(this);
		for (FrameLayout fr : FrameLayoutsFastColors)
			fr.setOnClickListener(this);

		// Creates the event of Button.
		ButtonSendColor.setOnClickListener(this);
		ButtonSendColor.setOnLongClickListener(this);
		ColorPickerSelected.setColorListener((ColorListener) (color, fromUser) ->
		{
			GradientDrawable bEnabled = (GradientDrawable)ResourcesCompat
				.getDrawable(getResources(), R.drawable.button_enabled,null );
			assert bEnabled != null;
			bEnabled.setStroke(1, color);
			ButtonSendColor.setBackground(bEnabled);
		});
		ColorHistoryListView.setOnItemClickListener(OnColorsListClickListener);
		SpinnerDevices.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
		{
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
			{
				if (BluetoothToConnect != null)
				{
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent)
			{

			}
		});
	}

	/**
	 * Click event handler for all controls defined in {@link IluminusBluetoothActivity#ControlsEvent()}
	 * @param v View reference of control triggered.
	 */
	@Override public void onClick(View v)
	{
		switch (v.getId())
		{
			case R.id.TextViewCleanColorHistory:
				Database.DeleteColorHistory();
				ColorHistoryAdapter.clear();
				Lumos.Show(this, R.string.Iluminus_Database_Cleaned);
				break;
			case R.id.FloatingActionButtonSettings:
				// Hides some controls.
				ViewUtils.Visible(false, FBColorSplit, ButtonSendColor,
					ColorPickerSelected);
				ViewUtils.FadeIn(SplitColorView);
				break;
			case R.id.FloatingActionButtonClose:
				// Show some controls.
				ViewUtils.Visible(true, FBColorSplit, ButtonSendColor,
					ColorPickerSelected);
				ViewUtils.FadeOut(SplitColorView);
				break;
			case R.id.FloatingActionButtonCloseColorHistory:
				ViewUtils.Visible(false, ColorHistoryView);
				ViewUtils.Visible(true, ButtonSendColor, FBColorSplit);
				break;
			case R.id.ButtonSendColor:
				SendCommand("L", ColorPickerSelected.getColor());
				break;
			case R.id.ViewLayoutFastColorSelected1:
			case R.id.ViewLayoutFastColorSelected2:
			case R.id.ViewLayoutFastColorSelected3:
			case R.id.ViewLayoutFastColorSelected4:
			case R.id.ViewLayoutFastColorSelected5:
				for(int i = 0; i < FrameLayoutsFastColors.size(); i++)
				{
					FrameLayout selectedColor = FrameLayoutsFastColors.get(i);
					if (selectedColor.getId() == v.getId())
					{
						int flColor = ((ColorDrawable)selectedColor.getBackground()).getColor();
						SendCommand("L", flColor);
						ColorPickerSelected.setPureColor(flColor);
						ColorPickerSelected
							.selectByHsv(flColor);
						break;
					}
				}
				break;
			default:
				throw new IllegalStateException("Unexpected value: " + v.getId());
		}
	}

	@Override
	public boolean onLongClick(View v)
	{
		// Clear the adapter.
		ColorHistoryAdapter.clear();

		// Read the history.
		ArrayList<ColorHistory> colorHistories = Database.ReadColorHistory();
		if (colorHistories.size() == 0)
		{
			SendCommand("L", Color.BLACK);
			return false;
		}

		// Fills the adapter.
		ColorHistoryAdapter.addAll(colorHistories);

		// Shows the color history selector.
		ViewUtils.Visible(true, ColorHistoryView);
		ViewUtils.Visible(false, ButtonSendColor, FBColorSplit);
		return true;
	}

	/**
	 * Listener to handle ListView clicks.
	 */
	private AdapterView.OnItemClickListener OnColorsListClickListener = (av, v, position, id) ->
	{
		// Get the object reference clicked.
		ColorHistory cHistory = (ColorHistory) av.getAdapter().getItem(position);
		SendCommand("L", cHistory.GetColor());
	};

	/**
	 * Allow to send commands through bluetooth.
	 * @param command Command to be sent.
	 * @param color Color.
	 */
	private void SendCommand(String command, int color)
	{
		ColorHistory hColor = new ColorHistory();

		// Splits the color into integer and setup the command line.
		hColor.setA(Color.alpha(color));
		hColor.setR(Color.red(color));
		hColor.setG(Color.green(color));
		hColor.setB(Color.blue(color));
		String cmd = (command + "," + hColor.getR() + "," + hColor.getG() + "," + hColor.getB());

		if (!BluetoothWriterHandler.SendCommand(cmd))
			Lumos.Show(this, R.string.Iluminus_Device_NotConnected);

		// Save the color only if it's sent.
		Database.Insert(hColor);
	}

	//endregion

	//region Classes to manipulate data.

	/**
	 * Class to manipulate the Bluetooth connection.
	 */
	private static class StartConnection extends BluetoothConnectionHandler
	{
		/**
		 * Object to show Progress while attempting to connect.
		 */
		private Alert ConnectingProgress;

		WeakReference<Context> MainContReference;

		/**
		 * Constructor for this abstract class.
		 *
		 * @param deviceToConnect Device selected to connect.
		 */
		StartConnection(Context ctx, BluetoothDevicesModel deviceToConnect)
		{
			super(deviceToConnect);
			MainContReference = new WeakReference<>(ctx);
			ConnectingProgress = new Alert(ctx, Alert.ProgressType.CONNECTING_PROGRESS);
		}

		@Override
		protected void onPreExecute()
		{
			ConnectingProgress.Show();
		}

		@Override
		protected void onPostExecute(Boolean result)
		{
			super.onPostExecute(result);
			if (IsDeviceConnected = this.IsSelectedDeviceConnected)
			{
				Lumos.Show(MainContReference.get(), R.string.Iluminus_Device_Connected);
		 		ConnectingProgress.Dismiss();

		 		// Starts the activity to read.
				ReadInputThread = new ReadInput();
			}
			else
			{
				Lumos.Show(MainContReference.get(), R.string.Iluminus_Device_NotConnected);
		 		ConnectingProgress.Dismiss();
				((IluminusBluetoothActivity)MainContReference.get()).finish();
			}
		}
	}

	/**
	 * Class to manipulate the Bluetooth disconnection.
	 */
	private static class StopConnection extends BluetoothDisconnectionHandler
	{
		@Override
		protected void onPostExecute(Boolean result)
		{
			super.onPostExecute(result);
			IsDeviceConnected = false;
		}
	}

	/**
	 * Class to manipulate Bluetooth incoming data.
	 */
	private static class ReadInput implements Runnable
	{
		//region Attributes

		/**
		 * Flag to control while loop.
		 */
		private boolean Stop = false;

		/**
		 * Thread to be run.
		 */
		private Thread thread;

		//endregion

		//region Constructor

		/**
		 * Initialize an object of ReadInput.
		 */
		ReadInput()
		{
			thread = new Thread(this, "Bluetooth Incoming Data Thread");
			thread.start();
		}

		//endregion

		//region Methods
		/**
		 * Get the status of the thread.
		 * @return True if the thread is running, false otherwise.
		 */
		boolean IsRunning()
		{
			return thread.isAlive();
		}

		/**
		 * Stop the running thread.
		 */
		void Stop()
		{
			Stop = true;
		}

		//endregion

		@Override
		public void run()
		{
			try
			{
				InputStream inStream = BluetoothConnectionHandler.BluetoothSocket.getInputStream();
				while (!Stop)
				{
					byte[] buffer = new byte[256];
					if (inStream.available() > 0)
					{
						int bRead = inStream.read(buffer);

						// Value received through bluetooth.
						String valueReceived = new String(buffer, 0, bRead);
						Log.i("valor", valueReceived);
					}

					Thread.sleep(500);
				}
			}
			catch (IOException | InterruptedException e)
			{
				e.printStackTrace();
			}
		}
	}

	//endregion
}
