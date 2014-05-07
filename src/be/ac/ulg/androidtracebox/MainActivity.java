/*
 * Tracebox for Android has been developed by Valentin THIRION
 * in the context of his Master Thesis
 * at the University of Liege (ULg) in Belgium in june 2014.
 * This work has been partially funded by the
 * European Commission funded mPlane ICT-318627 project
 * (http://www.ict-mplane.eu).
 * 
 * All information, copyrights and code about
 * this project can be found at: www.androidtracebox.com
 */

package be.ac.ulg.androidtracebox;

import java.net.URL;
import java.util.Calendar;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import be.ac.ulg.androidtracebox.core.MiscUtilities;
import be.ac.ulg.androidtracebox.core.TraceboxBackgroundService;
import be.ac.ulg.androidtracebox.core.TraceboxUtility;
import be.ac.ulg.androidtracebox.data.DatabaseHandler;
import be.ac.ulg.androidtracebox.data.Destination;

public class MainActivity extends Activity {

	private boolean installed = false;
	private boolean probing = false;
	private boolean instantProbing = false;

	private DatabaseHandler db;
	
	private SharedPreferences sharedpreferences;
	private Editor editor;

	private Button instantProbeButton;
	//private TextView nextProbeTitle;
	private ProgressDialog progressDialog;

	private AlarmManager alarm;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		setTitle("Tracebox for Android");

		db = new DatabaseHandler(this);

		// SET PREFERENCES STUFF
		sharedpreferences = PreferenceManager.getDefaultSharedPreferences(this);
        editor = sharedpreferences.edit();

        this.drawView();

		// Launch installer if not installed
		if (!installed == true)
		{
			Intent installationItent = new Intent(this, InstallationActivity.class);
			startActivity(installationItent);
		}		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public void onResume()
	{
		this.drawView();
	    super.onResume();
	}

	public void getAppStatus()
	{
		// GET VALUES
		installed = sharedpreferences.getBoolean("systemInstalled", false);
		probing = sharedpreferences.getBoolean("probingEnabled", false);

		if (installed)
		{
			int installedBusyboxVersion = sharedpreferences.getInt("installedBusyboxVersion", 0);
			String lastBusyboxVersion = getResources().getString(R.string.busybox_version);
			int lastBusyboxVersionInt;
			try {
				lastBusyboxVersionInt= Integer.parseInt(lastBusyboxVersion);
				if (installedBusyboxVersion < lastBusyboxVersionInt)
				{
					installed = false;
					probing = false;
					editor.putBoolean("probingEnabled", false);
				}
			}
			catch (Exception e)
			{
				installed = false;
				probing = false;
				editor.putBoolean("probingEnabled", false);
			}
		}
		
	}

	private void drawView()
	{
		this.getAppStatus();

		Button statusButton = (Button) this.findViewById(R.id.status_button);
		Button installationButton = (Button) this.findViewById(R.id.installation_button);
		instantProbeButton = (Button) this.findViewById(R.id.instant_probe_button);
		//nextProbeTitle = (TextView) this.findViewById(R.id.next_probe_title);

		// INSTALLATION
		if (this.installed)
		{
			installationButton.setBackgroundColor(Color.argb(255, 46, 204, 113)); // FLAT GREEN
			installationButton.setText(R.string.main_installation_installed_button);
			statusButton.setEnabled(true);
			instantProbeButton.setEnabled(true);
			if (MiscUtilities.isConnected(this))
				instantProbeButton.setBackgroundColor(Color.argb(255, 46, 204, 113)); // FLAT GREEN
			else
				instantProbeButton.setBackgroundColor(Color.argb(255, 231, 76, 60)); // FLAT RED

			if (instantProbing)
			{
				instantProbeButton.setEnabled(false);
				instantProbeButton.setBackgroundColor(Color.argb(255, 230, 126, 34)); // FLAT ORANGE
				//nextProbeTitle.setVisibility(View.VISIBLE);
			}
			else
			{
				instantProbeButton.setText(R.string.main_instant_probe_button);
				instantProbeButton.setEnabled(true);
				instantProbeButton.setBackgroundColor(Color.argb(255, 46, 204, 113)); // FLAT GREEN
			}

			if (this.probing)
			{
				statusButton.setBackgroundColor(Color.argb(255, 46, 204, 113)); // FLAT GREEN
				statusButton.setText(R.string.main_status_online_button);
			}
			else
			{
				statusButton.setBackgroundColor(Color.argb(255, 231, 76, 60)); // FLAT RED
				statusButton.setText(R.string.main_status_offline_button);
				//nextProbeTitle.setVisibility(View.INVISIBLE);
			}
		}
		else
		{
			System.out.println("TEST");
			statusButton.setEnabled(false);
			instantProbeButton.setEnabled(false);
			instantProbeButton.setBackgroundColor(Color.argb(255, 231, 76, 60)); // FLAT RED
		}
	}

	/*
	 * BUTTONS ACTIONS
	 */
	public void open_settings_page(View view)
	{
		Intent intent = new Intent(this, SettingsActivity.class);
		startActivity(intent);
	}

	public void open_destinations_page(View view)
	{
		Intent intent = new Intent(this, DestinationsActivity.class);
		startActivity(intent);
	}

	public void open_results_page(View view)
	{
		Intent intent = new Intent(this, ResultsActivity.class);
		startActivity(intent);
	}

