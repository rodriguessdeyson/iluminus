package com.rad.iluminus;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.rad.Database.DatabaseAccess;
import com.rad.Database.models.BlynkDeviceModel;
import com.rad.Database.models.ColorHistory;
import com.rad.Utils.Lumos;
import com.rad.Utils.ViewUtils;
import com.skydoves.colorpickerview.ColorPickerView;
import com.skydoves.colorpickerview.listeners.ColorListener;
import com.skydoves.colorpickerview.sliders.BrightnessSlideBar;

import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

/**
 * Class that manipulates the Activity Iluminus.
 */
public class IluminusBlynkActivity extends AppCompatActivity implements View.OnClickListener, View.OnLongClickListener
{
	//region Attributes

	/**
	 * Variable to register callbacks;
	 */
	private int EDIT_DEVICES = 1000;

	/**
	 * Color information.
	 */
	public static String ColorHistoryDate;

	/**
	 * InterstitialAd reference.
	 */
	private InterstitialAd BlynkInterstitialAd;

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
	private ArrayList<View> ViewLayoutsFastColors;

	/**
	 * FB reference to open color split.
	 */
	private FloatingActionButton FBColorSplit;

	/**
	 * FB reference to close color history layout.
	 */
	private FloatingActionButton FBCloseColorHistory;

	/**
	 * FB reference to manipulate devices.
	 */
	private FloatingActionButton FBDevices;

	/**
	 * FB reference to manipulate settings.
	 */
	private FloatingActionButton FBSettings;

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

		// Method to treat all events.
		ControlsEvent();

