package de.jurihock.voicesmith.activities;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import de.jurihock.voicesmith.DAFX;
import de.jurihock.voicesmith.R;
import de.jurihock.voicesmith.Utils;
import de.jurihock.voicesmith.audio.HeadsetMode;
import de.jurihock.voicesmith.services.DafxService;
import de.jurihock.voicesmith.services.ServiceListener;
import de.jurihock.voicesmith.widgets.ColoredToggleButton;
import de.jurihock.voicesmith.widgets.DafxPicker;
import de.jurihock.voicesmith.widgets.IntervalPicker;

public final class DafxActivity extends AudioServiceActivity<DafxService>
	implements
	PropertyChangeListener, OnClickListener,
	OnCheckedChangeListener, ServiceListener
{
	// Relevant activity widgets:
	private DafxPicker			viewDafxPicker			= null;
	private IntervalPicker		viewIntervalPicker		= null;
	private CheckBox			viewBluetoothHeadset	= null;
	private ColoredToggleButton	viewStartStopButton		= null;

	public DafxActivity()
	{
		super(DafxService.class);
	}

	/**
	 * Initializes the activity, its layout and widgets.
	 * */
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		this.setActionBarContentView(R.layout.dafx);

		viewDafxPicker = (DafxPicker) this.findViewById(R.id.viewDafxPicker);
		viewDafxPicker.setPropertyChangeListener(this);

		viewIntervalPicker = (IntervalPicker) this.findViewById(
			R.id.viewIntervalPicker);
		viewIntervalPicker.setPropertyChangeListener(this);

		viewBluetoothHeadset = (CheckBox) this.findViewById(
			R.id.viewBluetoothHeadset);
		viewBluetoothHeadset.setOnCheckedChangeListener(this);

		viewStartStopButton = (ColoredToggleButton) this.findViewById(
			R.id.viewStartStopButton);
		// set red if checked otherwise white
		viewStartStopButton.setOnClickListener(this);
	}

	@Override
	protected void onServiceConnected()
	{
		Utils.log("%s was connected to its service.",
			this.getClass().getName());

		getService().setActivityVisible(true, this.getClass());
		getService().setListener(this);

		// Update widgets
		viewDafxPicker.setDafx(getService().getDafx());
		viewBluetoothHeadset.setChecked(getService().getHeadsetMode()
			== HeadsetMode.BLUETOOTH_HEADSET);
		viewStartStopButton.setChecked(getService().isThreadRunning());

		if (getService().getDafx() == DAFX.Transpose)
		{
			viewIntervalPicker.setVisibility(View.VISIBLE);

			int interval = Integer.parseInt(
				getService().getThreadParams()[0].toString());
			viewIntervalPicker.setInterval(interval);
		}
		else
		{
			viewIntervalPicker.setVisibility(View.GONE);
		}
	}

	@Override
	protected void onServiceDisconnected()
	{
		Utils.log("%s was disconnected from its service.",
			this.getClass().getName());

		if (!this.isFinishing())
		{
			getService().setActivityVisible(false, this.getClass());
		}

		getService().setListener(null);
	}

	public void onClick(View view)
	{
		if (getService().isThreadRunning())
		{
			if (viewStartStopButton.isChecked())
				viewStartStopButton.setChecked(false);

			getService().stopThread(false);
		}
		else
		{
			if (!viewStartStopButton.isChecked())
				viewStartStopButton.setChecked(true);

			getService().startThread();
		}

		// BZZZTT!!1!
		viewStartStopButton.performHapticFeedback(0);
	}

	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
	{
		if (isChecked && getService().getHeadsetMode()
				== HeadsetMode.WIRED_HEADSET)
		{
			getService().setHeadsetMode(HeadsetMode.BLUETOOTH_HEADSET);
		}
		else if (!isChecked && getService().getHeadsetMode()
				== HeadsetMode.BLUETOOTH_HEADSET)
		{
			getService().setHeadsetMode(HeadsetMode.WIRED_HEADSET);
		}
	}

	@Override
	public void propertyChange(PropertyChangeEvent event)
	{
		if (event.getSource().equals(viewDafxPicker))
		{
			DAFX dafx = viewDafxPicker.getDafx();

			getService().setDafx(dafx);

			if (dafx == DAFX.Transpose)
			{
				viewIntervalPicker.setVisibility(View.VISIBLE);

				getService().setThreadParams(
					viewIntervalPicker.getInterval());
			}
			else
			{
				viewIntervalPicker.setVisibility(View.GONE);
			}
		}
		else if (event.getSource().equals(viewIntervalPicker))
		{
			int interval = viewIntervalPicker.getInterval();

			getService().setThreadParams(interval);
		}
	}
	
	public void onServiceFailed()
	{
		if (viewStartStopButton.isChecked())
			viewStartStopButton.setChecked(false);

		Utils.log(this, getString(R.string.ServiceFailureMessage));

		// BZZZTT!!1!
		viewStartStopButton.performHapticFeedback(0);
	}
}