	public void open_installation_page(View view)
	{
		if (MiscUtilities.isConnected(this))
		{
			Intent intent = new Intent(this, InstallationActivity.class);
			startActivity(intent);
		}
	}

	public void turn_probing_on_off(View view)
	{
		if (probing)
		{
			System.out.println("TURN PROBING OFF");

			//Intent intent = new Intent(this, TraceboxBackgroundService.class);
			//stopService(intent);
			//PendingIntent pintent = PendingIntent.getService(this, 0, intent, 0);
			//alarm.cancel(pintent);

			editor.putBoolean("probingEnabled", false);
		}
		else
		{
			System.out.println("TURN PROBING ON");

			int interval = (24 * 3600 / sharedpreferences.getInt("frequency", 10));
			int maxDuration = 0;
			//int numberOfDestinations = 0;
			alarm = (AlarmManager)getSystemService(Context.ALARM_SERVICE);

			TraceboxerBackgroundProbe backgroundProber = new TraceboxerBackgroundProbe(this, alarm, interval, maxDuration);
			backgroundProber.execute();

			editor.putBoolean("probingEnabled", true);
		}

		editor.commit();
		this.drawView();
	}

	public void show_about_page(View view)
	{
		Intent installationItent = new Intent(this, AboutActivity.class);
		startActivity(installationItent);
	}

	public void send_instant_probe(View view)
	{
		if (MiscUtilities.isConnected(this))
		{
			instantProbing = true;

			final Context thisOne = this;

			// Show message
			new AlertDialog.Builder(this)
		    .setTitle("Instant probe")
		    .setMessage("Tracebox is going to probe a random destination, you will have to accecpt the SU access in the prompt box.")
		    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
		        public void onClick(DialogInterface dialog, int which) {

		        	// Launch the prober
		        	TraceboxerInstantProbe prober = (TraceboxerInstantProbe) new TraceboxerInstantProbe(thisOne);
		        	Destination d = prober.getDestination();
					prober.execute();

		        	progressDialog = ProgressDialog.show(
	                        MainActivity.this, "Please wait",
	                        "Tracebox is probing " + d.getName() + "...\nThis could take up to one minute.", true);
	                progressDialog.setCancelable(false);
		        }
		     })
		     .show();
		}
		else
		{
			this.showDialogBox("Error", "You are not connected to internet by Wifi/Cellular. Tracebox can not send probes without a connection. Try later.");
		}
	}

	public void endInstantProbe(int probeResult)
	{
		progressDialog.cancel();

		Vibrator v = (Vibrator) this.getSystemService(Context.VIBRATOR_SERVICE);
		v.vibrate(500); // Vibrate for 500 milliseconds
		instantProbing = false;
		this.drawView();

		switch (probeResult)
		{
		case 1:
			showDialogBox("Great", "Your probe has been submitted to the server and will be used for statistics, thank you.");
			break;
		case 0:
			break;
		case -1:
			showDialogBox("ERROR", "There was an error while the probing. Sorry.");
			break;
		case -2:
			showDialogBox("ERROR", "The probe could not be saved, try again later.");
			break;
		case -3:
			showDialogBox("ERROR", "There was an error while posting the data on the server. Please, try again later");
			break;
		}
	}

	private void showDialogBox(String title, String message)
	{
		new AlertDialog.Builder(this)
	    .setTitle(title)
	    .setMessage(message)
	    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
	        public void onClick(DialogInterface dialog, int which) { 
	            // continue with delete
	        }
	     })
	     .show();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event)
	{
		if (keyCode == KeyEvent.KEYCODE_MENU)
			return false;
		if (keyCode == KeyEvent.KEYCODE_SEARCH)
			return false;
		if (keyCode == KeyEvent.KEYCODE_SETTINGS) 
			return false;
		return super.onKeyDown(keyCode, event);
	}

	private class TraceboxerInstantProbe extends AsyncTask<URL, Integer, Long>
	{
		private int probeResult = 0;
		private Context context;
		private TraceboxUtility tracebox;

		public TraceboxerInstantProbe (final Context c)
		{
	        super();
	        context = c;
	        tracebox = new TraceboxUtility(context);
	    }

		@Override
		protected Long doInBackground(URL... params)
		{
			probeResult = tracebox.doTraceboxAndPost();
			return null;
		}

		public Destination getDestination()
		{
			return tracebox.getDestination();
		}

		@Override
		protected void onPostExecute(Long result)
		{
			endInstantProbe(probeResult);				
		}
	}

	private class TraceboxerBackgroundProbe extends AsyncTask<URL, Integer, Long>
	{
		private Context context;
		private Calendar cal;
		private AlarmManager alarm;
		int interval;
		int maxDuration;

		public TraceboxerBackgroundProbe (Context c, AlarmManager a, int i, int m)
		{
	        super();
	        context = c;
	        alarm = a;
	        interval = i;
	        maxDuration = m;
	        cal = Calendar.getInstance();
	    }

		@Override
		protected Long doInBackground(URL... params)
		{
			Intent intent = new Intent(context, TraceboxBackgroundService.class);
			PendingIntent pintent = PendingIntent.getService(context, 0, intent, 0);

			alarm.setRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), interval * 1000, pintent);
			return null;
		}
	}
}
