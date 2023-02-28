package com.rad.iluminus;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.LoadAdError;
import com.rad.Database.DatabaseAccess;
import com.rad.Database.models.BlynkDeviceModel;
import com.rad.Utils.Alert;
import com.rad.Utils.Lumos;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;
import java.util.concurrent.ExecutionException;

public class BlynkConnection extends Activity
{
	/**
	 * Task status to conclude.
	 */
	public enum TaskState
	{
		Concluded,
		Failed,
	}

	//region Blynk

	/**
	 * CheckBox Enabled/Disabled reference.
	 */
	private CheckBox CheckBoxMerge;

	/**
	 * Button to try the settings.
	 */
	private Button ButtonTrySetting;

	/**
	 * Blynk application API auth token.
	 */
	private EditText EditTextAuthToken;

	/**
	 * Blynk application API auth token.
	 */
	private EditText EditTextName;

	/**
	 * Blynk pin.
	 */
	private EditText EditTextPin;

	/**
	 * Blynk R pin.
	 */
	private EditText EditTextPinR;

	/**
	 * Blynk G pin.
	 */
	private EditText EditTextPinG;

	/**
	 * Blynk B pin.
	 */
	private EditText EditTextPinB;

	// endregion

	//region Dialogs

	/**
	 * Progress dialog of connections.
	 */
	private static Alert ProgressDialog;

	//endregion

	//region Attributes

	/**
	 * InterstitialAd reference.
	 */
	private InterstitialAd BlynkInterstitialAd;

	/**
	 * Blynk device model setting.
	 */
	BlynkDeviceModel BlynkModel = new BlynkDeviceModel();

	/**
	 * Database reference.
	 */
	private DatabaseAccess BlynkDatabase;

	//endregion

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		// Set screens to all configuration.
		SetScreenSettings();
		super.onCreate(savedInstanceState);
		setContentView(R.layout.layout_configure_blynk);

		// Retrieves the settings, if passed.
		RetrieveSettings();

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

