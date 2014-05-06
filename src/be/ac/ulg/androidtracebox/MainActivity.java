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
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import be.ac.ulg.androidtracebox.core.APIPoster;
import be.ac.ulg.androidtracebox.core.MiscUtilities;
import be.ac.ulg.androidtracebox.core.MyLocation;
import be.ac.ulg.androidtracebox.core.TraceboxBackgroundService;
import be.ac.ulg.androidtracebox.core.TraceboxUtility;
import be.ac.ulg.androidtracebox.core.MyLocation.LocationResult;
import be.ac.ulg.androidtracebox.data.DatabaseHandler;
import be.ac.ulg.androidtracebox.data.Destination;
import be.ac.ulg.androidtracebox.data.Log;
import be.ac.ulg.androidtracebox.data.Probe;
import be.ac.ulg.androidtracebox.R;

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

	private void drawView()
	{
		// GET VALUES
		installed = sharedpreferences.getBoolean("systemInstalled", false);
		probing = sharedpreferences.getBoolean("probingEnabled", false);

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
		}
		else
		{
			statusButton.setEnabled(false);
			instantProbeButton.setEnabled(false);
			instantProbeButton.setBackgroundColor(Color.argb(255, 231, 76, 60)); // FLAT RED
		}

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

			try {
				DatabaseHandler db = new DatabaseHandler(this);

				final Destination randomDestination = db.getRandomDestination();
				instantProbeButton.setText("Probing: " + randomDestination.getName());

				// Show message
				new AlertDialog.Builder(this)
			    .setTitle("Instant probe")
			    .setMessage("Tracebox is going to probe " + randomDestination.getName() + ", you will have to accecpt the SU access in the prompt box.")
			    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
			        public void onClick(DialogInterface dialog, int which) {
			        	TraceboxerInstantProbe prober = (TraceboxerInstantProbe) new TraceboxerInstantProbe(randomDestination);
						prober.execute();

			        	progressDialog = ProgressDialog.show(
		                        MainActivity.this, "Please wait",
		                        "Tracebox is probing " + randomDestination.getName() + "...\nThis could take up to one minute.", true);
		                progressDialog.setCancelable(false);
			        }
			     })
			     .show();
			}
			catch (Exception e) {
				System.out.println("ERROR in busybox: " + e.getMessage());
			}
		}
		else
		{
			new AlertDialog.Builder(this)
		    .setTitle("Error")
		    .setMessage("You are not connected to internet by Wifi/Cellular. Tracebox can not send probes without a connection. Try later.")
		    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
		        public void onClick(DialogInterface dialog, int which) { 
		            // continue with delete
		        }
		     })
		     .show();
		}
	}

	public void endInstantProbe(final Probe p)
	{
		progressDialog.cancel();

		Vibrator v = (Vibrator) this.getSystemService(Context.VIBRATOR_SERVICE);
		v.vibrate(500); // Vibrate for 500 milliseconds
		instantProbing = false;
		this.drawView();

		if (p != null)
		{
			// SAVE THIS PROBE AND SEND IT TO THE SERVER
			p.setConnectivityMode(MiscUtilities.getConnectivityType(this)); // Save the connectivityMode
			p.setCarrierName(MiscUtilities.getCellularCarrierName(this));
			p.setCellularCarrierType(MiscUtilities.getCellularConnectionType(this));
			
			LocationResult locationResult = new LocationResult(){
			    @Override
			    public void gotLocation(Location location){
			        p.setLocation(location); // Save the location
			    }
			};
			MyLocation myLocation = new MyLocation();
			myLocation.getLocation(this, locationResult);

			// Save it
			db.addProbe(p);

			// Prepare the probe to be sent
			APIPoster poster = new APIPoster(this);
			poster.addProbe(p);

			// Save AS XML file
			if (poster.saveProbesAsXMLFile("out.xml"))
			{
				ProbePoster instantPoster = new ProbePoster(poster);
				instantPoster.execute();	
				// Save log
				db.addLog(new Log("Probed " + p.getDestination().getName()));
			}
			else
			{
				showDialogBox("ERROR", "The probe could not be saved, try again later.");
				// Save log
				db.addLog(new Log("Error while saving probe to " + p.getDestination().getName()));
			}
		}
		else
			showDialogBox("ERROR", "There was an error while the probing. Sorry.");
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
		private Destination dest;
		private Probe newProbe;

		public TraceboxerInstantProbe (Destination d)
		{
	        super();
	        dest = d;
	    }

		@Override
		protected Long doInBackground(URL... params)
		{
			TraceboxUtility tracebox = new TraceboxUtility(dest);
			newProbe = tracebox.doTracebox();
			return null;
		}

		@Override
		protected void onPostExecute(Long result)
		{
			endInstantProbe(newProbe);				
		}
	}

	private class ProbePoster extends AsyncTask<URL, Integer, Long>
	{
		private APIPoster poster;
		private boolean postOK = false;

		public ProbePoster (APIPoster p)
		{
	        super();
	        poster = p;
	    }

		@Override
		protected Long doInBackground(URL... params)
		{
			postOK = poster.tryToPostData();
			return null;
		}

		@Override
		protected void onPostExecute(Long result)
		{
			if (postOK)
				showDialogBox("Great", "Your probe has been submitted to the server and will be used for statistics, thank you.");
			else
				showDialogBox("ERROR", "There was an error while posting the data on the server. Please, try again later");
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
