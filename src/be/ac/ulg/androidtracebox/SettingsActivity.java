package be.ac.ulg.androidtracebox;

import be.ac.ulg.androidtracebox.R;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.View;
import android.widget.Button;

public class SettingsActivity extends Activity {

	private SharedPreferences sharedpreferences;
	private Editor editor;
	private boolean probing;
	private int frequency;
	private int numberOfDestinations;
	private int maxDuration;

	@SuppressLint("CommitPrefEdits")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_settings);

		setTitle("Tracebox for Android - Settings");

		// SET PREFERENCES STUFF
		sharedpreferences = PreferenceManager.getDefaultSharedPreferences(this);
		editor = sharedpreferences.edit();

		this.drawView();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	public void drawView()
	{
		// Status button
		probing = sharedpreferences.getBoolean("probingEnabled", false);
		Button statusButton = (Button) this.findViewById(R.id.settings_status_button);
		if (this.probing)
		{
			statusButton.setBackgroundColor(Color.argb(255, 46, 204, 113)); // FLAT GREEN
			statusButton.setText(R.string.main_status_online_button);			
		}
		else
		{
			statusButton.setBackgroundColor(Color.argb(255, 231, 76, 60)); // FLAT RED
			statusButton.setText(R.string.main_status_offline_button);
		}

		// Frequency
		frequency = sharedpreferences.getInt("frequency", 10);
		Button frequencyButton = (Button) this.findViewById(R.id.settings_frequency_button);
		frequencyButton.setText(frequency + "/day");

		// # of destinations
		numberOfDestinations = sharedpreferences.getInt("numberOfDestinations", 5);
		Button numberOfDestinationsButton = (Button) this.findViewById(R.id.settings_number_of_destinations_button);
		numberOfDestinationsButton.setText(numberOfDestinations + " dest");

		// Max duration
		maxDuration = sharedpreferences.getInt("maxDuration", 120);
		Button maxDurationButton = (Button) this.findViewById(R.id.settings_max_duration_button);
		maxDurationButton.setText(maxDuration + " s");
	}

	/*
	 * BUTTONS ACTIONS
	 */
	public void change_frequency_plus(View view)
	{
		if (frequency * maxDuration < 24 * 3600)
		{
			frequency = frequency + 1;
			editor.putInt("frequency", frequency);
			editor.commit();
			this.drawView();
		}
	}

	public void change_frequency_minus(View view)
	{
		if (frequency > 1)
		{
			frequency = frequency - 1;
			editor.putInt("frequency", frequency);
			editor.commit();
			this.drawView();
		}
	}

	public void change_number_of_destinations_plus(View view)
	{
		if (numberOfDestinations < 50)
		{
			numberOfDestinations = numberOfDestinations + 1;
			editor.putInt("numberOfDestinations", numberOfDestinations);
			editor.commit();
			this.drawView();
		}
	}

	public void change_number_of_destinations_minus(View view)
	{
		if (numberOfDestinations > 1)
		{
			numberOfDestinations = numberOfDestinations - 1;
			editor.putInt("numberOfDestinations", numberOfDestinations);
			editor.commit();
			this.drawView();
		}
	}

	public void change_max_duration_plus(View view)
	{
		if (maxDuration * frequency < 24 * 3600)
		{
			maxDuration = maxDuration + 10;
			editor.putInt("maxDuration", maxDuration);
			editor.commit();
			this.drawView();
		}
	}

	public void change_max_duration_minus(View view)
	{
		if (maxDuration > 10)
		{
			maxDuration = maxDuration - 10;
			editor.putInt("maxDuration", maxDuration);
			editor.commit();
			this.drawView();
		}
	}
}
