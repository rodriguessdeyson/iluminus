package com.rad.Database;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.rad.Database.models.BluetoothDevicesModel;
import com.rad.Database.models.BlynkDeviceModel;
import com.rad.Database.models.ColorHistory;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;

import static com.rad.Utils.ViewUtils.FormatDateTime;

public class DatabaseAccess
{
	/**
	 * Local instance of SQLiteDatabase to manipulate the .db file.
	 */
	private SQLiteDatabase SQLiteDatabase;

	/**
	 * Activity context references.
	 */
	private Context AppContext;

	/**
	 * Name of the database to be opened.
	 */
	private String Database;

	/**
	 * Initialize an objective of type DatabaseAccess.
	 * @param ctx Activity context.
	 * @param database Database name to use.
	 */
	public DatabaseAccess(Context ctx, String database)
	{
		this.AppContext = ctx;
		this.Database = database;
	}

	/**
	 * Creates a new SQLite database.
	 */
	public void CreateDatabase()
	{
		// Create the database informed.
		SQLiteDatabase = AppContext
				.openOrCreateDatabase(Database, Context.MODE_PRIVATE, null);

		// Create a table to persist all the colors history.
		SQLiteDatabase.execSQL(
				"CREATE TABLE IF NOT EXISTS COLORHISTORY(" +
						"Id INTEGER PRIMARY KEY AUTOINCREMENT," +
						"A INTEGER," +
						"R INTEGER," +
						"G INTEGER," +
						"B INTEGER," +
						"DateTime TEXT)");

		// Create a table to configure the devices to connect.
		SQLiteDatabase.execSQL(
				"CREATE TABLE IF NOT EXISTS BLUETOOTHCONFIGURATION(" +
						"Id INTEGER PRIMARY KEY AUTOINCREMENT," +
						"DeviceName TEXT," +
						"DeviceAddress TEXT," +
						"ServiceUUID TEXT);");

		// Create a table to configure the devices to connect.
		SQLiteDatabase.execSQL(
				"CREATE TABLE IF NOT EXISTS BLYNKCONFIGURATION(" +
						"Id INTEGER PRIMARY KEY AUTOINCREMENT," +
						"Name        TEXT," +
						"AuthToken   TEXT," +
						"MergedRGB BOOLEAN,"+
						"PINR        TEXT," +
						"PING        TEXT," +
						"PINB        TEXT," +
						"PIN   TEXT);");

		// Create a table to manipulate add show.
		SQLiteDatabase.execSQL(
				"CREATE TABLE IF NOT EXISTS ADMONITORING(" +
						"Id             INTEGER PRIMARY KEY AUTOINCREMENT," +
						"ShowedTime     TEXT NOT NULL)");
	}

	/**
	 * Opens an existing database by its name.
	 */
	private void OpenDatabase()
	{
		SQLiteDatabase = AppContext
				.openOrCreateDatabase(Database, Context.MODE_PRIVATE, null);
	}

	/**
	 * Closes an existing opened database.
	 */
	private void CloseDatabase()
	{
		if (SQLiteDatabase.isOpen())
			SQLiteDatabase.close();
	}

	/**
	 * Inserts into opened database a new configuration.
	 * @param configuration A configuration of the device.
	 */
	public void Insert(BluetoothDevicesModel configuration)
	{
		OpenDatabase();
		SQLiteDatabase.execSQL(
				"INSERT INTO BLUETOOTHCONFIGURATION(" +
						"DeviceName,"        +
						"DeviceAddress,"     +
						"ServiceUUID)"       +
						"VALUES("            +
						"'"+ configuration.getDeviceName()    +"'," +
						"'"+ configuration.getDeviceAddress() +"'," +
						"'"+ configuration.getServiceUUID()   +"')" + ";");
		CloseDatabase();
	}

