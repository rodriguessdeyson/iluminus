package com.rad.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.rad.Database.models.BlynkDeviceModel;
import com.rad.Interfaces.IClickListener;
import com.rad.iluminus.R;

import org.jetbrains.annotations.NotNull;

import java.lang.ref.WeakReference;
import java.util.List;

import static com.rad.Utils.ResourcesUtils.GetString;

/**
 * Custom RecyclerView adapter to manipulate custom layout.
 */
public class DeviceListAdapter<T> extends RecyclerView.Adapter<DeviceListAdapter.DeviceViewHolder>
{
	//region Interfaces

	/**
	 * Interface to implements view click.
	 */
	private final IClickListener IClickListener;

	//endregion

	//region Attributes

	/**
	 * Instantiate an XML into a corresponding View.
	 */
	private LayoutInflater ViewInflater;

	/**
	 * Data set list with all devices saved.
	 */
	private List<T> Devices;

	//endregion

	//region Constructor

	/**
	 * Initiates a new object of type GroceryListAdapter.
	 * @param ctx Application context.
	 * @param devices List with all groceries.
	 * @param listener IClickListener reference.
	 */
	public DeviceListAdapter(Context ctx, List<T> devices, IClickListener listener)
	{
		this.Devices                    = devices;
		this.ViewInflater               = LayoutInflater.from(ctx);
		this.IClickListener             = listener;
	}

	//endregion

	//region Methods/Overrides

	/**
	 * Initializes the ViewHolder structure.
	 * @param parent ViewGroup parent.
	 * @param viewType Type of view.
	 * @return A new instance of ViewHolder element.
	 */
	@NotNull
	@Override
	public DeviceViewHolder onCreateViewHolder(@NotNull ViewGroup parent, int viewType)
	{
		View view = null;
		if (Devices.get(0) instanceof BlynkDeviceModel)
			view = ViewInflater.inflate(R.layout.layout_blynk_item, parent, false);
		return new DeviceViewHolder(view, IClickListener);
	}

	/**
	 * Allows to bind the desired layout/view and its data.
	 * @param holder The ViewHolder created.
	 * @param position The desired position.
	 */
	@Override
	public void onBindViewHolder(@NotNull DeviceViewHolder holder, int position)
	{
		if (Devices.get(0) instanceof BlynkDeviceModel)
		{
			BlynkDeviceModel currentItem = (BlynkDeviceModel)Devices.get(position);
			holder.SetData(currentItem);
		}
	}

	/**
	 * Gets the quantity of elements bind.
	 * @return The number (count) of elements bind.
	 */
	@Override
	public int getItemCount()
	{
		return Devices.size();
	}

	//endregion

	//region Custom Class

	/**
	 * Class to handle the RecyclerView.ViewHolder.
	 */
	public static class DeviceViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener
	{
		//region Attributes

		/**
		 * WeakReference to IClickListener.
		 */
		private WeakReference<IClickListener> IClickListenerRef;

		/**
		 * EditText reference to device name.
		 */
		private TextView TextViewDeviceName;

		/**
		 * ImageView reference to imageView to delete the current item.
		 */
		private ImageView ImageViewDeleteDevice;

		/**
		 * ImageView reference to imageView to edit the current item.
		 */
		private ImageView ImageViewEditDevice;

		/**
		 * TextView reference to app auth token.
		 */
		private TextView TextViewApplicationAuthToken;

		//endregion

		//region Constructor

		/**
		 * Initializes a new object of type GroceryViewHolder.
		 * @param itemView Item view.
		 * @param listener IClickListener reference.
		 */
		public DeviceViewHolder(View itemView, IClickListener listener)
		{
			super(itemView);
			IClickListenerRef = new WeakReference<>(listener);

			// Initialize Views.
			InitializeControlViews();

			// Manipulates the views events.
			InitializeControlViewsEvents();
		}

		//endregion

		//region Methods

		/**
		 * Initialize the views defined in user interface.
		 */
		private void InitializeControlViews()
		{
			TextViewDeviceName           = itemView.findViewById(R.id.TextViewDeviceName);
			ImageViewDeleteDevice        = itemView.findViewById(R.id.ImageViewDeleteItem);
			ImageViewEditDevice          = itemView.findViewById(R.id.ImageViewEditItem);
			TextViewApplicationAuthToken = itemView.findViewById(R.id.TextViewAppAuthToken);
		}

		/**
		 * Initialize the views events to manipulate users control.
		 */
		private void InitializeControlViewsEvents()
		{
			// Initialize this listener by informing that the View.OnClick... it is implemented.
			ImageViewDeleteDevice.setOnClickListener(this);
			ImageViewEditDevice.setOnClickListener(this);
		}

		/**
		 * Runs the on click event.
		 * @param v The current view.
		 */
		@Override
		public void onClick(View v)
		{
			IClickListenerRef.get().onPositionClicked(v, ((BlynkDeviceModel)v.getTag()).getId());
		}

		/**
		 * Sets adapter value
		 * @param currentItem Current item.
		 */
		public <T> void SetData(T currentItem)
		{
			if (currentItem instanceof BlynkDeviceModel)
			{
				String authToken = GetString(R.string.BlynkDeviceAuthToken);
				String deviceName = GetString(R.string.BlynkDeviceName);
				this.TextViewApplicationAuthToken.setText(String.format(authToken, ((BlynkDeviceModel)currentItem)
					.getAuth()));
				this.TextViewDeviceName.setText(String.format(deviceName, ((BlynkDeviceModel)currentItem).getName()));
				ImageViewDeleteDevice.setTag(currentItem);
				ImageViewEditDevice.setTag(currentItem);
			}
		}
	}
	//endregion

	//endregion
}
