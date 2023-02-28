package com.rad.Interfaces;

import android.view.View;

/**
 * Interface to handle clicks in RecyclerView Adapter.
 */
public interface IClickListener
{
	/**
	 * Fires the Item click event.
	 * @param v View the fired the interface.
	 * @param itemDbId Id of the selected device in database.
	 */
	void onPositionClicked(View v, int itemDbId);
}