	/**
	 * Inserts into opened database a new blynk configuration.
	 * @param configuration a brand new configuration
	 */
	public void Insert(BlynkDeviceModel configuration)
	{
		OpenDatabase();
		SQLiteDatabase.execSQL(
			"INSERT INTO BLYNKCONFIGURATION(" +
				"Name,"      +
				"AuthToken," +
				"MergedRGB," +
				"PINR,"      +
				"PING,"      +
				"PINB,"      +
				"PIN)"       +
				"VALUES("            +
					"'"+ configuration.getName()  + "'," +
					"'"+ configuration.getAuth()  + "'," +
					"'"+ configuration.isMerged() + "'," +
					"'"+ configuration.getPinR()  + "'," +
					"'"+ configuration.getPinG()  + "'," +
					"'"+ configuration.getPinB()  + "'," +
					"'"+ configuration.getPin()   + "')" + ";");
		CloseDatabase();
	}

	/**
	 * Insert a color to color history in database.
	 */
	public void Insert(ColorHistory colorHistory)
	{
		OpenDatabase();
		SQLiteDatabase.execSQL(""            +
				"INSERT INTO COLORHISTORY("  +
				"A,"                         +
				"R,"                         +
				"G,"                         +
				"B,"                         +
				"DateTime)"                  +
				"VALUES("                    +
				"'"+ colorHistory.getA() +"',"+
				"'"+ colorHistory.getR() +"',"+
				"'"+ colorHistory.getG() +"',"+
				"'"+ colorHistory.getB() +"',"+
				"'"+ FormatDateTime() +"')"+";");
		CloseDatabase();
	}

	/**
	 * Inserts the dateTime that an ad was showed.
	 * @param dateTime Time in text of showing.
	 */
	public void Insert(String dateTime)
	{
		OpenDatabase();
		SQLiteDatabase.execSQL(
				"INSERT INTO ADMONITORING(" +
						"ShowedTime)"    +
						"VALUES("           +
						"'"+ dateTime +"')" + ";");
		CloseDatabase();
	}

	/**
	 * Delete all register from database.
	 */
	public void DeleteColorHistory()
	{
		OpenDatabase();
		SQLiteDatabase.delete("COLORHISTORY", null, null);
		SQLiteDatabase.execSQL("VACUUM");
		CloseDatabase();
	}

	/**
	 * Reads the whole color history.
	 * @return A list with all colors saved.
	 */
	public ArrayList<ColorHistory> ReadColorHistory()
	{
		// Create an configuration list with all devices configured.
		ArrayList<ColorHistory> colors = new ArrayList<>();

		// Creates the query to retrieve all devices.
		String selectQuery = "SELECT * FROM COLORHISTORY;";

		// Get all devices configured.
		OpenDatabase();
		Cursor dbCursor = SQLiteDatabase.rawQuery(selectQuery, null);

		// If rows exist, get the values.
		if (dbCursor.getCount() > 0)
		{
			while(dbCursor.moveToNext())
			{
				ColorHistory color = new ColorHistory();
				color.setId(dbCursor.getInt(0));
				color.setA(dbCursor.getInt(1));
				color.setR(dbCursor.getInt(2));
				color.setG(dbCursor.getInt(3));
				color.setB(dbCursor.getInt(4));
				color.setSavedTime(dbCursor.getString(5));
				colors.add(color);
			}
		}
		dbCursor.close();
		CloseDatabase();
		return colors;
	}

	/**
	 * Reades the configurations already in database.
	 * @return A list of configurations.
	 */
	public ArrayList<BluetoothDevicesModel> ReadBluetoothConfiguration()
	{
		// Create an configuration list with all devices configured.
		ArrayList<BluetoothDevicesModel> configurations = new ArrayList<>();

		// Creates the query to retrieve all devices.
		String selectQuery = "SELECT * FROM BLUETOOTHCONFIGURATION;";

		// Get all devices configured.
		OpenDatabase();
		Cursor dbCursor = SQLiteDatabase.rawQuery(selectQuery, null);

		// If rows exist, get the values.
		if (dbCursor.getCount() > 0)
		{
			while(dbCursor.moveToNext())
			{
				BluetoothDevicesModel configuration = new BluetoothDevicesModel();
				configuration.setId(dbCursor.getInt(0));
				configuration.setDeviceName(dbCursor.getString(1));
				configuration.setDeviceAddress(dbCursor.getString(2));
				configuration.setServiceUUID(UUID.fromString(dbCursor.getString(3)));
				configurations.add(configuration);
			}
		}
		dbCursor.close();
		return configurations;
	}

