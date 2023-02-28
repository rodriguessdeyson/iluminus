package com.rad.iluminus;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.rad.Database.DatabaseAccess;

/**
 * Allow o create an splash screen.
 */
public class SplashOpening extends Activity
{
	//region Attributes

	/**
	 * Animate the background of the screen.
	 */
	private AnimationDrawable AnimationDrawable;

	//endregion

	//region RuntimeEvents

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle icicle)
	{
		super.onCreate(icicle);
		setContentView(R.layout.layout_opening);

		// Creates a database, if not exists.
		DatabaseAccess database = new DatabaseAccess(this, getString(R.string.DatabaseName));
		database.CreateDatabase();

		// New Handler to start the Menu-Activity and close this Splash-Screen after some seconds.
		int splashDisplayLength = 2000;

		// Maps the background to animate.
		ConstraintLayout homeLayout = findViewById(R.id.ConstraintLayoutSplash);
		AnimationDrawable = (AnimationDrawable) homeLayout.getBackground();
		AnimationDrawable.setEnterFadeDuration(1000);
		AnimationDrawable.setExitFadeDuration(1000);

		// Starts the handle.
		new Handler().postDelayed(() ->
		{
			// Intent to initialize other activities.
			Intent startActivity = new Intent (SplashOpening.this, MainActivity.class);
			startActivity(startActivity);
			this.finish();
		}, splashDisplayLength);
	}

	/** Called when the activity is resumed. */
	@Override
	public void onResume()
	{
		super.onResume();
		if (AnimationDrawable != null && !AnimationDrawable.isRunning())
			AnimationDrawable.start();
	}

	/** Called when the activity is killed. */
	@Override
	protected void onDestroy()
	{
		super.onDestroy();
		if (AnimationDrawable != null && AnimationDrawable.isRunning())
			AnimationDrawable.stop();
	}

	//endregion
}