		// Reload AdRequest.
		ReloadAdRequest();
	}

	@Override
	protected void onResume()
	{
		super.onResume();
		RetrieveBlynkDevices();

		// Reload AdRequest.
		ReloadAdRequest();
	}

	@Override
	public void onBackPressed()
	{
		AlertDialog.Builder build = new AlertDialog.Builder(this, R.style.AlertDialog);
		build.setTitle(R.string.ilu_Close_Title);
		build.setMessage(R.string.ilu_Close_Message).setCancelable(true);
		build.setPositiveButton(R.string.ilu_yes, new DialogInterface.OnClickListener()
		{
			@Override
			public void onClick(DialogInterface dialog, int which)
			{
				// Check if ad is loaded and ready to show.
				if (BlynkInterstitialAd.isLoaded())
				{
					if (ShowAd())
					{
						Database.UpdateLastAdShown();
						BlynkInterstitialAd.show();
					}
					else
					{
						ReloadAdRequest();
					}
				}
				else
					ReloadAdRequest();

				BlynkInterstitialAd.setAdListener(new AdListener()
				{
					@Override
					public void onAdClosed()
					{
						ReloadAdRequest();
					}

					@Override
					public void onAdFailedToLoad(LoadAdError loadAdError)
					{
						ReloadAdRequest();
					}
				});
				finish();
			}
		});
		build.setNegativeButton(R.string.ilu_no, (dialog, which) -> dialog.cancel());
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
		Database               = new DatabaseAccess(this, getString(R.string.DatabaseName));
		ViewLayoutsFastColors  = new ArrayList<>();

		// Reference the layouts views.
		ViewLayoutsFastColors.addAll(Arrays.asList(
			findViewById(R.id.ViewLayoutFastColorSelected1),
			findViewById(R.id.ViewLayoutFastColorSelected2),
			findViewById(R.id.ViewLayoutFastColorSelected3),
			findViewById(R.id.ViewLayoutFastColorSelected4),
			findViewById(R.id.ViewLayoutFastColorSelected5)));
		ColorHistoryDate          = getResources().getString(R.string.Iluminus_Saved_Color);
		TextViewCleanColorHistory = findViewById(R.id.TextViewCleanColorHistory);
		FBColorSplit              = findViewById(R.id.FloatingActionButtonSplit);
		FBSettings                = findViewById(R.id.FloatingActionButtonSettings);
		FBDevices                 = findViewById(R.id.FloatingActionButtonDevices);
		FBClose                   = findViewById(R.id.FloatingActionButtonClose);
		FBCloseColorHistory       = findViewById(R.id.FloatingActionButtonCloseColorHistory);
		ButtonSendColor           = findViewById(R.id.ButtonSendColor);
		ColorPickerSelected       = findViewById(R.id.ColorPickerView);
		SplitColorView            = findViewById(R.id.SplitLedStrip);
		ColorHistoryView          = findViewById(R.id.ColorHistory);
		ColorHistoryListView      = findViewById(R.id.ListViewColorHistory);
		SpinnerDevices            = findViewById(R.id.SpinnerDevices);
		SpinnerDevices
			.getBackground()
			.setColorFilter(getResources().getColor(R.color.deep_purple_500),
				PorterDuff.Mode.SRC_ATOP);
		ColorHistoryAdapter       = GetColorHistoryAdapter();
		ColorHistoryListView.setAdapter(ColorHistoryAdapter);

		// Set the brightness.
		BrightnessSlideBar brightnessSlideBar = findViewById(R.id.BrightnessSlide);
		ColorPickerSelected.attachBrightnessSlider(brightnessSlideBar);

		// Initiate the ads configuration for layout_serial and preferences.
		BlynkInterstitialAd = new InterstitialAd(this);

		// BlynkInterstitialAd.setAdUnitId("ca-app-pub-4749632185565734/7227333302");
		BlynkInterstitialAd.setAdUnitId("ca-app-pub-3940256099942544/1033173712"); // to test ad.
	}

	/**
	 * Creates a custom adapter.
	 * @return New custom adapter.
	 */
	@NotNull private ArrayAdapter<ColorHistory> GetColorHistoryAdapter()
	{
		return new ArrayAdapter<ColorHistory>(this, android.R.layout.simple_list_item_1, 0)
		{
			@NonNull @Override
			public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent)
			{
				View row;
				if (convertView == null)
				{
					LayoutInflater inflater =
						(LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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
				gDraw.setColor(colorHistory.GetColor());
				imageView.setBackground(gDraw);
				return row;
			}
		};
	}

	/**
	 * Gets the selected Bluetooth device configuration to connect.
	 */
	private void RetrieveBlynkDevices()
	{
		ArrayList<BlynkDeviceModel> blynkDevices = Database.ReadBlynkConfiguration();
		ArrayAdapter<BlynkDeviceModel> bDevices =
			new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, blynkDevices);
		SpinnerDevices.setAdapter(bDevices);
	}

	/**
	 * 	Register the controls events to be triggered.
	 */
	private void ControlsEvent()
	{
		// Creates the event of FloatingButton.
		FBSettings.setOnClickListener(this);
		FBDevices.setOnClickListener(this);
		FBColorSplit.setOnClickListener(this);
		FBClose.setOnClickListener(this);
		FBCloseColorHistory.setOnClickListener(this);
		TextViewCleanColorHistory.setOnClickListener(this);
		for (View fr : ViewLayoutsFastColors)
			fr.setOnClickListener(this);

		// Creates the event of Button.
		ButtonSendColor.setOnClickListener(this);
		ButtonSendColor.setOnLongClickListener(this);
		ColorPickerSelected.setColorListener((ColorListener) (color, fromUser) ->
		{
			GradientDrawable bEnabled = (GradientDrawable)ResourcesCompat.getDrawable(getResources(),
				R.drawable.button_enabled, null);
			assert bEnabled != null;
			bEnabled.setStroke(1, color);
			ButtonSendColor.setBackground(bEnabled);
		});
		ColorHistoryListView.setOnItemClickListener(OnColorsListClickListener);
	}

	/**
	 * Method to reload an ad request.
	 */
	private void ReloadAdRequest()
	{
		AdRequest adRequest = new AdRequest.Builder().build();
		BlynkInterstitialAd.loadAd(adRequest);
	}

	/**
	 * Checks if the ad is to be shown.
	 * @return True or false.
	 */
	private boolean ShowAd()
	{
		// Checks the last time an ad was shown.
		Date lastShowed = Database.GetTimeLastAdShowed();
		@SuppressLint("SimpleDateFormat")
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
		Date date = new Date();
		if (lastShowed == null)
		{
			Database.Insert(dateFormat.format(date));
			return true;
		}

		int MAX_MINUTE = 60000;
		return date.getTime() - lastShowed.getTime() > MAX_MINUTE;
	}

	/**
	 * Click event handler for all controls defined in {@link IluminusBlynkActivity#ControlsEvent()}
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
				if (FBDevices.getVisibility() == View.VISIBLE)
				{
					// ViewUtils.FadeOut(FBColorSplit);
					ViewUtils.FadeOut(FBDevices);
				}
				else
				{
					// ViewUtils.FadeIn(FBColorSplit);
					ViewUtils.FadeIn(FBDevices);
				}
				break;
			case R.id.FloatingActionButtonDevices:
				// Opens the devices manipulator.
				Intent startActivity = new Intent(IluminusBlynkActivity.this, Devices.class);
				startActivityForResult(startActivity, EDIT_DEVICES);
				break;
			case R.id.FloatingActionButtonSplit:
				// Hides some controls.
				ViewUtils.Visible(false, FBColorSplit, ButtonSendColor, ColorPickerSelected);
				ViewUtils.FadeIn(SplitColorView);
				break;
			case R.id.FloatingActionButtonClose:
				// Show some controls.
				ViewUtils.Visible(true, FBColorSplit, ButtonSendColor, ColorPickerSelected);
				ViewUtils.FadeOut(SplitColorView);
				break;
			case R.id.FloatingActionButtonCloseColorHistory:
				ViewUtils.Visible(false, ColorHistoryView);
				ViewUtils.Visible(true, ButtonSendColor, FBColorSplit);
				break;
			case R.id.ButtonSendColor:
				SendCommand(ColorPickerSelected.getColor());
				break;
			case R.id.ViewLayoutFastColorSelected1:
			case R.id.ViewLayoutFastColorSelected2:
			case R.id.ViewLayoutFastColorSelected3:
			case R.id.ViewLayoutFastColorSelected4:
			case R.id.ViewLayoutFastColorSelected5:
				for(int i = 0; i < ViewLayoutsFastColors.size(); i++)
				{
					View selectedColor = ViewLayoutsFastColors.get(i);
					if (selectedColor.getId() == v.getId())
					{
						int flColor = ((ColorDrawable)selectedColor.getBackground()).getColor();
						SendCommand(flColor);
						ColorPickerSelected.setPureColor(flColor);
						ColorPickerSelected.selectByHsv(flColor);
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
			SendCommand(Color.BLACK);
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
	 * Callback when the activity is finished.
	 * @param requestCode Code to receive callback.
	 * @param resultCode Result code of the activity.
	 * @param data Data received from activity.
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == EDIT_DEVICES && resultCode == Activity.RESULT_CANCELED)
			finish();
	}

	/**
	 * Listener to handle ListView clicks.
	 */
	private AdapterView.OnItemClickListener OnColorsListClickListener = (av, v, position, id) ->
	{
		// Get the object reference clicked.
		ColorHistory cHistory = (ColorHistory) av.getAdapter().getItem(position);
		SendCommand(cHistory.GetColor());
	};

	/**
	 * Allow to send commands through bluetooth.
	 * @param color Color.
	 */
	private void SendCommand(int color)
	{
		ColorHistory hColor = new ColorHistory();

		// Splits the color into integer and setup the command line.
		hColor.setA(Color.alpha(color));
		hColor.setR(Color.red(color));
		hColor.setG(Color.green(color));
		hColor.setB(Color.blue(color));

		// Gets the selected device in spinner.
		BlynkDeviceModel bDev = (BlynkDeviceModel)SpinnerDevices.getSelectedItem();
		String url;

		// Process the color send.
		try
		{
			switch (bDev.isMerged())
			{
				case 1:
					url = String.format(Locale.ENGLISH, "%s/%s/update/%s?value=%d&value=%d&value=%d",
						"http://blynk-cloud.com",
						bDev.getAuth(), bDev.getPin(), hColor.getR(), hColor.getG(), hColor.getB());
					BlynkServerConnection blynkTask = new BlynkServerConnection();
					blynkTask.execute(url);
					break;
				case 0:
					UpdatePinValue(bDev.getAuth(), bDev.getPinR(), hColor.getR());
					UpdatePinValue(bDev.getAuth(), bDev.getPinG(), hColor.getG());
					UpdatePinValue(bDev.getAuth(), bDev.getPinB(), hColor.getB());
					break;
			}

			// Save the color only if it's sent.
			Database.Insert(hColor);
		}
		catch (ExecutionException | InterruptedException e)
		{
			e.printStackTrace();
			Lumos.Show(this, R.string.BlynkServerNotReached);
		}
	}

	/**
	 * Updates the blynk color for not merged method.
	 * @param auth Auth token.
	 * @param pin Pin to update (R, G, B).
	 * @param pinValue Pin Value, (0...255).
	 * @return Return the task process.
	 * @throws ExecutionException Exception.
	 * @throws InterruptedException Exception.
	 */
	private boolean UpdatePinValue(String auth, String pin, Integer pinValue) throws ExecutionException,
		InterruptedException
	{
		// Builds the connection string to test.
		BlynkServerConnection blynkTask = new BlynkServerConnection();
		String connection = String.format(
			Locale.ENGLISH,
			"%s/%s/update/%s?value=%d",
			"http://blynk-cloud.com", auth, pin, pinValue);
		BlynkConnection.TaskState state = blynkTask.execute(connection).get();
		return state != BlynkConnection.TaskState.Failed;
	}

	//endregion

	//region Auxiliary Class

	/**
	 * Class to handle HTTP request to Blynk Server.
	 */
	public static class BlynkServerConnection extends AsyncTask<String, String, BlynkConnection.TaskState>
	{
		/**
		 * Executes before the background run
		 */
		@Override protected void onPreExecute()
		{
			super.onPreExecute();
		}

		/**
		 * Runs the task in background.
		 * @param params Connection parameters.
		 * @return The result of the task.
		 */
		protected BlynkConnection.TaskState doInBackground(String... params)
		{
			HttpURLConnection connection = null;
			BufferedReader reader = null;
			try
			{
				URL url = new URL(params[0]);
				connection = (HttpURLConnection) url.openConnection();
				connection.connect();
				InputStream stream = connection.getInputStream();
				reader = new BufferedReader(new InputStreamReader(stream));
				StringBuilder buffer = new StringBuilder();
				String line = "";
				while ((line = reader.readLine()) != null)
				{
					buffer.append(line).append("\n");
					Log.d("Response: ", "> " + line);
				}
				return BlynkConnection.TaskState.Concluded;
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
			finally
			{
				if (connection != null)
					connection.disconnect();
				try
				{
					if (reader != null)
						reader.close();
				}
				catch (IOException e)
				{
					e.printStackTrace();
				}
			}
			return BlynkConnection.TaskState.Failed;
		}

		/**
		 * Executes after the task has been concluded.
		 * @param taskState Task state.
		 */
		@Override protected void onPostExecute(BlynkConnection.TaskState taskState)
		{
			super.onPostExecute(taskState);
		}
	}

	//endregion
}