	/**
	 * Reads the blynk configurations already in database.
	 * @return A list of configurations.
	 */
	public ArrayList<BlynkDeviceModel> ReadBlynkConfiguration()
	{
		// Create an configuration list with all devices configured.
		ArrayList<BlynkDeviceModel> configurations = new ArrayList<>();

		// Creates the query to retrieve all devices.
		String selectQuery = "SELECT * FROM BLYNKCONFIGURATION;";

		// Get all devices configured.
		OpenDatabase();
		Cursor dbCursor = SQLiteDatabase.rawQuery(selectQuery, null);

		// If rows exist, get the values.
		if (dbCursor.getCount() > 0)
		{
			while(dbCursor.moveToNext())
			{
				BlynkDeviceModel configuration = new BlynkDeviceModel();
				configuration.setId(dbCursor.getInt(     0));
				configuration.setName(dbCursor.getString(1));
				configuration.setAuth(dbCursor.getString(2));
				configuration.setMerged(dbCursor.getInt( 3));
				configuration.setPinR(dbCursor.getString(4));
				configuration.setPinG(dbCursor.getString(5));
				configuration.setPinB(dbCursor.getString(6));
				configuration.setPin(dbCursor.getString( 7));
				configurations.add(configuration);
			}
		}
		dbCursor.close();
		CloseDatabase();
		return configurations;
	}

	/**
	 * Deletes one of the row.
	 * @param id Id of the device.
	 */
	public void DeleteBlynkSettingById(int id)
	{
		OpenDatabase();
		String table = "BLYNKCONFIGURATION";
		String whereClause = "Id=?";
		String[] whereArgs = new String[] { String.valueOf(id) };
		SQLiteDatabase.delete(table, whereClause, whereArgs);
		CloseDatabase();
	}

	/**
	 * Updates the settings of blynk device.
	 * @param blynkModel Blynk setting
	 */
	public void UpdateBlynk(BlynkDeviceModel blynkModel)
	{
		OpenDatabase();
		String table = "BLYNKCONFIGURATION";
		String whereClause = "Id=?";
		String[] whereArgs = new String[] { String.valueOf(blynkModel.getId()) };
		ContentValues contentValues = new ContentValues();
		contentValues.put("Name",      blynkModel.getName());
		contentValues.put("AuthToken", blynkModel.getAuth());
		contentValues.put("MergedRGB", blynkModel.isMerged());
		contentValues.put("PINR",      blynkModel.getPinR());
		contentValues.put("PING",      blynkModel.getPinG());
		contentValues.put("PINB",      blynkModel.getPinB());
		contentValues.put("PIN",       blynkModel.getPin());
		SQLiteDatabase.update(table, contentValues, whereClause, whereArgs);
		CloseDatabase();
	}

	/**
	 * Updates the dateTime that an ad was showed.
	 */
	public void UpdateLastAdShown()
	{
		@SuppressLint("SimpleDateFormat")
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
		Date date = new Date();
		OpenDatabase();
		SQLiteDatabase.execSQL(
				"UPDATE ADMONITORING SET " +
						"ShowedTime =" + "'" + dateFormat.format(date) + "'" +
						"WHERE Id =" + "'" + 1 + "'" + ";");
		CloseDatabase();
	}

	/**
	 * Gets the last time an ad was shown
	 * @return THe DateTime of shown;
	 */
	public Date GetTimeLastAdShowed()
	{
		Date lastShowed = null;
		String selectQuery = "SELECT * FROM ADMONITORING;";

		OpenDatabase();
		Cursor dbCursor = SQLiteDatabase.rawQuery(selectQuery, null);

		// If rows exist, get the values.
		if (dbCursor.getCount() > 0)
		{
			@SuppressLint("SimpleDateFormat")
			SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
			try
			{
				while (dbCursor.moveToNext())
				{
					lastShowed = format.parse(dbCursor.getString(1));
				}
			}
			catch (ParseException e)
			{
				e.printStackTrace();
			}
		}
		dbCursor.close();
		CloseDatabase();
		return lastShowed;
	}
}
