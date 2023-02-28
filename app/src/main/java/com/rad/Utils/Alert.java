package com.rad.Utils;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.widget.ProgressBar;

import com.rad.iluminus.R;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

import static android.os.Build.VERSION.SDK_INT;

/**
 * Allows to create a custom progress bar.
 */
public class Alert
{
	//region Enumeration

	public enum ProgressType
	{
		SEARCHING_PROGRESS,
		CONNECTING_PROGRESS,
		CONNECTING_SERVER,
		OK
	}

	//endregion

	//region Attributes

	/**
	 * Allows to create an AlertDialog.
	 */
	private final AlertDialog Dialog;

	/**
	 * Type of dialog.
	 */
	private final ProgressType Type;

	//endregion

	//region Constructor

	public Alert(Context ct, ProgressType type)
	{
		Dialog = CreateDialog(ct, type, null, null);
		Type = type;
	}

	public Alert(Context ct, ProgressType type, String title, String message)
	{
		Dialog = CreateDialog(ct, type, title, message);
		Type = type;
	}

	//endregion

	//region Methods

	/**
	 * Show the configured progress bar.
	 */
	public void Show()
	{
		if (!IsShowing())
		{
			Dialog.show();
			if (Type != ProgressType.OK)
				Objects.requireNonNull(Dialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
		}
	}

	/**
	 * Dismiss the configured progress bar if it is showing.
	 */
	public void Dismiss()
	{
		if (IsShowing())
			Dialog.dismiss();
	}

	/**
	 * Checks if the Progress is showing.
	 * @return Returns true if the Progress is showing.
	 */
	@NotNull
	public Boolean IsShowing()
	{
		return Dialog.isShowing();
	}

	/**
	 * Builds an AlertDialog.
	 * @param ct Activity context.
	 * @param type Type of Progress to be shown.
	 * @return Returns a configured AlertDialog.
	 */
	private AlertDialog CreateDialog(Context ct, ProgressType type, String title, String message)
	{
		AlertDialog.Builder aBuilder;
		if (type == ProgressType.OK)
		{
			aBuilder = new AlertDialog.Builder(ct, R.style.AlertDialog);
			aBuilder
				.setTitle(title)
				.setMessage(message)
				.setCancelable(false)
				.setPositiveButton(R.string.OkDialog, (dialog, which) -> Dismiss());
		}
		else
		{
			aBuilder = new AlertDialog.Builder(ct);
			aBuilder.setCancelable(false);
			switch (type)
			{
				case SEARCHING_PROGRESS:
					aBuilder.setView(R.layout.layout_progress_searching);
					break;
				case CONNECTING_PROGRESS:
					aBuilder.setView(R.layout.layout_progress_connecting);
					break;
				case CONNECTING_SERVER:
					aBuilder.setView(R.layout.layout_progress_connecting_server);
					break;
			}
		}
		return aBuilder.create();
	}

	//endregion
}