		// Reload AdRequest.
		ReloadAdRequest();
	}

	@Override
	public void onBackPressed()
	{
		// Check if ad is loaded and ready to show.
		if (BlynkInterstitialAd.isLoaded())
		{
			if (ShowAd())
			{
				BlynkDatabase.UpdateLastAdShown();
				BlynkInterstitialAd.show();
			}
			else
				ReloadAdRequest();
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
		super.onBackPressed();
	}

	/**
	 * Retrieves the data passed through intent.
	 */
	private void RetrieveSettings()
	{
		// Check if there's data passed thought intent extras.
		Intent editData = getIntent();
		if (editData.hasExtra("EditValues"))
			BlynkModel = editData.getParcelableExtra("EditSettings");
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
		BlynkDatabase     = new DatabaseAccess(this, getString(R.string.DatabaseName));
		ButtonTrySetting  = findViewById(R.id.ButtonTryBlynkSetting);
		EditTextAuthToken = findViewById(R.id.EditTextAppToken);
		EditTextName      = findViewById(R.id.EditTextAppName);
		EditTextPin       = findViewById(R.id.EditTextAppPin);
		EditTextPinR      = findViewById(R.id.EditTextAppPinR);
		EditTextPinG      = findViewById(R.id.EditTextAppPinG);
		EditTextPinB      = findViewById(R.id.EditTextAppPinB);
		CheckBoxMerge     = findViewById(R.id.CheckBoxMerge);
		ProgressDialog    = new Alert(this, Alert.ProgressType.CONNECTING_PROGRESS, null, null);

		// Check if there's data passed through intent extras.
		Intent editData = getIntent();
		if (editData.hasExtra("EditValues"))
		{
			EditTextAuthToken.setText(BlynkModel.getAuth());
			EditTextName.setText(BlynkModel.getName());
			CheckBoxMerge.setChecked(BlynkModel.isMerged() == 1);
			if (CheckBoxMerge.isChecked())
				EditTextPin.setText(BlynkModel.getPin());
			else
			{
				EditTextPinR.setText(BlynkModel.getPinR());
				EditTextPinG.setText(BlynkModel.getPinG());
				EditTextPinB.setText(BlynkModel.getPinB());
			}
			UpdatePinsSettingState(CheckBoxMerge.isChecked());
		}

		// Initiate the ads configuration for layout_serial and preferences.
		BlynkInterstitialAd = new InterstitialAd(this);

		// BlynkInterstitialAd.setAdUnitId("ca-app-pub-4749632185565734/2885775371");
		BlynkInterstitialAd.setAdUnitId("ca-app-pub-3940256099942544/1033173712"); // to test ad.
	}

	/**
	 * Manipulate all events.
	 */
	private void ControlsEvent()
	{
		// Changes the configuration layout.
		CheckBoxMerge.setOnCheckedChangeListener((buttonView, isChecked) ->
				UpdatePinsSettingState(isChecked));

		// Tries the connection and save it.
		ButtonTrySetting.setOnClickListener(v ->
		{
			if (IsSettingsFieldOK())
				OnBlynkSettingAdRequest();
		});
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
	 * Method to show an ad when tuning button is clicked.
	 */
	private void OnBlynkSettingAdRequest()
	{
		// Check if ad is loaded and ready to show.
		if (BlynkInterstitialAd.isLoaded())
		{
			if (ShowAd())
			{
				BlynkDatabase.UpdateLastAdShown();
				BlynkInterstitialAd.show();
			}
			else
			{
				RunSetting();
				ReloadAdRequest();
			}
		}
		else
		{
			RunSetting();
			ReloadAdRequest();
		}

		BlynkInterstitialAd.setAdListener(new AdListener()
		{
			@Override
			public void onAdClosed()
			{
				RunSetting();
				ReloadAdRequest();
			}

			@Override
			public void onAdFailedToLoad(LoadAdError loadAdError)
			{
				ReloadAdRequest();
			}
		});
	}

	/**
	 * Checks if the ad is to be shown.
	 * @return True or false.
	 */
	private boolean ShowAd()
	{
		// Checks the last time an ad was shown.
		Date lastShowed = BlynkDatabase.GetTimeLastAdShowed();
		if (lastShowed == null)
			return false;

		Date date = new Date();
		// Max time between ad shown.
		int MAX_MINUTE = 60000;
		return date.getTime() - lastShowed.getTime() > MAX_MINUTE;
	}

	/**
	 * Run the device test and save setting.
	 */
	private void RunSetting()
	{
		// Validates the settings
		if (!IsSettingsFieldOK())
			return;
		try
		{
			SaveSetting();
		}
		catch (Exception ex)
		{
			new Alert(this, Alert.ProgressType.OK, "",
					getString(R.string.SettingsFailedSaving)).Show();
			Log.i("Saving Setting", ex.getMessage());
		}
	}

	/**
	 * Enables/Disable the pins setting.
	 * @param isChecked Bool to indicate the state.
	 */
	private void UpdatePinsSettingState(boolean isChecked)
	{
		LinearLayout red   = findViewById(R.id.LinearLayoutPinR);
		LinearLayout green = findViewById(R.id.LinearLayoutPinG);
		LinearLayout blue  = findViewById(R.id.LinearLayoutPinB);
		LinearLayout pin   = findViewById(R.id.LinearLayoutPin);
		if (isChecked)
		{
			// Hides the Pins.
			red.setVisibility(View.GONE);
			green.setVisibility(View.GONE);
			blue.setVisibility(View.GONE);
			pin.setVisibility(View.VISIBLE);
		}
		else
		{
			// Hides the Pins.
			red.setVisibility(View.VISIBLE);
			green.setVisibility(View.VISIBLE);
			blue.setVisibility(View.VISIBLE);
			pin.setVisibility(View.GONE);
		}
	}

	/**
	 * Checks if the settings are filled up.
	 * @return True if yes, false if not.
	 */
	private boolean IsSettingsFieldOK()
	{
		String name = EditTextName.getText().toString();
		String auth = EditTextAuthToken.getText().toString();
		String pin  = EditTextPin.getText().toString();
		String r    = EditTextPinR.getText().toString();
		String g    = EditTextPinG.getText().toString();
		String b    = EditTextPinB.getText().toString();
		if (name.isEmpty())
		{
			EditTextName.setError(getString(R.string.Setting_Field_Empty));
			return false;
		}
		else
			EditTextName.setError(null);
		if (auth.isEmpty())
		{
			EditTextAuthToken.setError(getString(R.string.Setting_Field_Empty));
			return false;
		}
		else
			EditTextAuthToken.setError(null);

		// Validates the R, G and B fields only if the merge is disabled.
		if (!CheckBoxMerge.isChecked())
		{
			if (r.isEmpty())
			{
				EditTextPinR.setError(getString(R.string.Setting_Field_Empty));
				return false;
			}
			else
				EditTextPinR.setError(null);
			if (g.isEmpty())
			{
				EditTextPinG.setError(getString(R.string.Setting_Field_Empty));
				return false;
			}
			else
				EditTextPinG.setError(null);
			if (b.isEmpty())
			{
				EditTextPinB.setError(getString(R.string.Setting_Field_Empty));
				return false;
			}
			else
				EditTextPinB.setError(null);
		}
		else
		{
			if (pin.isEmpty())
			{
				EditTextPin.setError(getString(R.string.Setting_Field_Empty));
				return false;
			}
			else
				EditTextPin.setError(null);
		}
		return true;
	}

	/**
	 * Testes the setting and save it.
	 */
	private void SaveSetting() throws ExecutionException, InterruptedException
	{
		// Get the auth token.
		String auth = EditTextAuthToken.getText().toString();

		if (CheckBoxMerge.isChecked())
		{
			// Gets the default pin for merged setting.
			String pinDefault = EditTextPin.getText().toString();

			// Builds the connection string to test.
			String connection = String
				.format("%s/%s/update/%s?value=255&value=255&value=255", "http://blynk-cloud.com",
				auth, pinDefault);
			BlynkServerConnection blynkTask = new BlynkServerConnection();
			TaskState state = blynkTask.execute(connection).get();
			switch (state)
			{
				case Concluded:
					BlynkModel.setName(EditTextName.getText().toString());
					BlynkModel.setAuth(EditTextAuthToken.getText().toString());
					BlynkModel.setMerged(CheckBoxMerge.isChecked() ? 1 : 0);
					BlynkModel.setPinR(null);
					BlynkModel.setPinG(null);
					BlynkModel.setPinB(null);
					BlynkModel.setPin(EditTextPin.getText().toString());
					if (getIntent().hasExtra("EditValues"))
						BlynkDatabase.UpdateBlynk(BlynkModel);
					else
						BlynkDatabase.Insert(BlynkModel);
					break;
				case Failed:
					new Alert(this, Alert.ProgressType.OK, "", getString(R.string.SettingsFailedSaving))
						.Show();
					return;
			}
		}
		else
		{
			//Gets the pins.
			String pinR = EditTextPinR.getText().toString();
			String pinG = EditTextPinG.getText().toString();
			String pinB = EditTextPinB.getText().toString();

			// Sends the update.
			if (!UpdatePinValue(auth, pinR))
			{
				new Alert(this, Alert.ProgressType.OK, "", getString(R.string.SettingsFailedSaving)).Show();
				return;
			}
			if (!UpdatePinValue(auth, pinG))
			{
				new Alert(this, Alert.ProgressType.OK, "", getString(R.string.SettingsFailedSaving)).Show();
				return;
			}
			if (!UpdatePinValue(auth, pinB))
			{
				new Alert(this, Alert.ProgressType.OK, "", getString(R.string.SettingsFailedSaving)).Show();
				return;
			}

			// Add to database.
			BlynkModel.setName(EditTextName.getText().toString());
			BlynkModel.setAuth(EditTextAuthToken.getText().toString());
			BlynkModel.setMerged(CheckBoxMerge.isChecked() ? 1 : 0);
			BlynkModel.setPinR(EditTextPinR.getText().toString());
			BlynkModel.setPinG(EditTextPinG.getText().toString());
			BlynkModel.setPinB(EditTextPinB.getText().toString());
			BlynkModel.setPin(null);

			if (getIntent().hasExtra("EditValues"))
				BlynkDatabase.UpdateBlynk(BlynkModel);
			else
				BlynkDatabase.Insert(BlynkModel);
		}

		// Let user know that everything ran fine.
		Lumos.Show(this, R.string.SettingsSaved);

		// Checks which activity to go.
		Intent startActivity;
		if (BlynkDatabase.ReadBlynkConfiguration().size() == 1)
		{
			// Go to the main activity.
			startActivity = new Intent (BlynkConnection.this, IluminusBlynkActivity.class);
			startActivity(startActivity);
		}
		this.setResult(-1);
		this.finish();
	}

	/**
	 * Updates the ZeRGB pins individually.
	 * @param auth Auth token.
	 * @param pin Pin id to update.
	 * @return Return false if failed.
	 * @throws ExecutionException Exception.
	 * @throws InterruptedException Exception.
	 */
	private boolean UpdatePinValue(String auth, String pin) throws ExecutionException, InterruptedException
	{
		// Builds the connection string to test.
		BlynkServerConnection blynkTask = new BlynkServerConnection();
		String connection = String.format("%s/%s/update/%s?value=255", "http://blynk-cloud.com", auth, pin);
		TaskState state = blynkTask.execute(connection).get();
		return state != TaskState.Failed;
	}

	/**
	 * Class to handle HTTP request to Blynk Server.
	 */
	public class BlynkServerConnection extends AsyncTask<String, String, TaskState>
	{
		/**
		 * Executes before the background run
		 */
		@Override protected void onPreExecute()
		{
			super.onPreExecute();
			ProgressDialog =
				new Alert(BlynkConnection.this, Alert.ProgressType.CONNECTING_PROGRESS, null, null);
		}

		/**
		 * Runs the task in background.
		 * @param params Connection parameters.
		 * @return The result of the task.
		 */
		protected TaskState doInBackground(String... params)
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
				return TaskState.Concluded;
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
			return TaskState.Failed;
		}

		/**
		 * Executes after the task has been concluded.
		 * @param taskState Task state.
		 */
		@Override protected void onPostExecute(TaskState taskState)
		{
			super.onPostExecute(taskState);
			if (ProgressDialog.IsShowing())
				ProgressDialog.Dismiss();
		}
	}
}