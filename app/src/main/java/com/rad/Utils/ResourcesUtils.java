package com.rad.Utils;

import android.app.Application;
import android.content.res.Resources;

/**
 * Auxiliary class to manipulate string resources outside of Activity/Context class.
 */
public class ResourcesUtils extends Application
{
	/**
	 * Attribute to resource reference.
	 */
	private static Resources Resources;

	/**
	 * Application class constructor.
	 */
	@Override
	public void onCreate()
	{
		super.onCreate();
		Resources = getResources();
	}

	/**
	 * Method to return as string format a resources using its id.
	 * @param resource Id of the string resource.
	 * @return The string value of the string resource id
	 */
	public static String GetString(int resource)
	{
		return Resources.getString(resource);
	}
}
